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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Properties;

public class PropsMgr extends Properties
{
	private String usrProps;
	
	public PropsMgr()
	{
		usrProps = System.getProperty("user.home",".") + File.separator
			+ ".jedit-props";
	}
	
	public void loadSystemProps()
	{
		if(!loadProps(getClass().getResourceAsStream("/properties"),
			"properties",true))
			System.exit(1);
	}

	public void loadUserProps()
	{
		loadProps(null,usrProps,false);
	}
	
	public boolean loadProps(InputStream in, String name, boolean defaults)
	{
		try
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
			return true;
		}
		catch(FileNotFoundException fnf)
		{
		}
		catch(IOException io)
		{
			io.printStackTrace();
		}

		return false;
	}

	public String getProperty(String name, Object[] args)
	{
		if(args == null)
			return getProperty(name,name);
		else
			return MessageFormat.format(getProperty(name,name),
				args);
	}

	public String setDefault(String name, String value)
	{
		if(defaults == null)
			defaults = new Properties();
		return (String)defaults.put(name,value);
	}
	public boolean saveUserProps()
	{
		return saveProps(null,usrProps);
	}
	
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
}
