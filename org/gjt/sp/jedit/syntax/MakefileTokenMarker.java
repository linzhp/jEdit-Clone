/*
 * MakefileTokenMarker.java - Makefile token marker
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

public class MakefileTokenMarker extends TokenMarker
{
	// public members
	public static final String MAKE_CMD = "make_cmd";
	public static final String COMMENT = "comment";
	public static final String VARIABLE = "variable";
	public static final String DQUOTE = "dquote";
	public static final String SQUOTE = "squote";

	public Token markTokens(Segment line, int lineIndex)
	{
		ensureCapacity(lineIndex);
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
					addToken((i+1) - lastOffset,MAKE_CMD);
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
					addToken(length - i,COMMENT);
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
							token = VARIABLE;
						else
						{
							addToken(2,VARIABLE);
							lastOffset += 2;
							i++;
						}
					}
				}
				break;
			case ')': case '}':
				backslash = false;
				if(token == VARIABLE)
				{
					token = null;
					addToken((i+1) - lastOffset,VARIABLE);
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
					token = DQUOTE;
					addToken(i - lastOffset,null);
					lastOffset = i;
				}
				else if(token == DQUOTE)
				{
					token = null;
					addToken((i+1) - lastOffset,DQUOTE);
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
					token = SQUOTE;
					addToken(i - lastOffset,null);
					lastOffset = i;
				}
				else if(token == SQUOTE)
				{
					token = null;
					addToken((i+1) - lastOffset,SQUOTE);
					lastOffset = i + 1;
				}
				break;
			default:
				backslash = false;
				break;
			}
		}
		if(lastOffset != length)
			addToken(length - lastOffset,lastOffset == offset ?
				 MAKE_CMD : token);
		lineInfo[lineIndex] = (token == DQUOTE || token == SQUOTE ?
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
