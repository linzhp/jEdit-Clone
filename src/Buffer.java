/*
 * Buffer.java - jEdit buffer
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
import com.sun.java.swing.event.DocumentEvent;
import com.sun.java.swing.event.DocumentListener;
import com.sun.java.swing.event.UndoableEditEvent;
import com.sun.java.swing.event.UndoableEditListener;
import com.sun.java.swing.preview.JFileChooser;
import com.sun.java.swing.text.BadLocationException;
import com.sun.java.swing.text.Element;
import com.sun.java.swing.text.PlainDocument;
import com.sun.java.swing.undo.UndoManager;
import java.awt.Graphics;
import java.awt.PrintJob;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Vector;

public class Buffer extends PlainDocument
implements DocumentListener, UndoableEditListener
{
	private File file;
	private File autosaveFile;
	private URL url;
	private String name;
	private String path;
	private boolean newFile;
	private boolean dirty;
	private UndoManager undo;
	private Vector markers;
	
	public Buffer(View view, String name, boolean load)
	{
		undo = new UndoManager();
		markers = new Vector();
		newFile = !load;
		setPath(name);
		if(load)
		{
			if(autosaveFile.exists())
			{
				Object[] args = { autosaveFile.getPath() };
				jEdit.message(view,"autosaveexists",args);
			}
			load(view);
		}
		addDocumentListener(this);
		addUndoableEditListener(this);
	}

	private void load(View view)
	{
		try
		{
			Reader in;
			URLConnection connection = null;
			if(url != null)
			{
				connection = url.openConnection();
				in = new InputStreamReader(
					connection.getInputStream());
			}
			else
			{
				in = new FileReader(file);
			}
			char[] buff = new char[4096];
			int n;
			while ((n = in.read(buff, 0, buff.length)) != -1)
			{
				insertString(getLength(),new String(buff,0,n),
					null);
			}
			in.close();
			newFile = false;
		}
		catch(FileNotFoundException fnf)
		{
			Object[] args = { path };
			jEdit.error(view,"notfounderror",args);
		}
		catch(Exception io)
		{
			Object[] args = { path, io.toString() };
			jEdit.error(view,"ioerror",args);
		}
		updateStatus();
	}

	public void autosave()
	{
		if(dirty)
		{
			try
			{
				save(new FileWriter(autosaveFile));
			}
			catch(Exception e)
			{
			}
		}
	}

	public boolean save(View view)
	{
		return save(view,null);
	}

	public boolean save(View view, String path)
	{
		if(path == null && !newFile)
			path = this.path;
		else if(newFile || "/as".equals(path))
		{
			JFileChooser fileChooser = new JFileChooser();
			if(view != null)
			{
				File fileN = view.getBuffer().getFile();
				String parent = fileN.getParent();
				if(parent != null)
					fileChooser.setCurrentDirectory(
						new File(parent));
				fileChooser.setSelectedFile(fileN);
			}
			fileChooser.setDialogTitle(jEdit.props
				.getProperty("savefile.title"));
			int retVal = fileChooser.showSaveDialog(view);
			if(retVal == JFileChooser.APPROVE_OPTION)
			{
				path = fileChooser.getSelectedFile().getPath();
				setPath(path);
			}
			else
				return false;
		}
		else if(path.equals("/url"))
		{
			path = (String)JOptionPane.showInputDialog(view,
				jEdit.props.getProperty("saveurl.message"),
				jEdit.props.getProperty("saveurl.title"),
				JOptionPane.QUESTION_MESSAGE,
				null,
				null,
				jEdit.props.getProperty("lasturl"));
			if(path != null)
			{
				jEdit.props.put("lasturl",path);
				setPath(path);
			}
		}
		backup();
		try
		{
			if(url != null)
			{
				URLConnection connection = url.openConnection();
				save(new OutputStreamWriter(connection
					.getOutputStream()));
			}
			else
				save(new FileWriter(file));
			dirty = newFile = false;
			autosaveFile.delete();
			updateStatus();
			return true;
		}
		catch(Exception e)
		{
			Object[] args = { getPath(), e.toString() };
			jEdit.error(view,"ioerror",args);
		}
		return false;
	}

	private void save(Writer out)
		throws Exception
	{
		String newline = jEdit.props.getProperty("line.separator",
			System.getProperty("line.separator"));
		BufferedReader in = new BufferedReader(new StringReader(
			getText(0,getLength())));
		String line;
		while((line = in.readLine()) != null)
		{
			out.write(line);
			out.write(newline);
		}
		out.close();
		in.close();
	}

	private void backup()
	{
		int backups;
		try
		{
			backups = Integer.parseInt(jEdit.props.getProperty(
				"backups"));
		}
		catch(NumberFormatException nf)
		{
			backups = 1;
		}
		if(backups == 0)
			return;
		File backup = null;
		for(int i = backups; i > 0; i--)
		{
			backup = new File(path + (backups == 1 ?
				"~" : "~" + i + "~"));
			if(backup.exists())
			{
				if(i == backups)
					backup.delete();
				else
					backup.renameTo(new File(name + "~"
						+ (i + 1) + "~"));
			}
		}
		file.renameTo(backup);
	}
	
	private void updateStatus()
	{
		Enumeration enum = jEdit.buffers.getViews();
		while(enum.hasMoreElements())
		{
			View view = (View)enum.nextElement();
			if(view.getBuffer() == this)
				view.updateStatus(true);
		}
	}

	public void print(View view)
	{
		PrintJob job = view.getToolkit().getPrintJob(view,name,null);
		if(job == null)
			return;
		int topMargin;
		int leftMargin;
		int bottomMargin;
		int rightMargin;
		int ppi = job.getPageResolution();
		try
		{
			topMargin = (int)(Float.valueOf(jEdit.props
				.getProperty("margin.top")).floatValue()
				* ppi);
		}
		catch(NumberFormatException nf)
		{
			topMargin = ppi / 2;
		}
		try
		{
			leftMargin = (int)(Float.valueOf(jEdit.props
				.getProperty("margin.left")).floatValue()
				* ppi);
		}
		catch(NumberFormatException nf)
		{
			leftMargin = ppi / 2;
		}
		try
		{
			bottomMargin = (int)(Float.valueOf(jEdit.props
				.getProperty("margin.bottom")).floatValue()
				* ppi);
		}
		catch(NumberFormatException nf)
		{
			bottomMargin = topMargin;
		}
		try
		{
			rightMargin = (int)(Float.valueOf(jEdit.props
				.getProperty("margin.right")).floatValue()
				* ppi);
		}
		catch(NumberFormatException nf)
		{
			rightMargin = leftMargin;
		}
		System.out.println("t=" + topMargin + ",l=" + leftMargin
			+ ",b=" + bottomMargin + ",r=" + rightMargin);
		job.end();
	}

	public File getFile()
	{
		return file;
	}

	public String getName()
	{
		return name;
	}

	public String getPath()
	{
		return path;
	}
	
	public void setPath(String path)
	{
		url = null;
		try
		{
			url = new URL(path);
			this.path = path;
			name = url.getFile();
			if(name.length() == 0)
				name = url.getHost();
			file = new File(name);
			name = file.getName();
		}
		catch(MalformedURLException mu)
		{
			file = new File(path);
			try
			{
				this.path = file.getCanonicalPath();
			}
			catch(IOException e)
			{
				this.path = path;
			}
			name = file.getName();
		}
		autosaveFile = new File(file.getParent(),'#' + name + '#');
	}
	
	public boolean isNewFile()
	{
		return newFile;
	}
	
	public boolean isDirty()
	{
		return dirty;
	}
	
	public UndoManager getUndo()
	{
		return undo;
	}

	public Enumeration getMarkers()
	{
		return markers.elements();
	}
	
	public void addMarker(String name, int start, int end)
		throws BadLocationException
	{
		Marker markerN = new Marker(name,createPosition(start),
			createPosition(end));
		for(int i = 0; i < markers.size(); i++)
		{
			Marker marker = (Marker)markers.elementAt(i);
			if(marker.getName().equals(name))
			{
				markers.removeElementAt(i);
				break;
			}
		}
		markers.addElement(markerN);
		updateMarkers();
	}

	public int[] getMarker(String name)
	{
		int[] retVal = new int[2];
		Enumeration enum = getMarkers();
		while(enum.hasMoreElements())
		{
			Marker marker = (Marker)enum.nextElement();
			if(marker.getName().equals(name))
			{
				retVal[0] = marker.getStart();
				retVal[1] = marker.getEnd();
				return retVal;
			}
		}
		return null;
	}

	public void removeMarker(String name)
	{
		for(int i = 0; i < markers.size(); i++)
		{
			Marker marker = (Marker)markers.elementAt(i);
			if(marker.getName().equals(name))
			{
				markers.removeElementAt(i);
				updateMarkers();
				return;
			}
		}
	}

	private void updateMarkers()
	{
		Enumeration enum = jEdit.buffers.getViews();
		while(enum.hasMoreElements())
		{
			View view = (View)enum.nextElement();
			if(view.getBuffer() == this)
				view.updateMarkerMenus();
		}
	}

	public void undoableEditHappened(UndoableEditEvent evt)
	{
		undo.addEdit(evt.getEdit());
	}

	public void insertUpdate(DocumentEvent evt)
	{
		if(!dirty)
		{
			dirty = true;
			updateStatus();
		}
	}

	public void removeUpdate(DocumentEvent evt)
	{
		if(!dirty)
		{
			dirty = true;
			updateStatus();
		}
	}

	public void changedUpdate(DocumentEvent evt)
	{
		if(!dirty)
		{
			dirty = true;
			updateStatus();
		}
	}
}
