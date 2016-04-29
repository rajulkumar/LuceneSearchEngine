#Set the jdk bin path of the system at <...> 
export PATH=$PATH:<Path of jdk bin on the system>
export CLASSPATH=$CLASSPATH:./src/com/search/index:./src:.:./lib/lucene-analyzers-common-4.7.2.jar:./lib/lucene-core-4.7.2.jar:./lib/lucene-queryparser-4.7.2.jar

javac ./src/com/search/Retrieve.java

java com/search/Retrieve
