# Makefile
all:
	@(cd src; $(MAKE))
	@(cd jars; $(MAKE))
install:
	@(cd src; $(MAKE) install)
	@(cd jars; $(MAKE) install)
	@(cd doc; $(MAKE) install)
	@(cd bin; $(MAKE) install)
	@echo
	@echo "Type '$(MAKE) kde' to install KDE applnks for jEdit and jOpen."
	@echo
kde:
	@(echo "Where is KDE located? [/opt/kde]";\
	read kdedir;\
	if [ "$$kdedir" = "" ];\
	then\
	kdedir=/opt/kde;\
	fi;\
	cp bin/*.kdelnk $$kdedir/share/applnk/Applications;)
clean:
	find . -name \*~ -exec rm {} \;
	find . -name .\*~ -exec rm {} \;
	find . -name \*.bak -exec rm {} \;
	find . -name \#\*\# -exec rm {} \;
	find . -name .\*.swp -exec rm {} \;
	find . -name \*.class -exec rm {} \;
realclean: clean
	find . -name \*.jar -exec rm {} \;
todos:
	find . -name \*.bat -exec todos {} \;
	find . -name \*.txt -exec todos {} \;
	todos VERSION README COPYING
manifest:
	find . -type f \! -name MANIFEST -exec md5sum {} \; > MANIFEST
check:
	find . -type f \! -name MANIFEST -exec md5sum {} \; | diff - MANIFEST
zip: clean todos manifest
	(cd ..; zip -qr9 jEdit-1.0.1.zip jEdit-1.0.1)
	(cd ..; tar cfz jEdit-1.0.1.tgz jEdit-1.0.1)
include Rules.make
