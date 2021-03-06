package com.nuclearw.farion;

import java.io.File;
import java.util.List;

public class Config {
	public static String nick, nickServPassword, channel, dccPassword, modChannel, hostname, password;
	public static int port;
	public static boolean ssl, retryConnect;
	public static List<String> remoteUsernames;

	public static String ircMessage, ircMeMessage;
	public static String ircJoinMessage, ircPartMessage;

	public static String gameMessage, gameMeMessage;
	public static String gameJoinMessage, gamePartMessage, gameNickChangeMessage;

	public static boolean showIrcMessage, showIrcMeMessage;
	public static boolean showIrcJoinMessage, showIrcPartMessage;

	public static boolean showGameMessage, showGameMeMessage;
	public static boolean showGameJoinMessage, showGamePartMessage, showGameNickChangeMessage;

	public static void load(Farion plugin) {
		if(!new File(plugin.getDataFolder() , "config.yml").exists()) {
			plugin.saveDefaultConfig();
		}

		nick = plugin.getConfig().getString("Bot.Nick");
		nickServPassword = plugin.getConfig().getString("Bot.NickServ");
		retryConnect = plugin.getConfig().getBoolean("Bot.RetryConnect");
		dccPassword = plugin.getConfig().getString("Bot.DCCPassword");

		hostname = plugin.getConfig().getString("Server.Hostname");
		port = plugin.getConfig().getInt("Server.Port");
		ssl = plugin.getConfig().getBoolean("Server.SSL");
		password = plugin.getConfig().getString("Server.Password");

		channel = plugin.getConfig().getString("Channel.Name");
		modChannel = plugin.getConfig().getString("ModChannel.Name");

		// Game -> IRC
		ircMessage = plugin.getConfig().getString("Message.Irc.Message");
		ircMeMessage = plugin.getConfig().getString("Message.Irc.MeMessage");
		ircJoinMessage = plugin.getConfig().getString("Message.Irc.JoinMessage");
		ircPartMessage = plugin.getConfig().getString("Message.Irc.PartMessage");

		// IRC -> Game
		gameMessage = plugin.getConfig().getString("Message.Game.Message");
		gameMeMessage = plugin.getConfig().getString("Message.Game.MeMessage");
		gameJoinMessage = plugin.getConfig().getString("Message.Game.JoinMessage");
		gamePartMessage = plugin.getConfig().getString("Message.Game.PartMessage");
		gameNickChangeMessage = plugin.getConfig().getString("Message.Game.NickMessage");

		showGameMessage = !gameMessage.isEmpty();
		showGameMeMessage = !gameMeMessage.isEmpty();
		showGameJoinMessage = !gameJoinMessage.isEmpty();
		showGamePartMessage = !gamePartMessage.isEmpty();
		showGameNickChangeMessage = !gameNickChangeMessage.isEmpty();

		showIrcMessage = !ircMessage.isEmpty();
		showIrcMeMessage = !ircMeMessage.isEmpty();
		showIrcJoinMessage = !ircJoinMessage.isEmpty();
		showIrcPartMessage = !ircPartMessage.isEmpty();

		remoteUsernames = plugin.getConfig().getStringList("RemoteConsoleUsers");
	}

	public static void reload(Farion plugin) {
		plugin.reloadConfig();
		load(plugin);
	}
}
