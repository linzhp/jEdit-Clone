/*
 * HyperSearch.java - HyperSearch dialog
 * Copyright (C) 1998, 1999 Slava Pestov
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

package org.gjt.sp.jedit.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import gnu.regexp.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import org.gjt.sp.jedit.event.*;
import org.gjt.sp.jedit.search.*;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.View;

/**
 * HyperSearch dialog.
 * @author Slava Pestov
 * @version $Id$
 */
public class HyperSearch extends JDialog
{
	public HyperSearch(View view)
	{
		super(view,jEdit.getProperty("hypersearch.title"),false);
		this.view = view;
		positions = new Vector();
		Container content = getContentPane();
		content.setLayout(new BorderLayout());
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel(jEdit.getProperty("hypersearch.find")),
			BorderLayout.WEST);
		find = new HistoryTextField("find");
		panel.add(find, BorderLayout.CENTER);
		content.add(panel, BorderLayout.NORTH);

		JPanel stretchPanel = new JPanel(new BorderLayout());

		panel = new JPanel();
		ignoreCase = new JCheckBox(jEdit.getProperty(
			"search.ignoreCase"),
			"on".equals(jEdit.getProperty("search.ignoreCase.toggle")));
		panel.add(ignoreCase);
		panel.add(new JLabel(jEdit.getProperty("search.regexp")));
		regexpSyntax = new JComboBox(RESearchMatcher.SYNTAX_LIST);
		regexpSyntax.setSelectedItem(jEdit.getProperty("search"
			+ ".regexp.value"));
		panel.add(regexpSyntax);
		findBtn = new JButton(jEdit.getProperty("hypersearch.findBtn"));
		panel.add(findBtn);
		getRootPane().setDefaultButton(findBtn);
		close = new JButton(jEdit.getProperty("common.close"));
		panel.add(close);
		stretchPanel.add(panel,BorderLayout.NORTH);

		results = new JList();
		results.setVisibleRowCount(10);
		results.addListSelectionListener(new ListHandler());
		stretchPanel.add(new JScrollPane(results), BorderLayout.CENTER);

		content.add(stretchPanel, BorderLayout.CENTER);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		KeyHandler keyListener = new KeyHandler();
		ActionHandler actionListener = new ActionHandler();

		find.addKeyListener(keyListener);
		addKeyListener(keyListener);
		find.addActionListener(actionListener);
		findBtn.addActionListener(actionListener);
		close.addActionListener(actionListener);

		jEdit.addEditorListener(editorListener = new EditorHandler());
		pack();
		GUIUtilities.loadGeometry(this,"hypersearch");

		show();
		find.requestFocus();
	}
	
	public void save()
	{
		find.addCurrentToHistory();
		SearchAndReplace.setSearchString(find.getText());
		SearchAndReplace.setIgnoreCase(ignoreCase.getModel().isSelected());
		SearchAndReplace.setSyntax((String)regexpSyntax.getSelectedItem());
		GUIUtilities.saveGeometry(this,"hypersearch");
	}
	
	public void dispose()
	{
		jEdit.removeEditorListener(editorListener);
		super.dispose();
	}

	// private members
	private View view;
	private Buffer buffer;
	private HistoryTextField find;
	private JCheckBox ignoreCase;
	private JComboBox regexpSyntax;
	private JButton findBtn;
	private JButton close;
	private JList results;
	private Vector positions;
	private EditorHandler editorListener;

	private void doHyperSearch()
	{
		try
		{
			positions.removeAllElements();
			buffer = view.getBuffer();
			int tabSize = buffer.getTabSize();
			Vector data = new Vector();
			SearchMatcher matcher = SearchAndReplace
				.getSearchMatcher();
			if(matcher == null)
			{
				view.getToolkit().beep();
				return;
			}
			Element map = buffer.getDefaultRootElement();
			int lines = map.getElementCount();
			for(int i = 1; i <= lines; i++)
			{
				Element lineElement = map.getElement(i - 1);
				int start = lineElement.getStartOffset();
				String lineString = buffer.getText(start,
					lineElement.getEndOffset() - start
					- 1);
				int[] match = matcher.nextMatch(lineString);
				if(match != null)
				{
					data.addElement(i + ":"
						+ lineString);
					positions.addElement(buffer
						.createPosition(start + match[0]));
				}
			}
			if(data.isEmpty())
				view.getToolkit().beep();
			results.setListData(data);
		}
		catch(Exception e)
		{
			Object[] args = { e.getMessage() };
			if(args[0] == null)
				args[0] = e.toString();
			GUIUtilities.error(view,"searcherror",args);
		}
	}

	class ActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			Object source = evt.getSource();
			if(source == close)
				dispose();
			else if(source == findBtn || source == find)
			{
				save();
				doHyperSearch();
			}
		}
	}

	class EditorHandler extends EditorAdapter
	{
		public void bufferClosed(EditorEvent evt)
		{
			if(evt.getBuffer() == buffer)
			{
				positions.removeAllElements();
				// XXX: need empty model
				results.setModel(new DefaultListModel());
				// Can't give it a value in the bottom half
				buffer = null;
			}
		}
	}

	class KeyHandler extends KeyAdapter
	{
		public void keyPressed(KeyEvent evt)
		{
			if(evt.getKeyCode() == KeyEvent.VK_ESCAPE)	
				dispose();
		}
	}

	class ListHandler implements ListSelectionListener
	{
		public void valueChanged(ListSelectionEvent evt)
		{
			if(results.isSelectionEmpty() || evt.getValueIsAdjusting())
				return;

			Position pos = (Position)positions.elementAt(results
				.getSelectedIndex());
			Element map = buffer.getDefaultRootElement();
			Element lineElement = map.getElement(map.getElementIndex(pos
				.getOffset()));
			if(lineElement == null)
				return;

			int start = lineElement.getStartOffset();
			int end = lineElement.getEndOffset() - 1;

			if(view.getBuffer() == buffer)
			{
				view.getTextArea().select(start,end);
			}
			else
			{
				buffer.setCaretInfo(start,end);
				view.setBuffer(buffer);
			}
		}
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.31  1999/05/29 08:06:56  sp
 * Search and replace overhaul
 *
 * Revision 1.30  1999/05/09 03:50:17  sp
 * HistoryTextField is now a text field again
 *
 * Revision 1.29  1999/04/23 07:35:11  sp
 * History engine reworking (shared history models, history saved to
 * .jedit-history)
 *
 * Revision 1.28  1999/04/19 05:44:34  sp
 * GUI updates
 *
 * Revision 1.27  1999/04/08 04:44:51  sp
 * New _setBuffer method in View class, new addTab method in Console class
 *
 * Revision 1.26  1999/04/02 03:21:09  sp
 * Added manifest file, common strings such as OK, etc are no longer duplicated
 * many times in jedit_gui.props
 *
 * Revision 1.25  1999/04/02 02:39:46  sp
 * Updated docs, console fix, getDefaultSyntaxColors() method, hypersearch update
 *
 * Revision 1.24  1999/04/01 04:13:00  sp
 * Bug fixing for 1.5final
 *
 * Revision 1.23  1999/03/27 02:45:07  sp
 * New JEditTextArea class that adds jEdit-specific features to SyntaxTextArea
 *
 * Revision 1.22  1999/03/20 04:52:55  sp
 * Buffer-specific options panel finished, attempt at fixing OS/2 caret bug, code
 * cleanups
 *
 * Revision 1.21  1999/03/19 08:32:22  sp
 * Added a status bar to views, Escape key now works in dialog boxes
 *
 * Revision 1.20  1999/03/19 07:12:11  sp
 * JOptionPane changes, did a fromdos of the source
 *
 */
