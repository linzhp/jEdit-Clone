/*
 * BufferListSet.java - Buffer list matcher
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
import java.util.Vector;

/**
 * A file set for searching a user-specified list of buffers.
 * @author Slava Pestov
 * @version $Id$
 */
public class BufferListSet implements SearchFileSet
{
	/**
	 * Creates a new buffer list search set.
	 * @param buffers The buffers to search
	 */
	public BufferListSet(Object[] buffers)
	{
		this.buffers = buffers;
	}

	/**
	 * Returns the list of buffers to search.
	 * @param view The view performing the search
	 */
	public Buffer[] getSearchBuffers(View view)
	{
		Vector _buffers = new Vector();
		for(int i = 0; i < buffers.length; i++)
		{
			Buffer buffer = (Buffer)buffers[i];
			if(!buffer.isClosed())
				_buffers.addElement(buffer);
		}
		Buffer[] bufferArray = new Buffer[_buffers.size()];
		_buffers.copyInto(bufferArray);
		return bufferArray;
	}

	/**
	 * Returns the next buffer to search.
	 * @param view The view performing the search
	 * @param buffer The last buffer searched
	 */
	public Buffer getNextBuffer(View view, Buffer buffer)
	{
		throw new InternalError("find-next not supported for "
			+ "this file set");
	}

	private Object[] buffers;
}
/*
 * ChangeLog:
 * $Log$
 * Revision 1.2  1999/06/09 05:22:11  sp
 * Find next now supports multi-file searching, minor Perl mode tweak
 *
 * Revision 1.1  1999/06/03 08:24:13  sp
 * Fixing broken CVS
 *
 */
