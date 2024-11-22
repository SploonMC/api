plugins {
    id("io.github.sploonmc.sploon") version "0.1.0"
    java
}

group = "io.github.sploonmc.api.examples"
version = "0.1.0"

/**
 * Needed for PacketEvents, if you're not using it, you can leave out the
 * repositories block. Sploon automatically adds the following repositories:
 *
 * - Maven Central
 * - Spigot
 * - Sonatype OSS - Snapshots
 * - Sonatype OSS - Central
 * - Minecraft Libraries
 */
repositories {
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {
    /**
     * Adds the Spigot API dependency.
     */
    sploon.spigot("1.8")

    /**
     * Adds the Minecraft internals (NMS, CraftBukkit) together with its dependencies.
     * This does not run BuildTools but rather downloads the vanilla server jar and applies our custom
     * binary patches from https://github.com/SploonMC/patches, which takes up to 10 seconds depending
     * on your computer. Currently lacking remapping
     */
    // sploon.minecraft("1.8")

    /**
     * Shades the Sploon API (this example is from the Sploon API).
     */
    implementation(rootProject)

    /**
     * Jar-in-Jars the following plugins. This will make the current plugin load these
     * at load-time, provided the plugin is not already installed on the server.
     */
    sploon.pluginImplementation(files("luckperms.jar"))
    sploon.pluginImplementation("com.github.retrooper:packetevents-spigot:2.6.0")
}

/**
 * Using Java 8 as we're targeting 1.8 just since it works on every version.
 */
tasks.withType<JavaCompile>() {
    options.release = 8
}

/**
 * Copies the jars to a test server located in `examples/run`.
 * This will soon™-ish be replaced with a custom `runServer` task.
 */
tasks.register("copyJars", Copy::class) {
    from(tasks.shadowJar)

    into("../run/plugins")
}