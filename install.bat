@echo off

rem Installation directory
set DIRECTORY=C:\Program Files\jEdit
rem Java VM: change this to `java' if you're using Java 2
set JAVA=jre

echo -- jEdit installation script

echo -
echo The installation directory is [%DIRECTORY%]
echo The Java virtual machine is [%JAVA%]
echo -
echo NOTE: If you're using Java 2/JDK 1.2, you MUST change the
echo Java virtual machine to `java'.
echo -
echo Edit the install.bat file to change these parameters.
echo -

choice Continue? 
if errorlevel 2 exit

echo %JAVA% -classpath "%DIRECTORY%\jedit.jar;%%CLASSPATH%%" -mx32m %%JEDIT%% org.gjt.sp.jedit.jEdit %%1 %%2 %%3 %%4 %%5 %%6 %%7 %%8 %%9 > jedit.bat

md "%DIRECTORY%"
copy jedit.jar "%DIRECTORY%"
copy jedit.bat "%DIRECTORY%"
md "%DIRECTORY%\jars"

echo -- Installation complete.
echo -- Run %DIRECTORY%\jedit.bat to start jEdit.
