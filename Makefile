# Makefile for jEdit 1.6.x
#
# This will only work on Unix. See Makefile.win for a Windows
# version.

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
	org/gjt/sp/jedit/CompilerError.class \
	org/gjt/sp/jedit/EditAction.class \
	org/gjt/sp/jedit/GUIUtilities.class \
	org/gjt/sp/jedit/JARClassLoader.class \
	org/gjt/sp/jedit/Marker.class \
	org/gjt/sp/jedit/MiscUtilities.class \
	org/gjt/sp/jedit/Mode.class \
	org/gjt/sp/jedit/OptionPane.class \
	org/gjt/sp/jedit/Plugin.class \
	org/gjt/sp/jedit/View.class \
	org/gjt/sp/jedit/actions/about.class \
	org/gjt/sp/jedit/actions/block_comment.class \
	org/gjt/sp/jedit/actions/box_comment.class \
	org/gjt/sp/jedit/actions/browser_open_sel.class \
	org/gjt/sp/jedit/actions/browser_open_url.class \
	org/gjt/sp/jedit/actions/buffer_options.class \
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
	org/gjt/sp/jedit/actions/exchange_anchor.class \
	org/gjt/sp/jedit/actions/exit.class \
	org/gjt/sp/jedit/actions/expand_abbrev.class \
	org/gjt/sp/jedit/actions/find.class \
	org/gjt/sp/jedit/actions/find_next.class \
	org/gjt/sp/jedit/actions/find_selection.class \
	org/gjt/sp/jedit/actions/format.class \
	org/gjt/sp/jedit/actions/global_options.class \
	org/gjt/sp/jedit/actions/goto_anchor.class \
	org/gjt/sp/jedit/actions/goto_end_indent.class \
	org/gjt/sp/jedit/actions/goto_line.class \
	org/gjt/sp/jedit/actions/goto_marker.class \
	org/gjt/sp/jedit/actions/help.class \
	org/gjt/sp/jedit/actions/hypersearch.class \
	org/gjt/sp/jedit/actions/indent_on_enter.class \
	org/gjt/sp/jedit/actions/indent_on_tab.class \
	org/gjt/sp/jedit/actions/insert_date.class \
	org/gjt/sp/jedit/actions/join_lines.class \
	org/gjt/sp/jedit/actions/locate_bracket.class \
	org/gjt/sp/jedit/actions/new_file.class \
	org/gjt/sp/jedit/actions/new_view.class \
	org/gjt/sp/jedit/actions/next_buffer.class \
	org/gjt/sp/jedit/actions/next_error.class \
	org/gjt/sp/jedit/actions/next_paragraph.class \
	org/gjt/sp/jedit/actions/open_file.class \
	org/gjt/sp/jedit/actions/open_path.class \
	org/gjt/sp/jedit/actions/open_selection.class \
	org/gjt/sp/jedit/actions/open_url.class \
	org/gjt/sp/jedit/actions/paste.class \
	org/gjt/sp/jedit/actions/paste_predefined.class \
	org/gjt/sp/jedit/actions/paste_previous.class \
	org/gjt/sp/jedit/actions/plugin_help.class \
	org/gjt/sp/jedit/actions/prev_buffer.class \
	org/gjt/sp/jedit/actions/prev_error.class \
	org/gjt/sp/jedit/actions/prev_paragraph.class \
	org/gjt/sp/jedit/actions/print.class \
	org/gjt/sp/jedit/actions/redo.class \
	org/gjt/sp/jedit/actions/reload.class \
	org/gjt/sp/jedit/actions/replace.class \
	org/gjt/sp/jedit/actions/replace_all.class \
	org/gjt/sp/jedit/actions/replace_in_selection.class \
	org/gjt/sp/jedit/actions/replace_next.class \
	org/gjt/sp/jedit/actions/save.class \
	org/gjt/sp/jedit/actions/save_all.class \
	org/gjt/sp/jedit/actions/save_as.class \
	org/gjt/sp/jedit/actions/save_url.class \
	org/gjt/sp/jedit/actions/scroll_line.class \
	org/gjt/sp/jedit/actions/select_all.class \
	org/gjt/sp/jedit/actions/select_anchor.class \
	org/gjt/sp/jedit/actions/select_block.class \
	org/gjt/sp/jedit/actions/select_buffer.class \
	org/gjt/sp/jedit/actions/select_line_range.class \
	org/gjt/sp/jedit/actions/select_next_paragraph.class \
	org/gjt/sp/jedit/actions/select_no_indent.class \
	org/gjt/sp/jedit/actions/select_prev_paragraph.class \
	org/gjt/sp/jedit/actions/send.class \
	org/gjt/sp/jedit/actions/set_anchor.class \
	org/gjt/sp/jedit/actions/set_marker.class \
	org/gjt/sp/jedit/actions/shift_left.class \
	org/gjt/sp/jedit/actions/shift_right.class \
	org/gjt/sp/jedit/actions/tab.class \
	org/gjt/sp/jedit/actions/toggle_console.class \
	org/gjt/sp/jedit/actions/to_lower.class \
	org/gjt/sp/jedit/actions/to_upper.class \
	org/gjt/sp/jedit/actions/undo.class \
	org/gjt/sp/jedit/actions/untab.class \
	org/gjt/sp/jedit/actions/wing_comment.class \
	org/gjt/sp/jedit/actions/word_count.class \
	org/gjt/sp/jedit/event/AbstractEditorEvent.class \
	org/gjt/sp/jedit/event/BufferAdapter.class \
	org/gjt/sp/jedit/event/BufferEvent.class \
	org/gjt/sp/jedit/event/BufferListener.class \
	org/gjt/sp/jedit/event/EditorAdapter.class \
	org/gjt/sp/jedit/event/EditorEvent.class \
	org/gjt/sp/jedit/event/EditorListener.class \
	org/gjt/sp/jedit/event/EventMulticaster.class \
	org/gjt/sp/jedit/event/ViewAdapter.class \
	org/gjt/sp/jedit/event/ViewEvent.class \
	org/gjt/sp/jedit/event/ViewListener.class \
	org/gjt/sp/jedit/gui/BufferOptions.class \
	org/gjt/sp/jedit/gui/ClippingEditor.class \
	org/gjt/sp/jedit/gui/Console.class \
	org/gjt/sp/jedit/gui/EnhancedMenuItem.class \
	org/gjt/sp/jedit/gui/GlobalOptions.class \
	org/gjt/sp/jedit/gui/HelpViewer.class \
	org/gjt/sp/jedit/gui/HistoryModel.class \
	org/gjt/sp/jedit/gui/HistoryTextField.class \
	org/gjt/sp/jedit/gui/HyperSearch.class \
	org/gjt/sp/jedit/gui/JEditTextArea.class \
	org/gjt/sp/jedit/gui/PastePredefined.class \
	org/gjt/sp/jedit/gui/PastePrevious.class \
	org/gjt/sp/jedit/gui/SearchAndReplace.class \
	org/gjt/sp/jedit/gui/SelectLineRange.class \
	org/gjt/sp/jedit/gui/SendDialog.class \
	org/gjt/sp/jedit/gui/SplashScreen.class \
	org/gjt/sp/jedit/mode/amstex.class \
	org/gjt/sp/jedit/mode/bat.class \
	org/gjt/sp/jedit/mode/c.class \
	org/gjt/sp/jedit/mode/cc.class \
	org/gjt/sp/jedit/mode/html.class \
	org/gjt/sp/jedit/mode/java_mode.class \
	org/gjt/sp/jedit/mode/javascript.class \
	org/gjt/sp/jedit/mode/latex.class \
	org/gjt/sp/jedit/mode/makefile.class \
	org/gjt/sp/jedit/mode/patch.class \
	org/gjt/sp/jedit/mode/props.class \
	org/gjt/sp/jedit/mode/sh.class \
	org/gjt/sp/jedit/mode/tex.class \
	org/gjt/sp/jedit/mode/text.class \
	org/gjt/sp/jedit/mode/tsql.class \
	org/gjt/sp/jedit/options/ColorTableOptionPane.class \
	org/gjt/sp/jedit/options/EditorOptionPane.class \
	org/gjt/sp/jedit/options/GeneralOptionPane.class \
	org/gjt/sp/jedit/options/KeyTableOptionPane.class \
	org/gjt/sp/jedit/proto/jeditplugins/Handler.class \
	org/gjt/sp/jedit/proto/jeditplugins/PluginListURLConnection.class \
	org/gjt/sp/jedit/proto/jeditresource/Handler.class \
	org/gjt/sp/jedit/proto/jeditresource/PluginResURLConnection.class \
	org/gjt/sp/jedit/syntax/BatchFileTokenMarker.class \
	org/gjt/sp/jedit/syntax/CCTokenMarker.class \
	org/gjt/sp/jedit/syntax/CTokenMarker.class \
	org/gjt/sp/jedit/syntax/DefaultSyntaxDocument.class \
	org/gjt/sp/jedit/syntax/HTMLTokenMarker.class \
	org/gjt/sp/jedit/syntax/JavaScriptTokenMarker.class \
	org/gjt/sp/jedit/syntax/JavaTokenMarker.class \
	org/gjt/sp/jedit/syntax/KeywordMap.class \
	org/gjt/sp/jedit/syntax/MakefileTokenMarker.class \
	org/gjt/sp/jedit/syntax/PatchTokenMarker.class \
	org/gjt/sp/jedit/syntax/PropsTokenMarker.class \
	org/gjt/sp/jedit/syntax/ShellScriptTokenMarker.class \
	org/gjt/sp/jedit/syntax/SQLTokenMarker.class \
	org/gjt/sp/jedit/syntax/SyntaxDocument.class \
	org/gjt/sp/jedit/syntax/SyntaxEditorKit.class \
	org/gjt/sp/jedit/syntax/SyntaxTextArea.class \
	org/gjt/sp/jedit/syntax/SyntaxUtilities.class \
	org/gjt/sp/jedit/syntax/SyntaxView.class \
	org/gjt/sp/jedit/syntax/TeXTokenMarker.class \
	org/gjt/sp/jedit/syntax/Token.class \
	org/gjt/sp/jedit/syntax/TokenMarker.class \
	org/gjt/sp/jedit/syntax/TSQLTokenMarker.class
no_compile_res=gnu/regexp/IntPair.class \
	gnu/regexp/CharUnit.class \
	'org/gjt/sp/jedit/actions/print$$PrintSyntaxView.class' \
	'org/gjt/sp/jedit/gui/BufferOptions$$ActionHandler.class' \
	'org/gjt/sp/jedit/gui/BufferOptions$$KeyHandler.class' \
	'org/gjt/sp/jedit/gui/Console$$StderrThread.class' \
	'org/gjt/sp/jedit/gui/Console$$StdoutThread.class' \
	'org/gjt/sp/jedit/gui/HelpViewer$$ActionHandler.class' \
	'org/gjt/sp/jedit/gui/HelpViewer$$KeyHandler.class' \
	'org/gjt/sp/jedit/gui/HelpViewer$$LinkHandler.class' \
	'org/gjt/sp/jedit/gui/HistoryTextField$$ActionHandler.class' \
	'org/gjt/sp/jedit/gui/HistoryTextField$$KeyHandler.class' \
	'org/gjt/sp/jedit/gui/HistoryTextField$$MouseHandler.class' \
	'org/gjt/sp/jedit/gui/HyperSearch$$ActionHandler.class' \
	'org/gjt/sp/jedit/gui/HyperSearch$$EditorHandler.class' \
	'org/gjt/sp/jedit/gui/HyperSearch$$KeyHandler.class' \
	'org/gjt/sp/jedit/gui/HyperSearch$$ListHandler.class' \
	'org/gjt/sp/jedit/gui/JEditTextArea$$MouseHandler.class' \
	'org/gjt/sp/jedit/gui/JEditTextArea$$MouseMotionHandler.class' \
	'org/gjt/sp/jedit/gui/JEditTextArea$$PropertyHandler.class' \
	'org/gjt/sp/jedit/gui/SearchAndReplace$$ActionHandler.class' \
	'org/gjt/sp/jedit/gui/SearchAndReplace$$KeyHandler.class' \
	'org/gjt/sp/jedit/options/ColorChoice.class' \
	'org/gjt/sp/jedit/options/ColorChoiceRenderer.class' \
	'org/gjt/sp/jedit/options/ColorChoiceRenderer$$UIResource.class' \
	'org/gjt/sp/jedit/options/ColorTableModel.class' \
	'org/gjt/sp/jedit/options/KeyTableModel.class' \
	'org/gjt/sp/jedit/options/KeyTableModel$$KeyBinding.class' \
	'org/gjt/sp/jedit/syntax/DefaultSyntaxDocument$$DocumentHandler.class' \
	'org/gjt/sp/jedit/syntax/KeywordMap$$Keyword.class' \
	'org/gjt/sp/jedit/syntax/TokenMarker$$LineInfo.class' \
	'org/gjt/sp/jedit/syntax/SyntaxTextArea$$BracketHighlighter.class' \
	'org/gjt/sp/jedit/syntax/SyntaxTextArea$$CurrentLineHighlighter.class' \
	'org/gjt/sp/jedit/syntax/SyntaxTextArea$$CaretHandler.class' \
	'org/gjt/sp/jedit/syntax/SyntaxTextArea$$DefaultKeyTypedAction.class' \
	'org/gjt/sp/jedit/syntax/SyntaxTextArea$$InsertKeyAction.class' \
	'org/gjt/sp/jedit/syntax/SyntaxTextArea$$SyntaxCaret.class' \
	'org/gjt/sp/jedit/syntax/SyntaxTextArea$$SyntaxSafeScroller.class' \
	'org/gjt/sp/jedit/jEdit$$Autosave.class' \
	'org/gjt/sp/jedit/jEdit$$EditorHandler.class' \
	'org/gjt/sp/jedit/jEdit$$Server.class' \
	'org/gjt/sp/jedit/jEdit$$ServerClientHandler.class' \
	'org/gjt/sp/jedit/Buffer$$BufferProps.class' \
	'org/gjt/sp/jedit/Buffer$$DocumentHandler.class' \
	'org/gjt/sp/jedit/Buffer$$EditorHandler.class' \
	'org/gjt/sp/jedit/Buffer$$UndoHandler.class' \
	'org/gjt/sp/jedit/View$$BufferHandler.class' \
	'org/gjt/sp/jedit/View$$EditorHandler.class' \
	'org/gjt/sp/jedit/View$$CaretHandler.class' \
	'org/gjt/sp/jedit/View$$KeyHandler.class' \
	'org/gjt/sp/jedit/View$$WindowHandler.class' \
	org/gjt/sp/jedit/jedit.props \
	org/gjt/sp/jedit/jedit_gui.props \
	org/gjt/sp/jedit/jedit_keys.props \
	org/gjt/sp/jedit/jedit_logo.gif \
	org/gjt/sp/jedit/jedit_tips.props \
	org/gjt/sp/jedit/toolbar/*.gif \
	doc/*.html \
	doc/api/*.html \
	doc/api/images/*.gif \
	doc/devel-guide/*.html \
	doc/users-guide/*.html

all: $(resources)
	-javadoc -J-mx24m -noindex -notree -d doc/api org.gjt.sp.jedit \
		org.gjt.sp.jedit.event \
		org.gjt.sp.jedit.syntax
	@jar cfm jedit.jar org/gjt/sp/jedit/jedit.manifest \
		$(resources) $(no_compile_res)
	(cd jars/GenerateText && make)
	(cd jars/Reverse && make)
	(cd jars/Rot13 && make)

install:
	mkdir -p $(JEDIT_HOME)
	cp jedit.jar jedit jedit_moz_remote $(JEDIT_HOME)
	mkdir -p $(JEDIT_HOME)/jars
	cp jars/*.jar $(JEDIT_HOME)/jars
	mkdir -p $(JEDIT_BIN)
	test "$(JEDIT_HOME)" != "$(JEDIT_BIN)" && \
		ln -sf $(JEDIT_HOME)/jedit $(JEDIT_BIN)/jedit
	test "$(JEDIT_HOME)" != "$(JEDIT_BIN)" && \
		ln -sf $(JEDIT_HOME)/jedit_moz_remote $(JEDIT_BIN)/jedit_moz_remote

clean:
	-find . -name \*~ -exec rm {} \;
	-find . -name .\*~ -exec rm {} \;
	-find . -name \*.bak -exec rm {} \;
	-find . -name \*.orig -exec rm {} \;
	-find . -name \*.rej -exec rm {} \;
	-find . -name \#\*\# -exec rm {} \;
	-find . -name .\*.swp -exec rm {} \;
	-find . -name \*.class -exec rm {} \;
	-find . -name .\#\* -exec rm {} \;
	-find . -name .new\* -exec rm {} \;
	-find . -name .directory -exec rm {} \;
	rm -f doc/api/*.html
	rm -f Rules.make

zip: clean
	chmod +x Configure jedit jedit_moz_remote
	todos Makefile.win install.bat README.txt
	(cd ..; zip -qr9 jEdit-$(VERSION).zip jEdit-$(VERSION))
	(cd ..; tar cfz jEdit-$(VERSION).tgz jEdit-$(VERSION))

Rules.make:
	@echo
	@echo "Before running 'make', you must run 'Configure' to specify"
	@echo "the installation directory, Java compiler, etc."
	@echo
	@exit 1
include Rules.make
