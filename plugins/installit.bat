set JAR_DIR=%1\jars
md %JAR_DIR%
cd ..\Reverse
call installit.bat %JAR_DIR%
cd ..\Rot13
call installit.bat %JAR_DIR%
cd ..
