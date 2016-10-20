# How to watch remote java process

To connect to remote JVM over JMX,
alter script /usr/local/bin/shtrom like following.

```
exec java \
-server \
-Xms1024m \
-Xmx1024m \
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.port=43001 \
-Dcom.sun.management.jmxremote.rmi.port=43002 \
-Djava.rmi.server.hostname=localhost \
shtrom.handler
```

Note that client application VisualVM needs not only JMX (Java Management Extensions) protocol but also RMI (Remote Method Invocation) protocol for controlling JVM.
After starting java process, forward JMX and RMI port with ssh.

```
ssh -L 43001:localhost:43001 -L 43002:localhost:43002 shtrom-profile
```

Now you can connect with VisualVM on localhost:43001 without authentication.

