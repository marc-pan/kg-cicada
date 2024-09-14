# A WKC installation method uses Jenkins and Fyre Quickburn
1. Create a large OCP cluster from fyre Quick burn (https://fyre.ibm.com/quick)

```text
Platform:           x86
OCP version:        4.12.22
Configuration:      large
Duration:           36 hours (max)
Additional Options: can choose name, site and Fips if needed
```

2. Log on infra-node of OCP, prepare install and create a storageclass:

```bash
$ sftp root@bests1.fyre.ibm.com
password: Speakerm1nd@123
sftp> get install.tar.gz
$ tar zxvf install.tar.gz
$ cd install
$ ./gen2-prep.sh
$ oc get storageclass
NAME PROVISIONER RECLAIMPOLICY VOLUMEBINDINGMODE ALLOWVOLUMEEXPANSION AGE
managed-nfs-storage fuseim.pri/ifs Delete Immediate false 94s
```

3. Log on Ironwood Jenkins: http://wkc-jenkins-app.fyre.ibm.com:8080 with intranet id

4. Deploy CPD by setting the following parameters, going to [Ironwood job page](http://wkc-jenkins-app.fyre.ibm.com:8080/job/Operator/job/Ironwood/) to deploy a WKC instance
You can also navigate the page from Dashboard -> Operator-based Installer -> Ironwood

```text
Operation:                  Fresh Install
Component Name:             Watson Knowledge Catalog
Release:                    4.7.2 to 4.8.1 (tested)
OpenShift REST API URL:     api.o1-XXXXXX.cp.fyre.ibm.com:6443
OpenShift Admin User:       kubeadmin
Admin User Password:        $YOUR_FYRE_Kubeadmin_Password
Root Password:              $YOUR_FYRE_ROOT_PASSWORD
Container Registry:         Staging
CASE Package:               Local
Deployment Mode:            Mini-Catalog
Install Type:               Express
Role Type:                  OOTB
Secured By:                 NONE
Vault Type:                 NONE
Logging Mode:               Terse
```
