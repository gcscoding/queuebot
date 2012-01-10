package queuebot.bot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.logging.Level;

import queuebot.queue.Message;

/**
 * A small IRC bot that sits on a single channel and listens for commands sent
 * to it by users in private messages. This bot also maintains a message queue
 * which can be emptied by a superuser.
 * 
 * @version 1.0 2012-01-05
 */
public class QueueBot {

	/**
	 * The maximum number of messages the bot's message queue may hold.
	 */
	public static int MAXMSGS;

	private String _ircserv;
	private int _ircport;
	private String _ircchan;
	private String _ircnick;
	private String _ircuser;
	private String _ircrnam;
	private boolean firstPing;
	private boolean joined;
	private int _pingafter;
	private LinkedList<Message> _mq;
	private LinkedList<String> tosend;
	private String _goduser;
	private long lastPinged;
	private boolean _DEBUG;
	private Socket _sock;
	private char[] ibuf;
	private BufferedWriter out;
	private boolean autoMode;
	private int autoCount;
	private int autoInterval;

	// private String leftovers;

	public static void main(String[] args) {
		String server = "";
		int port = 0;
		String chan = "";
		String nick = "";
		String user = "";
		String rname = "";
		int pingafter = 0;
		String superuser = "";
		int queuelimit = 0;
		boolean debug = false;

		try {
			QueueBotLogger.makeLogFile();
		} catch (IOException e) {
			System.out.println("THERE WAS AN ERROR CREATING THE LOG FILES.");
			System.exit(1);
		}
		File ini = new File("QueueBot.ini");
		FileReader r = null;
		try {
			r = new FileReader(ini);
		} catch (FileNotFoundException e) {
			QueueBotLogger.log(Level.SEVERE, "NO QueueBot.ini FILE FOUND");
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
				} else if (parts[0].equals("CHANNEL")) {
					chan = parts[1];
				} else if (parts[0].equals("NICK")) {
					nick = parts[1];
				} else if (parts[0].equals("USER")) {
					user = parts[1];
				} else if (parts[0].equals("RNAME")) {
					rname = parts[1];
				} else if (parts[0].equals("PINGAFTER")) {
					pingafter = Integer.parseInt(parts[1]);
				} else if (parts[0].equals("SUPERUSER")) {
					superuser = parts[1];
				} else if (parts[0].equals("QUEUELIMIT")) {
					queuelimit = Integer.parseInt(parts[1]);
				} else if (parts[0].equals("DEBUG")) {
					debug = parts[1].equals("true");
				}
			}
		} catch (IOException e) {
			QueueBotLogger
					.log(Level.SEVERE,
							"ERROR READING FROM QueueBot.ini. FAILED TO SET UP THE BOT");
			System.exit(1);
		}
		QueueBot qb = new QueueBot(server, port, chan, nick, user, rname,
				pingafter, superuser, queuelimit, debug);
		qb.start();
	}

	/**
	 * Constructs a new QueueBot
	 * 
	 * @param ircserv
	 *            the IRC server to connect to
	 * @param ircport
	 *            the port to connect on
	 * @param ircchan
	 *            the channel to join
	 * @param ircnick
	 *            the nickname to use
	 * @param ircuser
	 *            the bot's username
	 * @param ircrnam
	 *            the bot's real name
	 * @param pingafter
	 *            how long should the bot wait before pinging the server
	 * @param goduser
	 *            the nickname of the superuser
	 * @param maxmessages
	 *            the maximum number of messages the queue can hold
	 * @param DEBUG
	 *            should the bot print all messages received/sent to console
	 */
	public QueueBot(String ircserv, int ircport, String ircchan,
			String ircnick, String ircuser, String ircrnam, int pingafter,
			String goduser, int maxmessages, boolean DEBUG) {
		_ircserv = ircserv;
		_ircport = ircport;
		_ircchan = ircchan;
		_ircnick = ircnick;
		_ircuser = ircuser;
		_ircrnam = ircrnam;
		_pingafter = pingafter;
		_goduser = goduser;
		firstPing = false;
		joined = false;
		_mq = new LinkedList<Message>();
		tosend = new LinkedList<String>();
		lastPinged = System.currentTimeMillis();
		_DEBUG = DEBUG;
		ibuf = new char[4096];
		// leftovers = "";
		MAXMSGS = maxmessages;
		autoMode = false;
		autoCount = 0;
		autoInterval = 0;
	}

	/**
	 * Adds a string to be sent to the IRC server
	 * 
	 * @param s
	 *            the string to be sent
	 */
	public void prepSend(String s) {
		tosend.add(s);
	}

	/**
	 * Creates the socket connection, initializes the output and input buffers,
	 * does some initial setup for connecting to the IRC server, and starts the
	 * bot listening.
	 */
	public void start() {
		try {
			_sock = new Socket(_ircserv, _ircport);
			_sock.setSoTimeout(60000);
			prepSend("NICK " + _ircnick + "\r\n");
			prepSend("USER " + _ircuser + " 0 * : " + _ircrnam + "\r\n");
			out = new BufferedWriter(new OutputStreamWriter(
					_sock.getOutputStream()));
		} catch (IOException e) {
			QueueBotLogger.log(Level.SEVERE,
					"ERROR CREATING SOCKET: " + e.getMessage());
			e.printStackTrace();
			return;
		}
		listen();
	}

	/**
	 * The workhorse method of the bot's main thread. This method loops,
	 * checking to see if a PING or other messages needs to be sent, and if not
	 * then start reading data. After data is read, spawn a new thread to handle
	 * it and start the loop again.
	 */
	private void listen() {
		try {
			QueueBotAutoThread autoThread = null;
			BufferedReader in = new BufferedReader(new InputStreamReader(
					_sock.getInputStream()));
			while (true) {
				if (needsPing()) {
					prepSend("PING ALIVE" + System.currentTimeMillis() + "\r\n");
					lastPinged = System.currentTimeMillis();
				}
				if (autoMode) {
					if (autoThread == null || !autoThread.isAlive()) {
						autoThread = new QueueBotAutoThread(this, autoCount,
								autoInterval, _DEBUG);
						autoThread.setDaemon(true);
						autoThread.start();
						if (_DEBUG) {
							QueueBotLogger.log(Level.INFO,
									"Spawned a new auto thread");
						}
					}
				} else {
					if (autoThread != null && autoThread.isAlive()) {
						autoThread.shutDown();
						if (_DEBUG) {
							QueueBotLogger.log(Level.INFO,
									"Shutdown an auto thread");
						}
					}
				}
				// Worker threads should request a send, but this is backup
				trySend();
				try {
					if (in.read(ibuf) >= 0) {
						String[] lines = getLines(ibuf);
						ibuf = new char[ibuf.length];
						Thread t = new Thread(new QueueBotThread(this, lines,
								_DEBUG));
						t.setDaemon(true);
						t.start();
					}
				} catch (SocketTimeoutException e) {
					QueueBotLogger.log(Level.WARNING, "READ TIMED OUT.");
				}
			}
		} catch (IOException e) {
			QueueBotLogger.log(Level.SEVERE, e.getMessage());
		}
	}

	/**
	 * Parses the data read from the socket into lines.
	 * 
	 * @param buf
	 *            a character array hold read data
	 * @return an array of Strings containing full lines of IRC messages
	 */
	private String[] getLines(char[] buf) {
		StringBuilder builder = new StringBuilder();
		// builder.append(leftovers);
		// leftovers = "";
		builder.append(buf);
		builder.trimToSize();
		String ender = builder
				.substring(builder.length() - 2, builder.length());
		String[] lines = builder.toString().split("\r\n");
		if (!"\r\n".equals(ender)) {
			// leftovers = lines[lines.length - 1];
			lines[lines.length - 1] = null;
		}
		return lines;
	}

	/**
	 * @return true if enough time has elapsed that the bot needs to ping the
	 *         server.
	 */
	private boolean needsPing() {
		return joined
				&& ((System.currentTimeMillis() - lastPinged) / 1000) >= _pingafter;
	}

	/**
	 * Attempts to send whatever messages are in the tosend queue.
	 */
	public synchronized void trySend() {
		boolean doFlush = false;
		while (!tosend.isEmpty()) {
			doFlush = true; // we will need to flush after writing
			String o = tosend.remove();
			if (_DEBUG) {
				System.out.println(o.trim());
			}
			try {
				out.write(o);
			} catch (IOException e) {
				QueueBotLogger
						.log(Level.SEVERE, "FAILED TO SEND MESSAGE: " + o);
			}
		}
		try {
			if (doFlush) {
				out.flush();
			}
		} catch (IOException e) {
			QueueBotLogger.log(Level.WARNING,
					"WARNING: FAILED TO FLUSH OUTPUT STREAM");
		}
	}

	public String getNick() {
		return _ircnick;
	}

	public String getChan() {
		return _ircchan;
	}

	public String getGU() {
		return _goduser;
	}

	public boolean gotFirstPing() {
		return firstPing;
	}

	public void setFirstPing(boolean value) {
		firstPing = value;
	}

	public boolean isJoined() {
		return joined;
	}

	public void setJoined(boolean value) {
		joined = value;
	}

	public LinkedList<Message> getQueue() {
		return _mq;
	}

	public void setQueue(LinkedList<Message> q) {
		_mq = q;
	}

	public void setAutoMode(boolean value, int num, int interval) {
		autoMode = value;
		autoCount = num;
		autoInterval = interval;
	}
}
