/*
 * LiteralSearchMatcher.java - String literal matcher
 * Copyright (C) 1999 Slava Pestov
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

package org.gjt.sp.jedit.search;

import gnu.regexp.*;

/**
 * A string literal matcher.
 * @author Slava Pestov
 * @version $Id$
 */
public class LiteralSearchMatcher implements SearchMatcher
{
	/**
	 * Creates a new string literal matcher.
	 * @param search The search string
	 * @param replace The replacement string
	 * @param ignoreCase True if the matcher should be case insensitive,
	 * false otherwise
	 */
	public LiteralSearchMatcher(String search, String replace,
		boolean ignoreCase)
	{
		this.search = search;
		this.replace = replace;
		this.ignoreCase = ignoreCase;
	}

	/**
	 * Returns the offset of the first match of the specified text
	 * within this matcher.
	 * @param text The text to search in
	 * @return an array where the first element is the start offset
	 * of the match, and the second element is the end offset of
	 * the match
	 */
	public int[] nextMatch(String text)
	{
		int searchLen = search.length();
		int len = text.length() - searchLen + 1;

		for(int i = 0; i < len; i++)
		{
			if(text.regionMatches(ignoreCase,i,search,0,searchLen))
			{
				int[] result = { i, i + searchLen };
				return result;
			}
		}

		return null;
	}

	/**
	 * Returns the specified text, with any substitution specified
	 * within this matcher performed.
	 * @param text The text
	 */
	public String substitute(String text)
	{
		StringBuffer buf = null;
		int lastMatch = 0;
		int searchLen = search.length();
		int len = text.length() - searchLen + 1;
		boolean matchFound = false;

		int i = 0;
		while(i < len)
		{
			if(text.regionMatches(ignoreCase,i,search,0,searchLen))
			{
				if(buf == null)
					buf = new StringBuffer();
				if(i != lastMatch)
					buf.append(text.substring(lastMatch,i));
				buf.append(replace);
				i += searchLen;
				lastMatch = i;
				matchFound = true;
			}
			else
				i++;
		}
		if(!matchFound)
			return null;
		if(text.length() != lastMatch)
			buf.append(text.substring(lastMatch,text.length()));
		return buf.toString();
	}

	// private members
	private String search;
	private String replace;
	private boolean ignoreCase;
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.2  1999/06/06 05:05:25  sp
 * Search and replace tweaks, Perl/Shell Script mode updates
 *
 * Revision 1.1  1999/05/29 08:06:56  sp
 * Search and replace overhaul
 *
 */
