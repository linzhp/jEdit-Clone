/*
 * PluginListURLConnection.java - jEdit plugin list URL connection
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

package org.gjt.sp.jedit.proto.jeditplugins;

import java.io.*;
import java.net.*;
import java.text.MessageFormat;
import org.gjt.sp.jedit.*;

public class PluginListURLConnection extends URLConnection
{
	public PluginListURLConnection(URL url)
	{
		super(url);
		
		StringBuffer buf = new StringBuffer();

		String pluginEntry = jEdit.getProperty("pluginlist.entry");

		buf.append(jEdit.getProperty("pluginlist.header"));

		String[] args = new String[4];
		Plugin[] plugins = jEdit.getPlugins();
		for(int i = 0; i < plugins.length; i++)
		{
			Plugin plugin = plugins[i];
			String clazz = plugin.getClass().getName();

			args[0] = jEdit.getProperty("plugin." + clazz
				+ ".name");
			if(args[0] == null)
				continue;

			args[1] = jEdit.getProperty("plugin." + clazz
				+ ".author");
			if(args[1] == null)
				continue;

			args[2] = jEdit.getProperty("plugin." + clazz
				+ ".version");
			if(args[2] == null)
				continue;

			String docs = jEdit.getProperty("plugin."
				+ clazz + ".docs");
			if(docs == null)
				continue;
			JARClassLoader classLoader = (JARClassLoader)
				plugin.getClass().getClassLoader();
			args[3] = classLoader.getResourceAsPath(docs);
			buf.append(MessageFormat.format(
				pluginEntry,args));
		}

		buf.append(jEdit.getProperty("pluginlist.footer"));

		in = new StringBufferInputStream(buf.toString());
	}

	public void connect() {}

	public InputStream getInputStream()
	{
		return in;
	}

	public String getHeaderField(String name)
	{
		/* Gross hack but so is java.net.URLConnection */
		if(name.equals("content-type"))
			return "text/html";
		else
			return null;
	}
	
	// private members
	private StringBufferInputStream in;
}
