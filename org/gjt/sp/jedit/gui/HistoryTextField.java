/*
 * HistoryTextField.java - Text field with up arrow/down arrow recall
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

public class HistoryTextField extends JTextField implements KeyListener
{
	public HistoryTextField(String name, int width)
	{
		super(width);
		this.name = name;
		history = new String[100];
		String line;
		int i = 0;
		while((line = jEdit.getProperty("history." + name + "." + i)) != null)
		{
			history[i] = line;
			i++;
		}
		addKeyListener(this);
	}

	public void save()
	{
		jEdit.setProperty("history." + name + ".0",getText());
		for(int i = 0; i < history.length - 1; i++)
		{
			String line = history[i];
			if(line == null)
				break;
			jEdit.setProperty("history." + name + "." + (i + 1),line);
		}
	}

	public void keyTyped(KeyEvent evt) {}

	public void keyPressed(KeyEvent evt)
	{
		switch(evt.getKeyCode())
		{
		case KeyEvent.VK_DOWN:
			if(historyPos == 1)
                        {
                                historyPos = 0;
                                setText(current);
                        }
                        else if(historyPos < 1)
                                getToolkit().beep();
			else
				setText(history[--historyPos - 1]);
			evt.consume();
			break;
		case KeyEvent.VK_UP:
                        if(historyPos == 0)
                                current = getText();
			String line = history[historyPos];
			if(line == null)
				getToolkit().beep();
			else
			{
				historyPos++;
				setText(line);
			}
			evt.consume();
			break;
		}
	}

	public void keyReleased(KeyEvent evt) {}

	// private members
	private String name;
	private String[] history;
        private String current;
	private int historyPos;
}
