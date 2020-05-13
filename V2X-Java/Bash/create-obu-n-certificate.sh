#!/bin/bash

cd "../Authentication"

openssl genrsa -out OBU-N-private-key0.pem 2048
openssl req -new -key OBU-N-private-key0.pem -out OBU-N0.csr -subj "/C=SE/ST=Stockholm/L=Stockholm/O=KTH Thesis/OU=V2X Thesis/CN=test/emailAddress=arieltan@kth.se"
openssl x509 -req -days 365 -in OBU-N0.csr -CA CA-certificate.crt -CAkey CA-private-key.pem -CAcreateserial -out OBU-N-certificate0.crt -sha256
openssl pkcs8 -nocrypt -topk8 -inform PEM -in OBU-N-private-key0.pem -outform DER -out OBU-N-private-key0.der

openssl genrsa -out OBU-N-private-key1.pem 2048
openssl req -new -key OBU-N-private-key1.pem -out OBU-N1.csr -subj "/C=SE/ST=Stockholm/L=Stockholm/O=KTH Thesis/OU=V2X Thesis/CN=test/emailAddress=arieltan@kth.se"
openssl x509 -req -days 365 -in OBU-N1.csr -CA CA-certificate.crt -CAkey CA-private-key.pem -CAcreateserial -out OBU-N-certificate1.crt -sha256
openssl pkcs8 -nocrypt -topk8 -inform PEM -in OBU-N-private-key1.pem -outform DER -out OBU-N-private-key1.der

openssl genrsa -out OBU-N-private-key2.pem 2048
openssl req -new -key OBU-N-private-key2.pem -out OBU-N2.csr -subj "/C=SE/ST=Stockholm/L=Stockholm/O=KTH Thesis/OU=V2X Thesis/CN=test/emailAddress=arieltan@kth.se"
openssl x509 -req -days 365 -in OBU-N2.csr -CA CA-certificate.crt -CAkey CA-private-key.pem -CAcreateserial -out OBU-N-certificate2.crt -sha256
openssl pkcs8 -nocrypt -topk8 -inform PEM -in OBU-N-private-key2.pem -outform DER -out OBU-N-private-key2.der

openssl genrsa -out OBU-N-private-key3.pem 2048
openssl req -new -key OBU-N-private-key3.pem -out OBU-N3.csr -subj "/C=SE/ST=Stockholm/L=Stockholm/O=KTH Thesis/OU=V2X Thesis/CN=test/emailAddress=arieltan@kth.se"
openssl x509 -req -days 365 -in OBU-N3.csr -CA CA-certificate.crt -CAkey CA-private-key.pem -CAcreateserial -out OBU-N-certificate3.crt -sha256
openssl pkcs8 -nocrypt -topk8 -inform PEM -in OBU-N-private-key3.pem -outform DER -out OBU-N-private-key3.der

openssl genrsa -out OBU-N-private-key4.pem 2048
openssl req -new -key OBU-N-private-key4.pem -out OBU-N4.csr -subj "/C=SE/ST=Stockholm/L=Stockholm/O=KTH Thesis/OU=V2X Thesis/CN=test/emailAddress=arieltan@kth.se"
openssl x509 -req -days 365 -in OBU-N4.csr -CA CA-certificate.crt -CAkey CA-private-key.pem -CAcreateserial -out OBU-N-certificate4.crt -sha256
openssl pkcs8 -nocrypt -topk8 -inform PEM -in OBU-N-private-key4.pem -outform DER -out OBU-N-private-key4.der

echo "orders received"
exit

exec bash