@echo off
setlocal

rem generate-config-classes.cmd
rem
rem Launches the Swing "Config Class Generator" UI (Java 17), which lets you
rem pick a config-classes description XML file and a destination source
rem directory, and generates the config bean (+ DiffHelper) classes from it -
rem the replacement for the old Ant "codegen" target.
rem
rem Usage:
rem   generate-config-classes.cmd                 (opens the UI empty)
rem   generate-config-classes.cmd config-classes.xml   (pre-fills the XML field)
rem
rem Prerequisite: build the project once with:
rem   mvn clean package
rem which produces target\configclasses-framework-3.0.0-generator.jar
rem (an all-in-one jar with all dependencies bundled).

set SCRIPT_DIR=%~dp0
set GENERATOR_JAR=%SCRIPT_DIR%target\configclasses-framework-3.0.0-generator.jar

if not exist "%GENERATOR_JAR%" (
	echo Could not find "%GENERATOR_JAR%".
	echo Build the project first with:  mvn clean package
	exit /b 1
)

C:/java/jdk17/bin/java -jar "%GENERATOR_JAR%" %*

endlocal
