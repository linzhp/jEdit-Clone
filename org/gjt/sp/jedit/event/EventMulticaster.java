/*
 * EventMulticaster.java - Manages multiple event listeners
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

/**
 * A class whose instances can be used for firing editor events and
 * managing event listeners.<p>
 *
 * The <code>addListener()</code> and <code>removeListener()</code>
 * methods can be used to add and remove event listeners.
 * <code>fire()</code> will fire an event to all registered listeners
 * that can receive that event type. Several different listener types 
 * can be registered with each multicaster - events will only be
 * forwarded to those that understand that event type.
 *
 * @author Slava Pestov
 * @version $Id$
 */
public class EventMulticaster
{
	/**
	 * Creates a new <code>EventMulticaster</code>.
	 */
	public EventMulticaster()
	{
	}

	/**
	 * Adds an event listener to this multicaster.
	 * @param listener The event listener
	 */
	public void addListener(EventListener listener)
	{
		EventMulticaster mx = this;
		while(mx != null)
		{
			if(mx.listener == listener)
				return;
			mx = mx.next;
		}

		if(this.listener == null)
			this.listener = listener;
		else
			next = new EventMulticaster(listener,next);
	}	

	/**
	 * Removes an event listener from this multicaster.
	 * @param listener The event listener
	 */
	public void removeListener(EventListener listener)
	{
		EventMulticaster mx = this;
		EventMulticaster prev = null;

		while(mx != null)
		{
			if(mx.listener == listener)
			{
				if(prev != null)
					prev.next = mx.next;
				mx.next = null;
				mx.listener = null;
				return;
			}
			prev = mx;
			mx = mx.next;
		}
	}

	/**
	 * Forwards the specified event to all listeners that can receive
	 * it.
	 * @param evt The event
	 */
	public void fire(AbstractEditorEvent evt)
	{
		EventMulticaster mx = this;
		while(mx != null)
		{
			evt.fire(mx.listener);
			mx = mx.next;
		}
	}

	/**
	 * Returns a string representation of this event multicaster.
	 */
	public String toString()
	{
		return getClass().getName() + "[listener=" + listener +
			(next == null ? "]" : "]\n" + next.toString());
	}

	// private members
	private EventMulticaster next;
	private EventListener listener;

	private EventMulticaster(EventListener listener,
		EventMulticaster next)
	{
		this.listener = listener;
		this.next = next;
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.4  1999/04/24 01:55:28  sp
 * MiscUtilities.constructPath() bug fixed, event system bug(s) fix
 *
 * Revision 1.3  1999/03/16 04:34:46  sp
 * HistoryTextField updates, moved generate-text to a plugin, fixed spelling mistake in EditAction Javadocs
 *
 * Revision 1.2  1999/03/12 23:51:00  sp
 * Console updates, uncomment removed cos it's too buggy, cvs log tags added
 *
 */
