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
	public HTMLTokenMarker()
	{
		keywords = JavaScriptTokenMarker.getKeywords();
	}

	public Token markTokens(Segment line, int lineIndex)
	{
		lastToken = null;
		String token = lineIndex == 0 ? null : lineInfo[lineIndex - 1];
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
					token = null;
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
						token = Token.ALTTXT;
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
				if(token == Token.ALTTXT)
				{
					int len = i - lastKeyword;
					String id = keywords.lookup(line,lastKeyword,len);
					if(id != null)
					{
						if(lastKeyword != lastOffset)
							addToken(lastKeyword - lastOffset,null);
						addToken(len,id);
						lastOffset = i;
					}
					lastKeyword = i + 1;
				}
				break;
			case '<':
				backslash = false;
				if(token == null)
				{
					if(SyntaxUtilities.regionMatches(false,
						line,i,"<!--"))
						token = Token.COMMENT1;
					else
						token = Token.KEYWORD1;
					addToken(i - lastOffset,null);
					lastOffset = i;
				}
				else if(token == Token.ALTTXT)
				{
					if(SyntaxUtilities.regionMatches(true,
						line,i,"</SCRIPT>"))
					{
						token = Token.KEYWORD1;
						addToken(i - lastOffset,null);
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
						token = Token.ALTTXT;
					else
						token = null;
					addToken((i+1) - lastOffset,Token.KEYWORD1);
					lastOffset = i + 1;
				}
				else if(token == Token.COMMENT1)
				{
					if(SyntaxUtilities.regionMatches(false,line,
						i - 2,"-->"))
					{
						token = null;
						addToken((i+1) - lastOffset,
							 Token.COMMENT1);
						lastOffset = i + 1;
					}
				}
				break;
			case '&':
				backslash = false;
				if(token == null)
				{
					token = Token.KEYWORD2;
					addToken(i - lastOffset,null);
					lastOffset = i;
				}
				break;
			case ':':
				backslash = false;
				if(token == Token.ALTTXT && lastKeyword == offset)
				{
					addToken((i+1) - lastOffset,Token.LABEL);
					lastOffset = i + 1;
				}
				break;
			case '/':
				backslash = false;
				if(token == Token.ALTTXT && length - i > 1)
				{
					switch(line.array[i+1])
					{
					case '*':
						token = Token.COMMENT2;
						addToken(i - lastOffset,Token.ALTTXT);
						lastOffset = i;
						i++;
						break;
					case '/':
						addToken(i - lastOffset,token);
						addToken(length - i,Token.COMMENT2);
						lastOffset = length;
						break loop;
					}
				}
				break;
			case '"':
				if(backslash)
					backslash = false;
				else if(token == Token.ALTTXT)
				{
					token = Token.LITERAL1;
					addToken(i - lastOffset,Token.ALTTXT);
					lastOffset = i;
				}
				else if(token == Token.LITERAL1)
				{
					token = Token.ALTTXT;
					addToken((i+1) - lastOffset,Token.LITERAL1);
					lastOffset = i + 1;
				}
				break;
			case '\'':
				if(backslash)
					backslash = false;
				else if(token == Token.ALTTXT)
				{
					token = Token.LITERAL2;
					addToken(i - lastOffset,Token.ALTTXT);
					lastOffset = i;
				}
				else if(token == Token.LITERAL2)
				{
					token = Token.ALTTXT;
					addToken((i+1) - lastOffset,Token.LITERAL2);
					lastOffset = i + 1;
				}
				break;
			default:
				backslash = false;
				break;
			}
		}
		if(token == Token.ALTTXT)
		{
			int len = length - lastKeyword;
			String id = keywords.lookup(line,lastKeyword,len);
			if(id != null)
			{
				if(lastKeyword != lastOffset)
					addToken(lastKeyword - lastOffset,null);
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
				if(token == Token.KEYWORD2)
					token = null;
				else
					token = Token.ALTTXT;
			}
			else
				addToken(length - lastOffset,token);
		}
		lineInfo[lineIndex] = token;
		if(lastToken != null)
		{
			lastToken.nextValid = false;
			return firstToken;
		}
		else
			return null;
	}

	// private members
	private KeywordMap keywords;
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.21  1999/03/13 08:50:39  sp
 * Syntax colorizing updates and cleanups, general code reorganizations
 *
 * Revision 1.20  1999/03/12 23:51:00  sp
 * Console updates, uncomment removed cos it's too buggy, cvs log tags added
 *
 */
