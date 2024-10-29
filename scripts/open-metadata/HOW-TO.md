# HOW TO
## How to pull the image from docker hub in China network environment
Typically, it should ask to add the docker repostiroy `docker.getcollate.io/openmetadata` to a docker registry mirror in allow list. For instance, m.daocloud.io/docker.getcollate.io/openmetadata

```bash
marc:kg-ergo-demo/ (main) $ podman images                                                                                                                                                                                                                                                                                       [20:52:45]
REPOSITORY                                                   TAG         IMAGE ID      CREATED        SIZE
m.daocloud.io/docker.getcollate.io/openmetadata/ingestion    1.5.7       3a4b8119205a  11 days ago    4.42 GB
m.daocloud.io/docker.getcollate.io/openmetadata/server       1.5.7       a70ebe8d464e  11 days ago    480 MB
m.daocloud.io/docker.getcollate.io/openmetadata/db           1.5.7       d14f1e25f29c  11 days ago    520 MB
m.daocloud.io/docker.getcollate.io/openmetadata/postgresql   1.5.7       842117d24ca4  11 days ago    455 MB
m.daocloud.io/docker.elastic.co/elasticsearch/elasticsearch  8.10.2      23c2475f409b  13 months ago  760 MB
```

## How to get running the OpenMetadata instance in MacOS through Podman Desktop App
1. Set the system configuration for Elasticsearch instance in podman default machine
```bash
# podman machine ssh
Connecting to vm podman-machine-default. To close connection, use `~.` or `exit`
Fedora CoreOS 40.20241019.2.0
Tracker: https://github.com/coreos/fedora-coreos-tracker
Discuss: https://discussion.fedoraproject.org/tag/coreos

Last login: Tue Oct 29 09:54:25 2024 from 192.168.127.1
core@localhost:~$ echo vm.max_map_count = 262144 > sudo tee /etc/sysctl.conf
core@localhost:~$ echo "* - nofile 65535" > sudo tee /etc/security/limits.conf
core@localhost:~$ exit
```
2. Restart the podman default machine and Podman Desktop App
3. Enlarge podman resource configuration, such as CPU 8cores, Memory 12GB
4. Adjust the heap size for Elasticsearch instance and OpenMetadata server in `docker-compose-postgres.yml` or `docker-compose.yml` file
For Elasticsearch,
ES_JAVA_OPTS=-Xms500m -Xmx500m

For OpenMetadata,
OPENMETADATA_HEAP_OPTS: ${OPENMETADATA_HEAP_OPTS:--Xmx512m -Xms512m}

## How to make migrate server working
Doing investigation right now
