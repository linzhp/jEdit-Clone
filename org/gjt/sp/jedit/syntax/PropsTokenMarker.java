/*
 * PropsTokenMarker.java - Java props/DOS INI token marker
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

import javax.swing.text.Segment;
import org.gjt.sp.jedit.mode.javascript;
import org.gjt.sp.jedit.jEdit;

public class PropsTokenMarker extends TokenMarker
{
	public Token markTokens(Segment line, int lineIndex)
	{
		lastToken = null;
		String token = null;
		int offset = line.offset;
		int lastOffset = offset;
		int length = line.count + offset;
loop:		for(int i = offset; i < length; i++)
		{
			switch(line.array[i])
			{
			case '#': case ';':
				if(i == offset && token == null)
				{
					addToken(line.count,Token.COMMENT1);
					lastOffset = length;
					break loop;
				}
				break;
			case '[':
				if(i == offset && token == null)
				{
					token = Token.KEYWORD2;
					addToken(i - lastOffset,null);
					lastOffset = i;
				}
				break;
			case ']':
				if(token == Token.KEYWORD2)
				{
					token = null;
					addToken((i+1) - lastOffset,
						Token.KEYWORD2);
					lastOffset = (i+1);
				}
				break;
			case '=':
				if(token == null)
				{
					token = Token.ALTTXT; // Can't have [...] after =
					addToken(i - lastOffset,Token.KEYWORD1);
					lastOffset = i;
				}
				break;
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
