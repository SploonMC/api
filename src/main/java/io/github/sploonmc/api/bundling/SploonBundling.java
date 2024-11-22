package io.github.sploonmc.api.bundling;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public final class SploonBundling {

    private static final Logger LOGGER = LogManager.getLogger("SploonBundling");
    private static final String SPLOON_BUNDLED = "META-INF/sploon-jij";
    private static final String SPLOON_BUNDLED_DIR = "plugins/.sploon-jij";
    private static final String JAR_IN_JAR_FILE_EXTENSION = ".jjar";

    private SploonBundling() {
    }

    private static File extractJar(InputStream stream, String jarName) {
        Path bundledPath = Bukkit.getServer().getWorldContainer().toPath();
        Path resulting = bundledPath.resolve(SPLOON_BUNDLED_DIR).resolve(jarName);
        File resultingFile = resulting.toFile();

        if (resultingFile.exists()) {
            return resultingFile;
        }

        if (!resultingFile.getParentFile().mkdirs() && !resultingFile.getParentFile().exists()) {
            throw new RuntimeException("Failed creating " + SPLOON_BUNDLED_DIR + " directory. Check your filesystem permissions");
        };

        try (FileOutputStream output = new FileOutputStream(resultingFile)) {
            IOUtils.copy(stream, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return resultingFile;
    }

    private static void handleBundlingJar(Path jar) {
        File extracted;
        try {
            extracted = extractJar(Files.newInputStream(jar), jar.getFileName().toString().replace(JAR_IN_JAR_FILE_EXTENSION, ".jar"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (JarFile jarFile = new JarFile(extracted)) {
            JarEntry pluginYml = jarFile.getJarEntry("plugin.yml");

            if (pluginYml == null) {
                LOGGER.warn("Dependency {} does not have a plugin.yml. Ignoring!", jar);
                return;
            }

            PluginDescriptionFile yml = new PluginDescriptionFile(jarFile.getInputStream(pluginYml));

            Plugin existingPlugin = Bukkit.getPluginManager().getPlugin(yml.getName());

            if (existingPlugin != null) {
                LOGGER.info("Dependency {} is already installed. Ignoring!", yml.getName());
                return;
            }

            // TODO: handle outdated dependencies

            Plugin loaded = Bukkit.getPluginManager().loadPlugin(extracted);
            loaded.onLoad(); // FIXME: potentially bad? needs further testing
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void handleBundling(File pluginJarFile) {
        try (FileSystem jarFileSystem = FileSystems.newFileSystem(pluginJarFile.toPath(), null);
             Stream<Path> entries = Files.find(
                     jarFileSystem.getPath(SPLOON_BUNDLED),
                     1,
                     (path, _attr) -> path.getFileName().toString().endsWith(JAR_IN_JAR_FILE_EXTENSION),
                     FileVisitOption.FOLLOW_LINKS
             );
        ) {
            entries.forEach(path -> {
                handleBundlingJar(path);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
