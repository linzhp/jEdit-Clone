/*
 * CurrentBufferMatcher.java - File matcher interface with current buffer only
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

import org.gjt.sp.jedit.*;

/**
 * A SearchFileMatcher implementation that can be used when the current buffer
 * is to be searched.
 * @author Slava Pestov
 * @version $Id$
 */
public class CurrentBufferMatcher implements SearchFileMatcher
{
	/**
	 * Returns the next buffer in the list of buffers to search in.
	 * After the last buffer has been returned, the next call to
	 * <code>getNextBuffer()</code> should return the first buffer
	 * again.
	 * @param view The view that invoked this search &amp; replace
	 * operation.
	 */
	public Buffer getNextBuffer(View view)
	{
		return view.getBuffer();
	}

	/**
	 * Returns true if the end if the buffer list has been reached,
	 * false otherwise. When using this interface, check if this is
	 * true after calling <code>getNextBuffer()</code>, and if so,
	 * end the search.
	 */
	public boolean isLastBuffer()
	{
		return true;
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.2  1999/05/29 08:06:56  sp
 * Search and replace overhaul
 *
 * Revision 1.1  1999/05/27 09:55:21  sp
 * Search and replace overhaul started
 *
 */
