package queuebot.bot;

import queuebot.queue.Message;

/**
 * 
 * @author Winslow Dalpe
 *
 */
public class QueueBotAutoThread extends Thread {
	private QueueBot bot;
	private String channel;
	private int num;
	private int interval;
	private boolean doRun;

	/**
	 * 
	 * @param bot
	 * @param num
	 * @param interval
	 */
	public QueueBotAutoThread(QueueBot bot, int num, int interval) {
		super();
		this.bot = bot;
		this.channel = this.bot.getChannels()[0];
		this.num = num;
		this.interval = interval;
		doRun = true;
	}

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
					bot.sendMessage(gu,
							item[0].getSender() + " asked: " + item[0].getContent());
					bot.sendMessage(channel, item[0].getSender() + " asked: "
							+ item[0].getContent());

				} else {
					break;
				}

			}
		}
	}

	public void shutdown() {
		doRun = false;
	}
}
