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

ALL: bin/5 jvcard.jar

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

jvcard.jar: bin/be/nikiroo/jvcard/*/* bin/be/nikiroo/jvcard/*
	@mkdir -p bin/
	@echo 'Main-Class: be.nikiroo.jvcard.launcher.Main' > bin/manifest
	@echo >> bin/manifest
	@echo Creating jar file jvcard-`grep "APPLICATION_VERSION" src/be/nikiroo/jvcard/launcher/Main.java | cut -d'"' -f2`.jar...
	jar -m bin/manifest -cf jvcard-`grep "APPLICATION_VERSION" src/be/nikiroo/jvcard/launcher/Main.java | cut -d'"' -f2`.jar -C bin/ be -C bin/ com
	@rm bin/manifest
	@echo Copying to jvcard.jar...
	@cp jvcard-`grep "APPLICATION_VERSION" src/be/nikiroo/jvcard/launcher/Main.java | cut -d'"' -f2`.jar jvcard.jar

bin/5: lanterna bin/files
	@echo Compiling in Java 1.5 mode "('make bin/5')"...
	javac -cp bin/ -encoding UTF-8 -Xlint -source 5 @bin/files -d bin/
	@rm -f bin/[0-9]
	@touch bin/5

bin/6: lanterna bin/files
	@echo Compiling in Java 1.6 mode "('make bin/6')"...
	javac -cp bin/ -encoding UTF-8 -Xlint -source 6 @bin/files -d bin/
	@rm -f bin/[0-9]
	@touch bin/6

bin/7: lanterna bin/files
	@echo Compiling in Java 1.7 mode "('make bin/7')"...
	javac -cp bin/ -encoding UTF-8 -Xlint -source 7 @bin/files -d bin/
	@rm -f bin/[0-9]
	@touch bin/7

bin/8: lanterna bin/files
	@echo Compiling in Java 1.8 mode "('make bin/8')"...
	javac -cp bin/ -encoding UTF-8 -Xlint -source 8 @bin/files -d bin/
	@rm -f bin/[0-9]
	@touch bin/8

bin/files: src/be/nikiroo/jvcard/*/* src/be/nikiroo/jvcard/*
	@mkdir -p bin/
	@find src/be/ -name '*.java' > bin/files

bin/lanterna:
	@mkdir -p bin/
	@find src/com/ -name '*.java' > bin/lanterna
	javac -encoding UTF-8 -source 5 @lanterna -d bin/ || rm bin/lanterna
	@test -e bin/lanterna

