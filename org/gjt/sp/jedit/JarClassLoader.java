/*
 * JarClassLoader.java - Loads classes from JARs
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

import java.io.*;
import java.util.zip.*;
import java.util.Hashtable;

public class JarClassLoader extends ClassLoader
{
	JarClassLoader()
	{
		classes = new Hashtable();
	}
	
	public Class loadClass(String name, boolean resolveIt)
		throws ClassNotFoundException
	{
		Class clazz = findLoadedClass(name);
		if(clazz != null)
			return clazz;
		byte[] data = (byte[])classes.get(name);
		if(data == null)
			return findSystemClass(name);
		Class cls = defineClass(name,data,0,data.length);
		if(resolveIt)
			resolveClass(cls);
		return cls;
	}

	public String loadEntry(ZipFile jar, ZipEntry entry)
		throws IOException
	{
		InputStream in = jar.getInputStream(entry);
		String entryName = entry.getName();
		if(entryName.endsWith(".class"))
		{
			int len = (int)entry.getSize();
			byte[] cls = new byte[len];
			in.read(cls,0,len);
			String clsName = jEdit.fileToClass(entryName);
			classes.put(clsName,cls);
			return clsName;
		}
		else if(entryName.endsWith(".props"))
			jEdit.loadProps(in,entryName);
		in.close();
		return null;
	}

	private Hashtable classes;
}
