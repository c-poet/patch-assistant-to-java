@echo off
setlocal EnableDelayedExpansion

:: 自动请求管理员权限
>nul 2>&1 "%SYSTEMROOT%\system32\cacls.exe" "%SYSTEMROOT%\system32\config\system"
if '%errorlevel%' NEQ '0' (
    echo Requesting administrative privileges...
    goto UACPrompt
) else ( goto gotAdmin )

:UACPrompt
    echo Set UAC = CreateObject^("Shell.Application"^) > "%temp%\getadmin.vbs"
    echo UAC.ShellExecute "%~s0", "", "", "runas", 1 >> "%temp%\getadmin.vbs"
    "%temp%\getadmin.vbs"
    exit /B

:gotAdmin
    if exist "%temp%\getadmin.vbs" ( del "%temp%\getadmin.vbs" )
    pushd "%CD%"
    CD /D "%~dp0"

:: 获取当前脚本所在目录（即exe所在目录）
set "CurrentDir=%~dp0"
:: 移除路径末尾的反斜杠
set "CurrentDir=%CurrentDir:~0,-1%"

echo Installing PatchAssistant2J...
echo Current directory: %CurrentDir%

:: 注册PatchAssistant2J.JAR类型
reg add "HKEY_CLASSES_ROOT\PatchAssistant2J.JAR" /ve /t REG_SZ /d "PatchAssistant2J JAR" /f
reg add "HKEY_CLASSES_ROOT\PatchAssistant2J.JAR\DefaultIcon" /ve /t REG_SZ /d "%CurrentDir%\PatchAssistant2J.exe" /f
reg add "HKEY_CLASSES_ROOT\PatchAssistant2J.JAR\shell\open\command" /ve /t REG_SZ /d "\"%CurrentDir%\PatchAssistant2J.exe\" \"--target=%%1\"" /f

:: 注册.jar类型
reg add "HKEY_CLASSES_ROOT\*\shell\PatchAssistant2J\command" /ve /t REG_SZ /d "\"%CurrentDir%\PatchAssistant2J.exe\" \"--target=%%1\"" /f
reg add "HKEY_CLASSES_ROOT\*\shell\PatchAssistant2J\command" /ve /t REG_SZ /d "\"%CurrentDir%\PatchAssistant2J.exe\" \"--target=%%1\"" /f

:: 注册文件右键菜单
reg add "HKEY_CLASSES_ROOT\.jar" /ve /t REG_SZ /d "PatchAssistant2J.JAR" /f
reg add "HKEY_CLASSES_ROOT\.jar" /v "Content Type" /t REG_SZ /d "application/java-archive" /f

:: 注册文件夹右键菜单
reg add "HKEY_CLASSES_ROOT\Directory\shell\PatchAssistant2J" /v "Icon" /t REG_SZ /d "%CurrentDir%\PatchAssistant2J.exe" /f
reg add "HKEY_CLASSES_ROOT\Directory\shell\PatchAssistant2J\command" /ve /t REG_SZ /d "\"%CurrentDir%\PatchAssistant2J.exe\" \"--patch=%%1\"" /f

:: 注册文件夹背景右键菜单
reg add "HKEY_CLASSES_ROOT\Directory\Background\shell\PatchAssistant2J" /v "Icon" /t REG_SZ /d "%CurrentDir%\PatchAssistant2J.exe" /f
reg add "HKEY_CLASSES_ROOT\Directory\Background\shell\PatchAssistant2J\command" /ve /t REG_SZ /d "\"%CurrentDir%\PatchAssistant2J.exe\" \"--patch=%%V\"" /f

:: 创建桌面快捷方式
set "DESKTOP_DIR=%PUBLIC%\Desktop"
if exist "%USERPROFILE%\Desktop" set "DESKTOP_DIR=%USERPROFILE%\Desktop"

powershell -Command "$s=(New-Object -COM WScript.Shell).CreateShortcut('%DESKTOP_DIR%\PatchAssistant2J.lnk');$s.TargetPath='%CurrentDir%\PatchAssistant2J.exe';$s.WorkingDirectory='%CurrentDir%';$s.Save()"

:: 创建开始菜单快捷方式
set "START_MENU_DIR=%APPDATA%\Microsoft\Windows\Start Menu\Programs"
powershell -Command "$s=(New-Object -COM WScript.Shell).CreateShortcut('%START_MENU_DIR%\PatchAssistant2J.lnk');$s.TargetPath='%CurrentDir%\PatchAssistant2J.exe';$s.WorkingDirectory='%CurrentDir%';$s.Save()"

echo PatchAssistant2J installed successfully!
pause
