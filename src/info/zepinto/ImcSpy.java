package info.zepinto;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.PacketReceiver;
import jpcap.packet.Packet;
import jpcap.packet.UDPPacket;
import pt.lsts.imc.Announce;
import pt.lsts.imc.IMCDefinition;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.lsf.LsfMessageLogger;

public class ImcSpy {

	public static void loadJpcap() throws Exception {
		String os = System.getProperty("os.name");
		String arch = System.getProperty("os.arch");

		if (os.equalsIgnoreCase("windows")) {
			NativeUtils.loadLibraryFromJar("/jni/windows/" + System.mapLibraryName("jpcap"));
		} 
		else if (os.equalsIgnoreCase("linux")) {
			if (arch.equalsIgnoreCase("amd64"))
				NativeUtils.loadLibraryFromJar("/jni/linux64/" + System.mapLibraryName("jpcap"));
			else
				NativeUtils.loadLibraryFromJar("/jni/linux32/" + System.mapLibraryName("jpcap"));
		} 
		else {
			throw new Exception("Unsupported Operating System.");
		}
	}

	public static void main(String[] args) throws Exception {
		loadJpcap();
		NetworkInterface[] devices = JpcapCaptor.getDeviceList();
		if (args.length == 0) {
			System.err.println("Usage: java -jar ImcSpy.jar <interfaces to listen>");
			System.err.println("Available interfaces in this machine:");
			for (NetworkInterface ni : devices) {
				System.err.println(" * " + ni.name);
			}
			return;
		}

		List<String> nis = Arrays.asList(args);
		int count = 0;
		ExecutorService service = Executors.newCachedThreadPool();
		long sync = IMCDefinition.getInstance().getSyncWord();
		long swapped_sync = IMCDefinition.getInstance().getSwappedWord();

		for (NetworkInterface d : devices) {

			if (!nis.contains(d.name))
				continue;

			count++;
			service.submit(new Runnable() {
				final JpcapCaptor captor = JpcapCaptor.openDevice(d, 65535, false, 20);

				{
					try {
						captor.setFilter("proto UDP", true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				@Override
				public void run() {
					captor.loopPacket(-1, new PacketReceiver() {

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
									String txt = " -- " + m.getAbbrev();
									LsfMessageLogger.log(m);
									LsfMessageLogger.getLogDirSingleton();
									if (m.getMgid() == Announce.ID_STATIC)
										txt = " -- Announce [" + ((Announce) m).getSysName() + "]";
									txt += " ("+p.data.length+" Bytes)";
								
									System.out.printf("%8s / %15s:%-5s --> %15s:%-5s %s\n", 
											d.name, packet.src_ip.getHostAddress(), packet.src_port,
											packet.dst_ip.getHostAddress(), packet.dst_port, txt);									
									
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					});
				}
			});
		}

		if (count == 0) {
			System.err.println("Invalid interface. Run without arguments to list available interfaces.");
		}
	}

}
