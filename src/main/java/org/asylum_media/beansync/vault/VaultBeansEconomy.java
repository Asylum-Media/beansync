package org.asylum_media.beansync.vault;

import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.asylum_media.beansync.Beansync;
import org.bukkit.OfflinePlayer;
import org.bukkit.Bukkit;
import java.util.List;
import java.util.Collections;



public class VaultBeansEconomy extends AbstractEconomy {

    private final Beansync plugin;

    public VaultBeansEconomy(Beansync plugin) {
        this.plugin = plugin;
    }

    // --------------------
    // Required metadata
    // --------------------

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    @Override
    public String getName() {
        return "Beansync";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 0;
    }

    @Override
    public String currencyNameSingular() {
        return "Bean";
    }

    @Override
    public String currencyNamePlural() {
        return "Beans";
    }

    @Override
    public String format(double amount) {
        return ((int) amount) + " Beans";
    }
/* =====================
   Legacy String methods
   Required by VaultAPI
   ===================== */

    @Override
    public double getBalance(String playerName) {
        return getBalance(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public double getBalance(String playerName, String worldName) {
        return getBalance(playerName);
    }

    @Override
    public boolean has(String playerName, double amount) {
        return getBalance(playerName) >= amount;
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return withdrawPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return depositPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public boolean hasAccount(String playerName) {
        return true;
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return true;
    }
/* =====================
   Bank support (not used)
   ===================== */

    @Override
    public EconomyResponse createBank(String name, String player) {
        return notSupported();
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return notSupported();
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return notSupported();
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return notSupported();
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return notSupported();
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return notSupported();
    }

    @Override
    public EconomyResponse isBankOwner(String name, String player) {
        return notSupported();
    }

    @Override
    public EconomyResponse isBankMember(String name, String player) {
        return notSupported();
    }

    @Override
    public List<String> getBanks() {
        return Collections.emptyList();
    }

    private EconomyResponse notSupported() {
        return new EconomyResponse(
                0,
                0,
                EconomyResponse.ResponseType.NOT_IMPLEMENTED,
                "Bank accounts are not supported"
        );
    }

    // --------------------
    // Core economy logic
    // --------------------

    @Override
    public double getBalance(OfflinePlayer player) {
        return plugin.getBeans(player);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return plugin.getBeans(player) >= amount;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        int current = plugin.getBeans(player);
        int withdraw = (int) amount;

        if (current < withdraw) {
            return new EconomyResponse(
                    amount,
                    current,
                    EconomyResponse.ResponseType.FAILURE,
                    "Insufficient Beans"
            );
        }

        plugin.setBeans(player, current - withdraw);
        return new EconomyResponse(
                amount,
                current - withdraw,
                EconomyResponse.ResponseType.SUCCESS,
                null
        );
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        int current = plugin.getBeans(player);
        int deposit = (int) amount;

        plugin.setBeans(player, current + deposit);
        return new EconomyResponse(
                amount,
                current + deposit,
                EconomyResponse.ResponseType.SUCCESS,
                null
        );
    }

    // --------------------
    // Account handling
    // --------------------

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return true; // scoreboard-backed, always exists
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return true; // nothing to create
    }
}
