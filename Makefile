# default target: create the jar file
ALL: bin/5 jvcard.jar

jvcard.jar: bin/be/nikiroo/jvcard/*/* bin/be/nikiroo/jvcard/*
	echo TODO: jvcard-`grep "APPLICATION_VERSION" src/be/nikiroo/jvcard/launcher/Main.java | cut -d'"' -f2`.jar
	cp jvcard-`grep "APPLICATION_VERSION" src/be/nikiroo/jvcard/launcher/Main.java | cut -d'"' -f2`.jar jvcard.jar

bin/5: lanterna files
	javac -cp bin/ -encoding UTF-8 -Xlint -source 5 @files -d bin/
	touch bin/5

bin/6: lanterna files
	javac -cp bin/ -encoding UTF-8 -Xlint -source 6 @files -d bin/
	touch bin/6

bin/7: lanterna files
	javac -cp bin/ -encoding UTF-8 -Xlint -source 7 @files -d bin/
	touch bin/7

files: src/be/nikiroo/jvcard/*/* src/be/nikiroo/jvcard/*
	find src/be/ -name '*.java' > files

lanterna:
	find src/com/ -name '*.java' > lanterna
	javac -encoding UTF-8 -source 5 @lanterna -d bin/ || rm lanterna
	test -e lanterna

