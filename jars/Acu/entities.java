/*
 * Entities.java - part of the jEdit Acu plugin - v1.2
 * Converts accentuated chars to HTML entities
 * Copyright (C) 1999 Romain Guy
 * Speed improvement thanks to Slava Pestov
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
import java.awt.event.ActionEvent;
import org.gjt.sp.jedit.gui.SyntaxTextArea;
import javax.swing.text.BadLocationException;

public class entities extends EditAction
{
  public entities()
  {
    super("entities", true);
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
    StringBuffer buf = new StringBuffer();
    for(int i = 0; i < html.length(); i++)
    {
      switch(html.charAt(i))
      {
	case 'é':
          buf.append("&eacute;");
	  break;
	case 'è':
          buf.append("&egrave;");
	  break;
	case 'ê':
          buf.append("&ecirc;");
	  break;	  
	case 'ë':
          buf.append("&euml;");
	  break;
	case 'à':
          buf.append("&agrave;");
	  break;
	case 'â':
          buf.append("&acirc;");
	  break;
	case 'ä':
          buf.append("&auml;");
	  break;
	case 'î':
          buf.append("&icirc;");
	  break;
	case 'ï':
          buf.append("&iuml;");
	  break;
	case 'ù':
          buf.append("&ugrave;");
	  break;
	case 'ü':
          buf.append("&uuml;");
	  break;
	case 'û':
          buf.append("&ucirc;");
	  break;
	case 'ô':
          buf.append("&ocirc;");
	  break;
	case 'ö':
          buf.append("&ouml;");
	  break;
	case 'ç':
          buf.append("&ccedil;");
	  break;
	default:
          buf.append(html.charAt(i));
      }
    }
    return buf.toString();   
  }
}

// End of Entities.java
