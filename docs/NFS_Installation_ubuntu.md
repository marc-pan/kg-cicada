# NFS Installation on Ubuntu 22.04
## Setup NFS Server
1. Package install
On the **host** server

```bash
# apt update
# apt install nfs-kernel-server
```
Once the packages are installed, switch to the **client** server.

On the **client** server

```bash
# apt update
# apt install nfs-common
```

## Creating a share directory on the Host
### Exporting a general purpose mount

```bash
mkdir -p /opt/ext_disk1/cloudpack
chown nobody:nogroup /opt/ext_disk1/cloudpack
```

## Configuring the NFS Exports on the Host Server
On the **Host** server, open the `/etc/exports` file in your text editor with root privileges:
```bash
# vi /etc/exports
/opt/ext_disk1/cloudpack  *(rw,sync,no_root_squash,no_subtree_check)
```

Take a look at what each of these options mean:
- **rw**: This option gives the client computer both read and write access to the volume.
- **sync**: This option forces NFS to write changes to disk before replying. This results in a more stable and consistent environment since the reply reflects the actual state of the remote volume. However, it also reduces the speed of file operations.
- **no_subtree_check**: This option prevents subtree checking, which is a process where the host must check whether the file is actually still available in the exported tree for every request. This can cause many problems when a file is renamed while the client has it opened. In almost all cases, it is better to disable subtree checking.
- **no_root_squash**: By default, NFS translates requests from a root user remotely into a non-privileged user on the server. This was intended as security feature to prevent a root account on the client from using the file system of the host as root. `no_root_squash` disables this behavior for certain shares.

Restart the NFS server
```bash
# systemctl restart nfs-kernel-server
```

## Creating Mount Point and Mount Directory on the Client
```bash
# mount turquoise-vm.fyre.ibm.com:/opt/ext_disk1/cloudpack /mnt
# df -h
```

## Tesing NFS Access
Write a test file to the /opt/ext_disk1/cloudpack dir on the client:
```bash
# touch /opt/ext_disk1/cloudpack/general.test
```

## Mounting the remote NFS directory at boot
Open the `/etc/fstab` file with root privileges in your text editor:
```bash
# vi /etc/fstab
```

At the bottom of the file, add a line for each of your shares.
```
turquoise-vm.fyre.ibm.com:/opt/ext_disk1/cloudpack         /mnt            nfs auto,nofail,noatime,nolock,intr,tcp,actimeo=1800 0 0
```


