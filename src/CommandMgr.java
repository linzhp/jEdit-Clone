/*
 * CommandMgr.java - jEdit command manager
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

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CommandMgr extends ClassLoader
{
	private Hashtable classes;
	private Hashtable commands;
	private Vector plugins;
	
	public CommandMgr()
	{
		classes = new Hashtable();
		commands = new Hashtable();
		plugins = new Vector();
	}

	public void loadPlugins(String directory)
	{
		File file = new File(directory);
		if(!(file.exists() || file.isDirectory()))
			return;
		String[] plugins = file.list();
		if(plugins == null)
			return;
		for(int i = 0; i < plugins.length; i++)
			loadJar(directory + File.separator + plugins[i]);
	}

	public void loadJar(String name)
	{
		if(!name.endsWith(".jar"))
			return;
		System.err.print("Loading plugin ");
		System.err.println(name);
		try
		{
			ZipFile jar = new ZipFile(name);
			Enumeration entries = jar.entries();
			while(entries.hasMoreElements())
				loadEntry(jar,(ZipEntry)entries.nextElement());
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	public void loadEntry(ZipFile jar, ZipEntry entry)
		throws IOException
	{
		InputStream in = jar.getInputStream(entry);
		String entryName = entry.getName();
		if(entryName.endsWith(".class"))
		{
			int len = (int)entry.getSize();
			byte[] cls = new byte[len];
			int c = 0;
			while(len >= 0)
			{
				in.read(cls,c,Math.min(1024,len));
				c += 1024;
				len -= 1024;
			}
			String clsName = fileToClass(entryName);
			classes.put(clsName,cls);
			if(clsName.startsWith("Cmd_"))
				plugins.addElement(getCommand(clsName
					.substring(4)));
		}
		else if(entryName.endsWith(".props"))
			jEdit.props.loadProps(in,entryName,true);
		in.close();
	}
	
	public String fileToClass(String name)
	{
		char[] clsName = name.toCharArray();
		for(int i = clsName.length - 1; i >= 0; i--)
			if(clsName[i] == '/')
				clsName[i] = '.';
		return new String(clsName,0,clsName.length - 6);
	}

	public Command getCommand(String name)
	{
		Command cmd = (Command)commands.get(name);
		if(cmd != null)
			return cmd;
		try
		{
			Class cls = loadClass("Cmd_".concat(name));
			if(Command.class.isAssignableFrom(cls))
			{
				Object obj = cls.newInstance();
				cmd = (Command)obj;
				Hashtable args = new Hashtable();
				cmd.init(args);
				commands.put(name,obj);
				return cmd;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public Object execCommand(View view, String cmd)
	{
		Hashtable args = new Hashtable();
		int index = cmd.indexOf('@');
		if(index != -1)
		{
			args.put(Command.ARG,cmd.substring(index + 1));
			cmd = cmd.substring(0,index);
		}
		args.put(Command.VIEW,view);
		Command command = getCommand(cmd);
		if(command == null)
			throw new IllegalArgumentException("Aiee!!! Invalid"
				+ " command " + cmd);
		return command.exec(args);
	}

	public Enumeration getPlugins()
	{
		return plugins.elements();
	}
	
	public Class loadClass(String name, boolean resolveIt)
		throws ClassNotFoundException
	{
		byte[] data = (byte[])classes.get(name);
		if(data == null)
			return findSystemClass(name);
		Class cls = defineClass(name,data,0,data.length);
		if(resolveIt)
			resolveClass(cls);
		return cls;
	}
}
