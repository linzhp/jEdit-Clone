/*
 * AbstractEditorAdapter.java - Abstract listener adapter
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

package org.gjt.sp.jedit.event;

import java.util.EventListener;

/**
 * There are two ways to implement an event listener - either to
 * directory implement a listener interface, possibly inserting
 * empty stubs for unused methods, or to subclass an <i>adapter</i>
 * - an abstract class, implementing that interface, with empty
 * stubs for all required methods. If an adapter is subclassed,
 * only the used methods need to be implemented.
 */
public abstract class AbstractEditorAdapter implements AbstractEditorListener
{
}
