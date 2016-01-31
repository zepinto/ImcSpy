package info.zepinto.imcspy;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import info.zepinto.imcspy.ui.ImcSpyApp;
import info.zepinto.util.NativeUtils;
import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;

public class ImcSpy {

	private static ExecutorService execService = Executors.newCachedThreadPool();
	private static boolean loaded = false;
	public static void loadJpcap() throws Exception {
		if (loaded)
			return;
		loaded = true;
		
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
			throw new Exception("Unsupported Operating System: "+os);
		}
	}
	
	public ImcSpy(String... interfaces) throws Exception {
		loadJpcap();
		NetworkInterface[] devices = JpcapCaptor.getDeviceList();
		List<String> nis = Arrays.asList(interfaces);
		int count = 0;
		for (NetworkInterface itf : devices) {
			if (nis.contains(itf.name)) {				
				startCapturing(itf, null);
				count++;
			}			
		}
		System.out.println("Capturing from "+count+" interfaces.");
	}
	
	public static void startCapturing(final NetworkInterface itf, CapturedMessageHandler handler) throws Exception {
		loadJpcap();
		final JpcapCaptor captor = JpcapCaptor.openDevice(itf, 65535, false, 20);
		captor.setFilter("proto UDP", true);
		execService.submit(new Runnable() {
			public void run() {
				ImcCapturer capturer = new ImcCapturer(itf.name);
				capturer.setHandler(handler);
				while (!Thread.interrupted())
					captor.loopPacket(3, capturer);
			}
		});
	}
	
	public static void stopCapturing() {
		execService.shutdownNow();
		execService = Executors.newCachedThreadPool(); 
	}

	public static void main(String[] args) throws Exception {
		loadJpcap();
		if (args.length == 0) {
			NetworkInterface[] devices = JpcapCaptor.getDeviceList();
			System.err.println("Usage: java -jar ImcSpy.jar [options] <interfaces>?");
			System.err.println();
			System.err.println("Options:");
			System.err.println("   -gui\t\t\tStart visual interface");
			System.err.println();
			System.err.println("Available interfaces in this machine:");
			for (NetworkInterface ni : devices) {
				System.err.println("   " + ni.name);
			}
			return;
		}
		else {
			if (args[0].equals("-gui"))
				ImcSpyApp.main(Arrays.asList(args).subList(1, args.length).toArray(new String[0]));
		}
	}

}
