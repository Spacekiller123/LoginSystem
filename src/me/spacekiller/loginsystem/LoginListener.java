package me.spacekiller.loginsystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.spacekiller.loginsystem.data.UUIDConverter;
import me.spacekiller.loginsystem.util.StringUtil;

import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@SuppressWarnings("deprecation")
public class LoginListener implements Listener {
	
	

	private LoginSystem plugin;

	public LoginListener(LoginSystem i) {
		this.plugin = i;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		File session = new File("plugins/LoginSystem", "Session.yml");
		FileConfiguration cfg = YamlConfiguration.loadConfiguration(session);
		try {
			cfg.save(session);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		final Player player = event.getPlayer();
		String name = player.getName().toLowerCase();
		if(UUIDConverter.IS_CONVERTING) {
			player.kickPlayer("Der Server konvertiert gerade alle Login-Daten. Wir bitten um Ihre Geduld");
			return;
		} if (!player.getName().equals(StringUtil.cleanString(player.getName()))) {
			player.kickPlayer("Ungültige Zeichen im Namen");
			return;
		} if (plugin.sesUse) {
			if (cfg.contains("Session." + player.getName())) {
				if (cfg.contains("Session." + player.getName()) || plugin.data.getSessionId(player.getUniqueId().toString()).equalsIgnoreCase(player.getUniqueId().toString())) {
					if (cfg.getBoolean("Session." + player.getName() + ".Use") || plugin.data.getSessionId(player.getUniqueId().toString()).equalsIgnoreCase(player.getUniqueId().toString())) {
						if (cfg.getString("Session." + player.getName() + ".UUID").equals(player.getUniqueId().toString()) || plugin.data.getSessionId(player.getUniqueId().toString()).equalsIgnoreCase(player.getUniqueId().toString())) {
							if (cfg.getString("Session." + player.getName() + ".IP").equals(player.getAddress().getAddress().toString()) || plugin.data.getSessionIp(player.getAddress().getAddress().toString()).equalsIgnoreCase(player.getAddress().getAddress().toString())) {
								/**
								List<String> ls  = new ArrayList<String>();
								player.updateInventory();
								ls.add("Hiermit wählst du den Server aus");
								player.getInventory().setItem(8, setName(new ItemStack(Material.COMPASS), "§9Serverauswahl", ls));
								List<String> ls2  = new ArrayList<String>();
								player.updateInventory();
								ls2.add("Hiermit wählst du deine Partikel aus");
								player.getInventory().setItem(4, setName(new ItemStack(Material.CHEST), "§9Partikelauswahl", ls2));
								player.updateInventory();
								**/
								plugin.authList.remove(name);
								Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
									public int high = 2;
									public void run() {
										if (high == 2) {
											high--;
										}else if (high == 1) {
											player.sendMessage("§8§l[§9§lLogin§8§l] §r§7Du wurdest durch die Session-Funktion erfolgreich eingeloggt.");
											high--;
										}
											
									}
								}
								, 0L, 20L);
								return;
							}else {
								System.out.println("IP");
								System.out.println(cfg.getString("Session." + player.getName()+ ".IP"));
								System.out.println(player.getAddress().toString());
								plugin.data.deleteSession(player.getUniqueId().toString());
								cfg.set("Session." + player.getName() + ".UUID", null);
								cfg.set("Session." + player.getName() + ".IP", null);
								try {
									cfg.save(session);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}else {
							System.out.println("UUID");
							System.out.println(cfg.getString("Session." + player.getName()+ ".UUID"));
							System.out.println(player.getUniqueId().toString());
							cfg.set("Session." + player.getName() + ".UUID", null);
							cfg.set("Session." + player.getName() + ".IP", null);
							try {
								cfg.save(session);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public int high = 2;
			public void run() {
				if (high == 2) {
					high--;
				}else if (high == 1) {
					plugin.playerJoinPrompt(player);
					high--;
				}
					
			}
		}
		, 0L, 20L);
		
	}
	
	public ItemStack setName(ItemStack is, String name, List<String> lore) {
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);
		im.setLore(lore);
		is.setItemMeta(im);
		return is;
	}
	
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
		String name = event.getName().toLowerCase();
		//Check if the player is already online
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			String pname = player.getName().toLowerCase();
			if (plugin.authList.containsKey(pname)) {
				continue;
			}

			if (pname.equalsIgnoreCase(name)) {
				event.setResult(Result.KICK_OTHER);
				event.setKickMessage("Es ist bereits ein Spieler mit diesem Namen auf dem Server.");
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		if(UUIDConverter.IS_CONVERTING) {
			return;
		}

		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();
		String uuid = player.getUniqueId().toString().replaceAll("-", "");
		String ip = player.getAddress().getAddress().toString();

		if(plugin.authList.containsKey(name) && plugin.spawntp && plugin.loginLocations.containsKey(name)) {
			player.teleport(plugin.loginLocations.remove(name));
		} if (plugin.data.isRegistered(uuid)) {
			plugin.data.updateIp(uuid, ip);
			if (plugin.sesUse && !plugin.authList.containsKey(name)) {
				plugin.thread.getSession().put(name, plugin.sesDelay);
			}
		}

		plugin.authList.remove(name);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();
		if(plugin.loginLocations.containsKey(name)) {
			plugin.loginLocations.put(name, event.getRespawnLocation());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();
		Location from = event.getFrom();
		Location to = event.getTo().clone();

		if (plugin.authList.containsKey(name)) {
			to.setX(from.getX());
			to.setZ(from.getZ());
			event.setTo(to);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();

		if (plugin.authList.containsKey(name)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();

		if (plugin.authList.containsKey(name)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();

		if (plugin.authList.containsKey(name)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();

		if (plugin.authList.containsKey(name)) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent chat) {
		Player player = chat.getPlayer();
		String pname = player.getName().toLowerCase();
		if (plugin.authList.containsKey(pname)) {
			chat.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onHealthRegain(EntityRegainHealthEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Player)) {
			return;
		}
		Player player = (Player) entity;
		String pname = player.getName().toLowerCase();

		if (plugin.authList.containsKey(pname)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Player)) {
			return;
		}
		Player player = (Player) entity;
		String pname = player.getName().toLowerCase();

		if (plugin.authList.containsKey(pname)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		Entity entity = event.getWhoClicked();
		if (!(entity instanceof Player)) {
			return;
		}
		Player player = (Player) entity;
		String pname = player.getName().toLowerCase();

		if (plugin.authList.containsKey(pname)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		Entity entity = event.getEntity();

		if (entity instanceof Player) {
			Player player = (Player) entity;
			String name = player.getName().toLowerCase();
			if (plugin.authList.containsKey(name)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent event) {
		for (LivingEntity entity : event.getAffectedEntities()) {
			if (entity instanceof Player) {
				Player player = (Player) entity;
				String name = player.getName().toLowerCase();
				if (plugin.authList.containsKey(name)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity defender = event.getEntity();
		Entity damager = event.getDamager();

		if (defender instanceof Player) {
			Player p1 = (Player) defender;
			String n1 = p1.getName().toLowerCase();

			if (plugin.authList.containsKey(n1)) {
				event.setCancelled(true);
				return;
			}

			if (damager instanceof Player) {
				Player p2 = (Player) damager;
				String n2 = p2.getName().toLowerCase();

				if (plugin.authList.containsKey(n2)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityTarget(EntityTargetEvent event) {
		Entity entity = event.getTarget();

		if (entity instanceof Player) {
			Player player = (Player) entity;
			String name = player.getName().toLowerCase();

			if (plugin.authList.containsKey(name)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		String name = event.getPlayer().getName().toLowerCase();

		if (plugin.authList.containsKey(name)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();
		if (plugin.authList.containsKey(name)) {
			if (!event.getMessage().startsWith("/login") && !event.getMessage().startsWith("/register")) {
				
				if (event.getMessage().startsWith("/f")) {
					event.setMessage("/" + RandomStringUtils.randomAscii(name.length())); 
				}		    	
				event.setCancelled(true);
			}
		}
	}
}
