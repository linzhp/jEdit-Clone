/*
 * SyntaxView.java - jEdit's own Swing view implementation
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

import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.Buffer;

public class SyntaxView extends PlainView
{
	// public methods
	public SyntaxView(Element elem)
	{
		super(elem);
		line = new Segment();
	}
	
	public void drawLine(int lineIndex, Graphics g, int x, int y)
	{
		Buffer buffer = (Buffer)getDocument();
		FontMetrics metrics = g.getFontMetrics();
		Hashtable colors = buffer.getColors();
		TokenMarker tokenMarker = buffer.getTokenMarker();
		try
		{
			Element lineElement = getElement().getElement(
				lineIndex);
			int start = lineElement.getStartOffset();
			buffer.getText(start,lineElement.getEndOffset()
				- (start + 1),line);
			if(tokenMarker == null)
			{
				g.setColor(Color.black);
				Utilities.drawTabbedText(line,x,y,g,this,0);
			}
			else
			{
				Token tokens = tokenMarker.markTokens(line,
					lineIndex);
				int offset = 0;
				for(; tokens != null; tokens = tokens.next)
				{
					int length = tokens.length;
					Color color;
					String id = tokens.id;
					if(id == null)
						color = Color.black;
					else
						color = (Color)colors.get(id);
					g.setColor(color == null ?
						   Color.black : color);
				   	line.count = length;
					x = Utilities.drawTabbedText(line,x,
						   y,g,this,offset);
					line.offset += length;
					offset += length;
					if(!tokens.nextValid)
						break;
				}
			}
		}
		catch(BadLocationException bl)
		{
			// shouldn't happen
			bl.printStackTrace();
		}
	}

	// private members
	private Segment line;
}
