/*
 * BufferListener.java - Buffer event listener
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
 * The buffer event listener interface.
 *
 * @author Slava Pestov
 * @version $Id$
 */
public interface BufferListener extends AbstractEditorListener
{
	/**
	 * Method invoked when a buffer's dirty flag changes.
	 * This is invoked when a buffer is saved, or changed
	 * for the first time since the last save.
	 */
	public void bufferDirtyChanged(BufferEvent evt);

	/**
	 * Method invoked when a marker has been added or removed.
	 */
	public void bufferMarkersChanged(BufferEvent evt);

	/**
	 * Method invoked when a buffer's line separator has changed.
	 */
	public void bufferLineSepChanged(BufferEvent evt);

	/**
	 * Method invoked when a buffer's edit mode has changed.
	 */
	public void bufferModeChanged(BufferEvent evt);
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.3  1999/03/14 02:22:13  sp
 * Syntax colorizing tweaks, server bug fix
 *
 * Revision 1.2  1999/03/12 23:51:00  sp
 * Console updates, uncomment removed cos it's too buggy, cvs log tags added
 *
 */
