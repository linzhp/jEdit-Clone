/*
 * HTMLTokenMarker.java - HTML token marker
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
import org.gjt.sp.jedit.mode.javascript;
import org.gjt.sp.jedit.jEdit;

public class HTMLTokenMarker extends TokenMarker
{
	// public members
	public static final String COMMENT = "comment";
	public static final String TAG = "tag";
	public static final String ENTITY = "entity";
	public static final String JAVASCRIPT = "javascript";
	public static final String LABEL = "label";
	public static final String JS_COMMENT = "js_comment";
	public static final String DQUOTE = "dquote";
	public static final String SQUOTE = "squote";

	public HTMLTokenMarker()
	{
		keywords = javascript.getKeywords();
	}

	public Token markTokens(Segment line, int lineIndex)
	{
		ensureCapacity(lineIndex);
		lastToken = null;
		String token = lineIndex == 0 ? null : lineInfo[lineIndex - 1];
		boolean backslash = false;
		int offset = line.offset;
		int lastOffset = offset;
		int length = line.count + offset;
loop:		for(int i = offset; i < length; i++)
		{
			switch(line.array[i])
			{
			case '\\':
				backslash = !backslash;
				break;
			case ';':
				if(token == ENTITY)
				{
					token = null;
					addToken((i+1) - lastOffset,ENTITY);
					lastOffset = i + 1;
					break;
				}
			case '.': case ',': case ' ': case '\t':
			case '(': case ')': case '[': case ']':
				backslash = false;
				if(token == JAVASCRIPT)
				{
					int off = i;
					while(--off >= lastOffset)
					{
						if(!Character.isLetter(line.array[off]))
							break;
					}
					off++;
					int len = i - off;
					String id = keywords.lookup(line,off,len);
					if(id != null)
					{
						if(off != lastOffset)
							addToken(off - lastOffset,JAVASCRIPT);
						addToken(len,id);
						lastOffset = i;
					}
				}
				break;
			case '<':
				backslash = false;
				if(token == null)
				{
					if(jEdit.regionMatches(false,line,i,"<!--"))
						token = COMMENT;
					else
						token = TAG;
					addToken(i - lastOffset,null);
					lastOffset = i;
				}
				else if(token == JAVASCRIPT)
				{
					if(jEdit.regionMatches(true,line,i,
						"</SCRIPT>"))
					{
						token = TAG;
						addToken(i - lastOffset,JAVASCRIPT);
						lastOffset = i;
					}
				}
				break;
			case '>':
				backslash = false;
				if(token == TAG)
				{
					if(jEdit.regionMatches(true,line,
						lastOffset,"<SCRIPT"))
						token = JAVASCRIPT;
					else
						token = null;
					addToken((i+1) - lastOffset,TAG);
					lastOffset = i + 1;
				}
				else if(token == COMMENT)
				{
					if(jEdit.regionMatches(false,line,
						i - 2,"-->"))
					{
						token = null;
						addToken((i+1) - lastOffset,
							 COMMENT);
						lastOffset = i + 1;
					}
				}
				break;
			case '&':
				backslash = false;
				if(token == null)
				{
					token = ENTITY;
					addToken(i - lastOffset,null);
					lastOffset = i;
				}
				break;
			case ':':
				backslash = false;
				if(token == JAVASCRIPT)
				{
					addToken((i+1) - lastOffset,LABEL);
					lastOffset = i + 1;
				}
				break;
			case '/':
				backslash = false;
				if(token == JAVASCRIPT && length - i >= 1)
				{
					switch(line.array[i+1])
					{
					case '*':
						token = JS_COMMENT;
						addToken(i - lastOffset,JAVASCRIPT);
						lastOffset = i;
						i++;
						break;
					case '/':
						addToken(i - lastOffset,token);
						addToken(length - i,JS_COMMENT);
						lastOffset = length;
						break loop;
					}
				}
				break;
			case '*':
				backslash = false;
				if(token == JS_COMMENT && length - i >= 1)
				{
					if(length - i > 1 && line.array[i+1] == '/')
					{
						token = JAVASCRIPT;
						i++;
						addToken((i+1) - lastOffset,JS_COMMENT);
						lastOffset = i + 1;
					}
				}
				break;
			case '"':
				if(backslash)
					backslash = false;
				else if(token == JAVASCRIPT)
				{
					token = DQUOTE;
					addToken(i - lastOffset,JAVASCRIPT);
					lastOffset = i;
				}
				else if(token == DQUOTE)
				{
					token = JAVASCRIPT;
					addToken((i+1) - lastOffset,DQUOTE);
					lastOffset = i + 1;
				}
				break;
			case '\'':
				if(backslash)
					backslash = false;
				else if(token == JAVASCRIPT)
				{
					token = SQUOTE;
					addToken(i - lastOffset,JAVASCRIPT);
					lastOffset = i;
				}
				else if(token == SQUOTE)
				{
					token = JAVASCRIPT;
					addToken((i+1) - lastOffset,SQUOTE);
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
			int off = length;
			while(--off >= lastOffset)
			{
				if(!Character.isLetter(line.array[off]))
					break;
			}
			off++;
			int len = length - off;
			String id = keywords.lookup(line,off,len);
			if(id != null)
			{
				if(off != lastOffset)
					addToken(off - lastOffset,null);
				addToken(len,id);
				lastOffset = length;
			}
		}
		if(lastOffset != length)
			addToken(length - lastOffset,token);
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
