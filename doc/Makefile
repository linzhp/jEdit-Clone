#
# Makefile for doc directory
#
# It adds all HTML and GIF files to the JAR
#

others=doc/*.html \
	doc/api/*.html \
	doc/api/images/*.gif \
	doc/devel-guide/*.html \
	doc/users-guide/*.html

all:
	-javadoc -J-mx24m -noindex -notree -d doc/api org.gjt.sp.jedit \
		org.gjt.sp.jedit.event \
		org.gjt.sp.jedit.remote \
		org.gjt.sp.jedit.search \
		org.gjt.sp.jedit.syntax
	echo $(others) >> .resources

include Rules.make
