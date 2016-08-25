# ServerTrack solution

## Requirements

Requires maven and java 8. Only tested with Oracle Java 8 on Linux.

## Compile

mvn compile

## Run tests

mvn test

## Execute with a custom data file

mvn exec:java -Dexec.mainClass="ServerStatusMonitor" -Dexec.args="path/myfile.csv"

File format (CSV):
timestamp,serverName,cpuLoad,ramLoad

Example line:
-30,myserver1,1.5,0.5

Positive timestamps will be used verbatim, negative timestamps are
assumed to mean "this many seconds ago".


