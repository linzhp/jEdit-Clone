JEDIT README

* About jEdit

jEdit is a text editor written in 100% Pure Java. It is NOT a word
processor - it doesn't support styled text. Instead, it can be used for
editing plain text files, such as HTML, Java source, Perl scripts, LaTeX
documents, and so on. It has many commands useful for the editing of
such files.

jEdit is released under the GNU General Public License, basically, you
can share jEdit and modify it all you want, but you must give away your
modifications under the same terms. It can be viewed within jEdit by
selecting Help->Help Contents->Copying. Also, if you have a WWW browser
such as Netscape, you can view it outside of jEdit by opening
`copying.html' in the `doc' directory.

jEdit uses gnu.regexp by the Free Software Foundation. Only the parts of
gnu.regexp used by jEdit are included - the complete package can be
found at <http://www.cacas.org/java/gnu/regexp/>.

* jEdit on the Internet

The jEdit homepage contains the latest version of jEdit, along with
general information.

- jEdit homepage: <http://www.gjt.org/~sp/jedit.html>

There is a jEdit mailing list for announcements and general discussion.
If you have a feature suggestion, or you have written a plugin for
jEdit, or you just want to be informed when new versions are released,
you can subscribe to the mailing list.

- To subscribe/unsubscribe: Go to <http://www.gjt.org/~sp/jedit.html>
- To send a message to the list: Send mail to <jedit@listbot.com>

You may also contact the author of jEdit directly.

- Slava Pestov's e-mail: <sp@gjt.org>

* Installing jEdit

jEdit requires either JDK/JRE 1.1 with Swing 1.1beta3 or later or
JDK/JRE 1.2. Earlier JDK and Swing versions will not work.

Java runtimes for Solaris and Windows:
<http://java.sun.com/products/jdk/1.2>
Java runtime for Linux: <http://java.blackdown.org>
Java runtime for the MacOS: <http://java.apple.com>
Swing: <http://java.sun.com/products/jfc/index.html>

** Unix

Installation of jEdit on Unix is a two step process:
1. Run `sh Configure'. It will prompt for the installation directory and
   various other build parameters.
2. Run `make install' to install jEdit.

You also need to set the PATH variable to point to the directory with
the jedit script so that the shell can locate it. The default location
is /opt/slava/bin. If you're using a Bourne shell (sh, ash, jsh, bash,
zsh) the command to add /opt/slava/bin to the PATH is:

	set PATH=$PATH:/opt/slava/bin; export PATH

If you're using a C shell (csh, tcsh) the equivalent is:

	setenv PATH $PATH:/opt/slava/bin

It is advisable to place these commands in the .profile script (Bourne
shell) or the .csh.login script (C shell) so that the PATH will be set
every time you log in.

To pass options to the Java virtual machine, put them in the JEDIT
enviroment variable. With a Bourne shell, the command is:

	set JEDIT=<options>; export JEDIT

If you're using a C shell the equivalent is:

	setenv JEDIT <options>

** Windows 95 and 98

Installation of jEdit on Windows 95 and 98 is a two step process:

1. If you want to install jEdit elsewhere than `C:\Program Files\jEdit',
or your Java virtual machine is not named `java.exe', edit the install.bat
file by right-clicking on it and selecting `Edit' from the context menu.
2. Run `install.bat' to install jEdit.

It is also a good idea to delete any previous versions of jEdit prior
to installing, as old files can cause problems.

If you want to use jEdit from the command line, you must add its
directory to the PATH. By default, jEdit is installed in C:\Program
Files\jEdit. The command to add that directory to the PATH is:

	set PATH=%PATH%;C:\Program Files\jEdit

It is advisable to place this command in your C:\AUTOEXEC.BAT so that
it's executed every time the computer starts up.

To pass options to the Java virtual machine, put them in the JEDIT
enviroment variable, like this:

	set JEDIT=<options>

** Windows NT and OS/2

On Windows NT or OS/2, the installation process is the same, except
that install.cmd should be run instead of install.bat.

** MacOS

There are no specific steps that must be taken to install jEdit on the
MacOS. Simply run the jEdit class from the jedit.jar file with your
favourite JVM. If you use MRJ, try creating a JBindery file to make
jEdit easier to start. Sorry, I can't give any more advice, my only Mac
is an ancient 68040 LC575.

* Documentation

Once you have installed jEdit, a list of help topics can be displayed by
selecting `Help->Help Contents'.

Have fun!
<sp@gjt.org>
