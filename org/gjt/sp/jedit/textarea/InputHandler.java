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
 * An abstract interface for a key event handler. Concrete implementations
 * provide specific keystroke to action mappings.
 * @author Slava Pestov
 * @version $Id$
 * @see org.gjt.sp.jedit.textarea.DefaultInputHandler
 */
public interface InputHandler extends KeyListener
{
	/**
	 * Adds the default key bindings to this input handler.
	 */
	public void addDefaultKeyBindings();

	/**
	 * Adds a key binding to this input handler.
	 * @param keyBinding The key binding (the format of this is
	 * input-handler specific)
	 * @param action The action
	 */
	public void addKeyBinding(String keyBinding, ActionListener action);

	/**
	 * Removes a key binding from this input handler.
	 * @param keyBinding The key binding
	 */
	public void removeKeyBinding(String keyBinding);

	/**
	 * Removes all key bindings from this input handler.
	 */
	public void removeAllKeyBindings();

	/**
	 * Grabs the next key typed event and invokes the specified
	 * action with the key as a the action command.
	 * @param action The action
	 */
	public void grabNextKeyStroke(ActionListener listener);

	/**
	 * Returns if repeating is enabled. When repeating is enabled,
	 * actions will be executed multiple times. This is usually
	 * invoked with a special key stroke in the input handler.
	 */
	public boolean isRepeatEnabled();

	/**
	 * Enables repeating. When repeating is enabled, actions will be
	 * executed multiple times. Once repeating is enabled, the input
	 * handler should read a number from the keyboard.
	 */
	public void setRepeatEnabled(boolean repeat);

	/**
	 * Returns the number of times the next action will be repeated.
	 */
	public int getRepeatCount();

	/**
	 * Sets the number of times the next action will be repeated.
	 * @param repeatCount The repeat count
	 */
	public void setRepeatCount(int repeatCount);

	/**
	 * Returns a copy of this input handler that shares the same
	 * key bindings. Setting key bindings in the copy will also
	 * set them in the original.
	 */
	public InputHandler copy();

	/**
	 * If an action implements this interface, it should not be repeated.
	 */
	public static interface NonRepeatable {}
}
