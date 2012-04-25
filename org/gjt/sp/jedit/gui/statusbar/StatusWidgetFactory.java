/*
 * StatusWidgetFactory.java - The service for widget of the status bar
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2008 Matthieu Casanova
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

package org.gjt.sp.jedit.gui.statusbar;

import org.gjt.sp.jedit.View;

/** A widget factory for the status bar.
 * 
 * Implement this interface and register via
 * services.xml to add another status bar widget. 
 *
 * NOTE: The "name" of this service (in services.xml) is actually
 * org.gjt.sp.jedit.gui.statusbar.StatusWidget, although there is no 
 * actual class or interface of that exact name. 
 *
 * 
 * @author Matthieu Casanova
 * @since jEdit 4.3pre14 
 */
public interface StatusWidgetFactory 
{
	/**
	 * returns an instance of Widget for the given view
	 * @param view the view to which the created widget will belong
	 * @return a widget instance
	 */
	Widget getWidget(View view);
}
