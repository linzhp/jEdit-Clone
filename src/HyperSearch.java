/*
 * HyperSearch.java - HyperSearch dialog
 * Copyright (C) 1998 Slava Pestov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import com.sun.java.swing.JButton;
import com.sun.java.swing.JCheckBox;
import com.sun.java.swing.JDialog;
import com.sun.java.swing.JLabel;
import com.sun.java.swing.JList;
import com.sun.java.swing.JPanel;
import com.sun.java.swing.JSeparator;
import com.sun.java.swing.JTextArea;
import com.sun.java.swing.JTextField;
import com.sun.java.swing.SwingConstants;
import com.sun.java.swing.event.ListSelectionEvent;
import com.sun.java.swing.event.ListSelectionListener;
import com.sun.java.swing.text.BadLocationException;
import com.sun.java.swing.text.Element;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

public class HyperSearch extends JDialog
implements ActionListener, ListSelectionListener, WindowListener
{
	private View view;
	private JTextField find;
	private JCheckBox ignoreCase;
	private JButton findBtn;
	private JButton close;
	private JList results;
	
	public HyperSearch(View view)
	{
		super(view,jEdit.props.getProperty("hypersearch.title"),false);
		this.view = view;
		find = new JTextField(jEdit.props.getProperty("lastfind"),20);
		ignoreCase = new JCheckBox(jEdit.props.getProperty(
			"hypersearch.ignoreCase"),
			"on".equals(jEdit.props.getProperty("ignoreCase")));
		findBtn = new JButton(jEdit.props.getProperty(
			"hypersearch.findBtn"));
		close = new JButton(jEdit.props.getProperty(
			"hypersearch.close"));
		results = new JList();
		results.setVisibleRowCount(10);
		results.setFont(view.getTextArea().getFont());
		results.addListSelectionListener(this);
		getContentPane().setLayout(new BorderLayout());
		JPanel panel = new JPanel();
		panel.add(new JLabel(jEdit.props.getProperty(
			"hypersearch.find")));
		panel.add(find);
		panel.add(ignoreCase);
		panel.add(findBtn);
		panel.add(close);
		getContentPane().add("North",panel);
		getContentPane().add("Center",new JSeparator());
		getContentPane().add("South",results);
		Dimension screen = getToolkit().getScreenSize();
		pack();
		setLocation((screen.width - getSize().width) / 2,
			(screen.height - getSize().height) / 2);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		findBtn.addActionListener(this);
		close.addActionListener(this);
		show();
	}
	
	public void save()
	{
		jEdit.props.put("lastfind",find.getText());
		jEdit.props.put("ignoreCase",ignoreCase.getModel().isSelected()
			? "on" : "off");
	}
	
	public void actionPerformed(ActionEvent evt)
	{
		save();
		Object source = evt.getSource();
		if(source == close)
			dispose();
		else if(source == findBtn)
			doHyperSearch();
	}

	private void doHyperSearch()
	{
		try
		{
			int tabSize = view.getTextArea().getTabSize();
			Vector data = new Vector();
			results.setListData(data);
			char[] pattern = find.getText().toCharArray();
			Buffer buffer = view.getBuffer();
			Element map = buffer.getDefaultRootElement();
			int lines = map.getElementCount();
			for(int i = 1; i <= lines; i++)
			{
				Element lineElement = map.getElement(i - 1);
				int start = lineElement.getStartOffset();
				String lineString = buffer.getText(start,
					lineElement.getEndOffset() - start);
				char[] line = lineString.toCharArray();
				if(jEdit.find(pattern,line,0) != -1)
					data.addElement(i + ":"
						+ jEdit.untab(tabSize,
							lineString));
			}
			results.setListData(data);
			pack();
		}
		catch(BadLocationException bl)
		{
			Object[] args = { bl.toString() };
			jEdit.error(view,"error",args);
		}
	}
	
	public void valueChanged(ListSelectionEvent evt)
	{
		if(results.isSelectionEmpty())
			return;
		try
		{
			String selected = (String)results.getSelectedValue();
			int line = Integer.parseInt(selected.substring(0,
				selected.indexOf(':')));
			JTextArea textArea = view.getTextArea();
			textArea.setCaretPosition(
				textArea.getLineStartOffset(line - 1));
		}
		catch(Exception e)
		{
			Object[] args = { e.toString() };
			jEdit.error(view,"error",args);
		}
	}
	
	public void windowOpened(WindowEvent evt) {}
	
	public void windowClosing(WindowEvent evt)
	{
		save();
		dispose();
	}
	
	public void windowClosed(WindowEvent evt) {}
	public void windowIconified(WindowEvent evt) {}
	public void windowDeiconified(WindowEvent evt) {}
	public void windowActivated(WindowEvent evt) {}
	public void windowDeactivated(WindowEvent evt) {}
}
