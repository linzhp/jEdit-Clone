@echo off
set DIRECTORY=C:\Program Files\jEdit
set CLASSPATH=%DIRECTORY%\jedit.jar;%CLASSPATH%
javaw %JEDIT_J% jEdit -server=C:\Windows\Temp\jedit-server -plugindir="%DIRECTORY%\jars" -helpdir="%DIRECTORY%\doc" %JEDIT% %1 %2 %3 %4 %5 %6 %7 %8 %9
