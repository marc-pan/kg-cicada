#!/bin/env bash
set -e

print() {
  echo -e "\e[1;41m \e[1;36m $@ \e[0m"
}

export JAVA_HOME=/opt/ibm/openj9/jdk-11.0.14+9

export PORT=8080
APP_HOME="/opt/ibm/panxiny/kg-ergo-demo"
if [ x$DEBUG != 'x' ]
then
  JMX_OPTS="-Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"
fi

MAIN_CLASS="com.ibm.wdp.gs.kg.Query"
# 143600 is KA root category vertex id
# 36896 is my defined root category vertex id that has 10 depths, 41402496 is 5th category node, 37008 is 9th category node
# 8416 is my defined parent term vertex id which has 2 child terms, vertex ids are 4320 and 4104
ARGS=${@:-"-i 65640 -l 1 -t CATEGORY -n kg__999_datalineage"}

cmd="$JAVA_HOME/bin/java -Xmx1024m -Xms128m -Xss32m -XX:+HeapDumpOnOutOfMemoryError \
    ${JMX_OPTS} \
    -Dlog4j.configuration=file:${APP_HOME}/src/main/resources/log4j.properties \
    -Dgetrangemode=list \
    -Djava.compiler=NONE \
    -cp ${APP_HOME}/lib/janusgraph-foundationdb-0.1.0.jar:${APP_HOME}/target/kg-ergo-demo-1.0-SNAPSHOT.jar \
    ${MAIN_CLASS}"

if [ x$DEBUG != 'x' ]
then
  print "=============== Query Start with command line auguments: ${ARGS} ==============="
  sh scripts/java-massif.sh $cmd ${ARGS}
else
  exec $cmd $ARGS 2>&1
fi

print "ms_print massif.out.\$PID > heap.txt"
print "Detail refer to https://access.redhat.com/articles/1277173"
