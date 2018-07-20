package me.spacekiller.loginsystem.commands;

import com.google.common.base.Charsets;
import java.util.UUID;

import me.spacekiller.loginsystem.LoginSystem;
import me.spacekiller.loginsystem.data.DataManager;
import me.spacekiller.loginsystem.util.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AdminCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		LoginSystem plugin = LoginSystem.instance;
		if (!sender.hasPermission("ls.admin")) {
			sender.sendMessage("§8§l[§9§lLogin§8§l] §r§7Du hast keine Rechte um diesen Befehl auszuführen!");
			return true;
		} try {
			if (args.length == 0) {
				sender.sendMessage("&7============-{ §9§lL§7ogin§9S§7ystem&7}-============".replaceAll("&", String.valueOf('§')));
				sender.sendMessage(ChatColor.GREEN + "/ls changepass <user> <pass>");
				sender.sendMessage(ChatColor.GREEN + "/ls rmpass <user>");
				sender.sendMessage(ChatColor.GREEN + "/ls reload");
				return true;
			}
			if ((args.length >= 3) && (args[0].equalsIgnoreCase("changepass"))) {
				String user = args[1].toLowerCase();
				String uuid = Bukkit.getOfflinePlayer(user).getUniqueId().toString().replaceAll("-", "");
				if ((uuid !=null) && (!uuid.isEmpty()) && (plugin.data.isRegistered(uuid))) {
					String newpass = plugin.hasher.hash(args[2]);
					plugin.data.updatePassword(uuid, newpass, plugin.hasher.getTypeId());
					sender.sendMessage("§8§l[§9§lLogin§8§l] §r§7Das neue Passwort des Spielers lautet: §9" + args[2]);
					return true;
				}else {
					sender.sendMessage("§8§l[§9§lLogin§8§l] §r§7Ungültiger Name oder der Spieler ist nicht registriert");
				}
				return true;
			}
			if ((args.length >= 2) && (args[0].equalsIgnoreCase("rmpass"))) {
				String user = args[1].toLowerCase();
				String uuid = Bukkit.getOfflinePlayer(user).getUniqueId().toString().replaceAll("-", "");
				if ((uuid != null) && (!uuid.isEmpty()) && (plugin.data.isRegistered(uuid))) {
					plugin.data.removeUser(uuid);
					sender.sendMessage("§8§l[§9§lLogin§8§l] §r§7Spieler von der Datenbank entfernt");
					return true;
				}else {
					sender.sendMessage("§8§l[§9§lLogin§8§l] §r§7Ungültiger Name oder der Spieler ist nicht registriert");
				}
				return true;
			} 
			if ((args.length >= 2) && (args[0].equalsIgnoreCase("getpw"))) {
				String user = args[1].toLowerCase();
				String uuid = Bukkit.getOfflinePlayer(user).getUniqueId().toString().replace("-", "");
				DataManager data = plugin.data;
				String realpass = data.getPassword(uuid);
				sender.sendMessage("§8§l[§9§lLogin§8§l] §r§7Das Passwort von §9" + user + " §7ist §9" + realpass);
				return true;
			}else {
				sender.sendMessage("§8§l[§9§lLogin§8§l] §r§7Ungültiger Name oder der Spieler ist nicht registriert");
			}
			if ((args.length >= 1) && (args[0].equalsIgnoreCase("reload"))) {
				plugin.reloadConfig();
				sender.sendMessage("§8§l[§9§lLogin§8§l] §r§7Plugin neugeladen!");
				return true;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}return true;
	}
}