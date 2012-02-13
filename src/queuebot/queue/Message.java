package queuebot.queue;

/**
 * A small data structure that represents a message which contains body content
 * and a sender.
 * 
 * @author Winslow Dalpe
 */
public class Message {
	private String _sender;
	private String _content;

	/**
	 * Constructs a new message
	 * 
	 * @param sender
	 * @param content
	 */
	public Message(String sender, String content) {
		_sender = sender;
		_content = content;
	}

	/**
	 * 
	 * @return
	 */
	public String getSender() {
		return _sender;
	}

	/**
	 * 
	 * @param sender
	 */
	public void setSender(String sender) {
		_sender = sender;
	}

	/**
	 * 
	 * @return
	 */
	public String getContent() {
		return _content;
	}

	/**
	 * 
	 * @param content
	 */
	public void setContent(String content) {
		_content = content;
	}

	/**
	 * Checks the content of this {@link Message} against the content of another
	 * {@link Message}. If the two contents are equal (case-insensitive), then
	 * the two messages are considered equal.
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Message) {
			Message m = (Message) o;
			return m._content.compareToIgnoreCase(this._content) == 0;
		} else {
			return false;
		}
	}
}
