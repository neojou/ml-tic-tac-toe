#!/bin/sh

rm -fr build/dist/wasmJs/productionExecutable
rm -fr ../neojou.github.io/ml-tic-tac-toe/*
 ./gradlew wasmJsBrowserDistribution
cp -R build/dist/wasmJs/productionExecutable/. ../neojou.github.io/ml-tic-tac-toe


