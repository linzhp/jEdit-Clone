set JAR_DIR=%1\jars
md %JAR_DIR%
cd HelloWorld
call installit.bat %JAR_DIR%
cd ..\InsertDate
call installit.bat %JAR_DIR%
cd ..\Reverse
call installit.bat %JAR_DIR%
cd ..\Rot13
call installit.bat %JAR_DIR%
cd ..\Send
call installit.bat %JAR_DIR%
cd ..\ToLower
call installit.bat %JAR_DIR%
cd ..\ToUpper
call installit.bat %JAR_DIR%
cd ..\WordCount
call installit.bat %JAR_DIR%
cd ..
