@echo off
set DIRECTORY="C:\Program Files\jEdit"
md %DIRECTORY%
md %DIRECTORY%\doc
copy src\jedit.jar %DIRECTORY%
cd plugins
call .\installit
cd ..
copy bin\jedit.bat %DIRECTORY%
copy bin\jopen.bat %DIRECTORY%
copy doc\*.txt %DIRECTORY%\doc
copy README %DIRECTORY%\doc
copy COPYING %DIRECTORY%\doc
copy VERSION %DIRECTORY%\doc
