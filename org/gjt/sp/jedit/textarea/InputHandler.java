/*
 * InputHandler.java - Manages key bindings and executes actions
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

package org.gjt.sp.jedit.textarea;

import java.awt.event.ActionListener;
import java.awt.event.KeyListener;

/**
 * Handles key events and executes actions bound to keystrokes.
 * @author Slava Pestov
 * @version $Id$
 */
public interface InputHandler extends KeyListener
{
	public void install(JEditTextArea textArea);
	public void uninstall(JEditTextArea textArea);
	public void addKeyBinding(String keyBinding, ActionListener action);
	public void removeKeyBinding(String keyBinding);
	public void removeAllKeyBindings();
}
