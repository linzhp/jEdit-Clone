/*
 * BrowserEngine.java - Low level browser stuff
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

import java.util.Hashtable;

public class BrowserEngine
{
	public static void openNotify(Buffer buffer)
	{
		System.out.println("BrowserEngine::openNotify: " + buffer);
		BFile file = (BFile)files.get(buffer.getPath());
		if(file != null)
			file.buffer = buffer;
	}

	public static void saveNotify(Buffer buffer)
	{
		System.out.println("BrowserEngine::saveNotify: " + buffer);
		BFile file = (BFile)files.get(buffer.getPath());
		if(file != null)
			file.path = buffer.getPath();
	}

	public static void closeNotify(Buffer buffer)
	{
		System.out.println("BrowserEngine::closeNotify: " + buffer);
		BFile file = (BFile)files.get(buffer.getPath());
		if(file != null)
			file.buffer = null;
	}

	// private members
	private static final Hashtable files = new Hashtable();

	private BrowserEngine() {}

	private class BFile
	{
		BFile(String path)
		{
			this.path = path;
			buffer = jEdit.getBuffer(path);
		}

		String path;
		Buffer buffer;
		BTopLevel topLevel;
	}

	private class BTopLevel
	{
		BTopLevel(String name, int lineNo)
		{
			this.name = name;
			this.lineNo = lineNo;
		}

		String name;
		int lineNo;
		BMember members;
	}

	private class BMember
	{
		BMember(String name, int lineNo)
		{
			this.name = name;
			this.lineNo = lineNo;
		}

		String name;
		int lineNo;
		BReference references;
	}

	private class BReference
	{
		BReference(BFile file, BTopLevel topLevel, BMember member,
			int lineNo)
		{
			this.file = file;
			this.topLevel = topLevel;
			this.member = member;
			this.lineNo = lineNo;
		}

		BFile file;
		BTopLevel topLevel;
		BMember member;
		int lineNo;
	}
}
