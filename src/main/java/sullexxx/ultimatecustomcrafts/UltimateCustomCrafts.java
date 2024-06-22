package sullexxx.ultimatecustomcrafts;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class UltimateCustomCrafts extends JavaPlugin {

    private static UltimateCustomCrafts instance;
    private final List<NamespacedKey> loadedRecipes = new ArrayList<>();

    public static UltimateCustomCrafts getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reloadConfig();
        LanguageConfig.loadLanguage();
        if (!getConfig().getBoolean("General.Enable-plugin"))Bukkit.getPluginManager().disablePlugin(this);
        File file = new File(getDataFolder(), "recipes/LegendarySword.yml");
        if (!file.exists()) saveResource("recipes/LegendarySword.yml", false);
        RecipesReader recipesReader = new RecipesReader(this);
        try {
            recipesReader.readRecipes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new UltimateCustomCraftCommand(this);
    }

    public void loadRecipe(String fileName) throws IOException {
        RecipesReader recipesReader = new RecipesReader(this);
        recipesReader.loadRecipeFromFile(fileName);
    }

    public void unloadRecipe(String fileName) {
        NamespacedKey key = new NamespacedKey(this, fileName.toLowerCase());
        Iterator<Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) {
            Recipe recipe = it.next();
            if (recipe instanceof ShapedRecipe && ((ShapedRecipe) recipe).getKey().equals(key)) {
                it.remove();
                loadedRecipes.remove(key);
                break;
            }
        }
    }

    public List<NamespacedKey> getLoadedRecipes() {
        return loadedRecipes;
    }

    public void addLoadedRecipe(NamespacedKey key) {
        loadedRecipes.add(key);
    }
}
