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
import java.net.*;
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
		throws IOException
	{
		classLoaders.addElement(this);

		System.out.println("Scanning JAR file: " + path);

		zipFile = new ZipFile(path);

		Enumeration entires = zipFile.entries();
		while(entires.hasMoreElements())
		{
			ZipEntry entry = (ZipEntry)entires.nextElement();
			String name = entry.getName();
			if(name.toLowerCase().endsWith(".props"))
			{
				jEdit.loadProps(zipFile.getInputStream(entry));
			}
			else if(name.toLowerCase().endsWith(".class"))
			{
				if(name.endsWith("Plugin.class"))
					pluginClasses.addElement(name);
			}
		}
	}

	public void loadPluginClass(String name)
		throws Exception
	{
		Class clazz = loadClass(MiscUtilities.fileToClass(name),false);
		if(Plugin.class.isAssignableFrom(clazz))
		{
			Plugin plugin = (Plugin)clazz.newInstance();
			System.out.println(" -- loaded plugin: " +
				clazz.getName());
			jEdit.addPlugin(plugin);
		}
	}

	public Class loadClass(String clazz, boolean resolveIt)
		throws ClassNotFoundException
	{
		return loadClassFromZip(clazz,resolveIt,true);
	}

	public InputStream getResourceAsStream(String name)
	{
		try
		{
			ZipEntry entry = zipFile.getEntry(name);
			if(entry == null)
				return getSystemResourceAsStream(name);
			else
				return zipFile.getInputStream(entry);
		}
		catch(IOException io)
		{
			System.err.println("I/O error:");
			io.printStackTrace();

			return null;
		}
	}

	public URL getResource(String name)
	{
		try
		{
			return new URL(getResourceAsPath(name));
		}
		catch(MalformedURLException mu)
		{
			return null;
		}
	}

	public String getResourceAsPath(String name)
	{
		return "jeditresource:" + MiscUtilities.fileToClass(
			(String)pluginClasses.elementAt(0)) + "/" + name;
	}

	public static void initPlugins()
	{
		for(int i = 0; i < classLoaders.size(); i++)
		{
			JARClassLoader classLoader = (JARClassLoader)
				classLoaders.elementAt(i);
			classLoader.loadAllPlugins();
		}
	}

	// private members

	/* Loading of plugin classes is deferred until all JARs
	 * are loaded - this is necessary because a plugin might
	 * depend on classes stored in other JARs. */
	private static Vector classLoaders = new Vector();
	private Vector pluginClasses = new Vector();

	private ZipFile zipFile;

	private void loadAllPlugins()
	{
		for(int i = 0; i < pluginClasses.size(); i++)
		{
			String name = (String)pluginClasses.elementAt(i);
			try
			{
				loadPluginClass(name);
			}
			catch(Throwable t)
			{
				System.out.println(" -- error initializing"
					+ " plugin: " + name);
				t.printStackTrace();
			}
		}
	}

	private Class findOtherClass(String clazz, boolean resolveIt)
		throws ClassNotFoundException
	{
		for(int i = 0; i < classLoaders.size(); i++)
		{
			JARClassLoader loader = (JARClassLoader)
				classLoaders.elementAt(i);
			Class cls = loader.loadClassFromZip(clazz,resolveIt,
				false);
			if(cls != null)
				return cls;
		}

		/* Doesn't exist in any other plugin, look in system classes */
		return findSystemClass(clazz);
	}

	private Class loadClassFromZip(String clazz, boolean resolveIt,
		boolean doDepencies)
		throws ClassNotFoundException
	{
		String name = MiscUtilities.classToFile(clazz);

		Class cls = findLoadedClass(name);
		if(cls != null)
		{
			if(resolveIt)
				resolveClass(cls);
			return cls;
		}

		try
		{
			ZipEntry entry = zipFile.getEntry(name);

			if(entry == null)
			{
				if(doDepencies)
					return findOtherClass(clazz,resolveIt);
				else
					return null;
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
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.6  1999/05/06 07:16:14  sp
 * Plugins can use classes from other loaded plugins
 *
 * Revision 1.5  1999/04/27 06:53:38  sp
 * JARClassLoader updates, shell script token marker update, token marker compiles
 * now
 *
 * Revision 1.4  1999/04/19 05:47:35  sp
 * ladies and gentlemen, 1.6pre1
 *
 * Revision 1.3  1999/04/02 00:39:19  sp
 * Fixed console bug, syntax API changes, minor jEdit.java API change
 *
 * Revision 1.2  1999/03/24 05:45:27  sp
 * Juha Lidfors' backup directory patch, removed debugging messages from various locations, documentation updates
 *
 * Revision 1.1  1999/03/21 07:53:14  sp
 * Plugin doc updates, action API change, new method in MiscUtilities, new class
 * loader, new plugin interface
 *
 */
