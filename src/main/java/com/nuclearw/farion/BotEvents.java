package com.nuclearw.farion;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.DisconnectEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NickChangeEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;

import com.nuclearw.farion.runnable.RemovePlayerTask;

public class BotEvents extends ListenerAdapter {

	private PircBotX bot;
	private Plugin plugin;

	public BotEvents(Plugin plugin, PircBotX bot) {

		this.plugin = plugin;
		this.bot = bot;
	}

	public void onConnect(ConnectEvent event) throws Exception {
		plugin.getLogger().info("Connected to IRC");
		plugin.getLogger().info(Config.hostname + ", port " + Config.port);
	}

	public void onDisconnect(DisconnectEvent event) throws Exception {
		plugin.getLogger().info("Disconnected from IRC.");

		//Set a delayed task to attempt a rejoin
		if(Config.retryConnect = true) {
			plugin.getLogger().info("Retrying connect in 10 seconds...");

			//Schedule the actual task
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					Farion.reconnect();
				}
			}, 200L);

		//If retryConnect is false, don't even bother.
		} else {
			plugin.getLogger().info("RetryConnect is off: No additional connection attempts.");
		}
	}

	public void onJoin(JoinEvent event) throws Exception {
		if(event.getChannel().toString().equalsIgnoreCase(Config.channel)) {
			if(Config.showGameJoinMessage == true) {
				String message = ChatColor.translateAlternateColorCodes('&', Config.gameJoinMessage)
				                 .replace("{nickname}", event.getUser().toString());

				plugin.getServer().broadcastMessage(message);
			}
		}
	}

	public void onPart(PartEvent event) throws Exception {
		if(event.getChannel().toString().equalsIgnoreCase(Config.channel)) {
			if(Config.showGamePartMessage == true) {
				String message = ChatColor.translateAlternateColorCodes('&', Config.gamePartMessage)
				                 .replace("{nickname}", event.getUser().toString());

				plugin.getServer().broadcastMessage(message);
			}
		}
	}

	public void onNickChange(NickChangeEvent event) throws Exception {
		String message = ChatColor.translateAlternateColorCodes('&', Config.gameNickChangeMessage)
		                 .replace("{oldnick}", event.getOldNick())
		                 .replace("{newnick}", event.getNewNick());

		plugin.getServer().broadcastMessage(message);
		return;
	}

	public void onMessage(MessageEvent event) throws Exception {
		if(event.getChannel().toString().equalsIgnoreCase(Config.channel)) {
			if(event.getMessage().equalsIgnoreCase(".players")) {
				if(plugin.getServer().getOnlinePlayers().length == 0) {
					event.getBot().sendMessage(event.getChannel(), "Nobody online here.");
				} else {
					String send = "Players online (" + plugin.getServer().getOnlinePlayers().length + "/" + plugin.getServer().getMaxPlayers() + "): ";
					for(Player player : plugin.getServer().getOnlinePlayers()) {
						send += player.getName() + ", ";
					}
					send = send.substring(0, send.length() - 2);
					event.getBot().sendMessage(event.getChannel(), send);
				}
				return;
			}

			if(event.getMessage().toLowerCase().startsWith(".kick")) {
				if(!isVoiceOrOp(event.getUser(), event.getChannel())) {
					event.getBot().sendMessage(event.getChannel(), "nope.avi");
					return;
				}

				String words[] = event.getMessage().split(" ");

				if(words.length < 2) {
					event.getBot().sendMessage(event.getChannel(), "You're doing it wrong " + event.getUser().getNick());
					return;
				}

				String target = words[1];

				Player player = plugin.getServer().getPlayer(target);
				if(player == null) {
					event.getBot().sendMessage(event.getChannel(), "Cannot find player by the name of " + target);
					return;
				}

				String kickMessage = "";

				if(words.length > 2) {
					for(int i = 2; i < words.length; i++) {
						kickMessage += words[i] + " ";
					}
					kickMessage = kickMessage.substring(0, kickMessage.length()-1);
				} else {
					kickMessage = "Kicked by admin.";
				}

				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RemovePlayerTask(player, kickMessage, false));

				return;
			}

			if(event.getMessage().toLowerCase().startsWith(".ban")) {
				if(!isVoiceOrOp(event.getUser(), event.getChannel())) {
					event.getBot().sendMessage(event.getChannel(), "nope.avi");
					return;
				}

				String words[] = event.getMessage().split(" ");

				if(words.length < 2) {
					event.getBot().sendMessage(event.getChannel(), "You're doing it wrong " + event.getUser().getNick());
					return;
				}

				String target = words[1];

				Player player = plugin.getServer().getPlayer(target);
				if(player == null) {
					event.getBot().sendMessage(event.getChannel(), "Cannot find player by the name of " + target);
					return;
				}

				String kickMessage = "";

				if(words.length > 2) {
					for(int i = 2; i < words.length; i++) {
						kickMessage += words[i] + " ";
					}
					kickMessage = kickMessage.substring(0, kickMessage.length()-1);
				} else {
					kickMessage = "Banned by admin.";
				}

				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RemovePlayerTask(player, kickMessage, true));

				return;
			}

			if(event.getMessage().toLowerCase().startsWith(".clear")) {
				if(!isVoiceOrOp(event.getUser(), event.getChannel())) {
					event.getBot().sendMessage(event.getChannel(), "nope.avi");
					return;
				}

				//clearQueue();
				return;
			}

			if(Config.showGameMessage) {
				String sendMessage = ChatColor.translateAlternateColorCodes('&', Config.gameMessage)
				                     .replace("{nickname}", event.getUser().getNick())
				                     .replace("{message}", ColorConverter.ircToMinecraft(event.getMessage()));

				plugin.getServer().broadcastMessage(sendMessage);
			}
		} else if(event.getChannel().toString().equalsIgnoreCase(Config.modChannel)) {
			// TODO: Mod Channel
		}
	}

	public void onPrivateMessage(PrivateMessageEvent event) throws Exception {
		if(Farion.remoteSenders.containsKey(event.getUser().getNick())) {
			FarionRemoteConsoleCommandSender remote = Farion.remoteSenders.get(event.getUser().getNick());
			if(event.getMessage().equalsIgnoreCase(".on")) {
				bot.sendMessage(event.getUser().getNick(), "Console output is now enabled.");
				remote.setRecieve(true);
				return;
			} else if(event.getMessage().equalsIgnoreCase(".off")) {
				bot.sendMessage(event.getUser().getNick(), "Console output is now disabled.");
				remote.setRecieve(false);
				return;
			}

			if(remote.doesRecieve()) {
				FarionRemoteServerCommandEvent farionEvent = new FarionRemoteServerCommandEvent(remote, event.getMessage());
				Bukkit.getServer().getPluginManager().callEvent(farionEvent);
				Bukkit.getServer().dispatchCommand(remote, farionEvent.getCommand());
			} else {
				bot.sendMessage(event.getUser().getNick(), "Cannot send console commands while console output is disabled!");
			}
		}
	}

	private boolean isVoiceOrOp(User user, Channel channel) {
		if(user.getChannelsOpIn().contains(channel) || user.getChannelsVoiceIn().contains(channel)) {
			return true;
		} else {
			return false;
		}
	}
}