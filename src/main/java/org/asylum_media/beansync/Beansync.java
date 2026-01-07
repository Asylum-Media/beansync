package org.asylum_media.beansync;

import org.bukkit.plugin.java.JavaPlugin;

public final class Beansync extends JavaPlugin {

    private static Beansync instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        getCommand("beans").setExecutor(new BeansCommand());
        getLogger().info("beansync enabled (objective: " + getBeansObjectiveName() + ")");
    }

    @Override
    public void onDisable() {
        getLogger().info("beansync disabled");
    }

    public static Beansync getInstance() {
        return instance;
    }

    public String getBeansObjectiveName() {
        return getConfig().getString("beans-objective", "Beans");
    }
}
