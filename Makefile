# Makefile
docs=COPYING.txt \
	README.txt \
	NEWS.txt \
	VERSION.txt \
	.README.txt.marks \
	.NEWS.txt.marks \
	doc/index.txt \
	doc/starting.txt \
	doc/shortcuts.txt \
	doc/menus.txt \
	doc/props.txt \
	doc/plugins.txt \
	doc/regexp.txt \
	doc/security.txt \
	doc/.starting.txt.marks \
	doc/.menus.txt.marks \
	doc/.props.txt.marks \
	doc/.plugins.txt.marks \
	doc/.regexp.txt.marks \
	doc/.security.txt.marks
all:
	grep -- '^- \(.*\)$$' doc/menus.txt > doc/shortcuts.txt
	@(cd src; $(MAKE))
	@(cd jars/AutoIndent; $(MAKE))
	@(cd jars/JavaPrettyPrint; $(MAKE))
	@(cd jars/Netscape; $(MAKE))
	@(cd jars/Reverse; $(MAKE))
	@(cd jars/Rot13; $(MAKE))
install:
	$(MKDIRHIER) $(SLAVA_SHARE_DIR)
	cp jedit.jar $(SLAVA_SHARE_DIR)
	cp jedit.props $(SLAVA_SHARE_DIR)
	$(MKDIRHIER) $(SLAVA_JARS_DIR)
	cp jars/*.jar $(SLAVA_JARS_DIR)
	$(MKDIRHIER) $(SLAVA_BIN_DIR)
	cp bin/jedit bin/jopen $(SLAVA_BIN_DIR)
	$(MKDIRHIER) $(SLAVA_DOC_DIR)
	cp $(docs) $(SLAVA_DOC_DIR)
	(if test "$(KDE_DIR)" != ""; then \
	$(MKDIRHIER) $(KDE_DIR)/share/applnk/Applications; \
	cp bin/jEdit.kdelnk $(KDE_DIR)/share/applnk/Applications; fi)
clean:
	find . -name \*~ -exec rm {} \;
	find . -name .\*~ -exec rm {} \;
	find . -name \*.bak -exec rm {} \;
	find . -name \#\*\# -exec rm {} \;
	find . -name .\*.swp -exec rm {} \;
	find . -name \*.class -exec rm {} \;
	rm -f Rules.make bin/jedit bin/jopen
dist: clean
	chmod +x Configure
	find . -name \*.bat.in -exec todos {} \;
	find . -name \*.bat -exec todos {} \;
	find . -name \*.txt -exec todos {} \;
zip: dist
	(cd ..; zip -qr9 jEdit-$(VERSION).zip jEdit-$(VERSION))
	(cd ..; tar cfz jEdit-$(VERSION).tgz jEdit-$(VERSION))
include Rules.make
