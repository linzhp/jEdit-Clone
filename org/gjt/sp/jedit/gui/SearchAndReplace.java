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

/**
 * Search and replace dialog.
 * @author Slava Pestov
 * @version $Id$
 */
public class SearchAndReplace extends JDialog
{
	public SearchAndReplace(View view, String defaultFind)
	{
		super(view,jEdit.getProperty("search.title"),false);
		this.view = view;

		// silly hack :)
		selStart = view.getTextArea().getSelectionStart();
		selEnd = view.getTextArea().getSelectionEnd();

		find = new HistoryTextField("find");
		find.setSelectedItem(defaultFind);

		replace = new HistoryTextField("replace");
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
		JLabel label = new JLabel(jEdit.getProperty("search.find"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,constraints);
		panel.add(label);
		constraints.gridx = 1;
		constraints.gridwidth = constraints.REMAINDER;
		constraints.weightx = 1.0f;
		layout.setConstraints(find,constraints);
		panel.add(find);
		constraints.gridx = 0;
		constraints.gridwidth = 1;
		constraints.gridy = 1;
		constraints.weightx = 0.0f;
		label = new JLabel(jEdit.getProperty("search.replace"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,constraints);
		panel.add(label);
		constraints.gridx = 1;
		constraints.gridwidth = constraints.REMAINDER;
		constraints.weightx = 1.0f;
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

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		SearchKeyListener keyListener = new SearchKeyListener();
		addKeyListener(keyListener);

		find.getEditor().getEditorComponent()
			.addKeyListener(keyListener);
		replace.getEditor().getEditorComponent()
			.addKeyListener(keyListener);

		addWindowListener(new SearchWindowListener());

		SearchActionListener actionListener = new SearchActionListener();
		find.addActionListener(actionListener);
		replace.addActionListener(actionListener);
		findBtn.addActionListener(actionListener);
		replaceSelection.addActionListener(actionListener);
		replaceAll.addActionListener(actionListener);
		cancel.addActionListener(actionListener);
		
		pack();
		GUIUtilities.loadGeometry(this,"search");

		show();
		find.requestFocus();
	}

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

	class SearchActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			Object source = evt.getSource();
			if(source == cancel)
				dispose();
			else if(source == findBtn || source == find
				|| source == replace)
			{
				save();
				if(view.getBuffer().find(view,false))
					disposeOrKeepDialog();
			}
			else if(source == replaceSelection)
			{
				save();
				if(view.getBuffer().replaceAll(view,
					selStart,selEnd))
					disposeOrKeepDialog();
				else
					getToolkit().beep();
			}
			else if(source == replaceAll)
			{
				save();
				if(view.getBuffer().replaceAll(view,0,
					view.getBuffer().getLength()))
					disposeOrKeepDialog();
				else
					getToolkit().beep();
			}
		}
	}

	class SearchKeyListener extends KeyAdapter
	{
		public void keyPressed(KeyEvent evt)
		{
			if(evt.getKeyCode() == KeyEvent.VK_ESCAPE)
			{
				dispose();
			}
		}
	}

	class SearchWindowListener extends WindowAdapter
	{
		public void windowClosing(WindowEvent evt)
		{
			dispose();
		}
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.21  1999/03/19 08:32:22  sp
 * Added a status bar to views, Escape key now works in dialog boxes
 *
 * Revision 1.20  1999/03/19 07:12:11  sp
 * JOptionPane changes, did a fromdos of the source
 *
 * Revision 1.19  1999/03/17 05:32:52  sp
 * Event system bug fix, history text field updates (but it still doesn't work), code cleanups, lots of banging head against wall
 *
 */
