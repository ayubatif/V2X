#!/bin/bash
# Mvn compile then copy auth and bash to target
mvn clean compile assembly:single

mkdir obuA-setup
cp target/V2X-Java-1.0-SNAPSHOT-jar-with-dependencies.jar obuA-setup/
java -cp target/V2X-Java-1.0-SNAPSHOT-jar-with-dependencies.jar v2x.PseudonymAuthority a
for file in CADNS/*; do cp "$file" "${obuA-setup/Authentication/}";done
for file in Log/*; do cp "$file" "${obuA-setup/}";done

mkdir obuX-setup
cp target/V2X-Java-1.0-SNAPSHOT-jar-with-dependencies.jar obuX-setup/
java -cp target/V2X-Java-1.0-SNAPSHOT-jar-with-dependencies.jar v2x.PseudonymAuthority x
for file in CADNS/*; do cp "$file" "${obuX-setup/Authentication/}";done
for file in Log/*; do cp "$file" "${obuA-setup/}";done

mkdir obuN-setup
cp target/V2X-Java-1.0-SNAPSHOT-jar-with-dependencies.jar obuN-setup/
java -cp target/V2X-Java-1.0-SNAPSHOT-jar-with-dependencies.jar v2x.PseudonymAuthority n
for file in CADNS/*; do cp "$file" "${obuN-setup/Authentication/}";done
for file in Log/*; do cp "$file" "${obuA-setup/}";done

exit

exec bash