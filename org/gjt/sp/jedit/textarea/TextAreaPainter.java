/*
 * TextAreaPainter.java - Performs double buffering and paints the text area
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

		setFont(new Font("Monospaced",Font.PLAIN,14));
		setForeground(Color.black);
		setBackground(Color.white);
	}

	public FontMetrics getFontMetrics()
	{
		return getToolkit().getFontMetrics(getFont());
	}

	public void update(Graphics g)
	{
		ensureOffscreenValid();
		g.drawImage(offImg,0,0,null);
	}

	public void paint(Graphics g)
	{
		update(g);
	}

	public void offscreenRepaint()
	{
		if(offGfx == null)
			return;
		Rectangle bounds = getBounds();
		offGfx.clearRect(bounds.x,bounds.y,bounds.width,bounds.height);

		TextAreaModel model = textArea.getModel();

		int firstLine = textArea.getFirstLine();
		int lastLine = model.yToLine(bounds.height);

		offscreenRepaintLineRange(model,firstLine,lastLine);
	}

	public void offscreenRepaintLineRange(TextAreaModel model, int firstLine,
		int lastLine)
	{
		if(offGfx == null)
			return;

		FontMetrics fm = getFontMetrics();
		tabSize = fm.charWidth('w') * ((Integer)model.getDocument()
			.getProperty(PlainDocument.tabSizeAttribute))
			.intValue();

		int y = model.lineToY(firstLine);
		for(int i = firstLine; i < lastLine;)
		{
			i += offscreenRepaintLine(model,i,0,y);
			y += model.getLineHeight();
		}
	}

	public int offscreenRepaintLine(TextAreaModel model, int lineIndex,
		int x, int y)
	{
		SyntaxDocument document = model.getDocument();
		TokenMarker tokenMarker = document.getTokenMarker();
		Font defaultFont = getFont();
		Color defaultColor = getForeground();

		offGfx.setFont(defaultFont);
		offGfx.setColor(defaultColor);

		try
		{
			if(tokenMarker == null)
			{
				paintPlainLine(model,document,defaultFont,
					defaultColor,lineIndex,0,y);
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
				while(paintSyntaxLine(model,document,tokenMarker,
					defaultFont,defaultColor,lineIndex++,x,y));
				return count;
			}
		}
		catch(BadLocationException bl)
		{
			// shouldn't happen
			bl.printStackTrace();
		}
		return 1;
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

	protected void ensureOffscreenValid()
	{
		Dimension dim = getSize();
		if(offImg == null || offGfx == null
			|| offImg.getWidth(null) != dim.width
			|| offImg.getHeight(null) != dim.height)
		{
			offImg = textArea.createImage(dim.width,dim.height);
			offGfx = offImg.getGraphics();
			offscreenRepaint();
		}
	}

	protected void paintPlainLine(TextAreaModel model, SyntaxDocument document,
		Font defaultFont, Color defaultColor, int lineIndex, int x, int y)
		throws BadLocationException
	{
		Element lineElement = document.getDefaultRootElement()
			.getElement(lineIndex);
		int start = lineElement.getStartOffset();
		int end = lineElement.getEndOffset();

		document.getText(start,end - (start + 1),currentLine);

		Utilities.drawTabbedText(currentLine,x,y,offGfx,this,0);
	}

	protected boolean paintSyntaxLine(TextAreaModel model, SyntaxDocument document,
		TokenMarker tokenMarker, Font defaultFont, Color defaultColor,
		int lineIndex, int x, int y)
		throws BadLocationException
	{
		Element lineElement = document.getDefaultRootElement()
			.getElement(lineIndex);
		int start = lineElement.getStartOffset();
		int end = lineElement.getEndOffset();

		document.getText(start,end - (start + 1),currentLine);
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
				if(!defaultColor.equals(offGfx.getColor()))
					offGfx.setColor(defaultColor);
				if(!defaultFont.equals(offGfx.getFont()))
					offGfx.setFont(defaultFont);
			}
			else
				styles[id].setGraphicsFlags(offGfx,defaultFont);

			currentLine.count = length;
			x = Utilities.drawTabbedText(currentLine,x,y,offGfx,this,offset);
			currentLine.offset += length;
			offset += length;

			tokens = tokens.next;
		}

		return tokenMarker.isNextLineRequested();
	}
}
