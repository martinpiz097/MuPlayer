#!/bin/bash
INSTALL_PATH=/usr/share/muplayer
JAR_PATH=target/muplayer.jar

mvn clean package
sudo mkdir $INSTALL_PATH || true
sudo cp $JAR_PATH $INSTALL_PATH
sudo chmod -R 777 $INSTALL_PATH
#sudo rename /usr/share/muplayer/$1 /usr/share/muplayer/muplayer.jar /usr/share/muplayer/$1
