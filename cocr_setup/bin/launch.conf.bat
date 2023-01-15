rem ### -*- batch file -*- ######################################################
rem #                                                                          ##
rem #  COCR Bootstrap Script Configuration                                     ##
rem #                                                                          ##
rem #############################################################################

rem #
rem # This batch file is executed by launch.bat to initialize the environment
rem # variables that launch.bat uses. It is recommended to use this file to
rem # configure these variables, rather than modifying launch.bat itself.
rem #

if not "x%JAVA_OPTS%" == "x" (
  echo "JAVA_OPTS already set in environment; overriding default settings with values: %JAVA_OPTS%"
  goto JAVA_OPTS_SET
)

rem # JVM memory allocation pool parameters - modify as appropriate.
set "JAVA_OPTS=-Xms512M -Xmx4G -XX:MetaspaceSize=256M -XX:MaxMetaspaceSize=1024M"
rem # Default encoding
set "JAVA_OPTS=%JAVA_OPTS% -Dfile.encoding=utf-8"
rem # GPU properties
set "JAVA_OPTS=%JAVA_OPTS% -Dprism.targetvram=2G -Dprism.vsync=false -Dprism.scrollcacheopt=true"

rem # Uncomment this out to control garbage collection logging
rem set "GC_LOG=true"

:JAVA_OPTS_SET
