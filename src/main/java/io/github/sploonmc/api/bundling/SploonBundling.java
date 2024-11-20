package io.github.sploonmc.api.bundling;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
    private static final String SPLOON_BUNDLED = "META-INF/sploon-bundled";
    private static final String SPLOON_BUNDLED_DIR = "plugins/.sploon-bundled";

    private SploonBundling() {
    }

    private static File extractJar(InputStream stream, String jarName) {
        Path bundledPath = Bukkit.getServer().getWorldContainer().toPath();
        Path resulting = bundledPath.resolve(SPLOON_BUNDLED_DIR).resolve(jarName);
        File resultingFile = resulting.toFile();

        if (resultingFile.exists()) {
            return resultingFile;
        }

        try (FileOutputStream output = new FileOutputStream(resultingFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = stream.read(buffer)) != 1) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return resultingFile;
    }

    private static void handleBundlingJar(JavaPlugin originalPlugin, File originalPluginJar, Path jar) {
        try (JarFile jarFile = new JarFile(jar.toFile())) {
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

            File extracted = extractJar(Files.newInputStream(jar.toFile().toPath()), jar.getFileName().toString());

            Bukkit.getPluginManager().loadPlugin(extracted);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void handleBundling(JavaPlugin plugin, File jarFile) {
        try (FileSystem jarFileSystem = FileSystems.newFileSystem(jarFile.toPath(), null);
             Stream<Path> entries = Files.find(
                     jarFileSystem.getPath(SPLOON_BUNDLED),
                     1,
                     (path, _attr) -> path.endsWith(".jar"),
                     FileVisitOption.FOLLOW_LINKS
             );
        ) {
            entries.forEach(path -> {
                handleBundlingJar(plugin, jarFile, path);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
