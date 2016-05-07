###
### You can use this Makefile to generate an executable JAR file
### The available options are:
### -	make: will create the JAR file (must be compiled before)
### -	make bin/5 && make: will create a Java 1.5 JAR
### -	make bin/6 && make: will create a Java 1.6 JAR
### -	make bin/7 && make: will create a Java 1.7 JAR
### -	make bin/8 && make: will create a Java 1.8 JAR
### -	make clean: will clean temporary files
### -	make mrpropre: will clean temporary files and remove the JAR file

ALL: jvcard.jar

bin/bin: bin/be/nikiroo/jvcard/launcher/Main.class src/be/nikiroo/jvcard/*/* src/be/nikiroo/jvcard/*
	@echo You need to compile the code first:
	@echo "	make bin/5: will compile in Java 1.5 target mode"
	@echo "	make bin/6: will compile in Java 1.6 target mode"
	@echo "	make bin/7: will compile in Java 1.7 target mode"
	@echo "	make bin/8: will compile in Java 1.8 target mode"
	@false

.PHONY: ALL clean mrproper mrpropre love

love:
	@echo ...not war?

clean:
	@echo Cleaning files...
	@rm -f bin/[0-9] bin/bin bin/files bin/lanterna

mrproper: mrpropre

mrpropre: clean
	@echo Removing jar files...
	@rm -f jvcard.jar jvcard-`grep "APPLICATION_VERSION" src/be/nikiroo/jvcard/launcher/Main.java | head -n1 | cut -d'"' -f2`.jar

jvcard.jar: bin/bin
	@echo 'Main-Class: be.nikiroo.jvcard.launcher.Main' > bin/manifest
	@echo >> bin/manifest
	@echo Creating jar file jvcard-`grep "APPLICATION_VERSION" src/be/nikiroo/jvcard/launcher/Main.java | head -n1 | cut -d'"' -f2`.jar...
	jar cfm jvcard-`grep "APPLICATION_VERSION" src/be/nikiroo/jvcard/launcher/Main.java | head -n1 | cut -d'"' -f2`.jar bin/manifest -C bin/ be -C bin/ com -C bin/ default-theme.properties -C bin/ multilang
	@rm bin/manifest
	@echo Copying to jvcard.jar...
	@cp jvcard-`grep "APPLICATION_VERSION" src/be/nikiroo/jvcard/launcher/Main.java | head -n1 | cut -d'"' -f2`.jar jvcard.jar

bin/5: bin/lanterna bin/files
	@cp -r src/* bin/
	@echo Compiling in Java 1.5 mode "('make bin/5')"...
	javac -cp bin/ -encoding UTF-8 -Xlint -Xlint:-options -source 5 -target 5 @bin/files -d bin/
	@rm -f bin/[0-9]
	@touch bin/5
	@touch bin/bin

bin/6: bin/lanterna bin/files
	@cp -r src/* bin/
	@echo Compiling in Java 1.6 mode "('make bin/6')"...
	javac -cp bin/ -encoding UTF-8 -Xlint -Xlint:-options -source 6 -target 6 @bin/files -d bin/
	@rm -f bin/[0-9]
	@touch bin/6
	@touch bin/bin

bin/7: bin/lanterna bin/files
	@cp -r src/* bin/
	@echo Compiling in Java 1.7 mode "('make bin/7')"...
	javac -cp bin/ -encoding UTF-8 -Xlint -Xlint:-options -source 7 -target 7 @bin/files -d bin/
	@rm -f bin/[0-9]
	@touch bin/7
	@touch bin/bin

bin/8: bin/lanterna bin/files
	@cp -r src/* bin/
	@echo Compiling in Java 1.8 mode "('make bin/8')"...
	javac -cp bin/ -encoding UTF-8 -Xlint -Xlint:-options -source 8 -target 8 @bin/files -d bin/
	@rm -f bin/[0-9]
	@touch bin/8
	@touch bin/bin

bin/files: src/be/nikiroo/jvcard/*/* src/be/nikiroo/jvcard/*
	@mkdir -p bin/
	@find src/be/ -name '*.java' > bin/files

bin/lanterna: src/resources/* src/com/googlecode/lanterna/* src/com/googlecode/lanterna/*/* src/com/googlecode/lanterna/*/*/*
	@mkdir -p bin/
	@find src/com/ -name '*.java' > bin/lanterna
	cp -r src/resources/* bin/
	javac -encoding UTF-8 -source 5 @bin/lanterna -d bin/ || rm bin/lanterna
	@test -e bin/lanterna

