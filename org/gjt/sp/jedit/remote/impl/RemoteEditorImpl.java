/*
 * RemoteEditorImpl.java - RMI implementation of jEdit class
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

import java.net.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.*;
import org.gjt.sp.jedit.remote.*;
import org.gjt.sp.jedit.*;

/**
 * An RMI implementation of the jEdit class.
 * @author Slava Pestov
 * @version $Id$
 */
public class RemoteEditorImpl extends UnicastRemoteObject
	implements RemoteEditor
{
	public RemoteEditorImpl()
		throws RemoteException
	{}

	public RemoteBuffer newFile(RemoteView view)
		throws RemoteException
	{
		System.out.println("newFile() executing here");
		return new RemoteBufferImpl(jEdit.newFile(
			view == null ? null : ((RemoteViewImpl)view).view));
	}

	public RemoteBuffer openFile(RemoteView view, String parent, String path,
		boolean readOnly, boolean newFile)
		throws RemoteException
	{
		System.out.println("openFile() executing here");
		return new RemoteBufferImpl(jEdit.openFile(
			view == null ? null : ((RemoteViewImpl)view).view,
			parent,path,readOnly,newFile));
	}

	public void closeBuffer(RemoteView view, RemoteBuffer buffer)
		throws RemoteException
	{
		jEdit.closeBuffer(
			view == null ? null : ((RemoteViewImpl)view).view,
			((RemoteBufferImpl)buffer).buffer);
	}

	public RemoteBuffer getBuffer(String path)
		throws RemoteException
	{
		return new RemoteBufferImpl(jEdit.getBuffer(path));
	}

	public RemoteBuffer[] getBuffers()
		throws RemoteException
	{
		Buffer[] buffers = jEdit.getBuffers();
		RemoteBuffer[] _buffers = new RemoteBuffer[buffers.length];
		for(int i = 0; i < buffers.length; i++)
		{
			_buffers[i] = new RemoteBufferImpl(buffers[i]);
		}
		return _buffers;
	}

	public void waitForClose(RemoteBuffer buffer)
		throws RemoteException
	{
	}

	public RemoteView newView(RemoteView view, RemoteBuffer buffer)
		throws RemoteException
	{
		System.out.println("newView() executing here");
		System.out.println(buffer.getClass().getName());
		return new RemoteViewImpl(jEdit.newView(
			view == null ? null : ((RemoteViewImpl)view).view,
			buffer == null ? null : ((RemoteBufferImpl)buffer).buffer));
	}

	public void closeView(RemoteView view)
		throws RemoteException
	{
		jEdit.closeView(((RemoteViewImpl)view).view);
	}

	public RemoteView[] getViews()
		throws RemoteException
	{
		View[] views = jEdit.getViews();
		RemoteView[] _views = new RemoteView[views.length];
		for(int i = 0; i < views.length; i++)
		{
			_views[i] = new RemoteViewImpl(views[i]);
		}
		return _views;
	}

	public void waitForClose(RemoteView view)
		throws RemoteException
	{
	}

	public void exit(RemoteView view)
		throws RemoteException
	{
		jEdit.exit(view == null ? null : ((RemoteViewImpl)view).view);
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.1  1999/06/14 08:21:07  sp
 * Started rewriting `jEdit server' to use RMI (doesn't work yet)
 *
 */
