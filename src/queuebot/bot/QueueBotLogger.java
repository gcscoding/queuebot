package queuebot.bot;

import java.io.*;
import java.util.logging.*;

public class QueueBotLogger {
	private static FileHandler _handler;
	private static SimpleFormatter _format;
	private static Logger _log;
	
	public static void makeLogFile() throws IOException {
		_log = Logger.getLogger("queuebot.bot");
		_handler = new FileHandler("QueueBotLog.txt");
		_format = new SimpleFormatter();
		_handler.setFormatter(_format);
		_log.addHandler(_handler);
	}
	
	public static Logger getLogger() {
		return _log;
	}
	
	public static void log(Level lvl, String message) {
		_log.log(lvl, message);
	}
}
