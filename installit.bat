set DIRECTORY="C:\Program Files\jEdit"
md %DIRECTORY%
md %DIRECTORY%\doc
copy src\jedit.jar %DIRECTORY%
cd plugins
call installit %DIRECTORY%
cd ..
copy bin\*.bat %DIRECTORY%
copy etc\*.pif %DIRECTORY%
copy doc\*.txt %DIRECTORY%\doc
copy doc\*.marks %DIRECTORY%\doc
copy README %DIRECTORY%\doc
copy COPYING %DIRECTORY%\doc
copy VERSION %DIRECTORY%\doc
