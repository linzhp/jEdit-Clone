/*
 * ShellScriptTokenMarker.java - Shell script token marker
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

public class ShellScriptTokenMarker extends TokenMarker
{
	// public members
	public static final String COMMAND = "command";
	public static final String COMMENT = "comment";
	public static final String VARIABLE = "variable";
	public static final String LVARIABLE = "lvariable";
	public static final String DQUOTE = "dquote";
	public static final String SQUOTE = "squote";

	public Token markTokens(Segment line, int lineIndex)
	{
		ensureCapacity(lineIndex);
		lastToken = null;
		String token = lineIndex == 0 ? null : lineInfo[lineIndex - 1];
		byte cmdState = 0; // 0 = space before command, 1 = inside
				// command, 2 = after command
		int offset = line.offset;
		int lastOffset = offset;
		int length = line.count + offset;
		boolean backslash = false;
loop:		for(int i = offset; i < length; i++)
		{
			char c = line.array[i];
			if(token == VARIABLE)
			{
				backslash = false;
				if(!Character.isLetterOrDigit(c) && c != '_')
				{
					token = null;
					if(i != offset && line.array[i-1] == '$')
					{
						addToken((i+1) - lastOffset,
							VARIABLE);
						lastOffset = i + 1;
						continue;
					}
					else
					{
						addToken(i - lastOffset,
							VARIABLE);
						lastOffset = i;
					}
				}
			}
			else if(token == LVARIABLE && c == '}')
			{
				backslash = false;
				token = null;
				addToken((i+1) - lastOffset,VARIABLE);
				lastOffset = i + 1;
			}
			switch(c)
			{
			case '\\':
				backslash = !backslash;
				break;
			case ' ': case '\t': case '(': case ')':
				backslash = false;
				if(token == null && cmdState == 1/*insideCmd*/)
				{
					addToken(i - lastOffset,COMMAND);
					lastOffset = i;
					cmdState = 2; /*afterCmd*/
				}
				break;
			case '=':
				backslash = false;
				if(token == null && cmdState == 1/*insideCmd*/)
				{
					addToken(i - lastOffset,null);
					lastOffset = i;
					cmdState = 2; /*afterCmd*/
				}
				break;
			case '&': case '|': case ';':
				if(backslash)
					backslash = false;
				else
					cmdState = 0; /*beforeCmd*/
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
				else if(token == null)
				{
					if(length - i >= 2)
					{
						switch(line.array[i+1])
						{
						case '(':
							continue;
						case '{':
							token = LVARIABLE;
							break;
						default:
							token = VARIABLE;
							break;
						}
					}
					else
						token = VARIABLE;
					addToken(i - lastOffset,null);
					cmdState = 2; /*afterCmd*/
					lastOffset = i;
				}
				break;
			case '"':
				if(backslash)
					backslash = false;
				else if(token == null)
				{
					token = DQUOTE;
					addToken(i - lastOffset,null);
					cmdState = 2; /*afterCmd*/
					lastOffset = i;
				}
				else if(token == DQUOTE)
				{
					token = null;
					addToken((i+1) - lastOffset,DQUOTE);
					cmdState = 2; /*afterCmd*/
					lastOffset = i + 1;
				}
				break;
			case '\'':
				if(backslash)
					backslash = false;
				else if(token == null)
				{
					token = SQUOTE;
					addToken(i - lastOffset,null);
					cmdState = 2; /*afterCmd*/
					lastOffset = i;
				}
				else if(token == SQUOTE)
				{
					token = null;
					addToken((i+1) - lastOffset,SQUOTE);
					cmdState = 2; /*afterCmd*/
					lastOffset = i + 1;
				}
				break;
			default:
				backslash = false;
				if(Character.isLetter(c))
				{
					if(cmdState == 0 /*beforeCmd*/)
					{
						addToken(i - lastOffset,token);
						lastOffset = i;
						cmdState++; /*insideCmd*/
					}
				}
				break;
			}
		}
		if(lastOffset != length)
			addToken(length - lastOffset,token == null &&
				cmdState == 1 ? COMMAND : token);
		lineInfo[lineIndex] = (token == SQUOTE || token == DQUOTE
			? token : null);
		if(lastToken != null)
		{
			lastToken.nextValid = false;
			return firstToken;
		}
		else
			return null;
	}
}
