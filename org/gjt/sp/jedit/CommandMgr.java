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

package org.gjt.sp.jedit;

import javax.swing.SwingUtilities;
import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * The command manager.
 * <p>
 * Only one instance of the command manager exists. It is stored in
 * <code>jEdit.cmds</code>.
 * <p>
 * The main function of the command manager is loading and executing commands.
 * jEdit's commands are similar to Swing's actions - but they are more
 * powerful. At the moment, commands can only be bound to menu items, but
 * more uses will be available in the future.
 * <p>
 * The command manager is used to call hooks. A hook is a list of objects
 * that can be invoked when necessary. The following hooks are defined by
 * jEdit:
 * <ul>
 * <li><code>post_startup</code> - called after jEdit has finished
 * starting up
 * <li><code>pre_<i>command</i></code> - called before <i>command</i>	
 * is executed
 * <li><code>post_<i>command</i></code> - called after <i>command</i>
 * is executed
 * </ul>
 * <p>
 * The command manager is also used to load edit modes.
 * <p>
 * Finally, the command manager loads plugins - commands and modes packaged
 * in a JAR file separately from jEdit.
 * @see Command
 * @see Mode
 */
public class CommandMgr extends ClassLoader
{
	// public members
	
	/**
	 * Loads all plugins from the specified directory.
	 * <p>
	 * This calls <code>loadPlugin()</code> for each JAR file in the
	 * specified directory.
	 * @param directory The directory to load the plugins from
	 */
	public void loadPlugins(String directory)
	{
		File file = new File(directory);
		if(!(file.exists() || file.isDirectory()))
			return;
		String[] plugins = file.list();
		if(plugins == null)
			return;
		for(int i = 0; i < plugins.length; i++)
			loadPlugin(directory + File.separator + plugins[i]);
	}

	/**
	 * Loads the specified plugin, showing a dialog box if an error
	 * occurs.
	 * <p>
	 * The plugin must be a JAR file. All class files from the plugin are
	 * loaded and all `.props' files are added to the default properties
	 * list.
	 * @param name The path name of the plugin
	 * @see PropsMgr#loadProps
	 */
	public void loadPlugin(String name)
	{
		try
		{
			_loadPlugin(name);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Object[] args = { name };
			jEdit.error(null,"loadpluginerr",args);
		}
		
	}
	
	/**
	 * Loads the specified plugin.
	 * <p>
	 * The plugin must be a JAR file. All class files from the plugin are
	 * loaded and all `.props' files are added to the default properties
	 * list.
	 * @param name The path name of the plugin
	 * @exception IOException if an I/O error occurs
	 * @exception ClassNotFoundException if the class could not be found
	 * @exception InstantiationException if a zero-argument constructor
	 * doesn't exist
	 * @exception IllegalAccessException if the constructor isn't public
	 */
	public void _loadPlugin(String name)
		throws IOException, ClassNotFoundException,
		InstantiationException, IllegalAccessException
	{
		if(!name.endsWith(".jar"))
			return;
		System.out.print("Loading plugin ");
		System.out.println(name);
		Vector classes = new Vector();
		ZipFile jar = new ZipFile(name);
		Enumeration entries = jar.entries();
		while(entries.hasMoreElements())
		{
			String cls = loadEntry(jar,(ZipEntry)entries
				.nextElement());
			if(cls != null)
				classes.addElement(cls);
		}
		Enumeration enum = classes.elements();
		while(enum.hasMoreElements())
		{
			String clsName = (String)enum.nextElement();
			if(clsName.startsWith("org.gjt.sp.jedit.cmd."))
			{
				Command cmd = getCommand(clsName
					.substring(21));
				if(cmd != null)
					plugins.addElement(cmd);
			}
			else if(clsName.startsWith("org.gjt.sp.jedit.mode."))
				initMode(clsName.substring(22));
		}
	}
	
	/**
	 * Returns a command.
	 * <p>
	 * This loads a command if necessary. If the command could not be
	 * found, it returns <code>null</code>.
	 * @param name The name of the command
	 * @return <code>null</code> if the command could not be found,
	 * the command otherwise
	 * @exception ClassNotFoundException if the class could not be found
	 * @exception InstantiationException if a zero-argument constructor
	 * doesn't exist
	 * @exception IllegalAccessException if the constructor isn't public
	 */
	public Command getCommand(String name)
		throws ClassNotFoundException, InstantiationException,
			IllegalAccessException
	{
		Command cmd = (Command)commands.get(name);
		if(cmd != null)
			return cmd;
		Class cls = loadClass("org.gjt.sp.jedit.cmd."
			.concat(name));
		if(Command.class.isAssignableFrom(cls))
		{
			Object obj = cls.newInstance();
			commands.put(name,obj);
			return (Command)obj;
		}
		return null;
	}
	
	/**
	 * Executes the specified command, throwing an exception if an error
	 * occurs.
	 * <p>
	 * This loads the command if necessary and calls it's
	 * <code>exec()</code> method.
	 * <p>
	 * If <code>cmd</code> contains an '@' character, any text after the
	 * '@' is passed to the command's <code>exec()</code> method.
	 * @param buffer The buffer to execute the command in
	 * @param view The view to execute the command in
	 * @param cmd The command to execute
	 * @exception ClassNotFoundException if the class could not be found
	 * @exception InstantiationException if a zero-argument constructor
	 * doesn't exist
	 * @exception IllegalAccessException if the constructor isn't public
	 */
	public void execCommand(Buffer buffer, View view, String cmd)
		throws ClassNotFoundException, InstantiationException,
			IllegalAccessException
	{
		int index = cmd.indexOf('@');
		String arg = null;
		if(index != -1)
		{
			arg = cmd.substring(index + 1);
			cmd = cmd.substring(0,index);
		}
		execHook(buffer,view,"pre_".concat(cmd));
		Command command = getCommand(cmd);
		command.exec(buffer,view,arg,null);
		execHook(buffer,view,"post_".concat(cmd));
	}

	/**
	 * Returns an enumeration of available plugins.
	 */
	public Enumeration getPlugins()
	{
		return plugins.elements();
	}

	/**
	 * Loads the specified mode, throwing an exception if an error
	 * occurs.
	 * @param name The name of the mode
	 * @exception ClassNotFoundException if the class could not be found
	 * @exception InstantiationException if a zero-argument constructor
	 * doesn't exist
	 * @exception IllegalAccessException if the constructor isn't public
	 */
	public void initMode(String name)
		throws ClassNotFoundException, InstantiationException,
			IllegalAccessException
	{
		if(getMode(name) != null)
			return;
		Class cls = loadClass("org.gjt.sp.jedit.mode."
			.concat(name));
		if(!Mode.class.isAssignableFrom(cls))
			return;
		Mode mode = (Mode)cls.newInstance();
		modes.addElement(mode);
	}
	
	/**
	 * Returns an instance of the edit mode with the specified name.
	 * <p>
	 * This loads the mode if necessary. It will return <code>null</code>
	 * if the mode could not be found.
	 * @param name The mode name
	 * @return <code>null</code> if the mode could not be found.
	 */
	public Mode getMode(String name)
	{
		if(name == null)
			return null;
		String clsName = "org.gjt.sp.jedit.mode.".concat(name);
		Enumeration enum = modes.elements();
		while(enum.hasMoreElements())
		{
			Mode mode = (Mode)enum.nextElement();
			if(mode.getClass().getName().equals(clsName))
				return mode;
		}
		return null;
	}
	
	/**
	 * Returns the localised name for the specified mode.
	 * @param mode The mode
	 */
	public String getModeName(Mode mode)
	{
		if(mode == null)
			return jEdit.props.getProperty("modename.none");
		else
			return jEdit.props.getProperty("modename.".concat(
				mode.getClass().getName().substring(22)));
	}

	/**
	 * Returns an enumeration of available edit modes.
	 */
	public Enumeration getModes()
	{
		return modes.elements();
	}
	
	/**
	 * Adds an object to a hook.
	 * @param name The name of the hook
	 * @param cmd The object to invoke, this can be either be a
	 * <code>Runnable</code>, <code>Command</code>, or command name.
	 * @exception IllegalArgumentException if cmd is neither a
	 * <code>Runnable</code>, <code>Command</code> or string.
	 */
	public void addHook(String name, Object cmd)
	{
		if(!(cmd instanceof Runnable || cmd instanceof Command
			|| cmd instanceof String))
			throw new IllegalArgumentException("addHook: cmd"
				+ " must be Runnable, Command, or String");
		Vector hook = (Vector)hooks.get(name);
		if(hook == null)
			hooks.put(name,hook = new Vector());
		hook.addElement(cmd);
	}

	/**
	 * Removes an object from a hook.
	 * @param name The name of the hook
	 * @param cmd The object to be removed
	 */
	public void removeHook(String name, Object cmd)
	{
		Vector hook = (Vector)hooks.get(name);
		if(hook != null)
			hook.removeElement(cmd);
	}

	/**
	 * Removes a hook.
	 * @param name The name of the hook
	 */
	public void removeHook(String name)
	{
		hooks.remove(name);
	}
	
	/**
	 * Executes all objects in a hook.
	 * <p>
	 * The <code>Runnable</code> objects have their run() method invoked,
	 * the <code>Command</code> objects have their exec() method invoked,
	 * and the strings are passed to <code>execCommand()</code>.
	 * @param buffer The buffer executing this hook
	 * @param view The view executing this hook
	 * @param name The name of the hook
	 * @exception ClassNotFoundException if the class could not be found
	 * @exception InstantiationException if a zero-argument constructor
	 * doesn't exist
	 * @exception IllegalAccessException if the constructor isn't public
	 */
	public void execHook(Buffer buffer, View view, String name)
		throws ClassNotFoundException, InstantiationException,
			IllegalAccessException
	{
		Vector hook = (Vector)hooks.get(name);
		if(hook == null)
			return;
		Enumeration enum = hook.elements();
		while(enum.hasMoreElements())
		{
			Object cmd = enum.nextElement();
			if(cmd instanceof Runnable)
				((Runnable)cmd).run();
			else if(cmd instanceof Command)
				((Command)cmd).exec(buffer,view,null,null);
			else if(cmd instanceof String)
				execCommand(buffer,view,(String)cmd);
			else // shouldn't happen
				throw new InternalError();
		}
	}

	/**
	 * Loads the specified class.
	 * <p>
	 * This method should not be invoked directly.
	 * @param name The class name to load
	 * @param resolveIt True if the class should be resolved
	 * @exception ClassNotFoundException if the specified class could not
	 * be found
	 */
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

	// package-private members
	CommandMgr()
	{
		classes = new Hashtable();
		commands = new Hashtable();
		plugins = new Vector();
		modes = new Vector();
		hooks = new Hashtable();
	}
	
	// private members
	private Hashtable classes;
	private Hashtable commands;
	private Vector plugins;
	private Vector modes;
	private Hashtable hooks;

	private String loadEntry(ZipFile jar, ZipEntry entry)
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
			jEdit.props.loadProps(in,entryName,true);
		in.close();
		return null;
	}
}
