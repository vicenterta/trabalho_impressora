@echo off
setlocal
cd /d "%~dp0"
set "JAVA_BIN="
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "JAVA_BIN=%JAVA_HOME%\bin"
if not defined JAVA_BIN for /d %%J in ("%LOCALAPPDATA%\Programs\Java\jdk-21*") do if exist "%%~J\bin\javac.exe" set "JAVA_BIN=%%~J\bin"
if not defined JAVA_BIN for /f "delims=" %%J in ('where javac 2^>nul') do if not defined JAVA_BIN set "JAVA_BIN=%%~dpJ"
if not defined JAVA_BIN (
  echo Java JDK nao encontrado. Instale o JDK 21 e configure JAVA_HOME.
  pause
  exit /b 1
)
if not exist out mkdir out
"%JAVA_BIN%\javac.exe" -encoding UTF-8 -cp "libs Java\jna-5.15.0.jar" -d out src\*.java
if errorlevel 1 (
  echo Falha na compilacao. Confira o JDK 21 e a pasta libs Java.
  pause
  exit /b 1
)
if "%~1"=="--testes" (
  "%JAVA_BIN%\javac.exe" -encoding UTF-8 -cp "out;libs Java\jna-5.15.0.jar" -d out testes\Testes.java
  if errorlevel 1 exit /b 1
  "%JAVA_BIN%\java.exe" -cp "out;libs Java\jna-5.15.0.jar" Testes < nul
) else (
  "%JAVA_BIN%\java.exe" -cp "out;libs Java\jna-5.15.0.jar" Main
)
set "RESULTADO=%ERRORLEVEL%"
pause
exit /b %RESULTADO%
