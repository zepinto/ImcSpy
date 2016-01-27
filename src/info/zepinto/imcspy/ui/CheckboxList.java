package info.zepinto.imcspy.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * @author zp
 *
 */
public class CheckboxList extends JList<JCheckBox> {
    private static final long serialVersionUID = 7637534594039239173L;
    protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

    public CheckboxList() {
        setCellRenderer(new CellRenderer());

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int index = locationToIndex(e.getPoint());

                if (index != -1) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        JPopupMenu popup = new JPopupMenu();
                        popup.add("Select all").addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                for (int i = 0; i < getModel().getSize(); i++)
                                    getModel().getElementAt(i).setSelected(true);
                                repaint();
                            }
                        });

                        popup.add("Select none").addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                for (int i = 0; i < getModel().getSize(); i++)
                                    getModel().getElementAt(i).setSelected(false);
                                repaint();
                            }
                        });

                        popup.show(CheckboxList.this, getX(), e.getY());
                    }
                    else {
                        JCheckBox checkbox = getModel().getElementAt(index);
                        checkbox.setSelected(!checkbox.isSelected());
                        repaint();
                    }
                }
            }
        });

        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    protected class CellRenderer implements ListCellRenderer<JCheckBox> {
        public Component getListCellRendererComponent(JList<? extends JCheckBox> list, JCheckBox checkbox, int index,
                boolean isSelected, boolean cellHasFocus) {
            checkbox.setBackground(isSelected ? getSelectionBackground() : getBackground());
            checkbox.setForeground(isSelected ? getSelectionForeground() : getForeground());
            checkbox.setEnabled(isEnabled());
            checkbox.setFont(getFont());
            checkbox.setFocusPainted(false);
            checkbox.setBorderPainted(true);
            checkbox.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
            return checkbox;
        }
    }

    public static CheckboxList getInstance(String... options) {
        JCheckBox[] items = new JCheckBox[options.length];

        for (int i = 0; i < options.length; i++)
            items[i] = new JCheckBox(options[i]);

        CheckboxList list = new CheckboxList();
        list.setListData(items);

        return list;
    }

    public String[] getSelectedStrings() {
        ArrayList<String> selected = new ArrayList<>();
        for (int i = 0; i < getModel().getSize(); i++) {
            if (getModel().getElementAt(i).isSelected())
                selected.add(getModel().getElementAt(i).getText());
        }

        return selected.toArray(new String[selected.size()]);
    }
    
    public static String[] selectOptions(Component parent, String title, LinkedHashMap<String, Boolean> options) {
    	CheckboxList checkList = CheckboxList.getInstance(options.keySet().toArray(new String[0]));

    	for (int i = 0; i < checkList.getModel().getSize(); i++) {
    		String name = checkList.getModel().getElementAt(i).getText();
    		if (options.get(name) != null && options.get(name))
    			checkList.getModel().getElementAt(i).setSelected(true);
    	}
    	
    	int op = JOptionPane.showConfirmDialog(parent, new JScrollPane(checkList), title, JOptionPane.OK_CANCEL_OPTION);
        if (op != JOptionPane.OK_OPTION)
            return null;
        return checkList.getSelectedStrings();
    }
    

    public static String[] selectOptions(Component parent, String title, String... options) {
        CheckboxList checkList = CheckboxList.getInstance(options);

        int op = JOptionPane.showConfirmDialog(parent, new JScrollPane(checkList), title, JOptionPane.OK_CANCEL_OPTION);
        if (op != JOptionPane.OK_OPTION)
            return null;
        return checkList.getSelectedStrings();
    }

    // example usage
    public static void main(String[] args) {
        String[] options = selectOptions(null, "Title", "Option A", "Option B", "Option C");
        if (options == null)
            System.out.println("User cancelled the dialog");
        else
            System.out.println(Arrays.asList(options));
    }
}
