plugins {
    id("io.github.sploonmc.sploon") version "0.1.0"
    java
}

group = property("group") as String
version = property("version") as String

dependencies {
    sploon.minecraft("1.8")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile>() {
    options.release = 8
}

javaToolchains {
    compilerFor {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}