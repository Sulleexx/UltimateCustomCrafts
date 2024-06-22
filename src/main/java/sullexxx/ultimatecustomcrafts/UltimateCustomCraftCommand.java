package sullexxx.ultimatecustomcrafts;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UltimateCustomCraftCommand implements CommandExecutor, TabCompleter {
    private final UltimateCustomCrafts plugin;

    public UltimateCustomCraftCommand(UltimateCustomCrafts plugin) {
        this.plugin = plugin;
        plugin.getCommand("ultimatecustomcrafts").setExecutor(this);
        plugin.getCommand("ultimatecustomcrafts").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(LanguageConfig.getFormattedString("NotPlayer"));
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission(UltimateCustomCrafts.getInstance().getConfig().getString("General.Permission-Admin"))) {
            player.sendMessage(LanguageConfig.getFormattedString("NoPermission"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(LanguageConfig.getFormattedString("UnknownCommand"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(player);
                break;
            case "unload":
                if (args.length == 2) {
                    handleUnload(player, args[1]);
                } else {
                    player.sendMessage(LanguageConfig.getFormattedString("InvalidArguments"));
                }
                break;
            case "load":
                if (args.length == 2) {
                    handleLoad(player, args[1]);
                } else {
                    player.sendMessage(LanguageConfig.getFormattedString("InvalidArguments"));
                }
                break;
            default:
                player.sendMessage(LanguageConfig.getFormattedString("UnknownCommand"));
                break;
        }

        return true;
    }

    private void handleReload(Player player) {
        plugin.reloadConfig();
        player.sendMessage(LanguageConfig.getFormattedString("SuccessfulReload"));
    }

    private void handleUnload(Player player, String file) {
        plugin.unloadRecipe(file);
        player.sendMessage(LanguageConfig.getFormattedString("SuccessfulUnloadRecipe", file));
    }

    private void handleLoad(Player player, String file) {
        try {
            plugin.loadRecipe(file);
            player.sendMessage(LanguageConfig.getFormattedString("SuccessfulLoadRecipe", file));
        } catch (IOException e) {
            player.sendMessage(LanguageConfig.getFormattedString("FailedLoadRecipe", file));
            e.printStackTrace();
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("reload");
            completions.add("unload");
            completions.add("load");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("unload") || args[0].equalsIgnoreCase("load"))) {
            File folder = new File(plugin.getDataFolder(), "recipes");
            if (folder.exists() && folder.isDirectory()) {
                String[] files = folder.list((dir, name) -> name.endsWith(".yml"));
                if (files != null) {
                    completions.addAll(Arrays.asList(files));
                }
            }
        }
        return completions;
    }
}
