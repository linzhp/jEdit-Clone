/*
 * Acu.java - jEdit plugin - Converts accentuated chars to HTML entities
 * Copyright (C) 1999 Romain Guy
 * Very minor modifications (Marked with SP) copyright (C) 1999 Slava Pestov
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

import javax.swing.text.BadLocationException;
import java.awt.event.ActionEvent;
import org.gjt.sp.jedit.gui.SyntaxTextArea;
import org.gjt.sp.jedit.*;

public class acu extends EditAction
{
	// I made these public (not necessary, just makes it cleaner)
	// Also, constant names should be upper case
	public static final String ACCENT[] = { "é", "è", "ê", "ë", "à", "â",
		"ä", "î", "ï", "ù", "ü", "û", "ô", "ö", "ç" };
	// This is not Unicode - unicode is double-byte standard
	public static final String ENTITY[] = { "eacute", "egrave", "ecirc",
		"euml", "agrave", "acirc", "auml", "icirc", "iuml", "ugrave",
		"uuml", "ucirc", "ocirc", "ouml", "ccedil" };

	public acu()
	{
		super("acu",true);
	}

	public void actionPerformed(ActionEvent evt)
	{
		View view = getView(evt);
		Buffer buffer = view.getBuffer();
		SyntaxTextArea textArea = view.getTextArea();
		String selection = textArea.getSelectedText();
		if(selection != null)
			textArea.replaceSelection(doAcu(selection));
		else
		{
			// A better way to do this is with
			// buffer.remove(0,buffer.getLength())
			// buffer.insertString(...)
			// Does exactly the same, but
			// selectAll()/replaceSelection() is a bit messy
			// -- Slava Pestov
			try
			{
				String text = buffer.getText(0,buffer.getLength());
				buffer.remove(0,buffer.getLength());
				buffer.insertString(0,doAcu(text),null);
			}
			catch(BadLocationException bl)
			{
			}
			/*
			textArea.selectAll();
			textArea.replaceSelection(doAcu(textArea.getSelectedText()));
			*/
		}
	}

	public String doAcu(String html)
	{
		// Maybe you should use a StringBuffer here?
		// Creating all these substring()'s might be a little slow - SP
		String compare, replace;
		for (int i = 0; i < ACCENT.length; i++)
		{
			int search = 0;
			compare = ACCENT[i];
			replace = "&" + ENTITY[i] + ";";
			while (search != -1)
			{
				search = html.indexOf(compare);
				if (search != -1)
					html = html.substring(0, search)
						+ replace + html.substring(
						search + compare.length());
			}
		}
		return html;
	}
}
