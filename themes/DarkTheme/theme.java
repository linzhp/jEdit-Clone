/*
 * theme.java - Dummy action to clear user-defined colors
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

import java.awt.event.ActionEvent;
import org.gjt.sp.jedit.*;

public class theme extends EditAction
{
	public theme()
	{
		super("-theme-prop-unset-daemon-",false);
		jEdit.unsetProperty("buffer.colors.comment1");
		jEdit.unsetProperty("buffer.colors.comment2");
		jEdit.unsetProperty("buffer.colors.keyword1");
		jEdit.unsetProperty("buffer.colors.keyword2");
		jEdit.unsetProperty("buffer.colors.keyword3");
		jEdit.unsetProperty("buffer.colors.label");
		jEdit.unsetProperty("buffer.colors.literal1");
		jEdit.unsetProperty("buffer.colors.literal2");
		jEdit.unsetProperty("buffer.colors.operator");
		jEdit.unsetProperty("buffer.colors.invalid");
	}
	
	public void actionPerformed(ActionEvent evt)
	{
		throw new InternalError();
	}
}
