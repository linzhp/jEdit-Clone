/*
 * JARClassLoader.java - Loads classes from JAR files
 * Copyright (C) 1999 Slava Pestov
 * Portions copyright (C) 1999 mike dillon
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
import java.util.*;
import java.util.zip.*;

/**
 * A class loader implementation that loads classes from JAR files.
 * @author Slava Pestov
 * @version $Id$
 */
public class JARClassLoader extends ClassLoader
{
	public JARClassLoader(String path)
		throws Exception, Exception
	{
		System.out.println("Loading plugin: " + path);

		zipFile = new ZipFile(path);

		// We defer the loading so that when plugins are started,
		// all properties are already present
		Vector pluginClasses = new Vector();

		System.out.println("----------------");
		Enumeration entires = zipFile.entries();
		while(entires.hasMoreElements())
		{
			ZipEntry entry = (ZipEntry)entires.nextElement();
			String name = entry.getName();
			System.out.print("  -- " + name + ": ");
			if(name.toLowerCase().endsWith(".props"))
			{
				System.out.println("loading properties file");
				jEdit.loadProps(zipFile.getInputStream(entry),
					name);
			}
			else if(name.toLowerCase().endsWith(".class"))
			{
				if(name.endsWith("Plugin.class"))
				{
					System.out.println("will load later");
					pluginClasses.addElement(name);
				}
				else
					System.out.println("skipping class");
			}
			else
			{
				System.out.println("skipping resource");
			}
		}

		System.out.println("----------------");

		// Do deferred loading
		Enumeration enum = pluginClasses.elements();
		while(enum.hasMoreElements())
		{
			loadPluginClass((String)enum.nextElement());
		}
	}

	public void loadPluginClass(String name)
		throws Exception
	{
		Class clazz = loadClass(MiscUtilities.fileToClass(name));
		if(Plugin.class.isAssignableFrom(clazz))
		{
			System.out.println("    registering plugin: " + clazz);
			Plugin plugin = (Plugin)clazz.newInstance();
			System.out.println("    " + plugin);
			jEdit.addPlugin(plugin);
		}
	}

	public Class loadClass(String clazz, boolean resolveIt)
		throws ClassNotFoundException
	{
		String name = MiscUtilities.classToFile(clazz);

		Class cls = findLoadedClass(name);
		if(cls != null)
		{
			System.out.println("Already loaded: " + name);
			return cls;
		}
		else
			System.out.println("Searching for entry: " + name);

		try
		{
			ZipEntry entry = zipFile.getEntry(name);

			// XXX: do dependency search in other JAR files
			if(entry == null)
			{
				System.out.println(clazz +
					" not found in JAR; looking in"
					+ " system classes");
				return findSystemClass(clazz);
			}

			InputStream in = zipFile.getInputStream(entry);

			int len = (int)entry.getSize();
			byte[] data = new byte[len];
			int success = 0;
			int offset = 0;
			while (success < len) {
				len -= success;
				offset += success;
				success = in.read(data,offset,len);
				if (success == -1)
				{
					System.err.println("Error loading class "
						+ clazz + " from "
						+ zipFile.getName());
					throw new ClassNotFoundException(clazz);
				}
			}
			
			System.out.println("Read " + data.length + " bytes");

			cls = defineClass(clazz,data,0,data.length);

			if(resolveIt)
				resolveClass(cls);

			return cls;
		}
		catch(IOException io)
		{
			System.err.println("I/O error:");
			io.printStackTrace();

			throw new ClassNotFoundException(clazz);
		}
	}

	// private members
	private ZipFile zipFile;
}

/*
public String loadEntry(ZipFile jar, ZipEntry entry)
			throws IOException
		{
			InputStream in = jar.getInputStream(entry);
			String entryName = entry.getName();
			if(entryName.endsWith(".class"))
			{
				int len = (int)entry.getSize();
				byte[] cls = new byte[len];
				int success = 0;
				int offset = 0;
				String clsName = MiscUtilities.fileToClass(entryName);
				while (success < len) {
					len -= success;
					offset += success;
					success = in.read(cls,offset,len);
					if (success == -1)
					{
						System.err.println("Error loading class "
							+ clsName + " from "
							+ jar.getName());
						return null;
					}
				}
				classes.put(clsName,cls);
				return clsName;
			}
			else if(entryName.endsWith(".props"))
				jEdit.loadProps(in,entryName);
			in.close();
			return null;
		}
*/

/*
 * ChangeLog:
 * $Log$
 * Revision 1.1  1999/03/21 07:53:14  sp
 * Plugin doc updates, action API change, new method in MiscUtilities, new class
 * loader, new plugin interface
 *
 */
