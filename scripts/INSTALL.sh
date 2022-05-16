#!/bin/bash
mvn package
sudo cp target/muplayer.jar /usr/share/muplayer
#sudo rename /usr/share/muplayer/$1 /usr/share/muplayer/muplayer.jar /usr/share/muplayer/$1
