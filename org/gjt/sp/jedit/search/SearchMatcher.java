/*
 * SearchMatcher.java - Abstract string matcher interface
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
 * An abstract interface for matching strings.
 * @author Slava Pestov
 * @version $Id$
 */
public interface SearchMatcher
{
	/**
	 * Returns the offset of the first match of the specified text
	 * within this matcher.
	 * @param text The text to search in
	 * @return an array where the first element is the start offset
	 * of the match, and the second element is the end offset of
	 * the match
	 */
	public int[] nextMatch(String text);

	/**
	 * Returns the specified text, with any substitution specified
	 * within this matcher performed.
	 * @param text The text
	 * @return Null if no matches were found, or the changed string
	 */
	public String substitute(String text);
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.3  1999/06/06 05:05:25  sp
 * Search and replace tweaks, Perl/Shell Script mode updates
 *
 * Revision 1.2  1999/05/29 08:06:56  sp
 * Search and replace overhaul
 *
 * Revision 1.1  1999/05/27 09:55:21  sp
 * Search and replace overhaul started
 *
 */
