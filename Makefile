# Makefile for jEdit 2.0.x
#
# This is used to compile and install jEdit on Unix - on Windows, jEdit
# can be installed by running `install.bat', and if necessary it can be
# recompiled `manually' by running javac on all Java source files.

# Build jedit.jar
all:
	rm -f .resources
	$(MAKE) -f gnu/regexp/Makefile
	$(MAKE) -f org/gjt/sp/jedit/Makefile
	$(JAR) cfm0 jedit.jar org/gjt/sp/jedit/jedit.manifest `cat .resources`
#	Build plugins automatically. For example:
#	(cd jars/XXX && make)
	(cd jars/LatestVersion && make)

# Install jEdit
install:
	mkdir -p $(JEDIT_HOME)
	cp jedit.jar jedit $(JEDIT_HOME)
	mkdir -p $(JEDIT_HOME)/doc
	cp doc/jeditdocs.ps $(JEDIT_HOME)/doc
	mkdir -p $(JEDIT_HOME)/doc/api
	cp doc/api/*.html $(JEDIT_HOME)/doc/api
	mkdir -p $(JEDIT_HOME)/doc/api/images
	cp doc/api/images/*.gif $(JEDIT_HOME)/doc/api/images
	mkdir -p $(JEDIT_HOME)/doc/jeditdocs
	cp doc/jeditdocs/*.html $(JEDIT_HOME)/doc/jeditdocs
	mkdir -p $(JEDIT_HOME)/doc/images
	cp doc/images/*.gif $(JEDIT_HOME)/doc/images
	cp *.txt $(JEDIT_HOME)/doc
	rm -f $(JEDIT_DOC)
	ln -sf $(JEDIT_HOME)/doc $(JEDIT_DOC)
	mkdir -p $(JEDIT_HOME)/jars
	cp jars/*.jar $(JEDIT_HOME)/jars
	mkdir -p $(JEDIT_HOME)/org/gjt/sp/jedit/remote/impl
	cp org/gjt/sp/jedit/remote/*.class \
		$(JEDIT_HOME)/org/gjt/sp/jedit/remote
	cp org/gjt/sp/jedit/remote/impl/*_Stub.class \
		$(JEDIT_HOME)/org/gjt/sp/jedit/remote/impl
	mkdir -p $(JEDIT_BIN)
	test "$(JEDIT_HOME)" != "$(JEDIT_BIN)" && \
		ln -sf $(JEDIT_HOME)/jedit $(JEDIT_BIN)/jedit

# Remove garbage files
clean:
	-find . -name \*~ -exec rm {} \;
	-find . -name .\*~ -exec rm {} \;
	-find . -name \*.bak -exec rm {} \;
	-find . -name \*.orig -exec rm {} \;
	-find . -name \*.rej -exec rm {} \;
	-find . -name \#\*\# -exec rm {} \;
	-find . -name .\*.swp -exec rm {} \;
	-find . -name \*.class \! -name Remote*.class -exec rm {} \;
	-rm -f org/gjt/sp/jedit/remote/impl/*_Skel.class
	-rm -f org/gjt/sp/jedit/remote/impl/*Impl[$$]*.class
	-rm -f org/gjt/sp/jedit/remote/impl/*Impl.class
	-find . -name .\#\* -exec rm {} \;
	-find . -name .new\* -exec rm {} \;
	-find . -name .directory -exec rm {} \;
	-find doc -name \*.dvi -exec rm {} \;
	-find doc -name \*.eps -exec rm {} \;
	-rm -f Rules.make
	-rm -f .resources
	-rm -f doc/HTML.index
	-rm -f doc/index.sgml

# Remove generated javadocs and JARs as well
realclean: clean
	rm -f doc/api/*.html
	-find . -name \*.jar -exec rm {} \;
	-find . -name \*.class -exec rm {} \;
	-find doc -name \*.ps -exec rm {} \;
	-rm -rf doc/jeditdocs

# Create zip files for distribution
zip: clean
	chmod +x Configure jedit
	-todos install.bat *.txt
	(cd ..; zip -qr9 jEdit-$(VERSION).zip jEdit-$(VERSION))
	(cd ..; tar cfz jEdit-$(VERSION).tgz jEdit-$(VERSION))

# Delegates to doc/Makefile
htmldocs:
	(cd doc; make htmldocs)

dvidocs:
	(cd doc; make dvidocs)

psdocs:
	(cd doc; make psdocs)

api:
	(cd doc; make api)

# Complain that we need Rules.make
Rules.make:
	@echo
	@echo "Before running 'make', you must run 'Configure' to specify"
	@echo "the installation directory, Java compiler, etc."
	@echo
	@exit 1

include Rules.make
