/*
 * Marker.java - Named location
 * Copyright (C) 1998 Slava Pestov
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

package org.gjt.sp.jedit;

import com.sun.java.swing.text.Position;

/**
 * A marker is a named location in a buffer.
 * <p>
 * There is no public constructor for this class. Markers are created by
 * <code>Buffer.addMarker()</code>.
 * @see Buffer#addMarker(String,int,int)
 * @see Buffer#getMarker(String)
 * @see Buffer#removeMarker(String)
 */
public class Marker
{
	// public members
	
	/**
	 * Returns the name of this marker.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Returns the start position of this marker.
	 */
	public int getStart()
	{
		return start.getOffset();
	}

	/**
	 * Returns the end position of this marker.
	 */
	public int getEnd()
	{
		return end.getOffset();
	}

	// package-private members
	Marker(String name, Position start, Position end)
	{
		this.name = name;
		this.start = start;
		this.end = end;
	}

	// private members
	private String name;
	private Position start;
	private Position end;
}
