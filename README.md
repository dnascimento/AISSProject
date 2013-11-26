Security Project
===========

Security Email System using AES and Portuguese Citizen Card

##How it works?
This Java Applet application:
* zips your documents
* ciphers this documents using a AES Hardware implementation (using JNI)
* Signs the data with Portuguese Citizen SmartCard 

On receiver size:
* Signature validation against CA Certificate and name
* Decipher the documents
* Unzip the documents


Note: We know that zipping is not secure, it's a simplification.

##Authors:
Dário Nascimento and Gonçalo Carito
