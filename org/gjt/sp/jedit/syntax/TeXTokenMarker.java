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

import javax.swing.text.Segment;
import org.gjt.sp.jedit.jEdit;

/**
 * TeX token marker.
 *
 * @author Slava Pestov
 * @version $Id$
 */
public class TeXTokenMarker extends TokenMarker
{
	// public members
	public static final String BDFORMULA = "bdformula";
	public static final String EDFORMULA = "edformula";
	
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
			char c = line.array[i];
			// if a backslash is followed immediately
			// by a non-alpha character, the command at
			// the non-alpha char. If we have a backslash,
			// some text, and then a non-alpha char,
			// the command ends before the non-alpha char.
			if(Character.isLetter(c))
			{
				backslash = false;
			}
			else
			{
				if(backslash)
				{
					// \<non alpha>
					// we skip over this character,
					// hence the `continue'
					backslash = false;
					if(token == Token.KEYWORD2 || token == EDFORMULA)
						token = Token.KEYWORD2;
					addToken((i+1) - lastOffset,token);
					lastOffset = i+1;
					if(token == Token.KEYWORD1)
						token = null;
					continue;
				}
				else
				{
					//\blah<non alpha>
					// we leave the character in
					// the stream, and it's not
					// part of the command token
					if(token == BDFORMULA || token == EDFORMULA)
						token = Token.KEYWORD2;
					addToken(i - lastOffset,token);
					if(token == Token.KEYWORD1)
						token = null;
					lastOffset = i;
				}
			}
			switch(c)
			{
			case '%':
				if(backslash)
				{
					backslash = false;
					break;
				}
				addToken(i - lastOffset,token);
				addToken(length - i,Token.COMMENT1);
				lastOffset = length;
				break loop;
			case '\\':
				backslash = true;
				if(token == null)
				{
					token = Token.KEYWORD1;
					addToken(i - lastOffset,null);
					lastOffset = i;
				}
				break;
			case '$':
				backslash = false;
				if(token == null) // singe $
				{
					token = Token.KEYWORD2;
					addToken(i - lastOffset,null);
					lastOffset = i;
				}
				else if(token == Token.KEYWORD1) // \...$
				{
					token = Token.KEYWORD2;
					addToken(i - lastOffset,Token.KEYWORD1);
					lastOffset = i;
				}
				else if(token == Token.KEYWORD2) // $$aaa
				{
					if(i - lastOffset == 1 && line.array[i-1] == '$')
					{
						token = BDFORMULA;
						break;
					}
					token = null;
					addToken((i+1) - lastOffset,Token.KEYWORD2);
					lastOffset = i + 1;
				}
				else if(token == BDFORMULA) // $$aaa$
				{
					token = EDFORMULA;
				}
				else if(token == EDFORMULA) // $$aaa$$
				{
					token = null;
					addToken((i+1) - lastOffset,Token.KEYWORD2);
					lastOffset = i + 1;
				}
				break;
			}
		}
		if(lastOffset != length)
			addToken(length - lastOffset,token == BDFORMULA
				|| token == EDFORMULA ? Token.KEYWORD2 :
				token);
		lineInfo[lineIndex] = (token != Token.KEYWORD1 ? token : null);
		if(lastToken != null)
		{
			lastToken.nextValid = false;
			return firstToken;
		}
		else
			return null;
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.9  1999/03/12 23:51:00  sp
 * Console updates, uncomment removed cos it's too buggy, cvs log tags added
 *
 */
