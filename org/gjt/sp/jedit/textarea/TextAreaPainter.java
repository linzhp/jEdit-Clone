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
	}

	public SyntaxStyle[] getStyles()
	{
		return styles;
	}

	public void setStyles(SyntaxStyle[] styles)
	{
		this.styles = styles;
		invalidateOffscreen();
		repaint();
	}

	public Color getCaretColor()
	{
		return caretColor;
	}

	public void setCaretColor(Color caretColor)
	{
		this.caretColor = caretColor;
		invalidateSelectedLines();
	}

	public Color getSelectionColor()
	{
		return selectionColor;
	}

	public void setSelectionColor(Color selectionColor)
	{
		this.selectionColor = selectionColor;
		invalidateSelectedLines();
	}

	public Color getLineHighlightColor()
	{
		return lineHighlightColor;
	}

	public void setLineHighlightColor(Color lineHighlightColor)
	{
		this.lineHighlightColor = lineHighlightColor;
		invalidateSelectedLines();
	}

	public boolean isLineHighlightEnabled()
	{
		return lineHighlight;
	}

	public void setLineHighlightEnabled(boolean lineHighlight)
	{
		this.lineHighlight = lineHighlight;
		invalidateSelectedLines();
	}

	public Color getBracketHighlightColor()
	{
		return bracketHighlightColor;
	}

	public void setBracketHighlightColor(Color bracketHighlightColor)
	{
		this.bracketHighlightColor = bracketHighlightColor;
		invalidateLine(textArea.getBracketLine());
	}

	public boolean isBracketHighlightEnabled()
	{
		return bracketHighlight;
	}

	public void setBracketHighlightEnabled(boolean bracketHighlight)
	{
		this.bracketHighlight = bracketHighlight;
		invalidateLine(textArea.getBracketLine());
	}

	public boolean isBlockCaretEnabled()
	{
		return blockCaret;
	}

	public void setBlockCaretEnabled(boolean blockCaret)
	{
		this.blockCaret = blockCaret;
		invalidateSelectedLines();
	}

	public Color getEOLMarkerColor()
	{
		return eolMarkerColor;
	}

	public void setEOLMarkerColor(Color eolMarkerColor)
	{
		this.eolMarkerColor = eolMarkerColor;
		invalidateSelectedLines();
	}

	public boolean isEOLMarkerEnabled()
	{
		return eolMarkers;
	}

	public void setEOLMarkerEnabled(boolean eolMarkers)
	{
		this.eolMarkers = eolMarkers;
		invalidateSelectedLines();
	}

	public FontMetrics getFontMetrics()
	{
		if(fm == null)
			fm = Toolkit.getDefaultToolkit().getFontMetrics(getFont());
		return fm;
	}

	public void setFont(Font font)
	{
		super.setFont(font);
		fm = null;
		tabSize = 0;
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
			try
			{
				if(firstInvalid == lastInvalid)
				{
					paintLine(offGfx,firstInvalid,textArea
						.getHorizontalOffset());
				}
				else
					offscreenRepaintLineRange(firstInvalid,
						lastInvalid);
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

	public void paint(Graphics g)
	{
		update(g);
	}

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

	public void invalidateLine(int line)
	{
		_invalidateLine(line);
		repaint();
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

	public void invalidateLineRange(int firstLine, int lastLine)
	{
		_invalidateLineRange(firstLine,lastLine);
		repaint();
	}

	public void invalidateSelectedLines()
	{
		invalidateLineRange(textArea.getSelectionStartLine(),
			textArea.getSelectionEndLine());
	}

	public void scrollRepaint(int oldFirstLine, int newFirstLine)
	{
		if(offGfx == null)
			return;

		int y = getFontMetrics().getHeight() * (oldFirstLine - newFirstLine);
		offGfx.copyArea(0,0,offImg.getWidth(this),offImg.getHeight(this),0,y);
		int visibleLines = textArea.getVisibleLines();

		if(oldFirstLine < newFirstLine)
		{
			invalidateLineRange(oldFirstLine + visibleLines - 1,
				newFirstLine + visibleLines + 1);
		}
		else
		{
			invalidateLineRange(newFirstLine, oldFirstLine);
		}
	}

	public void invalidateOffscreen()
	{
		offImg = null;
		offGfx = null;
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

	protected boolean ensureOffscreenValid()
	{
		if(offImg == null || offGfx == null)
		{
			Dimension dim = getSize();
			offImg = textArea.createImage(dim.width,dim.height);
			offGfx = offImg.getGraphics();
			return true;
		}
		else
			return false;
	}

	protected void offscreenRepaint()
	{
		if(offGfx == null)
			return;
		Rectangle bounds = getBounds();

		int firstLine = textArea.getFirstLine();
		int lastLine = textArea.yToLine(bounds.height);

		offscreenRepaintLineRange(firstLine,lastLine);

		firstInvalid = lastInvalid = -1;
	}

	protected int offscreenRepaintLineRange(int firstLine, int lastLine)
	{
		if(offGfx == null)
			return 0;

		FontMetrics fm = getFontMetrics();
		if(tabSize == 0)
		{
			tabSize = fm.charWidth('w') * ((Integer)textArea
				.getDocument().getProperty(
				PlainDocument.tabSizeAttribute)).intValue();
		}

		int x = textArea.getHorizontalOffset();

		int line;
		for(line = firstLine; line <= lastLine;)
		{
			line += paintLine(offGfx,line,x);
		}

		return line - firstLine;
	}

	protected int paintLine(Graphics gfx, int line, int x)
	{
		TokenMarker tokenMarker = textArea.getTokenMarker();
		Font defaultFont = getFont();
		Color defaultColor = getForeground();
				
		gfx.setFont(defaultFont);

		int y = textArea.lineToY(line);

		if(line < 0 || line >= textArea.getLineCount())
		{
			paintHighlight(gfx,line,y);
			gfx.setColor(defaultColor);
			gfx.drawString("~",0,y + getFontMetrics().getHeight());
			return 1;
		}

		// Get the text
		textArea.getLineText(line,currentLine);
		currentLineIndex = line;

		if(tokenMarker == null)
		{
			paintPlainLine(gfx,line,defaultFont,defaultColor,x,y);
			return 1;
		}
		else
		{
			int count = 0;
			int lastVisibleLine = textArea.getFirstLine()
				+ textArea.getVisibleLines();
			int lastLine = textArea.getLineCount();
			do
			{
				if(count != 0)
				{
					currentLineIndex = line + count;
					textArea.getLineText(currentLineIndex,
						currentLine);
				}

				currentLineTokens = tokenMarker.markTokens(
						currentLine,currentLineIndex);

				paintSyntaxLine(gfx,line + count,defaultFont,
					defaultColor,x,y);
				y += getFontMetrics().getHeight();

				count++;
			}
			while(tokenMarker.isNextLineRequested()
				&& line + count < lastVisibleLine);
			return count;
		}
	}

	protected void paintPlainLine(Graphics gfx, int line, Font defaultFont,
		Color defaultColor, int x, int y)
	{
		paintHighlight(gfx,line,y);

		gfx.setColor(defaultColor);

		y += getFontMetrics().getHeight();
		x = Utilities.drawTabbedText(currentLine,x,y,gfx,this,0);

		if(eolMarkers)
		{
			gfx.setColor(eolMarkerColor);
			gfx.drawString(".",x,y);
		}
	}

	protected void paintSyntaxLine(Graphics gfx, int line, Font defaultFont,
		Color defaultColor, int x, int y)
	{
		paintHighlight(gfx,line,y);

		y += getFontMetrics().getHeight();

		// We do this because xToOffset() uses currentLine to avoid
		// unnecessary getText()s, and we must keep the offset and
		// count values from being mangled
		int segmentOffset = currentLine.offset;
		int segmentCount = currentLine.count;

		int offset = 0;
		Token tokens = currentLineTokens;
		for(;;)
		{
			byte id = tokens.id;
			if(id == Token.END)
				break;

			int length = tokens.length;
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
			x = Utilities.drawTabbedText(currentLine,x,y,gfx,this,0);
			currentLine.offset += length;
			offset += length;

			tokens = tokens.next;
		}
		if(eolMarkers)
		{
			gfx.setColor(eolMarkerColor);
			gfx.drawString(".",x,y);
		}

		currentLine.offset = segmentOffset;
		currentLine.count = segmentCount;
	}

	protected void paintHighlight(Graphics gfx, int line, int y)
	{
		/* Clear the line's bounding rectangle */
		FontMetrics fm = getFontMetrics();
		int gap = fm.getMaxDescent() + fm.getLeading();
		gfx.setColor(getBackground());
		gfx.fillRect(0,y + gap,offImg.getWidth(this),fm.getHeight());

		if(line >= textArea.getSelectionStartLine()
			&& line <= textArea.getSelectionEndLine())
			paintLineHighlight(gfx,line,y);

		if(bracketHighlight && line == textArea.getBracketLine())
			paintBracketHighlight(gfx,line,y);

		if(line == textArea.getCaretLine())
			paintCaret(gfx,line,y);
	}

	protected void paintLineHighlight(Graphics gfx, int line, int y)
	{
		FontMetrics fm = getFontMetrics();
		int height = fm.getHeight();
		y += fm.getLeading() + fm.getMaxDescent();

		int selectionStart = textArea.getSelectionStart();
		int selectionEnd = textArea.getSelectionEnd();

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

			gfx.fillRect(x1,y,x2 - x1,height);
		}

	}

	protected void paintBracketHighlight(Graphics gfx, int line, int y)
	{
		int position = textArea.getBracketPosition();
		if(position == -1)
			return;
		FontMetrics fm = getFontMetrics();
		y += fm.getLeading() + fm.getMaxDescent();
		int x = textArea.offsetToX(line,position);
		gfx.setColor(bracketHighlightColor);
		gfx.drawRect(x,y,fm.charWidth(textArea.getBracketCharacter()) - 1,
			fm.getHeight() - 1);
	}

	protected void paintCaret(Graphics gfx, int line, int y)
	{
		if(textArea.isCaretVisible())
		{
			int offset = textArea.getCaretPosition() 
				- textArea.getLineStartOffset(line);
			int caretX = textArea.offsetToX(line,offset);
			int caretWidth = ((blockCaret ||
				textArea.isOverwriteEnabled()) ?
				fm.charWidth('w') : 1);
			FontMetrics fm = getFontMetrics();
			y += fm.getLeading() + fm.getMaxDescent();
			int height = fm.getHeight();
			
			gfx.setColor(caretColor);

			if(textArea.isOverwriteEnabled())
			{
				gfx.fillRect(caretX,y + height - 1,
					caretWidth,1);
			}
			else
			{
				gfx.drawRect(caretX,y,caretWidth - 1,height - 1);
			}
		}
	}
}

/*
 * ChangeLog:
 * $Log$
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
