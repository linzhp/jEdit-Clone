/*
 * PerlTokenMarker.java - Perl token marker
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
 * Perl token marker.
 *
 * @author Slava Pestov
 * @version $Id$
 */
public class PerlTokenMarker extends TokenMarker
{
	// public members
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
		int matchCount = 0;
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
				if(!Character.isLetterOrDigit(c) && c != '_')
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
			else if(token == Token.KEYWORD3)
			{
				if(backslash)
					backslash = false;
				else
				{
					if(c == matchChar && --matchCount == 0)
					{
						token = Token.NULL;
						addToken(i1 - lastOffset,
							Token.KEYWORD3);
						lastOffset = i1;
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
				else if(token == Token.NULL)
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
			case '&': case '$': case '%': case '@':
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
				else if(token == Token.NULL)
				{
					token = Token.LITERAL2;
					addToken(i - lastOffset,Token.NULL);
					lastOffset = i;
				}
				else if(token == Token.LITERAL2)
				{
					token = Token.NULL;
					addToken(i1 - lastOffset,Token.LITERAL1);
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
			case 's': case 'y': case 't':
			case 'm': case 'q':
				if(token == Token.NULL && length - i > 2)
				{
					char ch = array[i1];
					if(!Character.isLetter(ch) && ch != '_'
						&& ch != ' ')
					{
						if(c == 's')
						{
							matchCount = 2;
							i++;
						}
						else if(c == 'y')
						{
							matchCount = 2;
							i++;
						}
						else if(c == 'm')
						{
							matchCount = 1;
							i++;
						}
					}
					else if(c == 't')
					{
						if(array[i1] == 'r')
						{
							matchCount = 2;
							i += 2;
						}
					}
					else if(c == 'q')
					{
						c = array[i1];
						if(c == 'q' || c == 'w' || c == 'x')
						{
							matchCount = 1;
							i += 2;
						}
						else if(!Character.isLetter(c))
						{
							matchCount = 1;
							i++;
						}
					}
				}/*
				else
					break;*/ // no m<2 chars> or q<2 chars> keywords
			case '/': case '?':
				if(token == Token.NULL && length - i > 1)
				{
					matchChar = array[i];
					if(!Character.isLetter(matchChar))
					{
						char ch = array[i+1];
						if(c == '/' || c == '?')
						{
							matchCount = 1;
							if(ch == '\t' || ch == ' ')
								break;
						}
						token = Token.KEYWORD3;
						addToken(i - lastOffset,Token.NULL);
						lastOffset = i;
					}
				}
			default:
				backslash = false;
				if(token == Token.NULL && c != '_' &&
					!Character.isLetter(c))
				{
					int len = i - lastKeyword;
					byte id = keywords.lookup(line,lastKeyword,len);
					if(id != Token.NULL)
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
			if(token == Token.KEYWORD3)
			{
				addToken(length - lastOffset,Token.INVALID);
				token = Token.NULL;
			}
			else
				addToken(length - lastOffset,token);
		}
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
		}
		return perlKeywords;
	}	
}

/**
 * ChangeLog:
 * $Log$
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
