/*
 * BufferMgr.java - jEdit buffer manager
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

import com.sun.java.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

public class BufferMgr
{
	private int untitledCount;
	private Vector buffers;
	private Vector views;
	private Vector recent;
	private int maxRecent;
	
	public BufferMgr()
	{
		buffers = new Vector();
		views = new Vector();
		recent = new Vector();
	}

	public void loadRecent()
	{
		try
		{
			maxRecent = Integer.parseInt(jEdit.props
				.getProperty("maxrecent"));
		}
		catch(NumberFormatException ex)
		{
			maxRecent = 8;
		}
		for(int i = 0; i < maxRecent; i++)
		{
			String file = jEdit.props.getProperty("recent." + i);
			if(file != null)
				recent.addElement(file);
		}
	}

	public void saveRecent()
	{
		for(int i = 0; i < recent.size(); i++)
		{
			String file = (String)recent.elementAt(i);
			jEdit.props.put("recent." + i,file);
		}
		jEdit.props.remove("recent." + maxRecent);
	}
	
	public void openBuffers(String[] files)
	{
		boolean opened = false;
		for(int i = 0; i < files.length; i++)
		{
			if(files[i] == null)
				continue;
			opened = true;
			openBuffer(files[i]);
		}
		if(!opened)
			newBuffer();
		newView(null);
	}
	
	public Buffer openBuffer(String name)
	{
		return openBuffer(null,name,true);
	}
	
	public Buffer openBuffer(View view, String name)
	{
		return openBuffer(view,name,true);
	}

	public Buffer openBuffer(View view, String name, boolean load)
	{
		String path = name;
		try
		{
			path = new File(name).getCanonicalPath();
		}
		catch(IOException io)
		{
		}
		Enumeration enum = getBuffers();
		while(enum.hasMoreElements())
		{
			Buffer buffer = (Buffer)enum.nextElement();
			if(buffer.getPath().equals(path))
			{
				if(view != null)
					view.setBuffer(buffer);
				return buffer;
			}
		}
		if(load)
		{
			if(!recent.contains(name))
			{
				recent.addElement(name);
				if(recent.size() > maxRecent)
					recent.removeElementAt(0);
			}
		}
		Buffer buffer = new Buffer(view,name,load);
		if(view != null)
			view.setBuffer(buffer);
		buffers.addElement(buffer);
		enum = getViews();
		while(enum.hasMoreElements())
		{
			view = (View)enum.nextElement();
			view.updateBuffersMenu();
			view.updateOpenRecentMenu();
		}
		return buffer;
	}

	public Buffer newBuffer()
	{
		return newBuffer(null);
	}
	
	public Buffer newBuffer(View view)
	{
		Object[] args = { new Integer(++untitledCount) };
		return openBuffer(view,jEdit.props.getProperty("untitled",
			args),false);
	}

	public boolean closeBuffer(View view, Buffer buffer)
	{
		if(buffer.isDirty())
		{
			Object[] args = { buffer.getName() };
			int result = JOptionPane.showConfirmDialog(view,
				jEdit.props.getProperty("notsaved.message",
				args),
				jEdit.props.getProperty("notsaved.title"),
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE);
			if(result == JOptionPane.YES_OPTION)
			// COLOSTOMY BAG!!!
			// should incorporate Cmd_save functionality into
			// Buffer.save()
				jEdit.cmds.execCommand(view,"save");
			else if(result == JOptionPane.CANCEL_OPTION)
				return false;
		}	
		
		buffers.removeElement(buffer);
		if(buffers.isEmpty())
			jEdit.exit(view);
		Enumeration enum = getViews();
		while(enum.hasMoreElements())
		{
			view = (View)enum.nextElement();
			view.updateBuffersMenu();
			if(view.getBuffer() == buffer)
				view.setBuffer(null);
		}
		return true;
	}

	public boolean closeAll(View view)
	{
		for(int i = buffers.size() - 1; i >= 0; i--)	
		{
			if(!closeBuffer(view,(Buffer)buffers.elementAt(i)))
				return false;
		}
		return true;
	}
	
	public Buffer getBuffer(String name)
	{
		Enumeration enum = getBuffers();
		while(enum.hasMoreElements())
		{
			Buffer buffer = (Buffer)enum.nextElement();
			if(buffer.getPath().equals(name))
				return buffer;
		}
		return null;
	}
	
	public Buffer getBufferAt(int index)
	{
		if(buffers.size() <= index)
			return null;
		else
			return (Buffer)buffers.elementAt(index);
	}
	
	public Enumeration getBuffers()
	{
		return buffers.elements();
	}

	public View newView()
	{
		return newView(null);
	}
	
	public View newView(View view)
	{
		View viewN = new View(view);
		views.addElement(viewN);
		return viewN;
	}

	public void closeView(View view)
	{
		if(views.size() == 1)
			jEdit.exit(view);
		else
		{
			view.dispose();
			views.removeElement(view);
			if(views.isEmpty())
				jEdit.exit(view);
		}
	}

	public Enumeration getViews()
	{
		return views.elements();
	}

	public Enumeration getRecent()
	{
		return recent.elements();
	}
}
