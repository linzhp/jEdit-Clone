/*
 * SearchAndReplace.java - Search and replace dialog
 * Copyright (C) 1998, 1999 Slava Pestov
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
implements ActionListener, KeyListener, WindowListener
{
	public SearchAndReplace(View view)
	{
		super(view,jEdit.getProperty("search.title"),false);
		this.view = view;

		// silly hack :)
		selStart = view.getTextArea().getSelectionStart();
		selEnd = view.getTextArea().getSelectionEnd();

		find = new HistoryTextField("find",30);
		replace = new HistoryTextField("replace",30);
		keepDialog = new JCheckBox(jEdit.getProperty(
			"search.keepDialog"),"on".equals(jEdit.getProperty(
			"search.keepDialog.toggle")));
		ignoreCase = new JCheckBox(jEdit.getProperty(
			"search.ignoreCase"),
			"on".equals(jEdit.getProperty("search."
				+ "ignoreCase.toggle")));
		regexpSyntax = new JComboBox(jEdit.SYNTAX_LIST);
		regexpSyntax.setSelectedItem(jEdit.getProperty("search"
			+ ".regexp.value"));
		findBtn = new JButton(jEdit.getProperty("search.findBtn"));
		replaceSelection = new JButton(jEdit.getProperty("search"
			+ ".replaceSelection"));
		replaceSelection.setMnemonic(jEdit.getProperty("search"
			+ ".replaceSelection.mnemonic").charAt(0));
		replaceAll = new JButton(jEdit.getProperty("search.replaceAll"));
		replaceAll.setMnemonic(jEdit.getProperty("search.replaceAll"
			+ ".mnemonic").charAt(0));
		cancel = new JButton(jEdit.getProperty("search.cancel"));
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
		panel.add(keepDialog);
		panel.add(ignoreCase);
		panel.add(new JLabel(jEdit.getProperty("search.regexp")));
		panel.add(regexpSyntax);
		getContentPane().add("Center",panel);
		panel = new JPanel();
		panel.add(findBtn);
		panel.add(replaceSelection);
		panel.add(replaceAll);
		panel.add(cancel);
		getRootPane().setDefaultButton(findBtn);
		getContentPane().add("South",panel);

		find.addKeyListener(this);
		replace.addKeyListener(this);
		addKeyListener(this);
		addWindowListener(this);
		findBtn.addActionListener(this);
		replaceSelection.addActionListener(this);
		replaceAll.addActionListener(this);
		cancel.addActionListener(this);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		pack();
		GUIUtilities.loadGeometry(this,"search");

		show();
		find.requestFocus();
	}
	
	public void actionPerformed(ActionEvent evt)
	{
		save();
		Object source = evt.getSource();
		if(source == cancel)
			dispose();
		else if(source == findBtn)
		{
			if(view.getBuffer().find(view,false))
				disposeOrKeepDialog();
		}
		else if(source == replaceSelection)
		{
			if(view.getBuffer().replaceAll(view,selStart,selEnd))
				disposeOrKeepDialog();
			else
				getToolkit().beep();
		}
		else if(source == replaceAll)
		{
			if(view.getBuffer().replaceAll(view,0,
				view.getBuffer().getLength()))
				disposeOrKeepDialog();
			else
				getToolkit().beep();
		}
	}

	public void keyPressed(KeyEvent evt)
	{
		switch(evt.getKeyCode())
		{
		case KeyEvent.VK_ENTER:
			save();
			if(view.getBuffer().find(view,false));
				disposeOrKeepDialog();
			break;
		case KeyEvent.VK_ESCAPE:
			save();
			dispose();
			break;
		}
	}
	
	public void keyReleased(KeyEvent evt) {}

	public void keyTyped(KeyEvent evt) {}
	
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

        // private members
	private View view;
	private HistoryTextField find;
	private HistoryTextField replace;
	private JCheckBox keepDialog;
	private JCheckBox ignoreCase;
	private JComboBox regexpSyntax;
	private JButton findBtn;
	private JButton replaceSelection;
	private JButton replaceAll;
	private JButton cancel;
	private int selStart;
	private int selEnd;
	
	private void save()
	{
		find.save();
		replace.save();
		jEdit.setProperty("search.keepDialog.toggle",keepDialog
			.getModel().isSelected() ? "on" : "off");
		jEdit.setProperty("search.ignoreCase.toggle",ignoreCase
			.getModel().isSelected() ? "on" : "off");
		jEdit.setProperty("search.regexp.value",(String)regexpSyntax
			.getSelectedItem());
		GUIUtilities.saveGeometry(this,"search");
	}

	private void disposeOrKeepDialog()
	{
		if(keepDialog.getModel().isSelected())
			return;
		dispose();
	}
}
