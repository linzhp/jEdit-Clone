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
 * A compiler error.
 */
public class CompilerError implements EditorListener
{
	/**
	 * Creates a new compiler error.
	 * @param path The path name of the file involved
	 * @param lineNo The line number
	 * @param error The error message
	 */
	public CompilerError(String path, int lineNo, String error)
	{
		this.path = MiscUtilities.constructPath(System.getProperty(
			"user.dir"),path);
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
	 * Returns the line number where the error occured.
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
	 * Returns the buffer where the error occured. This may be null.
	 */
	public Buffer getBuffer()
	{
		return buffer;
	}

	/**
	 * Returns the error message itsef.
	 */
	public String getError()
	{
		return error;
	}

	/**
	 * Returns the buffer, ensuring that it exists first.
	 */
	public Buffer openFile()
	{
		if(buffer == null)
			jEdit.openFile(null,null,path,false,false);
		// We should've gotten an openNotify
		if(buffer == null)
		{
			System.err.println("BUG: no openNotify() received");
			System.err.println("Report this to Slava Pestov <sp@gjt.org>");
		}
		return buffer;
	}

	/**
	 * Returns a string representation of this error.
	 */
	public String toString()
	{
		return name + ":" + getLineNo() + ":" + getError();
	}

	// event listeners
	
	// BEGIN EDITOR LISTENER
	public void bufferCreated(EditorEvent evt)
	{
		if(evt.getBuffer().getPath().equals(path))
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
			linePos = buffer.createPosition(map.getElement(lineNo)
				.getStartOffset());
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
