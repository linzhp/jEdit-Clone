/*
 * RemoteEditor.java - RMI mirror of jEdit class
 * Copyright (C) 1999 Slava Pestov
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA.
 */
package org.gjt.sp.jedit.remote;

import java.rmi.*;
import org.gjt.sp.jedit.*;

/**
 * An RMI mirror of the jEdit class.
 * @author Slava Pestov
 * @version $Id$
 */
public interface RemoteEditor extends Remote
{
	RemoteBuffer newFile(RemoteView view)
		throws RemoteException;

	RemoteBuffer openFile(RemoteView view, String parent, String path,
		boolean readOnly, boolean newFile)
		throws RemoteException;

	void closeBuffer(RemoteView view, RemoteBuffer buffer)
		throws RemoteException;

	RemoteBuffer getBuffer(String path)
		throws RemoteException;

	RemoteBuffer[] getBuffers()
		throws RemoteException;

	void waitForClose(RemoteBuffer buffer)
		throws RemoteException;

	RemoteView newView(RemoteView view, RemoteBuffer buffer)
		throws RemoteException;

	void closeView(RemoteView view)
		throws RemoteException;

	RemoteView[] getViews()
		throws RemoteException;

	void waitForClose(RemoteView view)
		throws RemoteException;

	void exit(RemoteView view)
		throws RemoteException;
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.1  1999/06/14 08:21:07  sp
 * Started rewriting `jEdit server' to use RMI (doesn't work yet)
 *
 */
