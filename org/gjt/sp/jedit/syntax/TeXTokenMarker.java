/*
 * TeXTokenMarker.java - TeX/LaTeX/AMS-TeX token marker
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

public class TeXTokenMarker extends JSTokenMarker
{
	// public members
	public static final String COMMENT = "comment";
	public static final String COMMAND = "command";
	public static final String FORMULA = "formula";
	public static final String BDFORMULA = "bdformula";
	public static final String EDFORMULA = "edformula";

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
			if(token == COMMAND)
			{
				if(!Character.isLetter(c))
				{
					token = null;
					tokens.addElement(new JSToken(line
						.substring(lastOffset,i + 1),COMMAND));
					lastOffset = i + 1;
					continue;
				}
			}
			switch(c)
			{
			case '%':
				if(i != 0 && line.charAt(i - 1) == '\\')
					break;
				tokens.addElement(new JSToken(line.substring(
					lastOffset,i),token));
				tokens.addElement(new JSToken(line.substring(i),
					COMMENT));
				lastOffset = length;
				break loop;
			case '\\':
				if(token == null)
				{
					token = COMMAND;
					tokens.addElement(new JSToken(line
						.substring(lastOffset,i),null));
					lastOffset = i;
				}
				break;
			case '$':
				if(token == null) // singe $
				{
					token = FORMULA;
					tokens.addElement(new JSToken(line
						 .substring(lastOffset,i),null));
					lastOffset = i;
				}
				else if(token == COMMAND) // \...$
				{
					token = FORMULA;
					tokens.addElement(new JSToken(line
						 .substring(lastOffset,i),
						 COMMAND));
					lastOffset = i;
				}
				else if(token == FORMULA) // $$aaa
				{
					if(i != 0 && line.charAt(i - 1) == '$')
					{
						token = BDFORMULA;
						break;
					}
					token = null;
					tokens.addElement(new JSToken(line
						.substring(lastOffset,i + 1),
						FORMULA));
					lastOffset = i + 1;
				}
				else if(token == BDFORMULA) // $$aaa$
				{
					token = EDFORMULA;
				}
				else if(token == EDFORMULA) // $$aaa$$
				{
					token = null;
					tokens.addElement(new JSToken(line
						.substring(lastOffset,i + 1),
						FORMULA));
					lastOffset = i + 1;
				}
				break;
			}
		}
		if(lastOffset != length)
			tokens.addElement(new JSToken(line.substring(
				lastOffset,length),token));
		lineInfo[lineIndex] = (token == FORMULA || token == BDFORMULA
			|| token == EDFORMULA) ? token : null;
		lastLine = lineIndex;
		return tokens.elements();
	}
}
