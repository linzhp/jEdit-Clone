/*
 * SyntaxStyle.java - A simple text style class
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
package org.gjt.sp.jedit.syntax;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;
import java.util.StringTokenizer;

/**
 * A simple text style class. It can specify the color, italic flag,
 * and bold flag of a run of text.
 * @author Slava Pestov
 * @version $Id$
 */
public class SyntaxStyle
{
	/**
	 * Creates a new SyntaxStyle.
	 * @param color The text color
	 * @param italics True if the text should be italics
	 * @param bold True if the text should be bold
	 */
	public SyntaxStyle(Color color, boolean italics, boolean bold)
	{
		this.color = color;
		this.italics = italics;
		this.bold = bold;
	}

	/**
	 * Returns the color specified in this style.
	 */
	public Color getColor()
	{
		return color;
	}

	/**
	 * Returns true if italics is enabled for this style.
	 */
	public boolean isItalics()
	{
		return italics;
	}

	/**
	 * Returns true if boldface is enabled for this style.
	 */
	public boolean isBold()
	{
		return bold;
	}

	/**
	 * Returns the specified font, but with the style's bold and
	 * italic flags applied.
	 */
	public Font getStyledFont(Font font)
	{
		if(font == lastFont)
			return lastStyledFont;
		lastFont = font;
		lastStyledFont = new Font(font.getFamily(),
			(bold ? Font.BOLD : 0)
			| (italics ? Font.ITALIC : 0),
			font.getSize());
		return lastStyledFont;
	}

	/**
	 * Sets the foreground color and font of the specified graphics
	 * context to that specified in this style.
	 * @param gfx The graphics context
	 * @param font The font to add the styles to
	 */
	public void setGraphicsFlags(Graphics gfx, Font font)
	{
		Font _font = getStyledFont(font);
		if(gfx.getFont() != _font)
			gfx.setFont(_font);
		if(gfx.getColor() != color)
			gfx.setColor(color);
	}

	// private members
	private Color color;
	private boolean italics;
	private boolean bold;
	private Font lastFont;
	private Font lastStyledFont;
}
