/*
 * SyntaxTextArea.java - jEdit's own text component
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

package org.gjt.sp.jedit.syntax;

import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.JEditorPane;
import java.awt.*;
import java.beans.*;
import org.gjt.sp.jedit.Buffer;

/**
 * A subclass of <code>JEditorPane</code> whose default editor kit is
 * <code>SyntaxEditorKit</code>
 * @see org.gjt.sp.jedit.syntax.SyntaxEditorKit
 */
public class SyntaxTextArea extends JEditorPane
{
	// public members

	/**
	 * Creates a new SyntaxTextArea component.
	 */
	public SyntaxTextArea()
	{
		setCaret(new SyntaxCaret());

		try
		{
			lineHighlightTag = getHighlighter().addHighlight(0,0,
				new CurrentLineHighlighter());
		}
		catch(BadLocationException bl)
		{
			bl.printStackTrace();
		}
		
		lineHighlightColor = new Color(0xeeeeee);
	}

	/**
	 * Returns the default editor kit for this text component.
	 */
	public EditorKit createDefaultEditorKit()
	{
		return new SyntaxEditorKit();
	}

	/**
	 * Sets the highlighted line.
	 * @param lineStart The start offset of the line in the document
	 * @param lineEnd The end offset of the line in the document
	 */
	public void setHighlightedLine(int lineStart, int lineEnd)
	{
		try
		{
			getHighlighter().changeHighlight(lineHighlightTag,
				lineStart,lineEnd);
		}
		catch(BadLocationException bl)
		{
			bl.printStackTrace();
		}
	}

	/**
	 * Sets the line highlight color.
	 * @param color The line highlight color
	 */
	public void setLineHighlightColor(Color color)
	{
		lineHighlightColor = color;
	}

	/**
	 * Sets the current line highlighting.
	 * @param lineHighlight True if the current line should be
	 * highlighted, false otherwise.
	 */
	public void setLineHighlight(boolean lineHighlight)
	{
		this.lineHighlight = lineHighlight;
	}

	// private members
	private boolean lineHighlight;
	private Color lineHighlightColor;
	private Object lineHighlightTag;

	private class SyntaxCaret extends DefaultCaret
		implements PropertyChangeListener
	{
		public void install(JTextComponent c)
		{
			super.install(c);
			c.addPropertyChangeListener(this);
		}

		public void deinstall(JTextComponent c)
		{
			super.deinstall(c);
			c.removePropertyChangeListener(this);
		}

		public void propertyChange(PropertyChangeEvent evt)
		{
			Object newValue = evt.getNewValue();
			if(newValue instanceof Buffer)
			{
				Buffer buf = (Buffer)newValue;
				setDot(buf.getSavedSelStart());
				moveDot(buf.getSavedSelEnd());
			}
		}
	}

	private class CurrentLineHighlighter implements Highlighter.HighlightPainter
	{
		public void paint(Graphics g, int p0, int p1, Shape bounds,
			JTextComponent textComponent)
		{
			if(getSelectionStart() != getSelectionEnd()
				|| !lineHighlight)
				return;
			FontMetrics metrics = g.getFontMetrics();
			Document doc = getDocument();
			int lineNo = doc.getDefaultRootElement()
				.getElementIndex(p0);
			Rectangle rect = (Rectangle)bounds;
			int height = metrics.getHeight();
			int x = rect.x;
			int y = rect.y + height * lineNo;
			g.setColor(lineHighlightColor);
			g.fillRect(x,y,rect.width,height);
		}
	}
}
