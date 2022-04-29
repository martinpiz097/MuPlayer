#!/bin/bash
sudo rm /usr/share/muplayer/muplayer.jar || true
sudo cp $1 /usr/share/muplayer/muplayer.jar
#sudo rename /usr/share/muplayer/$1 /usr/share/muplayer/muplayer.jar /usr/share/muplayer/$1
cd ..
