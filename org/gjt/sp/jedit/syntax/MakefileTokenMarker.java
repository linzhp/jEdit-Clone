/*
 * MakefileTokenMarker.java - Makefile token marker
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

public class MakefileTokenMarker extends TokenMarker
{
	// public members
	public Token markTokens(Segment line, int lineIndex)
	{
		lastToken = null;
		String token = lineIndex == 0 ? null : lineInfo[lineIndex - 1];
		int offset = line.offset;
		int lastOffset = offset;
		int length = line.count + offset;
		boolean backslash = false;
loop:		for(int i = offset; i < length; i++)
		{
			switch(line.array[i])
			{
			case '\\':
				backslash = !backslash;
				break;
			case ':': case '=': case ' ':
				backslash = false;
				if(token == null && lastOffset == offset)
				{
					addToken((i+1) - lastOffset,Token.KEYWORD1);
					lastOffset = i + 1;
				}
				break;
			case '\t':
				// silly hack
				backslash = false;
				if(token == null && lastOffset == offset)
				{
					addToken((i+1) - lastOffset,null);
					lastOffset = i + 1;
				}
				break;
			case '#':
				if(backslash)
					backslash = false;
				else if(token == null)
				{
					addToken(i - lastOffset,null);
					addToken(length - i,Token.COMMENT1);
					lastOffset = length;
					break loop;
				}
				break;
			case '$':
				if(backslash)
					backslash = false;
				else if(token == null && lastOffset != offset)
				{
					addToken(i - lastOffset,null);
					lastOffset = i;
					if(length - i > 1)
	 				{
						char c = line.array[i + 1];
				      		if(c == '(' || c == '{')
							token = Token.KEYWORD2;
						else
						{
							addToken(2,Token.KEYWORD2);
							lastOffset += 2;
							i++;
						}
					}
				}
				break;
			case ')': case '}':
				backslash = false;
				if(token == Token.KEYWORD2)
				{
					token = null;
					addToken((i+1) - lastOffset,Token.KEYWORD2);
					lastOffset = i + 1;
				}
				break;
			case '"':
				if(backslash)
				{
					backslash = false;
					break;
				}
				if(token == null)
				{
					token = Token.LITERAL1;
					addToken(i - lastOffset,null);
					lastOffset = i;
				}
				else if(token == Token.LITERAL1)
				{
					token = null;
					addToken((i+1) - lastOffset,Token.LITERAL1);
					lastOffset = i + 1;
				}
				break;
			case '\'':
				if(backslash)
				{
					backslash = false;
					break;
				}
				if(token == null)
				{
					token = Token.LITERAL2;
					addToken(i - lastOffset,null);
					lastOffset = i;
				}
				else if(token == Token.LITERAL2)
				{
					token = null;
					addToken((i+1) - lastOffset,Token.LITERAL2);
					lastOffset = i + 1;
				}
				break;
			default:
				backslash = false;
				break;
			}
		}
		if(lastOffset != length)
		{
			if(token != null && token != Token.KEYWORD2)
			{
				addToken(length - lastOffset,Token.INVALID);
				token = null;
			}
			else
			{
				addToken(length - lastOffset,lastOffset == offset ?
					 Token.KEYWORD1 : token);
			}
		}
		lineInfo[lineIndex] = (token == Token.LITERAL1 || token == Token.LITERAL2 ?
			token : null);
		if(lastToken != null)
		{
			lastToken.nextValid = false;
			return firstToken;
		}
		else
			return null;
	}
}
