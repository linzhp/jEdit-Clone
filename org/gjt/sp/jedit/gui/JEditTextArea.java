/*
 * JEditTextArea.java - Syntax text area subclass with jEdit specific stuff
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

import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.*;
import java.beans.*;
import org.gjt.sp.jedit.syntax.SyntaxTextArea;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.jEdit;

/**
 * A subclass of <code>SyntaxTextArea</code> that implements several
 * jEdit specific features.
 *
 * @author Slava Pestov
 * @version $Id$
 * @see org.gjt.sp.jedit.syntax.SyntaxTextArea
 */
public class JEditTextArea extends SyntaxTextArea
{
	/**
	 * Creates a new JEditTextArea component.
	 */
	public JEditTextArea()
	{
		addMouseListener(new TextAreaMouseListener());
		addMouseMotionListener(new TextAreaMouseMotionListener());
		addPropertyChangeListener(new TextAreaPropertyListener());
	}

	/**
	 * Copies the selected text to the clipboard, adding it to the
	 * jEdit clip history.
	 */
	public void copy()
	{
		String selection = getSelectedText();
		if(selection != null)
		{
			super.copy();
			jEdit.addToClipHistory(selection);
		}
	}

	/**
	 * Copies the selected text to the clipboard, removing it from
	 * the document and adding it to the jEdit clip history.
	 */
	public void cut()
	{
		String selection = getSelectedText();
		if(selection != null)
		{
			super.cut();
			jEdit.addToClipHistory(selection);
		}
	}

	/**
	 * Inserts the clipboard contents at the caret.
	 */
	public void paste()
	{
		Clipboard clipboard = getToolkit().getSystemClipboard();
		Transferable content = clipboard.getContents(this);
		if(content != null)
		{
			try
			{
				String text = (String)content.getTransferData(
					DataFlavor.stringFlavor);
				jEdit.addToClipHistory(text);
				replaceSelection(text);
			}
			catch(Exception e)
			{
				getToolkit().beep();
			}
		}
	}

	/**
	 * Sets the popup menu that is to be displayed when the
	 * text area is right clicked.
	 * @param menu The context menu
	 */
	public void setContextMenu(JPopupMenu menu)
	{
		this.menu = menu;
	}

	// private members
	private JPopupMenu menu;

	class TextAreaMouseListener extends MouseAdapter
	{
		public void mousePressed(MouseEvent evt)
		{
			if((evt.getModifiers() & InputEvent.BUTTON3_MASK) != 0)
			{
				menu.show(JEditTextArea.this,
					evt.getX(),evt.getY());
				evt.consume();
			}
		}
	}

	class TextAreaMouseMotionListener implements MouseMotionListener
	{
		public void mouseMoved(MouseEvent evt)
		{
			if((evt.getModifiers() & InputEvent.BUTTON3_MASK) != 0)
				evt.consume();
		}
	
		public void mouseDragged(MouseEvent evt)
		{
			if((evt.getModifiers() & InputEvent.BUTTON3_MASK) != 0)
				evt.consume();
		}
	}

	class TextAreaPropertyListener implements PropertyChangeListener
	{
		public void propertyChange(PropertyChangeEvent evt)
		{
			Object newValue = evt.getNewValue();
			if(newValue instanceof Buffer)
			{
				Buffer buf = (Buffer)newValue;
				int selStart = buf.getSavedSelStart();
				int selEnd = buf.getSavedSelEnd();
				getCaret().setDot(selStart);
				getCaret().moveDot(selEnd);
				Element map = getDocument().getDefaultRootElement();
				int startLine = map.getElementIndex(selStart);
				int endLine = map.getElementIndex(selEnd) + 1;
				int height = Toolkit.getDefaultToolkit()
					.getFontMetrics(getFont()).getHeight();
				Rectangle rect = new Rectangle(0,startLine * height,
					0,(endLine - startLine) * height);
				doElectricScroll(rect);
			}
		}
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.1  1999/03/27 02:45:07  sp
 * New JEditTextArea class that adds jEdit-specific features to SyntaxTextArea
 *
 */
