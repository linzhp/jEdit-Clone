@echo off
set VERSION=1.2final

rem Change this if necessary
set DIRECTORY=C:\Program Files\jEdit
set JAVA=jre

echo -- Configuring jEdit %VERSION%

echo The installation directory is [%DIRECTORY%]
echo The java virtual machine is [%JAVA%]
echo Edit the install.bat file to change these parameters.
choice Continue? 
if errorlevel 2 exit

echo Creating batch files...
echo set DIRECTORY=%DIRECTORY%>> wrapper.bat
echo set JAVA=%JAVA%>> wrapper.bat
type wrapper.bat > jedit.bat
type jedit.bat.in >> jedit.bat
del wrapper.bat

echo Installing...
md "%DIRECTORY%"
copy jedit.jar "%DIRECTORY%"
copy jedit.props "%DIRECTORY%"
copy jedit.bat "%DIRECTORY%"
md "%DIRECTORY%\jars"
md "%DIRECTORY%\doc"
copy doc\*.* "%DIRECTORY%\doc"
copy README.txt "%DIRECTORY%\doc"
copy COPYING.txt "%DIRECTORY%\doc"

echo -- Installation complete.
echo -- Run %DIRECTORY%\jedit.bat to start jEdit.
