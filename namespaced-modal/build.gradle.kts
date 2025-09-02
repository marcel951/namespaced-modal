plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.jline:jline-reader:3.25.1")
    implementation("org.jline:jline-terminal:3.25.1")
    implementation("org.jline:jline-console:3.25.1")
}

application {
    mainClass.set("Main")
    applicationDefaultJvmArgs = listOf(
        "--enable-preview",
        "--enable-native-access=ALL-UNNAMED"
    )
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.compileJava {
    options.compilerArgs.addAll(listOf("--enable-preview"))
    options.release.set(21)
}

tasks.test {
    jvmArgs("--enable-preview")
}

tasks.withType<JavaExec> {
    jvmArgs(
        "--enable-preview",
        "--enable-native-access=ALL-UNNAMED"
    )
    standardInput = System.`in`
}

// Shadow JAR konfigurieren (Fat JAR)
tasks.shadowJar {
    archiveBaseName.set("namespaced-modal")
    archiveVersion.set("1.0")
    archiveClassifier.set("all")

    manifest {
        attributes["Main-Class"] = "Main"
        attributes["Multi-Release"] = "true"
    }
}

// Standard JAR
tasks.jar {
    archiveBaseName.set("namespaced-modal")
    archiveVersion.set("1.0")

    manifest {
        attributes["Main-Class"] = "Main"
        attributes["Multi-Release"] = "true"
    }
}