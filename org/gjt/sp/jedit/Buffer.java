/*
 * Buffer.java - jEdit buffer
 * Copyright (C) 1998, 1999 Slava Pestov
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

import gnu.regexp.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import org.gjt.sp.jedit.event.*;
import org.gjt.sp.jedit.gui.JEditTextArea;
import org.gjt.sp.jedit.syntax.*;

/**
 * An in-memory copy of an open file.
 */
public class Buffer extends DefaultSyntaxDocument
{
	/**
	 * Size of I/O buffers.
	 */
	public static final int IOBUFSIZE = 32768;

	/**
	 * Line separator property.
	 */
	public static final String LINESEP = "lineSeparator";

	/**
	 * Reloads settings from the properties. This should be called
	 * after the <code>syntax</code> buffer-local property is
	 * changed.
	 */
	public void propertiesChanged()
	{
		syntaxColorizing = "on".equals(getProperty("syntax"));
	}

	/**
	 * Autosaves this buffer.
	 */
	public void autosave()
	{
		if(adirty)
		{
			try
			{
				File autosaveTmp = new File(autosaveFile
					.getPath().concat("+tmp+#"));
				save(new FileOutputStream(autosaveTmp));
				/* workaround for JDK 1.2 bug */
				autosaveFile.delete();
				autosaveTmp.renameTo(autosaveFile);
				/* XXX race alert if setDirty() runs here */
				adirty = false;
			}
			catch(FileNotFoundException fnf)
			{
				/* this could happen if eg, the directory
				 * containing this file was renamed.
				 * we ignore the error then so the user
				 * isn't flooded with exceptions. */
			}
			catch(Exception e)
			{
				System.err.println("Error during autosave:");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Prompts the user for a file to save this buffer to.
	 * @param view The view
	 */
	public boolean saveAs(View view)
	{
		JFileChooser chooser = new JFileChooser(file.getParent());
		chooser.setSelectedFile(file);
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int retVal = chooser.showDialog(view,null);
		if(retVal == JFileChooser.APPROVE_OPTION)
		{
			File file = chooser.getSelectedFile();
			if(file != null)
				return save(view,file.getAbsolutePath());
		}
		return false;
	}

	/**
	 * Saves this buffer to the specified path name, or the current path
	 * name if it's null.
	 * @param view The view
	 * @param path The path name to save the buffer to
	 */
	public boolean save(View view, String path)
	{
		if(path == null && newFile)
			return saveAs(view);
		URL saveUrl;
		File saveFile;
		if(path == null)
		{
			saveUrl = url;
			path = this.path;
			saveFile = file;
			// only do this check if saving to original file
			// if user is doing a `Save As' they should know
			// what they're doing anyway
			long newModTime = saveFile.lastModified();
			if(newModTime > modTime)
			{
				Object[] args = { path };
				int result = JOptionPane.showConfirmDialog(view,
					jEdit.getProperty("filechanged.message",
					args),jEdit.getProperty("filechanged.title"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
				if(result != JOptionPane.YES_OPTION)
					return false;
			}
		}
		else
		{
			try
			{
				saveUrl = new URL(path);
			}
			catch(MalformedURLException mu)
			{
				saveUrl = null;
			}
			saveFile = new File(path);
		}
		backup(saveFile);
		try
		{
			OutputStream out;
			if(saveUrl != null)
			{
				URLConnection connection = saveUrl
					.openConnection();
				out = connection.getOutputStream();
			}
			else
				out = new FileOutputStream(saveFile);
			if(path.endsWith(".gz"))
				out = new GZIPOutputStream(out);
			save(out);
			url = saveUrl;
			this.path = path;
			file = saveFile;
			setPath();
			saveMarkers();
			if(newFile || mode.getName().equals("text")) // XXX
				setMode();
			adirty = dirty = readOnly = newFile = false;

			autosaveFile.delete();
			modTime = file.lastModified();

			fireBufferEvent(new BufferEvent(BufferEvent
				.DIRTY_CHANGED,this));

			return true;
		}
		catch(IOException io)
		{
			Object[] args = { io.toString() };
			GUIUtilities.error(view,"ioerror",args);
		}
		catch(BadLocationException bl)
		{
		}
		return false;
	}
	
	/**
	 * Reloads the buffer from disk.
	 * @param view The view that will be used to display error dialogs, etc
	 */
	public void reload(View view)
	{
		// Delete the autosave
		autosaveFile.delete();

		// This is so that `dirty' isn't set
		init = true;

		load(view);
		loadMarkers();

		// Maybe incorrect? Anyway people won't notice
		setMode();

		tokenizeLines();

		// The anchor gets f*cked across reloads, so clear it
		anchor = null;
		
		// Clear dirty flag
		dirty = false;
		fireBufferEvent(new BufferEvent(BufferEvent
				.DIRTY_CHANGED,this));

		init = false;
	}

	/**
	 * Returns the line number where the paragraph of specified
	 * location starts. Paragraphs are separated by double newlines.
	 * @param lineNo The line number
	 */
	public int locateParagraphStart(int lineNo)
	{
		Element map = getDefaultRootElement();
		for(int i = lineNo - 1; i >= 0; i--)
		{
			Element lineElement = map.getElement(i);
			if(lineElement.getEndOffset() - lineElement
				.getStartOffset() == 1)
			{
				return i;
			}
		}
		return 0;
	}

	/**
	 * Returns the line number where the paragraph of specified
	 * location ends. Paragraphs are separated by double newlines.
	 * @param lineNo The line number
	 */
	public int locateParagraphEnd(int lineNo)
	{
		Element map = getDefaultRootElement();
		int lineCount = map.getElementCount();
		for(int i = lineNo + 1; i < lineCount; i++)
		{
			Element lineElement = map.getElement(i);
			if(lineElement.getEndOffset() - lineElement
				.getStartOffset() == 1)
			{
				return i;
			}
		}
		return lineCount - 1;
	}

	/**
	 * Returns the URL this buffer is editing.
	 */
	public URL getURL()
	{
		return url;
	}
	
	/**
	 * Returns the file this buffer is editing.
	 */
	public File getFile()
	{
		return file;
	}

	/**
	 * Returns the autosave file for this buffer.
	 */
	public File getAutosaveFile()
	{
		return autosaveFile;
	}

	/**
	 * Returns the name of this buffer.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Returns the path name of this buffer.
	 */
	public String getPath()
	{
		return path;
	}

	/**
	 * Returns true if this buffer has been closed (with
	 * <code>jEdit.closeBuffer</code>).
	 */
	public boolean isClosed()
	{
		return closed;
	}

	/**
	 * Returns true if this is an untitled file, false otherwise.
	 */
	public boolean isNewFile()
	{
		return newFile;
	}
	
	/**
	 * Returns true if this file has changed since last save, false
	 * otherwise.
	 */
	public boolean isDirty()
	{
		return dirty;
	}
	
	/**
	 * Returns true if this file is read only, false otherwise.
	 */
	public boolean isReadOnly()
	{
		return readOnly;
	}
	
	/**
	 * Sets the `dirty' (changed since last save) flag of this buffer.
	 */
	public void setDirty(boolean d)
	{
		if(d)
		{
			if(init || readOnly)
				return;
			if(dirty && !adirty)
				return;
			dirty = adirty = true;
		}
		else
			dirty = false;
		fireBufferEvent(new BufferEvent(BufferEvent.DIRTY_CHANGED,
			this));
	}

	/**
	 * Returns this buffer's undo manager.
	 */
	public UndoManager getUndo()
	{
		return undo;
	}

	/**
	 * Starts a compound edit that can be undone in one go.
	 */
	public void beginCompoundEdit()
	{
		if(compoundEdit == null)
			compoundEdit = new CompoundEdit();
		else
			throw new InternalError("You can't do this");
	}

	/**
	 * Ends a compound edit.
	 */
	public void endCompoundEdit()
	{
		if(compoundEdit != null)
		{
			compoundEdit.end();
			if(compoundEdit.canUndo())
				undo.addEdit(compoundEdit);
			compoundEdit = null;
		}
	}

	/**
	 * Returns this buffer's edit mode.
	 */
	public Mode getMode()
	{
		return mode;
	}
	
	/**
	 * Returns the localised name of this buffer's edit mode.
	 */
	public String getModeName()
	{
		return jEdit.getModeName(mode);
	}
	
	/**
	 * Sets this buffer's edit mode.
	 * @param mode The mode
	 */
	public void setMode(Mode mode)
	{
		/* This protects against stupid people (like me)
		 * doing stuff like buffer.setMode(jEdit.getMode(...)); */
		if(mode == null)
			throw new NullPointerException("You suck!!!");
		if(this.mode == mode)
			return;

		View[] views = jEdit.getViews();

		if(this.mode != null)
		{
			for(int i = 0; i < views.length; i++)
			{
				View view = views[i];
				if(view.getBuffer() == this)
					this.mode.leaveView(view);
			}
			this.mode.leave(this);
		}

		this.mode = mode;

		mode.enter(this);

		setTokenMarker(mode.createTokenMarker());
		loadColors();

		for(int i = 0; i < views.length; i++)
		{
			View view = views[i];
			if(view.getBuffer() == this)
				this.mode.enterView(view);
		}

		fireBufferEvent(new BufferEvent(BufferEvent.MODE_CHANGED,this));
	}

	/**
	 * Sets this buffer's edit mode by looking at the extension,
	 * file name and first line.
	 */
	public void setMode()
	{
		String userMode = (String)getProperty("mode");
		if(userMode != null)
		{
			Mode m = jEdit.getMode(userMode);
			if(m != null)
			{
				setMode(m);
				return;
			}
		}
			
		String nogzName = name.substring(0,name.length() -
			(name.endsWith(".gz") ? 3 : 0)).toLowerCase();
		Mode mode = jEdit.getMode(jEdit.getProperty(
			"mode.filename.".concat(nogzName)));
		if(mode != null)
			setMode(mode);
		else
		{
			int index = nogzName.lastIndexOf('.') + 1;
			mode = jEdit.getMode(jEdit.getProperty(
				"mode.extension.".concat(nogzName
				.substring(index))));
			if(mode != null)
				setMode(mode);
			else
			{
				Element lineElement = getDefaultRootElement()
					.getElement(0);
				int start = lineElement.getStartOffset();
				try
				{
					String line = getText(start,lineElement
						.getEndOffset() - start - 1);
					mode = jEdit.getMode(jEdit.getProperty(
						"mode.firstline." + line));
					if(mode != null)
						setMode(mode);
				}
				catch(BadLocationException bl)
				{
				}
			}
		}
	}

	/**
	 * Returns the token marker for this buffer.
	 */
	public TokenMarker getTokenMarker()
	{
		if(syntaxColorizing)
			return tokenMarker;
		else
			return null;
	}

	/**
	 * Returns an enumeration of set markers.
	 */
	public Enumeration getMarkers()
	{
		return markers.elements();
	}
	
	/**
	 * Adds a marker to this buffer.
	 * @param name The name of the marker
	 * @param start The start offset of the marker
	 * @param end The end offset of this marker
	 */
	public void addMarker(String name, int start, int end)
	{
		if(readOnly && !init)
			return;
		dirty = !init;
		name = name.replace(';',' ');
		Marker markerN;
		try
		{
			markerN = new Marker(name,createPosition(start),
				createPosition(end));
		}
		catch(BadLocationException bl)
		{
			return;
		}
		if(!init)
		{
			for(int i = 0; i < markers.size(); i++)
			{
				Marker marker = (Marker)markers.elementAt(i);
				if(marker.getName().equals(name))
				{
					markers.setElementAt(markerN,i);
					break;
				}
				if(marker.getStart() > start)
				{
					markers.insertElementAt(markerN,i);
					return;
				}
			}
		}
		markers.addElement(markerN);
		fireBufferEvent(new BufferEvent(BufferEvent.MARKERS_CHANGED,this));
		fireBufferEvent(new BufferEvent(BufferEvent.DIRTY_CHANGED,this));
	}

	/**
	 * Removes the marker with the specified name.
	 * @param name The name of the marker to remove
	 */
	public void removeMarker(String name)
	{
		if(readOnly)
			return;
		dirty = !init;
loop:		for(int i = 0; i < markers.size(); i++)
		{
			Marker marker = (Marker)markers.elementAt(i);
			if(marker.getName().equals(name))
				markers.removeElementAt(i);
		}
		fireBufferEvent(new BufferEvent(BufferEvent.MARKERS_CHANGED,this));
		fireBufferEvent(new BufferEvent(BufferEvent.DIRTY_CHANGED,this));
	}
	
	/**
	 * Returns the marker with the specified name.
	 * @param name The marker name
	 */
	public Marker getMarker(String name)
	{
		Enumeration enum = getMarkers();
		while(enum.hasMoreElements())
		{
			Marker marker = (Marker)enum.nextElement();
			if(marker.getName().equals(name))
				return marker;
		}
		return null;
	}

	/**
	 * Moves the anchor to a new position.
	 * @param pos The new anchor position
	 */
	public void setAnchor(int pos)
	{
		try
		{
			anchor = createPosition(pos);
		}
		catch(BadLocationException bl)
		{
			anchor = null;
		}
	}

	/**
	 * Returns the anchor position.
	 */
	public int getAnchor()
	{
		return (anchor == null ? -1 : anchor.getOffset());
	}

	/**
	 * Returns the tab size.
	 */
	public int getTabSize()
	{
		Integer i = (Integer)getProperty(tabSizeAttribute);
		if(i != null)
			return i.intValue();
		else
			return 8;
	}
	
	/**
	 * Saves the caret information.
	 * @param savedSelStart The selection start
	 * @param savedSelEnd The selection end
	 */
	public void setCaretInfo(int savedSelStart, int savedSelEnd)
	{
		this.savedSelStart = savedSelStart;
		this.savedSelEnd = savedSelEnd;
	}

	/**
	 * Returns the saved selection start.
	 */
	public int getSavedSelStart()
	{
		return savedSelStart;
	}

	/**
	 * Returns the saved selection end.
	 */
	public int getSavedSelEnd()
	{
		return savedSelEnd;
	}

	/**
	 * Adds a buffer event listener to this buffer.
	 * @param listener The event listener
	 */
	public void addBufferListener(BufferListener listener)
	{
		multicaster.addListener(listener);
	}

	/**	
	 * Removes a buffer event listener from this buffer.
	 * @param listener The event listener
	 */
	public void removeBufferListener(BufferListener listener)
	{
		multicaster.removeListener(listener);
	}

	/**
	 * Forwards a buffer event to all registered listeners.
	 * @param evt The event
	 */
	public void fireBufferEvent(BufferEvent evt)
	{
		multicaster.fire(evt);
	}

	/**
	 * Returns a string representation of this buffer.
	 * This simply returns the path name.
	 */
	public String toString()
	{
		return path;
	}

	// package-private members
	Buffer(View view, URL url, String path, boolean readOnly, boolean newFile)
	{
		init = true;
		
		this.url = url;
		this.path = path;
		this.newFile = newFile;
		this.readOnly = readOnly;

		setDocumentProperties(new BufferProps());
		putProperty("i18n",Boolean.FALSE);
		
		multicaster = new EventMulticaster();
		undo = new UndoManager();
		markers = new Vector();
		addDocumentListener(new DocumentHandler());
		
		setPath();

		// Set default mode
		setMode(jEdit.getMode(jEdit.getProperty("buffer.defaultMode")));

		// Load syntax property
		propertiesChanged();

		if(!newFile)
		{
			if(file.exists())
				this.readOnly |= !file.canWrite();
			if(autosaveFile.exists())
			{
				Object[] args = { autosaveFile.getPath() };
				GUIUtilities.message(view,"autosaveexists",args);
			}
			load(view);
			loadMarkers();
		}

		setMode();

		addUndoableEditListener(new UndoHandler());
		jEdit.addEditorListener(new EditorHandler());
		
		init = false;
	}

	void close()
	{
		closed = true;
	}

	// private members
	private File file;
	private long modTime;
	private File autosaveFile;
	private File markersFile;
	private URL url;
	private URL markersUrl;
	private String name;
	private String path;
	private boolean closed;
	private boolean init;
	private boolean newFile;
	private boolean adirty; /* Has file changed since last *auto* save? */
	private boolean dirty;
	private boolean readOnly;
	private boolean alreadyBackedUp;
	private boolean syntaxColorizing;
	private Mode mode;
	private UndoManager undo;
	private CompoundEdit compoundEdit;
	private Vector markers;
	private Position anchor;
	private int savedSelStart;
	private int savedSelEnd;
	private EventMulticaster multicaster;
	
	private void setPath()
	{
		if(url == null)
		{
			file = new File(path);
			name = file.getName();
			markersFile = new File(file.getParent(),'.' + name
				+ ".marks");
		}
		else
		{
			name = url.getFile();
			if(name.length() == 0)
				name = url.getHost();
			file = new File(name);
			name = file.getName();
			try
			{
				markersUrl = new URL(url,'.' + new File(name)
					.getName() + ".marks");
			}
			catch(MalformedURLException mu)
			{
				markersUrl = null;
			}
		}
		// if we don't do this, the autosave of a file won't be
		// deleted after a save as
		if(autosaveFile != null)
			autosaveFile.delete();
		autosaveFile = new File(file.getParent(),'#' + name + '#');
	}
	
	private void load(View view)
	{
		InputStream _in;
		URLConnection connection = null;
		StringBuffer sbuf = new StringBuffer();
		try
		{
			
			if(url != null)
				_in = url.openStream();
			else
				_in = new FileInputStream(file);
			if(name.endsWith(".gz"))
				_in = new GZIPInputStream(_in);
			InputStreamReader in = new InputStreamReader(_in);
			char[] buf = new char[IOBUFSIZE];
			int len; // Number of characters in buffer
			int lineCount = 0;
			boolean CRLF = false; // Windows line endings
			boolean CROnly = false; // MacOS line endings
			boolean lastWasCR = false; // Was the previous character CR?
			
			while((len = in.read(buf,0,buf.length)) != -1)
			{
				int lastLine = 0; // Offset of last line
				for(int i = 0; i < len; i++)
				{
					switch(buf[i])
					{
					case '\r':
						if(lastWasCR) // \r\r, probably Mac
						{
							CROnly = true;
							CRLF = false;
						}
						else
						{
							lastWasCR = true;
						}
						sbuf.append(buf,lastLine,i -
							lastLine);
						sbuf.append('\n');
						lastLine = i + 1;
						break;
					case '\n':
						if(lastWasCR) // \r\n, probably DOS
						{
							CROnly = false;
							CRLF = true;
							lastWasCR = false;
							lastLine = i + 1;
						}
						else // Unix
						{
							CROnly = false;
							CRLF = false;
							sbuf.append(buf,lastLine,
								i - lastLine);
							sbuf.append('\n');
							lastLine = i + 1;
						}
						break;
					default:
						if(lastWasCR)
						{
							CROnly = true;
							CRLF = false;
							lastWasCR = false;
						}
						break;
					}
				}
				sbuf.append(buf,lastLine,len - lastLine);
			}
			if(CRLF)
				putProperty(LINESEP,"\r\n");
			else if(CROnly)
				putProperty(LINESEP,"\r");
			else
				putProperty(LINESEP,"\n");
			in.close();
                        if(sbuf.length() != 0 && sbuf.charAt(sbuf.length() - 1) == '\n')
				sbuf.setLength(sbuf.length() - 1);

			// For `reload' command
			remove(0,getLength());

			insertString(0,sbuf.toString(),null);
			newFile = false;
			modTime = file.lastModified();

			// One day, we should interleave this code into
			// the above loop for top hat efficency. But for
			// now, this will suffice.
			Element map = getDefaultRootElement();
			for(int i = 0; i < 10; i++)
			{
				Element lineElement = map.getElement(i);
				if(lineElement == null)
					break;
				String line = getText(lineElement.getStartOffset(),
					lineElement.getEndOffset()
					- lineElement.getStartOffset());
				processProperty(line);
			}
		}
		catch(BadLocationException bl)
		{
			bl.printStackTrace();
		}
		catch(FileNotFoundException fnf)
		{
			Object[] args = { path };
			GUIUtilities.error(view,"notfounderror",args);
		}
		catch(IOException io)
		{
			Object[] args = { io.toString() };
			GUIUtilities.error(view,"ioerror",args);
		}
	}

	private void loadColors()
	{
		colors[Token.COMMENT1] = GUIUtilities.parseColor(
			(String)getProperty("colors.comment1"));
		colors[Token.COMMENT2] = GUIUtilities.parseColor(
			(String)getProperty("colors.comment2"));
		colors[Token.KEYWORD1] = GUIUtilities.parseColor(
			(String)getProperty("colors.keyword1"));
		colors[Token.KEYWORD2] = GUIUtilities.parseColor(
			(String)getProperty("colors.keyword2"));
		colors[Token.KEYWORD3] = GUIUtilities.parseColor(
			(String)getProperty("colors.keyword3"));
		colors[Token.LABEL] = GUIUtilities.parseColor(
			(String)getProperty("colors.label"));
		colors[Token.LITERAL1] = GUIUtilities.parseColor(
			(String)getProperty("colors.literal1"));
		colors[Token.LITERAL2] = GUIUtilities.parseColor(
			(String)getProperty("colors.literal2"));
		colors[Token.OPERATOR] = GUIUtilities.parseColor(
			(String)getProperty("colors.operator"));
		colors[Token.INVALID] = GUIUtilities.parseColor(
			(String)getProperty("colors.invalid"));
	}

	private void processProperty(String prop)
	{
		StringBuffer buf = new StringBuffer();
		String name = null;
		boolean escape = false;
		for(int i = 0; i < prop.length(); i++)
		{
			char c = prop.charAt(i);
			switch(c)
			{
			case ':':
				if(escape)
				{
					escape = false;
					buf.append(':');
					break;
				}
				if(name != null)
				{
					String value = buf.toString();
					try
					{
						putProperty(name,new Integer(
							value));
					}
					catch(NumberFormatException nf)
					{
						putProperty(name,value);
					}
				}
				buf.setLength(0);
				break;
			case '=':
				if(escape)
				{
					escape = false;
					buf.append('=');
					break;
				}
				name = buf.toString();
				buf.setLength(0);
				break;
			case '\\':
				if(escape)
					buf.append('\\');
				escape = !escape;
				break;
			case 'n':
				if(escape)
				{	buf.append('\n');
					escape = false;
					break;
				}
			case 't':
				if(escape)
				{
					buf.append('\t');
					escape = false;
					break;
				}
			default:
				buf.append(c);
				break;
			}
		}
	}

	private void loadMarkers()
	{
		// For `reload' command
		markers.removeAllElements();

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

	private void save(OutputStream _out)
		throws IOException, BadLocationException
	{
		BufferedWriter out = new BufferedWriter(
			new OutputStreamWriter(_out),IOBUFSIZE);
		String newline = (String)getProperty(LINESEP);
		if(newline == null)
			newline = System.getProperty("line.separator");
		Element map = getDefaultRootElement();
		for(int i = 0; i < map.getElementCount(); i++)
		{
			Element line = map.getElement(i);
			int start = line.getStartOffset();
			out.write(getText(start,line.getEndOffset() - start
				- 1));
			out.write(newline);
		}
		out.close();
	}
	
	private void saveMarkers()
		throws IOException
	{
		if(markers.isEmpty())
		{
			markersFile.delete();
			return;
		}
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

	private void backup(File file)
	{
		if(alreadyBackedUp)
			return;
		alreadyBackedUp = true;
		
		// Fetch properties
		int backups;
		try
		{
			backups = Integer.parseInt(jEdit.getProperty(
				"backups"));
		}
		catch(NumberFormatException nf)
		{
			backups = 1;
		}

		if(backups == 0)
			return;

		String backupPrefix = jEdit.getProperty("backup.prefix","");
		String backupSuffix = jEdit.getProperty("backup.suffix","~");

		// Check for backup.directory property, and create that
		// directory if it doesn't exist
		String backupDirectory = jEdit.getProperty("backup.directory");
		if(backupDirectory == null || backupDirectory.length() == 0)
			backupDirectory = file.getParent();
		else
		{
			backupDirectory = MiscUtilities.constructPath(
				System.getProperty("user.home"),backupDirectory);
			new File(backupDirectory).mkdirs();
		}
		
		String name = file.getName();

		// If backups is 1, create ~ file
		if(backups == 1)
		{
			file.renameTo(new File(backupDirectory,
				backupPrefix + name + backupSuffix));
		}
		// If backups > 1, move old ~n~ files, create ~1~ file
		else
		{
			new File(backupDirectory,
				backupPrefix + name + backupSuffix
				+ backups + backupSuffix).delete();

			for(int i = backups - 1; i > 0; i--)
			{
				File backup = new File(backupDirectory,
					backupPrefix + name + backupSuffix
					+ i + backupSuffix);

				backup.renameTo(new File(backupDirectory,
					backupPrefix + name + backupSuffix
					+ (i+1) + backupSuffix));
			}

			file.renameTo(new File(backupDirectory,
				backupPrefix + name + backupSuffix
				+ "1" + backupSuffix));
		}
	}

	class BufferProps extends Hashtable
	{
		public Object get(Object key)
		{
			// First try the buffer-local properties
			Object o = super.get(key);
			if(o != null)
				return o;

			// Now try mode.<mode>.<property>
			String value = jEdit.getProperty("mode."
				+ mode.getName() + "." + key);

			// Now try buffer.<property>
			if(value == null)
			{
				value = jEdit.getProperty("buffer." + key);
				if(value == null)
					return null;
			}

			// Try returning it as an integer first
			try
			{
				return new Integer(value);
			}
			catch(NumberFormatException nf)
			{
				return value;
			}
		}
	}

	// event handlers
	class EditorHandler
	extends EditorAdapter
	{
		public void propertiesChanged(EditorEvent evt)
		{
			Buffer.this.propertiesChanged();
			loadColors();
		}
	}

	class UndoHandler
	implements UndoableEditListener
	{
		public void undoableEditHappened(UndoableEditEvent evt)
		{
			if(compoundEdit != null)
				compoundEdit.addEdit(evt.getEdit());
			else
				getUndo().addEdit(evt.getEdit());
		}
	}

	class DocumentHandler
	implements DocumentListener
	{
		public void insertUpdate(DocumentEvent evt)
		{
			setDirty(true);
		}
	
		public void removeUpdate(DocumentEvent evt)
		{
			setDirty(true);
		}
	
		public void changedUpdate(DocumentEvent evt)
		{
			setDirty(true);
		}
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.80  1999/06/05 00:42:04  sp
 * Expand abbreviation & buffer mode selection bugs fixed
 *
 * Revision 1.79  1999/06/03 08:24:12  sp
 * Fixing broken CVS
 *
 * Revision 1.78  1999/05/29 08:06:56  sp
 * Search and replace overhaul
 *
 * Revision 1.77  1999/05/22 08:33:53  sp
 * FAQ updates, mode selection tweak, patch mode update, javadoc updates, JDK 1.1.8 fix
 *
 * Revision 1.76  1999/04/24 07:34:46  sp
 * Documentation updates
 *
 * Revision 1.75  1999/04/23 07:35:10  sp
 * History engine reworking (shared history models, history saved to
 * .jedit-history)
 *
 * Revision 1.74  1999/04/21 07:39:18  sp
 * FAQ added, plugins can now add panels to the options dialog
 *
 * Revision 1.73  1999/04/21 06:13:26  sp
 * Fixed bug in loadDesktop(), added idiot-proofing to Buffer.setMode()
 *
 * Revision 1.72  1999/04/20 06:38:26  sp
 * jEdit.addPluginMenu() method added
 *
 * Revision 1.71  1999/04/19 05:47:35  sp
 * ladies and gentlemen, 1.6pre1
 *
 * Revision 1.70  1999/04/02 00:39:19  sp
 * Fixed console bug, syntax API changes, minor jEdit.java API change
 *
 * Revision 1.69  1999/03/28 01:36:24  sp
 * Backup system overhauled, HistoryTextField updates
 *
 * Revision 1.68  1999/03/27 23:47:57  sp
 * Updated docs, view tweak, goto-line fix, next/prev error tweak
 *
 * Revision 1.67  1999/03/27 03:05:17  sp
 * Modular SyntaxTextArea
 *
 * Revision 1.66  1999/03/27 00:44:15  sp
 * Documentation updates, various bug fixes
 *
 * Revision 1.65  1999/03/26 04:14:45  sp
 * EnhancedMenuItem tinkering, fixed compile error, fixed backup bug
 *
 * Revision 1.64  1999/03/24 09:33:22  sp
 * Fixed backup.directory bug, updated options dialog, updated documentation
 *
 * Revision 1.63  1999/03/24 05:45:27  sp
 * Juha Lidfors' backup directory patch, removed debugging messages from various locations, documentation updates
 *
 */
