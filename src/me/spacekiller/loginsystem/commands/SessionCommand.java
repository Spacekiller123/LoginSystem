package me.spacekiller.loginsystem.commands;

import java.io.File;
import java.io.IOException;

import me.spacekiller.loginsystem.LoginSystem;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class SessionCommand implements CommandExecutor {
	LoginSystem plugin;
	
	public static File session = new File("plugins/LoginSystem", "Session.yml");
	public static FileConfiguration cfg;
	
	public SessionCommand(LoginSystem plugin) {
		this.plugin = plugin;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
		cfg = YamlConfiguration.loadConfiguration(session);
		Player p = (Player) sender;
		if (cmd.getName().equalsIgnoreCase("session")) {
			if (cfg.contains("Session." + p.getName())) {
				if (cfg.getBoolean("Session." + p.getName() + ".Use") == true) {
					cfg.set("Session." + p.getName(), null);
					p.sendMessage("§8§l[§9§lLogin§8§l] §r§7Session-Funktion abgeschaltet");
					try {
						cfg.save(session);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}else {
				cfg.set("Session." + p.getName() + ".Use", true);
				cfg.set("Session." + p.getName() + ".UUID", p.getUniqueId().toString());
				cfg.set("Session." + p.getName() + ".IP", p.getAddress().getAddress().toString());
				p.sendMessage("§8§l[§9§lLogin§8§l] §r§7Session-Funktion eingeschaltet");
				p.sendMessage("§8§l[§9§lLogin§8§l] §r§7Deine IP und UUID wurden gespeichert.");
				p.sendMessage("§8§l[§9§lLogin§8§l] §r§7Die Session läuft aus, wenn du eine andere IP hast oder wenn der Server neustartet");
				//p.sendMessage("§8§l[§9§lLogin§8§l] §r§7UUID: §9" + p.getUniqueId().toString());
				//p.sendMessage("§8§l[§9§lLogin§8§l] §r§7Aktuelle IP: §9" + p.getAddress().getAddress().toString());
				String uuid = p.getUniqueId().toString().replace("-", "");
				plugin.data.session(p.getUniqueId().toString(), "1234");
				try {
					cfg.save(session);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

}
