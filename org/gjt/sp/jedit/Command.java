/*
 * Command.java - jEdit command
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

import java.util.Hashtable;

/**
 * A command. At the moment, commands can be bound to menu items. More uses
 * for them will be available in the future.
 * @see CommandMgr#getCommand
 * @see CommandMgr#execCommand
 */
public interface Command
{
	/**
	 * Executes the command.
	 * @param buffer The buffer to execute the command in
	 * @param view The view to execute the command in
	 * @param arg Text after the '@' character in the command name
	 * @param args Reserved for future use
	 */
	public abstract void exec(Buffer buffer, View view, String arg,
		Hashtable args);
}
