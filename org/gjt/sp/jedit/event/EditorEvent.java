/*
 * EditorEvent.java - Event fired when views and buffers are created and closed
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

import org.gjt.sp.jedit.*;

/**
 * The event fired when a view or buffer is created or closed.
 * Each editor event carries the buffer or view involved, along with
 * the event type and timestamp.
 */
public class EditorEvent extends AbstractEditorEvent
{
	/**
	 * The first event id that denotes an editor event.
	 */
	public static final int EDITOR_FIRST = ID_FIRST;

	/**
	 * The return value of the <code>getID()</code> function when
	 * a buffer has been created. This event is fired after the
	 * buffer has been added to the buffer list.
	 */
	public static final int BUFFER_CREATED = EDITOR_FIRST;

	/**
	 * The return value of the <code>getID()</code> function when
	 * a buffer has been closed. This event is fired after the
	 * buffer is removed from the buffer list.
	 */
	public static final int BUFFER_CLOSED = EDITOR_FIRST + 1;

	/**
	 * The return value of the <code>getID()</code> function when
	 * a view has been created. This event is fired after the view
	 * frame is shown.
	 */
	public static final int VIEW_CREATED = EDITOR_FIRST + 2;

	/**
	 * The return value of the <code>getID()</code> function when
	 * a view has been closed. This event is fired after the view
	 * frame has been destroyed.
	 */
	public static final int VIEW_CLOSED = EDITOR_FIRST + 3;

	/**
	 * The return value of the <code>getID()</code> function when
	 * a buffer's dirty status has been changed.
	 */
	public static final int BUFFER_DIRTY_CHANGED = EDITOR_FIRST + 4;

	/**
	 * The last event id that denotes an editor event.
	 */
	public static final int EDITOR_LAST = EDITOR_FIRST + 4;

	/**
	 * Creates a new editor event.
	 * @param id The event type
	 * @param view The view involved
	 * @param buffer The buffer involved
	 */
	public EditorEvent(int id, View view, Buffer buffer)
	{
		super(id);

		// Check the id value
		if(id < EDITOR_FIRST || id > EDITOR_LAST)
			throw new IllegalArgumentException("id out of range");

		this.view = view;
		this.buffer = buffer;
	}

	/**
	 * Returns the buffer involved. This is set for all event types.
	 */
	public Buffer getBuffer()
	{
		return buffer;
	}

	/**
	 * Returns the view involved. This is set in all cases except
	 * for the buffers being created before the initial view is
	 * shown, and those created by the server.
	 */
	public View getView()
	{
		return view;
	}

	/**
	 * Fires the event to the specified listener. This invokes
	 * the appropriate method of the listener, depending on the
	 * event type.
	 * @param listener The event listener
	 */
	public void fire(AbstractEditorListener listener)
	{
		if(!(listener instanceof EditorListener))
			return;

		EditorListener l = (EditorListener)listener;

		switch(id)
		{
		case BUFFER_CREATED:
			l.bufferCreated(this);
			break;
		case BUFFER_CLOSED:
			l.bufferClosed(this);
			break;
		case VIEW_CREATED:
			l.viewCreated(this);
			break;
		case VIEW_CLOSED:
			l.viewClosed(this);
			break;
		case BUFFER_DIRTY_CHANGED:
			l.bufferDirtyChanged(this);
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
			+ timestamp + ",view=" + view + ",buffer="
			+ buffer + "]";
	}

	// protected members
	protected View view;
	protected Buffer buffer;
}
