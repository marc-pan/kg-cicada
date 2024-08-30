## How to enable RMQ route in CP4D environment
### Log in the CP4D cluster

```bash
# oc login https://api.oc4-7.pl.eurolabs.ibm.com:6443 --insecure-skip-tls-verify -u ocuser -p ocuser
```

### Retrieve RMQ information

```bash
# oc get pod | grep rabbit
rabbitmq-ha-0                                                1/1     Running             0          33h
rabbitmq-ha-1                                                1/1     Running             0          31h
rabbitmq-ha-2                                                1/1     Running             0          31h
# oc get service | grep rabbit
rabbitmq-ha                              ClusterIP   172.30.209.105   <none>        15671/TCP,5672/TCP,4369/TCP,25672/TCP,5671/TCP                                    3d
rabbitmq-ha-discovery                    ClusterIP   None             <none>        15671/TCP,5672/TCP,4369/TCP,25672/TCP,5671/TCP                                    3d
```

### Create RMQ route

```
# oc create route passthrough rabbitmq-ha --service=rabbitmq-ha -n wkc
# oc get route -n wkc
NAME          HOST/PORT                                        PATH   SERVICES        PORT                   TERMINATION          WILDCARD
cpd           cpd-wkc.apps.oc4-7.pl.eurolabs.ibm.com                  ibm-nginx-svc   ibm-nginx-https-port   reencrypt/Redirect   None
rabbitmq-ha   rabbitmq-ha-wkc.apps.oc4-7.pl.eurolabs.ibm.com          rabbitmq-ha     http                   passthrough          None
```

### Obtain RMQ username&password

```bash
##get password of the admin
# oc get secret rabbitmq-ha -n wkc -o jsonpath='{ .data.rabbitmq-password }' | base64 -d
agpvzvb0au69of6whqo6lwcB%
##get user name
# oc get secret rabbitmq-ha -n wkc -o jsonpath='{ .data.rabbitmq-username }' | base64 -d
admin%
```
