package info.zepinto.imcspy;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import jpcap.PacketReceiver;
import jpcap.packet.Packet;
import jpcap.packet.UDPPacket;
import pt.lsts.imc.IMCDefinition;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.lsf.LsfMessageLogger;

public class ImcCapturer implements PacketReceiver {

	long sync = IMCDefinition.getInstance().getSyncWord();
	long swapped_sync = IMCDefinition.getInstance().getSwappedWord();
	private String device;
	private CapturedMessageHandler handler = null;
	public ImcCapturer(String device) {
		this.device = device;
	}
	
	public void setHandler(CapturedMessageHandler handler) {
		this.handler = handler;
	}
	
	@Override
	public void receivePacket(Packet p) {
		if (p instanceof UDPPacket) {
			UDPPacket packet = (UDPPacket) p;
			ByteBuffer buffer = ByteBuffer.wrap(packet.data);
			long read = buffer.getShort();
			if (read == swapped_sync)
				buffer.order(ByteOrder.LITTLE_ENDIAN);
			else if (read != sync) {
				return;
			}

			buffer.position(0);
			try {
				IMCMessage m = IMCDefinition.getInstance().nextMessage(buffer);
				LsfMessageLogger.log(m);
				CapturedMessage msg = new CapturedMessage()
						.withMessage(m)
						.withReceiver(new InetSocketAddress(packet.dst_ip, packet.dst_port))
						.withSender(new InetSocketAddress(packet.src_ip, packet.src_port))
						.withDevice(device);
				if (handler == null)
					System.out.println(msg);
				else
					handler.captured(msg);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
