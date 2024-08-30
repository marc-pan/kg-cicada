Make a curl request with trust CA certificate via OCP route/cpd

```
[root@api.mj1001.cp.fyre.ibm.com custom-cert]# curl -v --cacert ./ca.crt --cert tls.crt --key tls.key https://cpd-dpx.apps.mj1001.cp.fyre.ibm.com/data_product_exchange/v1/heartbeat && echo
*   Trying 9.46.198.211...
* TCP_NODELAY set
* Connected to cpd-dpx.apps.mj1001.cp.fyre.ibm.com (9.46.198.211) port 443 (#0)
* ALPN, offering h2
* ALPN, offering http/1.1
* successfully set certificate verify locations:
*   CAfile: ./ca.crt
  CApath: none
* TLSv1.3 (OUT), TLS handshake, Client hello (1):
* TLSv1.3 (IN), TLS handshake, Server hello (2):
* TLSv1.3 (IN), TLS handshake, [no content] (0):
* TLSv1.3 (IN), TLS handshake, Encrypted Extensions (8):
* TLSv1.3 (IN), TLS handshake, [no content] (0):
* TLSv1.3 (IN), TLS handshake, Certificate (11):
* TLSv1.3 (IN), TLS handshake, [no content] (0):
* TLSv1.3 (IN), TLS handshake, CERT verify (15):
* TLSv1.3 (IN), TLS handshake, [no content] (0):
* TLSv1.3 (IN), TLS handshake, Finished (20):
* TLSv1.3 (OUT), TLS change cipher, Change cipher spec (1):
* TLSv1.3 (OUT), TLS handshake, [no content] (0):
* TLSv1.3 (OUT), TLS handshake, Finished (20):
* SSL connection using TLSv1.3 / TLS_AES_128_GCM_SHA256
* ALPN, server did not agree to a protocol
* Server certificate:
*  subject: CN=dpx-ca-tls
*  start date: Mar 29 14:20:50 2024 GMT
*  expire date: Jun 27 14:20:50 2024 GMT
*  subjectAltName: host "cpd-dpx.apps.mj1001.cp.fyre.ibm.com" matched cert's "*.apps.mj1001.cp.fyre.ibm.com"
*  issuer: CN=dpx-ca-tls
*  SSL certificate verify ok.
* TLSv1.3 (OUT), TLS app data, [no content] (0):
> GET /data_product_exchange/v1/heartbeat HTTP/1.1
> Host: cpd-dpx.apps.mj1001.cp.fyre.ibm.com
> User-Agent: curl/7.61.1
> Accept: */*
>
* TLSv1.3 (IN), TLS handshake, [no content] (0):
* TLSv1.3 (IN), TLS handshake, Newsession Ticket (4):
* TLSv1.3 (IN), TLS handshake, [no content] (0):
* TLSv1.3 (IN), TLS handshake, Newsession Ticket (4):
* TLSv1.3 (IN), TLS app data, [no content] (0):
< HTTP/1.1 200 OK
< date: Fri, 29 Mar 2024 15:03:12 GMT
< content-type: application/json
< content-length: 69
< x-global-transaction-id: 27ea83b0-9b80-4836-b623-8860fa012ca4
< strict-transport-security: max-age=8640000; includeSubDomains
< x-xss-protection: 1; mode=block
< content-security-policy: default-src 'none'
< x-content-type-options: nosniff
< cache-control: no-cache, no-store, must-revalidate
< pragma: no-cache
< x-frame-options: DENY
< content-language: en-US
< server: ---
< x-frame-options: SAMEORIGIN
< set-cookie: 6211807d0c0a17113379d731e6302ffb=ffda2fc8df10535a83d55dbd162db48f; path=/; HttpOnly; Secure; SameSite=Lax
<
* Connection #0 to host cpd-dpx.apps.mj1001.cp.fyre.ibm.com left intact
{"version":"1.0.626","status":"ok","service_name":"data-product-api"}
```
