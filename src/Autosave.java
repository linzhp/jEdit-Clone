/*
 * Autosave.java - Autosave thread
 * Copyright (C) 1998 Slava Pestov
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

import java.util.Enumeration;

public class Autosave extends Thread
{
	public Autosave()
	{
		super("***jEdit autosave thread***");
		start();
	}

	public void run()
	{
		int interval;
		try
		{
			interval = Integer.parseInt(jEdit.props.getProperty(
				"daemon.autosave.interval"));
		}
		catch(NumberFormatException nf)
		{
			interval = 30;
		}
		if(interval == 0)
			return;
		interval *= 1000;
		for(;;)
		{
			try
			{
				Thread.sleep(interval);
			}
			catch(InterruptedException i)
			{
			}
			Enumeration enum = jEdit.buffers.getBuffers();
			while(enum.hasMoreElements())
				((Buffer)enum.nextElement()).autosave();
		}
	}
}
