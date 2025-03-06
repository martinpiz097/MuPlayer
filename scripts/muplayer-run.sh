#!/bin/bash

MUPLAYER_PATH=/usr/share/muplayer

if [ -z "$1" ]; then
	java -jar $MUPLAYER_PATH/muplayer.jar
else
	java -jar $MUPLAYER_PATH/muplayer.jar $1
fi
