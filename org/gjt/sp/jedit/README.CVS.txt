BUILDING THE JEDIT CVS SNAPSHOT:

Basically, you must move the doc/ directory to the top level (where
org/ resides). Then, you should:

$ make -f org/gjt/sp/jedit/Makefile
$ make -f doc/Makefile
$ jar cf jedit.jar `cat .resources`

CVS snapshots might not work well, I suggest you download the latest
development version from <http://www.gjt.org/~sp/jedit.html> instead.

--
Slava Pestov <sp@gjt.org>
