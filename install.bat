@echo off

rem Crude install script for jEdit on Windows 95/98/NT

rem To change the value of a set command, edit the text after the `='.
rem Don't change the enviroment variable name (DIRECTORY, JAVA).

rem The installation directory.
set DIRECTORY=C:\Program Files\jEdit

rem The Java virtual machine. The `.exe' suffix is not required.
set JAVA=java

rem Don't change anything after this point unless you know what you
rem are doing.

echo -- jEdit installation script

echo -
echo The installation directory is [%DIRECTORY%]
echo The Java virtual machine is [%JAVA%]
echo -
echo To change these parameters, right-click on `install.bat' and
echo select `Edit' in the context menu.
echo -

rem Create the batch file for starting jEdit.
rem Because of DOS's poor scripting abilities, we can't write a
rem generic script that determines the install directory, so if
rem the user moves jEdit is a different directory, boomfuck!!!
echo %JAVA% "-Djava.rmi.server.codebase=file://%DIRECTORY%/" -classpath "%DIRECTORY%\jedit.jar;%%CLASSPATH%%" %%JEDIT%% org.gjt.sp.jedit.jEdit %%1 %%2 %%3 %%4 %%5 %%6 %%7 %%8 %%9 > jedit.bat

rem Copy the required files (jedit.jar, jedit.bat)
mkdir "%DIRECTORY%"
copy jedit.jar "%DIRECTORY%"
copy jedit.bat "%DIRECTORY%"

rem Copy the docs
mkdir "%DIRECTORY%\doc"
mkdir "%DIRECTORY%\doc\api"
copy doc\api\*.html "%DIRECTORY%\doc\api"
mkdir "%DIRECTORY%\doc\api\images"
copy doc\api\images\*.gif "%DIRECTORY%\doc\api\images"
copy *.txt "%DIRECTORY%\doc"
copy doc\jeditdocs.ps "%DIRECTORY%\doc"
mkdir "%DIRECTORY%\doc\jeditdocs"
copy doc\jeditdocs\*.html "%DIRECTORY%\doc\jeditdocs"
mkdir "%DIRECTORY%\doc\images"
copy doc\images\*.gif "%DIRECTORY%\doc\images"

rem Create directory where user can install plugins
mkdir "%DIRECTORY%\jars"
copy jars\*.jar "%DIRECTORY%\jars"

rem Copy RMI stubs
mkdir "%DIRECTORY%\org\gjt\sp\jedit\remote\impl"
copy org\gjt\sp\jedit\remote\*.class "%DIRECTORY%\org\gjt\sp\jedit\remote"
copy org\gjt\sp\jedit\remote\impl\*_Stub.class "%DIRECTORY%\org\gjt\sp\jedit\remote\impl"

echo -- Installation complete.
echo -- Run %DIRECTORY%\jedit.bat to start jEdit.
