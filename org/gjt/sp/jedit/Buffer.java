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

import com.sun.java.swing.*;
import com.sun.java.swing.event.*;
import com.sun.java.swing.text.*;
import com.sun.java.swing.undo.UndoManager;
import gnu.regexp.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import jstyle.JSTokenMarker;
import org.gjt.sp.jedit.syntax.SyntaxTextArea;

/**
 * A buffer is an in-memory copy of an open file.
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
	// public methods
	
	/**
	 * Finds the next instance of the search string in this buffer.
	 * @param view The view
	 * @param done For internal use really. False if a `keep searching'
	 * dialog should be shown if no more matches have been found.
	 */
	public boolean find(View view, boolean done)
	{
		try
		{
			RE regexp = jEdit.getRE();
			if(regexp == null)
			{
				view.getToolkit().beep();
				return false;
			}
			int caret = view.getTextArea().getCaretPosition();
			String text = getText(caret,getLength() - caret);
			REMatch match = regexp.getMatch(text);
			if(match != null)
			{
				view.getTextArea().select(caret + match
					.getStartIndex(),
					caret + match.getEndIndex());
				return true;
			}
			if(done)
			{
				view.getToolkit().beep();
				return false;
			}
			int result = JOptionPane.showConfirmDialog(view,
				jEdit.props.getProperty(
					"keepsearching.message"),
				jEdit.props.getProperty("keepsearching.title"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
			if(result == JOptionPane.YES_OPTION)
			{
				view.getTextArea().setCaretPosition(0);
				return find(view,true);
			}
		}
		catch(REException re)
		{
			Object[] args = { re.getMessage() };
			jEdit.error(view,"reerror",args);
		}
		catch(BadLocationException bl)
		{
		}
		return false;
	}

	/**
	 * Replaces the current selection with the replacement string.
	 * @param view The view
	 */
	public void replace(View view)
	{
		try
		{
			RE regexp = jEdit.getRE();
			String replaceStr = jEdit.props.getProperty("search"
				+ ".replace.value");
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
	 */
	public void replaceAll(View view)
	{
		try
		{
			RE regexp = jEdit.getRE();
			String replaceStr = jEdit.props.getProperty("search"
				+ ".replace.value");
			if(regexp == null)
			{
				view.getToolkit().beep();
				return;
			}
			REMatch match;
			int index = 0;
			while((match = regexp.getMatch(getText(index,
				getLength() - index))) != null)
			{
				int start = match.getStartIndex() + index;
				int len = match.getEndIndex() - match
					.getStartIndex();
				index += len;
				String str = getText(start,len);
				remove(start,len);
				insertString(start,regexp.substitute(str,
					replaceStr),null);
			}
		}
		catch(REException re)
		{
			Object[] args = { re.getMessage() };
			jEdit.error(view,"reerror",args);
		}
		catch(BadLocationException bl)
		{
		}
	}
	
	/**
	 * Does a word count and displays the results in a dialog box.
	 * @param view The view
	 */
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

	/**
	 * Executes <code>cmd</code>, or if it is null, prompts the user for
	 * a command to execute.
	 * <p>
	 * Note that these aren't jEdit commands, but OS commands.
	 * @param view The view
	 * @param cmd The command to execute
	 */
	public void execute(View view, String cmd)
	{
		if(cmd == null)
			cmd = jEdit.input(view,"execute","execute.cmd");
		if(cmd == null)
			return;		
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

	/**
	 * Prompts the user for a URL to save this buffer to.
	 * @param view The view
	 */
	public boolean saveToURL(View view)
	{
		String path = jEdit.input(view,"saveurl","openurl.url");
		if(path != null)
			return save(view,path);
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
			setPath(null,path);
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
		catch(BadLocationException bl)
		{
		}
		return false;
	}
	
	/**
	 * Prints this buffer.
	 * @param view The view
	 */
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
				.getProperty("buffer.margin.top")).floatValue()
				* ppi);
		}
		catch(NumberFormatException nf)
		{
			topMargin = ppi / 2;
		}
		try
		{
			leftMargin = (int)(Float.valueOf(jEdit.props
				.getProperty("buffer.margin.left"))
				.floatValue() * ppi);
		}
		catch(NumberFormatException nf)
		{
			leftMargin = ppi / 2;
		}
		try
		{
			bottomMargin = (int)(Float.valueOf(jEdit.props
				.getProperty("buffer.margin.bottom"))
				.floatValue() * ppi);
		}
		catch(NumberFormatException nf)
		{
			bottomMargin = topMargin;
		}
		try
		{
			rightMargin = (int)(Float.valueOf(jEdit.props
				.getProperty("buffer.margin.right"))
				.floatValue() * ppi);
		}
		catch(NumberFormatException nf)
		{
			rightMargin = leftMargin;
		}
		String header = view.getTitle();
		Element map = getDefaultRootElement();
		SyntaxTextArea textArea = view.getTextArea();
		Graphics gfx = null;
		Font font = textArea.getFont();
		int fontHeight = font.getSize();
		int tabSize = getTabSize();
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
		return jEdit.cmds.getModeName(mode);
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
			updateStatus();
		if(mode != null)
			mode.enter(this);
	}
	
	/**
	 * Sets the token marker for this buffer.
	 * @param tokenMarker the new token marker
	 */
	public void setTokenMarker(JSTokenMarker tokenMarker)
	{
		if("on".equals(jEdit.props.getProperty("buffer.syntax")))
			this.tokenMarker = tokenMarker;
	}

	/**
	 * Returns the token marker for this buffer.
	 */
	public JSTokenMarker getTokenMarker()
	{
		return tokenMarker;
	}
	
	/**
	 * Marks the specified line up into tokens.
	 * @param lineIndex The line number
	 */
	public Enumeration markTokens(int lineIndex)
	{
		if(tokenMarker == null)
			return null;
		Element map = getDefaultRootElement();
		try
		{
			//boolean next = false;
			// this is painfully slow because of JScrollPane bugs,
			// so we're just going to have to put up with
			// inaccurate colorizing for now :(
			/*for(int i = oldLineIndex + 1; i < lineIndex; i++)
			{
				Element lineElement = map.getElement(i);
				int start = lineElement.getStartOffset();
				String line = getText(start,lineElement
					.getEndOffset() - start);
				tokenMarker.markTokens(line,lineIndex,next);
				next = true;
				// FIXME this doesn't work
				//if(!tokenMarker.isNextLineRequested())
				//	break;
			}*/
			Element lineElement = map.getElement(lineIndex);
			int start = lineElement.getStartOffset();
			String line = jEdit.untab(getTabSize(),getText(start,
				lineElement.getEndOffset() - start - 1));
			boolean next = (lineIndex - oldLineIndex == 1);
			oldLineIndex = lineIndex;
			return tokenMarker.markTokens(line,lineIndex,next);
		}
		catch(BadLocationException bl)
		{
			return null;
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
		String prop = jEdit.props.getProperty("colors.".concat(name));
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
	 * Clears the color table.
	 */
	public void clearColors()
	{
		colors.clear();
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
	 * <p>
	 * This should be called after the <code>buffer.tabsize</code>
	 * has been changed.
	 */
	public void propertiesChanged()
	{
		try
		{
			Object oldSize = getProperty(tabSizeAttribute);
			if(oldSize == null)
			{
				Integer tabSize = new Integer(jEdit.props
					.getProperty("buffer.tabsize"));
				putProperty(tabSizeAttribute,tabSize);
			}
		}
		catch(NumberFormatException nf)
		{
		}
	}

	// event handlers
	public void undoableEditHappened(UndoableEditEvent evt)
	{
		undo.addEdit(evt.getEdit());
	}

	public void insertUpdate(DocumentEvent evt)
	{
		dirty();
		/*if(tokenMarker == null)
			return;
		DocumentEvent.ElementChange ch = evt.getChange(
			getDefaultRootElement());
		if(ch == null)
			return;
		int line = ch.getIndex();
		Element[] children = ch.getChildrenAdded();
		if(children == null)
			return;
		for(int i = line; i < children.length; i++)
			tokenMarker.insertLine(i);*/
	}

	public void removeUpdate(DocumentEvent evt)
	{
		dirty();
		/*if(tokenMarker == null)
			return;
		DocumentEvent.ElementChange ch = evt.getChange(
			getDefaultRootElement());
		if(ch == null)
			return;
		int line = ch.getIndex();
		Element[] children = ch.getChildrenRemoved();
		if(children == null)
			return;
		for(int i = line; i < children.length; i++)
			tokenMarker.deleteLine(line);*/
	}

	public void changedUpdate(DocumentEvent evt)
	{
		dirty();
	}

	// package-private methods
	Buffer(String parent, String path, boolean readOnly, boolean newFile)
	{
		init = true;
		undo = new UndoManager();
		markers = new Vector();
		colors = new Hashtable();
		this.newFile = newFile;
		this.readOnly = readOnly;
		setPath(parent,path);
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
			setMode(jEdit.cmds.getMode(userMode));
		else
		{
			String nogzName = name.substring(0,name.length() -
				(name.endsWith(".gz") ? 3 : 0));
			int index = nogzName.lastIndexOf('.') + 1;
			setMode(jEdit.cmds.getMode(jEdit.props.getProperty(
				"mode.".concat(nogzName.substring(index)))));
		}
		addDocumentListener(this);
		addUndoableEditListener(this);
		updateMarkers();
		propertiesChanged();
		caretInfo = new int[4];
		init = false;
	}
	
	void setCaretInfo(int scrollH, int scrollV, int selStart,
		int selEnd)
	{
		caretInfo[0] = scrollH;
		caretInfo[1] = scrollV;
		caretInfo[2] = selStart;
		caretInfo[3] = selEnd;
	}

	int[] getCaretInfo()
	{
		return caretInfo;
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
	private Vector markers;
	private int[] caretInfo;
	private JSTokenMarker tokenMarker;
	private int oldLineIndex;
	private Hashtable colors;

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
				if(count++ < 10)
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
		jEdit.message(view,"wordcount",args);
	}

	private void save(OutputStream out)
		throws IOException, BadLocationException
	{
		save(new OutputStreamWriter(out));
	}

	private void save(Writer out)
		throws IOException, BadLocationException
	{
		String newline = jEdit.props.getProperty("buffer.line."
			+ "separator",System.getProperty("line.separator"));
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
			backups = Integer.parseInt(jEdit.props.getProperty(
				"buffer.backup.count"));
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

	private void setPath(String parent, String path)
	{
		// absolute pathnames
		if(path.startsWith(File.separator))
			parent = null;
		// windows pathnames, eg C:\document
		else if(path.length() >= 3 && path.charAt(1) == ':'
			&& path.charAt(2) == '\\')
			parent = null;
		// relative pathnames
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
			name = file.getName();
			markersUrl = new URL(url,'.' + new File(name)
				.getName() + ".marks");
			this.path = path;
		}
		catch(MalformedURLException mu)
		{
			if(parent == null)
				file = new File(path);
			else
				file = new File(parent,path);
			name = file.getName();
			markersFile = new File(file.getParent(),'.' + name
				+ ".marks");
			// sledgehammer fix
			/*if(newFile)
				this.path = name;
			else
			{*/
			try
			{
				this.path = file.getCanonicalPath();
			}
			catch(IOException io)
			{
				this.path = file.getPath();
			}
		}
		autosaveFile = new File(file.getParent(),'#' + name + '#');

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

	private void dirty()
	{
		if(!(dirty || readOnly))
		{
			dirty = !init;
			updateStatus();
		}
	}
}
