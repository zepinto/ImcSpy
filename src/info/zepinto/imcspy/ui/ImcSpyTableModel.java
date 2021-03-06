package info.zepinto.imcspy.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import javax.swing.table.AbstractTableModel;

import info.zepinto.imcspy.CapturedMessage;

public class ImcSpyTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -7738636090730683253L;
	private static final int MAX_MESSAGES = 100000;
	private ArrayList<CapturedMessage> messages = new ArrayList<>();
	private SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS");
	{
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	public static final int IDX_TIMESTAMP = 0,
			IDX_DEVICE = 1,
			IDX_SENDER = 2,
			IDX_RECEIVER = 3,
			IDX_TYPE = 4;
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}
	
	@Override
	public String getColumnName(int column) {
		switch (column) {
		case IDX_TIMESTAMP:
			return "Timestamp";
		case IDX_DEVICE:
			return "Device";
		case IDX_RECEIVER:
			return "Receiver";
		case IDX_SENDER:
			return "Sender";
		case IDX_TYPE:
			return "Type";
		default:
			return "";
		}
	}
	
	public CapturedMessage get(int index) {
		return messages.get(index);
	}
	
	public void addMessage(CapturedMessage msg) {
		synchronized (messages) {
			if (messages.size() >= MAX_MESSAGES) {
				messages.remove(0);
				fireTableRowsDeleted(0, 1);
			}
			messages.add(msg);
			fireTableRowsInserted(messages.size()-1, messages.size()-1);
		}
		
	}
	
	public void clear() {
		synchronized (messages) {
			if (messages.size() > 0)
				fireTableRowsDeleted(0, messages.size()-1);
			messages.clear();				
		}
	}

	
	@Override
	public int getRowCount() {
		synchronized (messages) {
			return messages.size();
		}
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		CapturedMessage msg;
		
		synchronized (messages) {
			if (rowIndex < 0 || rowIndex >= messages.size())
				return null;
			msg = messages.get(rowIndex);
		}
		
		switch (columnIndex) {
		case IDX_TIMESTAMP:
			return sdf.format(msg.getTimestamp());
		case IDX_RECEIVER:
			return msg.getReceiver().getHostString()+":"+msg.getReceiver().getPort();
		case IDX_SENDER:
			return msg.getSender().getHostString()+":"+msg.getSender().getPort();
		case IDX_DEVICE:
			return msg.getDevice();
		case IDX_TYPE:
			return msg.getMessage().getAbbrev();
		default:
			return null;
		}
	}
}
