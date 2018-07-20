package me.spacekiller.loginsystem.commands;

import me.spacekiller.loginsystem.LoginSystem;
import me.spacekiller.loginsystem.encryption.PasswordManager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RmPassCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		LoginSystem plugin = LoginSystem.instance;
		if(!(sender instanceof Player)) {
			sender.sendMessage("Du muss ein Spieler sein");
			return true;
		}
		
		Player player = (Player)sender;
		String uuid = player.getUniqueId().toString().replaceAll("-", "");
		
		if (!plugin.data.isRegistered(uuid)) {
			player.sendMessage("§8§l[§9§lLogin§8§l] §r§7Du bist nicht auf §9Reflexcraft §7registriert.");
			return true;
		}if (args.length < 1) {
			player.sendMessage("§8§l[§9§lLogin§8§l] §r§7Zu wenig Argumente");
			player.sendMessage("§8§l[§9§lLogin§8§l] §r§7Benutzung: " + cmd.getUsage());
			return true;
		}if (!PasswordManager.checkPass(uuid, args[0])) {
			player.sendMessage("§8§l[§9§lLogin§8§l] §r§7Dein Passwort ist falsch!");
			return true;
		}if (plugin.required) {
			player.sendMessage("§8§l[§9§lLogin§8§l] §r§7Du muss dich auf §9Reflexcraft §7registrieren.");
		      return true;
		}

		plugin.data.removeUser(uuid);
		player.sendMessage("§8§l[§9§lLogin§8§l] §r§7Erfolgreich dein Password entfernt");
		return true;
	}
	
}
