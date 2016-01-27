package info.zepinto.util;

import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JOptionPane;

public class UIUtils {
	static public void exceptionDialog(Component parent, Exception ex, String message, String title) {
		String text = message + "\n\n" + "Error details:\n\n" + ex.toString();

		String[] buttons = { "OK", "Save" };

		int ret = JOptionPane.showOptionDialog(parent, text, title, JOptionPane.YES_NO_OPTION,
				JOptionPane.ERROR_MESSAGE, null, buttons, buttons[0]);

		if (ret == JOptionPane.NO_OPTION) {
			try {
				File report = new File("report.log");
				FileWriter out = new FileWriter(report);
				PrintWriter print = new PrintWriter(out);
				print.print(text);
				ex.printStackTrace(print);
				print.close();
			} catch (IOException ex1) {
				System.out.println(":'-( I'm really buggy! I can't do a dump of my exceptions");
				ex1.printStackTrace();
			}
		}
	}
}
