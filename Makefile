# default target: create the jar file
ALL: jar

# always re-generate the files list
.PHONY: classes files 5 6 7 jar

jar: classes
	echo TODO: jvcard-`grep "APPLICATION_VERSION" src/be/nikiroo/jvcard/launcher/Main.java | cut -d'"' -f2`.jar
	cp jvcard-`grep "APPLICATION_VERSION" src/be/nikiroo/jvcard/launcher/Main.java | cut -d'"' -f2`.jar jvcard.jar

# Default java version: 1.5
classes: 5

5: lanterna files
	javac -cp bin/ -encoding UTF-8 -Xlint -source 5 @files -d bin/

6: lanterna files
	javac -cp bin/ -encoding UTF-8 -Xlint -source 6 @files -d bin/

7: lanterna files
	javac -cp bin/ -encoding UTF-8 -Xlint -source 7 @files -d bin/

files:
	find src/be/ -name '*.java' > files

lanterna:
	find src/com/ -name '*.java' > lanterna
	javac -encoding UTF-8 -source 5 @lanterna -d bin/ || rm lanterna
	test -e lanterna

