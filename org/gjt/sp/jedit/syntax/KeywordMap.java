/*
 * KeywordMap.java - Fast keyword->id map
 * Copyright (C) 1998, 1999 Slava Pestov
 * Copyright (C) 1999 Mike Dillon
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
import org.gjt.sp.jedit.TextUtilities;

/**
 * A <code>KeywordMap</code> is similar to a hashtable in that it maps keys
 * to values. However, the `keys' are Swing segments. This allows lookups of
 * text substrings without the overhead of creating a new string object.
 * <p>
 * This class is used by <code>CTokenMarker</code> to map keywords to ids.
 * @see org.gjt.sp.jedit.syntax.CTokenMarker
 */
public class KeywordMap
{
	// public members

	/**
	 * Creates a new <code>KeywordMap</code>.
	 * @param ignoreCase True if keys are case insensitive
	 */
	public KeywordMap(boolean ignoreCase)
	{
		this(ignoreCase, 52);
		this.ignoreCase = ignoreCase;
	}

	protected KeywordMap(boolean ignoreCase, int mapLength)
	{
		this.mapLength = mapLength;
		this.ignoreCase = ignoreCase;
		map = new Keyword[mapLength];
	}

	/**
	 * Looks up a key.
	 * @param text The text segment
	 * @param offset The offset of the substring within the text segment
	 * @param length The length of the substring
	 */
	public String lookup(Segment text, int offset, int length)
	{
		if(length == 0)
			return null;
		Keyword k = map[getSegmentMapKey(text, offset, length)];
		while(k != null)
		{
			if(length != k.keyword.length)
			{
				k = k.next;
				continue;
			}
			if(TextUtilities.regionMatches(ignoreCase,text,offset,
				k.keyword))
				return k.id;
			k = k.next;
		}
		return null;
	}

	/**
	 * Adds a key-value mapping.
	 * @param keyword The key
	 * @Param id The value
	 */
	public void add(String keyword, String id)
	{
		int key = getStringMapKey(keyword);
		map[key] = new Keyword(keyword.toCharArray(),id,map[key]);
	}

	// protected members
	protected int mapLength;

	protected int getStringMapKey(String s)
	{
		return (Character.toUpperCase(s.charAt(0)) +
				Character.toUpperCase(s.charAt(s.length()-1)))
				% mapLength;
	}

	protected int getSegmentMapKey(Segment s, int off, int len)
	{
		return (Character.toUpperCase(s.array[off]) +
				Character.toUpperCase(s.array[off + len - 1]))
				% mapLength;
	}

	// private members
	private class Keyword
	{
		public Keyword(char[] keyword, String id, Keyword next)
		{
			this.keyword = keyword;
			this.id = id;
			this.next = next;
		}

		public char[] keyword;
		public String id;
		public Keyword next;
	}

	private Keyword[] map;
	private boolean ignoreCase;
}
