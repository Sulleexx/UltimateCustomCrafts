package sullexxx.ultimatecustomcrafts;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import jdk.internal.jmod.JmodFile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.intellij.lang.annotations.Language;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class RecipesReader {

    private final JavaPlugin plugin;

    public RecipesReader(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void readRecipes() throws IOException {
        File folder = new File(plugin.getDataFolder(), "recipes");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        Yaml yaml = new Yaml();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (files == null) {
            return;
        }

        for (File file : files) {
            loadRecipeFromFile(file.getName());
        }
    }

    public void loadRecipeFromFile(String fileName) throws IOException {
        File file = new File(plugin.getDataFolder(), "recipes/" + fileName);
        if (!file.exists()) {
            return;
        }

        Yaml yaml = new Yaml();
        try (FileInputStream fis = new FileInputStream(file)) {
            Map<String, Object> data = yaml.load(fis);

            if (data == null) {
                plugin.getLogger().log(Level.WARNING, "File {0} is empty or malformed.", fileName);
                return;
            }

            if (data.get("Enabled") != null && (Boolean) data.get("Enabled")) {
                createRecipe(data, fileName);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error loading recipe from file: " + fileName, e);
        }
    }

    private void createRecipe(Map<String, Object> data, String fileName) {
        List<String> ingredients = (List<String>) data.get("Ingredients");

        Map<String, Object> resultItemMap = (Map<String, Object>) data.get("ResultItem");
        if (resultItemMap == null) {
            plugin.getLogger().log(Level.SEVERE, "ResultItem map is missing in recipe file: " + fileName);
            return;
        }

        String itemKey = (String) resultItemMap.get("Item");
        if (itemKey == null || itemKey.isEmpty()) {
            plugin.getLogger().log(Level.SEVERE, "Item key is missing in ResultItem in recipe file: " + fileName);
            return;
        }

        Map<String, Object> resultItemData = (Map<String, Object>) data.get(itemKey);
        if (resultItemData == null) {
            plugin.getLogger().log(Level.SEVERE, "Item configuration for key " + itemKey + " is missing in recipe file: " + fileName);
            return;
        }

        Integer resultAmount = (Integer) resultItemMap.get("Amount");
        if (resultAmount == null) {
            plugin.getLogger().log(Level.SEVERE, "Amount is missing in ResultItem in recipe file: " + fileName);
            return;
        }

        ItemStack resultItem = createItem(resultItemData, resultAmount);
        if (resultItem == null) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create result item for recipe file: " + fileName);
            return;
        }

        NamespacedKey key = new NamespacedKey(plugin, fileName.toLowerCase());
        ShapedRecipe recipe = new ShapedRecipe(key, resultItem);

        Map<String, Character> ingredientMap = new HashMap<>();
        char ingredientChar = 'a';

        String[] shape = new String[3];
        for (int i = 0; i < ingredients.size() && i < 3; i++) {
            StringBuilder row = new StringBuilder();
            for (String itemString : ingredients.get(i).split(" ")) {
                if (!ingredientMap.containsKey(itemString)) {
                    ingredientMap.put(itemString, ingredientChar);
                    ingredientChar++;
                }
                row.append(ingredientMap.get(itemString));
            }
            shape[i] = row.toString();
        }

        recipe.shape(shape[0], shape[1], shape[2]);

        for (Map.Entry<String, Character> entry : ingredientMap.entrySet()) {
            String materialName = entry.getKey();
            Character character = entry.getValue();

            Material material;
            try {
                material = Material.valueOf(materialName);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.WARNING, "Invalid material: " + materialName, e);
                continue;
            }

            recipe.setIngredient(character, material);
        }

        Bukkit.addRecipe(recipe);
        UltimateCustomCrafts.getInstance().addLoadedRecipe(key);
    }

    private ItemStack createItem(Map<String, Object> itemData, int amount) {
        if (!itemData.containsKey("Material")) {
            plugin.getLogger().log(Level.SEVERE, "Missing Material in item data: " + itemData);
            return null;
        }

        Material material;
        try {
            material = Material.valueOf(itemData.get("Material").toString());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.SEVERE, "Invalid Material: " + itemData.get("Material"), e);
            return null;
        }

        ItemStack item = new ItemStack(material, amount);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (itemData.containsKey("DisplayName")) {
                meta.displayName(LanguageConfig.getFormattedStringE(itemData.get("DisplayName").toString()));
            }

            if (itemData.containsKey("Lore")) {
                List<String> loreLines = (List<String>) itemData.get("Lore");
                List<Component> formattedLore = new ArrayList<>();
                for (String line : loreLines) {
                    formattedLore.add(LanguageConfig.getFormattedStringE(line));
                }

                List<String> plainTextLore = formattedLore.stream()
                        .map(component -> LegacyComponentSerializer.legacySection().serialize(component))
                        .collect(Collectors.toList());

                meta.setLore(plainTextLore);
            }

            if (itemData.containsKey("CustomModelData")) {
                try {
                    meta.setCustomModelData(Integer.parseInt(itemData.get("CustomModelData").toString()));
                } catch (NumberFormatException e) {
                    plugin.getLogger().log(Level.WARNING, "Invalid CustomModelData: " + itemData.get("CustomModelData"), e);
                }
            }

            if (itemData.containsKey("Enchantments")) {
                List<String> enchantments = (List<String>) itemData.get("Enchantments");
                for (String enchant : enchantments) {
                    String[] parts = enchant.split(" ");
                    if (parts.length != 2) {
                        plugin.getLogger().log(Level.WARNING, "Invalid enchantment format: " + enchant);
                        continue;
                    }

                    Enchantment enchantment = Enchantment.getByName(parts[0].toUpperCase());
                    if (enchantment == null) {
                        plugin.getLogger().log(Level.WARNING, "Invalid enchantment name: " + parts[0]);
                        continue;
                    }

                    int level;
                    try {
                        level = Integer.parseInt(parts[1].replace("[", "").replace("]", ""));
                    } catch (NumberFormatException e) {
                        plugin.getLogger().log(Level.WARNING, "Invalid enchantment level: " + parts[1], e);
                        continue;
                    }

                    meta.addEnchant(enchantment, level, true);
                }
            }

            item.setItemMeta(meta);

            if (itemData.containsKey("NBTTags")) {
                Map<String, Object> tags = (Map<String, Object>) itemData.get("NBTTags");
                for (String nbt : tags.keySet()) {
                    Object value = tags.get(nbt);
                    item = NBTEditor.set(item, value, nbt);
                }
            }
        }
        return item;
    }
}
