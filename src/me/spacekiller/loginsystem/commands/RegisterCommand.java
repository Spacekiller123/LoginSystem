package me.spacekiller.loginsystem.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.spacekiller.loginsystem.LoginSystem;
import me.spacekiller.loginsystem.ThreadManager;
import me.spacekiller.loginsystem.data.MySQL;
import me.spacekiller.loginsystem.encryption.EncryptionType;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RegisterCommand implements CommandExecutor{
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		LoginSystem plugin = LoginSystem.instance;
		MySQL sql = MySQL.instance;
		if (!(sender instanceof Player)) {
			sender.sendMessage("Du muss ein Spieler sein");
			return true;
		}

		Player player = (Player)sender;
		String uuid = player.getUniqueId().toString().replaceAll("-", "");
		String name = player.getName().toLowerCase();

		if (plugin.data.isRegistered(uuid)) {
			player.sendMessage("§8§l[§9§lLogin§8§l] §r§7Du bist bereits registriert!");
			return true;
		}
		if (args.length < 1) {
			player.sendMessage("§8§l[§9§lLogin§8§l] §r§7Benutzung: " + cmd.getUsage());
			return true;
		}

		String password = plugin.hasher.hash(args[0]);
		plugin.data.register(uuid, password, plugin.hasher.getTypeId(), player.getAddress().getAddress().toString());
		plugin.authList.remove(name);
		plugin.thread.timeout.remove(name);
		plugin.rehabPlayer(player, name);
		//lobby
		List<String> ls  = new ArrayList<String>();
		player.updateInventory();
		ls.add("Hiermit wählst du den Server aus");
		player.getInventory().setItem(8, setName(new ItemStack(Material.COMPASS), "§9Serverauswahl", ls));
		List<String> ls2  = new ArrayList<String>();
		player.updateInventory();
		ls2.add("Hiermit wählst du deine Partikel aus");
		player.getInventory().setItem(4, setName(new ItemStack(Material.CHEST), "§9Partikelauswahl", ls2));
		player.updateInventory();

		player.sendMessage("§8§l[§9§lLogin§8§l] §r§7Erfolgreich registriert - Passwort:§9 " + args[0]);
		LoginSystem.log.log(Level.INFO, "[LoginSystem] {0} registered sucessfully", player.getName());
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