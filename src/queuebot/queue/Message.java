package queuebot.queue;

/**
 * A small data structure that represents a message which contains body content
 * and a sender.
 */
public class Message {
	private String _username;
	private String _content;
	
	public Message(String username, String content) {
		_username = username;
		_content = content;
	}
	
	public String getUsername() {
		return _username;
	}
	public void setUsername(String username) {
		_username = username;
	}
	public String getContent() {
		return _content;
	}
	public void setContent(String content) {
		_content = content;
	}
}
