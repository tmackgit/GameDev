export CLASSPATH=.:./tmpclasses:lib/log4j-1.2.6.jar:$CLASSPATH

java -cp $CLASSPATH -verbose:gc -server com.hypefiend.javagamebook.server.GameServer
#java -cp $CLASSPATH -verbose:gc -Xrunhprof:cpu=times com.hypefiend.javagamebook.server.GameServer
