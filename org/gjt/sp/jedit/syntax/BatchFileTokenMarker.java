/*
 * BatchFileTokenMarker.java - Batch file token marker
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

public class BatchFileTokenMarker extends TokenMarker
{
	// public members
	public static final String COMMAND = "command";
	public static final String COMMENT = "comment";
	public static final String VARIABLE = "variable";
	public static final String QUOTE = "quote";

	public Token markTokens(Segment line, int lineIndex)
	{
		lastToken = null;
		String token = null;
		int offset = line.offset;
		int lastOffset = offset;
		int length = line.count + offset;
loop:		for(int i = line.offset; i < length; i++)
		{
			switch(line.array[i])
			{
			case '%':
				if(token == null)
				{
					addToken(i - lastOffset,null);
					lastOffset = i;
					if(length - i <= 3 || line.array[i+2]
					   == ' ')
					{
						addToken(2,VARIABLE);
						i++;
						lastOffset = i + 1;
					}
					else
						token = VARIABLE;
				}
				else if(token == VARIABLE)
				{
					token = null;
					addToken((i+1) - lastOffset,VARIABLE);
					lastOffset = i + 1;
				}
				break;
			case '"':
				if(token == null)
				{
					token = QUOTE;
					addToken(i - lastOffset,null);
					lastOffset = i;
				}
				else if(token == QUOTE)
				{
					token = null;
					addToken((i+1) - lastOffset,QUOTE);
					lastOffset = i + 1;
				}
				break;
			case '=':
				if(token == null && lastOffset == offset)
				{
					addToken(i - lastOffset,null);
					lastOffset = i;
				}
				break;
			case ' ':
				if(lastOffset == offset)
				{
					if(jEdit.regionMatches(true,line,
							       i - 3,"rem"))
					{
						addToken(length - lastOffset,
							 COMMENT);
						lastOffset = length;
						break loop;
					}
					else if(token == null)
					{
						addToken(i - lastOffset,
							 COMMAND);
						lastOffset = i;
					}
				}
				break;
			}
		}
		if(lastOffset != length)
			addToken(length - lastOffset,lastOffset == offset ?
				COMMAND : token);
		if(lastToken != null)
		{
			lastToken.nextValid = false;
			return firstToken;
		}
		else
			return null;
	}
}
