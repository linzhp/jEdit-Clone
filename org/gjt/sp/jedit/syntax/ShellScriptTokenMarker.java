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

import java.util.*;
import jstyle.*;

public class ShellScriptTokenMarker extends JSTokenMarker
{
	// public members
	public static final String COMMAND = "command";
	public static final String COMMENT = "comment";
	public static final String VARIABLE = "variable";
	public static final String LVARIABLE = "lvariable";
	public static final String DQUOTE = "dquote";
	public static final String SQUOTE = "squote";

	public Enumeration markTokens(String line, int lineIndex)
	{
		ensureCapacity(lineIndex);
		String token = lineIndex == 0 ? null : lineInfo[lineIndex - 1];
		tokens.removeAllElements();
		int lastOffset = 0;
		int length = line.length();
loop:		for(int i = 0; i < length; i++)
		{
			char c = line.charAt(i);
			if(token == VARIABLE)
			{
				if(!Character.isLetterOrDigit(c) && c != '_')
				{
					token = null;
					tokens.addElement(new JSToken(line
						.substring(lastOffset,i),
						VARIABLE));
					lastOffset = i;
				}
			}
			else if(token == LVARIABLE && c == '}')
			{
				token = null;
				tokens.addElement(new JSToken(line
						.substring(lastOffset,i + 1),
						VARIABLE));
				lastOffset = i + 1;
			}
			switch(c)
			{
			case ' ': case '\t': case '=': case '(': case ')':
				if(token == null && lastOffset == 0)
				{
					tokens.addElement(new JSToken(line
						.substring(lastOffset,i),
						COMMAND));
					lastOffset = i;
				}
				break;
			case '#':
				if(token == null && (i == 0 ||
					line.charAt(i - 1) != '\\'))
				{
					tokens.addElement(new JSToken(line
						.substring(lastOffset,i),
						null));
					tokens.addElement(new JSToken(line
						.substring(i,length),
						COMMENT));
					lastOffset = length;
					break loop;
				}
				break;
			case '$':
				if(token == null)
				{
					if(i != length - 2)
					{
						switch(line.charAt(i+1))
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
					tokens.addElement(new JSToken(line
						.substring(lastOffset,i),
						null));
					lastOffset = i;
				}
				break;
			case '"':
				if(i != 0 && line.charAt(i - 1) == '\\')
					break;
				if(token == null)
				{
					token = DQUOTE;
					tokens.addElement(new JSToken(line
						.substring(lastOffset,i),
						null));
					lastOffset = i;
				}
				else if(token == DQUOTE)
				{
					token = null;
					tokens.addElement(new JSToken(line
						.substring(lastOffset,i + 1),
						DQUOTE));
					lastOffset = i + 1;
				}
				break;
			case '\'':
				if(i != 0 && line.charAt(i - 1) == '\\')
					break;
				if(token == null)
				{
					token = SQUOTE;
					tokens.addElement(new JSToken(line
						.substring(lastOffset,i),
						null));
					lastOffset = i;
				}
				else if(token == SQUOTE)
				{
					token = null;
					tokens.addElement(new JSToken(line
						.substring(lastOffset,i + 1),
						SQUOTE));
					lastOffset = i + 1;
				}
				break;
			}
		}
		if(lastOffset != length)
		{
			tokens.addElement(new JSToken(line.substring(
				lastOffset,length),lastOffset == 0 && token == null?
				COMMAND : token));
		}
		lineInfo[lineIndex] = (token == SQUOTE || token == DQUOTE
			? token : null);
		lastLine = lineIndex;
		return tokens.elements();
	}
}
