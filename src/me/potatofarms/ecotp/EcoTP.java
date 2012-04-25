package me.potatofarms.ecotp;

import java.util.logging.Logger;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
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
		loadConfiguration();
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

	public void loadConfiguration() {
		getConfig().addDefault("general.tpcost", 200);
		getConfig().options().copyDefaults(true);
		saveConfig();

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
			String subCommand = args.length > 0 ? args[0].toLowerCase() : "";
			if (args.length == 0) {
				player.sendMessage(ChatColor.RED
						+ "Invalid Syntax. Correct Syntax is: /ecotp <targetplayer>");
				return true;
			} else if (args.length >= 1) {
				if (Bukkit.getServer().getPlayer(args[0]) == null) {
					if (subCommand.equalsIgnoreCase("help")) {
						if (player.hasPermission("ecotp.help")) {
							player.sendMessage(ChatColor.GOLD + "EcoTP Help:");
							player.sendMessage(ChatColor.GOLD + "Commands: "
									+ ChatColor.BLUE + "/ecotp "
									+ ChatColor.AQUA + "<targetplayer>");
							player.sendMessage(ChatColor.BLUE + "/ecotp "
									+ ChatColor.AQUA + "price" + ChatColor.GOLD + "-- Check the price of the teleport.");
							player.sendMessage(ChatColor.BLUE + "/ecotp "
									+ ChatColor.AQUA + "bal" + ChatColor.GOLD + "-- Check your current balance.");
						} else {
							player.sendMessage(ChatColor.RED
									+ "You don't have permission to use this.");
						}
						return true;

					} else if (subCommand.equalsIgnoreCase("bal")
							|| subCommand.equalsIgnoreCase("balance")) {
						if (player.hasPermission("ecotp.balance")) {
							player.sendMessage(ChatColor.GREEN
									+ "Your current balance is: "
									+ ChatColor.GOLD
									+ economy.getBalance(player.getName()));
						} else {
							player.sendMessage(ChatColor.RED
									+ "You don't have permission to use this.");
						}
						return true;
					} else if (subCommand.equalsIgnoreCase("price")) {
						if (player.hasPermission("ecotp.price")) {
							player.sendMessage(ChatColor.GREEN + "It costs "
									+ ChatColor.GOLD
									+ getConfig().getInt("general.tpcost")
									+ " to teleport");
						} else {
							player.sendMessage(ChatColor.RED
									+ "You don't have permission to use this.");
						}
						return true;
					} else if (subCommand.equalsIgnoreCase("reload")) {
						if (player.hasPermission("ecotp.reload")) {
							reloadConfig();
						} else {
							player.sendMessage(ChatColor.RED
									+ "You don't have permission to use this.");
						}
						return true;
					} else if (subCommand.equalsIgnoreCase("setprice")) {
						if (player.hasPermission("ecotp.setprice")) {
							String price = args.length > 1 ? args[1] : "";
							int iprice;
							iprice = Integer.parseInt(price);

							getConfig().set("general.tpcost", iprice);
							saveConfig();
						} else {
							player.sendMessage(ChatColor.RED
									+ "You don't have permission to use this.");
						}
						return true;
					}

				}

				// get price from config
				int price = getConfig().getInt("general.tpcost");
				String price_str = String.valueOf(price);
				// Get the target player
				if (player.hasPermission("ecotp.tp")) {
					if (Bukkit.getServer().getPlayer(args[0]) != null) {
						Player TargetPlayer = player.getServer().getPlayer(
								args[0]);
						if (TargetPlayer.getName().equalsIgnoreCase(
								player.getName())) {
							player.sendMessage(ChatColor.RED
									+ "You cannot teleport to yourself.");
							return true;
						} else {

						}
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
							player.sendMessage(ChatColor.RED
									+ "Not enough money.");
							return true;
						}
					} else {

						player.sendMessage(ChatColor.RED + "Player not online.");
						return true;

					}
				} else {
					player.sendMessage(ChatColor.RED
							+ "You don't have permission to use this.");
					return true;
				}

			} else if (args.length == 2) {
				player.sendMessage(ChatColor.RED
						+ "Invalid Syntax. Correct Syntax is: /ecotp <targetplayer>");
			}

		}
		return false;

	}

}
