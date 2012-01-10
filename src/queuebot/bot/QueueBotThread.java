package queuebot.bot;

import java.util.LinkedList;
import java.util.logging.Level;

import queuebot.queue.Message;

public class QueueBotThread implements Runnable {

	private QueueBot _bot;
	private String[] _lines;
	private boolean _DEBUG;

	public QueueBotThread(QueueBot bot, String[] lines, boolean DEBUG) {
		_bot = bot;
		_lines = lines;
		_DEBUG = DEBUG;
	}

	public void run() {
		for (String line : _lines) {
			if (line != null && !line.isEmpty()) {
				if (_DEBUG) {
					System.out.println(line);
				}
				handleMsg(line);
			}
		}
		_bot.trySend();
	}

	private void handleMsg(String msg) {
		String nick = _bot.getNick();
		String chan = _bot.getChan();
		String[] parts = msg.split(" ");
		if ("PING".equals(parts[0])) {
			String pong = msg.replaceAll("PING", "PONG");
			_bot.prepSend(pong + "\r\n");
			if (!_bot.gotFirstPing()) {
				_bot.setFirstPing(true);
			}
		}
		if (_bot.gotFirstPing()) {
			if (_bot.isJoined()) {
				if (parts.length > 3) {
					if ("PRIVMSG".equals(parts[1]) && nick.equals(parts[2])) {
						String uname = getUsername(msg.substring(1));
						String body = msg.split(nick + " :")[1];
						// QueueBotLogger.log(Level.INFO, "MESSAGE RECEIVED: "
						// + body + "\nANALYZING FOR COMMANDS");
						parse(uname, body);
					}
				}
			} else if (!_bot.isJoined() && msg.indexOf(nick + " +i") > 0) {
				_bot.prepSend("JOIN " + chan + "\r\n");
				_bot.prepSend("MODE " + chan + "\r\n");
				// prepSend("WHO " + chan + "\r\n");
			} else if (!_bot.isJoined()) {
				String uname = getUsername(msg);
				if ((":" + nick).equals(uname)) {
					if ("JOIN".equals(parts[1])
							&& chan.compareToIgnoreCase(parts[2]) == 0) {
						_bot.setJoined(true);
					}
				}
			}
		}
	}

	private void parse(String uname, String line) {
		String gu = _bot.getGU();
		String chan = _bot.getChan();
		String[] parts = line.split(" ");
		LinkedList<Message> q = _bot.getQueue();
		if ("!ask".equals(parts[0])) {
			// QueueBotLogger.log(Level.INFO, "FOUND COMMAND: !ask");
			parts[0] = "";
			if (q.size() >= QueueBot.MAXMSGS) {
				_bot.prepSend("PRIVMSG "
						+ uname
						+ " :Sorry, but the queue is at capacity right now. Please ask again after it has been cleared.\r\n");
				return;
			}
			StringBuilder b = new StringBuilder();
			for (String p : parts) {
				b.append(p);
				b.append(" ");
			}
			String s = b.toString().trim();
			Message m = new Message(uname, s);
			q.add(m);
			_bot.prepSend("PRIVMSG " + uname + " :Added your message: \"" + s
					+ "\" to the queue. Your position in queue is " + q.size()
					+ ".\r\n");
		} else if ("!get".equals(parts[0])) {
			// QueueBotLogger.log(Level.INFO, "FOUND COMMAND: !get");
			boolean res = verifyUname(uname);
			if (res) {
				int num = 1;
				if (parts.length == 2) {
					try {
						num = Integer.parseInt(parts[1]);
					} catch (NumberFormatException e) {
						num = 1;
					}
				}
				for (int i = 0; i < num; i++) {
					if (!q.isEmpty()) {
						Message item = q.remove();
						_bot.prepSend("PRIVMSG " + gu + " :"
								+ item.getUsername() + " asked: "
								+ item.getContent() + "\r\n");
						_bot.prepSend("PRIVMSG " + chan + " :"
								+ item.getUsername() + " asked: "
								+ item.getContent() + "\r\n");
					} else {
						_bot.prepSend("PRIVMSG " + gu
								+ " :MESSAGE QUEUE EMPTY\r\n");
					}
				}
			} else {
				QueueBotLogger.log(Level.WARNING, "INVALID USER ACCESS: "
						+ uname);
			}
		} else if ("!trim".equals(parts[0])) {
			// QueueBotLogger.log(Level.INFO, "FOUND COMMAND: !trim");
			boolean res = verifyUname(uname);
			if (res) {
				if (parts.length != 2) {
					_bot.prepSend("PRIVMSG " + gu
							+ " :Invalid usage. Proper usage: !trim X\r\n");
					return;
				}
				try {
					int num = Integer.parseInt(parts[1]);
					if (num <= q.size()) {
						_bot.setQueue(new LinkedList<Message>(q.subList(q.size() - num, q.size())));
					}
				} catch (NumberFormatException e) {
					_bot.prepSend("PRIVMSG " + gu
							+ " :Invalid usage. Proper usage: !trim X\r\n");
				}
			} else {
				QueueBotLogger.log(Level.WARNING, "INVALID USER ACCESS: "
						+ uname);
			}
		} else if ("!clear".equals(parts[0])) {
			// QueueBotLogger.log(Level.INFO, "FOUND COMMAND: !clear");
			boolean res = verifyUname(uname);
			if (res) {
				_bot.getQueue().clear();
			} else {
				QueueBotLogger.log(Level.WARNING, "INVALID USER ACCESS: "
						+ uname);
			}
		} else if ("!count".equals(parts[0])) {
			// QueueBotLogger.log(Level.INFO, "FOUND COMMAND: !count");
			_bot.prepSend("PRIVMSG " + uname + " :There are currently "
					+ _bot.getQueue().size() + " messages in the queue.\r\n");
		} else if ("!help".equals(parts[0])) {
			// QueueBotLogger.log(Level.INFO, "FOUND COMMAND: !help");
			boolean res = verifyUname(uname);
			_bot.prepSend("PRIVMSG " + uname + " :!ask QUESTION\r\n");
			_bot.prepSend("PRIVMSG " + uname + " :!count\r\n");
			if (res) {
				_bot.prepSend("PRIVMSG " + uname + " :!get [X]\r\n");
				_bot.prepSend("PRIVMSG " + uname + " :!trim X\r\n");
				_bot.prepSend("PRIVMSG " + uname + " :!clear\r\n");
				_bot.prepSend("PRIVMSG " + uname + " :!auto <off|N D>\r\n");
			}
		} else if ("!auto".equals(parts[0])) {
			boolean res = verifyUname(uname);
			if (res) {
				if (parts.length != 2 && parts.length != 3) {
					_bot.prepSend("PRIVMSG "
							+ gu
							+ " :Invalid usage. Proper usage: !auto <off|N D>\r\n");
				} else {
					if ("off".equals(parts[1])) {
						_bot.setAutoMode(false, 0, 0);
					} else {
						try {
							int num = Integer.parseInt(parts[1]);
							int interval = Integer.parseInt(parts[2]);
							_bot.setAutoMode(true, num, interval);
						} catch (NumberFormatException e) {
							_bot.prepSend("PRIVMSG "
									+ gu
									+ " :Invalid usage. Proper usage: !auto <off|N D>\r\n");
						}
					}
				}
			} else {
				QueueBotLogger.log(Level.WARNING, "INVALID USER ACCESS: "
						+ uname);
			}
		}
	}

	private boolean verifyUname(String uname) {
		return _bot.getGU().equals(uname);
	}

	private String getUsername(String line) {
		String[] parts = line.split("!");
		if (parts.length > 1) {
			return parts[0];
		} else {
			return null;
		}
	}
}
