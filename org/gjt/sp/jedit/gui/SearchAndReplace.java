/*
 * SearchAndReplace.java - Search and replace dialog
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.gjt.sp.jedit.*;

public class SearchAndReplace extends JDialog
implements ActionListener, WindowListener
{
	private View view;
	private JTextField find;
	private JTextField replace;
	private JCheckBox ignoreCase;
	private JComboBox regexpSyntax;
	private JButton findBtn;
	private JButton replaceAll;
	private JButton close;
	
	public SearchAndReplace(View view)
	{
		super(view,jEdit.getProperty("search.title"),false);
		this.view = view;
		find = new JTextField(jEdit.getProperty("search.find"
			+ ".value"),30);
		replace = new JTextField(jEdit
			.getProperty("search.replace.value"),30);
		ignoreCase = new JCheckBox(jEdit.getProperty(
			"search.ignoreCase"),
			"on".equals(jEdit.getProperty("search."
				+ "ignoreCase.toggle")));
		regexpSyntax = new JComboBox(jEdit.SYNTAX_LIST);
		regexpSyntax.setSelectedItem(jEdit.getProperty("search"
			+ ".regexp.value"));
		findBtn = new JButton(jEdit.getProperty("search.findBtn"));
		replaceAll = new JButton(jEdit.getProperty("search.replaceAll"));
		close = new JButton(jEdit.getProperty("search.close"));
		getContentPane().setLayout(new BorderLayout());
		JPanel panel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		panel.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth = constraints.gridheight = 1;
		constraints.fill = constraints.BOTH;
		constraints.weightx = 1.0f;
		JLabel label = new JLabel(jEdit.getProperty("search.find"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,constraints);
		panel.add(label);
		constraints.gridx = 1;
		constraints.gridwidth = 2;
		layout.setConstraints(find,constraints);
		panel.add(find);
		constraints.gridx = 0;
		constraints.gridwidth = 1;
		constraints.gridy = 2;
		label = new JLabel(jEdit.getProperty("search.replace"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,constraints);
		panel.add(label);
		constraints.gridx = 1;
		constraints.gridwidth = 2;
		layout.setConstraints(replace,constraints);
		panel.add(replace);
		getContentPane().add("North",panel);
		panel = new JPanel();
		panel.add(ignoreCase);
		panel.add(new JLabel(jEdit.getProperty("search.regexp")));
		panel.add(regexpSyntax);
		getContentPane().add("Center",panel);
		panel = new JPanel();
		panel.add(findBtn);
		panel.add(replaceAll);
		panel.add(close);
		getRootPane().setDefaultButton(findBtn);
		getContentPane().add("South",panel);
		Dimension screen = getToolkit().getScreenSize();
		pack();
		setLocation((screen.width - getSize().width) / 2,
			(screen.height - getSize().height) / 2);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		find.addActionListener(this);
		replaceAll.addActionListener(this);
		close.addActionListener(this);
		show();
	}
	
	public void save()
	{
		jEdit.setProperty("search.find.value",find.getText());
		jEdit.setProperty("search.replace.value",replace.getText());
		jEdit.setProperty("search.ignoreCase.toggle",ignoreCase
			.getModel().isSelected() ? "on" : "off");
		jEdit.setProperty("search.regexp.value",(String)regexpSyntax
			.getSelectedItem());
	}
	
	public void actionPerformed(ActionEvent evt)
	{
		save();
		Object source = evt.getSource();
		if(source == close)
			dispose();
		else if(source == findBtn)
		{
			view.getBuffer().find(view,false);
			dispose();
		}
		else if(source == replaceAll)
		{
			view.getBuffer().replaceAll(view);
			dispose();
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
