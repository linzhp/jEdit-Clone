BUILDING THE JEDIT CVS SNAPSHOT:

Basically, you must run `make' and create the JAR file:

$ rm -f .resources
$ make -f org/gjt/sp/jedit/Makefile
$ jar cf jedit.jar `cat .resources`

CVS snapshots might not work well, I suggest you download the latest
development version from <http://www.gjt.org/~sp/jedit.html> instead.
