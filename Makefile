# Makefile for jEdit 1.3.x
resources=gnu/regexp/RESyntax.class \
	gnu/regexp/CharIndexed.class \
	gnu/regexp/REMatch.class \
	gnu/regexp/REToken.class \
	gnu/regexp/REException.class \
	gnu/regexp/RETokenChar.class \
	gnu/regexp/RETokenRange.class \
	gnu/regexp/RETokenPOSIX.class \
	gnu/regexp/RETokenOneOf.class \
	gnu/regexp/RETokenAny.class \
	gnu/regexp/RETokenRepeated.class \
	gnu/regexp/RETokenBackRef.class \
	gnu/regexp/CharIndexedString.class \
	gnu/regexp/CharIndexedCharArray.class \
	gnu/regexp/CharIndexedStringBuffer.class \
	gnu/regexp/CharIndexedInputStream.class \
	gnu/regexp/RE.class \
	gnu/regexp/RETokenStart.class \
	gnu/regexp/RETokenEnd.class \
	gnu/regexp/REMatchEnumeration.class \
	org/gjt/sp/jedit/jEdit.class \
	org/gjt/sp/jedit/Buffer.class \
	org/gjt/sp/jedit/EditAction.class \
	org/gjt/sp/jedit/Marker.class \
	org/gjt/sp/jedit/Mode.class \
	org/gjt/sp/jedit/View.class \
	org/gjt/sp/jedit/actions/about.class \
	org/gjt/sp/jedit/actions/browser_open_sel.class \
	org/gjt/sp/jedit/actions/browser_open_url.class \
	org/gjt/sp/jedit/actions/clear.class \
	org/gjt/sp/jedit/actions/clear_marker.class \
	org/gjt/sp/jedit/actions/close_file.class \
	org/gjt/sp/jedit/actions/close_view.class \
	org/gjt/sp/jedit/actions/compile.class \
	org/gjt/sp/jedit/actions/copy.class \
	org/gjt/sp/jedit/actions/cut.class \
	org/gjt/sp/jedit/actions/delete_end_line.class \
	org/gjt/sp/jedit/actions/delete_line.class \
	org/gjt/sp/jedit/actions/delete_no_indent.class \
	org/gjt/sp/jedit/actions/delete_paragraph.class \
	org/gjt/sp/jedit/actions/delete_start_line.class \
	org/gjt/sp/jedit/actions/execute.class \
	org/gjt/sp/jedit/actions/exit.class \
	org/gjt/sp/jedit/actions/expand_abbrev.class \
	org/gjt/sp/jedit/actions/find.class \
	org/gjt/sp/jedit/actions/find_next.class \
	org/gjt/sp/jedit/actions/find_selection.class \
	org/gjt/sp/jedit/actions/format.class \
	org/gjt/sp/jedit/actions/goto_anchor.class \
	org/gjt/sp/jedit/actions/goto_line.class \
	org/gjt/sp/jedit/actions/goto_marker.class \
	org/gjt/sp/jedit/actions/help.class \
	org/gjt/sp/jedit/actions/hypersearch.class \
	org/gjt/sp/jedit/actions/insert_date.class \
	org/gjt/sp/jedit/actions/join_lines.class \
	org/gjt/sp/jedit/actions/locate_bracket.class \
	org/gjt/sp/jedit/actions/make.class \
	org/gjt/sp/jedit/actions/new_file.class \
	org/gjt/sp/jedit/actions/new_view.class \
	org/gjt/sp/jedit/actions/next_paragraph.class \
	org/gjt/sp/jedit/actions/open_file.class \
	org/gjt/sp/jedit/actions/open_path.class \
	org/gjt/sp/jedit/actions/open_selection.class \
	org/gjt/sp/jedit/actions/open_url.class \
	org/gjt/sp/jedit/actions/options.class \
	org/gjt/sp/jedit/actions/paste.class \
	org/gjt/sp/jedit/actions/paste_previous.class \
	org/gjt/sp/jedit/actions/pipe_selection.class \
	org/gjt/sp/jedit/actions/prev_paragraph.class \
	org/gjt/sp/jedit/actions/print.class \
	org/gjt/sp/jedit/actions/redo.class \
	org/gjt/sp/jedit/actions/replace.class \
	org/gjt/sp/jedit/actions/replace_all.class \
	org/gjt/sp/jedit/actions/replace_next.class \
	org/gjt/sp/jedit/actions/save.class \
	org/gjt/sp/jedit/actions/save_as.class \
	org/gjt/sp/jedit/actions/save_url.class \
	org/gjt/sp/jedit/actions/select_all.class \
	org/gjt/sp/jedit/actions/select_anchor.class \
	org/gjt/sp/jedit/actions/select_block.class \
	org/gjt/sp/jedit/actions/select_buffer.class \
	org/gjt/sp/jedit/actions/select_mode.class \
	org/gjt/sp/jedit/actions/select_next_paragraph.class \
	org/gjt/sp/jedit/actions/select_no_indent.class \
	org/gjt/sp/jedit/actions/select_prev_paragraph.class \
	org/gjt/sp/jedit/actions/send.class \
	org/gjt/sp/jedit/actions/set_anchor.class \
	org/gjt/sp/jedit/actions/set_marker.class \
	org/gjt/sp/jedit/actions/shift_left.class \
	org/gjt/sp/jedit/actions/shift_right.class \
	org/gjt/sp/jedit/actions/tab.class \
	org/gjt/sp/jedit/actions/to_lower.class \
	org/gjt/sp/jedit/actions/to_upper.class \
	org/gjt/sp/jedit/actions/undo.class \
	org/gjt/sp/jedit/actions/untab.class \
	org/gjt/sp/jedit/actions/word_count.class \
	org/gjt/sp/jedit/gui/CommandOutput.class \
	org/gjt/sp/jedit/gui/HelpViewer.class \
	org/gjt/sp/jedit/gui/HyperSearch.class \
	org/gjt/sp/jedit/gui/Options.class \
	org/gjt/sp/jedit/gui/PastePrevious.class \
	org/gjt/sp/jedit/gui/SearchAndReplace.class \
	org/gjt/sp/jedit/gui/SendDialog.class \
	org/gjt/sp/jedit/gui/SplashScreen.class \
	org/gjt/sp/jedit/gui/SyntaxTextArea.class \
	org/gjt/sp/jedit/mode/amstex.class \
	org/gjt/sp/jedit/mode/autoindent.class \
	org/gjt/sp/jedit/mode/bat.class \
	org/gjt/sp/jedit/mode/c.class \
        org/gjt/sp/jedit/mode/cc.class \
	org/gjt/sp/jedit/mode/html.class \
	org/gjt/sp/jedit/mode/java_mode.class \
	org/gjt/sp/jedit/mode/javascript.class \
	org/gjt/sp/jedit/mode/latex.class \
	org/gjt/sp/jedit/mode/makefile.class \
	org/gjt/sp/jedit/mode/sh.class \
	org/gjt/sp/jedit/mode/tex.class \
	org/gjt/sp/jedit/options/Colors1OptionPane.class \
	org/gjt/sp/jedit/options/Colors2OptionPane.class \
	org/gjt/sp/jedit/options/ColorsOptionPane.class \
	org/gjt/sp/jedit/options/EditorOptionPane.class \
	org/gjt/sp/jedit/options/GeneralOptionPane.class \
	org/gjt/sp/jedit/options/OptionPane.class \
	org/gjt/sp/jedit/syntax/BatchFileTokenMarker.class \
	org/gjt/sp/jedit/syntax/CTokenMarker.class \
	org/gjt/sp/jedit/syntax/HTMLTokenMarker.class \
	org/gjt/sp/jedit/syntax/KeywordMap.class \
	org/gjt/sp/jedit/syntax/MakefileTokenMarker.class \
	org/gjt/sp/jedit/syntax/ShellScriptTokenMarker.class \
	org/gjt/sp/jedit/syntax/SyntaxEditorKit.class \
	org/gjt/sp/jedit/syntax/SyntaxView.class \
	org/gjt/sp/jedit/syntax/TeXTokenMarker.class \
	org/gjt/sp/jedit/syntax/Token.class \
	org/gjt/sp/jedit/syntax/TokenMarker.class
no_compile_res=gnu/regexp/IntPair.class \
	gnu/regexp/CharUnit.class \
	'org/gjt/sp/jedit/actions/print$$1.class' \
	'org/gjt/sp/jedit/syntax/KeywordMap$$Keyword.class' \
	'org/gjt/sp/jedit/gui/CommandOutput$$1.class' \
	'org/gjt/sp/jedit/gui/CommandOutput$$StderrThread.class' \
	'org/gjt/sp/jedit/gui/CommandOutput$$StdoutThread.class' \
	'org/gjt/sp/jedit/gui/SyntaxTextArea$$BracketHighlighter.class' \
	'org/gjt/sp/jedit/gui/SyntaxTextArea$$CurrentLineHighlighter.class' \
	'org/gjt/sp/jedit/gui/SyntaxTextArea$$SyntaxCaret.class' \
	'org/gjt/sp/jedit/jEdit$$1.class' \
	'org/gjt/sp/jedit/jEdit$$Autosave.class' \
	'org/gjt/sp/jedit/jEdit$$JarClassLoader.class' \
	'org/gjt/sp/jedit/jEdit$$Server.class' \
	'org/gjt/sp/jedit/Buffer$$BufferProps.class' \
	'org/gjt/sp/jedit/Buffer$$ColorList.class' \
	org/gjt/sp/jedit/jedit.props \
	org/gjt/sp/jedit/jedit_gui.props \
	org/gjt/sp/jedit/jedit_keys.props \
	org/gjt/sp/jedit/jedit_logo.gif \
	doc/*.html \
	doc/api
all: $(resources)
	rm -f doc/api/*.html
	javadoc -notree -noindex -d doc/api org.gjt.sp.jedit \
		org.gjt.sp.jedit.syntax
	@jar cf jedit.jar $(resources) $(no_compile_res)
	(cd sample-jars/BlueTheme && make)
	(cd sample-jars/DarkTheme && make)
	(cd sample-jars/DefaultTheme && make)
	(cd sample-jars/Reverse && make)
	(cd sample-jars/Rot13 && make)
install:
	mkdir -p $(JEDIT_HOME)
	cp jedit.jar jedit jedit_moz_remote $(JEDIT_HOME)
	mkdir -p $(JEDIT_HOME)/jars
	mkdir -p $(JEDIT_BIN)
	ln -sf $(JEDIT_HOME)/jedit $(JEDIT_BIN)/jedit
	ln -sf $(JEDIT_HOME)/jedit_moz_remote $(JEDIT_BIN)/jedit_moz_remote
	(if test "$(KDE_DIR)" != ""; then \
	mkdir -p $(KDE_DIR)/share/applnk/Applications; \
	cp jEdit.kdelnk $(KDE_DIR)/share/applnk/Applications; fi)
clean:
	find . -name \*~ -exec rm {} \;
	find . -name .\*~ -exec rm {} \;
	find . -name \*.bak -exec rm {} \;
	find . -name \*.orig -exec rm {} \;
	find . -name \#\*\# -exec rm {} \;
	find . -name .\*.swp -exec rm {} \;
	find . -name \*.class -exec rm {} \;
	find . -name \*.u -exec rm {} \;
	rm -f Rules.make
	rm -f doc/api/*.html
realclean: clean
	find . -name \*.jar -exec rm {} \;
distclean: clean
	chmod +x Configure jedit jedit_moz_remote
	todos install.bat
	find . -name \*.txt -exec todos {} \;
bindist: distclean
	(cd ..; zip -qr9 jEdit-$(VERSION).zip jEdit-$(VERSION))
	(cd ..; tar cfz jEdit-$(VERSION).tgz jEdit-$(VERSION))
srcdist: distclean realclean
	(cd ..; zip -qr9 jEdit-$(VERSION)-src.zip jEdit-$(VERSION))
	(cd ..; tar cfz jEdit-$(VERSION)-src.tgz jEdit-$(VERSION))
zip: bindist
include Rules.make
