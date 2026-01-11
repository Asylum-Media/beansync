package org.asylum_media.beansync;

import net.milkbowl.vault.economy.Economy;
import org.asylum_media.beansync.vault.VaultBeansEconomy;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;


public final class Beansync extends JavaPlugin {

    private static Beansync instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        getCommand("beans").setExecutor(new BeansCommand());

        // IMPORTANT: initialise Vault AFTER config & commands
        setupVault();

        getLogger().info("beansync enabled (objective: " + getBeansObjectiveName() + ")");
    }
    public int getBeans(OfflinePlayer player) {
        Scoreboard board = getServer().getScoreboardManager().getMainScoreboard();
        Objective objective = board.getObjective(getBeansObjectiveName());
        if (objective == null) return 0;
        return objective.getScore(player.getName()).getScore();
    }

    public void setBeans(OfflinePlayer player, int value) {
        Scoreboard board = getServer().getScoreboardManager().getMainScoreboard();
        Objective objective = board.getObjective(getBeansObjectiveName());
        if (objective == null) return;
        objective.getScore(player.getName()).setScore(Math.max(0, value));
    }

    @Override
    public void onDisable() {
        getLogger().info("beansync disabled");
    }

    private void setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault not found – Vault integration disabled.");
            return;
        }

        getServer().getServicesManager().register(
                Economy.class,
                new VaultBeansEconomy(this),
                this,
                ServicePriority.Highest
        );

        getLogger().info("Vault economy hooked (Beansync).");
    }

    public static Beansync getInstance() {
        return instance;
    }

    public String getBeansObjectiveName() {
        return getConfig().getString("beans-objective", "Beans");
    }
}
