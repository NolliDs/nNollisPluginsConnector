package pl.nollis.connector.managers;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.nollis.connector.NollisPluginsConnector;

import java.util.List;

public class LobbyItemManager {

    private final NollisPluginsConnector plugin;

    public LobbyItemManager(NollisPluginsConnector plugin) {
        this.plugin = plugin;
    }

    /**
     * Give lobby items to player (compass on slot 4)
     * @param player Player to give items to
     */
    public void giveLobbyItems(Player player) {
        ConfigurationSection itemSection = plugin.getConfig().getConfigurationSection("lobby-items.game-selector");

        if (itemSection == null || !itemSection.getBoolean("enabled", true)) {
            return;
        }

        int slot = itemSection.getInt("slot", 4);
        ItemStack compass = createGameSelectorItem();

        player.getInventory().setItem(slot, compass);
    }

    /**
     * Create the game selector compass item
     * @return ItemStack of compass
     */
    public ItemStack createGameSelectorItem() {
        ConfigurationSection itemSection = plugin.getConfig().getConfigurationSection("lobby-items.game-selector");

        if (itemSection == null) {
            return createDefaultGameSelector();
        }

        String materialName = itemSection.getString("material", "COMPASS");
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material '" + materialName + "' in config! Using COMPASS.");
            material = Material.COMPASS;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Set name
            String name = itemSection.getString("name", "§6§lGame Mode Selector");
            meta.setDisplayName(name);

            // Set lore
            List<String> lore = itemSection.getStringList("lore");
            if (lore.isEmpty()) {
                lore = List.of(
                        "§7Click to choose a game mode!",
                        "",
                        "§eRight click to open menu"
                );
            }
            meta.setLore(lore);

            // Set unbreakable
            if (itemSection.getBoolean("unbreakable", true)) {
                meta.setUnbreakable(true);
            }

            // Hide attributes
            if (itemSection.getBoolean("hide-attributes", true)) {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Create default game selector if config is missing
     * @return Default compass item
     */
    private ItemStack createDefaultGameSelector() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§6§lGame Mode Selector");
            meta.setLore(List.of(
                    "§7Click to choose a game mode!",
                    "",
                    "§eRight click to open menu"
            ));
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            compass.setItemMeta(meta);
        }

        return compass;
    }

    /**
     * Check if item is the game selector compass
     * @param item ItemStack to check
     * @return true if it's the game selector
     */
    public boolean isGameSelectorItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }

        String expectedName = plugin.getConfig().getString("lobby-items.game-selector.name", "§6§lGame Mode Selector");
        return meta.getDisplayName().equals(expectedName);
    }
}