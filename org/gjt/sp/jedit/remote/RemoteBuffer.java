/*
 * RemoteBuffer.java - RMI mirror of Buffer class
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
 * An RMI mirror of the Buffer class.
 * @author Slava Pestov
 * @version $Id$
 */
public interface RemoteBuffer extends Remote
{
	void save(RemoteView view, String path)
		throws RemoteException;

	String getText(int start, int len)
		throws RemoteException;

	int getLineStartOffset(int line)
		throws RemoteException;

	int getLineEndOffset(int line)
		throws RemoteException;

	String getPath()
		throws RemoteException;

	boolean isDirty()
		throws RemoteException;

	boolean isClosed()
		throws RemoteException;
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.1  1999/06/14 08:21:07  sp
 * Started rewriting `jEdit server' to use RMI (doesn't work yet)
 *
 */
