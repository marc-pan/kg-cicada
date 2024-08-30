Make curl request inside ibm nginx pod to the data product exchange api service with trust CA certificate.

```bash
bash-5.1$ curl -v --cacert ./ca.crt --cert ./tls.crt --key ./tls.key https://dataproduct-api.dpx.svc.cluster.local:443/data_product_exchange/v1/heartbeat && echo
*   Trying 172.30.169.66:443...
* Connected to dataproduct-api.dpx.svc.cluster.local (172.30.169.66) port 443 (#0)
* ALPN, offering h2
* ALPN, offering http/1.1
*  CAfile: ./ca.crt
* TLSv1.0 (OUT), TLS header, Certificate Status (22):
* TLSv1.3 (OUT), TLS handshake, Client hello (1):
* TLSv1.2 (IN), TLS header, Certificate Status (22):
* TLSv1.3 (IN), TLS handshake, Server hello (2):
* TLSv1.2 (IN), TLS handshake, Certificate (11):
* TLSv1.2 (IN), TLS handshake, Server key exchange (12):
* TLSv1.2 (IN), TLS handshake, Server finished (14):
* TLSv1.2 (OUT), TLS header, Certificate Status (22):
* TLSv1.2 (OUT), TLS handshake, Client key exchange (16):
* TLSv1.2 (OUT), TLS header, Finished (20):
* TLSv1.2 (OUT), TLS change cipher, Change cipher spec (1):
* TLSv1.2 (OUT), TLS header, Certificate Status (22):
* TLSv1.2 (OUT), TLS handshake, Finished (20):
* TLSv1.2 (IN), TLS header, Finished (20):
* TLSv1.2 (IN), TLS header, Certificate Status (22):
* TLSv1.2 (IN), TLS handshake, Finished (20):
* SSL connection using TLSv1.2 / ECDHE-RSA-AES128-GCM-SHA256
* ALPN, server accepted to use h2
* Server certificate:
*  subject: CN=dpx-ca-tls
*  start date: Mar 29 09:43:19 2024 GMT
*  expire date: Jun 27 09:43:19 2024 GMT
*  subjectAltName: host "dataproduct-api.dpx.svc.cluster.local" matched cert's "*.dpx.svc.cluster.local"
*  issuer: CN=dpx-ca-tls
*  SSL certificate verify ok.
* Using HTTP2, server supports multi-use
* Connection state changed (HTTP/2 confirmed)
* Copying HTTP/2 data in stream buffer to connection buffer after upgrade: len=0
* TLSv1.2 (OUT), TLS header, Unknown (23):
* TLSv1.2 (OUT), TLS header, Unknown (23):
* TLSv1.2 (OUT), TLS header, Unknown (23):
* Using Stream ID: 1 (easy handle 0x5604112950a0)
* TLSv1.2 (OUT), TLS header, Unknown (23):
> GET /data_product_exchange/v1/heartbeat HTTP/2
> Host: dataproduct-api.dpx.svc.cluster.local
> user-agent: curl/7.76.1
> accept: */*
>
* TLSv1.2 (IN), TLS header, Unknown (23):
* TLSv1.2 (OUT), TLS header, Unknown (23):
* TLSv1.2 (IN), TLS header, Unknown (23):
* TLSv1.2 (IN), TLS header, Unknown (23):
< HTTP/2 200
< server: IBM Data Product API Service/1.0.626
< x-global-transaction-id: bd687d17-be99-45fd-af77-d10da3e35ab8
< strict-transport-security: max-age=8640000; includeSubDomains
< x-xss-protection: 1; mode=block
< content-security-policy: default-src 'none'
< x-content-type-options: nosniff
< cache-control: no-cache, no-store, must-revalidate
< pragma: no-cache
< content-type: application/json
< date: Fri, 29 Mar 2024 13:37:12 GMT
< x-frame-options: DENY
< content-language: en-US
< content-length: 69
<
* TLSv1.2 (IN), TLS header, Unknown (23):
* Connection #0 to host dataproduct-api.dpx.svc.cluster.local left intact
{"version":"1.0.626","status":"ok","service_name":"data-product-api"}
```
