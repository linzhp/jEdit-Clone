/*
 * HTMLTokenMarker.java - HTML token marker
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

public class HTMLTokenMarker extends JSTokenMarker
{
	// public members
	public static final String COMMENT = "comment";
	public static final String TAG = "tag";
	public static final String ENTITY = "entity";

	public Enumeration markTokens(String line, int lineIndex)
	{
		ensureCapacity(lineIndex);
		String token = lineIndex == 0 ? null : lineInfo[lineIndex - 1];
		tokens.removeAllElements();
		int lastOffset = 0;
		int length = line.length();
		for(int i = 0; i < length; i++)
		{
			char c = line.charAt(i);
			switch(c)
			{
			case '<':
				// check if it's a comment
				if(length - i > 3)
				{
					if(line.regionMatches(i,"<!--",0,3))
					{
						token = COMMENT;
						tokens.addElement(new JSToken(
							line.substring(
							lastOffset,i),null));
						lastOffset = i;
						break;
					}
				}
				if(token == null)
				{
					token = TAG;
					tokens.addElement(new JSToken(line
						.substring(lastOffset,i),
							null));
					lastOffset = i;
				}
				break;
			case '>':
				if(token == TAG)
				{
					token = null;
					tokens.addElement(new JSToken(line
						.substring(lastOffset,i + 1),
						TAG));
					lastOffset = i + 1;
				}
				else if(token == COMMENT)
				{
					if(i >= 2 && line.regionMatches(i - 2,
						"-->",0,3))
					{
						token = null;
						tokens.addElement(new JSToken(
							line.substring(lastOffset,i + 1),
							COMMENT));
						lastOffset = i + 1;
					}
				}
				break;
			case '&':
				if(token == null)
				{
					token = ENTITY;
					tokens.addElement(new JSToken(line
						.substring(lastOffset,i),
							 null));
					lastOffset = i;
				}
				break;
			case ';':
				if(token == ENTITY)
				{
					token = null;
					tokens.addElement(new JSToken(line
						.substring(lastOffset,i + 1),
						ENTITY));
					lastOffset = i + 1;
				}
				break;
			}
		}
		if(lastOffset != length)
			tokens.addElement(new JSToken(line.substring(
				lastOffset,length),token));
		lineInfo[lineIndex] = token;
		lastLine = lineIndex;
		return tokens.elements();
	}
}
