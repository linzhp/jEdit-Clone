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
		this.buffers = new Buffer[buffers.length];
		System.arraycopy(buffers,0,this.buffers,0,buffers.length);
	}

	/**
	 * Returns the list of buffers to search.
	 * @param view The view performing the search
	 */
	public Buffer[] getSearchBuffers(View view)
	{
		updateBufferList();
		return buffers;
	}

	/**
	 * Returns the next buffer to search.
	 * @param view The view performing the search
	 * @param buffer The last buffer searched
	 */
	public Buffer getNextBuffer(View view, Buffer buffer)
	{
		updateBufferList();
		if(buffer == null)
			return buffers[0];
		else
		{
			for(int i = 0; i < buffers.length; i++)
			{
				if(buffers[i] == buffer)
				{
					if(buffers.length - i > 1)
						return buffers[i+1];
					else
						break;
				}
			}
		}
		System.out.println("returning null");
		return null;
	}

	/**
	 * Returns the first buffer to search.
	 * @param view The view performing the search
	 */
	public Buffer getFirstBuffer(View view)
	{
		updateBufferList();
		return buffers[0];
	}

	private Buffer[] buffers;

	private void updateBufferList()
	{
		Vector _buffers = new Vector(buffers.length);
		for(int i = 0; i < buffers.length; i++)
		{
			Buffer buffer = buffers[i];
			if(!buffer.isClosed())
				_buffers.addElement(buffer);
		}
		if(_buffers.size() != buffers.length)
		{
			Buffer[] bufferArray = new Buffer[_buffers.size()];
			_buffers.copyInto(bufferArray);
			buffers = bufferArray;
		}
	}
}
/*
 * ChangeLog:
 * $Log$
 * Revision 1.4  1999/06/15 05:03:54  sp
 * RMI interface complete, save all hack, views & buffers are stored as a link
 * list now
 *
 * Revision 1.3  1999/06/09 07:28:10  sp
 * Multifile search and replace tweaks, removed console.html
 *
 * Revision 1.2  1999/06/09 05:22:11  sp
 * Find next now supports multi-file searching, minor Perl mode tweak
 *
 * Revision 1.1  1999/06/03 08:24:13  sp
 * Fixing broken CVS
 *
 */
