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
import com.sun.java.swing.JTextArea;
import com.sun.java.swing.event.DocumentEvent;
import com.sun.java.swing.event.DocumentListener;
import com.sun.java.swing.event.UndoableEditEvent;
import com.sun.java.swing.event.UndoableEditListener;
import com.sun.java.swing.text.BadLocationException;
import com.sun.java.swing.text.Element;
import com.sun.java.swing.text.PlainDocument;
import com.sun.java.swing.undo.UndoManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.PrintJob;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Buffer extends PlainDocument
implements DocumentListener, UndoableEditListener
{
	private File file;
	private File autosaveFile;
	private File markersFile;
	private URL url;
	private URL markersUrl;
	private String name;
	private String path;
	private boolean init;
	private boolean newFile;
	private boolean dirty;
	private boolean readOnly;
	private UndoManager undo;
	private Vector markers;
	
	public Buffer(String parent, String path, boolean readOnly,
		boolean newFile)
	{
		undo = new UndoManager();
		markers = new Vector();
		this.newFile = newFile;
		init = true;
		setPath(parent,path);
		this.readOnly = readOnly;
		if(!newFile)
		{
			this.readOnly |= !file.canWrite();
			if(autosaveFile.exists())
			{
				Object[] args = { autosaveFile.getPath() };
				jEdit.message(null,"autosaveexists",args);
			}
			load();
			loadMarkers();
		}
		addDocumentListener(this);
		addUndoableEditListener(this);
		updateStatus();
		updateMarkers();
		init = false;
	}

	private void load()
	{
		try
		{
			InputStream in;
			URLConnection connection = null;
			if(url != null)
				in = url.openStream();
			else
				in = new FileInputStream(file);
			if(name.endsWith(".gz"))
				in = new GZIPInputStream(in);
			byte[] buff = new byte[4096];
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
			jEdit.error(null,"notfounderror",args);
		}
		catch(IOException io)
		{
			Object[] args = { io.toString() };
			jEdit.error(null,"ioerror",args);
		}
		catch(BadLocationException bl)
		{
			Object[] args = { bl.toString() };
			jEdit.error(null,"error",args);
		}
	}

	private void loadMarkers()
	{
		try
		{
			InputStream in;
			if(url != null)
				in = markersUrl.openStream();
			else
				in = new FileInputStream(markersFile);
			StringBuffer buf = new StringBuffer();
			int c;
			boolean eof = false;
			String name = null;
			int start = -1;
			int end = -1;
			for(;;)
			{
				if(eof)
					break;
				switch(c = in.read())
				{
				case -1:
					eof = true;
				case ';': case '\n': case '\r':
					if(buf.length() == 0)
						continue;
					String str = buf.toString();
					buf.setLength(0);
					if(name == null)
						name = str;
					else if(start == -1)
					{
						try
						{
							start = Integer
								.parseInt(str);
						}
						catch(NumberFormatException nf)
						{
							System.err.println(
								"Invalid"
								+ " start: "
								+ str);
							start = 0;
						}
					}
					else if(end == -1)
					{
						try
						{
							end = Integer
								.parseInt(str);
						}
						catch(NumberFormatException nf)
						{
							System.err.println(
								"Invalid"
								+ " end: "
								+ str);
							end = 0;
						}
						addMarker(name,start,end);
						name = null;
						start = -1;
						end = -1;
					}
					break;
				default:
					buf.append((char)c);
					break;
				}
			}
			in.close();
		}
		catch(Exception e)
		{
		}
	}

	public void find(View view)
	{
		new SearchAndReplace(view);
	}
	
	public boolean find(View view, String str, boolean done)
	{
		try
		{
			boolean ignoreCase = "on".equals(jEdit.props
				.getProperty("ignoreCase"));
			if(ignoreCase)
				str = str.toLowerCase();
			char[] pattern = str.toCharArray();
			Element map = getDefaultRootElement();
			int lines = map.getElementCount();
			int caret = view.getTextArea().getSelectionEnd();
			int startLine = map.getElementIndex(caret);
			int startOff = caret - map.getElement(startLine)
				.getStartOffset();
			for(int i = startLine; i < lines; i++)
			{
				Element lineElement = map.getElement(i);
				int start = lineElement.getStartOffset();
				String lineString = getText(start,lineElement
					.getEndOffset() - start);
				if(ignoreCase)
					lineString = lineString.toLowerCase();
				char[] line = lineString.toCharArray();
				int offset = jEdit.find(pattern,line,startOff);
				if(offset != -1)
				{
					offset += start;
					view.getTextArea().select(offset,
						offset + pattern.length);
					return true;
				}
				startOff = 0;
			}
		}
		catch(BadLocationException bl)
		{
			Object[] args = { bl.toString() };
			jEdit.error(view,"error",args);
		}
		if(done)
		{
			view.getToolkit().beep();
			return false;
		}
		int result = JOptionPane.showConfirmDialog(view,
			jEdit.props.getProperty("keepsearching.message"),
			jEdit.props.getProperty("keepsearching.title"),
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE);
		if(result == JOptionPane.YES_OPTION)
		{
			view.getTextArea().setCaretPosition(0);
			return find(view,str,true);
		}
		else
			return false;
	}

	public boolean findNext(View view)
	{
		String findStr = jEdit.props.getProperty("lastfind");
		if(findStr == null || "".equals(findStr))
		{
			view.getToolkit().beep();
			return false;
		}
		else
			return find(view,findStr,false);
	}

	public void replace(View view)
	{
		String replaceStr = jEdit.props.getProperty("lastreplace");
		if(replaceStr == null)
			view.getToolkit().beep();
		else
			view.getTextArea().replaceSelection(replaceStr);
	}

	public void replaceAll(View view)
	{
		String findStr = jEdit.props.getProperty("lastfind");
		String replaceStr = jEdit.props.getProperty("lastreplace");
		JTextArea textArea = view.getTextArea();
		if(findStr == null || replaceStr == null || "".equals(findStr))
		{
			view.getToolkit().beep();
			return;
		}
		else
		{
			while(find(view,findStr,false))
				textArea.replaceSelection(replaceStr);
		}
	}
	
	public void hypersearch(View view)
	{
		new HyperSearch(view);
	}
	
	public void wordCount(View view)
	{
		if(view != null)
		{
			String selection = view.getTextArea()
				.getSelectedText();
			if(selection != null)
			{
				wordCount(view,selection);
				return;
			}
		}
		try
		{
			wordCount(view,getText(0,getLength()));
		}
		catch(BadLocationException bl)
		{
		}
	}
	
	private void wordCount(View view, String text)
	{
		char[] chars = text.toCharArray();
		int characters = chars.length;
		int words;
		if(characters == 0)
			words = 0;
		else
			words = 1;
		int lines = 1;
		boolean word = false;
		for(int i = 0; i < chars.length; i++)
		{
			switch(chars[i])
			{
			case '\r': case '\n':
				lines++;
			case ' ': case '\t':
				if(word)
				{
					words++;
					word = false;
				}
				break;
			default:
				word = true;
				break;
			}
		}
		Object[] args = { new Integer(characters), new Integer(words),
			new Integer(lines) };
		jEdit.message(view,"word_count",args);
	}

	public void execute(View view)
	{
		String cmd = jEdit.input(view,"execute","lastcmd");
		if(cmd != null)
			execute(view,cmd);
	}

	public void execute(View view, String cmd)
	{
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < cmd.length(); i++)
		{
			switch(cmd.charAt(i))
			{
			case '%':
				if(i != cmd.length() - 1)
				{
					switch(cmd.charAt(++i))
					{
					case 'u':
						buf.append(path);
						break;
					case 'p':
						buf.append(file.getPath());
						break;
					default:
						buf.append('%');
						break;
					}
					break;
				}
			default:
				buf.append(cmd.charAt(i));
			}
		}
		try
		{
			Runtime.getRuntime().exec(buf.toString());
		}
		catch(IOException io)
		{
			Object[] error = { io.toString() };
			jEdit.error(view,"ioerror",error);
		}
	}

	public void send(View view)
	{
		new SendDialog(view);
	}

	public void autosave()
	{
		if(dirty)
		{
			try
			{
				save(new FileOutputStream(autosaveFile));
			}
			catch(Exception e)
			{
				System.err.println("Error during autosave:");
				e.printStackTrace();
			}
		}
	}
	
	public boolean save(View view)
	{
		return save(view,null);
	}
	
	public boolean saveAs(View view)
	{
		FileDialog fileDialog = new FileDialog(view,jEdit.props
			.getProperty("savefile.title"),FileDialog.LOAD);
		String parent = getFile().getParent();
		if(parent != null)
			fileDialog.setDirectory(parent);
		fileDialog.setFile(name);
		fileDialog.show();
		String file = fileDialog.getFile();
		if(file == null)
			return false;
		else
			return save(view,fileDialog.getDirectory() + file);
		
	}

	public boolean saveToURL(View view)
	{
		String path = jEdit.input(view,"saveurl","lasturl");
		if(path != null)
			return save(view,path);
		return false;
	}

	public boolean save(View view, String path)
	{
		if(path == null)
		{
			if(newFile)
				return saveAs(view);
			else
				path = this.path;
		}
		setPath(path);
		backup();
		try
		{
			OutputStream out;
			if(url != null)
			{
				URLConnection connection = url.openConnection();
				out = connection.getOutputStream();
			}
			else
				out = new FileOutputStream(file);
			if(name.endsWith(".gz"))
				out = new GZIPOutputStream(out);
			save(out);
			saveMarkers();
			dirty = newFile = readOnly = false;
			autosaveFile.delete();
			updateStatus();
			return true;
		}
		catch(IOException io)
		{
			Object[] args = { io.toString() };
			jEdit.error(view,"ioerror",args);
		}
		catch(Exception e)
		{
			Object[] args = { e.toString() };
			jEdit.error(view,"error",args);
		}
		return false;
	}
	
	private void save(OutputStream out)
		throws Exception
	{
		save(new OutputStreamWriter(out));
	}

	private void save(Writer out)
		throws Exception
	{
		String newline = jEdit.props.getProperty("line.separator",
			System.getProperty("line.separator"));
		Element map = getDefaultRootElement();
		for(int i = 0; i < map.getElementCount();)
		{
			Element line = map.getElement(i);
			int start = line.getStartOffset();
			out.write(getText(start,line.getEndOffset() - start
				- 1));
			if(++i != map.getElementCount())
				out.write(newline);
		}
		out.close();
	}
	
	private void saveMarkers()
		throws IOException
	{
		if(markers.isEmpty())
			return;
		OutputStream out;
		if(url != null)
		{
			URLConnection connection = markersUrl.openConnection();
			out = connection.getOutputStream();
		}
		else
			out = new FileOutputStream(markersFile);
		Writer o = new OutputStreamWriter(out);
		Enumeration enum = getMarkers();
		while(enum.hasMoreElements())
		{
			Marker marker = (Marker)enum.nextElement();
			o.write(marker.getName());
			o.write(';');
			o.write(String.valueOf(marker.getStart()));
			o.write(';');
			o.write(String.valueOf(marker.getEnd()));
			o.write('\n');
		}
		o.close();
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
		String header;
		if(url != null)
			header = "jEdit: " + url;
		else
			header = "jEdit: " + getPath();
		Element map = getDefaultRootElement();
		JTextArea textArea = view.getTextArea();
		Graphics gfx = null;
		Font font = textArea.getFont();
		int fontHeight = font.getSize();
		int tabSize = view.getTextArea().getTabSize();
		Dimension pageDimension = job.getPageDimension();
		int pageWidth = pageDimension.width;
		int pageHeight = pageDimension.height;
		int y = 0;
loop:		for(int i = 0; i < map.getElementCount(); i++)
		{
			if(gfx == null)
			{
				gfx = job.getGraphics();
				gfx.setFont(font);
				FontMetrics fm = gfx.getFontMetrics();
				gfx.setColor(Color.lightGray);
				gfx.fillRect(leftMargin,topMargin,pageWidth
					- leftMargin - rightMargin,
					  fm.getMaxAscent()
					  + fm.getMaxDescent()
					  + fm.getLeading());
				gfx.setColor(Color.black);
				y = topMargin + fontHeight;
				gfx.drawString(header,leftMargin,y);
				y += fontHeight;
			}
			Element line = map.getElement(i);
			try
			{
				int start = line.getStartOffset();
				gfx.drawString(jEdit.untab(tabSize,getText(
					start,line.getEndOffset() - start
					- 1)),leftMargin,y += fontHeight);
			}
			catch(BadLocationException bl)
			{
				Object [] arg = { bl.toString() };
				jEdit.error(view,"error",arg);
			}
			if((y > pageHeight - bottomMargin) ||
				(i == map.getElementCount() - 1))
			{
				gfx.dispose();
				gfx = null;
			}
		}
		job.end();
	}
	
	public File getFile()
	{
		return file;
	}

	public File getAutosaveFile()
	{
		return autosaveFile;
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
		setPath(null,path);
	}

	public void setPath(String parent, String path)
	{
		if(path.startsWith(File.separator))
			parent = null;
		else if(path.length() >= 3 && path.charAt(1) == ':'
			&& path.charAt(2) == '\\')
			parent = null;
		else if(parent == null)
			parent = System.getProperty("user.dir");
		url = null;
		try
		{
			url = new URL(path);
			name = url.getFile();
			if(name.length() == 0)
				name = url.getHost();
			if(name.startsWith(File.separator))
				parent = null;
			if(parent == null)
				file = new File(name);
			else
				file = new File(parent,name);
			System.out.println(name);
			markersUrl = new URL(url,'.' + new File(name)
				.getName() + ".marks");
			name = file.getName();
		}
		catch(MalformedURLException mu)
		{
			if(parent == null)
				file = new File(path);
			else
				file = new File(parent,path);
			try
			{
				this.path = file.getCanonicalPath();
			}
			catch(IOException io)
			{
				this.path = file.getPath();
			}
			name = file.getName();
			markersFile = new File(file.getParent(),'.' + name
				+ ".marks");
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
	
	public boolean isReadOnly()
	{
		return readOnly;
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
		if(readOnly && !init)
			throw new RuntimeException();
		dirty = !init;
		name = name.replace(';',' ');
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
		if(!init)
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
		if(readOnly)
			throw new RuntimeException();
		dirty = !init;
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
			{
				view.updateStatus(true);
				view.updateMarkerMenus();
			}
		}
	}

	private void dirty()
	{
		if(!(dirty || readOnly))
		{
			dirty = !init;
			updateStatus();
		}
	}
	
	public void undoableEditHappened(UndoableEditEvent evt)
	{
		undo.addEdit(evt.getEdit());
	}

	public void insertUpdate(DocumentEvent evt)
	{
		dirty();
	}

	public void removeUpdate(DocumentEvent evt)
	{
		dirty();
	}

	public void changedUpdate(DocumentEvent evt)
	{
		dirty();
	}
}
