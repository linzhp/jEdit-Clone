# Makefile
# Version: 1.0

subdirs = src plugins doc bin
all: subdirs
install: subdirs-install
clean:
	find -name \*~ -exec rm {} \;
	find -name \*.bak -exec rm {} \;
	find -name \#\*\# -exec rm {} \;
	find -name .\*.swp -exec rm {} \;
	find -name \*.class -exec rm {} \;
realclean:
	find -name \*.jar -exec rm {} \;
zip: all clean todos
	(cd ..; zip -qr9 jEdit-`date +%Y%m%d`.zip jEdit)
tgz: all clean todos
	(cd ..; tar cfz jEdit-`date +%Y%m%d`.tgz jEdit)
todos:
	find -name \*.bat -exec todos {} \;
	find -name \*.java -exec todos {} \;
	find -name \*.txt -exec todos {} \;
	todos BUGS
	todos COPYING
	todos INSTALL
	todos README
	todos TODO
include Rules.make
