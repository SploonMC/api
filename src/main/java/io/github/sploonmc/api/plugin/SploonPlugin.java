package io.github.sploonmc.api.plugin;

import io.github.sploonmc.api.bundling.SploonBundling;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class SploonPlugin extends JavaPlugin {

    public void enable() {}
    public void load() {}
    public void disable() {}

    @Override
    public final void onEnable() {
        this.enable();
    }

    @Override
    public final void onDisable()  {
        this.disable();
    }

    @Override
    public final void onLoad() {
        SploonBundling.handleBundling(this.getFile());
        this.load();
    }
}
