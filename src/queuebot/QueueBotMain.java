package queuebot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;

import queuebot.bot.QueueBot;

public class QueueBotMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String server = "";
		int port = 0;
		String chan = "";
		String nick = "";
		String superuser = "";
		boolean debug = false;

		File ini = new File("QueueBot.ini");
		FileReader r = null;
		try {
			r = new FileReader(ini);
		} catch (FileNotFoundException e) {
			System.out.println("ERROR OPENING .ini FILE");
			System.exit(1);
		}
		BufferedReader b = new BufferedReader(r);
		String line = "";
		try {
			while ((line = b.readLine()) != null) {
				String[] parts = line.trim().split("=");
				if (parts[0].equals("SERVER")) {
					server = parts[1];
				} else if (parts[0].equals("PORT")) {
					port = Integer.parseInt(parts[1]);
				} else if (parts[0].equals("CHANNELS")) {
					chan = parts[1];
				} else if (parts[0].equals("NICK")) {
					nick = parts[1];
				} else if (parts[0].equals("SUPERUSER")) {
					superuser = parts[1];
				} else if (parts[0].equals("DEBUG")) {
					debug = parts[1].equals("true");
				}
			}
		} catch (IOException e) {
			System.out.println("ERROR PARSING .ini FILE: " + e.getMessage());
			System.exit(1);
		}

		QueueBot bot = new QueueBot(chan, superuser);
		bot.subSetName(nick);
		bot.setAutoNickChange(true);
		bot.setVerbose(debug);
		try {
			bot.connect(server, port);
		} catch (NickAlreadyInUseException e) {

		} catch (IrcException e) {
			bot.log("SERVER WOULD NOT LET US JOIN: " + e.getMessage());
		} catch (IOException e) {
			bot.log("COULD NOT CONNECT TO SERVER: " + e.getMessage());
		}
		bot.joinChannel(chan);
	}

}
