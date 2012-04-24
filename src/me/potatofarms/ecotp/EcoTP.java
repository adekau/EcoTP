package me.potatofarms.ecotp;

import java.util.logging.Logger;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class EcoTP extends JavaPlugin {
	public final Logger logger = Logger.getLogger("minecraft");
	public static EcoTP plugin;
	public static Permission permission = null;
	public static Economy economy = null;
	public static Chat chat = null;
	FileConfiguration config;

	@Override
	public void onDisable() {

		PluginDescriptionFile pdffile = this.getDescription();
		this.logger.info(pdffile.getName() + "has been disabled.");
	}

	@Override
	public void onEnable() {
		try{
			config = getConfig();
			File ec = new File("plugins" + File.separator + "EcoTP" + File.separator + "config.yml");
			ec.mkdir();
			if(!config.contains("general.tpcost")){
			config.set("general.tpcost", 200);
			}

			saveConfig();
			}catch(Exception e1){
			e1.printStackTrace();
			}

		PluginDescriptionFile pdffile = this.getDescription();
		this.logger.info(pdffile.getName() + " version " + pdffile.getVersion()
				+ "has been enabled.");
		if (!setupEconomy()) {
			this.logger.severe(String.format(
					"[%s] - Disabled due to no Vault dependency found!",
					getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		setupPermissions();
		setupChat();
	}

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = getServer()
				.getServicesManager().getRegistration(
						net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}
		return (permission != null);
	}

	private boolean setupChat() {
		RegisteredServiceProvider<Chat> chatProvider = getServer()
				.getServicesManager().getRegistration(
						net.milkbowl.vault.chat.Chat.class);
		if (chatProvider != null) {
			chat = chatProvider.getProvider();
		}

		return (chat != null);
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer()
				.getServicesManager().getRegistration(
						net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return (economy != null);
	}

	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		Player player = (Player) sender;

		if (commandLabel.equalsIgnoreCase("ecotp")) {
			if (args.length == 0) {
				player.sendMessage(ChatColor.RED
						+ "Invalid Syntax. Correct Syntax is: /ecotp <targetplayer>");
			} else if (args.length == 1) {
				// get price from config
				int price = getConfig().getInt("general.tpcost");
				String price_str = String.valueOf(price);
				// Get the target player

				if (Bukkit.getServer().getPlayer(args[0]) != null) {
					Player TargetPlayer = player.getServer().getPlayer(args[0]);
					// get the target player's location
					Location location = TargetPlayer.getLocation();
					// Get balance of player (sender)
					Double Balance = economy.getBalance(player.getName());
					if (Balance >= price) {
						// deduct money from balance
						economy.withdrawPlayer(player.getName(), price);
						// tell the player
						player.sendMessage(ChatColor.GREEN + price_str
								+ " has been deducted from your account");
						// teleport player to other player's location
						player.teleport(location);
					} else if (Balance <= price) {
						player.sendMessage(ChatColor.RED + "Not enough money.");

					}
				} else {

					player.sendMessage(ChatColor.RED + "Player not online.");

				}

			} else if (args.length == 2) {
				player.sendMessage(ChatColor.RED
						+ "Invalid Syntax. Correct Syntax is: /ecotp <targetplayer>");
			}

		}
		return false;

	}

}
