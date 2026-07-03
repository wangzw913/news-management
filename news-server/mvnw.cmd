@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup script for Windows
@REM ----------------------------------------------------------------------------
@echo off
setlocal enabledelayedexpansion

set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.10"
set "MVNW_VERBOSE=false"

if not "%JAVA_HOME%" == "" goto findJavaFromJavaHome
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. >&2
exit /b 1

:findJavaFromJavaHome
set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"

if not exist "%JAVA_EXE%" (
    echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% >&2
    exit /b 1
)

set "MAVEN_PROJECTBASEDIR=%~dp0"
set "MVNW_DIR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper"
set "MVNW_JAR=%MVNW_DIR%\maven-wrapper.jar"
set "MVNW_PROPERTIES=%MVNW_DIR%\maven-wrapper.properties"

if not exist "%MVNW_JAR%" (
    echo Downloading Maven Wrapper...
    powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.1/maven-wrapper-3.3.1.jar' -OutFile '%MVNW_JAR%'}"
    if not exist "%MVNW_JAR%" (
        echo ERROR: Failed to download Maven Wrapper JAR >&2
        echo You can manually download it from: >&2
        echo https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.1/maven-wrapper-3.3.1.jar >&2
        echo And place it at: %MVNW_JAR% >&2
        exit /b 1
    )
)

set "CLASSWORLDS_JAR=%MVNW_DIR%\maven-wrapper.jar"
if not exist "%CLASSWORLDS_JAR%" (
    set "CLASSWORLDS_JAR=%MAVEN_PROJECTBASEDIR%target\maven-wrapper.jar"
)

"%JAVA_EXE%" ^
    -classpath "%CLASSWORLDS_JAR%" ^
    "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
    org.apache.maven.wrapper.MavenWrapperMain ^
    %*
