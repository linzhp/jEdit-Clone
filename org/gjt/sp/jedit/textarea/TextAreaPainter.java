/*
 * TextAreaPainter.java - Performs double buffering and paints the text area
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
	public TextAreaPainter(JEditTextArea textArea, SyntaxStyle[] styles,
		int cols, int rows)
	{
		this.textArea = textArea;
		this.styles = styles;
		this.cols = cols;
		this.rows = rows;
		currentLine = new Segment();

		firstInvalid = lastInvalid = -1;

		setFont(new Font("Monospaced",Font.PLAIN,14));
		setForeground(Color.black);
		setBackground(Color.white);
	}

	public SyntaxStyle[] getStyles()
	{
		return styles;
	}

	public void setStyles(SyntaxStyle[] styles)
	{
		this.styles = styles;
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

	public void invalidateLineRange(int firstLine, int lastLine)
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
		repaint();
	}

	public void scrollRepaint(int oldFirstLine, int newFirstLine)
	{
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
		int ntabs = ((int)x) / tabSize;
		return (ntabs + 1) * tabSize;
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

	// protected members
	protected JEditTextArea textArea;
	protected SyntaxStyle[] styles;
	protected int cols;
	protected int rows;

	protected Segment currentLine;
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

		int linesPainted = firstLine;
		while(linesPainted <= lastLine)
		{
			linesPainted += paintLine(model,offGfx,linesPainted,x,y);
			y += model.getLineHeight();
		}

		return linesPainted - firstLine;
	}

	protected int paintLine(TextAreaModel model, Graphics gfx, int lineIndex,
		int x, int y)
	{
		TokenMarker tokenMarker = model.getDocument().getTokenMarker();
		Font defaultFont = getFont();
		Color defaultColor = getForeground();

		/* Clear the line's bounding rectangle */
		FontMetrics fm = getFontMetrics();
		int gap = fm.getMaxDescent() + fm.getLeading();
		gfx.setColor(getBackground());
		gfx.fillRect(0,y + gap,offImg.getWidth(this),fm.getHeight());
				
		gfx.setFont(defaultFont);
		gfx.setColor(defaultColor);

		if(lineIndex >= model.getLineCount())
		{
			gfx.drawString("~",0,y + model.getLineHeight());
			return 1;
		}

		if(tokenMarker == null)
		{
			paintPlainLine(model,gfx,defaultFont,defaultColor,
				lineIndex,x,y + model.getLineHeight());
			return 1;
		}
		else
		{
			int count = 0;
			do
			{
				count++;
				y += model.getLineHeight();
			}
			while(paintSyntaxLine(model,gfx,tokenMarker,defaultFont,
				defaultColor,lineIndex++,x,y));
			return count;
		}
	}

	protected void paintPlainLine(TextAreaModel model, Graphics gfx,
		Font defaultFont, Color defaultColor, int lineIndex, int x, int y)
	{
		model.getLineText(lineIndex,currentLine);
		textArea.checkLongestLine(lineIndex,Utilities.drawTabbedText(
			currentLine,x,y,gfx,this,0));
	}

	protected boolean paintSyntaxLine(TextAreaModel model, Graphics gfx,
		TokenMarker tokenMarker, Font defaultFont, Color defaultColor,
		int lineIndex, int x, int y)
	{
		model.getLineText(lineIndex,currentLine);
		Token tokens = tokenMarker.markTokens(currentLine,lineIndex);
		int offset = 0;
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
			x = Utilities.drawTabbedText(currentLine,x,y,gfx,this,offset);
			currentLine.offset += length;
			offset += length;

			tokens = tokens.next;
		}

		textArea.checkLongestLine(lineIndex,x);

		return tokenMarker.isNextLineRequested();
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.4  1999/06/25 06:54:08  sp
 * Text area updates
 *
 */
