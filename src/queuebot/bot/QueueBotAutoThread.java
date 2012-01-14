package queuebot.bot;

import java.util.LinkedList;

import queuebot.queue.Message;

public class QueueBotAutoThread extends Thread {
	private QueueBot bot;
	private String channel;
	private int num;
	private int interval;
	private boolean doRun;

	public QueueBotAutoThread(QueueBot bot, int num,
			int interval) {
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
			LinkedList<Message> q = bot.getQueue();
			String gu = bot.getSuperUser();
			for (int i = 0; i < num; i++) {
				if (!q.isEmpty()) {
					Message item = q.remove();
					bot.sendMessage(gu,
							item.getUsername() + " asked: " + item.getContent());
					bot.sendMessage(channel, item.getUsername() + " asked: "
							+ item.getContent());
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
