/*
 * Plugin.java - Obsolete plugin interface
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

package org.gjt.sp.jedit;

import java.util.Enumeration;
import java.util.Vector;
import org.gjt.sp.jedit.gui.OptionsDialog;

/**
 * The interface between a plugin and jEdit. The job of this interface is
 * to register any actions and modes provided with the plugin, and do
 * initial setup required at start up time.<p>
 *
 * Due to the way jEdit loads classes, Plugin implementation class names
 * must end with `Plugin', otherwise they will be ignored.<p>
 *
 * As of jEdit 1.5, EditActions and Modes aren't automatically loaded from
 * plugin JAR files on startup; it is the responsibility of the plugin
 * class implementation to do so.
 *
 * @author Slava Pestov
 * @version $Id$
 *
 * @deprecated As of jEdit 2.1pre1, should use EditPlugin class instead
 */
public interface Plugin
{
	/**
	 * Called at start up time when the plugin is being loaded.
	 */
	public void start();

	/**
	 * Called when jEdit is exiting.
	 */
	public void stop();

	// Converts old menu and option pane code (jEdit.addPluginAction(),
	// jEdit.addPluginMenu(), jEdit.addOptionPane()) to new. Only one
	// instance of this class exists, and it manages menus and option
	// panes for all legacy plugins.
	static class OldAPI extends EditPlugin
	{
		Vector pluginActions = new Vector();
		Vector pluginMenus = new Vector();

		// Called by jEdit.addPluginAction
		void addPluginAction(EditAction action)
		{
			pluginActions.addElement(action);
		}

		void addPluginMenu(String menu)
		{
			pluginMenus.addElement(menu);
		}

		public void createMenuItems(View view, Vector menus, Vector menuItems)
		{
			Enumeration menuEnum = pluginMenus.elements();
			while(menuEnum.hasMoreElements())
			{
				menus.addElement(GUIUtilities.loadMenu(view,
					(String)menuEnum.nextElement()));
			}

			Enumeration actionEnum = pluginActions.elements();
			while(actionEnum.hasMoreElements())
			{
				menuItems.addElement(GUIUtilities.loadMenuItem(view,
					((EditAction)actionEnum.nextElement())
					.getName()));
			}
		}
	}

	/**
	 * When a plugin is loaded into jEdit 2.1pre1 or later, it is
	 * wrapped in this class.
	 */
	public static class Wrapper extends EditPlugin
	{
		Wrapper(Plugin plugin) { this.plugin = plugin; }
		public void start() { plugin.start(); }
		public void stop() { plugin.stop(); }

		/**
		 * Returns the underlying old API plugin.
		 */
		public Plugin getPlugin()
		{
			return plugin;
		}

		Plugin plugin;
	}
}
