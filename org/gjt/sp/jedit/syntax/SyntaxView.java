/*
 * SyntaxView.java - jEdit's own Swing view implementation
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

import javax.swing.text.*;
import java.awt.*;
import java.util.*;

/**
 * A Swing view implementation that colorizes lines of a
 * <code>SyntaxDocument</code> using a <code>TokenMarker</code>.<p>
 *
 * This class should not be used directly; a <code>SyntaxEditorKit</code>
 * should be used instead.
 *
 * @author Slava Pestov
 * @version $Id$
 */
public class SyntaxView extends PlainView
{
	/**
	 * Creates a new <code>SyntaxView</code> for painting the specified
	 * element.
	 * @param elem The element
	 */
	public SyntaxView(Element elem)
	{
		super(elem);
		line = new Segment();
		newRect = new Rectangle();
	}
	
	/**
	 * Paints the specified line.
	 * <p>
	 * This method performs the following:
	 * <ul>
	 * <li>Gets the token marker and color table from the current document,
	 * typecast to a <code>SyntaxDocument</code>.
	 * <li>Tokenizes the required line by calling the
	 * <code>markTokens()</code> method of the token marker.
	 * <li>Paints each token, obtaining the color by looking up the
	 * the <code>Token.id</code> value in the color table.
	 * </ul>
	 * If either the document doesn't implement
	 * <code>SyntaxDocument</code>, or if the returned token marker is
	 * null, the line will be painted with no colorization.
	 *
	 * @param lineIndex The line number
	 * @param g The graphics context
	 * @param x The x co-ordinate where the line should be painted
	 * @param y The y co-ordinate where the line should be painted
	 */
	public void drawLine(int lineIndex, Graphics g, int x, int y)
	{
		SyntaxDocument syntaxDocument;
		TokenMarker tokenMarker;

		Document document = getDocument();

		if(document instanceof SyntaxDocument)
		{
			syntaxDocument = (SyntaxDocument)document;
			tokenMarker = syntaxDocument.getTokenMarker();
		}
		else
		{
			syntaxDocument = null;
			tokenMarker = null;
		}

		FontMetrics metrics = g.getFontMetrics();
		Color def = getDefaultColor();

		try
		{
			Element lineElement = getElement()
				.getElement(lineIndex);
			int start = lineElement.getStartOffset();
			int end = lineElement.getEndOffset();

			document.getText(start,end - (start + 1),line);

			if(tokenMarker == null)
			{
				g.setColor(def);
				Utilities.drawTabbedText(line,x,y,g,this,0);
			}
			else
			{
				paintSyntaxLine(line,lineIndex,x,y,g,
					syntaxDocument,tokenMarker,def);

				if(tokenMarker.isNextLineRequested())
					forceRepaint(g,x,y);
			}
		}
		catch(BadLocationException bl)
		{
			// shouldn't happen
			bl.printStackTrace();
		}
	}

	// protected members
	protected Color getDefaultColor()
	{
		return getContainer().getForeground();
	}

	// private members
	private Segment line;
	private Rectangle newRect;

	private void paintSyntaxLine(Segment line, int lineIndex, int x, int y,
		Graphics g, SyntaxDocument syntaxDocument,
		TokenMarker tokenMarker, Color def)
	{
		Color[] colors = syntaxDocument.getColors();
		Token tokens = tokenMarker.markTokens(line,lineIndex);
		int offset = 0;
		for(;;)
		{
			byte id = tokens.id;
			if(id == Token.END)
				break;

			int length = tokens.length;
			Color color;
			if(id == Token.NULL)
				color = def;
			else
				color = colors[id];
			g.setColor(color == null ? def : color);

			line.count = length;
			x = Utilities.drawTabbedText(line,x,y,g,this,offset);
			line.offset += length;
			offset += length;

			tokens = tokens.next;
		}
	}

	/** Stupid hack that repaints from y to the end of the text component */
	private void forceRepaint(Graphics g, int x, int y)
	{
		Component host = getContainer();
		Dimension size = host.getSize();

		System.out.println("Repaint forced");

		host.repaint(x,y,size.width - x,size.height - y);
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.19  1999/05/01 00:55:11  sp
 * Option pane updates (new, easier API), syntax colorizing updates
 *
 * Revision 1.18  1999/04/30 23:20:38  sp
 * Improved colorization of multiline tokens
 *
 * Revision 1.17  1999/04/19 05:38:20  sp
 * Syntax API changes
 *
 * Revision 1.16  1999/03/13 08:50:39  sp
 * Syntax colorizing updates and cleanups, general code reorganizations
 *
 * Revision 1.15  1999/03/12 23:51:00  sp
 * Console updates, uncomment removed cos it's too buggy, cvs log tags added
 *
 */
