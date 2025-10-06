.DEFAULT_GOAL := compile

Main.h Main.class: Main.java
	${JAVA_HOME}/bin/javac \
		-h . \
		Main.java

libmain.so: Main.h main.cpp
	g++ -shared -fPIC -o libmain.so -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux main.cpp

run: Main.class libmain.so
	${JAVA_HOME}/bin/java \
		-XX:ReservedCodeCacheSize=10M \
		-Djava.library.path=. \
		-Xlog:codecache=info,jni+resolve=error \
		Main

run-gdb:
	gdb --command gdb_conf -ex run --args ${JAVA_HOME}/bin/java \
		-XX:ReservedCodeCacheSize=10M \
		-Djava.library.path=. \
		-Xlog:codecache=info,jni+resolve=error \
		Main
clean:
	rm -f Main.class Main.h libmain.so
