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

import javax.swing.SwingUtilities;
import java.net.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.*;
import org.gjt.sp.jedit.event.*;
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
	{
		jEdit.addEditorListener(editorHandler = new EditorHandler());
	}

	public void stop()
	{
		jEdit.removeEditorListener(editorHandler);
	}

	public RemoteBuffer newFile(final RemoteView view)
		throws RemoteException
	{
		final RemoteBuffer[] returnValue = new RemoteBuffer[1];
		try
		{
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run()
				{
					returnValue[0] = getRemoteBuffer(
						jEdit.newFile(getLocalView(view)));
				}
			});
		}
		catch(Exception e)
		{
		}
		return returnValue[0];
	}

	public RemoteBuffer openFile(final RemoteView view, final String parent,
		final String path, final boolean readOnly, final boolean newFile)
		throws RemoteException
	{
		final RemoteBuffer[] returnValue = new RemoteBuffer[1];
		try
		{
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run()
				{
					returnValue[0] = getRemoteBuffer(
						jEdit.openFile(getLocalView(view),
						parent,path,readOnly,newFile));
				}
			});
		}
		catch(Exception e)
		{
		}
		return returnValue[0];
	}

	public void closeBuffer(final RemoteView view, final RemoteBuffer buffer)
		throws RemoteException
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				jEdit.closeBuffer(getLocalView(view),
					getLocalBuffer(buffer));
			}
		});
	}

	public RemoteBuffer getBuffer(String path)
		throws RemoteException
	{
		return getRemoteBuffer(jEdit.getBuffer(path));
	}

	public RemoteBuffer[] getBuffers()
		throws RemoteException
	{
		Buffer[] buffers = jEdit.getBuffers();
		RemoteBuffer[] _buffers = new RemoteBuffer[buffers.length];
		for(int i = 0; i < buffers.length; i++)
		{
			_buffers[i] = getRemoteBuffer(buffers[i]);
		}
		return _buffers;
	}

	public void waitForClose(RemoteBuffer buffer)
		throws RemoteException
	{
		synchronized(bufferWaitLock)
		{
			try
			{
				while(!buffer.isClosed())
					bufferWaitLock.wait();
			}
			catch(InterruptedException i)
			{
			}
		}
	}

	public RemoteView newView(final RemoteView view, final RemoteBuffer buffer)
		throws RemoteException
	{
		final RemoteView[] returnValue = new RemoteView[1];
		try
		{
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run()
				{
					returnValue[0] = getRemoteView(
						jEdit.newView(getLocalView(view),
						getLocalBuffer(buffer)));
				}
			});
		}
		catch(Exception e)
		{
		}
		return returnValue[0];
	}

	public void closeView(final RemoteView view)
		throws RemoteException
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				jEdit.closeView(getLocalView(view));
			}
		});
	}

	public RemoteView[] getViews()
		throws RemoteException
	{
		View[] views = jEdit.getViews();
		RemoteView[] _views = new RemoteView[views.length];
		for(int i = 0; i < views.length; i++)
		{
			_views[i] = getRemoteView(views[i]);
		}
		return _views;
	}

	public void waitForClose(RemoteView view)
		throws RemoteException
	{
		synchronized(viewWaitLock)
		{
			try
			{
				while(!view.isClosed())
					viewWaitLock.wait();
			}
			catch(InterruptedException i)
			{
			}
		}
	}

	public void exit(final RemoteView view)
		throws RemoteException
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				jEdit.exit(getLocalView(view));
			}
		});
	}

	public static View getLocalView(RemoteView view)
	{
		try
		{
			if(view == null)
				return null;
			else
				return jEdit.getView(view.getUID());
		}
		catch(RemoteException r)
		{
			// can be thrown by getUID() which is remote
			return null;
		}
	}

	public static RemoteView getRemoteView(View view)
	{
		try
		{
			return new RemoteViewImpl(view);
		}
		catch(RemoteException r)
		{
			return null;
		}
	}

	public static Buffer getLocalBuffer(RemoteBuffer buffer)
	{
		try
		{
			if(buffer == null)
				return null;
			else
				return jEdit.getBuffer(buffer.getUID());
		}
		catch(RemoteException r)
		{
			// can be thrown by getUID() which is remote
			return null;
		}
	}

	public static RemoteBuffer getRemoteBuffer(Buffer buffer)
	{
		try
		{
			return new RemoteBufferImpl(buffer);
		}
		catch(RemoteException r)
		{
			return null;
		}
	}

	class EditorHandler extends EditorAdapter
	{
		public void bufferClosed(EditorEvent evt)
		{
			synchronized(viewWaitLock)
			{
				viewWaitLock.notifyAll();
			}
		}

		public void viewClosed(EditorEvent evt)
		{
			synchronized(viewWaitLock)
			{
				viewWaitLock.notifyAll();
			}
		}
	}

	// private members
	private EditorHandler editorHandler;
	private Object bufferWaitLock = new Object();
	private Object viewWaitLock = new Object();
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.3  1999/07/08 06:06:04  sp
 * Bug fixes and miscallaneous updates
 *
 * Revision 1.2  1999/06/15 05:03:54  sp
 * RMI interface complete, save all hack, views & buffers are stored as a link
 * list now
 *
 * Revision 1.1  1999/06/14 08:21:07  sp
 * Started rewriting `jEdit server' to use RMI (doesn't work yet)
 *
 */
