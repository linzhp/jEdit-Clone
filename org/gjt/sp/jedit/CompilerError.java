/*
 * CompilerError.java - Compiler error
 * Copyright (C) 1999 Slava Pestov
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

import javax.swing.text.Element;
import javax.swing.text.Position;
import java.io.File;
import org.gjt.sp.jedit.event.*;

/**
 * A compiler error. When jEdit runs an external command, it attempts to
 * parse any errors, and creates <code>CompilerError</code> instances for
 * those it can.<p>
 *
 * A list of compiler errors is placed in the `Errors' tab of the
 * <code>org.gjt.sp.jedit.gui.Console</code> component. Also, the
 * Tools-&gt;Next Error and Tools-&lt;Previous Error commands can be
 * used to go to the next and previous errors, respectively.<p>
 *
 * A list of compiler errors can be obtained with the
 * <code>Console.getErrorList()</code> method. It returns an
 * implementation of <code>ListModel</code> that can be used by
 * a JList.<p>
 *
 * The `current' compiler error, for use with the Next Error and
 * Previous Error commands, can be obtained and changed with the
 * <code>Console.getCurrentError()</code> and
 * <code>Console.setCurrentError()</code> methods.<p>
 *
 * An instance of the console component exists for each view, and
 * one can be obtained with the <code>View.getConsole()</code>
 * method.
 *
 * @author Slava Pestov
 * @version $Id$
 *
 * @see org.gjt.sp.jedit.gui.Console
 * @see org.gjt.sp.jedit.gui.Console#getErrorList()
 * @see org.gjt.sp.jedit.gui.Console#getError()
 * @see org.gjt.sp.jedit.gui.Console#getCurrentError()
 * @see org.gjt.sp.jedit.gui.Console#setCurrentError()
 * @see org.gjt.sp.jedit.View#getConsole()
 */
public class CompilerError implements EditorListener
{
	/**
	 * Creates a new compiler error. This constructor should
	 * not normally be invoked.
	 * @param path The path name of the file involved
	 * @param lineNo The line number
	 * @param error The error message
	 */
	public CompilerError(String path, int lineNo, String error)
	{
		this.path = MiscUtilities.constructPath(System
			.getProperty("user.dir"),path);
		this.lineNo = lineNo - 1;
		this.error = error;

		jEdit.addEditorListener(this);

		name = new File(path).getName();

		Buffer buffer = jEdit.getBuffer(this.path);
		if(buffer != null)
			openNotify(buffer);
	}

	/**
	 * Returns the path name of the file involved.
	 */
	public String getPath()
	{
		return path;
	}

	/**
	 * Returns the line number where the error occured. If the
	 * file is open, the line number position `floats' as the
	 * file is changed.
	 */
	public int getLineNo()
	{
		if(linePos != null)
			return buffer.getDefaultRootElement().getElementIndex(
				linePos.getOffset());
		else
			return lineNo;
	}

	/**
	 * Returns the buffer where the error occured. This may be null
	 * if the file isn't open.
	 */
	public Buffer getBuffer()
	{
		return buffer;
	}

	/**
	 * Returns the error message.
	 */
	public String getError()
	{
		return error;
	}

	/**
	 * Returns the buffer where the error occured. This is the
	 * same as <code>getBuffer()</code> except that the file is
	 * opened if it isn't already first.
	 */
	public Buffer openFile()
	{
		// Try opening the file
		if(buffer == null)
			buffer = jEdit.openFile(null,null,path,false,false);

		return buffer;
	}

	/**
	 * Returns a string representation of this error. This is
	 * the string displayed in the error list.
	 */
	public String toString()
	{
		return name + ":" + (getLineNo() + 1) + ":" + getError();
	}

	// event listeners
	
	// BEGIN EDITOR LISTENER
	public void bufferCreated(EditorEvent evt)
	{
		if(buffer != null && evt.getBuffer().getPath().equals(path))
			openNotify(evt.getBuffer());
	}

	public void bufferClosed(EditorEvent evt)
	{
		if(evt.getBuffer() == buffer)
			closeNotify();
	}

	public void viewCreated(EditorEvent evt) {}
	public void viewClosed(EditorEvent evt) {}
	public void bufferDirtyChanged(EditorEvent evt) {}
	public void propertiesChanged(EditorEvent evt) {}
	// END EDITOR LISTENER

	// private members
	private String path;
	private String name;
	private Buffer buffer;
	private int lineNo;
	private Position linePos;
	private String error;

	private void openNotify(Buffer buffer)
	{
		this.buffer = buffer;
		Element map = buffer.getDefaultRootElement();
		try
		{
			Element lineElement = map.getElement(lineNo);
			if(lineElement != null)
			{
				linePos = buffer.createPosition(lineElement
					.getStartOffset());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void closeNotify()
	{
		buffer = null;
		linePos = null;
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.10  1999/04/01 04:13:00  sp
 * Bug fixing for 1.5final
 *
 * Revision 1.9  1999/03/27 00:44:15  sp
 * Documentation updates, various bug fixes
 *
 * Revision 1.8  1999/03/22 04:20:01  sp
 * Syntax colorizing updates
 *
 * Revision 1.7  1999/03/12 07:23:19  sp
 * Fixed serious view bug, Javadoc updates
 *
 */
