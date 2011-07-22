/*
 * ListDirectoryBrowserTask
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright © 2010 Matthieu Casanova
 * Portions Copyright (C) 2000, 2003 Slava Pestov
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

package org.gjt.sp.jedit.browser;

//{{{ Imports
import org.gjt.sp.jedit.io.VFS;
import org.gjt.sp.jedit.io.VFSFile;
import org.gjt.sp.jedit.io.VFSManager;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.util.Log;

import java.io.IOException;
//}}}

/**
 * @author Matthieu Casanova
 * @version $Id$
 */
class ListDirectoryBrowserTask extends AbstractBrowserTask
{
	//{{{ BrowserIORequest constructor
	/**
	 * Creates a new browser I/O request.
	 * @param browser The VFS browser instance
	 * @param path1 The first path name to operate on
	 * @param loadInfo A two-element array filled out by the request;
	 * element 1 is the canonical path, element 2 is the file list.
	 */
	ListDirectoryBrowserTask(VFSBrowser browser,
		Object session, VFS vfs, String path1,
		Object[] loadInfo, Runnable awtRunnable)
	{
		super(browser, session, vfs, path1, null, loadInfo, awtRunnable);
	} //}}}

	//{{{ run() method
	@Override
	public void _run()
	{
		String[] args = { path1 };
		setStatus(jEdit.getProperty("vfs.status.listing-directory",args));

		String canonPath = path1;

		VFSFile[] directory = null;
		try
		{
			setCancellable(true);

			canonPath = vfs._canonPath(session,path1,browser);
			directory = vfs._listFiles(session,canonPath,browser);
		}
		catch(IOException io)
		{
			setCancellable(false);
			Log.log(Log.ERROR,this,io);
			String[] pp = { io.toString() };
			VFSManager.error(browser,path1,"ioerror.directory-error",pp);
		}
		finally
		{
			try
			{
				vfs._endVFSSession(session,browser);
			}
			catch(IOException io)
			{
				setCancellable(false);
				Log.log(Log.ERROR,this,io);
				String[] pp = { io.toString() };
				VFSManager.error(browser,path1,"ioerror.directory-error",pp);
			}
		}

		setCancellable(false);

		loadInfo[0] = canonPath;
		loadInfo[1] = directory;
	} //}}}

	//{{{ toString() method
	public String toString()
	{
		return getClass().getName() + "[type=LIST_DIRECTORY"
			+ ",vfs=" + vfs + ",path1=" + path1 + ']';
	} //}}}
}