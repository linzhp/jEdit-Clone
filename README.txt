JEDIT README (README.txt, last modified 13 Nov 1998)

Contents
--------
1. About jEdit
2. Installing jEdit
2.1 Unix
2.2 Windows 95, 98 and NT
2.3 MacOS
3. Getting Started

1. About jEdit
--------------
jEdit is a text editor fully written in Java.

I wasn't satisfied with the quality and feature sets of other editors written
in Java, so I decided to show how it should be done.

jEdit is released under the GNU General Public License, read the enclosed
COPYING.txt file for more information. Basically, you can share jEdit and
modify it all you want, but you must give away your modifications under the
same terms.

jEdit uses the following class libraries:
- jStyle, by Tal Davidson <tald@md2.huji.ac.il>. It is released under the
  Artistic License, which can be found in jars/JavaPrettyPrint/license.html.
  Only the parts of jStyle used by jEdit are included - the complete package
  can be found at <http://www.bigfoot.com/~davidsont/jstyle/>.
- gnu.regexp, by the Free Software Foundation. It is released under the
  GNU Library General Public License. Only the source code is included - the
  complete package, including documentation and license can be found at
  <http://www.cacas.org/java/gnu/regexp/>.

I would like to thank the authors of those libraries for their work. Without
thir efforts, jEdit would suck :).

2. Installing jEdit
-------------------
jEdit requires a Java 1.1 compatible runtime and Swing version 1.0.x.
Swing 1.1beta2 might work. 1.1beta3 will NOT work. When Swing 1.1 is out of
beta, I will port jEdit to it, but for now, stick to 1.0.x.

Java runtimes for Solaris and Windows: <http://java.sun.com/products/jdk/1.1>
Java runtime for Linux: <http://java.blackdown.org>
Java runtime for the MacOS: <http://java.apple.com>
Swing: <http://java.sun.com/products/jfc/index.html>

2.1. Unix
---------
Installation of jEdit on Unix is a two step process:
1. Run `sh Configure'. It will prompt for the installation directory and
   various other build parameters.
2. Run `make install' to install jEdit.

2.2. Windows 95, 98 and NT
--------------------------
Installation of jEdit on Windows 95, 98 and NT is a two step process:
1. Edit `config.bat' to change the default install directory and Java
   virtual machine.
2. Run `config.bat' to install jEdit.

2.3. MacOS
----------
There are no specific steps that must be taken to install jEdit on the MacOS.
Simply run the jEdit class from the jedit.jar file with your favourite JVM.
If you use MRJ, try creating a JBindery file to make jEdit easier to start.
Sorry, I can't give any more advice, my only Mac is an ancient 68040 LC575.

3. Getting Started
------------------
To get started with jEdit, I suggest you take a look at doc/starting.txt.

Have fun!

-- Slava Pestov
<slava_pestov@geocities.com>
