/*
 * HistoryTextField.java - Text field with a history
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

/**
 * Text field with an arrow-key accessable history.
 * @author Slava Pestov
 * @version $Id$
 */
public class HistoryTextField extends JTextField
{
	public HistoryTextField(String name)
	{
		historyModel = HistoryModel.getModel(name);
		addKeyListener(new KeyHandler());

		index = -1;
	}

	public void addCurrentToHistory()
	{
		historyModel.addItem(getText());
		index = 0;
	}

	public void setText(String text)
	{
		super.setText(text);
		index = -1;
	}

	// private members
	HistoryModel historyModel;
	String current;
	int index;

	private void doBackwardSearch()
	{
		if(getSelectionEnd() != getDocument().getLength())
		{
			setCaretPosition(getDocument().getLength());
		}

		String text = getText().substring(0,getSelectionStart());
		if(text == null)
		{
			historyPrevious();
			return;
		}

		for(int i = index + 1; i < historyModel.getSize(); i++)
		{
			String item = historyModel.getItem(i);
			if(item.startsWith(text))
			{
				replaceSelection(item.substring(text.length()));
				select(text.length(),getDocument().getLength());
				index = i;
				return;
			}
		}

		getToolkit().beep();
	}

	private void doForwardSearch()
	{
		if(getSelectionEnd() != getDocument().getLength())
		{
			setCaretPosition(getDocument().getLength());
		}

		String text = getText().substring(0,getSelectionStart());
		if(text == null)
		{
			historyNext();
			return;
		}

		for(int i = index - 1; i >= 0; i--)
		{
			String item = historyModel.getItem(i);
			if(item.startsWith(text))
			{
				replaceSelection(item.substring(text.length()));
				select(text.length(),getDocument().getLength());
				index = i;
				return;
			}
		}

		getToolkit().beep();
	}

	private void historyPrevious()
	{
		if(index == historyModel.getSize() - 1)
			getToolkit().beep();
		else if(index == -1)
		{
			current = getText();
			setText(historyModel.getItem(0));
			index = 0;
		}
		else
		{
			// have to do this because setText() sets index to -1
			int newIndex = index + 1;
			setText(historyModel.getItem(newIndex));
			index = newIndex;
		}
	}

	private void historyNext()
	{
		if(index == -1)
			getToolkit().beep();
		else if(index == 0)
			setText(current);
		else
		{
			// have to do this because setText() sets index to -1
			int newIndex = index - 1;
			setText(historyModel.getItem(newIndex));
			index = newIndex;
		}
	}

	class KeyHandler extends KeyAdapter
	{
		public void keyPressed(KeyEvent evt)
		{
			if(evt.getKeyCode() == KeyEvent.VK_ENTER)
			{
				addCurrentToHistory();
			}
			else if(evt.getKeyCode() == KeyEvent.VK_UP)
			{
				if((evt.getModifiers() & InputEvent.CTRL_MASK) != 0)
					doBackwardSearch();
				else
					historyPrevious();
				evt.consume();
			}
			else if(evt.getKeyCode() == KeyEvent.VK_DOWN)
			{
				if((evt.getModifiers() & InputEvent.CTRL_MASK) != 0)
					doForwardSearch();
				else
					historyNext();
				evt.consume();
			}
			else if(evt.getKeyCode() == KeyEvent.VK_TAB
				&& (evt.getModifiers() & InputEvent.CTRL_MASK) != 0)
			{
				doBackwardSearch();
				evt.consume();
			}
		}
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.27  1999/05/09 03:50:17  sp
 * HistoryTextField is now a text field again
 *
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
 */
