/*
 * Cmd_about.java - Command
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

import java.util.Hashtable;

public class Cmd_about implements Command
{
	public void exec(Buffer buffer, View view, String arg, Hashtable args)
	{
		Object[] aboutArgs = { jEdit.VERSION, jEdit.BUILD,
			System.getProperty("java.version"),
			System.getProperty("os.name"),
			System.getProperty("os.version"),
			System.getProperty("os.arch"),
			new Long(Runtime.getRuntime().freeMemory() / 1024),
			new Long(Runtime.getRuntime().totalMemory() / 1024) };
		jEdit.message(view,"about",aboutArgs);
	}
}
