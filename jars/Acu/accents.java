/*
 * Accents.java - part of the jEdit Acu plugin - v1.2
 * Converts HTML entities to accentuated chars
 * Copyright (C) 1999 Romain Guy - Speed improvements thanks to Slava Pestov
 * Very minor modifications copyright (C) 1999 Slava Pestov
 *
 * www.chez.com/powerteam
 * powerteam@chez.com
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

import org.gjt.sp.jedit.*;
import java.util.Hashtable;
import java.awt.event.ActionEvent;
import org.gjt.sp.jedit.gui.SyntaxTextArea;
import javax.swing.text.BadLocationException;

public class accents extends EditAction
{
  public accents()
  {
    super("accents", true);
  }

  public void actionPerformed(ActionEvent evt)
  {
    View view = getView(evt);
    Buffer buffer = view.getBuffer();
    SyntaxTextArea textArea = view.getTextArea();
    String selection = textArea.getSelectedText();

    if (selection != null)
      textArea.replaceSelection(doAcu(selection));
    else
    {
      try
      {
        String text = buffer.getText(0, buffer.getLength());
        buffer.remove(0, buffer.getLength());
        buffer.insertString(0, doAcu(text), null);
      }
      catch(BadLocationException bl)
      {
      }
    }
  }

  public String doAcu(String html)
  {
    Hashtable replace = new Hashtable();
    replace.put("&eacute;", "é");
    replace.put("&egrave;", "è");
    replace.put("&ecirc;", "ê");
    replace.put("&euml;", "ë");
    replace.put("&agrave;", "à");
    replace.put("&acirc;", "â");
    replace.put("&auml;", "ä");
    replace.put("&icirc;", "î");
    replace.put("&iuml;", "ï");
    replace.put("&ugrave;", "ù");
    replace.put("&uuml;", "ü");
    replace.put("&ucirc;", "û");
    replace.put("&ograve;", "ô");
    replace.put("&ouml;", "ö");
    replace.put("&ccedil;", "ç");

    StringBuffer buf = new StringBuffer();
    int entityOff = -1;

    for (int i = 0; i < html.length(); i++)
    {
      switch(html.charAt(i))
      {
        case '&':
          if (entityOff == -1)
            entityOff = i;
          break;
        case ';':
          if (entityOff != -1)
          {
            String entity = html.substring(entityOff, i + 1);
            String accent = (String)replace.get(entity);
            buf.append(accent == null ? entity : accent);
	    entityOff = -1;
	    break;
	  }
        default:
          if (entityOff == -1)
            buf.append(html.charAt(i));
          break;
      }
    }
    return buf.toString();
  }
}

// End of Accents.java
