@echo off
title Lacroix Electronics
echo Demarrage de Lacroix Electronics...
java -jar --add-modules=javafx.controls,javafx.fxml,javafx.swing,javafx.web --add-exports javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.scene.input=ALL-UNNAMED LacroixElectronics.jar
if %errorlevel% neq 0 (
    echo Erreur: Java 17+ est requis. Telechargez Java sur https://adoptium.net
    pause
)