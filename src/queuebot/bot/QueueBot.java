package queuebot.bot;

import org.jibble.pircbot.PircBot;

import queuebot.queue.Message;
import queuebot.bot.QueueBotThread;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class QueueBot extends PircBot {

	private boolean hasSetName;
	private String superuser;
	private LinkedList<Message> mq;
	private boolean autoMode;
	private QueueBotAutoThread autoThread;

	public QueueBot(String channel, String superuser) {
		this.superuser = superuser;
		hasSetName = false;
		mq = new LinkedList<Message>();
		autoMode = false;
		autoThread = null;
	}

	public boolean isSuperUser(String nick) {
		return superuser.equals(nick);
	}

	public String getSuperUser() {
		return superuser;
	}

	public void setSuperUser(String user) {
		superuser = user;
	}

	public void subSetName(String name) {
		if (!hasSetName) {
			this.setName(name);
			this.setLogin(name);
			hasSetName = true;
		} else {
			this.changeNick(name);
		}
	}

	protected void onJoin(String channel, String nick, String login,
			String hostname) {
		channel = channel.toLowerCase();
		if (nick.equals(getNick())) {
			if (channel.equals("#guardsmanbob")) {
				sendMessage(channel, "I rawr Bob!");
			}
		}
	}

	protected void onMessage(String channel, String sender, String login,
			String hostname, String message) {
		onPrivateMessage(sender, login, hostname, message);
	}

	protected void onPrivateMessage(String sender, String login,
			String hostname, String message) {
		if (message.charAt(0) == '!') {
			Thread t = new Thread(new QueueBotThread(this, sender, message));
			t.setDaemon(true);
			t.start();
		}
	}

	public void addMessage(Message m) {
		mq.add(m);
	}

	public synchronized Message[] getMessage(String channel, int num) {
		channel = channel.toLowerCase();
		Message[] m = new Message[num];
		if (!mq.isEmpty()) {
			for (int i = 0; i < num; i++) {
				try {
					m[i] = mq.remove();
				} catch (NoSuchElementException e) {
					m[i] = null;
				}
			}
			return m;
		} else {
			return null;
		}
	}

	public boolean containsMessage(Message m) {
		return mq.contains(m);
	}

	public int getQueueSize() {
		return mq.size();
	}

	public synchronized void setQueue(LinkedList<Message> q) {
		mq = q;
	}

	public LinkedList<Message> getQueue() {
		return mq;
	}

	public synchronized void setAutoMode(String channel, boolean value,
			int num, int interval) {
		channel = channel.toLowerCase();
		if (value && !autoMode) {
			autoThread = new QueueBotAutoThread(this, num, interval);
			autoThread.setDaemon(true);
			autoThread.start();
			autoMode = true;
		} else {
			if (autoThread != null && autoThread.isAlive()) {
				autoThread.shutdown();
				autoMode = false;
			}
		}
	}
}
