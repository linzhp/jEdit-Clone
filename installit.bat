set DIRECTORY="C:\Program Files\jEdit"
md %DIRECTORY%
md %DIRECTORY%\doc
copy jedit.jar %DIRECTORY%
md %DIRECTORY%\jars
copy jars\*.jar %DIRECTORY%\jars 
copy jedit.bat %DIRECTORY%
copy jopen.bat %DIRECTORY%
copy doc\*.txt %DIRECTORY%\doc
copy doc\*.marks %DIRECTORY%\doc
copy README %DIRECTORY%\doc
copy COPYING %DIRECTORY%\doc
copy VERSION %DIRECTORY%\doc
