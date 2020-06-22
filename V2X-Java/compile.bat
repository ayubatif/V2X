@echo off
title Mvn compile then copy auth and bash to target
mvn clean compile assembly:single
xcopy "./Authentication" "./target/" /s/h/e/k/f/c
xcopy "./Bash" "./target/" /s/h/e/k/f/c