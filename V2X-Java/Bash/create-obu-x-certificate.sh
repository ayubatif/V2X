#!/bin/bash

cd "../Authentication"

openssl genrsa -out OBU-X-private-key0.pem 2048
openssl req -new -key OBU-X-private-key0.pem -out OBU-X0.csr -subj "/C=SE/ST=Stockholm/L=Stockholm/O=KTH Thesis/OU=V2X Thesis/CN=test/emailAddress=arieltan@kth.se"
openssl x509 -req -days 365 -in OBU-X0.csr -CA CA-certificate.crt -CAkey CA-private-key.pem -CAcreateserial -out OBU-X-certificate0.crt -sha256
openssl pkcs8 -nocrypt -topk8 -inform PEM -in OBU-X-private-key0.pem -outform DER -out OBU-X-private-key0.der

openssl genrsa -out OBU-X-private-key1.pem 2048
openssl req -new -key OBU-X-private-key1.pem -out OBU-X1.csr -subj "/C=SE/ST=Stockholm/L=Stockholm/O=KTH Thesis/OU=V2X Thesis/CN=test/emailAddress=arieltan@kth.se"
openssl x509 -req -days 365 -in OBU-X1.csr -CA CA-certificate.crt -CAkey CA-private-key.pem -CAcreateserial -out OBU-X-certificate1.crt -sha256
openssl pkcs8 -nocrypt -topk8 -inform PEM -in OBU-X-private-key1.pem -outform DER -out OBU-X-private-key1.der

openssl genrsa -out OBU-X-private-key2.pem 2048
openssl req -new -key OBU-X-private-key2.pem -out OBU-X2.csr -subj "/C=SE/ST=Stockholm/L=Stockholm/O=KTH Thesis/OU=V2X Thesis/CN=test/emailAddress=arieltan@kth.se"
openssl x509 -req -days 365 -in OBU-X2.csr -CA CA-certificate.crt -CAkey CA-private-key.pem -CAcreateserial -out OBU-X-certificate2.crt -sha256
openssl pkcs8 -nocrypt -topk8 -inform PEM -in OBU-X-private-key2.pem -outform DER -out OBU-X-private-key2.der

openssl genrsa -out OBU-X-private-key3.pem 2048
openssl req -new -key OBU-X-private-key3.pem -out OBU-X3.csr -subj "/C=SE/ST=Stockholm/L=Stockholm/O=KTH Thesis/OU=V2X Thesis/CN=test/emailAddress=arieltan@kth.se"
openssl x509 -req -days 365 -in OBU-X3.csr -CA CA-certificate.crt -CAkey CA-private-key.pem -CAcreateserial -out OBU-X-certificate3.crt -sha256
openssl pkcs8 -nocrypt -topk8 -inform PEM -in OBU-X-private-key3.pem -outform DER -out OBU-X-private-key3.der

openssl genrsa -out OBU-X-private-key4.pem 2048
openssl req -new -key OBU-X-private-key4.pem -out OBU-X4.csr -subj "/C=SE/ST=Stockholm/L=Stockholm/O=KTH Thesis/OU=V2X Thesis/CN=test/emailAddress=arieltan@kth.se"
openssl x509 -req -days 365 -in OBU-X4.csr -CA CA-certificate.crt -CAkey CA-private-key.pem -CAcreateserial -out OBU-X-certificate4.crt -sha256
openssl pkcs8 -nocrypt -topk8 -inform PEM -in OBU-X-private-key4.pem -outform DER -out OBU-X-private-key4.der

echo "orders received"
exit

exec bash