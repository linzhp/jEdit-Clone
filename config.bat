@echo off
set VERSION=1.1.2

rem Change this if necessary
set DIRECTORY=C:\Program Files\jEdit
set JAVA=jre

echo -- Configuring jEdit %VERSION%

echo The installation directory is [%DIRECTORY%]
echo The java virtual machine is [%JAVA%]
echo Edit the config.bat file to change these parameters.
choice Continue? 
if errorlevel 2 exit

echo Creating batch files...
echo @echo off > wrapper.bat
echo set DIRECTORY=%DIRECTORY%>> wrapper.bat
echo set JAVA=%JAVA%>> wrapper.bat
type wrapper.bat > jedit.bat
type jedit.bat.in >> jedit.bat
type wrapper.bat > jopen.bat
type jopen.bat.in >> jopen.bat
del wrapper.bat

echo Installing...
md "%DIRECTORY%"
copy jedit.jar "%DIRECTORY%"
copy jedit.props "%DIRECTORY%"
copy jedit.bat "%DIRECTORY%"
copy jopen.bat "%DIRECTORY%"
md "%DIRECTORY%\jars"
copy jars\*.jar "%DIRECTORY%\jars"
md "%DIRECTORY%\doc"
copy doc\*.* "%DIRECTORY%\doc"
copy README.txt "%DIRECTORY%\doc"
copy VERSION.txt "%DIRECTORY%\doc"

echo -- Installation complete.
echo -- Run %DIRECTORY%\jedit.bat to open jEdit,
echo -- and %DIRECTORY%\jopen.bat to open files in a running jEdit.
