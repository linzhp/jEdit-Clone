/*
 * PerlTokenMarker.java - Perl token marker
 * Copyright (C) 1998, 1999 Slava Pestov
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA.
 */
package org.gjt.sp.jedit.syntax;

import javax.swing.text.Segment;

/**
 * Perl token marker.
 *
 * @author Slava Pestov
 * @version $Id$
 */
public class PerlTokenMarker extends TokenMarker
{
	// public members
	public static final byte S_ONE = Token.INTERNAL_FIRST;
	public static final byte S_TWO = Token.INTERNAL_FIRST + 1;
	public static final byte S_END = Token.INTERNAL_FIRST + 2;

	public PerlTokenMarker()
	{
		this(getKeywords());
	}

	public PerlTokenMarker(KeywordMap keywords)
	{
		this.keywords = keywords;
	}

	public byte markTokensImpl(byte token, Segment line, int lineIndex)
	{
		char[] array = line.array;
		int offset = line.offset;
		int lastOffset = offset;
		int lastKeyword = offset;
		char matchChar = '\0';
		boolean matchCharBracket = false;
		boolean matchSpacesAllowed = false;
		int length = line.count + offset;

		if(token == Token.LITERAL1 && lineIndex != 0
			&& lineInfo[lineIndex - 1].obj != null)
		{
			String str = (String)lineInfo[lineIndex - 1].obj;
			if(str != null && str.length() == line.count
				&& SyntaxUtilities.regionMatches(false,line,
				offset,str))
			{
				addToken(line.count,Token.LITERAL1);
				return Token.NULL;
			}
			else
			{
				addToken(line.count,Token.LITERAL1);
				lineInfo[lineIndex].obj = str;
				return Token.LITERAL1;
			}
		}

		boolean backslash = false;
loop:		for(int i = offset; i < length; i++)
		{
			int i1 = (i+1);

			char c = array[i];
			if(token == Token.KEYWORD2)
			{
				backslash = false;
				if(!Character.isLetterOrDigit(c) && c != '_'
					&& c != '#' && c != '\'' && c != ':')
				{
					token = Token.NULL;
					if(i != offset && array[i-1] == '$')
					{
						addToken(i1 - lastOffset,
							Token.KEYWORD2);
						lastOffset = i1;
						continue;
					}
					else
					{
						addToken(i - lastOffset,
							Token.KEYWORD2);
						lastOffset = i;
					}
				}
			}
			else if(token == S_ONE || token == S_TWO)
			{
				if(c == '\\')
					backslash = !backslash;
				else if(backslash)
					backslash = false;
				else
				{
					if(matchChar == '\0')
					{
						if(c == ' ' || c == '\t'
							&& !matchSpacesAllowed)
							continue;
						else
							matchChar = c;
					}
					else
					{
						switch(matchChar)
						{
						case '(':
							matchChar = ')';
							matchCharBracket = true;
							break;
						case '[':
							matchChar = ']';
							matchCharBracket = true;
							break;
						case '{':
							matchChar = '}';
							matchCharBracket = true;
							break;
						case '<':
							matchChar = '>';
							matchCharBracket = true;
							break;
						default:
							matchCharBracket = false;
							break;
						}
						if(c != matchChar)
							continue;
						if(token == S_TWO)
						{
							token = S_ONE;
							if(matchCharBracket)
								matchChar = '\0';
						}
						else
						{
							token = S_END;
							addToken(i1 - lastOffset,
								Token.LITERAL2);
							lastOffset = i1;
						}
					}
				}
				continue;
			}

			switch(c)
			{
			case '\\':
				backslash = !backslash;
				break;
			case '#':
				if(backslash)
					backslash = false;
				else if(token == Token.NULL
					|| token == S_ONE
					|| token == S_TWO
					|| lastKeyword == i)
				{
					addToken(i - lastOffset,Token.NULL);
					addToken(length - i,Token.COMMENT1);
					lastOffset = length;
					break loop;
				}
				break;
			case '=':
				if(i == offset)
				{
					if(token == Token.NULL)
					{
						token = Token.COMMENT2;
						addToken(length - i,Token.COMMENT2);
						lastOffset = length;
						break loop;
					}
					else if(token == Token.COMMENT2)
					{
						if(SyntaxUtilities.regionMatches(
							false,line,offset,"=cut"))
						{
							token = Token.NULL;
						}
						addToken(line.count,Token.COMMENT2);
						lastOffset = length;
						break loop;
					}
				}
				break;
			case '$':
				// this is done so that $" is handled
				// correctly in string literals
				if(token == Token.LITERAL1
					|| token == Token.LITERAL2)
				{
					backslash = true;
					break;
				}
			case '&': case '%': case '@':
				if(token == Token.NULL && length - i > 1)
				{
					if(c == '&' && array[i+1] == '&')
						i++;
					else
					{
						addToken(i - lastOffset,Token.NULL);
						lastOffset = i;
						token = Token.KEYWORD2;
					}
				}
				break;
			case '"':
				if(backslash)
					backslash = false;
				else if(token == Token.NULL)
				{
					token = Token.LITERAL1;
					lineInfo[lineIndex].obj = null;
					addToken(i - lastOffset,Token.NULL);
					lastOffset = i;
				}
				else if(token == Token.LITERAL1)
				{
					token = Token.NULL;
					addToken(i1 - lastOffset,Token.LITERAL1);
					lastOffset = i1;
				}
				break;
			case '\'':
				if(backslash)
					backslash = false;
				else if(token == Token.NULL && lastKeyword == i)
				{
					token = Token.LITERAL2;
					addToken(i - lastOffset,Token.NULL);
					lastOffset = i;
				}
				else if(token == Token.LITERAL2)
				{
					token = Token.NULL;
					addToken(i1 - lastOffset,Token.LITERAL2);
					lastOffset = i1;
				}
				break;
			case '`':
				if(backslash)
					backslash = false;
				else if(token == Token.NULL)
				{
					token = Token.OPERATOR;
					addToken(i - lastOffset,Token.NULL);
					lastOffset = i;
				}
				else if(token == Token.OPERATOR)
				{
					token = Token.NULL;
					addToken(i1 - lastOffset,Token.OPERATOR);
					lastOffset = i1;
				}
				break;
			case '<':
				if(backslash)
					backslash = false;
				else if(token == Token.NULL)
				{
					if(length - i > 1 && array[i1] == '<')
					{
						addToken(i - lastOffset,Token.NULL);
						lastOffset = i;
						token = Token.LITERAL1;
						int len = length - (i+2);
						if(array[length - 1] == ';')
							len--;
						lineInfo[lineIndex].obj =
							new String(array,i + 2,len);
					}
				}
				break;
			case ':':
				if(token == Token.NULL && lastOffset == i)
				{
					backslash = false;
					addToken(i1 - lastOffset,Token.LABEL);
					lastOffset = i1;
				}
				break;
			case '/': case '?':
				if(token == Token.NULL && 
					lastKeyword == i && length - i > 1)
				{
					backslash = false;
					char ch = array[i1];
					if(ch == '\t' || ch == ' ')
						break;
					matchChar = c;
					matchSpacesAllowed = false;
					token = S_ONE;
					addToken(i - lastOffset,Token.NULL);
					lastOffset = i;
					break;
				}
			default:
				backslash = false;
				if(c != '_' && !Character.isLetterOrDigit(c))
				{
					if(token == S_END)
					{
						addToken(i - lastOffset,Token.LITERAL2);
						token = Token.NULL;
						lastOffset = i;
						break;
					}
					else if(token != Token.NULL)
					{
						lastKeyword = i1;
						break;
					}

					int len = i - lastKeyword;
					byte id = keywords.lookup(line,lastKeyword,len);
					if(id == S_ONE || id == S_TWO)
					{
						addToken(i - lastOffset,Token.LITERAL2);
						lastOffset = i;
						if(c == ' ' || c == '\t')
							matchChar = '\0';
						else
							matchChar = c;
						matchSpacesAllowed = true;
						token = id;
					}
					else if(id != Token.NULL)
					{
						if(lastKeyword != lastOffset)
							addToken(lastKeyword - lastOffset,Token.NULL);
						addToken(len,id);
						lastOffset = i;
					}
					lastKeyword = i1;
				}
				break;
			}
		}
		if(token == Token.NULL)
		{
			int len = length - lastKeyword;
			byte id = keywords.lookup(line,lastKeyword,len);
			if(id == S_ONE || id == S_TWO)
				token = id;
			else if(id != Token.NULL)
			{
				if(lastKeyword != lastOffset)
					addToken(lastKeyword - lastOffset,Token.NULL);
				addToken(len,id);
				lastOffset = length;
			}
		}
		if(token == Token.KEYWORD2)
		{
			addToken(length - lastOffset,Token.KEYWORD2);
			token = Token.NULL;
		}
		else if(token == Token.KEYWORD3) 
		{
			addToken(length - lastOffset,Token.INVALID);
			token = Token.NULL;
		}
		else if(token == S_ONE || token == S_TWO)
		{
			addToken(length - lastOffset,Token.LITERAL2);
		}
		else if(token == S_END)
		{
			addToken(length - lastOffset,Token.LITERAL2);
			token = Token.NULL;
		}
		else
			addToken(length - lastOffset,token);
		return token;
	}

	// private members
	private KeywordMap keywords;

	private static KeywordMap perlKeywords;

	private static KeywordMap getKeywords()
	{
		if(perlKeywords == null)
		{
			perlKeywords = new KeywordMap(false);
			perlKeywords.add("continue",Token.KEYWORD1);
			perlKeywords.add("do",Token.KEYWORD1);
			perlKeywords.add("else",Token.KEYWORD1);
			perlKeywords.add("elsif",Token.KEYWORD1);
			perlKeywords.add("for",Token.KEYWORD1);
			perlKeywords.add("foreach",Token.KEYWORD1);
			perlKeywords.add("goto",Token.KEYWORD1);
			perlKeywords.add("last",Token.KEYWORD1);
			perlKeywords.add("if",Token.KEYWORD1);
			perlKeywords.add("my",Token.KEYWORD1);
			perlKeywords.add("next",Token.KEYWORD1);
			perlKeywords.add("sub",Token.KEYWORD1);
			perlKeywords.add("unless",Token.KEYWORD1);
			perlKeywords.add("until",Token.KEYWORD1);
			perlKeywords.add("while",Token.KEYWORD1);

			perlKeywords.add("m",S_ONE);
			perlKeywords.add("q",S_ONE);
			perlKeywords.add("qq",S_ONE);
			perlKeywords.add("qw",S_ONE);
			perlKeywords.add("qx",S_ONE);
			perlKeywords.add("s",S_TWO);
			perlKeywords.add("tr",S_TWO);
			perlKeywords.add("y",S_TWO);
		}
		return perlKeywords;
	}	
}

/**
 * ChangeLog:
 * $Log$
 * Revision 1.4  1999/06/06 05:05:25  sp
 * Search and replace tweaks, Perl/Shell Script mode updates
 *
 * Revision 1.3  1999/06/05 00:22:58  sp
 * LGPL'd syntax package
 *
 * Revision 1.2  1999/06/03 08:24:14  sp
 * Fixing broken CVS
 *
 * Revision 1.3  1999/05/31 08:11:10  sp
 * Syntax coloring updates, expand abbrev bug fix
 *
 * Revision 1.2  1999/05/31 04:38:51  sp
 * Syntax optimizations, HyperSearch for Selection added (Mike Dillon)
 *
 * Revision 1.1  1999/05/30 04:57:15  sp
 * Perl mode started
 *
 */
