/*
 * Plugin.java - Interface all plugins must implement
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

package org.gjt.sp.jedit;

/**
 * The interface between a plugin and jEdit. The job of this interface is
 * to register any actions and modes provided with the plugin, and do
 * initial setup required at start up time.<p>
 *
 * Due to the way jEdit loads classes, Plugin implementation class names
 * must end with `Plugin', otherwise they will be ignored.<p>
 *
 * As of jEdit 1.5, EditActions and Modes aren't automatically loaded from
 * plugin JAR files on startup; it is the responsibility of the plugin
 * class implementation to do so.<p>
 *
 * @author Slava Pestov
 * @version $Id$
 */
public interface Plugin
{
	/**
	 * Returns the plugin's internal name, which is used to fetch
	 * several properties.
	 */
	public String getName();

	/**
	 * Called at start up time when the plugin is being loaded.
	 */
	public void start();

	/**
	 * Called when jEdit is exiting.
	 */
	public void stop();
}
