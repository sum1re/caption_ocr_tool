@echo off
rem -------------------------------------------------------------------------
rem COCR Bootstrap Script for Windows
rem -------------------------------------------------------------------------

@if not "%ECHO%" == ""  echo %ECHO%
setlocal

rem Get the program name before using shift as the command modify the variable ~nx0
if "%OS%" == "Windows_NT" (
  set "PROGNAME=%~nx0%"
  set "DIRNAME=%~dp0%"
) else (
  set "PROGNAME=launch.bat"
  set DIRNAME=.\
)

pushd "%DIRNAME%.."
set "RESOLVED_COCR_HOME=%CD%"
popd

if "x%COCR_HOME%" == "x" (
  set "COCR_HOME=%RESOLVED_COCR_HOME%"
)

pushd "%COCR_HOME%"
set "SANITIZED_COCR_HOME=%CD%"
popd

if /i "%RESOLVED_COCR_HOME%" NEQ "%SANITIZED_COCR_HOME%" (
   echo.
   echo   WARNING:  COCR_HOME may be pointing to a different installation - unpredictable results may occur.
   echo.
   echo       COCR_HOME: "%COCR_HOME%"
   echo.
)

rem Read an optional configuration file.
if "x%STANDALONE_CONF%" == "x" (
   set "STANDALONE_CONF=%DIRNAME%launch.conf.bat"
)
if exist "%STANDALONE_CONF%" (
   echo Calling "%STANDALONE_CONF%"
   call "%STANDALONE_CONF%" %*
) else (
   echo Config file not found "%STANDALONE_CONF%"
)

if NOT "x%GC_LOG%" == "x" (
  set "GC_LOG=%GC_LOG%
)

rem Setup COCR specific properties
set "JAVA_OPTS=-Dprogram.name=%PROGNAME% %JAVA_OPTS%"

set "JAVA_HOME=%COCR_HOME%\jre"

if not exist "%JAVA_HOME%" (
  echo JAVA_HOME "%JAVA_HOME%" path doesn't exist
  goto END
 ) else (
   if not exist "%JAVA_HOME%\bin\java.exe" (
     echo "%JAVA_HOME%\bin\java.exe" does not exist
     goto END_NO_PAUSE
   )
    echo Setting JAVA property to "%JAVA_HOME%\bin\java"
  set "JAVA=%JAVA_HOME%\bin\java"
)

"%JAVA%" --add-modules=java.base -version >nul 2>&1 && (set MODULAR_JDK=true) || (set MODULAR_JDK=false)

set "JAVA_OPTS=-server %JAVA_OPTS%"

rem Setup directories, note directories with spaces do not work
setlocal EnableDelayedExpansion
set baseDirFound=false
set logDirFound=false
for %%a in (!JAVA_OPTS!) do (
   if !baseDirFound! == true (
      set "COCR_BASE_DIR=%%~a"
      set baseDirFound=false
   )
   if !logDirFound! == true (
      set "COCR_LOG_DIR=%%~a"
      set logDirFound=false
   )
   if "%%~a" == "-Dcocr.base.dir" (
       set baseDirFound=true
   )
   if "%%~a" == "-Dcocr.log.dir" (
       set logDirFound=true
   )
)
setlocal DisableDelayedExpansion

rem Set the app base dir
if "x%COCR_BASE_DIR%" == "x" (
  set  "COCR_BASE_DIR=%COCR_HOME%\app"
)
rem Set the app log dir
if "x%COCR_LOG_DIR%" == "x" (
  set  "COCR_LOG_DIR=%COCR_BASE_DIR%\logs"
)

setlocal EnableDelayedExpansion
call "!DIRNAME!common.bat" :setModularJdk
setlocal DisableDelayedExpansion

if "%GC_LOG%" == "true" (
  if not exist "%COCR_LOG_DIR%" > nul 2>&1 (
        mkdir "%COCR_LOG_DIR%"
  )
  rem Add rotating GC logs, if supported, and not already defined
  echo "%JAVA_OPTS%" | findstr /I "\-Xlog:*gc" > nul
  if errorlevel == 1 (
    rem Back up any prior logs
    move /y "%COCR_LOG_DIR%\gc.log" "%COCR_LOG_DIR%\backupgc.log" > nul 2>&1
    move /y "%COCR_LOG_DIR%\gc.log.0" "%COCR_LOG_DIR%\backupgc.log.0" > nul 2>&1
    move /y "%COCR_LOG_DIR%\gc.log.1" "%COCR_LOG_DIR%\backupgc.log.1" > nul 2>&1
    move /y "%COCR_LOG_DIR%\gc.log.2" "%COCR_LOG_DIR%\backupgc.log.2" > nul 2>&1
    move /y "%COCR_LOG_DIR%\gc.log.3" "%COCR_LOG_DIR%\backupgc.log.3" > nul 2>&1
    move /y "%COCR_LOG_DIR%\gc.log.4" "%COCR_LOG_DIR%\backupgc.log.4" > nul 2>&1
    move /y "%COCR_LOG_DIR%\gc.log.*.current" "%COCR_LOG_DIR%\backupgc.log.current" > nul 2>&1

    setlocal EnableDelayedExpansion
    if "!MODULAR_JDK!" == "true" (
      set TMP_PARAM=-Xlog:gc*:file="\"!COCR_LOG_DIR!\gc.log\"":time,uptimemillis:filecount=5,filesize=3M
    ) else (
      set TMP_PARAM=-verbose:gc -Xloggc:"!COCR_LOG_DIR!\gc.log" -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=3M -XX:-TraceClassUnloading
    )
    "!JAVA!" !TMP_PARAM! -version > nul 2>&1
    if not errorlevel == 1 (
      set "JAVA_OPTS=%JAVA_OPTS% !TMP_PARAM!"
    )
    rem Remove the gc.log file from the -version check
    del /F /Q "%COCR_LOG_DIR%\gc.log" > nul 2>&1
	setlocal DisableDelayedExpansion	
  )
)




echo ===============================================================================
echo.
echo   COCR Bootstrap Environment
echo.
echo   COCR_HOME: "%COCR_HOME%"
echo.
echo   JAVA: "%JAVA%"
echo.
echo   JAVA_OPTS: "%JAVA_OPTS%"
echo.
echo ===============================================================================
echo.

:RESTART
  "%JAVA%" %JAVA_OPTS% ^
    "-Dcocr.home.dir=%COCR_HOME%" ^
    "-Dcocr.base.dir=%COCR_BASE_DIR%" ^
    "-Dcocr.log.dir=%COCR_LOG_DIR%" ^
    -jar "%COCR_HOME%\cocr.jar"

if %errorlevel% equ 10 (
	goto RESTART
)

:END
if "x%NOPAUSE%" == "x" pause

:END_NO_PAUSE
