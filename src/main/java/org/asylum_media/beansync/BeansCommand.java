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

    private static final String OBJECTIVE_NAME = "Beans";

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        boolean silent = isSilent(args);

        Objective objective = getBeansObjective();
        if (objective == null) {
            if (!silent) {
                player.sendMessage("§cBeans objective does not exist.");
            }
            return true;
        }

        int balance = getBeans(player, objective);

        // /beans OR /beans get
        if (args.length == 0 || args[0].equalsIgnoreCase("get")) {
            if (!silent) {
                player.sendMessage("§6Beans§7: §e" + balance);
            }
            return true;
        }

        // /beans has <amount> [silent]
        if (args[0].equalsIgnoreCase("has")) {
            Integer amount = parseAmount(player, args, silent);
            if (amount == null) return false;

            boolean hasEnough = balance >= amount;

            if (!silent) {
                if (hasEnough) {
                    player.sendMessage("§aYes§7 — you have at least §e" + amount + " Beans§7.");
                } else {
                    player.sendMessage("§cNo§7 — you only have §e" + balance + " Beans§7.");
                }
            }

            return hasEnough;
        }

        // /beans take <amount> [silent]
        if (args[0].equalsIgnoreCase("take")) {

            if (!hasPermission(player, "beans.take")) {
                if (!silent) {
                    player.sendMessage("§cYou do not have permission to take Beans.");
                }
                return true;
            }

            Integer amount = parseAmount(player, args, silent);
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

        // /beans give <amount> [silent]
        if (args[0].equalsIgnoreCase("give")) {

            if (!hasPermission(player, "beans.give")) {
                if (!silent) {
                    player.sendMessage("§cYou do not have permission to give Beans.");
                }
                return true;
            }

            Integer amount = parseAmount(player, args, silent);
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
            player.sendMessage("§7Usage: /beans [get|has|take|give] <amount> [silent]");
        }

        return true;
    }

    /* =======================
       Helpers
       ======================= */

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
        return Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("silent"));
    }

    private boolean hasPermission(Player player, String node) {
        return player.hasPermission("beans.admin") || player.hasPermission(node);
    }

    private Integer parseAmount(Player player, String[] args, boolean silent) {
        if (args.length < 2) {
            if (!silent) {
                player.sendMessage("§cAmount required.");
            }
            return null;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            if (!silent) {
                player.sendMessage("§cAmount must be a number.");
            }
            return null;
        }

        if (amount <= 0) {
            if (!silent) {
                player.sendMessage("§cAmount must be greater than zero.");
            }
            return null;
        }

        return amount;
    }
}
