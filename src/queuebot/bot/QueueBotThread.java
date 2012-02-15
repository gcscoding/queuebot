package queuebot.bot;

import java.util.LinkedList;

import queuebot.bot.QueueBot;
import queuebot.queue.Message;

/**
 * 
 * @author Winslow Dalpe
 *
 */
public class QueueBotThread implements Runnable {

	private QueueBot bot;
	private String uname;
	private String line;

	public QueueBotThread(QueueBot bot, String uname, String line) {
		this.bot = bot;
		this.uname = uname;
		this.line = line;
	}

	public void run() {
		if (line != null && !line.isEmpty() && uname != null
				&& !uname.isEmpty()) {
			parse(uname, line);
		}
	}

	private void parse(String user, String line) {
		String chan = bot.getChannels()[0];
		String gu = bot.getSuperUser();
		String[] parts = line.split(" ");
		LinkedList<Message> q = bot.getQueue();
		if ("!ask".equals(parts[0])) {
			parts[0] = "";
			StringBuilder b = new StringBuilder();
			for (String p : parts) {
				b.append(p);
				b.append(" ");
			}
			String s = b.toString().trim();
			Message m = new Message(user, s);
			if (bot.containsMessage(m)) {
				bot.sendMessage(user,
						"Sorry, but that exact question is already in the queue.");
				return;
			}
			bot.addMessage(m);
			bot.sendMessage(user,
					"Added your message: \"" + s
							+ "\" to the queue. Your position in queue is "
							+ bot.getQueueSize() + ".");
		} else if ("!get".equals(parts[0])) {
			boolean res = verifyUname(user);
			if (res) {
				int num = 1;
				if (parts.length == 2) {
					try {
						num = Integer.parseInt(parts[1]);
					} catch (NumberFormatException e) {
						num = 1;
					}
				}
				Message[] items = bot.getMessage(num);
				for (int i = 0; i < items.length; i++) {
					if (items[i] != null) {
						Message item = items[i];
						bot.sendMessage(gu, item.getSender() + " asked: "
								+ item.getContent());
						bot.sendMessage(chan, item.getSender() + " asked: "
								+ item.getContent());
					} else {
						bot.sendMessage(gu, "MESSAGE QUEUE EMPTY");
						break;
					}
				}
			} else {
				bot.log("INVALID USER ACCESS: " + user);
			}
		} else if ("!trim".equals(parts[0])) {
			boolean res = verifyUname(user);
			if (res) {
				if (parts.length != 2) {
					bot.sendMessage(gu, "Invalid usage. Proper usage: !trim X");
					return;
				}
				try {
					int num = Integer.parseInt(parts[1]);
					if (num <= bot.getQueueSize()) {
						bot.setQueue(new LinkedList<Message>(q.subList(q.size()
								- num, q.size())));
					}
				} catch (NumberFormatException e) {
					bot.sendMessage(gu, "Invalid usage. Proper usage: !trim X");
				}
			} else {
				bot.log("INVALID USER ACCESS: " + user);
			}
		} else if ("!clear".equals(parts[0])) {
			boolean res = verifyUname(user);
			if (res) {
				q.clear();
			} else {
				bot.log("INVALID USER ACCESS: " + user);
			}
		} else if ("!count".equals(parts[0])) {
			bot.sendMessage(uname, "There are currently " + q.size()
					+ " messages in the queue.");
		} else if ("!help".equals(parts[0])) {
			boolean res = verifyUname(user);
			bot.sendMessage(uname, "!ask QUESTION");
			bot.sendMessage(uname, "!count");
			if (res) {
				bot.sendMessage(uname, "!get [X]");
				bot.sendMessage(uname, "!trim X");
				bot.sendMessage(uname, "!clear");
				bot.sendMessage(uname, "!auto <off|N D>");
			}
		} else if ("!auto".equals(parts[0])) {
			boolean res = verifyUname(user);
			if (res) {
				if (parts.length != 2 && parts.length != 3) {
					bot.sendMessage(gu,
							"Invalid usage. Proper usage: !auto <off|N D>");
				} else {
					if ("off".equals(parts[1])) {
						bot.setAutoMode(chan, false, 0, 0);
					} else {
						try {
							int num = Integer.parseInt(parts[1]);
							int interval = Integer.parseInt(parts[2]);
							bot.setAutoMode(chan, true, num, interval);
						} catch (NumberFormatException e) {
							bot.sendMessage(gu,
									"Invalid usage. Proper usage: !auto <off|N D>");
						}
					}
				}
			} else {
				bot.log("INVALID USER ACCESS: " + user);
			}
		}
	}

	private boolean verifyUname(String uname) {
		return bot.isSuperUser(uname);
	}
}
