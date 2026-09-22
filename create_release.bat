@echo off
setlocal

call mvn clean
if %errorlevel% neq 0 goto :error

call mvn org.codehaus.mojo:build-helper-maven-plugin:3.3.0:remove-project-artifact
if %errorlevel% neq 0 goto :error

call mvn build-helper:parse-version versions:set -DnewVersion=${parsedVersion.nextMajorVersion}
if %errorlevel% neq 0 goto :error

for /f "delims=" %%i in ('call mvn org.apache.maven.plugins:maven-help-plugin:3.4.0:evaluate -Dexpression=project.version -q -DforceStdout') do set NEW_VERSION=%%i

echo [INFO] New Version: %NEW_VERSION%

call mvn install
if %errorlevel% neq 0 goto :error

git add pom.xml
git add src/main/resources/effective-pom.xml
git commit -m "Build new version %NEW_VERSION%"
if %errorlevel% neq 0 goto :error

echo.
echo   -^> %CD%\target\RIS.war
echo.
echo [SUCCESS] Done.
goto :eof

:error
echo.
echo [FAILED] Error occurred.
exit /b %errorlevel%