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

import java.util.*;
import jstyle.*;

public class BatchFileTokenMarker extends JSTokenMarker
{
	// public members
	public static final String COMMAND = "command";
	public static final String COMMENT = "comment";
	public static final String VARIABLE = "variable";
	public static final String QUOTE = "quote";

	public Enumeration markTokens(String line, int lineIndex)
	{
		String token = null;
		tokens.removeAllElements();
		int lastOffset = 0;
		int length = line.length();
loop:		for(int i = 0; i < length; i++)
		{
			char c = line.charAt(i);
			switch(c)
			{
			case '%':
				if(token == null)
				{
					tokens.addElement(new JSToken(line
						.substring(lastOffset,i),null));
					lastOffset = i;
					if(length - i <= 3 || line.charAt(i+2)
					   == ' ')
					{
						tokens.addElement(new JSToken(
							line.substring(i,i+2),
							VARIABLE));
						i++;
						lastOffset = i + 1;
					}
					else
						token = VARIABLE;
				}
				else if(token == VARIABLE)
				{
					token = null;
					tokens.addElement(new JSToken(line
						.substring(lastOffset,i + 1),
						VARIABLE));
					lastOffset = i + 1;
				}
				break;
			case '"':
				if(token == null)
				{
					token = QUOTE;
					tokens.addElement(new JSToken(line
						.substring(lastOffset,i),
						null));
					lastOffset = i;
				}
				else if(token == QUOTE)
				{
					token = null;
					tokens.addElement(new JSToken(line
						.substring(lastOffset,i + 1),
						QUOTE));
					lastOffset = i + 1;
				}
				break;
			case '=':
				if(token == null && lastOffset == 0)
				{
					tokens.addElement(new JSToken(line
						.substring(0,i),null));
					lastOffset = i;
				}
				break;
			case ' ':
				if(lastOffset == 0)
				{
					if(line.regionMatches(true,i - 3,"rem",
							      0,3))
					{
						tokens.addElement(new JSToken(
							line,COMMENT));
						lastOffset = length;
						break loop;
					}
					else if(token == null)
					{
						tokens.addElement(new JSToken(
							line.substring(
							lastOffset,i),
							COMMAND));
						lastOffset = i;
					}
				}
				break;
			}
		}
		if(lastOffset != length)
		{
			tokens.addElement(new JSToken(line.substring(
				lastOffset,length),lastOffset == 0 ?
				COMMAND : token));
		}
		return tokens.elements();
	}

	public boolean isNextLineRequested()
	{
		return false;
	}
}
