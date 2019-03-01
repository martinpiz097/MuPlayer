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
                    <id>clojars</id>
                    <url>https://clojars.org/repo</url>
                </repository>
            </repositories>
        
        Then add the dependency:
            <dependency>
                <groupId>org.orangeplayer</groupId>
                <artifactId>muplayer</artifactId>
                <version>1.2-RC</version>
            </dependency>
    Gradle:
        Add the repository:
            allprojects {
                repositories {
                    ...
                    maven { url 'https://clojars.org/repo' }
                }
            }
        Then add the dependency:
            dependencies {
                implementation 'org.orangeplayer:muplayer:1.2-RC'
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
                    <url>https://clojars.org/repo</url>
                </repository>
            </repositories>
        
        Luego añade la dependencia:
            <dependency>
                <groupId>org.orangeplayer</groupId>
                <artifactId>muplayer</artifactId>
                <version>1.2-RC</version>
            </dependency>
    Gradle:
        Añade el repositorio:
            allprojects {
                repositories {
                    ...
                    maven { url 'https://clojars.org/repo' }
                }
            }
        Luego añade la dependencia:
            dependencies {
                implementation 'org.orangeplayer:muplayer:1.2-RC'
            }
    
