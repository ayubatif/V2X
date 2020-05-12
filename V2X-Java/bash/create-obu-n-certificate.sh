#!/bin/bash

cd "../Authentication"

openssl genrsa -out test-private-key.pem 2048
openssl req -new -key test-private-key.pem -out test-csr.csr -subj "/C=SE/ST=Stockholm/L=Stockholm/O=KTH Thesis/OU=V2X Thesis/CN=test/emailAddress=arieltan@kth.se"
openssl x509 -req -days 365 -in test-csr.csr -CA CA-certificate.crt -CAkey CA-private-key.pem -CAcreateserial -out test-certificate-n.crt -sha256
openssl pkcs8 -nocrypt -topk8 -inform PEM -in test-private-key.pem -outform DER -out test-private-key.der

echo "orders received"

exec bash