@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-17.0.1
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d "%~dp0"
if not exist "mvnw.cmd" (
    echo Downloading Maven Wrapper...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper-distribution/3.2.0/maven-wrapper-distribution-3.2.0-bin.zip' -OutFile 'maven-wrapper.zip'; Expand-Archive -Path 'maven-wrapper.zip' -DestinationPath '.' -Force; Remove-Item 'maven-wrapper.zip'"
)
echo Starting Kawai Backend Application...
call mvnw.cmd spring-boot:run
pause

