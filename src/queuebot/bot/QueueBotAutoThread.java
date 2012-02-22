package queuebot.bot;

import queuebot.queue.Message;

/**
 * QueueBotAutoThreads are a singleton which prints a certain number of messages
 * from a bot's queue every so often.
 * 
 * @author Winslow Dalpe
 * 
 */
public class QueueBotAutoThread extends Thread {
	private QueueBot bot;
	private String channel;
	private int num;
	private int interval;
	private static boolean doRun;
	private static QueueBotAutoThread instance;

	/**
	 * Constructs a new QueueBotAutoThread
	 * 
	 * @param bot
	 *            the QueueBot with which the thread is associated
	 * @param num
	 *            the number of messages to print
	 * @param interval
	 *            the interval between groups of printings
	 */
	private QueueBotAutoThread(QueueBot bot, int num, int interval) {
		super();
		this.bot = bot;
		this.channel = this.bot.getChannels()[0];
		this.num = num;
		this.interval = interval;
	}

	/**
	 * 
	 * @param bot
	 *            the QueueBot with which the thread is associated
	 * @param num
	 *            the number of messages to print
	 * @param interval
	 *            the interval between groups of printings
	 * @return a singleton instance of a QueueBotAutoThread
	 */
	public static synchronized QueueBotAutoThread getInstance(QueueBot bot,
			int num, int interval) {
		if (!doRun || instance == null) {
			doRun = true;
			instance = new QueueBotAutoThread(bot, num, interval);
		}

		return instance;
	}

	/**
	 * If an instance is running, wait for a certain interval then print a
	 * certain number of messages from the bot's queue.
	 */
	@Override
	public void run() {
		while (doRun) {
			try {
				Thread.sleep(interval * 1000);
			} catch (InterruptedException e) {
				bot.log("THREAD INTERRUPTED DURING SLEEP");
				continue;
			}
			String gu = bot.getSuperUser();
			for (int i = 0; i < num; i++) {
				Message item[] = bot.getMessage(1);
				if (item[0] != null) {
					bot.sendMessage(gu, item[0].getSender() + " asked: "
							+ item[0].getContent());
					bot.sendMessage(channel, item[0].getSender() + " asked: "
							+ item[0].getContent());

				} else {
					break;
				}

			}
		}
	}

	/**
	 * Destroys an active instance of a QueueBotAutoThread
	 */
	public static synchronized void closeInstance() {
		if (doRun) {
			doRun = false;
			instance = null;
		}
	}
}
