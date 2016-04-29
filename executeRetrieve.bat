:set jdk bin path of the system here
set path=%PATH%;C:\Program Files\Java\jdk1.8.0_05\bin
set CLASSPATH=%CLASSPATH%;./src/com/search/index;./src;./lib/lucene-analyzers-common-4.7.2.jar;./lib/lucene-core-4.7.2.jar;./lib/lucene-queryparser-4.7.2.jar

javac ./src/com/search/Retrieve.java

java com/search/Retrieve
