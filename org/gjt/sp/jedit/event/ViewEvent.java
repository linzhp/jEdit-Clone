/*
 * ViewEvent.java - Event fired when a views's state changes
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
 * The event fired when a view's state changes.
 *
 * @author Slava Pestov
 * @version $Id$
 */
public class ViewEvent extends AbstractEditorEvent
{
	/**
	 * The first event id that denotes an view event.
	 */
	public static final int VIEW_FIRST = BufferEvent.BUFFER_LAST + 1;

	/**
	 * The return value of the <code>getID()</code> function when
	 * a view's current error number has changed.
	 */
	public static final int CURRENT_ERROR_CHANGED = VIEW_FIRST + 1;

	/**
	 * The return value of the <code>getID()</code> function when
	 * a view's current buffer has changed.
	 */
	public static final int BUFFER_CHANGED = VIEW_FIRST + 2;

	/**
	 * The last event id that denotes a view event.
	 */
	public static final int VIEW_LAST = VIEW_FIRST + 2;

	/**
	 * Creates a new view event.
	 * @param id The event type
	 * @param view The view involved
	 * @param buffer Only for BUFFER_CHANGED event type - the original buffer
	 */
	public ViewEvent(int id, View view, Buffer buffer)
	{
		super(id);

		// Check the id value
		if(id < VIEW_FIRST || id > VIEW_LAST)
			throw new IllegalArgumentException("id out of range");

		this.view = view;
		this.buffer = buffer;
	}

	/**
	 * Returns the view involved. This is set for all event types.
	 */
	public View getView()
	{
		return view;
	}

	/**
	 * For the BUFFER_CHANGED event type only - returns the view's
	 * previous buffer. Null is returned if the event is of a different
	 * type, or if the view had no original buffer (if it's the first
	 * view created during startup).
	 */
	public Buffer getBuffer()
	{
		return buffer;
	}

	/**
	 * Fires the event to the specified listener. This invokes
	 * the appropriate method of the listener, depending on the
	 * event type.
	 * @param listener The event listener
	 */
	public void fire(EventListener listener)
	{
		if(!(listener instanceof ViewListener))
			return;

		ViewListener l = (ViewListener)listener;

		switch(id)
		{
		case CURRENT_ERROR_CHANGED:
			l.viewCurrentErrorChanged(this);
			break;
		case BUFFER_CHANGED:
			l.viewBufferChanged(this);
			break;
		default:
			// shouldn't happen
			throw new InternalError();
		}
	}

	/**
	 * Returns a string representation of this event.
	 */
	public String toString()
	{
		return getClass().getName() + "[id=" + id + ",timestamp="
			+ timestamp + ",view=" + view + ",buffer=" + buffer + "]";
	}

	// protected members
	protected View view;
	protected Buffer buffer;
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.4  1999/03/17 05:32:52  sp
 * Event system bug fix, history text field updates (but it still doesn't work), code cleanups, lots of banging head against wall
 *
 * Revision 1.3  1999/03/16 04:34:46  sp
 * HistoryTextField updates, moved generate-text to a plugin, fixed spelling mistake in EditAction Javadocs
 *
 * Revision 1.2  1999/03/12 23:51:00  sp
 * Console updates, uncomment removed cos it's too buggy, cvs log tags added
 *
 */
