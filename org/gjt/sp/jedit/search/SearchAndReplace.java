/*
 * SearchAndReplace.java - Search and replace manager
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

/**
 * The main class of the jEdit search and replace system.
 * @author Slava Pestov
 * @version $Id$
 */
public class SearchAndReplace
{
	/**
	 * Returns the string matcher.
	 */
	public static SearchMatcher getMatcher()
	{
		return matcher;
	}

	/**
	 * Sets the string matcher.
	 * @param matcher The new matcher
	 */
	public static void setMatcher(SearchMatcher matcher)
	{
		SearchAndReplace.matcher = matcher;
	}

	/**
	 * Returns the file matcher.
	 */
	public static SearchFileMatcher getFileMatcher()
	{
		return fileMatcher;
	}

	/**
	 * Sets the file matcher.
	 * @param fileMatcher The file matcher
	 */
	public static void setFileMatcher(SearchFileMatcher fileMatcher)
	{
		SearchAndReplace.fileMatcher = fileMatcher;
	}

	// private members
	private static SearchMatcher matcher;
	private static SearchFileMatcher fileMatcher;

	private SearchAndReplace() {}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.1  1999/05/27 09:55:21  sp
 * Search and replace overhaul started
 *
 */
