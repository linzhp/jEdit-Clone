BUILDING THE JEDIT CVS SNAPSHOT:

Basically, you must move the doc/ directory to the top level (where
org/ resides). Then, you should:

$ javac org/gjt/sp/jedit/jEdit.java
$ javac org/gjt/sp/jedit/proto/jeditplugins/Handler.java
$ javac org/gjt/sp/jedit/proto/jeditresource/Handler.java
$ jar cf jedit.jar org doc

CVS snapshots might not work well, I suggest you download the latest
development version from <http://www.gjt.org/~sp/jedit.html> instead.

--
Slava Pestov <sp@gjt.org>
