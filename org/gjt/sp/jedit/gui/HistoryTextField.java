/*
 * HistoryTextField.java - Text field with a combo box history
 * Copyright (C) 1999 Slava Pestov
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
 * Text field with a combo box component listing previously entered
 * strings.
 * @author Slava Pestov
 * @version $Id$
 */
public class HistoryTextField extends JComboBox
{
	public HistoryTextField(String name)
	{
		this.name = name;
		String line;
		int i = 0;
		while((line = jEdit.getProperty("history." + name + "." + i)) != null)
		{
			addItem(line);
			i++;
		}
		setEditable(true);
		setMaximumRowCount(10);
		setSelectedItem(null);

		getEditor().getEditorComponent()
			.addKeyListener(new HistoryKeyListener());
	}

	public void save()
	{
		String text = (String)getEditor().getItem();
		if(text == null)
			text = (String)getSelectedItem();
		if(text != null && text.length() != 0)
			insertItemAt(text,0);
		for(int i = 0; i < getItemCount(); i++)
		{
			jEdit.setProperty("history." + name + "." + i,
				(String)getItemAt(i));
		}
	}

	public void addCurrentToHistory()
	{
		String text = (String)getEditor().getItem();
		if(text == null)
			text = (String)getSelectedItem();
		if(text != null && text.length() != 0)
			insertItemAt(text,0);
		if(getItemCount() > 100)
			removeItemAt(getItemCount() - 1);
	}

	public void actionPerformed(ActionEvent evt)
	{
		Object current = getEditor().getItem();
		setSelectedItem(current);

		// Don't fire actionEvent here
	}

	public void selectedItemChanged()
	{
		if(selectedItemReminder != null)
		{
			fireItemStateChanged(new ItemEvent(this,
				ItemEvent.ITEM_STATE_CHANGED,
				selectedItemReminder,ItemEvent.DESELECTED));
		}

		selectedItemReminder = getModel().getSelectedItem();

		if(selectedItemReminder != null)
		{
			fireItemStateChanged(new ItemEvent(this,
				ItemEvent.ITEM_STATE_CHANGED,
				selectedItemReminder,ItemEvent.SELECTED));
		}
		
		// Don't fire actionEvent here
	}
	
	// private members
	private String name;

	class HistoryKeyListener extends KeyAdapter
	{
		public void keyPressed(KeyEvent evt)
		{
			if(evt.getKeyCode() == KeyEvent.VK_ENTER)
			{
				Object current = getEditor().getItem();
				setSelectedItem(current);
				fireActionEvent();
			}
		}
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.13  1999/03/19 06:08:45  sp
 * Fixed conflicts from incomplete commit, removed obsolete files
 *
 */
