@echo off
chcp 65001 > nul
echo ============================================
echo   Dang khoi dong ung dung Spring Boot...
echo ============================================

:: Thiet lap JAVA_HOME
set JAVA_HOME=C:\Users\Admin\.jdks\ms-21.0.12
set PATH=%JAVA_HOME%\bin;%PATH%

:: Tu dong tat tien trinh cu dang chiem cong 8082
echo Dang kiem tra cong 8082...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8082 ^| findstr LISTENING') do (
    echo Tim thay tien trinh cu PID=%%a dang chiem cong 8082, dang tat...
    taskkill /PID %%a /F > nul 2>&1
)
echo Cong 8082 da san sang!
echo.

call mvnw.cmd spring-boot:run
pause
