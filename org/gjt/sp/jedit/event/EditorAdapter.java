/*
 * EditorAdapter.java - Empty editor event listener that can be subclassed
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
 * An implementation of EditorListener with all empty methods. It can be
 * subclassed instead of EditorListener so that empty stubs for unused
 * methods are not necessary.
 */
public abstract class EditorAdapter extends AbstractEditorAdapter
implements EditorListener
{
	/**
	 * Method invoked when a buffer has been created.
	 */
	public void bufferCreated(EditorEvent evt) {}

	/**
	 * Method invoked when a buffer has been closed.
	 */
	public void bufferClosed(EditorEvent evt) {}

	/**
	 * Method invoked when a view has been created.
	 */
	public void viewCreated(EditorEvent evt) {}

	/**
	 * Method invoked when a view has been closed.
	 */
	public void viewClosed(EditorEvent evt) {}

	/**
	 * Method invoked when a buffer's dirty flag changes.
	 * This is invoked when a buffer is saved, or changed
	 * for the first time since the last save.
	 */
	public void bufferDirtyChanged(EditorEvent evt) {}

	/**
	 * Method invoked when the value of properties that
	 * might require settings to be reloaded are changed.
	 * This is invoked by the Options dialog box.
	 */
	public void propertiesChanged(EditorEvent evt) {}
}
