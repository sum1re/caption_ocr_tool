SET SCRIPT="%TEMP%\cocr.vbs"

DEL /F /Q %SCRIPT%
RD /S /Q %SCRIPT%

ECHO Set WshShell = WScript.CreateObject("WScript.Shell") >> %SCRIPT%
ECHO Set Shortcut = WshShell.CreateShortcut("%~dp0\Caption OCR Tool.lnk") >> %SCRIPT%
ECHO Shortcut.TargetPath = "%~dp0\runtime\bin\javaw.exe" >> %SCRIPT%
ECHO Shortcut.Arguments = "-server -Xmx4G -Dfile.encoding=utf-8 -Dcocr.dir=..\..\app -Djava.library.path=..\..\lib -Dprism.targetvram=2G -Dprism.vsync=false -Dprism.scrollcacheopt=true -Djavafx.preloader=com.neo.caption.ocr.AppPreloader -jar ..\..\app\cocr.jar" >> %SCRIPT%
ECHO Shortcut.WorkingDirectory = "%~dp0\runtime\bin" >> %SCRIPT%
ECHO Shortcut.IconLocation = "%~dp0\Caption OCR Tool.ico" >> %SCRIPT%
ECHO Shortcut.Save >> %SCRIPT%

CSCRIPT /nologo %SCRIPT%

DEL /F /Q %SCRIPT%
RD /S /Q %SCRIPT%