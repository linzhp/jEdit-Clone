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
import com.sun.java.swing.preview.JFileChooser;
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
		catch(NumberFormatException nf)
		{
			maxRecent = 8;
		}
		recent.removeAllElements();
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

	public int getMaxRecent()
	{
		return maxRecent;
	}

	public void openFiles(String[] files, boolean readOnly)
	{
		boolean opened = false;
		for(int i = 0; i < files.length; i++)
		{
			if(files[i] == null)
				continue;
			opened = true;
			openFile(files[i],readOnly);
		}
		if(!opened)
			newFile();
		newView(null);
	}

	public Buffer openURL(View view)
	{
		String path = (String)JOptionPane.showInputDialog(view,
			jEdit.props.getProperty("openurl.message"),
			jEdit.props.getProperty("openurl.title"),
			JOptionPane.QUESTION_MESSAGE,
			null,
			null,
			jEdit.props.getProperty("lasturl"));
		if(path == null)
			return null;
		jEdit.props.put("lasturl",path);
		return openFile(view,null,path,false,true);
	}
	
	public Buffer openFile(View view)
	{
		JFileChooser fileChooser = new JFileChooser();
		if(view != null)
		{
			String parent = view.getBuffer().getFile().getParent();
			if(parent != null)
				fileChooser.setCurrentDirectory(
					new File(parent));
		}
		fileChooser.setDialogTitle(jEdit.props
			.getProperty("openfile.title"));
		int retVal = fileChooser.showOpenDialog(view);
		if(retVal == JFileChooser.APPROVE_OPTION)
			return openFile(view,null,fileChooser.getSelectedFile()
				.getPath(),false,true);
		else
			return null;
	}

	public Buffer openFile(String path)
	{
		return openFile(null,null,path,false,true);
	}
	
	public Buffer openFile(String path, boolean readOnly)
	{
		return openFile(null,null,path,readOnly,true);
	}
	
	public Buffer openFile(View view, String path)
	{
		return openFile(view,null,path,false,true);
	}

	public Buffer openFile(View view, String path, boolean readOnly,
		boolean load)
	{
		return openFile(view,null,path,readOnly,load);
	}
	
	public Buffer openFile(View view, String parent, String path,
		boolean readOnly, boolean load)
	{
		if(view != null && parent == null)
			parent = view.getBuffer().getFile().getParent();
		int index = path.indexOf('#');
		String marker = null;
		if(index != -1)
		{
			marker = path.substring(index + 1);
			path = path.substring(0,index);
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
		Buffer buffer = new Buffer(parent,path,readOnly,load);
		if(load)
		{
			path = buffer.getPath();
			if(!recent.contains(path))
			{
				recent.addElement(path);
				if(recent.size() > maxRecent)
					recent.removeElementAt(0);
			}
		}
		if(view != null)
		{
			view.setBuffer(buffer);
			if(marker != null)
			{
				int[] pos = buffer.getMarker(marker);
				if(pos != null)
					view.getTextArea().select(pos[0],
						pos[1]);
			}
		}
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

	public Buffer newFile()
	{
		return newFile(null);
	}
	
	public Buffer newFile(View view)
	{
		Object[] args = { new Integer(++untitledCount) };
		return openFile(view,jEdit.props.getProperty("untitled",
			args),false,false);
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
			{
				if(!buffer.save(view))
					return false;
			}
			else if(result == JOptionPane.NO_OPTION)
				buffer.getAutosaveFile().delete();
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
	
	public Buffer getBuffer(String path)
	{
		Enumeration enum = getBuffers();
		while(enum.hasMoreElements())
		{
			Buffer buffer = (Buffer)enum.nextElement();
			if(buffer.getPath().equals(path))
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
