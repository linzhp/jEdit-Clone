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
		int offset = line.offset;
		int lastOffset = offset;
		int lastKeyword = offset;
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
			char c = line.array[i];
			if(token == Token.KEYWORD2)
			{
				backslash = false;
				if(!Character.isLetterOrDigit(c) && c != '_')
				{
					token = Token.NULL;
					if(i != offset && line.array[i-1] == '$')
					{
						addToken((i+1) - lastOffset,
							Token.KEYWORD2);
						lastOffset = i + 1;
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
					if(c == '&' && line.array[i+1] == '&')
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
					addToken((i+1) - lastOffset,Token.LITERAL1);
					lastOffset = i + 1;
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
					addToken((i+1) - lastOffset,Token.LITERAL1);
					lastOffset = i + 1;
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
					addToken((i+1) - lastOffset,Token.OPERATOR);
					lastOffset = i + 1;
				}
				break;
			case '<':
				if(backslash)
					backslash = false;
				else if(token == Token.NULL)
				{
					if(length - i > 1 && line.array[i+1] == '<')
					{
						addToken(i - lastOffset,Token.NULL);
						lastOffset = i;
						token = Token.LITERAL1;
						int len = length - (i+2);
						if(line.array[length - 1] == ';')
							len--;
						lineInfo[lineIndex].obj =
							new String(line.array,i + 2,len);
					}
				}
				break;
			case ':':
				if(token == Token.NULL && lastOffset == i)
				{
					backslash = false;
					addToken((i+1) - lastOffset,Token.LABEL);
					lastOffset = i + 1;
				}
				break;
			// odd stuff
			case '/':
				if(token == Token.NULL && lastKeyword == i
					&& length - i > 2)
				{
					token = Token.KEYWORD3;
					addToken(i - lastOffset,Token.NULL);
					lastOffset = i;
				}
				else if(token == Token.KEYWORD3)
				{
					token = Token.NULL;
					addToken((i+1) - lastOffset,Token.KEYWORD3);
					lastOffset = (i+1);
				}
				break;
			case 'q': case 'm':
				if(token == Token.NULL && lastKeyword == i
					&& length - i > 2) // XXX
				{
					backslash = false;
					char ch = line.array[i+1];
					if(ch == 'q' || ch == 'w' || ch == 'x')
					{
						ch = line.array[i+2];
					}
					if(ch
					int end = doWeirdSlashThing(line.array,
						ch,1,i,length);
					addToken(i - lastOffset,Token.NULL);
					addToken(end - lastOffset,Token.KEYWORD3);
					lastOffset = end;
					i = end;
					break;
				}
			case 't': case 'y': case 's':
				if(token == Token.NULL && lastKeyword == offset
					&& length - i > 2) // XXX
				{
					backslash = false;
					char ch = line.array[i+1];
					if(ch == 'r')
					{
						ch = line.array[i+2];
					}
					int end = doWeirdSlashThing(line.array,
						ch,2,i,length);
					addToken(i - lastOffset,Token.NULL);
					addToken(end - lastOffset,Token.KEYWORD3);
					lastOffset = end;
					i = end;
					break;
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
					lastKeyword = i + 1;
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
			addToken(length - lastOffset,token);
		return token;
	}

	// private members
	private KeywordMap keywords;
	
	private int doWeirdSlashThing(char[] line, char ch, int count,
		int start, int length)
	{
		boolean backslash = false;
		for(int i = start; i < length; i++)
		{
			if(line[i] == '\\')
				backslash = !backslash;
			else if(line[i] == ch)
			{
				if(backslash)
					backslash = false;
				else if(--count == 0)
					return i;
			}
		}
		return start;
	}

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
 * Revision 1.1  1999/05/30 04:57:15  sp
 * Perl mode started
 *
 */
