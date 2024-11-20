plugins {
    id("java")
    id("io.github.sploonmc.sploon") version "0.1.0"
    id("com.gradleup.shadow") version "8.3.5"
}

group = "io.github.sploonmc.api.examples"
version = "0.1.0"

repositories {
    exclusiveContent {
        forRepository {
            maven("https://api.modrinth.com/maven")
        }

        filter {
            includeGroup("maven.modrinth")
        }
    }
}

tasks.assemble {
    dependsOn("shadowJar")
}

dependencies {
    sploon.spigot("1.8")
    implementation(rootProject)

    // The modrinth maven repo just does not contain the latest LP version?
    // Or at least, I cannot find it.
    sploon.pluginImplementation(files("luckperms.jar"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile>() {
    options.release = 8
}
