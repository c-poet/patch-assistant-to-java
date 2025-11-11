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

echo Installing PatchAssistant2J context menu...
echo Current directory: %CurrentDir%

:: 注册文件右键菜单
reg add "HKEY_CLASSES_ROOT\*\shell\PatchAssistant2J" /v "Icon" /t REG_SZ /d "%CurrentDir%\PatchAssistant2J.exe" /f
reg add "HKEY_CLASSES_ROOT\*\shell\PatchAssistant2J\command" /ve /t REG_SZ /d "\"%CurrentDir%\PatchAssistant2J.exe\" \"--target=%%1\"" /f

:: 注册文件夹右键菜单
reg add "HKEY_CLASSES_ROOT\Directory\shell\PatchAssistant2J" /v "Icon" /t REG_SZ /d "%CurrentDir%\PatchAssistant2J.exe" /f
reg add "HKEY_CLASSES_ROOT\Directory\shell\PatchAssistant2J\command" /ve /t REG_SZ /d "\"%CurrentDir%\PatchAssistant2J.exe\" \"--patch=%%1\"" /f

:: 注册文件夹背景右键菜单
reg add "HKEY_CLASSES_ROOT\Directory\Background\shell\PatchAssistant2J" /v "Icon" /t REG_SZ /d "%CurrentDir%\PatchAssistant2J.exe" /f
reg add "HKEY_CLASSES_ROOT\Directory\Background\shell\PatchAssistant2J\command" /ve /t REG_SZ /d "\"%CurrentDir%\PatchAssistant2J.exe\" \"--patch=%%V\"" /f

echo PatchAssistant2J context menu installed successfully!
pause
