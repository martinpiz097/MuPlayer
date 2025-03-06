#!/bin/bash
SCRIPTS_PARENT_FOLDER=$(cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd)
INSTALL_PATH=/usr/share/muplayer
JAR_PATH=target/muplayer.jar

mvn clean package
sudo mkdir $INSTALL_PATH || true
sudo cp $JAR_PATH $INSTALL_PATH
sudo cp $SCRIPTS_PARENT_FOLDER/muplayer-run.sh $INSTALL_PATH
sudo chmod -R 777 $INSTALL_PATH

sudo unlink /usr/bin/muplayer || true
sudo ln -s $INSTALL_PATH/muplayer-run.sh /usr/bin/muplayer
