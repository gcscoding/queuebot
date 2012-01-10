package queuebot.bot;

import java.util.LinkedList;
import java.util.logging.Level;

import queuebot.queue.Message;

public class QueueBotAutoThread extends Thread {
	private QueueBot _bot;
	private int _num;
	private int _interval;
	private boolean _DEBUG;
	private long lastTime;
	private boolean doRun;

	public QueueBotAutoThread(QueueBot bot, int num, int interval, boolean DEBUG) {
		super();
		_bot = bot;
		_num = num;
		_interval = interval;
		_DEBUG = DEBUG;
		doRun = true;
	}

	public void run() {
		while (doRun) {
			try {
				Thread.sleep(_interval * 1000);
			} catch (InterruptedException e) {
				QueueBotLogger.log(Level.WARNING,
						"THREAD INTERRUPTED DURING SLEEP");
				continue;
			}
			LinkedList<Message> q = _bot.getQueue();
			String chan = _bot.getChan();
			String gu = _bot.getGU();
			lastTime = System.currentTimeMillis();
			for (int i = 0; i < _num; i++) {
				if (!q.isEmpty()) {
					Message item = q.remove();
					_bot.prepSend("PRIVMSG " + gu + " :" + item.getUsername()
							+ " asked: " + item.getContent() + "\r\n");
					_bot.prepSend("PRIVMSG " + chan + " :" + item.getUsername()
							+ " asked: " + item.getContent() + "\r\n");
				} else {
					break;
				}

			}
			_bot.trySend();
		}
	}
	
	public void shutDown() {
		doRun = false;
	}
}
