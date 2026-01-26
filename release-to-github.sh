#!/bin/sh

 ./gradlew wasmJsBrowserDistribution
cp -R build/dist/wasmJs/productionExecutable/. ../neojou.github.io/ml-tic-tac-toe


