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
		super(HistoryModel.getModel(name));
		this.name = name;

		setEditable(true);
		setMaximumRowCount(20);
		setSelectedItem(null);

		getEditor().getEditorComponent()
			.addKeyListener(new KeyHandler());
	}

	public Object getSelectedItem()
	{
		Object obj = getEditor().getItem();
		if(obj == null)
			obj = super.getSelectedItem();
		return obj;
	}
	
	public void addCurrentToHistory()
	{
		String str = (String)getEditor().getItem();
		if(str == null)
			str = (String)getSelectedItem();
		if(str != null && str.length() != 0)
		{
			((HistoryModel)getModel()).addItem(str);
			setSelectedIndex(0);
		}
	}

	/**
	 * Stupid workaround so that selecting an item or losing
	 * focus won't fire an action event
	 */
	public void _fireActionEvent()
	{
		super.fireActionEvent();
	}

	public void fireActionEvent() {}

	// private members
	private String name;
	private int max;

	class KeyHandler extends KeyAdapter
	{
		public void keyPressed(KeyEvent evt)
		{
			if(evt.getKeyCode() == KeyEvent.VK_ENTER)
			{
				Object current = getEditor().getItem();
				setSelectedItem(current);
				_fireActionEvent();
			}
		}
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.26  1999/05/08 00:13:00  sp
 * Splash screen change, minor documentation update, toolbar API fix
 *
 * Revision 1.25  1999/05/05 07:20:45  sp
 * jEdit 1.6pre5
 *
 * Revision 1.24  1999/05/04 04:51:25  sp
 * Fixed HistoryTextField for Swing 1.1.1
 *
 * Revision 1.23  1999/04/25 03:39:37  sp
 * Documentation updates, console updates, history text field updates
 *
 * Revision 1.22  1999/04/23 07:35:11  sp
 * History engine reworking (shared history models, history saved to
 * .jedit-history)
 *
 * Revision 1.21  1999/04/19 05:44:34  sp
 * GUI updates
 *
 * Revision 1.20  1999/03/28 01:36:24  sp
 * Backup system overhauled, HistoryTextField updates
 *
 * Revision 1.19  1999/03/27 03:22:16  sp
 * Number of items in a history list can now be set
 *
 * Revision 1.18  1999/03/27 03:08:55  sp
 * Changed max number of items in history to 25 from 100
 *
 * Revision 1.17  1999/03/21 08:37:16  sp
 * Slimmer action system, history text field update
 *
 * Revision 1.16  1999/03/20 01:55:42  sp
 * New color option pane, fixed search & replace bug
 *
 */
