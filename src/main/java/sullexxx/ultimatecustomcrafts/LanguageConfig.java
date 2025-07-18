package sullexxx.ultimatecustomcrafts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

public class LanguageConfig {
    public static FileConfiguration lang;
    private static final FileConfiguration config = UltimateCustomCrafts.getInstance().getConfig();
    public static final LegacyComponentSerializer unusualHexSerializer = LegacyComponentSerializer.builder().hexColors().useUnusualXRepeatedCharacterHexFormat().build();

    public static void loadLanguage() {
        String selectedLang = config.getString("General.Language", "en_US");
        File folder = new File(UltimateCustomCrafts.getInstance().getDataFolder(), "lang");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        UltimateCustomCrafts.getInstance().saveResource("lang/" + selectedLang + ".yml", false);
        File file = new File(UltimateCustomCrafts.getInstance().getDataFolder(), "lang/" + selectedLang + ".yml");

        if (!file.exists()) {
            try {
                UltimateCustomCrafts.getInstance().saveResource("lang/" + selectedLang + ".yml", false);
            } catch (Exception e) {
                System.out.println("Lang file doesn't exist: " + selectedLang + ". Using English language.");
                selectedLang = "en_US";
                File en_Us_DEF = new File(UltimateCustomCrafts.getInstance().getDataFolder(), "lang/en_US.yml");
                if (!en_Us_DEF.exists()) {
                    UltimateCustomCrafts.getInstance().saveResource("lang/en_US.yml", false);
                }
            }
        }

        lang = YamlConfiguration.loadConfiguration(new File(
                UltimateCustomCrafts.getInstance().getDataFolder(), "lang/" + selectedLang + ".yml"));
    }

    public static Component getFormattedString(String path) {
        String rawString = lang.getString(path, "undefined");
        if (rawString.contains("{prefix}")) {
            rawString.replace("{prefix}", config.getString("General.Plugin-Prefix"));
        }
        return doubleFormat(rawString);
    }

    public static Component getFormattedStringE(String text) {
        String rawString = text;
        return doubleFormat(rawString);
    }


    public static Component getFormattedString(String path, String player) {
        String rawString = lang.getString(path, "undefined");
        if (rawString.contains("{prefix}")) {
            rawString = rawString.replace("{prefix}", config.getString("General.Plugin-Prefix"));
        }
        if (rawString.contains("{player}")) {
            rawString = rawString.replace("{player}", player);
        }
        return doubleFormat(rawString);
    }




    public static Component getFormattedString(String path, String player, String item) {
        String rawString = lang.getString(path, "undefined");
        if (rawString.contains("{prefix}")) {
            rawString.replace("{prefix}", Objects.requireNonNull(config.getString("General.Plugin-Prefix")));
        }
        if (rawString.contains("{player}")) {
            rawString.replace("{player}", player);
        }
        if (rawString.contains("{item}")) {
            rawString.replace("{item}", item);
        }
        return doubleFormat(rawString);
    }

    @NotNull
    public static Component doubleFormat(@NotNull String message) {
        message = message.replace('§', '&');
        Component component = MiniMessage.miniMessage().deserialize(message).decoration(TextDecoration.ITALIC, false);
        String legacyMessage = toLegacy(component);
        legacyMessage = ChatColor.translateAlternateColorCodes('&', legacyMessage);
        return unusualHexSerializer.deserialize(legacyMessage);
    }

    public static String toLegacy(Component component) {
        return unusualHexSerializer.serialize(component);
    }

}
