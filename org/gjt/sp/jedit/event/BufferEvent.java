/*
 * BufferEvent.java - Event fired when a buffer's state changes
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

package org.gjt.sp.jedit.event;

import java.util.EventListener;
import org.gjt.sp.jedit.*;

/**
 * The event fired when a buffer's state changes.
 *
 * @author Slava Pestov
 * @version $Id$
 */
public class BufferEvent extends AbstractEditorEvent
{
	/**
	 * The first event id that denotes an buffer event.
	 */
	public static final int BUFFER_FIRST = EditorEvent.EDITOR_LAST + 1;

	/**
	 * The return value of the <code>getID()</code> function when
	 * a buffer's dirty status has been changed. This event is fired
	 * when the buffer is saved, and the first time it is changed
	 * after a save.
	 */
	public static final int DIRTY_CHANGED = BUFFER_FIRST;

	/**
	 * The return value of the <code>getID()</code> function when
	 * a marker has been added or removed to the buffer.
	 */
	public static final int MARKERS_CHANGED = BUFFER_FIRST + 1;

	/**
	 * The return value of the <code>getID()</code> function when
	 * a buffer's edit mode has changed.
	 */
	public static final int MODE_CHANGED = BUFFER_FIRST + 2;

	/**
	 * The last event id that denotes a buffer event.
	 */
	public static final int BUFFER_LAST = BUFFER_FIRST + 2;

	/**
	 * Creates a new buffer event.
	 * @param id The event type
	 * @param buffer The buffer involved
	 */
	public BufferEvent(int id, Buffer buffer)
	{
		super(id,null,buffer);

		// Check the id value
		if(id < BUFFER_FIRST || id > BUFFER_LAST)
			throw new IllegalArgumentException("id out of range");
	}

	/**
	 * Fires the event to the specified listener. This invokes
	 * the appropriate method of the listener, depending on the
	 * event type.
	 * @param listener The event listener
	 */
	public void fire(EventListener listener)
	{
		if(!(listener instanceof BufferListener))
			return;

		BufferListener l = (BufferListener)listener;

		switch(id)
		{
		case DIRTY_CHANGED:
			l.bufferDirtyChanged(this);
			break;
		case MARKERS_CHANGED:
			l.bufferMarkersChanged(this);
			break;
		case MODE_CHANGED:
			l.bufferModeChanged(this);
			break;
		default:
			// shouldn't happen
			throw new InternalError();
		}
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.7  1999/07/08 06:06:04  sp
 * Bug fixes and miscallaneous updates
 *
 * Revision 1.6  1999/04/24 01:55:28  sp
 * MiscUtilities.constructPath() bug fixed, event system bug(s) fix
 *
 * Revision 1.5  1999/03/20 04:52:55  sp
 * Buffer-specific options panel finished, attempt at fixing OS/2 caret bug, code
 * cleanups
 *
 * Revision 1.4  1999/03/16 04:34:46  sp
 * HistoryTextField updates, moved generate-text to a plugin, fixed spelling mistake in EditAction Javadocs
 *
 * Revision 1.3  1999/03/14 02:22:13  sp
 * Syntax colorizing tweaks, server bug fix
 *
 * Revision 1.2  1999/03/12 23:51:00  sp
 * Console updates, uncomment removed cos it's too buggy, cvs log tags added
 *
 */
