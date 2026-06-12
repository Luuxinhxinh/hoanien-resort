@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-17
cd /d "%~dp0"
apache-maven-3.9.6\bin\mvn.cmd test -Dtest=TourBookingServiceTest,TourBookingTddServiceTest -DfailIfNoTests=false
pause