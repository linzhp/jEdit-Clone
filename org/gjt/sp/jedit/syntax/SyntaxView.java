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

import com.sun.java.swing.text.*;
import java.awt.*;
import java.util.*;
import jstyle.*;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.Buffer;

public class SyntaxView extends PlainView
{
	// public methods
	public SyntaxView(Element elem)
	{
		super(elem);
	}
	
	public void drawLine(int lineIndex, Graphics g, int x, int y)
	{
		Buffer buffer = (Buffer)getDocument();
		FontMetrics metrics = g.getFontMetrics();
		Hashtable colors = buffer.getColors();
		JSTokenMarker tokenMarker = buffer.getTokenMarker();
		try
		{
			Element lineElement = getElement().getElement(
				lineIndex);
			int start = lineElement.getStartOffset();
			String line = jEdit.untab(getTabSize(),
				buffer.getText(start,lineElement.getEndOffset()
					       - start));
			if(tokenMarker == null)
			{
				Color color = (Color)colors.get("default");
				if(color == null)
					color = Color.black;
				g.setColor(color);
				g.drawString(line,x,y);
			}
			else
			{
				Enumeration enum = tokenMarker.markTokens(line,
					lineIndex,buffer.isNextLine(lineIndex));
				while(enum.hasMoreElements())
				{
					JSToken token = (JSToken)enum
						.nextElement();
					Object id = token.id;
					if(id == null)
						id = "default";
					String sequence = token.sequence;
					Color color = (Color)colors.get(id);
					g.setColor(color == null ?
						   Color.black : color);
					g.drawString(sequence,x,y);
					x += metrics.stringWidth(sequence);
				}
			}
		}
		catch(BadLocationException bl)
		{
			// shouldn't happen
			bl.printStackTrace();
		}
	}
}
