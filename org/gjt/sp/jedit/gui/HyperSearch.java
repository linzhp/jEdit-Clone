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

package org.gjt.sp.jedit.gui;

import com.sun.java.swing.*;
import com.sun.java.swing.event.*;
import com.sun.java.swing.text.*;
import gnu.regexp.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import org.gjt.sp.jedit.syntax.SyntaxTextArea;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.View;

public class HyperSearch extends JDialog
implements ActionListener, ListSelectionListener, WindowListener
{
	private View view;
	private JTextField find;
	private JCheckBox ignoreCase;
	private JComboBox regexpSyntax;
	private JButton findBtn;
	private JButton close;
	private JList results;
	
	public HyperSearch(View view)
	{
		super(view,jEdit.props.getProperty("hypersearch.title"),false);
		this.view = view;
		Container content = getContentPane();
		content.setLayout(new BorderLayout());
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add("West",new JLabel(jEdit.props.getProperty(
			"hypersearch.find")));
		find = new JTextField(jEdit.props.getProperty("search.find"
			+ ".value"),20);
		panel.add("Center",find);
		content.add("North",panel);
		panel = new JPanel();
		ignoreCase = new JCheckBox(jEdit.props.getProperty(
			"search.ignoreCase"),
			"on".equals(jEdit.props.getProperty("search"
				+ ".ignoreCase.toggle")));
		panel.add(ignoreCase);
		panel.add(new JLabel(jEdit.props.getProperty(
			"search.regexp")));
		regexpSyntax = new JComboBox(jEdit.SYNTAX_LIST);
		regexpSyntax.setSelectedItem(jEdit.props.getProperty("search"
			+ ".regexp.value"));
		panel.add(regexpSyntax);
		findBtn = new JButton(jEdit.props.getProperty(
			"hypersearch.findBtn"));
		panel.add(findBtn);
		close = new JButton(jEdit.props.getProperty(
			"hypersearch.close"));
		panel.add(close);
		content.add("Center",panel);
		results = new JList();
		results.setVisibleRowCount(10);
		results.setFont(view.getTextArea().getFont());
		results.addListSelectionListener(this);
		content.add("South",new JScrollPane(results));
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
		jEdit.props.put("search.find.value",find.getText());
		jEdit.props.put("search.ignoreCase.toggle",ignoreCase
			.getModel().isSelected() ? "on" : "off");
		jEdit.props.put("search.regexp.value",regexpSyntax
			.getSelectedItem());
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
			Buffer buffer = view.getBuffer();
			int tabSize = buffer.getTabSize();
			Vector data = new Vector();
			RE regexp = new RE(find.getText(),(ignoreCase
				.getModel().isSelected() ? RE.REG_ICASE : 0),
				jEdit.getRESyntax(jEdit.props.getProperty(
				"search.regexp.value")));
			Element map = buffer.getDefaultRootElement();
			int lines = map.getElementCount();
			for(int i = 1; i <= lines; i++)
			{
				Element lineElement = map.getElement(i - 1);
				int start = lineElement.getStartOffset();
				String lineString = buffer.getText(start,
					lineElement.getEndOffset() - start
					- 1);
				if(regexp.getMatch(lineString) != null)
					data.addElement(i + ":"
						+ jEdit.untab(tabSize,
							lineString));
			}
			results.setListData(data);
			pack();
		}
		catch(REException re)
		{
			Object[] args = { re.getMessage() };
			jEdit.error(view,"reerror",args);
		}
		catch(BadLocationException bl)
		{
		}
	}
	
	public void valueChanged(ListSelectionEvent evt)
	{
		if(results.isSelectionEmpty())
			return;
		String selected = (String)results.getSelectedValue();
		int line = Integer.parseInt(selected.substring(0,selected
			.indexOf(':')));
		SyntaxTextArea textArea = view.getTextArea();
		textArea.setCaretPosition(view.getBuffer()
			.getDefaultRootElement().getElementIndex(line - 1));
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
