package info.zepinto.imcspy.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Predicate;

import javax.swing.RowFilter;

import info.zepinto.imcspy.CapturedMessage;

public class MessageFilter extends RowFilter<ImcSpyTableModel, Integer> {

	private ArrayList<Predicate<String>> predDevices = null;
	private ArrayList<Predicate<String>> predSenders = null;
	private ArrayList<Predicate<String>> predReceivers = null;
	private ArrayList<Predicate<String>> predTypes = null;	
	
	private ArrayList<String> allowedDevices = new ArrayList<>();
	private ArrayList<String> allowedReceivers = new ArrayList<>();
	private ArrayList<String> allowedSenders = new ArrayList<>();
	private ArrayList<String> allowedMessages = new ArrayList<>();
	
	@Override
	public boolean include(javax.swing.RowFilter.Entry<? extends ImcSpyTableModel, ? extends Integer> entry) {
		CapturedMessage msg = entry.getModel().get(entry.getIdentifier());
		
		if (predDevices != null) {
			for (Predicate<String> p : predDevices)
				if (p.test(msg.getDevice()))
					return true;
			return false;			
		}
		
		if (predSenders != null) {
			for (Predicate<String> p : predSenders)
				if (p.test(msg.getSender().getHostString()))
					return true;
			return false;			
		}
		
		if (predReceivers != null) {
			for (Predicate<String> p : predReceivers)
				if (p.test(msg.getReceiver().getHostString()))
					return true;
			return false;			
		}
		
		if (predTypes != null) {
			for (Predicate<String> p : predTypes)
				if (p.test(msg.getMessage().getAbbrev()))
					return true;
			return false;			
		}
		
		return true;
	}
	
	public void setAllowedDevices(String...devices) {
		allowedDevices.addAll(Arrays.asList(devices));

		Predicate<String> p = new Predicate<String>() {
			@Override
			public boolean test(String t) {
				return allowedDevices.contains(t);
			}
		};
		
		if (predDevices == null)
			predDevices = new ArrayList<>();
		predDevices.clear();
		predDevices.add(p);
	}
	
	public void setAllowedReceivers(String...receivers) {
		
		allowedReceivers.addAll(Arrays.asList(receivers));

		Predicate<String> p = new Predicate<String>() {
			@Override
			public boolean test(String t) {
				return allowedReceivers.contains(t);
			}
		};
		
		if (predReceivers == null)
			predReceivers = new ArrayList<>();
		predReceivers.clear();
		predReceivers.add(p);
	}
	
	public void setAllowedSenders(String...senders) {
		
		allowedSenders.addAll(Arrays.asList(senders));

		Predicate<String> p = new Predicate<String>() {
			@Override
			public boolean test(String t) {
				return allowedSenders.contains(t);
			}
		};
		if (predSenders == null)
			predSenders = new ArrayList<>();

		predSenders.clear();
		predSenders.add(p);
	}
	
	public void setAllowedMessages(String...abbrevs) {
		
		allowedMessages.addAll(Arrays.asList(abbrevs));

		Predicate<String> p = new Predicate<String>() {
			@Override
			public boolean test(String t) {
				return allowedMessages.contains(t);
			}
		};
		if (predTypes == null)
			predTypes = new ArrayList<>();
		predTypes.clear();
		predTypes.add(p);
	}	
	
	public ArrayList<String> getAllowedDevices() {
		return allowedDevices;
	}

	public ArrayList<String> getAllowedReceivers() {
		return allowedReceivers;
	}

	public ArrayList<String> getAllowedSenders() {
		return allowedSenders;
	}

	public ArrayList<String> getAllowedMessages() {
		return allowedMessages;
	}
}
