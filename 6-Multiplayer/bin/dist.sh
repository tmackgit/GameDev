# zip up the source and related files
find ../javagamebook/src -name "*.java" -print | zip chap06.zip -@
find ../javagamebook/bin -name "*.sh" -print | zip chap06.zip -@
find ../javagamebook/lib -name "LICENSE" -print | zip chap06.zip -@
find ../javagamebook/lib -name "*.jar" -print | zip chap06.zip -@
find ../javagamebook/ -name "build.xml" -print | zip chap06.zip -@
find ../javagamebook/ -name "README" -print | zip chap06.zip -@
