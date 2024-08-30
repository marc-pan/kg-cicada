#!/bin/env bash

# Step 1. scale down the deploy of ibm-common-service-operator from operator namespace
oc scale deployment ibm-common-service-operator -n wkc-operator --replicas=0

# Step 2. delete relevant self-signed CA issuer, certificate, and secret from instance namespace
oc delete issuer cs-ss-issuer -n wkc
oc delete issuer  oc delete certificate cs-ca-certificate -n wkc
oc delete secret cs-ca-certificate-secret -n wkc

# Step 3. Create new CA issuer, certificate, and replace
oc apply -f my_cert.yaml

# Step 4. Checking if the secret `internal-tls` is created automatically by ibm cert manager controller
oc get secret/internal-tls
