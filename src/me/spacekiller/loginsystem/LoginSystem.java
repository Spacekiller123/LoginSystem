package me.spacekiller.loginsystem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import me.spacekiller.loginsystem.commands.*;
import me.spacekiller.loginsystem.data.Converter;
import me.spacekiller.loginsystem.data.Converter.FileType;
import me.spacekiller.loginsystem.data.DataManager;
import me.spacekiller.loginsystem.data.MySQL;
import me.spacekiller.loginsystem.data.SQLite;
import me.spacekiller.loginsystem.encryption.EncryptionType;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LoginSystem extends JavaPlugin {

	public DataManager data;
	public static LoginSystem instance;
	public Map<String, Boolean> authList = new HashMap<String, Boolean>();
	public Map<String, Location> loginLocations = new HashMap<String, Location>();
	public List<String> messaging = new ArrayList<String>();
	public boolean required, blindness, sesUse, timeUse, spawntp;
	public int sesDelay, timeDelay;
	public static final Logger log = Logger.getLogger("Minecraft");
	public ThreadManager thread;
	public String prefix;
	public EncryptionType hasher;
	public Map<String, CommandExecutor> commandMap = new HashMap<String, CommandExecutor>();
	public static int PHP_VERSION;
	public static String encoder;
	private static Logger serverLog;
	private CommandFilter commandFilter = new CommandFilter();
	
	public static File session = new File("plugins/LoginSystem", "Session.yml");
	public static FileConfiguration cfg = YamlConfiguration.loadConfiguration(session);
	
	@Override
	public void onEnable() {
		
		//setup quickcalls
		FileConfiguration config = this.getConfig();
		PluginManager pm = this.getServer().getPluginManager();

		//setup config
		config.addDefault("settings.password-required", false);
		config.addDefault("settings.encryption", "BCRYPT");
		config.addDefault("settings.encoder", "UTF-8");
		config.addDefault("settings.PHP_VERSION", 4);
		config.addDefault("settings.messager-api", true);
		config.addDefault("settings.blindness", true);
		config.addDefault("settings.fake-location", false);
		config.addDefault("settings.session.use", true);
		config.addDefault("settings.session.timeout (sec)", 60);
		config.addDefault("settings.timeout.use", true);
		config.addDefault("settings.timeout.timeout (sec)", 60);
		config.addDefault("settings.table prefix", "ls_");
		config.addDefault("MySQL.use", false);
		config.addDefault("MySQL.host", "localhost");
		config.addDefault("MySQL.port", 3306);
		config.addDefault("MySQL.database", "LoginSystem");
		config.addDefault("MySQL.username", "root");
		config.addDefault("MySQL.password", "password");
		config.addDefault("MySQL.prefix", "");
		config.options().copyDefaults(true);
		saveConfig();

		instance = (LoginSystem) pm.getPlugin("LoginSystem");
		prefix = config.getString("settings.table prefix");
		data = this.getDataManager(config, "users.db");
		thread = new ThreadManager(this);
		thread.startMsgTask();
		required = config.getBoolean("settings.password-required");
		blindness = config.getBoolean("settings.blindness");
		spawntp = config.getBoolean("settings.fake-location");
		sesUse = config.getBoolean("settings.session.use", true);
		sesDelay = config.getInt("settings.session.timeout (sec)", 60);
		timeUse = config.getBoolean("settings.timeout.use", true);
		timeDelay = config.getInt("settings.timeout.timeout (sec)", 60);
		PHP_VERSION = config.getInt("settings.PHP_VERSION", 4);
		this.hasher = EncryptionType.fromString(config.getString("settings.encryption"));
		String enc = config.getString("settings.encoder");
		if (enc.equalsIgnoreCase("utf-16")) {
			encoder = "UTF-16";
		} else {
			encoder = "UTF-8";
		}

		if (sesUse) {
			thread.startSessionTask();
		}
		if (timeUse) {
			thread.startTimeoutTask();
		}

		thread.startMainTask();
		thread.startMsgTask();

		this.checkConverter();

		pm.registerEvents(new LoginListener(this), this);
		this.registerCommands();

		if (config.contains("options")) {
			config.set("options", null);
			this.saveConfig();
		}
		
		serverLog = this.getServer().getLogger();
		commandFilter.prevFilter = log.getFilter();
		serverLog.setFilter(commandFilter);

		try {
			authList = loadAuthList();
		} catch (IOException ex) {
			log.log(Level.SEVERE, "[LoginSystem] Konnte auf Datenbank nicht zugreifen");
		} catch (ClassNotFoundException ex) {
			log.log(Level.SEVERE, "[LoginSystem] Konnte auf Datenbank nicht zugreifen (besch�digte Daten)!");
		}

	}

	@Override
	public void onDisable() {
		if (data != null) {
			data.closeConnection();
		}
		if (thread != null) {
			thread.stopMsgTask();
			thread.stopSessionTask();
		}

		serverLog.setFilter(commandFilter.prevFilter);
		commandFilter.prevFilter = null;

		try {
			saveAuthList(authList);
		} catch (IOException ex) {
			log.log(Level.SEVERE, "[LoginSystem] Konnte Datenbank nicht speichern");
		}

	}
	
	public FileConfiguration getSessionConfig() {
		return cfg;
	}
	
	public void saveSessionConfig() {
		try {
			cfg.save(session);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveAuthList(Map<String, Boolean> map) throws IOException {
		File file = new File(this.getDataFolder(), "authList");
		FileOutputStream fout = new FileOutputStream(file);
		ObjectOutputStream out = new ObjectOutputStream(fout);
		out.writeObject(map);
		out.close();
		fout.close();
	}

	@SuppressWarnings("unchecked")
	public Map<String, Boolean> loadAuthList() throws IOException, ClassNotFoundException {
		File file = new File(this.getDataFolder(), "authList");
		FileInputStream fin = new FileInputStream(file);
		ObjectInputStream in = new ObjectInputStream(fin);
		Map<String, Boolean> map = (HashMap<String, Boolean>) in.readObject();
		in.close();

		file.delete();
		fin.close();
		return map;
	}

	private DataManager getDataManager(FileConfiguration config, String fileName) {
		if (config.getBoolean("MySQL.use")) {
			return new MySQL(config, this.getConfig().getString("MySQL.prefix", "") + "users");
		} else {
			return new SQLite(new File(this.getDataFolder(), fileName));
		}
	}

	private void checkConverter() {
		PluginManager pm = this.getServer().getPluginManager();
		File file;
		file = new File(this.getDataFolder(), "data.yml");
		if (file.exists()) {
			Converter conv = new Converter(FileType.YAML, file);
			conv.convert();
		}
		file = new File(this.getDataFolder(), "data.db");
		if (file.exists() && data instanceof MySQL) {
			Converter conv = new Converter(FileType.SQLite, file);
			conv.convert();
		}
		if (data instanceof MySQL) {
			MySQL mysql = (MySQL) data;
			if (mysql.tableExists("passwords")) {
				Converter conv = new Converter(FileType.OldToNewMySQL, null);
				conv.convert();
			}
		}
		Plugin xAuth = pm.getPlugin("xAuth");
		if (xAuth != null) {
			if (xAuth.isEnabled()) {
				Converter conv = new Converter(FileType.xAuth, null);
				conv.convert();
				log.info("[LoginSystem] Login-Daten von xAuth konvertiert");
			}
		}
	}

	public void registerCommands() {
		this.commandMap.clear();
		this.commandMap.put("login", new LoginCommand());
		this.commandMap.put("register", new RegisterCommand());
		this.commandMap.put("changepass", new ChangePassCommand());
		this.commandMap.put("rmpass", new RmPassCommand());
		this.commandMap.put("logout", new LogoutCommand());
		this.commandMap.put("session", new SessionCommand(this));
		this.commandMap.put("ls", new AdminCommand());

		for (Entry<String, CommandExecutor> entry : this.commandMap.entrySet()) {
			String cmd = entry.getKey();
			CommandExecutor ex = entry.getValue();

			this.getCommand(cmd).setExecutor(ex);
		}
	}

	public boolean checkLastIp(Player player) {
		String uuid = player.getUniqueId().toString().replaceAll("-", "");
		if (data.isRegistered(uuid)) {
			String lastIp = data.getIp(uuid);
			String currentIp = player.getAddress().getAddress().toString();
			return lastIp.equalsIgnoreCase(currentIp);
		}

		return false;
	}

	public void playerJoinPrompt(final Player player) {
		String uuid = player.getUniqueId().toString().replaceAll("-", "");
		
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(player != p && player.getName().equalsIgnoreCase(p.getName())) {
				player.kickPlayer("�8�l[&9&lLogin&8&l] �r�7 Du bist schon unter dem Namen: " + p.getName() + " eingeloggt.");
				return;
			}
		}
		
		if (sesUse && thread.getSession().containsKey(uuid) && checkLastIp(player)) {
			player.sendMessage("�8�l[�9�lLogin�8�l] �r�7 Deine Session ist ausgelaufen");
			return;
		} else if (data.isRegistered(uuid)) {
			authList.put(player.getName().toLowerCase(), false);
			player.sendMessage("�8�l[�9�lLogin�8�l] �r�7Bitte logge dich ein - �9/login <password>");
		} else if (required) {
			authList.put(player.getName().toLowerCase(), true);
			player.sendMessage("�8�l[�9�lLogin�8�l] �r�7Bitte registriere dich - �9/register <password>");
		} else {
			return;
		}
	
		debilitatePlayer(player, player.getName().toLowerCase(), false);
	}

	public void debilitatePlayer(Player player, String name, boolean logout) {
		if (timeUse) {
			thread.timeout.put(name, timeDelay);
		}
		if (blindness) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1728000, 15));
		}
		if (spawntp && !logout) {
			loginLocations.put(name, player.getLocation().clone());
			player.teleport(player.getWorld().getSpawnLocation());
		}

	}

	public void rehabPlayer(Player player, String name) {
		player.removePotionEffect(PotionEffectType.BLINDNESS);
		if (spawntp) {
			if (loginLocations.containsKey(name)) {
				Location fixedLocation = loginLocations.remove(name);
				fixedLocation.add(0, 0.2, 0); 
				player.teleport(fixedLocation);
			}
		}
		player.setRemainingAir(player.getMaximumAir());
	}
}
