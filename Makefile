# Makefile
all:
	@make -C src
	@make -C plugins
install:
	@make -C src install
	@make -C plugins install
	@make -C doc install
	@make -C bin install
clean:
	find -name \*~ -exec rm {} \;
	find -name \*.bak -exec rm {} \;
	find -name \#\*\# -exec rm {} \;
	find -name .\*.swp -exec rm {} \;
realclean: clean
	find -name \*.class -exec rm {} \;
	find -name \*.jar -exec rm {} \;
zip: all clean todos
	(cd ..; zip -qr9 jEdit-`date +%Y%m%d`.zip jEdit)
tgz: all clean todos
	(cd ..; tar cfz jEdit-`date +%Y%m%d`.tgz jEdit)
todos:
	find -name \*.bat -exec todos {} \;
	find -name \*.java -exec todos {} \;
	find -name \*.txt -exec todos {} \;
	todos README
	todos COPYING
	todos VERSION
include Rules.make
