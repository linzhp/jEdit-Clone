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
	 * a buffer's dirty status has been changed. This event is fired
	 * when the buffer is saved, and the first time it is changed
	 * after a save.
	 */
	public static final int BUFFER_DIRTY_CHANGED = EDITOR_FIRST + 4;

	/**
	 * The return value of the <code>getID()</code> function when
	 * value of properties that might require settings to be
	 * reloaded have changed. At the moment, this is fired by
	 * the Options dialog box only.
	 */
	public static final int PROPERTIES_CHANGED = EDITOR_FIRST + 5;

	/**
	 * The last event id that denotes an editor event.
	 */
	public static final int EDITOR_LAST = EDITOR_FIRST + 5;

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
	 * Returns the buffer involved. This is set for all event types
	 * except PROPERTIES_CHANGED.
	 */
	public Buffer getBuffer()
	{
		return buffer;
	}

	/**
	 * Returns the view involved. This not set for some event
	 * types, namely BUFFER_DIRTY_CHANGED and PROPERTIES_CHANGED.
	 * Also, this is not set for BUFFER_CREATED events at
	 * startup, and BUFFER_CREATED events caused by the server.
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
		case PROPERTIES_CHANGED:
			l.propertiesChanged(this);
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
