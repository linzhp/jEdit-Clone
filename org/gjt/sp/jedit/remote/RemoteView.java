/*
 * RemoteView.java - RMI mirror of View class
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
 * An RMI mirror of the View class.
 * @author Slava Pestov
 * @version $Id$
 */
public interface RemoteView extends Remote
{
	void setCaretPosition(int pos)
		throws RemoteException;

	void select(int start, int end)
		throws RemoteException;

	RemoteBuffer getBuffer()
		throws RemoteException;

	void setBuffer(RemoteBuffer buffer)
		throws RemoteException;

	boolean isClosed()
		throws RemoteException;

	int getUID()
		throws RemoteException;
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
