/*
 * HTMLTokenMarker.java - HTML token marker
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

/**
 * HTML token marker.
 *
 * @author Slava Pestov
 * @version $Id$
 */
public class HTMLTokenMarker extends TokenMarker
{
	public static final byte JAVASCRIPT = Token.INTERNAL_FIRST;

	public HTMLTokenMarker()
	{
		keywords = JavaScriptTokenMarker.getKeywords();
	}

	public byte markTokensImpl(byte token, Segment line, int lineIndex)
	{
		boolean backslash = false;
		int offset = line.offset;
		int lastOffset = offset;
		int lastKeyword = offset;
		int length = line.count + offset;
loop:		for(int i = offset; i < length; i++)
		{
			switch(line.array[i])
			{
			case '\\':
				backslash = !backslash;
				break;
			case ';':
				if(token == Token.KEYWORD2)
				{
					token = Token.NULL;
					addToken((i+1) - lastOffset,Token.KEYWORD2);
					lastOffset = i + 1;
					break;
				}
			case '*':
				if(token == Token.COMMENT2 && length - i > 1)
				{
					if(length - i > 1 && line.array[i+1] == '/')
					{
						backslash = false;
						token = JAVASCRIPT;
						i++;
						addToken((i+1) - lastOffset,Token.COMMENT2);
						lastOffset = i + 1;
						break;
					}
				}
			case '.': case ',': case ' ': case '\t':
			case '(': case ')': case '[': case ']':
			case '{': case '}':
				backslash = false;
				if(token == JAVASCRIPT)
				{
					int len = i - lastKeyword;
					byte id = keywords.lookup(line,lastKeyword,len);
					if(id != Token.NULL)
					{
						if(lastKeyword != lastOffset)
							addToken(lastKeyword - lastOffset,
								Token.NULL);
						addToken(len,id);
						lastOffset = i;
					}
					lastKeyword = i + 1;
				}
				break;
			case '<':
				backslash = false;
				if(token == Token.NULL)
				{
					if(SyntaxUtilities.regionMatches(false,
						line,i,"<!--"))
						token = Token.COMMENT1;
					else
						token = Token.KEYWORD1;
					addToken(i - lastOffset,Token.NULL);
					lastOffset = i;
				}
				else if(token == JAVASCRIPT)
				{
					if(SyntaxUtilities.regionMatches(true,
						line,i,"</SCRIPT>"))
					{
						token = Token.KEYWORD1;
						addToken(i - lastOffset,Token.NULL);
						lastOffset = i;
					}
				}
				break;
			case '>':
				backslash = false;
				if(token == Token.KEYWORD1)
				{
					if(SyntaxUtilities.regionMatches(true,line,
						lastOffset,"<SCRIPT"))
						token = JAVASCRIPT;
					else
						token = Token.NULL;
					addToken((i+1) - lastOffset,Token.KEYWORD1);
					lastOffset = i + 1;
				}
				else if(token == Token.COMMENT1)
				{
					if(SyntaxUtilities.regionMatches(false,line,
						i - 2,"-->"))
					{
						token = Token.NULL;
						addToken((i+1) - lastOffset,
							 Token.COMMENT1);
						lastOffset = i + 1;
					}
				}
				break;
			case '&':
				backslash = false;
				if(token == Token.NULL)
				{
					token = Token.KEYWORD2;
					addToken(i - lastOffset,Token.NULL);
					lastOffset = i;
				}
				break;
			case ':':
				backslash = false;
				if(token == JAVASCRIPT && lastKeyword == offset)
				{
					addToken((i+1) - lastOffset,Token.LABEL);
					lastOffset = i + 1;
				}
				break;
			case '/':
				backslash = false;
				if(token == JAVASCRIPT && length - i > 1)
				{
					switch(line.array[i+1])
					{
					case '*':
						token = Token.COMMENT2;
						addToken(i - lastOffset,Token.NULL);
						lastOffset = i;
						i++;
						break;
					case '/':
						addToken(i - lastOffset,Token.NULL);
						addToken(length - i,Token.COMMENT2);
						lastOffset = length;
						break loop;
					}
				}
				break;
			case '"':
				if(backslash)
					backslash = false;
				else if(token == JAVASCRIPT)
				{
					token = Token.LITERAL1;
					addToken(i - lastOffset,Token.NULL);
					lastOffset = i;
				}
				else if(token == Token.LITERAL1)
				{
					token = JAVASCRIPT;
					addToken((i+1) - lastOffset,Token.LITERAL1);
					lastOffset = i + 1;
				}
				break;
			case '\'':
				if(backslash)
					backslash = false;
				else if(token == JAVASCRIPT)
				{
					token = Token.LITERAL2;
					addToken(i - lastOffset,Token.NULL);
					lastOffset = i;
				}
				else if(token == Token.LITERAL2)
				{
					token = JAVASCRIPT;
					addToken((i+1) - lastOffset,Token.LITERAL1);
					lastOffset = i + 1;
				}
				break;
			default:
				backslash = false;
				break;
			}
		}
		if(token == JAVASCRIPT)
		{
			int len = length - lastKeyword;
			byte id = keywords.lookup(line,lastKeyword,len);
			if(id != Token.NULL)
			{
				if(lastKeyword != lastOffset)
					addToken(lastKeyword - lastOffset,Token.NULL);
				addToken(len,id);
				lastOffset = length;
			}
		}
		if(lastOffset != length)
		{
			if(token == Token.LITERAL1 || token == Token.LITERAL2
				|| token == Token.KEYWORD2)
			{
				addToken(length - lastOffset,Token.INVALID);
				token = Token.NULL;
			}
			else if(token == JAVASCRIPT)
				addToken(length - lastOffset,Token.NULL);
			else
				addToken(length - lastOffset,token);
		}
		return token;
	}

	// private members
	private KeywordMap keywords;
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.24  1999/05/03 04:28:01  sp
 * Syntax colorizing bug fixing, console bug fix for Swing 1.1.1
 *
 * Revision 1.23  1999/04/22 06:03:26  sp
 * Syntax colorizing change
 *
 * Revision 1.22  1999/04/19 05:38:20  sp
 * Syntax API changes
 *
 * Revision 1.21  1999/03/13 08:50:39  sp
 * Syntax colorizing updates and cleanups, general code reorganizations
 *
 * Revision 1.20  1999/03/12 23:51:00  sp
 * Console updates, uncomment removed cos it's too buggy, cvs log tags added
 *
 */
