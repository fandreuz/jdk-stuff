.DEFAULT_GOAL := compile

Main.h Main.class: Main.java
	${JAVA_HOME}/bin/javac \
		--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED \
		-h . \
		Main.java

libmain.so: Main.h
	g++ -shared -fPIC -o libmain.so -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux main.cpp

run: Main.class libmain.so
	${JAVA_HOME}/bin/java \
		-XX:+PrintCodeCache2 -XX:+PrintCodeCache \
		-XX:+PrintCodeCacheOnCompilation \
		-XX:+PrintCodeHeapAnalytics \
		--add-exports java.base/jdk.internal.misc=ALL-UNNAMED \
		--add-opens java.base/jdk.internal.misc=ALL-UNNAMED \
		-Djava.library.path=. \
		Main

clean:
	rm -f Main.class Main.h libmain.so
