/*
 * TextAreaPainter.java - Performs double buffering and paints the text area
 * Copyright (C) 1999 Slava Pestov
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA.
 */

package org.gjt.sp.jedit.textarea;

import javax.swing.text.*;
import javax.swing.JComponent;
import java.awt.*;

import org.gjt.sp.jedit.syntax.*;

/**
 * The text area repaint manager. It performs double buffering and paints
 * lines of text.
 * @author Slava Pestov
 * @version $Id$
 */
public class TextAreaPainter extends JComponent implements TabExpander
{
	/**
	 * Creates a new repaint manager. This should be not be called
	 * directly.
	 */
	public TextAreaPainter(JEditTextArea textArea, TextAreaDefaults defaults)
	{
		this.textArea = textArea;

		currentLine = new Segment();
		currentLineIndex = -1;

		firstInvalid = lastInvalid = -1;

		setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

		setFont(new Font("Monospaced",Font.PLAIN,14));
		setForeground(Color.black);
		setBackground(Color.white);

		styles = defaults.styles;
		cols = defaults.cols;
		rows = defaults.rows;
		caretColor = defaults.caretColor;
		selectionColor = defaults.selectionColor;
		lineHighlightColor = defaults.lineHighlightColor;
		lineHighlight = defaults.lineHighlight;
		bracketHighlightColor = defaults.bracketHighlightColor;
		bracketHighlight = defaults.bracketHighlight;
		eolMarkerColor = defaults.eolMarkerColor;
		eolMarkers = defaults.eolMarkers;

		copyAreaBroken = defaults.copyAreaBroken;
	}

	/**
	 * Returns if this component can be traversed by pressing the
	 * Tab key. This returns false.
	 */
	public final boolean isManagingFocus()
	{
		return false;
	}

	/**
	 * Returns the syntax styles used to paint colorized text. Entry <i>n</i>
	 * will be used to paint tokens with id = <i>n</i>.
	 * @see org.gjt.sp.jedit.syntax.Token
	 */
	public final SyntaxStyle[] getStyles()
	{
		return styles;
	}

	/**
	 * Sets the syntax styles used to paint colorized text. Entry <i>n</i>
	 * will be used to paint tokens with id = <i>n</i>.
	 * @param styles The syntax styles
	 * @see org.gjt.sp.jedit.syntax.Token
	 */
	public final void setStyles(SyntaxStyle[] styles)
	{
		this.styles = styles;
		invalidateOffscreen();
		repaint();
	}

	/**
	 * Returns the caret color.
	 */
	public final Color getCaretColor()
	{
		return caretColor;
	}

	/**
	 * Sets the caret color.
	 * @param caretColor The caret color
	 */
	public final void setCaretColor(Color caretColor)
	{
		this.caretColor = caretColor;
		invalidateSelectedLines();
	}

	/**
	 * Returns the selection color.
	 */
	public final Color getSelectionColor()
	{
		return selectionColor;
	}

	/**
	 * Sets the selection color.
	 * @param selectionColor The selection color
	 */
	public final void setSelectionColor(Color selectionColor)
	{
		this.selectionColor = selectionColor;
		invalidateSelectedLines();
	}

	/**
	 * Returns the line highlight color.
	 */
	public final Color getLineHighlightColor()
	{
		return lineHighlightColor;
	}

	/**
	 * Sets the line highlight color.
	 * @param lineHighlightColor The line highlight color
	 */
	public final void setLineHighlightColor(Color lineHighlightColor)
	{
		this.lineHighlightColor = lineHighlightColor;
		invalidateSelectedLines();
	}

	/**
	 * Returns true if line highlight is enabled, false otherwise.
	 */
	public final boolean isLineHighlightEnabled()
	{
		return lineHighlight;
	}

	/**
	 * Enables or disables current line highlighting.
	 * @param lineHighlight True if current line highlight should be enabled,
	 * false otherwise
	 */
	public final void setLineHighlightEnabled(boolean lineHighlight)
	{
		this.lineHighlight = lineHighlight;
		invalidateSelectedLines();
	}

	/**
	 * Returns the bracket highlight color.
	 */
	public final Color getBracketHighlightColor()
	{
		return bracketHighlightColor;
	}

	/**
	 * Sets the bracket highlight color.
	 * @param bracketHighlightColor The bracket highlight color
	 */
	public final void setBracketHighlightColor(Color bracketHighlightColor)
	{
		this.bracketHighlightColor = bracketHighlightColor;
		invalidateLine(textArea.getBracketLine());
	}

	/**
	 * Returns true if bracket highlighting is enabled, false otherwise.
	 * When bracket highlighting is enabled, the bracket matching the
	 * one before the caret (if any) is highlighted.
	 */
	public final boolean isBracketHighlightEnabled()
	{
		return bracketHighlight;
	}

	/**
	 * Enables or disables bracket highlighting.
	 * When bracket highlighting is enabled, the bracket matching the
	 * one before the caret (if any) is highlighted.
	 * @param bracketHighlight True if bracket highlighting should be
	 * enabled, false otherwise
	 */
	public final void setBracketHighlightEnabled(boolean bracketHighlight)
	{
		this.bracketHighlight = bracketHighlight;
		invalidateLine(textArea.getBracketLine());
	}

	/**
	 * Returns true if the caret should be drawn as a block, false otherwise.
	 */
	public final boolean isBlockCaretEnabled()
	{
		return blockCaret;
	}

	/**
	 * Sets if the caret should be drawn as a block, false otherwise.
	 * @param blockCaret True if the caret should be drawn as a block,
	 * false otherwise.
	 */
	public final void setBlockCaretEnabled(boolean blockCaret)
	{
		this.blockCaret = blockCaret;
		invalidateSelectedLines();
	}

	/**
	 * Returns the EOL marker color.
	 */
	public final Color getEOLMarkerColor()
	{
		return eolMarkerColor;
	}

	/**
	 * Sets the EOL marker color.
	 * @param eolMarkerColor The EOL marker color
	 */
	public final void setEOLMarkerColor(Color eolMarkerColor)
	{
		this.eolMarkerColor = eolMarkerColor;
		invalidateOffscreen();
		repaint();
	}

	/**
	 * Returns true if EOL markers are drawn, false otherwise.
	 */
	public final boolean isEOLMarkerEnabled()
	{
		return eolMarkers;
	}

	/**
	 * Sets if EOL markers are to be drawn.
	 * @param eolMarkers True if EOL markers should be dranw, false otherwise
	 */
	public final void setEOLMarkerEnabled(boolean eolMarkers)
	{
		this.eolMarkers = eolMarkers;
		invalidateOffscreen();
		repaint();
	}

	/**
	 * Returns the font metrics used by this component.
	 */
	public FontMetrics getFontMetrics()
	{
		return fm;
	}

	/**
	 * Sets the font for this component. This is overridden to update the
	 * cached font metrics and to recalculate which lines are visible.
	 * @param font The font
	 */
	public void setFont(Font font)
	{
		super.setFont(font);
		fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
		textArea.recalculateVisibleLines();
	}

	/**
	 * Returns if the copyArea() should not be used.
	 */
	public boolean isCopyAreaBroken()
	{
		return copyAreaBroken;
	}

	/**
	 * Disables the use of the copyArea() function (which is broken in
	 * JDK 1.2).
	 */
	public void setCopyAreaBroken(boolean copyAreaBroken)
	{
		this.copyAreaBroken = copyAreaBroken;
	}

	/**
	 * Queues a repaint of the changed lines only.
	 */
	public final void fastRepaint()
	{
		if(firstInvalid == -1 && lastInvalid == -1)
			repaint();
		else
		{
			repaint(0,textArea.lineToY(firstInvalid)
				+ fm.getLeading() + fm.getMaxDescent(),
				getWidth(),(lastInvalid - firstInvalid + 1)
				* fm.getHeight());
		}
	}

	/**
	 * Paints any lines that changed since the last paint to the offscreen
	 * graphics, then repaints the offscreen to the specified graphics
	 * context.
	 * @param g The graphics context
	 */
	public void update(Graphics g)
	{
		tabSize = fm.charWidth('w') * ((Integer)textArea
			.getDocument().getProperty(
			PlainDocument.tabSizeAttribute)).intValue();

		// returns true if offscreen was created. When it's created,
		// all lines, not just the invalid ones, need to be painted.
		if(ensureOffscreenValid())
		{
			firstInvalid = textArea.getFirstLine();
			lastInvalid = firstInvalid + textArea.getVisibleLines();
		}

		if(firstInvalid != -1 && lastInvalid != -1)
		{
			int lineCount;
			try
			{
				if(firstInvalid == lastInvalid)
				{
					lineCount = offscreenRepaintLine(firstInvalid,
						textArea.getHorizontalOffset());
				}
				else
				{
					lineCount = offscreenRepaintLineRange(
						firstInvalid,lastInvalid);
				}
				if(lastInvalid - firstInvalid + 1 != lineCount)
				{
					// XXX stupid hack
					Rectangle clip = g.getClipBounds();
					if(!clip.equals(getBounds()))
						repaint();
				}
			}
			catch(Exception e)
			{
				System.err.println("Error repainting line"
					+ " range {" + firstInvalid + ","
					+ lastInvalid + "}:");
				e.printStackTrace();
			}
			firstInvalid = lastInvalid = -1;
		}

		g.drawImage(offImg,0,0,null);
	}

	/**
	 * Same as <code>update(g)</code>.
	 */
	public void paint(Graphics g)
	{
		update(g);
	}

	/**
	 * Marks a line as needing a repaint, but doesn't actually repaint it
	 * until <code>repaint()</code> is called manually.
	 * @param line The line to invalidate
	 */
	public void _invalidateLine(int line)
	{
		int firstVisible = textArea.getFirstLine();
		int lastVisible = firstVisible + textArea.getVisibleLines();

		if(line < firstVisible || line > lastVisible)
			return;

		if(line >= firstInvalid && line <= lastInvalid)
			return;

		if(firstInvalid == -1 && lastInvalid == -1)
			firstInvalid = lastInvalid = line;
		else
		{
			firstInvalid = Math.min(line,firstInvalid);
			lastInvalid = Math.max(line,lastInvalid);
		}
	}

	/**
	 * Repaints the specified line. This is equivalent to calling
	 * <code>_invalidateLine()</code> and <code>repaint()</code>.
	 * @param line The line
	 * @see #_invalidateLine(int)
	 */
	public final void invalidateLine(int line)
	{
		_invalidateLine(line);
		fastRepaint();
	}

	/**
	 * Marks a range of lines as needing a repaint, but doesn't actually
	 * repaint them until <code>repaint()</code> is called.
	 * @param firstLine The first line to invalidate
	 * @param lastLine The last line to invalidate
	 */
	public void _invalidateLineRange(int firstLine, int lastLine)
	{
		int firstVisible = textArea.getFirstLine();
		int lastVisible = firstVisible + textArea.getVisibleLines();

		if(firstLine > lastLine)
		{
			int tmp = firstLine;
			firstLine = lastLine;
			lastLine = tmp;
		}

		if(lastLine < firstVisible || firstLine > lastVisible)
			return;

		if(firstInvalid == -1 && lastInvalid == -1)
		{
			firstInvalid = firstLine;
			lastInvalid = lastLine;
		}
		else
		{
			if(firstLine >= firstInvalid && lastLine <= lastInvalid)
				return;

			firstInvalid = Math.min(firstInvalid,firstLine);
			lastInvalid = Math.max(lastInvalid,lastLine);
		}

		firstInvalid = Math.max(firstInvalid,firstVisible);
		lastInvalid = Math.min(lastInvalid,lastVisible);
	}

	/**
	 * Repaints the specified line range. This is equivalent to calling
	 * <code>_invalidateLineRange()</code> then <code>repaint()</code>.
	 * @param firstLine The first line to repaint
	 * @param lastLine The last line to repaint
	 */
	public final void invalidateLineRange(int firstLine, int lastLine)
	{
		_invalidateLineRange(firstLine,lastLine);
		fastRepaint();
	}

	/**
	 * Repaints the lines containing the selection.
	 */
	public final void invalidateSelectedLines()
	{
		invalidateLineRange(textArea.getSelectionStartLine(),
			textArea.getSelectionEndLine());
	}

	/**
	 * Simulates scrolling from <code>oldFirstLine</code> to
	 * <code>newFirstLine</code> by shifting the offscreen graphics
	 * and repainting any revealed lines. This should not be called
	 * directly; use <code>JEditTextArea.setFirstLine()</code>
	 * instead.
	 * @param oldFirstLine The old first line
	 * @param newFirstLine The new first line
	 * @see org.gjt.sp.jedit.textarea.JEditTextArea#setFirstLine(int)
	 */
	public void scrollRepaint(int oldFirstLine, int newFirstLine)
	{
		if(offGfx == null)
			return;

		int visibleLines = textArea.getVisibleLines();

		// No point doing this crap if the user scrolled by >= visibleLines
		if(copyAreaBroken || oldFirstLine + visibleLines <= newFirstLine
			|| newFirstLine + visibleLines <= oldFirstLine)
		{
			_invalidateLineRange(newFirstLine,newFirstLine + visibleLines + 1);
		}
		else
		{
			int y = fm.getHeight() * (oldFirstLine - newFirstLine);
			offGfx.copyArea(0,0,offImg.getWidth(this),offImg.getHeight(this),0,y);

			if(oldFirstLine < newFirstLine)
			{
				_invalidateLineRange(oldFirstLine + visibleLines - 1,
					newFirstLine + visibleLines + 1);
			}
			else
			{
				_invalidateLineRange(newFirstLine, oldFirstLine);
			}
		}
	}

	/**
	 * Invalidates the offscreen graphics context. This should not be called
	 * directly.
	 */
	public final void invalidateOffscreen()
	{
		offImg = null;
		offGfx = null;
	}

	/**
	 * Implementation of TabExpander interface. Returns next tab stop after
	 * a specified point.
	 * @param x The x co-ordinate
	 * @param tabOffset Ignored
	 * @return The next tab stop after <i>x</i>
	 */
	public float nextTabStop(float x, int tabOffset)
	{
		int offset = textArea.getHorizontalOffset();
		int ntabs = ((int)x - offset) / tabSize;
		return (ntabs + 1) * tabSize + offset;
	}

	/**
	 * Returns the painter's preferred size.
	 */
	public Dimension getPreferredSize()
	{
		Dimension dim = new Dimension();
		dim.width = fm.charWidth('w') * cols;
		dim.height = fm.getHeight() * rows;
		return dim;
	}


	/**
	 * Returns the painter's minimum size.
	 */
	public Dimension getMinimumSize()
	{
		return getPreferredSize();
	}

	// package-private members
	int currentLineIndex;
	Token currentLineTokens;
	Segment currentLine;

	// protected members
	protected JEditTextArea textArea;
	
	protected SyntaxStyle[] styles;
	protected Color caretColor;
	protected Color selectionColor;
	protected Color lineHighlightColor;
	protected Color bracketHighlightColor;
	protected Color eolMarkerColor;

	protected boolean blockCaret;
	protected boolean lineHighlight;
	protected boolean bracketHighlight;
	protected boolean eolMarkers;
	protected int cols;
	protected int rows;
	
	protected int tabSize;
	protected FontMetrics fm;
	protected Graphics offGfx;
	protected Image offImg;

	protected int firstInvalid;
	protected int lastInvalid;

	protected static boolean copyAreaBroken;

	protected boolean ensureOffscreenValid()
	{
		if(offImg == null || offGfx == null)
		{
			offImg = textArea.createImage(getWidth(),getHeight());
			offGfx = offImg.getGraphics();
			return true;
		}
		else
			return false;
	}

	protected int offscreenRepaintLineRange(int firstLine, int lastLine)
	{
		if(offGfx == null)
			return 0;

		int x = textArea.getHorizontalOffset();

		int line;
		for(line = firstLine; line <= lastLine;)
		{
			line += offscreenRepaintLine(line,x);
		}

		return line - firstLine;
	}

	protected int offscreenRepaintLine(int line, int x)
	{
		TokenMarker tokenMarker = textArea.getTokenMarker();
		Font defaultFont = getFont();
		Color defaultColor = getForeground();
				
		int y = textArea.lineToY(line);

		if(line < 0 || line >= textArea.getLineCount())
		{
			paintHighlight(line,y);
			styles[Token.INVALID].setGraphicsFlags(offGfx,defaultFont);
			offGfx.drawString("~",0,y + fm.getHeight());
			return 1;
		}

		if(tokenMarker == null)
		{
			currentLineIndex = line;
			paintPlainLine(line,defaultFont,defaultColor,x,y);
			return 1;
		}
		else
		{
			int count = 0;
			int lastVisibleLine = Math.min(textArea.getLineCount(),
				textArea.getFirstLine() + textArea.getVisibleLines());
			do
			{
				currentLineIndex = line + count;
				paintSyntaxLine(tokenMarker,currentLineIndex,
					defaultFont,defaultColor,x,y);
				y += fm.getHeight();

				count++;
			}
			while(tokenMarker.isNextLineRequested()
				&& line + count < lastVisibleLine);
			return count;
		}
	}

	protected void paintPlainLine(int line, Font defaultFont,
		Color defaultColor, int x, int y)
	{
		paintHighlight(line,y);
		textArea.getLineText(line,currentLine);

		offGfx.setFont(defaultFont);
		offGfx.setColor(defaultColor);

		y += fm.getHeight();
		x = Utilities.drawTabbedText(currentLine,x,y,offGfx,this,0);

		if(eolMarkers)
		{
			offGfx.setColor(eolMarkerColor);
			offGfx.drawString(".",x,y);
		}
	}

	protected void paintSyntaxLine(TokenMarker tokenMarker, int line,
		Font defaultFont, Color defaultColor, int x, int y)
	{
		textArea.getLineText(currentLineIndex,currentLine);
		currentLineTokens = tokenMarker.markTokens(currentLine,
			currentLineIndex);

		paintHighlight(line,y);

		offGfx.setFont(defaultFont);
		offGfx.setColor(defaultColor);
		y += fm.getHeight();
		x = SyntaxUtilities.paintSyntaxLine(currentLine,
			currentLineTokens,styles,this,offGfx,x,y);

		if(eolMarkers)
		{
			offGfx.setColor(eolMarkerColor);
			offGfx.drawString(".",x,y);
		}
	}

	protected void paintHighlight(int line, int y)
	{
		/* Clear the line's bounding rectangle */
		int gap = fm.getMaxDescent() + fm.getLeading();
		offGfx.setColor(getBackground());
		offGfx.fillRect(0,y + gap,offImg.getWidth(this),fm.getHeight());

		if(line >= textArea.getSelectionStartLine()
			&& line <= textArea.getSelectionEndLine())
			paintLineHighlight(line,y);

		if(bracketHighlight && line == textArea.getBracketLine())
			paintBracketHighlight(line,y);

		if(line == textArea.getCaretLine())
			paintCaret(line,y);
	}

	protected void paintLineHighlight(int line, int y)
	{
		int height = fm.getHeight();
		y += fm.getLeading() + fm.getMaxDescent();

		int selectionStart = textArea.getSelectionStart();
		int selectionEnd = textArea.getSelectionEnd();

		if(selectionStart == selectionEnd)
		{
			if(lineHighlight)
			{
				offGfx.setColor(lineHighlightColor);
				offGfx.fillRect(0,y,offImg.getWidth(this),height);
			}
		}
		else
		{
			offGfx.setColor(selectionColor);

			int selectionStartLine = textArea.getSelectionStartLine();
			int selectionEndLine = textArea.getSelectionEndLine();
			int lineStart = textArea.getLineStartOffset(line);

			int x1, x2;
			if(selectionStartLine == selectionEndLine)
			{
				x1 = textArea.offsetToX(line,
					selectionStart - lineStart);
				x2 = textArea.offsetToX(line,
					selectionEnd - lineStart);
			}
			else if(line == selectionStartLine)
			{
				x1 = textArea.offsetToX(line,
					selectionStart - lineStart);
				x2 = offImg.getWidth(this);
			}
			else if(line == selectionEndLine)
			{
				x1 = 0;
				x2 = textArea.offsetToX(line,
					selectionEnd - lineStart);
			}
			else
			{
				x1 = 0;
				x2 = offImg.getWidth(this);
			}

			offGfx.fillRect(x1,y,x2 - x1,height);
		}

	}

	protected void paintBracketHighlight(int line, int y)
	{
		int position = textArea.getBracketPosition();
		if(position == -1)
			return;
		y += fm.getLeading() + fm.getMaxDescent();
		int x = textArea.offsetToX(line,position);
		offGfx.setColor(bracketHighlightColor);
		// Hack!!! Since there is no fast way to get the character
		// from the bracket matching routine, we use ( since all
		// brackets probably have the same width anyway
		offGfx.drawRect(x,y,fm.charWidth('(') - 1,
			fm.getHeight() - 1);
	}

	protected void paintCaret(int line, int y)
	{
		if(textArea.isCaretVisible())
		{
			int offset = textArea.getCaretPosition() 
				- textArea.getLineStartOffset(line);
			int caretX = textArea.offsetToX(line,offset);
			int caretWidth = ((blockCaret ||
				textArea.isOverwriteEnabled()) ?
				fm.charWidth('w') : 1);
			y += fm.getLeading() + fm.getMaxDescent();
			int height = fm.getHeight();
			
			offGfx.setColor(caretColor);

			if(textArea.isOverwriteEnabled())
			{
				offGfx.fillRect(caretX,y + height - 1,
					caretWidth,1);
			}
			else
			{
				offGfx.drawRect(caretX,y,caretWidth - 1,height - 1);
			}
		}
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.14  1999/08/21 01:48:18  sp
 * jEdit 2.0pre8
 *
 * Revision 1.13  1999/07/29 08:50:21  sp
 * Misc stuff for 1.7pre7
 *
 * Revision 1.12  1999/07/16 23:45:49  sp
 * 1.7pre6 BugFree version
 *
 * Revision 1.11  1999/07/08 06:06:04  sp
 * Bug fixes and miscallaneous updates
 *
 * Revision 1.10  1999/07/05 04:38:40  sp
 * Massive batch of changes... bug fixes, also new text component is in place.
 * Have fun
 *
 * Revision 1.9  1999/06/30 07:08:02  sp
 * Text area bug fixes
 *
 * Revision 1.8  1999/06/30 05:01:55  sp
 * Lots of text area bug fixes and optimizations
 *
 * Revision 1.7  1999/06/29 09:01:24  sp
 * Text area now does bracket matching, eol markers, also xToOffset() and
 * offsetToX() now work better
 *
 * Revision 1.6  1999/06/28 09:17:20  sp
 * Perl mode javac compile fix, text area hacking
 *
 * Revision 1.5  1999/06/27 04:53:16  sp
 * Text selection implemented in text area, assorted bug fixes
 *
 * Revision 1.4  1999/06/25 06:54:08  sp
 * Text area updates
 *
 */
