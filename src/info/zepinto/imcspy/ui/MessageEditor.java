package info.zepinto.imcspy.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import info.zepinto.util.FormatUtils;
import info.zepinto.util.UIUtils;
import pt.lsts.imc.Abort;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.PlanControl;
import pt.lsts.imc.net.UDPTransport;

public class MessageEditor extends JPanel {

	private static final long serialVersionUID = -449856037981913932L;

	public enum MODE {JSON, XML}

	private IMCMessage msg; 
	private MODE mode;
	private JToggleButton xmlToggle = new JToggleButton("XML");
	private JToggleButton jsonToggle = new JToggleButton("JSON");
	RSyntaxTextArea textArea;

	public MessageEditor() {
		setLayout(new BorderLayout());
		textArea = new RSyntaxTextArea();
		RTextScrollPane scroll = new RTextScrollPane(textArea);
		ButtonGroup bgGroup = new ButtonGroup();
		bgGroup.add(xmlToggle);
		bgGroup.add(jsonToggle);
		setMode(MODE.JSON);
		JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
		top.add(xmlToggle);
		top.add(jsonToggle);
		add(top, BorderLayout.NORTH);

		ActionListener toggleListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					msg = getMessage();
				}
				catch (Exception ex) {
				}

				if (jsonToggle.isSelected())
					setMode(MODE.JSON);
				else
					setMode(MODE.XML);
			}
		};

		jsonToggle.addActionListener(toggleListener);
		xmlToggle.addActionListener(toggleListener);

		add(scroll, BorderLayout.CENTER);

		JButton validate = new JButton("Validate");

		validate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					msg = getMessage();					
				}
				catch (Exception ex) {
					UIUtils.exceptionDialog(MessageEditor.this, ex, "Error parsing message", "Validate message");
					return;
				}
				JOptionPane.showMessageDialog(MessageEditor.this, "Message parsed successfully.", "Validate message", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		top.add(validate);
	}

	public IMCMessage getMessage() throws Exception {
		switch (mode) {
		case JSON:
			this.msg = IMCMessage.parseJson(textArea.getText());
			break;
		case XML:
			this.msg = IMCMessage.parseXml(textArea.getText());
			break;
		}
		return this.msg;
	}

	public void setMode(MODE mode) {
		this.mode = mode;
		switch (mode) {
		case JSON:
			textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
			break;
		case XML:
			textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
			break;
		}

		jsonToggle.setSelected(mode == MODE.JSON);
		xmlToggle.setSelected(mode == MODE.XML);
		setMessage(msg);
	}

	public void setMessage(IMCMessage msg) {
		if (msg == null) {
			textArea.setText("");
			return;
		}
		switch (mode) {
		case JSON:
			try {
				textArea.setText(FormatUtils.formatJSON(msg.asJSON()));
			}
			catch(Exception e) {
				e.printStackTrace();
				textArea.setText("");
			}
			break;
		case XML:
			try {
				textArea.setText(FormatUtils.formatXML(msg.asXml(false)));
			}
			catch(Exception e) {
				textArea.setText("");
			}
			break;
		}
	}

	public static void main(String[] args) throws Exception  {
		UDPTransport.sendMessage(new Abort(), "127.0.0.1", 6002);
		JFrame frm = new JFrame("Test MessageEditor");
		MessageEditor editor = new MessageEditor();
		editor.setMessage(new PlanControl());
		frm.getContentPane().add(editor);
		frm.setSize(800, 600);
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frm.setVisible(true);
	}
}
