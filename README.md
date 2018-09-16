# MuPlayer
Music player library in pure Java.

## Note 1: If you wish to edit the source code and compile you will need the AuCom dependency, you will find it in the following link: https://gitlab.com/martinpiz097/AuCom
    Once downloaded and inside the project folder execute: 
    mvn install 
    to compile the project in the local maven repository of your machine.

## Note 2: To compile the missing dependencies jogg and tritonus_jorbis you must execute the following commands: 
    mvn install: install-file -Dfile = libs/jogg-0.0.7.jar -DgroupId = org.soundlibs -DartifactId = jogg -Dversion = 0.0.7 -Dpackaging = jar
    
    mvn install: install-file -Dfile = libs/tritonus_jorbis-0.3.6.jar -DgroupId = org.tritonus -DartifactId = jorbis -Dversion = 0.3.6 -Dpackaging = jar
    
    You must be placed in the root folder of the project, which has the same effect as mvn install for the previous case.
# ------------------------------------------------------------------------------------
## Nota 1: Si desea editar el código fuente y compilar, necesitará la dependencia AuCom, la encontrará en el siguiente enlace: https://gitlab.com/martinpiz097/AuCom
     Una vez descargado y dentro de la carpeta del proyecto, ejecute:
     mvn install
     para compilar el proyecto en el repositorio Maven local de su máquina.

## Nota 2: Para compilar las dependencias que faltan jogg y tritonus_jorbis debes ejecutar los siguientes comandos:
     mvn install: install-file -Dfile = libs/jogg-0.0.7.jar -DgroupId = org.soundlibs -DartifactId = jogg -Dversion = 0.0.7 -Dpackaging = jar
     
     mvn install: install-file -Dfile = libs/tritonus_jorbis-0.3.6.jar -DgroupId = org.tritonus -DadifactId = jorbis -Dversion = 0.3.6 -Dpackaging = jar
  
    ud se debe situar en la carpeta raíz del proyecto, que tiene el mismo efecto que mvn install para el caso anterior.
