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
 * The superclass of all adapters - event listener implementations with
 * all empty methods.
 *
 * @author Slava Pestov
 * @version $Id$
 */
public abstract class AbstractEditorAdapter implements AbstractEditorListener
{
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.3  1999/03/14 04:13:40  sp
 * Fixed ArrayIndexOutOfBounds in TokenMarker, minor Javadoc updates, minor documentation updates
 *
 * Revision 1.2  1999/03/12 23:51:00  sp
 * Console updates, uncomment removed cos it's too buggy, cvs log tags added
 *
 */
