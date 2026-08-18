@echo off
title Lacroix Electronics
cd /d "%~dp0"
echo Demarrage de Lacroix Electronics...
java --add-modules javafx.controls,javafx.fxml,javafx.swing,javafx.web ^
     --add-exports javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED ^
     --add-exports javafx.graphics/com.sun.javafx.scene.input=ALL-UNNAMED ^
     -jar LacroixElectronics.jar
if %errorlevel% neq 0 (
    echo.
    echo ERREUR: Java 17+ requis.
    echo Telechargez: https://adoptium.net/temurin/releases/?version=17
    pause
)
