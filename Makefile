# Makefile for jEdit 1.2.x
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
	org/gjt/sp/jedit/cmd/about.class \
	org/gjt/sp/jedit/cmd/clear.class \
	org/gjt/sp/jedit/cmd/clear_marker.class \
	org/gjt/sp/jedit/cmd/close_file.class \
	org/gjt/sp/jedit/cmd/close_view.class \
	org/gjt/sp/jedit/cmd/copy.class \
	org/gjt/sp/jedit/cmd/cut.class \
	org/gjt/sp/jedit/cmd/execute.class \
	org/gjt/sp/jedit/cmd/exit.class \
	org/gjt/sp/jedit/cmd/expand_abbrev.class \
	org/gjt/sp/jedit/cmd/find.class \
	org/gjt/sp/jedit/cmd/find_next.class \
	org/gjt/sp/jedit/cmd/find_selection.class \
	org/gjt/sp/jedit/cmd/format.class \
	org/gjt/sp/jedit/cmd/goto_line.class \
	org/gjt/sp/jedit/cmd/goto_marker.class \
	org/gjt/sp/jedit/cmd/help.class \
	org/gjt/sp/jedit/cmd/hypersearch.class \
	org/gjt/sp/jedit/cmd/insert_date.class \
	org/gjt/sp/jedit/cmd/locate_block.class \
	org/gjt/sp/jedit/cmd/locate_bracket.class \
	org/gjt/sp/jedit/cmd/locate_paragraph.class \
	org/gjt/sp/jedit/cmd/netscape_open_sel.class \
	org/gjt/sp/jedit/cmd/netscape_open_url.class \
	org/gjt/sp/jedit/cmd/new_file.class \
	org/gjt/sp/jedit/cmd/new_view.class \
	org/gjt/sp/jedit/cmd/open_file.class \
	org/gjt/sp/jedit/cmd/open_selection.class \
	org/gjt/sp/jedit/cmd/open_url.class \
	org/gjt/sp/jedit/cmd/options.class \
	org/gjt/sp/jedit/cmd/paste.class \
	org/gjt/sp/jedit/cmd/print.class \
	org/gjt/sp/jedit/cmd/redo.class \
	org/gjt/sp/jedit/cmd/replace.class \
	org/gjt/sp/jedit/cmd/replace_all.class \
	org/gjt/sp/jedit/cmd/replace_next.class \
	org/gjt/sp/jedit/cmd/save.class \
	org/gjt/sp/jedit/cmd/save_as.class \
	org/gjt/sp/jedit/cmd/save_url.class \
	org/gjt/sp/jedit/cmd/select_all.class \
	org/gjt/sp/jedit/cmd/select_buffer.class \
	org/gjt/sp/jedit/cmd/select_mode.class \
	org/gjt/sp/jedit/cmd/send.class \
	org/gjt/sp/jedit/cmd/set_marker.class \
	org/gjt/sp/jedit/cmd/to_lower.class \
	org/gjt/sp/jedit/cmd/to_upper.class \
	org/gjt/sp/jedit/cmd/undo.class \
	org/gjt/sp/jedit/cmd/word_count.class \
	org/gjt/sp/jedit/gui/HyperSearch.class \
	org/gjt/sp/jedit/gui/Options.class \
	org/gjt/sp/jedit/gui/SearchAndReplace.class \
	org/gjt/sp/jedit/gui/SendDialog.class \
	org/gjt/sp/jedit/mode/c.class \
	org/gjt/sp/jedit/mode/bat.class \
	org/gjt/sp/jedit/mode/html.class \
	org/gjt/sp/jedit/mode/java_mode.class \
	org/gjt/sp/jedit/mode/makefile.class \
	org/gjt/sp/jedit/mode/sh.class \
	org/gjt/sp/jedit/mode/tex.class \
	org/gjt/sp/jedit/syntax/BatchFileTokenMarker.class \
	org/gjt/sp/jedit/syntax/CTokenMarker.class \
	org/gjt/sp/jedit/syntax/HTMLTokenMarker.class \
	org/gjt/sp/jedit/syntax/MakefileTokenMarker.class \
	org/gjt/sp/jedit/syntax/ShellScriptTokenMarker.class \
	org/gjt/sp/jedit/syntax/SyntaxEditorKit.class \
	org/gjt/sp/jedit/syntax/SyntaxTextArea.class \
	org/gjt/sp/jedit/syntax/SyntaxView.class \
	org/gjt/sp/jedit/syntax/TeXTokenMarker.class \
	org/gjt/sp/jedit/syntax/Token.class \
	org/gjt/sp/jedit/syntax/TokenMarker.class \
	org/gjt/sp/jedit/mode/autoindent.class \
	org/gjt/sp/jedit/jEdit.class \
	org/gjt/sp/jedit/Autosave.class \
	org/gjt/sp/jedit/Buffer.class \
	org/gjt/sp/jedit/BufferMgr.class \
	org/gjt/sp/jedit/Command.class \
	org/gjt/sp/jedit/CommandMgr.class \
	org/gjt/sp/jedit/Marker.class \
	org/gjt/sp/jedit/Mode.class \
	org/gjt/sp/jedit/PropsMgr.class \
	org/gjt/sp/jedit/Server.class \
	org/gjt/sp/jedit/View.class
no_compile_res=gnu/regexp/IntPair.class \
	gnu/regexp/CharUnit.class \
	'org/gjt/sp/jedit/cmd/print$$1.class' \
	'org/gjt/sp/jedit/BufferMgr$$1.class' \
	'org/gjt/sp/jedit/View$$1.class' \
	jedit.props
all: $(resources)
	jar cf jedit.jar $(resources) $(no_compile_res)
	egrep -- '^- .*([CAMS]*-.)' doc/menus.txt > doc/shortcuts.txt
install:
	mkdir -p $(JEDIT_HOME)
	cp jedit.jar jedit $(JEDIT_HOME)
	mkdir -p $(JEDIT_HOME)/jars
	mkdir -p $(JEDIT_BIN)
	ln -sf $(JEDIT_HOME)/jedit $(JEDIT_BIN)/jedit
	mkdir -p $(JEDIT_HOME)/doc
	cp README.txt COPYING.txt .README.txt.marks doc/*.txt doc/.*.marks \
		$(JEDIT_HOME)/doc
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
realclean: clean
	find . -name \*.jar -exec rm {} \;
dist: clean
	chmod +x Configure
	find . -name \*.bat.in -exec todos {} \;
	find . -name \*.bat -exec todos {} \;
	find . -name \*.txt -exec todos {} \;
zip: dist
	(cd ..; zip -qr9 jEdit-$(VERSION).zip jEdit-$(VERSION))
	(cd ..; tar cfz jEdit-$(VERSION).tgz jEdit-$(VERSION))
include Rules.make
