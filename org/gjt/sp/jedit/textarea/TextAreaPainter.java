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
import java.awt.*;

import org.gjt.sp.jedit.syntax.*;

/**
 * The text area repaint manager. It performs double buffering and paints
 * lines of text.
 * @author Slava Pestov
 * @version $Id$
 */
public class TextAreaPainter extends Component implements TabExpander
{
	/**
	 * Creates a new repaint manager. This should be not be called
	 * directly.
	 */
	public TextAreaPainter(JEditTextArea textArea, int cols, int rows)
	{
		this.textArea = textArea;
		this.styles = styles;
		this.cols = cols;
		this.rows = rows;
		currentLine = new Segment();
		currentLineIndex = -1;

		firstInvalid = lastInvalid = -1;

		setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

		setFont(new Font("Monospaced",Font.PLAIN,14));
		setForeground(Color.black);
		setBackground(Color.white);
		setStyles(SyntaxUtilities.getDefaultSyntaxStyles());
		setCaretColor(Color.red);
		setSelectionColor(new Color(0xccccff));
		setLineHighlightColor(new Color(0xe0e0e0));
		setLineHighlight(true);
	}

	public SyntaxStyle[] getStyles()
	{
		return styles;
	}

	public void setStyles(SyntaxStyle[] styles)
	{
		this.styles = styles;
	}

	public Color getCaretColor()
	{
		return caretColor;
	}

	public void setCaretColor(Color caretColor)
	{
		this.caretColor = caretColor;
	}

	public Color getSelectionColor()
	{
		return selectionColor;
	}

	public void setSelectionColor(Color selectionColor)
	{
		this.selectionColor = selectionColor;
	}

	public Color getLineHighlightColor()
	{
		return lineHighlightColor;
	}

	public void setLineHighlightColor(Color lineHighlightColor)
	{
		this.lineHighlightColor = lineHighlightColor;
	}

	public boolean getLineHighlight()
	{
		return lineHighlight;
	}

	public void setLineHighlight(boolean lineHighlight)
	{
		this.lineHighlight = lineHighlight;
		invalidateCurrentLine();
	}

	public boolean getBlockCaret()
	{
		return blockCaret;
	}

	public void setBlockCaret(boolean blockCaret)
	{
		this.blockCaret = blockCaret;
		invalidateCurrentLine();
	}

	public FontMetrics getFontMetrics()
	{
		return getToolkit().getFontMetrics(getFont());
	}

	public void update(Graphics g)
	{
		// returns true if offscreen was created. When it's created,
		// all lines, not just the invalid ones, need to be painted.
		if(ensureOffscreenValid())
		{
			firstInvalid = textArea.getFirstLine();
			lastInvalid = firstInvalid + textArea.getVisibleLines();
		}

		if(firstInvalid != -1 && lastInvalid != -1)
		{
			offscreenRepaintLineRange(firstInvalid,lastInvalid);
			firstInvalid = lastInvalid = -1;
		}

		g.drawImage(offImg,0,0,null);
	}

	public void paint(Graphics g)
	{
		update(g);
	}

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

		firstLine = Math.max(firstLine,firstVisible);
		lastLine = Math.min(lastLine,lastVisible);

		if(firstInvalid == -1 && lastInvalid == -1)
		{
			firstInvalid = firstLine;
			lastInvalid = lastLine;
		}
		else
		{
			if(firstLine > firstInvalid && lastLine < lastInvalid)
				return;

			firstInvalid = Math.min(firstInvalid,firstLine);
			lastInvalid = Math.max(lastInvalid,lastLine);
		}

		/* Because the model uses cached text and token lists
		 * for the current line, we must invalidate that info
		 * if the current line changes.
		 */
		if(currentLineIndex >= firstInvalid &&
			currentLineIndex <= lastInvalid)
			currentLineIndex = -1;
	}

	public void invalidateLineRange(int firstLine, int lastLine)
	{
		_invalidateLineRange(firstLine,lastLine);
		repaint();
	}

	public void invalidateCurrentLine()
	{
		// this method is called by setLineHighlight(),
		// which is called from the constructor, where
		// the model is not yet set
		if(textArea.getModel() == null)
			return;
		int line = textArea.getModel().getCaretLine();
		invalidateLineRange(line,line);
	}

	public void scrollRepaint(int oldFirstLine, int newFirstLine)
	{
		if(offGfx == null)
			return;

		TextAreaModel model = textArea.getModel();
		int y = model.getLineHeight() * (oldFirstLine - newFirstLine);
		offGfx.copyArea(0,0,offImg.getWidth(this),offImg.getHeight(this),0,y);
		int visibleLines = textArea.getVisibleLines();

		if(oldFirstLine < newFirstLine)
		{
			invalidateLineRange(oldFirstLine + visibleLines,
				newFirstLine + visibleLines);
		}
		else
		{
			invalidateLineRange(newFirstLine, oldFirstLine);
		}
	}

	public void offscreenRepaint()
	{
		if(offGfx == null)
			return;
		Rectangle bounds = getBounds();

		int firstLine = textArea.getFirstLine();
		int lastLine = textArea.getModel().yToLine(bounds.height);

		offscreenRepaintLineRange(firstLine,lastLine);
	}

	public float nextTabStop(float x, int tabOffset)
	{
		int offset = textArea.getHorizontalOffset();
		int ntabs = ((int)x - offset) / tabSize;
		return (ntabs + 1) * tabSize + offset;
	}

	public Dimension getPreferredSize()
	{
		Dimension dim = new Dimension();
		FontMetrics fm = getFontMetrics();
		dim.width = fm.charWidth('w') * cols;
		dim.height = fm.getHeight() * rows;
		return dim;
	}

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

	protected boolean blockCaret;
	protected boolean lineHighlight;
	protected int cols;
	protected int rows;

	
	protected int tabSize;
	protected Graphics offGfx;
	protected Image offImg;

	protected int firstInvalid;
	protected int lastInvalid;

	protected boolean ensureOffscreenValid()
	{
		Dimension dim = getSize();
		if(offImg == null || offGfx == null
			|| offImg.getWidth(null) != dim.width
			|| offImg.getHeight(null) != dim.height)
		{
			offImg = textArea.createImage(dim.width,dim.height);
			offGfx = offImg.getGraphics();
			offscreenRepaint();
			return true;
		}
		else
			return false;
	}

	protected int offscreenRepaintLineRange(int firstLine, int lastLine)
	{
		if(offGfx == null)
			return 0;

		TextAreaModel model = textArea.getModel();

		FontMetrics fm = getFontMetrics();
		tabSize = fm.charWidth('w') * ((Integer)model.getDocument()
			.getProperty(PlainDocument.tabSizeAttribute))
			.intValue();

		int x = textArea.getHorizontalOffset();
		int y = model.lineToY(firstLine);

		currentLineIndex = firstLine;
		while(currentLineIndex <= lastLine)
		{
			currentLineIndex += paintLine(model,offGfx,x,y);
			y += model.getLineHeight();
		}

		return currentLineIndex - firstLine;
	}

	protected int paintLine(TextAreaModel model, Graphics gfx,
		int x, int y)
	{
		TokenMarker tokenMarker = model.getTokenMarker();
		Font defaultFont = getFont();
		Color defaultColor = getForeground();

		/* Clear the line's bounding rectangle */
		FontMetrics fm = getFontMetrics();
		int gap = fm.getMaxDescent() + fm.getLeading();
		gfx.setColor(getBackground());
		gfx.fillRect(0,y + gap,offImg.getWidth(this),fm.getHeight());
				
		gfx.setFont(defaultFont);

		if(currentLineIndex < 0
			|| currentLineIndex >= model.getLineCount())
		{
			gfx.setColor(defaultColor);
			gfx.drawString("~",0,y + model.getLineHeight());
			return 1;
		}

		// Get the text
		model.getLineText(currentLineIndex,currentLine);

		// draw the highlights
		if(currentLineIndex >= model.getSelectionStartLine()
			&& currentLineIndex <= model.getSelectionEndLine())
			paintHighlight(model,gfx,y);

		gfx.setColor(defaultColor);

		if(tokenMarker == null)
		{
			paintPlainLine(model,gfx,defaultFont,defaultColor,
				x,y + model.getLineHeight());
			return 1;
		}
		else
		{
			int count = 0;
			int lastVisibleLine = textArea.getFirstLine()
				+ textArea.getVisibleLines();
			do
			{
				model.getLineText(currentLineIndex,currentLine);
				currentLineTokens = tokenMarker.markTokens(
					currentLine,currentLineIndex);
				currentLineIndex++;

				if(currentLineIndex <= lastVisibleLine)
				{
					y += model.getLineHeight();
					paintSyntaxLine(model,gfx,defaultFont,
						defaultColor,x,y);
				}

				count++;
			}
			while(tokenMarker.isNextLineRequested());
			return count;
		}
	}

	protected void paintPlainLine(TextAreaModel model, Graphics gfx,
		Font defaultFont, Color defaultColor, int x, int y)
	{
		Utilities.drawTabbedText(currentLine,x,y,gfx,this,0);
	}

	protected void paintSyntaxLine(TextAreaModel model, Graphics gfx,
		Font defaultFont, Color defaultColor, int x, int y)
	{
		int offset = 0;
		for(;;)
		{
			byte id = currentLineTokens.id;
			if(id == Token.END)
				break;

			int length = currentLineTokens.length;
			if(id == Token.NULL)
			{
				if(!defaultColor.equals(gfx.getColor()))
					gfx.setColor(defaultColor);
				if(!defaultFont.equals(gfx.getFont()))
					gfx.setFont(defaultFont);
			}
			else
				styles[id].setGraphicsFlags(gfx,defaultFont);

			currentLine.count = length;
			x = Utilities.drawTabbedText(currentLine,x,y,gfx,this,offset);
			currentLine.offset += length;
			offset += length;

			currentLineTokens = currentLineTokens.next;
		}
	}

	protected void paintHighlight(TextAreaModel model, Graphics gfx, int y)
	{
		FontMetrics fm = getFontMetrics();
		int height = fm.getHeight();
		y += fm.getLeading() + fm.getMaxDescent();

		int selectionStart = model.getSelectionStart();
		int selectionEnd = model.getSelectionEnd();

		if(selectionStart == selectionEnd)
		{
			if(lineHighlight)
			{
				gfx.setColor(lineHighlightColor);
				gfx.fillRect(0,y,offImg.getWidth(this),height);
			}
		}
		else
		{
			gfx.setColor(selectionColor);

			int selectionStartLine = model.getSelectionStartLine();
			int selectionEndLine = model.getSelectionEndLine();
			int lineStart = model.getLineStartOffset(currentLineIndex);

			int x1, x2;
			if(selectionStartLine == selectionEndLine)
			{
				x1 = model.offsetToX(currentLineIndex,selectionStart
					- lineStart);
				x2 = model.offsetToX(currentLineIndex,selectionEnd
					- lineStart);
			}
			else if(currentLineIndex == selectionStartLine)
			{
				x1 = model.offsetToX(currentLineIndex,selectionStart
					- lineStart);
				x2 = offImg.getWidth(this);
			}
			else if(currentLineIndex == selectionEndLine)
			{
				x1 = 0;
				x2 = model.offsetToX(currentLineIndex,selectionEnd
					- lineStart);
			}
			else
			{
				x1 = 0;
				x2 = offImg.getWidth(this);
			}

			gfx.fillRect(x1,y,x2 - x1,height);
		}

		if(textArea.getCaretVisible()
			&& currentLineIndex == model.getCaretLine())
		{
			int offset = model.getCaretPosition() 
				- model.getLineStartOffset(currentLineIndex);
			int caretX = model.offsetToX(currentLineIndex,offset);
			int caretWidth = (blockCaret ?
				fm.charWidth('w') : 1);
			gfx.setColor(caretColor);
			gfx.fillRect(caretX,y,caretWidth,height);
		}
	}
}

/*
 * ChangeLog:
 * $Log$
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
