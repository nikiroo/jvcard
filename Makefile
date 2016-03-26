###
### You can use this Makefile to generate an executable JAR file
### The available options are:
### -	make: will create the default (or currently compiled) JAR
### -	make bin/5 && make: will create a Java 1.5 JAR
### -	make bin/6 && make: will create a Java 1.6 JAR
### -	make bin/7 && make: will create a Java 1.7 JAR
### -	make bin/8 && make: will create a Java 1.8 JAR
### -	make clean: will clean temporary files
### -	make mrpropre: will clean temporary files and remove the JAR file

ALL: jvcard.jar

# Default: Java 1.5
bin/be/nikiroo/jvcard/launcher/Main.class: bin/5

.PHONY: ALL clean mrproper mrpropre love

love:
	@echo ...not war?

clean:
	@echo Cleaning files...
	@rm -f bin/[0-9] bin/files bin/lanterna

mrproper: mrpropre

mrpropre: clean
	@echo Removing jar files...
	@rm -f jvcard.jar jvcard-`grep "APPLICATION_VERSION" src/be/nikiroo/jvcard/launcher/Main.java | cut -d'"' -f2`.jar

jvcard.jar: bin/be/nikiroo/jvcard/launcher/Main.class src/be/nikiroo/jvcard/*/* src/be/nikiroo/jvcard/*
	@mkdir -p bin/
	@echo 'Main-Class: be.nikiroo.jvcard.launcher.Main' > bin/manifest
	@echo >> bin/manifest
	@echo Creating jar file jvcard-`grep "APPLICATION_VERSION" src/be/nikiroo/jvcard/launcher/Main.java | cut -d'"' -f2`.jar...
	jar cfm jvcard-`grep "APPLICATION_VERSION" src/be/nikiroo/jvcard/launcher/Main.java | cut -d'"' -f2`.jar bin/manifest -C bin/ be -C bin/ com
	@rm bin/manifest
	@echo Copying to jvcard.jar...
	@cp jvcard-`grep "APPLICATION_VERSION" src/be/nikiroo/jvcard/launcher/Main.java | cut -d'"' -f2`.jar jvcard.jar

bin/5: bin/lanterna bin/files
	@cp -r src/* bin/
	@echo Compiling in Java 1.5 mode "('make bin/5')"...
	javac -cp bin/ -encoding UTF-8 -Xlint -Xlint:-options -source 5 -target 5 @bin/files -d bin/
	@rm -f bin/[0-9]
	@touch bin/5

bin/6: bin/lanterna bin/files
	@cp -r src/* bin/
	@echo Compiling in Java 1.6 mode "('make bin/6')"...
	javac -cp bin/ -encoding UTF-8 -Xlint -Xlint:-options -source 6 -target 6 @bin/files -d bin/
	@rm -f bin/[0-9]
	@touch bin/6

bin/7: bin/lanterna bin/files
	@cp -r src/* bin/
	@echo Compiling in Java 1.7 mode "('make bin/7')"...
	javac -cp bin/ -encoding UTF-8 -Xlint -Xlint:-options -source 7 -target 7 @bin/files -d bin/
	@rm -f bin/[0-9]
	@touch bin/7

bin/8: bin/lanterna bin/files
	@cp -r src/* bin/
	@echo Compiling in Java 1.8 mode "('make bin/8')"...
	javac -cp bin/ -encoding UTF-8 -Xlint -Xlint:-options -source 8 -target 8 @bin/files -d bin/
	@rm -f bin/[0-9]
	@touch bin/8

bin/files: src/be/nikiroo/jvcard/*/* src/be/nikiroo/jvcard/*
	@mkdir -p bin/
	@find src/be/ -name '*.java' > bin/files

bin/lanterna:
	@mkdir -p bin/
	@find src/com/ -name '*.java' > bin/lanterna
	javac -encoding UTF-8 -source 5 @bin/lanterna -d bin/ || rm bin/lanterna
	@test -e bin/lanterna

