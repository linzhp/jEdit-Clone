/*
 * HelpTokenMarker.java - jEdit help file token marker
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

import javax.swing.text.Segment;
import org.gjt.sp.jedit.jEdit;

public class HelpTokenMarker extends TokenMarker
{
	// public members
	public static final String HEADING = "heading";
	public static final String LINK = "link";

	public Token markTokens(Segment line, int lineIndex)
	{
		lastToken = null;
		String token = null;
		int offset = line.offset;
		int lastOffset = offset;
		int length = line.count + offset;
		if(line.array[offset] == '*' || line.array[offset] == '=')
		{
			addToken(length - offset,HEADING);
			lastToken.nextValid = false;
			return firstToken;
		}
loop:		for(int i = line.offset; i < length; i++)
		{
			if(line.array[i] == '|')
			{
				if(token == null)
				{
					token = LINK;
					addToken(i - lastOffset,null);
					lastOffset = i;
				}
				else if(token == LINK)
				{
					token = null;
					addToken((i+1) - lastOffset,LINK);
					lastOffset = i + 1;
				}
			}
		}
		if(lastOffset != length)
			addToken(length - lastOffset,null);
		if(lastToken != null)
		{
			lastToken.nextValid = false;
			return firstToken;
		}
		else
			return null;
	}
}
