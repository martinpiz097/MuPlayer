# MuPlayer
Music player library in pure Java.

## Note 1: If you wish to edit the source code and compile you will need the AuCom dependency, you will find it in the following link: https://gitlab.com/martinpiz097/AuCom
    Once downloaded and inside the project folder execute: 
    mvn install 
    to compile the project in the local maven repository of your machine.

## Note 2: To compile the missing dependency jogg and tritonus_jorbis you must execute the following commands: 
    mvn install: install-file -Dfile = libs/jogg-0.0.7.jar -DgroupId = org.soundlibs -DartifactId = jogg -Dversion = 0.0.7 -Dpackaging = jar
    mvn install: install-file -Dfile = libs/tritonus_jorbis-0.3.6.jar -DgroupId = org.tritonus -DartifactId = jorbis -Dversion = 0.3.6 -Dpackaging = jar
    being in the root folder of the project, which has the same effect as mvn install for the previous case.

