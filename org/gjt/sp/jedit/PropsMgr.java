/*
 * PropsMgr.java - jEdit property manager
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
import java.text.MessageFormat;
import java.util.Properties;

/**
 * The properties manager.
 * <p>
 * Only one instance of the properties manager exists. It is stored in
 * <code>jEdit.props</code>.
 * <p>
 * The properties manager is a subclass of <code>java.util.Properties</code>,
 * so it supports all methods defined there. In addition, it defines an
 * enhanced <code>getProperty</code> method, and methods for loading and
 * saving property files.
 * @see jEdit#props
 * @see #getProperty(String,Object[])
 * @see #loadProps(InputStream,String,boolean)
 * @see #saveProps(OutputStream,String)
 */
public class PropsMgr extends Properties
{
	// public members
	
	/**
	 * The path name of the user properties file.
	 */
	public static final String USER_PROPS = System.getProperty("user.home")
		+ File.separator + ".jedit-props";
	
	/**
	 * Loads properties.
	 * The properties are loaded from the specified input stream, or if
	 * it's null, from the file named <code>name</code>.
	 * @param in The input stream to load the properties from
	 * @param name The name of the file to load the properties from if
	 * <code>in</code> is null
	 * @return True if the properties were loaded, false if an error
	 * occured
	 */
	public boolean loadProps(InputStream in, String name, boolean defaults)
	{
		try
		{
			_loadProps(in,name,defaults);
			return true;
		}
		catch(FileNotFoundException fnf)
		{
		}
		catch(IOException io)
		{
			System.err.println("Error while loading properties:");
			io.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Loads properties, throwing an exception if an error occurs.
	 * The properties are loaded from the specified input stream, or if
	 * it's null, from the file named <code>name</code>.
	 * @param in The input stream to load the properties from
	 * @param name The name of the file to load the properties from if
	 * <code>in</code> is null
	 */
	public void _loadProps(InputStream in, String name, boolean defaults)
		throws FileNotFoundException, IOException
	{
		if(in == null)
			in = new FileInputStream(name);
		if(defaults)
		{
			if(this.defaults == null)
				this.defaults = new Properties();
			this.defaults.load(in);
		}
		else
			load(in);
		in.close();
	}
	
	/**
	 * Returns a property.
	 * The <code>MessageFormat</code> class is used to format the message
	 * with <code>args</code> as the positional parameters.
	 * @param name The name of the property
	 * @param args The positional parameters
	 */
	public String getProperty(String name, Object[] args)
	{
		if(name == null)
			return null;
		if(args == null)
			return getProperty(name,name);
		else
			return MessageFormat.format(getProperty(name,name),
				args);
	}

	/**
	 * Saves properties not in the defaults list.
	 * The properties are saved to the specified output stream, or if
	 * it's null, to the file named <code>name</code>.
	 * @param out The output stream to save the properties to
	 * @param name The name of the file to save the properties to if
	 * <code>out</code> is null
	 * @return True if the properties were saved, false if an error
	 * occured
	 */
	public boolean saveProps(OutputStream out, String name)
	{
		try
		{
			if(out == null)
				out = new FileOutputStream(name);

			save(out,"jEdit properties file");
			out.close();
			return true;
		}
		catch(IOException io)
		{
			io.printStackTrace();
		}

		return false;
	}

	// package-private methods
	PropsMgr() {}
	
	void loadSystemProps()
	{
		if(loadProps(null,jEdit.getJEditHome() + File.separator
			+ "jedit.props",true))
			return;
		System.err.println(">> ERROR LOADING SYSTEM PROPERTIES <<\n"
			+ "The jEdit system properties file `jedit.props'\n"
			+ "could not be loaded. Try reinstalling jEdit.");
		System.exit(1);
	}

	void loadUserProps()
	{
		loadProps(null,USER_PROPS,false);
	}
	
	boolean saveUserProps()
	{
		return saveProps(null,USER_PROPS);
	}
}
