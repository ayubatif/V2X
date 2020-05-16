#!/bin/bash
# Mvn compile then copy auth and bash to target
mvn clean compile assembly:single
cp -rf Authentication target/
cp -rf Bash target/

exit

exec bash