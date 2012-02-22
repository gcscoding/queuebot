package queuebot.bot;

import org.jibble.pircbot.PircBot;

import queuebot.queue.Message;
import queuebot.bot.QueueBotThread;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * QueueBot is a basic PIRC bot with the ability to keep track of a message
 * queue. Users may add messages to the queue and request its size. A superuser
 * may manipulate messages in the queue and print them to the channel.
 * 
 * @author Winslow Dalpe
 * 
 */
public class QueueBot extends PircBot {

	private boolean hasSetName;
	private String superuser;
	private LinkedList<Message> mq;
	private boolean autoMode;
	private QueueBotAutoThread autoThread;

	/**
	 * Constructs a new QueueBot
	 * 
	 * @param channel
	 *            the bot joins this channel after connecting
	 * @param superuser
	 *            the nickname of this bot's superuser
	 */
	public QueueBot(String channel, String superuser) {
		this.superuser = superuser;
		hasSetName = false;
		mq = new LinkedList<Message>();
		autoMode = false;
		autoThread = null;
	}

	/**
	 * Checks if a given nickname is the nickname of the bot's superuser
	 * 
	 * @param nick
	 * @return true if nick is the nickname of the superuser
	 */
	public boolean isSuperUser(String nick) {
		return superuser.equals(nick);
	}

	/**
	 * 
	 * @return the superuser's nickname
	 */
	public String getSuperUser() {
		return superuser;
	}

	/**
	 * Sets important login information.
	 * 
	 * @param name
	 *            should be the desired nickname of the bot
	 */
	public void subSetName(String name) {
		if (!hasSetName) {
			this.setName(name);
			this.setLogin(name);
			hasSetName = true;
		} else {
			this.changeNick(name);
		}
	}

	/**
	 * Overrides the default {@link PircBot} onJoin method
	 * 
	 * @param channel
	 *            The name of the channel that was joined
	 * @param nick
	 *            the nickname of whoever just joined channel
	 * @param login
	 *            the login name of whoever just joined
	 * @param hostname
	 *            the hostname of whoever just joined
	 */
	@Override
	protected void onJoin(String channel, String nick, String login,
			String hostname) {
		channel = channel.toLowerCase();
		if (nick.equals(getNick())) {
			if (channel.equals("#guardsmanbob")) {
				sendMessage(channel, "I rawr Bob!");
			}
		}
	}

	// protected void onMessage(String channel, String sender, String login,
	// String hostname, String message) {
	// onPrivateMessage(sender, login, hostname, message);
	// }

	/**
	 * Overrides the default {@link PircBot} onPrivateMessage method
	 * 
	 * @param sender
	 *            the nickname of the sender
	 * @param the
	 *            login name of the sender
	 * @param hostname
	 *            the hostname of the send
	 * @param message
	 */
	protected void onPrivateMessage(String sender, String login,
			String hostname, String message) {
		if (message.charAt(0) == '!') {
			Thread t = new Thread(new QueueBotThread(this, sender, message));
			t.setDaemon(true);
			t.start();
		}
	}

	/**
	 * Adds a {@link Message} to the QueueBot's queue
	 * 
	 * @param m
	 */
	public void addMessage(Message m) {
		mq.add(m);
	}

	/**
	 * Pops an array of {@link Message} off the queue
	 * 
	 * @param num
	 *            the number of messages to pop off the queue
	 * @return an array of messages with an element equal to null if the queue
	 *         is empty
	 */
	public synchronized Message[] getMessage(int num) {
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
			m[0] = null;
			return m;
		}
	}

	/**
	 * 
	 * @param m
	 * @return true if the queue contains the {@link Message} m
	 */
	public boolean containsMessage(Message m) {
		return mq.contains(m);
	}

	/**
	 * 
	 * @return the size of the queue
	 */
	public int getQueueSize() {
		return mq.size();
	}

	/**
	 * Sets the bot's queue to a certain state
	 * 
	 * @param q
	 *            the new queue for the bot
	 */
	public synchronized void setQueue(LinkedList<Message> q) {
		mq = q;
	}

	/**
	 * 
	 * @return the bot's queue
	 */
	public LinkedList<Message> getQueue() {
		return mq;
	}

	/**
	 * Puts the bot into auto mode. While in auto mode, the bot will attempt to
	 * print a certain number of messages from its queue every so often.
	 * 
	 * @param value
	 *            true if the bot should enter auto mode
	 * @param num
	 *            the number of messages to print
	 * @param interval
	 *            the interval between printings
	 */
	public synchronized void setAutoMode(boolean value, int num, int interval) {
		if (value && !autoMode) {
			autoThread = QueueBotAutoThread.getInstance(this, num, interval);
			autoThread.setDaemon(true);
			autoThread.start();
			autoMode = true;
		} else {
			if (autoThread != null && autoThread.isAlive()) {
				QueueBotAutoThread.closeInstance();
				autoThread = null;
				autoMode = false;
			}
		}
	}
}
