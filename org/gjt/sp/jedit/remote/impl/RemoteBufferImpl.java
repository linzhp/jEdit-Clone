/*
 * RemoteBufferImpl.java - RMI implementation of Buffer class
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
package org.gjt.sp.jedit.remote.impl;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.*;
import org.gjt.sp.jedit.remote.*;
import org.gjt.sp.jedit.*;

/**
 * An RMI implementation of the Buffer class.
 * @author Slava Pestov
 * @version $Id$
 */
public class RemoteBufferImpl extends UnicastRemoteObject
	implements RemoteBuffer
{
	public RemoteBufferImpl(Buffer buffer)
		throws RemoteException
	{
		this.buffer = buffer;
	}

	public void save(RemoteView view, String path)
		throws RemoteException
	{
		buffer.save(jEdit.getView(view.getUID()),path);
	}

	public String getText(int start, int len)
		throws RemoteException
	{
		try
		{
			return buffer.getText(start,len);
		}
		catch(BadLocationException bl)
		{
			return null;
		}
	}

	public int getLineStartOffset(int line)
		throws RemoteException
	{
		Element map = buffer.getDefaultRootElement();
		Element lineElement = map.getElement(map.getElementIndex(line));
		if(lineElement == null)
			return -1;
		else
			return lineElement.getStartOffset();
	}

	public int getLineEndOffset(int line)
		throws RemoteException
	{
		Element map = buffer.getDefaultRootElement();
		Element lineElement = map.getElement(map.getElementIndex(line));
		if(lineElement == null)
			return -1;
		else
			return lineElement.getEndOffset();
	}

	public String getPath()
		throws RemoteException
	{
		return buffer.getPath();
	}

	public boolean isDirty()
		throws RemoteException
	{
		return buffer.isDirty();
	}

	public boolean isClosed()
		throws RemoteException
	{
		return buffer.isClosed();
	}

	public int getUID()
		throws RemoteException
	{
		return buffer.getUID();
	}

	// package-private members
	Buffer buffer;
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.2  1999/06/15 05:03:54  sp
 * RMI interface complete, save all hack, views & buffers are stored as a link
 * list now
 *
 * Revision 1.1  1999/06/14 08:21:07  sp
 * Started rewriting `jEdit server' to use RMI (doesn't work yet)
 *
 */
