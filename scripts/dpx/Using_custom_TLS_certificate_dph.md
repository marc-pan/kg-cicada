# Using custom TLS certificate to connect to the DPX API

## Prepare Issuer and Certificate file
1. Create issuer by `oc apply -f dpx-issuer.yaml`
2. Create certificate by `oc apply -f dpx-cert.yaml`
3. After that, it will create new `secret/external-tls-secret` in DPX project

## Copy out the certificate to local file
1. Copy ca.crt to local file by `oc extract -n dpx secret/external-tls-secret --to=- --keys=ca.crt > ca.crt`
2. Copy tls.crt to local file by `oc extract -n dpx secret/external-tls-secret --to=- --keys=tls.crt > tls.crt`
3. Copy tls.key to local file by `oc extract -n dpx secret/external-tls-secret --to=- --keys=tls.key > tls.key`

**Caution**: Remove 1st line in each local file

## Take effect in cpd route
1. Download cpd-cli package
2. Using cpd-cli login to ocp
  ```bash
    # ./cpd-cli manager login-to-ocp --server=https://<host>:6443 -u kubeadmin -p <password>
  ```
3. Replace self-sign certificate with custom certificate
  ```bash
    # ./cpd-cli manage setup-route --cpd_instance_ns=dpx --route_secret=external-tls-secret
  ```
4. Modify DPX front door to use the custom certificate
```bash
oc edit -n dpx zenextension/dataproduct-frontdoor-extn
```
and change the value of proxy_ssl_trusted_certificate parameter to `/etc/custom-ssl-certs/ca.crt`
5. Monitor the change take effect from `InProgress` until `Completed`
```bash
# oc get -n dpx zenextension
NAME                               STATUS      AGE
common-web-ui-zen-extension        Completed   13h
data-refinery-routes               Completed   11h
databases-zen-extensions           Completed   12h
dataproduct-frontdoor-extn         Completed   10h
dataview-base-routes               Completed   12h
environments-routes                Completed   12h
hummingbird-route-extn             Completed   12h
runtime-manager-zen-extension      Completed   12h
spaces-routes                      Completed   12h
wkc-base-routes                    Completed   12h
wml-main-zen-frontdoor-extension   Completed   12h
ws-base-zen-frontdoor-extension    Completed   12h
ws-job-scheduler-zen-extension     Completed   12h
zen-watchdog-frontdoor-extension   Completed   13h
```

## Take effect in DPX API & UI
1. Scale down the deployment to 0
```bash
# oc scale -n dpx deploy/dataproduct-api --replicas=0
# oc scale -n dpx deploy/dataproduct-ui --replicas=0
```
2. Replace `internal-tls` with `external-tls-secret` in each deployment
3. Scale up the deployment to 1
```bash
# oc scale -n dpx deploy/dataproduct-api --replicas=1
# oc scale -n dpx deploy/dataproduct-ui --replicas=1
```

## Test the TLS certifcate connect to DPX API inside pod
1. Go into anyone ibm nginx pod
2. Go to `/etc/custom-ssl-certs/..data/` directory
3. Send a request to heartbeat endpoint for DPX API service
```bash
# curl -v --cacert ./ca.crt --cert ./tls.crt --key ./tls.key https://dataproduct-api.dpx.svc.cluster.local:443/data_product_exchange/v1/heartbeat && echo
```
4. See the response in `dpx_tls_inside_pod.md` file


## Test the TLS certificate connect to DPX API outside pod
1. Go to the directory that contains the custom certificate files
2. Send a request to heartbeat endpoint for DPX API service
```bash
# curl -v --cacert ./ca.crt --cert tls.crt --key tls.key https://cpd-dpx.apps.mj1001.cp.fyre.ibm.com/data_product_exchange/v1/heartbeat && echo
```
3. See the response in `dpx_tls_outside_pod.md` file


## References
- [Creating Certificate manager (cert-manager) certificates](https://www.ibm.com/docs/en/cloud-paks/foundational-services/4.5?topic=manager-creating-certificate-cert-certificates)
- [Customizing and securing the route to the platform](https://www.ibm.com/docs/en/cloud-paks/cp-data/4.8.x?topic=environment-customizing-securing-route-platform)
- [CP4D CPD CLI reference to manage setup-route](https://www.ibm.com/docs/en/cloud-paks/cp-data/4.8.x?topic=manage-setup-route)
