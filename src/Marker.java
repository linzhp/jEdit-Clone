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

import com.sun.java.swing.text.Position;

public class Marker
{
	private String name;
	private Position start;
	private Position end;

	public Marker(String name, Position start, Position end)
	{
		this.name = name;
		this.start = start;
		this.end = end;
	}

	public String getName()
	{
		return name;
	}

	public int getStart()
	{
		return start.getOffset();
	}

	public int getEnd()
	{
		return end.getOffset();
	}
}
