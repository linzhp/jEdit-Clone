/*
 * SyntaxTextArea.java - Enhanced text component
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

package org.gjt.sp.jedit.syntax;

import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.*;

/**
 * An enhanced <code>JEditorPane</code> useful in text editors. It has
 * the following advantages over the standard Swing text components:
 * <ul>
 * <li>Uses the <code>SyntaxEditorKit</code> by default
 * <li>Implements line highlighting, where the line the caret is on
 * has a different background color
 * <li>Implements bracket highlighting, where if the caret is on a
 * bracket, the matching one is highlighted.
 * <li>Has an optional block caret
 * </ul>
 *
 * @author Slava Pestov
 * @version $Id$
 * @see org.gjt.sp.jedit.syntax.SyntaxEditorKit
 */
public class SyntaxTextArea extends JEditorPane
{
	/**
	 * The default editor kit for this text component.
	 */
	public static final EditorKit EDITOR_KIT = new SyntaxEditorKit();

	/**
	 * Creates a new SyntaxTextArea component.
	 */
	public SyntaxTextArea()
	{
		setCaret(new SyntaxCaret());

		lineHighlightColor = new Color(0xe0e0e0);
		bracketHighlightColor = new Color(0xffaaaa);
		lineSegment = new Segment();

		addCaretListener(new SyntaxCaretListener());
	}

	/**
	 * Returns the default editor kit for this text component.
	 */
	public EditorKit createDefaultEditorKit()
	{
		return EDITOR_KIT;
	}

	/**
	 * Sets the currently highlighted line.
	 * @param lineStart The start offset of the line in the document
	 * @param lineEnd The end offset of the line in the document
	 */
	public void setHighlightedLine(int lineStart, int lineEnd)
	{
		if(lineHighlightTag == null)
			return;

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
	 * Returns the line highlight color.
	 */
	public Color getLineHighlightColor()
	{
		return lineHighlightColor;
	}

	/**
	 * Sets the current line highlighting feature.
	 * @param lineHighlight True if the current line should be
	 * highlighted, false otherwise.
	 */
	public void setLineHighlight(boolean lineHighlight)
	{
		if(lineHighlightTag != null)
			getHighlighter().removeHighlight(lineHighlightTag);
		
		if(lineHighlight)
		{
			try
			{
				lineHighlightTag = getHighlighter().addHighlight(
					0,0,new CurrentLineHighlighter());
			}
			catch(BadLocationException bl)
			{
				bl.printStackTrace();
			}
		}
		else
			lineHighlightTag = null;
	}

	/**
	 * Returns true if current line highlighting is enabled, false
	 * otherwise.
	 */
	public boolean getLineHighlight()
	{
		return (lineHighlightTag != null);
	}

	/**
	 * Sets the highlighted bracket.
	 * @param bracketPos The offset of the bracket in the document
	 */
	public void setHighlightedBracket(int bracketPos)
	{
		if(bracketHighlightTag == null)
			return;

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
	 * Returns the bracket highlight color.
	 */
	public Color getBracketHighlightColor()
	{
		return bracketHighlightColor;
	}

	/**
	 * Sets the bracket highlighting feature.
	 * @param bracketHighlight True if matching brackets should be
	 * highlighted, false otherwise.
	 */
	public void setBracketHighlight(boolean bracketHighlight)
	{
		if(bracketHighlightTag != null)
			getHighlighter().removeHighlight(bracketHighlightTag);
		
		if(bracketHighlight)
		{
			try
			{
				bracketHighlightTag = getHighlighter().addHighlight(
					0,0,new BracketHighlighter());
			}
			catch(BadLocationException bl)
			{
				bl.printStackTrace();
			}
		}
		else
			bracketHighlightTag = null;
	}

	/**
	 * Returns true if bracket highlighting is enabled, false otherwise.
	 */
	public boolean getBracketHighlight()
	{
		return (bracketHighlightTag != null);
	}

	/**
	 * Sets the number of lines from the top or bottom of the
	 * text area from which autoscroll begins.
	 * @param lines The number of lines
	 */
	public void setElectricBorders(int lines)
	{
		electricLines = lines;
	}

	/**
	 * Returns the number of lines from the top or bottom of the
	 * text area from which autoscrolling begins.
	 */
	public int getElectricBorders()
	{
		return electricLines;
	}

	/**
	 * Sets the block caret.
	 * @param block True if a block caret should be drawn, false
	 * otherwise
	 */
	public void setBlockCaret(boolean block)
	{
		this.block = block;
	}

	/**
	 * Returns true if a block caret is enabled, false otherwise.
	 */
	public boolean getBlockCaret()
	{
		return block;
	}

	/**
	 * Updates the bracket and line highlighters.
	 */
	public void updateHighlighters()
	{
		Document doc = getDocument();
		int dot = getCaretPosition();
		Element map = doc.getDefaultRootElement();
		int lineNo = map.getElementIndex(dot);

		Element lineElement = map.getElement(lineNo);
		int start = lineElement.getStartOffset();
		int end = lineElement.getEndOffset();

		if(getSelectionStart() == getSelectionEnd())
		{
			if(lineNo != lastLine)
			{
				setHighlightedLine(start,end);
				lastLine = lineNo;
			}
		}
		
		try
		{
			if(dot != 0)
			{
				dot--;
				doc.getText(dot,1,lineSegment);
				char bracket = lineSegment.array
					[lineSegment.offset];
				int otherBracket;
				switch(bracket)
				{
				case '(':
					otherBracket = SyntaxUtilities.locateBracketForward(
						doc,dot,'(',')');
					break;
				case ')':
					otherBracket = SyntaxUtilities.locateBracketBackward(
						doc,dot,'(',')');
					break;
				case '[':
					otherBracket = SyntaxUtilities.locateBracketForward(
						doc,dot,'[',']');
					break;
				case ']':
					otherBracket = SyntaxUtilities.locateBracketBackward(
						doc,dot,'[',']');
					break;
				case '{':
					otherBracket = SyntaxUtilities.locateBracketForward(
						doc,dot,'{','}');
					break;
				case '}':
					otherBracket = SyntaxUtilities.locateBracketBackward(
						doc,dot,'{','}');
					break;
				default:
					otherBracket = -1;
					break;
				}
				setHighlightedBracket(otherBracket);
			}
			else
				setHighlightedBracket(-1);
		}
		catch(BadLocationException bl)
		{
			//bl.printStackTrace();
		}
	}

	/**
	 * Works like Caret.adjustVisibility(), but does `electric'
	 * border scrolling (caret is never on the first or last
	 * visible line, if it ends up there the display is scrolled
	 * up or down)
	 * @param rect The rectangle to scroll to
	 */
	public void doElectricScroll(Rectangle rect)
	{
		SwingUtilities.invokeLater(new SyntaxSafeScroller(rect));
	}

	// private members
	private Color lineHighlightColor;
	private Object lineHighlightTag;
	private Color bracketHighlightColor;
	private Object bracketHighlightTag;
	private int electricLines;
	private boolean block;
	private Segment lineSegment;
	private int lastLine = -1;

	class SyntaxCaret extends DefaultCaret
	{
		public void focusGained(FocusEvent evt)
		{
			/* we do this even if the text area is read only,
			 * otherwise stuff like line and bracket highlighting
			 * will look weird without a caret */
			SyntaxCaret.this.setVisible(true);
		}

		public void adjustVisibility(Rectangle rect)
		{
			doElectricScroll(rect);
		}

		public void damage(Rectangle r)
		{
			if(r != null)
			{
				x = r.x;
				y = r.y;
				height = r.height + 2;
				SyntaxCaret.this.repaint();
			}
		}
				
		public void paint(Graphics g)
		{
			if(getDot() != getMark() ||
				!SyntaxCaret.this.isVisible())
				return;
			try
			{
				int dot = getDot();
				Rectangle r = modelToView(dot);
				width = g.getFontMetrics().charWidth('m');
				r.width = block ? width - 1 : 0;
				width += 2;
				g.setColor(getCaretColor());
				g.drawRect(r.x,r.y,r.width,r.height - 1);
			}
			catch(BadLocationException bl)
			{
				System.out.println("Caret fuckup:");
				bl.printStackTrace();
			}
		}
	}

	class SyntaxSafeScroller implements Runnable
	{
		private Rectangle rect;

		SyntaxSafeScroller(Rectangle rect)
		{
			this.rect = rect;
		}

		public void run()
		{
			int height = getToolkit().getFontMetrics(
				getFont()).getHeight();
			int y = Math.max(0,rect.y - height * electricLines);
			int lines = height * electricLines * 2;
			if(y + lines + rect.height <= getHeight())
			{
				rect.y = y;
				rect.height += lines;
			}
			scrollRectToVisible(rect);
		}
	}
			
	class CurrentLineHighlighter implements Highlighter.HighlightPainter
	{
		public void paint(Graphics g, int p0, int p1, Shape bounds,
			JTextComponent textComponent)
		{
			if(lineHighlightTag == null || getSelectionStart()
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

	class BracketHighlighter implements Highlighter.HighlightPainter
	{
		public void paint(Graphics g, int p0, int p1, Shape bounds,
			JTextComponent textComponent)
		{
			if(getSelectionStart() != getSelectionEnd()
				|| bracketHighlightTag == null)
				return;
			if(p0 == p1)
				return;
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
			g.fillRect(bracket.x,bracket.y,
				bracket.width,bracket.height);
		}
	}

	class SyntaxCaretListener implements CaretListener
	{
		public void caretUpdate(CaretEvent evt)
		{
			updateHighlighters();
		}
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.15  1999/03/27 23:47:57  sp
 * Updated docs, view tweak, goto-line fix, next/prev error tweak
 *
 * Revision 1.14  1999/03/27 02:46:17  sp
 * SyntaxTextArea is now modular
 *
 */
