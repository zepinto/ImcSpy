package info.zepinto.imcspy;

import java.net.InetSocketAddress;
import java.util.Date;

import pt.lsts.imc.Announce;
import pt.lsts.imc.IMCMessage;

public class CapturedMessage {
	private InetSocketAddress sender = null, receiver = null;
	private Date timestamp = new Date();
	private IMCMessage message = null;
	private String device = null;

	public CapturedMessage withSender(InetSocketAddress sender) {
		setSender(sender);
		return this;
	}

	public CapturedMessage withReceiver(InetSocketAddress receiver) {
		setReceiver(receiver);
		return this;
	}

	public CapturedMessage withMessage(IMCMessage message) {
		setMessage(message);
		return this;
	}

	public CapturedMessage withTimestamp(Date timestamp) {
		setTimestamp(timestamp);
		return this;
	}
	
	public CapturedMessage withDevice(String device) {
		setDevice(device);
		return this;
	}

	/**
	 * @return the sender
	 */
	public InetSocketAddress getSender() {
		return sender;
	}

	/**
	 * @param sender
	 *            the sender to set
	 */
	public void setSender(InetSocketAddress sender) {
		this.sender = sender;
	}

	/**
	 * @return the receiver
	 */
	public InetSocketAddress getReceiver() {
		return receiver;
	}

	/**
	 * @param receiver
	 *            the receiver to set
	 */
	public void setReceiver(InetSocketAddress receiver) {
		this.receiver = receiver;
	}

	/**
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp
	 *            the timestamp to set
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the message
	 */
	public IMCMessage getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(IMCMessage message) {
		this.message = message;
	}

	/**
	 * @return the device
	 */
	public String getDevice() {
		return device;
	}

	/**
	 * @param device
	 *            the device to set
	 */
	public void setDevice(String device) {
		this.device = device;
	}

	@Override
	public String toString() {
		String txt = " | " + getMessage().getAbbrev();
		if (getMessage().getMgid() == Announce.ID_STATIC)
			txt = " | Announce [" + ((Announce) getMessage()).getSysName() + "]";
		txt += " ("+getMessage().getSize()+" Bytes)";
		return String.format("%7s | %15s:%-5s >> %15s:%-5s %s", getDevice(), getSender().getHostString(), getSender().getPort(),
				getReceiver().getHostString(), getReceiver().getPort(), txt);
	}
}
