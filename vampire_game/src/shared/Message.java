package shared;

import java.io.Serializable;

public class Message implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private MessageType type;
	private String sender;
	private String content;
	
	public Message(MessageType type, String sender, String content){
		this.type=type;
		this.sender=sender;
		this.content=content;
	}

	public MessageType getType() {return type;}
	public String getSender() {return sender;}
	public String getContent() {return content;}

	@Override
	public String toString() {
		 return "[" + type + "] " + sender + ": " + content;
	}
	
}
