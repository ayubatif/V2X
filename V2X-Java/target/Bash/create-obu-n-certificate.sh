#!/bin/bash

cd "../Authentication"

openssl genrsa -out OBU-N-private-key.pem 2048
openssl req -new -key OBU-N-private-key.pem -out OBU-N.csr -subj "/C=SE/ST=Stockholm/L=Stockholm/O=KTH Thesis/OU=V2X Thesis/CN=test/emailAddress=arieltan@kth.se"
openssl x509 -req -days 365 -in OBU-N.csr -CA CA-certificate.crt -CAkey CA-private-key.pem -CAcreateserial -out OBU-N-certificate.crt -sha256
openssl pkcs8 -nocrypt -topk8 -inform PEM -in OBU-N-private-key.pem -outform DER -out OBU-N-private-key.der

echo "orders received"

exec bash