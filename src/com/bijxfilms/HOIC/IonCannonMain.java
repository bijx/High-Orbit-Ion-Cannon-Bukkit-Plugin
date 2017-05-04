package com.bijxfilms.HOIC;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import net.md_5.bungee.api.ChatColor;

public class IonCannonMain extends JavaPlugin implements Listener {

	private int explosionDelay, abortDelay;
	private byte colour;
	private boolean cancelFire = false;

	@Override
	public void onEnable() {
		PluginDescriptionFile pdf = this.getDescription();
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		this.getLogger().info("High Orbital Ion Cannon ready to fire!");

		explosionDelay = getConfig().getInt("explosion_delay") * 20;
		colour = (byte) getConfig().getInt("laser_colour");
		abortDelay = getConfig().getInt("abort_delay") * 20;
	}

	@Override
	public void onDisable() {
		this.getLogger().info("High Orbital Ion Cannon on standby!");
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player p = (Player) sender;

		if (cmd.getName().equals("hoic")) {
			if (p.hasPermission("hoic.use")) {

				if (args.length == 0 && !args[0].equals("look") && !args[0].equals("abort")) {
					plug(p, ChatColor.RED + "Please specify a target!");
					plug(p, "/hoic <x> <z>");
				} else if (args[0].equals("look")) {
					Block b = p.getTargetBlock((HashSet<Byte>) null, 200);
					Location l = b.getLocation();
					int x = l.getBlockX(), z = l.getBlockZ();
					particles(x, z, p);
					plug(p, ChatColor.RED
							+ "Orbital cannon preparing to fire on the position you're looking at. Type /hoic abort to abort fire sequence.");
					BukkitScheduler scheduler = getServer().getScheduler();
					scheduler.scheduleSyncDelayedTask(this, new Runnable() {
						@Override
						public void run() {
							if (cancelFire == false) {
								launchCannon(x, z, p);
							} else {
								cancelFire = false;
							}
						}
					}, abortDelay);

				} else if (args[0].equals("abort")) {
					plug(p, ChatColor.YELLOW + "Launch aborted!");
					cancelFire = true;
				} else if (args.length > 2) {
					plug(p, ChatColor.RED + "Too many arguments!");
					plug(p, "/hoic <x> <z>");
				} else {
					try {
						int x = Integer.parseInt(args[0]), z = Integer.parseInt(args[1]);

						launchCannon(x, z, p);

					} catch (Exception e) {
						plug(p, ChatColor.RED + "Please use numbers for coordinates!");
						plug(p, "/hoic <x> <z>");
					}

				}
			}else{
				plug(p, ChatColor.DARK_RED + "You don't have permission to use this command!");
			}
		}

		return false;
	}

	@SuppressWarnings("deprecation")
	public void launchCannon(int x, int z, Player p) {
		plug(p, ChatColor.DARK_RED + ChatColor.BOLD.toString() + "Firing ion cannon to x:" + x + " z:" + z);
		// Set iron blanket
		addIronBlock(x, z, p);
		addIronBlock(x + 1, z + 1, p);
		addIronBlock(x + 1, z, p);
		addIronBlock(x + 1, z - 1, p);
		addIronBlock(x, z - 1, p);
		addIronBlock(x - 1, z - 1, p);
		addIronBlock(x - 1, z, p);
		addIronBlock(x - 1, z + 1, p);
		addIronBlock(x, z + 1, p);
		// Set beacon block
		Location bl = new Location(p.getWorld(), x, 2, z);
		Block bb = p.getWorld().getBlockAt(bl);
		bb.setType(Material.BEACON);
		// Set colour modifier block
		Location cm = new Location(p.getWorld(), x, 3, z);
		Block cmb = p.getWorld().getBlockAt(cm);
		cmb.setType(Material.STAINED_GLASS);
		cmb.setData(colour);

		// Drill through earth from bedrock to sky
		for (int y = 4; y < 256; y++) {
			Location l = new Location(p.getWorld(), x, y, z);
			Block b = p.getWorld().getBlockAt(l);
			b.setType(Material.AIR);
		}

		BukkitScheduler scheduler = getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				blast(x, z, p);
				plug(p, ChatColor.GOLD + ChatColor.BOLD.toString() + "Ion laser launched successfully.");
			}
		}, explosionDelay);

	}

	public void addIronBlock(int x, int z, Player p) {
		Location l = new Location(p.getWorld(), x, 1, z);
		Block b = p.getWorld().getBlockAt(l);
		b.setType(Material.IRON_BLOCK);
	}

	@SuppressWarnings("deprecation")
	public void blast(int x, int z, Player p) {
		for (int j = 1; j < 200; j++) {
			Location l = new Location(p.getWorld(), x, 3, z);
			Entity tnt = p.getWorld().spawn(l, TNTPrimed.class);
			((TNTPrimed) tnt).setFuseTicks(j / 2);
		}

		Location bl = new Location(p.getWorld(), x, 3, z);
		p.getLocation().getWorld().createExplosion(bl, 100);

		for (int i = 0; i < 256; i++) {
			for (int j = 1; j < 10; j++) {
				Location l = new Location(p.getWorld(), x, i, z);
				Entity tnt = p.getWorld().spawn(l, TNTPrimed.class);
				((TNTPrimed) tnt).setFuseTicks(4);
			}
		}

	}

	@SuppressWarnings("deprecation")
	public void particles(int x, int z, Player p) {
		Block b = p.getTargetBlock((HashSet<Byte>) null, 25);
		Location l = b.getLocation();

		p.getWorld().spigot().playEffect(l, Effect.WITCH_MAGIC, 0, 0, 2.0F, 1.0F, 2.0F, 0.1F, 100, 100);
		p.getWorld().spigot().playEffect(l, Effect.VOID_FOG, 0, 0, 2.0F, 2.0F, 2.0F, 0.1F, 500, 100);
	}

	public void plug(Player p, String msg) {
		p.sendMessage(ChatColor.BOLD + ChatColor.GOLD.toString() + "[" + ChatColor.DARK_RED + ChatColor.BOLD.toString()
				+ "HOIC" + ChatColor.BOLD + ChatColor.GOLD.toString() + "]" + ChatColor.RESET + " " + msg);
	}

}
