@echo off
cd src
javac *.java
jar cf jedit.jar *.class properties
cd ..
cd plugins
call .\makeit
cd ..
@echo on
