package me.spacekiller.loginsystem.commands;

import me.spacekiller.loginsystem.LoginSystem;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class LogoutCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		LoginSystem plugin = LoginSystem.instance;
		if (!(sender instanceof Player)) {
			sender.sendMessage("Du muss ein Spieler sein");
			return true;
		}

		Player player = (Player) sender;
		String name = player.getName().toLowerCase();

		if (plugin.authList.containsKey(name)) {
			player.sendMessage("§8§l[§9§lLogin§8§l] §r§7Bitte logge dich ein - §9/login <password>");
			return true;
		} else {
			if (!plugin.data.isRegistered(name)) {
				player.sendMessage("§8§l[§9§lLogin§8§l] §r§7Du bist nicht auf §9Reflexcraft §7registriert.");
			}
		}
		

		plugin.authList.put(name, false);
		plugin.debilitatePlayer(player, name, true);
		// terminate user's current session
		if (plugin.sesUse) {
			plugin.thread.getSession().remove(name);
		}

		player.sendMessage("§8§l[§9§lLogin§8§l] §r§7Erfolgreich ausgeloggt!");
	    LoginSystem.log.log(Level.INFO, "[LoginSystem] {0} logged out", player.getName());
		return true;
	}
}
