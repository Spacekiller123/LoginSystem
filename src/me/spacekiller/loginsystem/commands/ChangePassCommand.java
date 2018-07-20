package me.spacekiller.loginsystem.commands;

import me.spacekiller.loginsystem.LoginSystem;
import me.spacekiller.loginsystem.encryption.PasswordManager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class ChangePassCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		LoginSystem plugin = LoginSystem.instance;
		if (!(sender instanceof Player)) {
			sender.sendMessage("Du muss ein Spieler sein");
			return true;
		}

		Player player = (Player) sender;
		String uuid = player.getUniqueId().toString().replaceAll("-", "");

		if (!plugin.data.isRegistered(uuid)) {
			player.sendMessage("§8§l[§9§lLogin§8§l] §r§7Du bist nicht auf §9Reflexcraft §7registriert.");
			return true;
		}
		if (args.length < 2) {
			player.sendMessage("§8§l[§9§lLogin§8§l] §r§7Benutzung: " + cmd.getUsage());
			return true;
		}
		if (!PasswordManager.checkPass(uuid, args[0])) {
			player.sendMessage("§8§l[§9§lLogin§8§l] §r§7Dein Passwort ist falsch!");
		    LoginSystem.log.log(Level.WARNING, "[LoginSystem] {0} failed to change password", player.getName());
			return true;
		}

		String newPass = plugin.hasher.hash(args[1]);
		plugin.data.updatePassword(uuid, newPass, plugin.hasher.getTypeId());
		player.sendMessage("§8§l[§9§lLogin§8§l] §r§7Du hast dein Passwort geändert zu: §9" + args[1]);
	    LoginSystem.log.log(Level.INFO, "[LoginSystem] {0} sucessfully changed password", player.getName());

		return true;
	}
}
