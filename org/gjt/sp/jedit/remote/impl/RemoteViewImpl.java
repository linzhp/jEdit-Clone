/*
 * RemoteViewImpl.java - RMI implementation of View class
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

import java.rmi.server.UnicastRemoteObject;
import java.rmi.*;
import org.gjt.sp.jedit.remote.*;
import org.gjt.sp.jedit.*;

/**
 * An RMI implementation of the View class.
 * @author Slava Pestov
 * @version $Id$
 */
public class RemoteViewImpl extends UnicastRemoteObject implements RemoteView
{
	public RemoteViewImpl(View view)
		throws RemoteException
	{
		this.view = view;
	}

	public void setCaretPosition(int pos) throws RemoteException
	{
		view.getTextArea().setCaretPosition(pos);
	}

	public void select(int start, int end) throws RemoteException
	{
		view.getTextArea().select(start,end);
	}

	public RemoteBuffer getBuffer() throws RemoteException
	{
		return new RemoteBufferImpl(view.getBuffer());
	}

	public void setBuffer(RemoteBuffer buffer) throws RemoteException
	{
		view.setBuffer(jEdit.getBuffer(buffer.getUID()));
	}

	public boolean isClosed()
		throws RemoteException
	{
		return view.isClosed();
	}

	public int getUID()
		throws RemoteException
	{
		return view.getUID();
	}

	// package-private members
	View view;
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
