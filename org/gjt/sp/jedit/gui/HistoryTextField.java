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
	}

	public void save()
	{
		String text = (String)getSelectedItem();
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
		String text = (String)getSelectedItem();
		if(text != null && text.length() != 0)
			insertItemAt(text,0);
		if(getItemCount() > 100)
			removeItemAt(getItemCount() - 1);
	}

	// private members
	private String name;
}
