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

package org.gjt.sp.jedit;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * The buffer manager.
 * <p>
 * Only one instance of the buffer manager exists. It is stored in
 * <code>jEdit.buffers</code>.
 * <p>
 * The buffer manager opens and closes buffers and views.
 * @see Buffer
 * @see View
 */
public class BufferMgr
{
	// public members
	
	/**
	 * Prompts the user for a URL to open.
	 * @param view The view to display the dialog box for
	 */
	public Buffer openURL(View view)
	{
		String path = jEdit.input(view,"openurl","openurl.url");
		if(path == null)
			return null;
		return openFile(view,null,path,false,false);
	}
	
	/**
	 * Prompts the user for a file to open.
	 * @param view The view to display the dialog box for
	 */
	public Buffer openFile(View view)
	{
		JFileChooser chooser = new JFileChooser(view.getBuffer()
			.getFile().getParent());
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int retVal = chooser.showDialog(view,null);
		if(retVal == JFileChooser.APPROVE_OPTION)
		{
			File file = chooser.getSelectedFile();
			if(file != null)
				return openFile(view,null,file
					.getAbsolutePath(),false,false);
		}
		return null;
	}

	/**
	 * Opens the specified file.
	 * @param view The view to open the file in
	 * @param parent The directory containing this file
	 * @param path The path name of this file
	 * @param readOnly True if the file should be opened read only
	 * @param newFile True if the file shouldn't be loaded and marked
	 * untitled
	 */
	public Buffer openFile(View view, String parent, String path,
		boolean readOnly, boolean newFile)
	{
		if(view != null && parent == null)
			parent = view.getBuffer().getFile().getParent();
		int index = path.indexOf('#');
		String marker = null;
		if(index != -1)
		{
			marker = path.substring(index + 1);
			path = path.substring(0,index);
			if(path.length() == 0)
			{
				if(view == null)
					return null;
				Buffer buffer = view.getBuffer();
				gotoMarker(buffer,view,marker);
				return buffer;
			}
		}
		URL url = null;
		try
		{
			url = new URL(path);
		}
		catch(MalformedURLException mu)
		{
			path = jEdit.constructPath(parent,path);
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
		}
		Buffer buffer = new Buffer(url,path,readOnly,newFile);
		if(!newFile)
		{
			if(recent.contains(path))
				recent.removeElement(path);
			recent.insertElementAt(path,0);
			if(recent.size() > maxRecent)
				recent.removeElementAt(maxRecent);
		}
		if(view != null)
			view.setBuffer(buffer);
		buffers.addElement(buffer);
		Enumeration enum = getViews();
		while(enum.hasMoreElements())
		{
			View v = (View)enum.nextElement();
			v.updateBuffersMenu();
			v.updateOpenRecentMenu();
		}
		if(marker != null)
			gotoMarker(buffer,view,marker);
		return buffer;
	}

	/**
	 * Creates a new, untitled file.
	 * @param view The view to create the file in
	 */
	public Buffer newFile(View view)
	{
		Object[] args = { new Integer(++untitledCount) };
		return openFile(view,null,jEdit.props.getProperty("buffermgr."
			+ "untitled",args),false,true);
	}

	/**
	 * Closes an open buffer.
	 * @param view The view to display the confirmation dialog box in,
	 * if needed
	 * @param buffer The buffer to close
	 * @return True if the buffer was closed, false if the user cancelled
	 * the operation
	 */
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
				if(!buffer.save(view,null))
					return false;
			}
			else if(result == JOptionPane.NO_OPTION)
				buffer.getAutosaveFile().delete();
			else if(result == JOptionPane.CANCEL_OPTION)
				return false;
		}
		int index = buffers.indexOf(buffer);
		buffers.removeElement(buffer);
		if(buffers.isEmpty())
			jEdit.exit(view);
		Buffer prev = (Buffer)buffers.elementAt(Math.max(0,index - 1));
		Enumeration enum = getViews();
		while(enum.hasMoreElements())
		{
			view = (View)enum.nextElement();
			view.updateBuffersMenu();
			if(view.getBuffer() == buffer)
				view.setBuffer(prev);
		}
		return true;
	}

	/**
	 * Returns the buffer with the specified path name.
	 * @param path The path name of the buffer to return
	 */
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
	
	/**
	 * Returns all open buffers.
	 */
	public Enumeration getBuffers()
	{
		return buffers.elements();
	}

	/**
	 * Creates a new view.
	 * @param view The view to inherit the current buffer from
	 */
	public View newView(View view)
	{
		View viewN = new View(view);
		views.addElement(viewN);
		return viewN;
	}

	/**
	 * Closes the specified view.
	 * @param view The view to close
	 */
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

	/**
	 * Returns all open views.
	 */
	public Enumeration getViews()
	{
		return views.elements();
	}

	/**
	 * Returns a list of recently opened files.
	 */
	public Enumeration getRecent()
	{
		return recent.elements();
	}

	// package-private members
	BufferMgr()
	{
		buffers = new Vector();
		views = new Vector();
		recent = new Vector();
	}

	void loadRecent()
	{
		try
		{
			maxRecent = Integer.parseInt(jEdit.props
				.getProperty("buffermgr.recent"));
		}
		catch(NumberFormatException nf)
		{
			maxRecent = 8;
		}
		recent.removeAllElements();
		for(int i = 0; i < maxRecent; i++)
		{
			String file = jEdit.props.getProperty("buffermgr."
				+ "recent." + i);
			if(file != null)
				recent.addElement(file);
		}
	}

	void saveRecent()
	{
		for(int i = 0; i < recent.size(); i++)
		{
			String file = (String)recent.elementAt(i);
			jEdit.props.put("buffermgr.recent." + i,file);
		}
		jEdit.props.remove("buffermgr.recent." + maxRecent);
	}
	
	boolean closeAll(View view)
	{
		for(int i = buffers.size() - 1; i >= 0; i--)	
		{
			if(!closeBuffer(view,(Buffer)buffers.elementAt(i)))
				return false;
		}
		return true;
	}

	// private members
	private int untitledCount;
	private Vector buffers;
	private Vector views;
	private Vector recent;
	private int maxRecent;

	private void gotoMarker(final Buffer buffer, final View view,
		String marker)
	{
		final Marker m = buffer.getMarker(marker);
		if(m == null)
			return;
		if(view != null)
		{
			SwingUtilities.invokeLater(new Runnable() {
				public void run()
				{
					view.getTextArea().select(m.getStart(),
						m.getEnd());
				}
			});
		}
	}
}
