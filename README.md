
# Purpose

This repo is intended for Knowledge Graph and ERGO demo

## Git flow plugin for version update
1. Add git-flow plugin in pom.xml, detail refer to [The Git-Flow Maven Plugin](https://github.com/aleksandr-m/gitflow-maven-plugin)
2. Issue the command as below to bump new version
```bash
# mvn -B gitflow:release -DversionProperty=revision -DskipUpdateVersion=true -DskipTag=true -DargLine='-DprocessAllModules' -DversionDigitToIncrement=2 -DnoBackMerge=true
```

**Caution**
- keepBranch if this parameter is true it will keep local release branch
- skipReleaseMergeProdBranch if this parameter is true here it will skip the merge from release branch to production branch
- noBackMerge if this parameter is true the merge from production branch to development branch

## Add a proxy server
Go to [README.md](httpd/README.md) for detail.

## How to run DPX api service from VSCode
- Install gradle, openn liberty extensions
- Confirm the location of the keyStore's id `JRECACERTSStore` in `src/resources/local_testing/server.xml` file
- Make sure if the JDK also has the keyStore
- Launch the api service in VSCode
- Send a heartbeat request from anywhere with the command `curl -s http://localhost:9080/data_product_exchange/v1/heartbeat`
- The heartbeat response should be
```json
{
  "version":"1.0.581-SNAPSHOT",
  "status":"ok",
  "service_name":"data-product-api"
}
```

## How to test Mutual TLS (mTLS) authentication between client and dpx api service
1. Create the CA (Certificate Authority)
Generate the CA private key, providing a strong passphrase is desirable.
```bash
openssl genrsa -aes256 -out dpx_ca.key 4096
```
Now we'll generate the CA certificate. Feel free to leave attribute fields blank by entering a period `.`. It's recommended to specify the CommonName, this will be used to identify the certificate authority.
```bash
openssl req -new -x509 -sha256 -days 365 -key dpx_ca.key -out dpx_ca.cert
```

2. Create a Client Certificate
Generate the client private key
```bash
openssl genrsa -out dpx_client.key 2048
```
Create a signing request for the client private key. Feel free to leave attribute fields blank by entering a period `.`. It's recommended to specify the CommonName, this will be used to identify the client certificate.
```bash
openssl req -new -key dpx_client.key -out dpx_client.csr
```
Use the CA certificate to sign the request and generate the client certificate
```bash
openssl x509 -req -days 365 -sha256 -in dpx_client.csr -CA dpx_ca.cert -CAkey dpx_ca.key -set_serial 0x"$(openssl rand -hex 16)" -out dpx_client.cert
```

3. Put the CA `dpx_ca.cert` to `subprojects/common/src/main/resources/ca_certificates/server_trust_store` key store in DPX API service
4. Build the DPX code and launch the service locally
5. Obtail the CA from DPX api service, type "quit", followed by the "ENTER" key. The certificate has BEGIN CERTIFICATE and END CERTIFICATE markers.
```bash
openssl s_client -showcerts -servername teal-vm.fyre.ibm.com -connect teal-vm.fyre.ibm.com:9443 > dpx_api_cacert.pem
```
6. Extract the CA from `BEGIN CERTIFICATE` to `END CERTIFICATE`, and save it to `dpx_server_ca.cert` file
7. Send a request with the mTLS authentication
**Valid Service CA**
```bash
curl -s -X GET -H "Authorization: Bearer $access_token" --cacert dpx_server_ca.cert --cert dpx_client.cert --key dpx_client.key https://teal-vm.fyre.ibm.com:9443/data_product_exchange/v1/data_products?limit=200 | jq
```
Response:
```
{
  "limit": 200,
  "first": {
    "href": "https://teal-vm.fyre.ibm.com:9443/data_product_exchange/v1/data_products?limit=200"
  },
  "data_products": []
}
```

**Invalid Service CA**
```bash
curl -s -X GET -H "Authorization: Bearer $access_token" --cacert dpx_ca.cert --cert dpx_client.cert --key dpx_client.key https://teal-vm.fyre.ibm.com:9443/data_product_exchange/v1/data_products?limit=200 | jq
```
Response:
```
curl: (60) SSL certificate problem: self-signed certificate
More details here: https://curl.se/docs/sslcerts.html

curl failed to verify the legitimacy of the server and therefore could not
establish a secure connection to it. To learn more about this situation and
how to fix it, please visit the web page mentioned above.
```

**Invalid Client Certificate**
```bash
curl -s -X GET -H "Authorization: Bearer $access_token" --cacert dpx_ca.cert --cert dpx_client.cert --key dpx_client.key https://teal-vm.fyre.ibm.com:9443/data_product_exchange/v1/data_products?limit=200 | jq
```
Response:
```
n/a
```

## How to test mTLS authentication in browser
1. Convert the client certificate and private key into a PKCS12 format:
    - Use the following commands to combine the client certificate and private key into a PKCS12 file:
      ```bash
        cat client_cert.pem client_key.pem > pkcs12.pem
        openssl pkcs12 -in pkcs12.pem -export -out client_cert.p12
      ```
2. Import the client certificate into the browser's certificate store:
    - Open the browser and navigate to the certificate settings.
    - Import the client certificate (client_cert.p12) into the browser's personal certificate store.
3. Access the mTLS-enabled website:
    - Navigate to the website that requires mTLS authentication (e.g., https://example.com).
    - The browser will prompt you to select the client certificate for authentication.
4. Select the client certificate:
    - Choose the imported client certificate from the browser's certificate selection prompt.
5. Verify successful authentication:
    - If the client certificate is successfully selected and used for authentication, you should be able to access the mTLS-enabled website without any authentication errors.

By following these steps, you can test mTLS with a browser by configuring it to use the client certificate for mutual authentication.


## How to enable mTLS authentication in web service

