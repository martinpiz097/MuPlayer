# MuPlayer
MuPlayer is a music player library written in pure Java.

## Requirements
- JDK 11 or higher

## Compilation
To compile the project, use Maven with the following command:

```bash
mvn clean install
```

## Using MuPlayer as a Dependency
### Maven
#### Add repository
Add the following repository to your pom.xml file:

```xml
<repositories>
    <repository>
        <id>clojars</id>
        <url>https://clojars.org/repo</url>
    </repository>
</repositories>
```

#### Add dependency
Add the following dependency to your pom.xml file:
```xml
<dependency>
    <groupId>org.orangeplayer</groupId>
    <artifactId>muplayer</artifactId>
    <version>RELEASE</version>
</dependency>
```

### Gradle
#### Add repository
Add the following repository to your build.gradle file:

```groovy
allprojects {
    repositories {
        maven { url 'https://clojars.org/repo' }
    }
}
```

#### Add dependency
Add the following dependency to your build.gradle file:

```groovy
dependencies {
    implementation 'org.orangeplayer:muplayer'
}
```

## Contributing
If you'd like to contribute to the project, please follow these steps:

1. Fork the repository.
2. Create a new branch (git checkout -b feature/new-feature).
3. Make your changes and commit (git commit -am 'Add new feature').
4. Push your branch (git push origin feature/new-feature).
5. Open a Merge Request.

## License
This project is licensed under the Apache License, Version 2.0