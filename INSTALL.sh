#!/bin/bash
sudo rm -r /usr/share/muplayer/muplayer.jar
sudo mv $1 /usr/share/muplayer/
sudo rename /usr/share/muplayer/$1 /usr/share/muplayer/muplayer.jar /usr/share/muplayer/$1
