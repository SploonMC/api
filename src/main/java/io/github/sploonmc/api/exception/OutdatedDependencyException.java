package io.github.sploonmc.api.exception;

public class OutdatedDependencyException extends Exception {

    public OutdatedDependencyException(String pluginName, String dependencyName, String version) {
        super("Outdated dependency for '" + pluginName + "': " + dependencyName + ". Needed version: " + version + " or higher");
    }
}
