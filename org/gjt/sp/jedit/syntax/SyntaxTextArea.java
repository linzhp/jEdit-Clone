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
import java.awt.datatransfer.*;
import java.beans.*;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.jEdit;

/**
 * A subclass of <code>JEditorPane</code> whose default editor kit is
 * <code>SyntaxEditorKit</code>. It also does current line and bracket
 * highlighting.
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
			bracketHighlightTag = getHighlighter().addHighlight(
				0,0,new BracketHighlighter());
		}
		catch(BadLocationException bl)
		{
			bl.printStackTrace();
		}
		
		lineHighlightColor = new Color(0xe0e0e0);
		bracketHighlightColor = new Color(0xcc0000);
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
	 * Sets the current line highlighting feature.
	 * @param lineHighlight True if the current line should be
	 * highlighted, false otherwise.
	 */
	public void setLineHighlight(boolean lineHighlight)
	{
		this.lineHighlight = lineHighlight;
	}

	/**
	 * Sets the highlighted bracket.
	 * @param bracketPos The offset of the bracket in the document
	 */
	public void setHighlightedBracket(int bracketPos)
	{
		try
		{
			getHighlighter().changeHighlight(bracketHighlightTag,
				bracketPos,bracketPos+1);
		}
		catch(BadLocationException bl)
		{
			bl.printStackTrace();
		}
	}

	/**
	 * Sets the bracket highlight color.
	 * @param color The bracket highlight color
	 */
	public void setBracketHighlightColor(Color color)
	{
		bracketHighlightColor = color;
	}

	/**
	 * Sets the bracket highlighting feature.
	 * @param bracketHighlight True if the current line should be
	 * highlighted, false otherwise.
	 */
	public void setBracketHighlight(boolean bracketHighlight)
	{
		this.bracketHighlight = bracketHighlight;
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

	// private members
	private boolean lineHighlight;
	private Color lineHighlightColor;
	private Object lineHighlightTag;
	private boolean bracketHighlight;
	private Color bracketHighlightColor;
	private Object bracketHighlightTag;

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

	private class BracketHighlighter implements Highlighter.HighlightPainter
	{
		public void paint(Graphics g, int p0, int p1, Shape bounds,
			JTextComponent textComponent)
		{
			if(getSelectionStart() != getSelectionEnd()
				|| !bracketHighlight)
				return;
			Rectangle rect = (Rectangle)bounds;
			Rectangle bracket;
			try
			{
				bracket = modelToView(p0);
				if(bracket == null)
					return;
			}
			catch(BadLocationException bl)
			{
				return;
			}
			int x = rect.x + bracket.x;
			int y = rect.y + bracket.y;
			g.setColor(bracketHighlightColor);
			g.fillRect(x,y,bracket.width,bracket.height);
		}
	}
}
