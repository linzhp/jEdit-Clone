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

package org.gjt.sp.jedit;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;
import gnu.regexp.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import org.gjt.sp.jedit.syntax.*;

/**
 * 
 * @see BufferMgr
 * @see BufferMgr#openFile(View)
 * @see BufferMgr#openURL(View)
 * @see BufferMgr#openFile(View,String,String,boolean,boolean)
 * @see BufferMgr#closeBuffer(View)
 * @see BufferMgr#getBuffers()
 */
public class Buffer extends PlainDocument
implements DocumentListener, UndoableEditListener
{
	/**
	 * Finds the next instance of the search string in this buffer.
	 * The search string is obtained from the
	 * <code>search.find.value</code> property.
	 * @param view The view
	 * @param done For internal use. False if a `keep searching'
	 * dialog should be shown if no more matches have been found.
	 */
	public boolean find(View view, boolean done)
	{
		return find(view,view.getTextArea().getCaretPosition(),done);
	}

	/**
	 * Finds the next instance of the search string in this buffer.
	 * The search string is obtained from the
	 * <code>search.find.value</code> property.
	 * @param view The view
	 * @param start Location where to start the search
	 * @param done For internal use. False if a `keep searching'
	 * dialog should be shown if no more matches have been found.
	 */
	public boolean find(View view, int start, boolean done)
	{
		try
		{
			RE regexp = jEdit.getRE();
			if(regexp == null)
			{
				view.getToolkit().beep();
				return false;
			}
			String text = getText(start,getLength() - start);
			REMatch match = regexp.getMatch(text);
			if(match != null)
			{
				view.getTextArea().select(start + match
					.getStartIndex(),
					start + match.getEndIndex());
				return true;
			}
			if(done)
			{
				view.getToolkit().beep();
				return false;
			}
			int result = JOptionPane.showConfirmDialog(view,
				jEdit.getProperty("keepsearching.message"),
				jEdit.getProperty("keepsearching.title"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
			if(result == JOptionPane.YES_OPTION)
				return find(view,0,true);
		}
		catch(Exception e)
		{
			Object[] args = { e.getMessage() };
			jEdit.error(view,"reerror",args);
		}
		return false;
	}

	/**
	 * Replaces the current selection with the replacement string.
	 * <p>
	 * The replacement string is obtained from the
	 * <code>search.replace.value</code> property.
	 * @param view The view
	 */
	public void replace(View view)
	{
		try
		{
			RE regexp = jEdit.getRE();
			String replaceStr = jEdit.getProperty("search.replace.value");
			if(regexp == null)
			{
				view.getToolkit().beep();
				return;
			}
			SyntaxTextArea textArea = view.getTextArea();
			String selection = textArea.getSelectedText();
			if(selection == null)
				selection = "";
			textArea.replaceSelection(regexp.substitute(selection,
				replaceStr));
		}
		catch(REException re)
		{
			Object[] args = { re.getMessage() };
			jEdit.error(view,"reerror",args);
		}
	}

	/**
	 * Replaces all occurances of the search string with the replacement
	 * string.
	 * @param view The view
	 * @return True if the replace operation was successful, false
	 * if no matches were found
	 */
	public boolean replaceAll(View view)
	{
		boolean found = false;
		beginCompoundEdit();
		try
		{
			RE regexp = jEdit.getRE();
			String replaceStr = jEdit.getProperty("search.replace.value");
			if(regexp == null)
			{
				endCompoundEdit();
				return false;
			}
			REMatch match;
			int index = 0;
			while((match = regexp.getMatch(getText(index,
				getLength() - index))) != null)
			{
				int start = match.getStartIndex() + index;
				int len = match.getEndIndex() - match
					.getStartIndex();
				String str = getText(start,len);
				remove(start,len);
				String subst = regexp.substitute(str,replaceStr);
				index = start + subst.length();
				insertString(start,subst,null);
				found = true;
			}
		}
		catch(REException re)
		{
			Object[] _args = { re.getMessage() };
			jEdit.error(view,"reerror",_args);
		}
		catch(BadLocationException bl)
		{
		}
		endCompoundEdit();
		return found;
	}

	/**
	 * Autosaves this buffer.
	 */
	public void autosave()
	{
		if(dirty)
		{
			try
			{
				save(new FileOutputStream(autosaveFile));
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
		if(path != null)
		{
			try
			{
				url = new URL(path);
			}
			catch(MalformedURLException mu)
			{
				url = null;
			}
			this.path = path;
			setPath();
		}
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
			updateTitles();
			updateBufferMenus();
			return true;
		}
		catch(IOException io)
		{
			Object[] args = { io.toString() };
			jEdit.error(view,"ioerror",args);
		}
		catch(BadLocationException bl)
		{
		}
		return false;
	}
	
	/**
	 * Finds the previous instance of an opening bracket in the buffer.
	 * The closing bracket is needed as well to handle nested brackets
	 * properly.
	 * @param dot The starting position
	 * @param openBracket The opening bracket
	 * @param closeBracket The closing bracket
	 */
	 public int locateBracketBackward(int dot, char openBracket,
		char closeBracket)
		throws BadLocationException
	{
		int count;
		Element map = getDefaultRootElement();
		// check current line
		int lineNo = map.getElementIndex(dot);
		Element lineElement = map.getElement(lineNo);
		int start = lineElement.getStartOffset();
		int offset = scanBackwardLine(getText(start,dot - start),
			openBracket,closeBracket,0);
		count = -offset - 1;
		if(offset >= 0)
			return start + offset;
		// check previous lines
		for(int i = lineNo - 1; i >= 0; i--)
		{
			lineElement = map.getElement(i);
			start = lineElement.getStartOffset();
			offset = scanBackwardLine(getText(start,lineElement
				.getEndOffset() - start),openBracket,
				closeBracket,count);
			count = -offset - 1;
			if(offset >= 0)
				return start + offset;
		}
		// not found
		return -1;
	}

	/**
	 * Finds the next instance of a closing bracket in the buffer.
	 * The opening bracket is needed as well to handle nested brackets
	 * properly.
	 * @param dot The starting position
	 * @param openBracket The opening bracket
	 * @param closeBracket The closing bracket
	 */
	public int locateBracketForward(int dot, char openBracket,
		char closeBracket)
		throws BadLocationException
	{
		int count;
		Element map = getDefaultRootElement();
		// check current line
		int lineNo = map.getElementIndex(dot);
		Element lineElement = map.getElement(lineNo);
		int start = lineElement.getStartOffset();
		int end = lineElement.getEndOffset();
		int offset = scanForwardLine(getText(dot + 1,end - (dot + 1)),
			openBracket,closeBracket,0);
		count = -offset - 1;
		if(offset >= 0)
			return dot + offset + 1;
		// check following lines
		for(int i = lineNo + 1; i < map.getElementCount(); i++)
		{
			lineElement = map.getElement(i);
			start = lineElement.getStartOffset();
			offset = scanForwardLine(getText(start,lineElement
				.getEndOffset() - start),openBracket,
				closeBracket,count);
			count = -offset - 1;
			if(offset >= 0)
				return start + offset;
		}
		// not found
		return -1;
	}

	/**
	 * Returns the line number where the paragraph of specified
	 * location starts. Paragraphs are separated by double newlines.
	 * @param lineNo The line number
	 */
	public int locateParagraphStart(int lineNo)
	{
		Element map = getDefaultRootElement();
		for(int i = lineNo; i >= 0; i--)
		{
			Element lineElement = map.getElement(i);
			if(lineElement.getEndOffset() - lineElement
				.getStartOffset() == 1)
			{
				if(i == lineNo)
					continue;
				else
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
		for(int i = lineNo; i < lineCount; i++)
		{
			Element lineElement = map.getElement(i);
			if(lineElement.getEndOffset() - lineElement
				.getStartOffset() == 1)
			{
				if(i == lineNo)
					continue;
				else
					return i;
			}
		}
		return lineCount;
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
		if(this.mode != null)
			this.mode.leave(this);
		this.mode = mode;
		if(!init)
		{
			updateStatus();
			updateBufferMenus();
		}
		if(mode == null)
		{
			tokenMarker = null;
			colors.clear();
		}
		else
		{
			setTokenMarker(mode.createTokenMarker());
			loadColors(mode.getClass().getName().substring(22));
			mode.enter(this);
		}
	}

	/**
	 * Returns the token marker for this buffer.
	 */
	public TokenMarker getTokenMarker()
	{
		if(jEdit.getSyntaxColorizing())
			return tokenMarker;
		else
			return null;
	}

	/**
	 * Sets the token marker for this buffer.
	 * @param tokenMarker the new token marker
	 */
	public void setTokenMarker(TokenMarker tokenMarker)
	{
		this.tokenMarker = tokenMarker;
		if(tokenMarker == null)
			return;
		Segment line = new Segment();
		try
		{
			Element map = getDefaultRootElement();
			int lines = map.getElementCount();
			for(int i = 0; i < lines; i++)
			{
				Element lineElement = map.getElement(i);
				int start = lineElement.getStartOffset();
				getText(start,lineElement.getEndOffset()
					- start,line);
				tokenMarker.markTokens(line,i);
			}
		}
		catch(BadLocationException bl)
		{
			bl.printStackTrace();
		}
	}

	/**
	 * Loads the colors used for syntax colorizing from the properties.
	 * <p>
	 * The colors are stored in the property as a white space separated
	 * list of <i>name</i>@<i>value</i> pairs. <i>name</i> is mode
	 * specific, <i>value</i> is the name of a predefined color in
	 * the <code>java.awt.Color</code> class.
	 * @param name The name of the property to load the colors from
	 */
	public void loadColors(String name)
	{
		colors.clear();
		String prop = jEdit.getProperty("mode." + name + ".colors");
		if(prop == null)
			return;
		StringTokenizer st = new StringTokenizer(prop);
		while(st.hasMoreTokens())
		{
			String color = st.nextToken();
			int index = color.indexOf('@');
			if(index == -1)
				continue;
			String id = color.substring(0,index);
			color = color.substring(index + 1);
			colors.put(id,jEdit.parseColor(color));
		}
	}

	/**
	 * Returns the colors for syntax colorizing.
	 */
	public Hashtable getColors()
	{
		return colors;
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
					updateMarkers();
					return;
				}
			}
		}
		markers.addElement(markerN);
		if(!init)
			updateMarkers();
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
	 * Reloads the tab size setting from the properties.
	 */
	public void propertiesChanged()
	{
		try
		{
			Object oldSize = getProperty(tabSizeAttribute);
			if(oldSize == null)
			{
				Integer tabSize = new Integer(jEdit
					.getProperty("buffer.tabSize"));
				putProperty(tabSizeAttribute,tabSize);
			}
		}
		catch(NumberFormatException nf)
		{
		}
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

	// event handlers
	public void undoableEditHappened(UndoableEditEvent evt)
	{
		if(compoundEdit != null)
			compoundEdit.addEdit(evt.getEdit());
		else
			undo.addEdit(evt.getEdit());
	}

	public void insertUpdate(DocumentEvent evt)
	{
		dirty();
		if(mode instanceof DocumentListener)
			((DocumentListener)mode).insertUpdate(evt);
		if(tokenMarker == null)
			return;
		DocumentEvent.ElementChange ch = evt.getChange(
			getDefaultRootElement());
		if(ch == null)
			return;
		int line = ch.getIndex();
		Element[] children = ch.getChildrenAdded();
		if(children == null)
			return;
		for(int i = 0; i < children.length; i++)
			tokenMarker.insertLine(i + line);
	}

	public void removeUpdate(DocumentEvent evt)
	{
		dirty();
		if(mode instanceof DocumentListener)
			((DocumentListener)mode).removeUpdate(evt);
		if(tokenMarker == null)
			return;
		DocumentEvent.ElementChange ch = evt.getChange(
			getDefaultRootElement());
		if(ch == null)
			return;
		int line = ch.getIndex();
		Element[] children = ch.getChildrenRemoved();
		if(children == null)
			return;
		for(int i = 0; i < children.length; i++)
			tokenMarker.deleteLine(i + line);
	}

	public void changedUpdate(DocumentEvent evt)
	{
		dirty();
		if(mode instanceof DocumentListener)
			((DocumentListener)mode).changedUpdate(evt);
	}

	// package-private methods
	Buffer(URL url, String path, boolean readOnly, boolean newFile)
	{
		this.url = url;
		this.path = path;
		this.newFile = newFile;
		this.readOnly = readOnly;
		init();
	}

	// private methods
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
	private Mode mode;
	private UndoManager undo;
	private CompoundEdit compoundEdit;
	private Vector markers;
	private Position anchor;
	private int savedSelStart;
	private int savedSelEnd;
	private TokenMarker tokenMarker;
	private Hashtable colors;

	private void init()
	{	
		init = true;
		setDocumentProperties(new BufferProps());
		// silly hack for backspace to work
		putProperty("i18n",Boolean.FALSE);
		undo = new UndoManager();
		markers = new Vector();
		colors = new Hashtable();
		setPath();
		if(!newFile)
		{
			if(file.exists())
				this.readOnly |= !file.canWrite();
			if(autosaveFile.exists())
			{
				Object[] args = { autosaveFile.getPath() };
				jEdit.message(null,"autosaveexists",args);
			}
			load();
			loadMarkers();
		}
		String userMode = (String)getProperty("mode");
		if(userMode != null)
			setMode(jEdit.getMode(userMode));
		addDocumentListener(this);
		addUndoableEditListener(this);
		updateMarkers();
		propertiesChanged();
		init = false;
	}
	
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
		if(mode == null)
		{
			String nogzName = name.substring(0,name.length() -
				(name.endsWith(".gz") ? 3 : 0));
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
			}
		}
	}
	
	private void load()
	{
		InputStream in;
		URLConnection connection = null;
		StringBuffer buf = new StringBuffer();
		try
		{
			
			if(url != null)
				in = url.openStream();
			else
				in = new FileInputStream(file);
			if(name.endsWith(".gz"))
				in = new GZIPInputStream(in);
			BufferedReader bin = new BufferedReader(new
				InputStreamReader(in));
			String line;
			int count = 0;
			while ((line = bin.readLine()) != null)
			{
				buf.append(line);
				buf.append('\n');
				if(count++ == 0)
				{
					if(mode == null)
					{
						setMode(jEdit.getMode(jEdit.getProperty(
							"mode.firstline." + line)));
					}
				}
				if(count < 10)
				{
					int index = line.indexOf("(:");
					if(index == -1)
						continue;
					int end = line.indexOf(')',index + 6);
					processProperty(line.substring(index,
						end));
				}
			}
			bin.close();
			insertString(0,buf.toString(),null);
			newFile = false;
			/* Although this is done in setPath(),
			 * setPath() is called before the lines are
			 * available, so we do it here as well.
			 */
			if(tokenMarker == null)
				return;
			Segment lineSegment = new Segment();
			Element map = getDefaultRootElement();
			int lines = map.getElementCount();
			for(int i = 0; i < lines; i++)
			{
				Element lineElement = map.getElement(i);
				int start = lineElement.getStartOffset();
				getText(start,lineElement.getEndOffset()
					- start,lineSegment);
				tokenMarker.markTokens(lineSegment,i);
			}
		}
		catch(BadLocationException bl)
		{
			bl.printStackTrace();
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
		OutputStreamWriter out = new OutputStreamWriter(_out);
		String newline = jEdit.getProperty("buffer.line.separator",
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

	private void backup()
	{
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

	// the return value is as follows:
	// >= 0: offset in line where bracket was found
	// < 0: -1 - count
	private int scanBackwardLine(String line, char openBracket,
		char closeBracket, int count)
	{
		for(int i = line.length() - 1; i >= 0; i--)
		{
			char c = line.charAt(i);
			if(c == closeBracket)
				count++;
			else if(c == openBracket)
			{
				if(--count < 0)
					return i;
			}
		}
		return -1 - count;
	}

	// the return value is as follows:
	// >= 0: offset in line where bracket was found
	// < 0: -1 - count
	private int scanForwardLine(String line, char openBracket,
		char closeBracket, int count)
	{
		for(int i = 0; i < line.length(); i++)
		{
			char c = line.charAt(i);
			if(c == openBracket)
				count++;
			else if(c == closeBracket)
			{
				if(--count < 0)
					return i;
			}
		}
		return -1 - count;
	}

	private void updateStatus()
	{
		Enumeration enum = jEdit.getViews();
		while(enum.hasMoreElements())
		{
			View view = (View)enum.nextElement();
			if(view.getBuffer() == this)
				view.updateStatus(true);
		}
	}

	private void updateTitles()
	{
		Enumeration enum = jEdit.getViews();
		while(enum.hasMoreElements())
		{
			View view = (View)enum.nextElement();
			if(view.getBuffer() == this)
				view.updateTitle();
		}
	}

	private void updateBufferMenus()
	{
		Enumeration enum = jEdit.getViews();
		while(enum.hasMoreElements())
		{
			((View)enum.nextElement()).updateBuffersMenu();
		}
	}

	private void updateMarkers()
	{
		Enumeration enum = jEdit.getViews();
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
			updateBufferMenus();
		}
	}

	private class BufferProps extends Hashtable
	{
		public Object get(Object key)
		{
			Object o = super.get(key);
			if(o != null)
				return o;
			if(mode == null)
				return null;
			String clazz = mode.getClass().getName();
			String value = jEdit.getProperty("mode." + clazz
				.substring(clazz.lastIndexOf('.')+1) + "." + key);
			if(value == null)
			{
				value = jEdit.getProperty("buffer." + key);
				if(value == null)
					return null;
			}
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
}
