JEDIT README

* About jEdit

jEdit is an Open Source, cross platform text editor written in Java. It
is NOT a word processor - it doesn't support styled text. Instead, it
can be used for editing plain text files, such as HTML, Java source,
Perl scripts, LaTeX documents, and so on. It has many commands useful
for the editing of such files.

jEdit uses gnu.regexp by the Free Software Foundation. Only the parts of
gnu.regexp used by jEdit are included - the complete package can be
found at <http://www.cacas.org/java/gnu/regexp/>.

* jEdit's License

Most of jEdit is released under the GNU General Public License, except
for the following packages which are released under the GNU Lesser
General Public License, which allows those packages to be linked into
commercial programs:

- org.gjt.sp.jedit.rmi (RMI interfaces)
- org.gjt.sp.jedit.syntax (Syntax highlighting core and parsers)
- org.gjt.sp.jedit.textarea (Syntax highlighting text area component)

Note that jEdit comes with ABSOLUTELY NO WARRANTY OF ANY KIND; see
section 11 and 12 of the GNU General Public License for details.

The GNU General and Lesser Publice Licenses are stored in the
COPYING.txt and COPYING-LIB.txt files, respectively.

* jEdit on the Internet

The jEdit homepage contains the latest version of jEdit, along with
general information.

- jEdit homepage: <http://www.gjt.org/~sp/jedit.html>

There is a jEdit mailing list for announcements and general discussion.
If you have a feature suggestion, or you have written a plugin for
jEdit, or you just want to be informed when new versions are released,
you can subscribe to the mailing list.

- To subscribe: Send mail to <jedit-subscribe@listbot.com>
- To unsubscribe: Send mail to <jedit-unsubscribe@listbot.com>
- To send a message to the list: Send mail to <jedit@listbot.com>
- To view the archive: Go to <http://jedit.listbot.com>

You may also contact the author of jEdit directly.

- Slava Pestov's e-mail: <sp@gjt.org>

* Installing jEdit

jEdit requires either JDK/JRE 1.1 with Swing 1.1 or later or JDK/JRE
1.2. Earlier JDK and Swing versions will not work.

Java runtimes for Solaris and Windows:
JDK 1.1: <http://java.sun.com/products/jdk/1.1>
JDK 1.2: <http://java.sun.com/products/jdk/1.2>

Java runtime for Linux: <http://java.blackdown.org>
Java runtime for the MacOS: <http://java.apple.com>

Swing: <http://java.sun.com/products/jfc>

** Unix

Installation of jEdit on Unix is a two step process:

1. Run `sh Configure'. It will prompt for the installation directory and
   various other build parameters.
2. Run `make install' to install jEdit.

To pass options to the Java virtual machine, put them in the JEDIT
environment variable. With a Bourne shell, the command is:

	set JEDIT=<options>; export JEDIT

If you're using a C shell the equivalent is:

	setenv JEDIT <options>

** Windows 95, 98 and NT

Installation of jEdit on Windows 9x and NT is a two step process:

1. If you want to install jEdit elsewhere than `C:\Program Files\jEdit',
or your Java virtual machine is not named `java.exe', edit the install.bat
file by right-clicking on it and selecting `Edit' from the context menu.
2. Run `install.bat' to install jEdit.

It is also a good idea to delete any previous versions of jEdit prior
to installing, as old files can cause problems.

If you get a `java.exe: not found' error when starting jEdit, you must
enter the full path to your `java.exe' in install.bat and reinstall.

If you want to use jEdit from the command line, you must add its
directory to the PATH. By default, jEdit is installed in C:\Program
Files\jEdit. The command to add that directory to the PATH is:

	set PATH=%PATH%;C:\Program Files\jEdit

It is advisable to place this command in your C:\AUTOEXEC.BAT so that
it's executed every time the computer starts up.

To pass options to the Java virtual machine, put them in the JEDIT
environment variable, like this:

	set JEDIT=<options>

** OS/2

On OS/2, no installation is necessary - place the jEdit distribution
directory in the desired place and run jedit.cmd to start jEdit.

** MacOS

There are no specific steps that must be taken to install jEdit on the
MacOS. Simply run the `org.gjt.sp.jedit.jEdit' class from the jedit.jar
file with your favorite JVM. If you use MRJ, try creating a JBindery
file to make jEdit easier to start. Sorry, I can't give any more advice,
my only Mac is an ancient 68040 LC575.

* Documentation

jEdit's documentation is written in SGML-DocBook. HTML and PostScript
versions are installed by default, and other formats such as text only,
RTF and DVI can be generated from the SGML source.

To view the HTML version, select `Help->Help Contents' in jEdit, or open
doc/jeditdocs/index.html in a WWW browser. To view the PostScript, open
doc/jeditdocs.ps in a PostScript viewer such as GV.

Have fun!
<sp@gjt.org>
