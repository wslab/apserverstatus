# ServerTrack solution

## Requirements

Requires maven and java 8. Only tested with Oracle Java 8 on Linux.

To prepare the host, you can do the following:

sudo apt-get install maven

sudo add-apt-repository ppa:webupd8team/java

sudo apt-get update

sudo apt-get install oracle-java8-installer

## Compile

From the project directory, run

mvn compile

## Run unit tests

From the project directory, run

mvn test

## Execute with a custom data file

From the project directory, run

mvn exec:java -Dexec.mainClass="ServerStatusMonitor" -Dexec.args="path/myfile.csv"

File format (CSV):
timestamp,serverName,cpuLoad,ramLoad

Example line:
-30,myserver1,1.5,0.5

Positive timestamps will be used verbatim, negative timestamps are
assumed to mean "this many seconds ago".

There is a sample file in the project directory called "input.csv".

