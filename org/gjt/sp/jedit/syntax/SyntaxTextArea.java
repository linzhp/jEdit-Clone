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
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.FocusEvent;
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
		bracketHighlightColor = new Color(0xffaaaa);
		lineSegment = new Segment();
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
			if(bracketPos == -1)
				getHighlighter().changeHighlight(
					bracketHighlightTag,0,0);
			else
				getHighlighter().changeHighlight(
					bracketHighlightTag,bracketPos,
					bracketPos+1);
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
	 * Sets the caret blink rate.
	 * @param blinkRate the new blink rate, 0 = no blink
	 */
	public void setCaretBlinkRate(int blinkRate)
	{
		getCaret().setBlinkRate(blinkRate);
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
	private Segment lineSegment;

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
				int height = getToolkit().getFontMetrics(
					getFont()).getHeight();
				int selStart = buf.getSavedSelStart();
				int selEnd = buf.getSavedSelEnd();
				select(selStart,selEnd);
				Element map = getDocument().getDefaultRootElement();
				int startLine = map.getElementIndex(selStart);
				int endLine = map.getElementIndex(selEnd) + 1;
				final Rectangle rect = new Rectangle(0,
					startLine * height,0,(endLine - startLine)
					* height);
				SwingUtilities.invokeLater(new Runnable() {
					public void run()
					{
						scrollRectToVisible(rect);
					}
				});
			}
		}

		public void focusGained(FocusEvent evt)
		{
			/* we do this even if the text area is read only,
			 * otherwise stuff like line and bracket highlighting
			 * will look weird without a caret */
			SyntaxTextArea.SyntaxCaret.this.setVisible(true);
		}
	}

	private class CurrentLineHighlighter implements Highlighter.HighlightPainter
	{
		public void paint(Graphics g, int p0, int p1, Shape bounds,
			JTextComponent textComponent)
		{
			if(!lineHighlight || getSelectionStart()
				!= getSelectionEnd())
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
			if(p0 == p1)
				return;
			Rectangle rect = (Rectangle)bounds;
			Rectangle bracket;
			Document doc = getDocument();
			FontMetrics metrics = g.getFontMetrics();
			try
			{
				bracket = modelToView(p0);
				doc.getText(p0,1,lineSegment);
				bracket.width += metrics.charWidth(lineSegment
					.array[lineSegment.offset]);
			}
			catch(BadLocationException bl)
			{
				bl.printStackTrace();
				return;
			}
			g.setColor(bracketHighlightColor);
			g.fillRect(rect.x + bracket.x,rect.y + bracket.y,
				bracket.width,bracket.height);
		}
	}
}
