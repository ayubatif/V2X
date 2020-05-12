#!/bin/bash

cd "../Authentication"

openssl genrsa -out OBU-X-private-key.pem 2048
openssl req -new -key OBU-X-private-key.pem -out OBU-X.csr -subj "/C=SE/ST=Stockholm/L=Stockholm/O=KTH Thesis/OU=V2X Thesis/CN=test/emailAddress=arieltan@kth.se"
openssl x509 -req -days 365 -in OBU-X.csr -CA CA-certificate.crt -CAkey CA-private-key.pem -CAcreateserial -out OBU-X-certificate.crt -sha256
openssl pkcs8 -nocrypt -topk8 -inform PEM -in OBU-X-private-key.pem -outform DER -out OBU-X-private-key.der

echo "orders received"

exec bash