package me.spacekiller.loginsystem.commands;

import me.spacekiller.loginsystem.LoginSystem;
import me.spacekiller.loginsystem.data.MySQL;
import me.spacekiller.loginsystem.encryption.PasswordManager;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		LoginSystem plugin = LoginSystem.instance;
		
		MySQL sql = MySQL.instance;
		
		if(!(sender instanceof Player)) {
			sender.sendMessage("Du muss ein Spieler sein");
			return true;
		}
		
		Player player = (Player)sender;
		String uuid = player.getUniqueId().toString().replaceAll("-", "");
		String name = player.getName().toLowerCase();
		
		if(!plugin.authList.containsKey(name)) {
			player.sendMessage("§8§l[§9§lLogin§8§l] §r§7Du bist bereits eingeloggt!");
			return true;
		}
		if(!plugin.data.isRegistered(uuid)) {
			player.sendMessage("§8§l[§9§lLogin§8§l] §r§7Du bist nicht auf §9Reflexcraft §7registriert.");
			return true;
		}
		if(args.length < 1) {
			player.sendMessage("§8§l[§9§lLogin§8§l] §r§7Benutzung: " + cmd.getUsage());
			return true;
		}
		if(PasswordManager.checkPass(uuid, args[0])) {
			plugin.authList.remove(name);
			plugin.thread.timeout.remove(name);
			plugin.rehabPlayer(player, name);
			player.sendMessage("§8§l[§9§lLogin§8§l] §r§7Erfolgreich eingeloggt!");
			//Session
			File session = new File("plugins/LoginSystem", "Session.yml");
			FileConfiguration cfg = YamlConfiguration.loadConfiguration(session);
			if (cfg.contains("Session." + player.getName())) {
				if (cfg.getBoolean("Session." + player.getName() + ".Use")) {
					player.sendMessage("§8§l[§9§lLogin§8§l] §r§7Deine IP hat sich geändert und wurde jetzt in der Datenbank aktualisiert");
					player.sendMessage("§8§l[§9§lLogin§8§l] §r§7UUID: §9" + player.getUniqueId().toString());
					player.sendMessage("§8§l[§9§lLogin§8§l] §r§7Aktuelle IP: §9" + player.getAddress().getAddress().toString());
					cfg.set("Session." + player.getName() + ".UUID", player.getUniqueId().toString());
					cfg.set("Session." + player.getName() + ".IP", player.getAddress().getAddress().toString());
					plugin.data.session(player.getUniqueId().toString(), player.getAddress().getAddress().toString());
					try {
						cfg.save(session);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}else {
				player.sendMessage("§8§l[§9§lLogin§8§l] §r§7Benutze /session, um deine UUID und IP zu speichern!");
			}
			
			LoginSystem.log.log(Level.INFO, "[LoginSystem] {0} authenticated", player.getName());
		} else {
			player.sendMessage("§8§l[§9§lLogin§8§l] §r§7Dein Passwort ist falsch!");
		    LoginSystem.log.log(Level.WARNING, "[LoginSystem] {0} entered an incorrect password", player.getName());
		}	
		return true;
	}

	
	public ItemStack setName(ItemStack is, String name, List<String> lore) {
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);
		im.setLore(lore);
		is.setItemMeta(im);
		return is;
	}
}
