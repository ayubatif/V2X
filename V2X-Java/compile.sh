#!/bin/bash
# Mvn compile then copy auth and bash to target
#mvn clean compile assembly:single

whoami

mkdir obuASetup
cp target/V2X-Java-1.0-SNAPSHOT-jar-with-dependencies.jar obuASetup/
java -cp obuASetup/V2X-Java-1.0-SNAPSHOT-jar-with-dependencies.jar v2x.PseudonymAuthority a
for file in CADNS/*; do cp "$file" "${obuASetup/Authentication/}";done
for file in Log/*; do cp "$file" "${obuASetup/}";done

mkdir obuXSetup
cp target/V2X-Java-1.0-SNAPSHOT-jar-with-dependencies.jar obuXSetup/
java -cp obuXSetup/V2X-Java-1.0-SNAPSHOT-jar-with-dependencies.jar v2x.PseudonymAuthority x
for file in CADNS/*; do cp "$file" "${obuXSetup/Authentication/}";done
for file in Log/*; do cp "$file" "${obuXSetup/}";done

mkdir obuNSetup
cp target/V2X-Java-1.0-SNAPSHOT-jar-with-dependencies.jar obuNSetup/
java -cp obuNSetup/V2X-Java-1.0-SNAPSHOT-jar-with-dependencies.jar v2x.PseudonymAuthority n
for file in CADNS/*; do cp "$file" "${obuNSetup/Authentication/}";done
for file in Log/*; do cp "$file" "${obuNSetup/}";done

exit

exec bash