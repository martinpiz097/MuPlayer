# MuPlayer
Music player library in pure Java.

## Note: 
    To compile the project you must execute "mvn clean install" command to
    generate the jar file, this file you can find in "target" folder.

## Note 2:
    If you want to use the project as a dependency add the following lines if you are working with maven or gradle:

    Maven:
        Add the repository:
            <repositories>
                <repository>
                    <id>jitpack.io</id>
                    <url>https://jitpack.io</url>
                </repository>
            </repositories>
        
        Then add the dependency:
            <dependency>
                <groupId>com.gitlab.martinpiz097</groupId>
                <artifactId>muplayer</artifactId>
                <version>1.1</version>
            </dependency>
    Gradle:
        Add the repository:
            allprojects {
                repositories {
                    ...
                    maven { url 'https://jitpack.io' }
                }
            }
        Then add the dependency:
            dependencies {
                implementation 'com.gitlab.martinpiz097:MuPlayer:Tag'
            }
# ------------------------------------------------------------------------------------

## Nota: 
    Para compilar el proyecto debe ejecutar el comando "mvn clean install" para 
    generar el archivo jar, este archivo se puede encontrar en la carpeta "target".
    
## Nota 2:

    Si tu deseas utilizar el proyecto como dependencia agregar las siguientes líneas si estas trabajando con maven o gradle:

    Maven:
        Añade el repositorio:
            <repositories>
                <repository>
                    <id>jitpack.io</id>
                    <url>https://jitpack.io</url>
                </repository>
            </repositories>
        
        Luego añade la dependencia:
            <dependency>
                <groupId>com.gitlab.martinpiz097</groupId>
                <artifactId>muplayer</artifactId>
                <version>1.1</version>
            </dependency>
    Gradle:
        Añade el repositorio:
            allprojects {
                repositories {
                    ...
                    maven { url 'https://jitpack.io' }
                }
            }
        Luego añade la dependencia:
            dependencies {
                implementation 'com.gitlab.martinpiz097:MuPlayer:Tag'
            }
    
