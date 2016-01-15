package info.zepinto.imcspy.ui;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

import info.zepinto.imcspy.CapturedMessage;
import info.zepinto.imcspy.CapturedMessageHandler;
import info.zepinto.imcspy.ImcSpy;
import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import pt.lsts.imc.IMCDefinition;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.IMCUtil;
import pt.lsts.imc.lsf.LsfMessageLogger;

public class ImcSpyApp extends JPanel implements CapturedMessageHandler{
	private static final long serialVersionUID = 6663249963843784261L;
	private ImcSpyTableModel model = new ImcSpyTableModel();
	private TableRowSorter<ImcSpyTableModel> sorter = new TableRowSorter<ImcSpyTableModel>(model);
	private MessageFilter filter = new MessageFilter();
	
	private JTable table = new JTable(model);
	{
		sorter.setRowFilter(filter);
		table.setRowSorter(sorter);		
	}	
	private JEditorPane editor = new JEditorPane();
	
	ArrayList<String> selectedDevices = new ArrayList<String>();
	
	public ImcSpyApp() {
		this(new ArrayList<String>());
	}
	
	public ImcSpyApp(List<String> selectedDevices) {
		this.selectedDevices.addAll(selectedDevices);
		setLayout(new BorderLayout());
		JScrollPane pane = new JScrollPane(table);
		editor.setContentType("text/html");
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				try {
					int index = table.convertRowIndexToModel(table.getSelectedRow());
					IMCMessage msg = model.get(index).getMessage();
					editor.setText(IMCUtil.getAsHtml(msg));
					editor.setCaretPosition(0);			
				}
				catch (Exception ex) {
					editor.setText("");
				}
			}
		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					try {
						int index = table.convertRowIndexToModel(table.getSelectedRow());
						final IMCMessage msg = model.get(index).getMessage();
						JPopupMenu popup = new JPopupMenu();
						popup.add("Copy to clipboard as XML").addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								String xml = msg.asXml(false);
								StringSelection stringSelection = new StringSelection(xml);
								Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
								clpbrd.setContents(stringSelection, null);
							}
						});
						
						popup.add("Copy to clipboard as JSON").addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								String json = msg.asJSON(true);
								StringSelection stringSelection = new StringSelection(json);
								Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
								clpbrd.setContents(stringSelection, null);
							}
						});
						
						popup.show(table, e.getX(), e.getY());
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}					
				}
			}
		});
		editor.setEditable(false);
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pane, new JScrollPane(editor));
		add(split, BorderLayout.CENTER);	
		
		if (!this.selectedDevices.isEmpty())
			startCapture();
		
		
	}
	
	private boolean selectDevices() throws Exception {
		ImcSpy.loadJpcap();
		NetworkInterface[] devices = JpcapCaptor.getDeviceList();
		JPanel al = new JPanel(new GridLayout(0, 1));
		LinkedHashMap<String, JCheckBox> checkBoxes = new LinkedHashMap<>();
		for (NetworkInterface itf : devices) {
			JCheckBox box = new JCheckBox(itf.name);
			box.setSelected(selectedDevices.contains(itf.name));
			al.add(box);
			checkBoxes.put(itf.name, box);
		}
		int op = JOptionPane.showConfirmDialog(this, al, "Select devices to capture", JOptionPane.OK_CANCEL_OPTION);
		
		if (op != JOptionPane.OK_OPTION)
			return false;
		
		selectedDevices.clear();
		for (Entry<String, JCheckBox> entry : checkBoxes.entrySet()) {
			if (entry.getValue().isSelected())
				selectedDevices.add(entry.getKey());
		}
		
		return true;
	}
	
	private void startCapture() {
		try {
			ImcSpy.loadJpcap();
			for (NetworkInterface itf : JpcapCaptor.getDeviceList())
				if (selectedDevices.contains(itf.name))
					ImcSpy.startCapturing(itf, this);				
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void stopCapture() {
		ImcSpy.stopCapturing();
	}
	
	@Override
	public void captured(CapturedMessage message) {
		model.addMessage(message);
		LsfMessageLogger.log(message.getMessage());
	}
	
	class ActionStartCapture extends AbstractAction {
		private static final long serialVersionUID = -6843355503327783623L;
		boolean started = false;
		{
			if (selectedDevices.isEmpty())
				putValue(NAME, "Start Capture");
			else {
				putValue(NAME, "Stop Capture");
				started = true;
			}
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (started) {
				putValue(NAME, "Start Capture");
				stopCapture();
				started = false;
			}
			else {
				try {
					if (selectDevices()) {
						putValue(NAME, "Stop Capture");
						startCapture();
						started = true;
					}
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
				
			}
		}		
	}
	
	class ActionClear extends AbstractAction {
		private static final long serialVersionUID = 6063332621136883241L;
		{	
			putValue(NAME, "Clear"); 	
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			model.clear();
		}
	}
	
	class ActionAllowedMessages extends AbstractAction {
		private static final long serialVersionUID = 1L;
		{	
			putValue(NAME, "Filter by message type"); 	
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			
			LinkedHashMap<String, Boolean> options = new LinkedHashMap<>();
			IMCDefinition.getInstance().getMessageNames().forEach(m -> options.put(m, false));
			filter.getAllowedMessages().forEach(m -> options.put(m, true));
			
			String[] messages = CheckboxList.selectOptions(ImcSpyApp.this, "Messages to show",
					options);
			
			if (messages == null)
				return;
			
			filter.setAllowedMessages(messages);			
		}
	}
	
	public List<AbstractAction> getFileActions() {
		return Arrays.asList(new ActionStartCapture(), new ActionClear());
	}
	
	public List<AbstractAction> getOptionsActions() {
		return Arrays.asList(new ActionAllowedMessages());
	}	
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("IMC Spy");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ImcSpyApp spyApp = new ImcSpyApp();
		frame.getContentPane().add(spyApp);
		JMenuBar menubar = new JMenuBar();
		frame.setJMenuBar(menubar);
		
		JMenu file = new JMenu("File");
		for (AbstractAction act : spyApp.getFileActions())
			file.add(new JMenuItem(act));
		menubar.add(file);
		System.out.println("Add options!");
		JMenu options = new JMenu("Options");
		for (AbstractAction act : spyApp.getOptionsActions())
			options.add(new JMenuItem(act));
		menubar.add(options);
		
		frame.setSize(800, 600);
		frame.setVisible(true);
	}
}
