/*
 * AbstractEditorEvent.java - Abstract jEdit event
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
 * The superclass of all jEdit events.<p>
 *
 * Each event carries an event type value, which is an integer, and a
 * timestamp, which is the time the event was fired.
 *
 * @author Slava Pestov
 * @version $Id$
 */
public abstract class AbstractEditorEvent
{
	/**
	 * The first valid value of the `id' field.
	 */
	public static final int ID_FIRST = 0;

	/**
	 * Returns the event type. Each type of editor event has a
	 * unique event id, so that they can be compared without
	 * knowing the class of the event.
	 */
	public int getID()
	{
		return id;
	}

	/**
	 * Returns the view involved in this event. This might be
	 * null.
	 */
	public View getView()
	{
		return view;
	}

	/**
	 * Returns the buffer involved in this event. This might be
	 * null.
	 */
	public Buffer getBuffer()
	{
		return buffer;
	}

	/**
	 * Fires the event to the specified listener. This invokes
	 * the appropriate method of the listener, depending on the
	 * event type. If the listener is of the wrong type, it
	 * should be ignored. An exception should not be thrown.
	 * @param listener The event listener
	 */
	public abstract void fire(EventListener listener);

	/**
	 * Returns a string representation of this event.
	 */
	public String toString()
	{
		return getClass().getName() + "[id=" + id
			+ ",view=" + view + ",buffer="
			+ buffer + "]";
	}

	// protected members
	protected int id;
	protected View view;
	protected Buffer buffer;

	/**
	 * Creates a new <code>AbstractEditorEvent</code>.
	 * @param id The event type
	 * @param view The view involved
	 * @param buffer The buffer involved
	 */
	protected AbstractEditorEvent(int id, View view, Buffer buffer)
	{
		this.id = id;
		this.view = view;
		this.buffer = buffer;
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.6  1999/10/01 07:31:39  sp
 * RMI server replaced with socket-based server, minor changes
 *
 * Revision 1.5  1999/07/08 06:06:04  sp
 * Bug fixes and miscallaneous updates
 *
 * Revision 1.4  1999/03/16 04:34:46  sp
 * HistoryTextField updates, moved generate-text to a plugin, fixed spelling mistake in EditAction Javadocs
 *
 * Revision 1.3  1999/03/14 04:13:40  sp
 * Fixed ArrayIndexOutOfBounds in TokenMarker, minor Javadoc updates, minor documentation updates
 *
 * Revision 1.2  1999/03/12 23:51:00  sp
 * Console updates, uncomment removed cos it's too buggy, cvs log tags added
 *
 */
