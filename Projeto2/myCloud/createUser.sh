#!/bin/bash

if [ ! -n "$1" ]
then
  echo "Usage: createUser user password"
  exit $E_BADARGS
fi  

keytool -noprompt -genkeypair -keysize 2048 -alias $1 -keyalg rsa -keystore "$1.keystore" -storetype PKCS12 -storepass "$2"\
 -dname "CN=$1, OU=FCUL, O=FCUL, L=Lisboa, S=Lisboa, C=PT"
keytool -export -keystore "$1.keystore" -alias "$1" -file "$1.cer" -storepass "$2"
