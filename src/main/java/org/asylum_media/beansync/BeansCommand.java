package org.asylum_media.beansync;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class BeansCommand implements CommandExecutor {

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        boolean silent = isSilent(args);

        // ---------- CONSOLE-SAFE: /beans takeIf <player> <amount> <command...> ----------
        // Must be handled BEFORE we enforce "players only", because CommandPanels uses console.
        if (args.length > 0 && args[0].equalsIgnoreCase("takeif")) {
            return handleTakeIf(sender, args, silent);
        }

        // Everything else remains player-facing
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players (except /beans takeif).");
            return true;
        }

        Objective objective = getBeansObjective();
        if (objective == null) {
            if (!silent) player.sendMessage("§cBeans objective does not exist.");
            return true;
        }

        int balance = getBeans(player, objective);

        // /beans OR /beans get
        if (args.length == 0 || args[0].equalsIgnoreCase("get")) {
            if (!silent) player.sendMessage("§6Beans§7: §e" + balance);
            return true;
        }

        // /beans has <amount> [silent]
        if (args[0].equalsIgnoreCase("has")) {
            Integer amount = parseAmount(sender, args, silent, 1);
            if (amount == null) return false;

            boolean hasEnough = balance >= amount;
            if (!silent) {
                if (hasEnough) player.sendMessage("§aYes§7 — you have at least §e" + amount + " Beans§7.");
                else player.sendMessage("§cNo§7 — you only have §e" + balance + " Beans§7.");
            }
            return hasEnough;
        }

        // /beans take <amount> [silent]  (permission gated)
        if (args[0].equalsIgnoreCase("take")) {
            if (!hasPermission(player, "beans.take")) {
                if (!silent) player.sendMessage("§cYou do not have permission to take Beans.");
                return true;
            }

            Integer amount = parseAmount(sender, args, silent, 1);
            if (amount == null) return false;

            if (balance < amount) {
                if (!silent) {
                    player.sendMessage("§cYou do not have enough Beans.");
                    player.sendMessage("§7Balance: §e" + balance);
                }
                return false;
            }

            setBeans(player, objective, balance - amount);

            if (!silent) {
                player.sendMessage("§aRemoved §e" + amount + " Beans§a.");
                player.sendMessage("§7New balance: §e" + (balance - amount));
            }
            return true;
        }

        // /beans give <amount> [silent]  (permission gated)
        if (args[0].equalsIgnoreCase("give")) {
            if (!hasPermission(player, "beans.give")) {
                if (!silent) player.sendMessage("§cYou do not have permission to give Beans.");
                return true;
            }

            Integer amount = parseAmount(sender, args, silent, 1);
            if (amount == null) return false;

            setBeans(player, objective, balance + amount);

            if (!silent) {
                player.sendMessage("§aAdded §e" + amount + " Beans§a.");
                player.sendMessage("§7New balance: §e" + (balance + amount));
            }
            return true;
        }

        if (!silent) {
            player.sendMessage("§cUnknown subcommand.");
            player.sendMessage("§7Usage:");
            player.sendMessage("§7/beans [get]");
            player.sendMessage("§7/beans has <amount> [silent]");
            player.sendMessage("§7/beans take <amount> [silent]");
            player.sendMessage("§7/beans give <amount> [silent]");
            player.sendMessage("§7/beans takeif <player> <amount> <command...> [silent]");
        }
        return true;
    }

    // ===================== takeif implementation =====================

    private boolean handleTakeIf(CommandSender sender, String[] args, boolean silent) {
        // args: takeif <player> <amount> <command...> [silent]
        if (args.length < 4) {
            if (!silent) sender.sendMessage("Usage: /beans takeif <player> <amount> <command...> [silent]");
            return true;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            if (!silent) sender.sendMessage("Player not found (must be online): " + targetName);
            return true;
        }

        Integer amount = parseAmount(sender, args, silent, 2);
        if (amount == null) return false;

        Objective objective = getBeansObjective();
        if (objective == null) {
            if (!silent) sender.sendMessage("Beans objective does not exist.");
            return true;
        }

        int balance = getBeans(target, objective);
        if (balance < amount) {
            // purchase fails; optional feedback to player if NOT silent
            if (!silent) target.sendMessage("§cYou do not have enough Beans for that.");
            return false;
        }

        // Deduct first (atomic-ish)
        setBeans(target, objective, balance - amount);

        // Build command: join args[3..] excluding trailing "silent" tokens
        StringBuilder sb = new StringBuilder();
        for (int i = 3; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("silent")) continue;
            sb.append(args[i]).append(' ');
        }
        String cmd = sb.toString().trim();

        if (cmd.isEmpty()) {
            if (!silent) sender.sendMessage("No command provided to run.");
            return false;
        }

        // Run as console
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        return true;
    }

    // ===================== helpers =====================

    private Objective getBeansObjective() {
        String objectiveName = Beansync.getInstance().getBeansObjectiveName();
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        return scoreboard.getObjective(objectiveName);
    }

    private int getBeans(Player player, Objective objective) {
        return objective.getScore(player.getName()).getScore();
    }

    private void setBeans(Player player, Objective objective, int value) {
        objective.getScore(player.getName()).setScore(Math.max(0, value));
    }

    private boolean isSilent(String[] args) {
        return Arrays.stream(args).anyMatch(a -> a.equalsIgnoreCase("silent"));
    }

    private boolean hasPermission(Player player, String node) {
        return player.hasPermission("beans.admin") || player.hasPermission(node);
    }

    /**
     * Parses an integer amount from args[index].
     */
    private Integer parseAmount(CommandSender sender, String[] args, boolean silent, int index) {
        if (args.length <= index) {
            if (!silent) sender.sendMessage("Amount required.");
            return null;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[index]);
        } catch (NumberFormatException e) {
            if (!silent) sender.sendMessage("Amount must be a number.");
            return null;
        }

        if (amount <= 0) {
            if (!silent) sender.sendMessage("Amount must be greater than zero.");
            return null;
        }

        return amount;
    }
}
