package pl.nollis.connector.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.nollis.connector.NollisPluginsConnector;

import java.util.List;
import java.util.UUID;

public class GameModeGUIManager {

    private final NollisPluginsConnector plugin;

    public GameModeGUIManager(NollisPluginsConnector plugin) {
        this.plugin = plugin;
    }

    /**
     * Open the game mode selection GUI for a player
     * @param player Player to open GUI for
     */
    public void openGameModeGUI(Player player) {
        String title = plugin.getConfig().getString("gui.title", "§6§lGame Mode Selector");
        int size = plugin.getConfig().getInt("gui.size", 9);

        Inventory gui = Bukkit.createInventory(null, size, title);

        // Fill with background glass
        fillWithBackground(gui);

        // Add game mode items
        addGameModeItems(gui);

        player.openInventory(gui);
    }

    /**
     * Fill GUI with background glass panes
     * @param gui Inventory to fill
     */
    private void fillWithBackground(Inventory gui) {
        String materialName = plugin.getConfig().getString("gui.filler-material", "BLACK_STAINED_GLASS_PANE");
        String fillerName = plugin.getConfig().getString("gui.filler-name", " ");

        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid filler material '" + materialName + "'! Using BLACK_STAINED_GLASS_PANE.");
            material = Material.BLACK_STAINED_GLASS_PANE;
        }

        ItemStack filler = new ItemStack(material);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(fillerName);
            filler.setItemMeta(meta);
        }

        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, filler);
        }
    }

    /**
     * Add game mode items to GUI based on config
     * @param gui Inventory to add items to
     */
    private void addGameModeItems(Inventory gui) {
        ConfigurationSection gameModes = plugin.getConfig().getConfigurationSection("game-modes");
        if (gameModes == null) {
            plugin.getLogger().warning("No game modes configured!");
            return;
        }

        for (String gameMode : gameModes.getKeys(false)) {
            ConfigurationSection modeSection = gameModes.getConfigurationSection(gameMode);
            if (modeSection == null || !modeSection.getBoolean("enabled", false)) {
                continue;
            }

            int slot = modeSection.getInt("slot", 4);
            ItemStack item = createGameModeItem(modeSection);

            if (slot >= 0 && slot < gui.getSize()) {
                gui.setItem(slot, item);
            }
        }
    }

    /**
     * Create an item for a game mode
     * @param section Configuration section for the game mode
     * @return ItemStack for the game mode
     */
    private ItemStack createGameModeItem(ConfigurationSection section) {
        String materialName = section.getString("material", "BEDROCK");
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material '" + materialName + "'! Using BEDROCK.");
            material = Material.BEDROCK;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String displayName = section.getString("display-name", "§6§lUnknown");
            meta.setDisplayName(displayName);

            List<String> lore = section.getStringList("lore");
            if (!lore.isEmpty()) {
                meta.setLore(lore);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Handle click on game mode item
     * @param player Player who clicked
     * @param item ItemStack that was clicked
     */
    public void handleGameModeClick(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        String displayName = item.getItemMeta().getDisplayName();

        // Find matching game mode in config
        ConfigurationSection gameModes = plugin.getConfig().getConfigurationSection("game-modes");
        if (gameModes == null) {
            return;
        }

        for (String gameMode : gameModes.getKeys(false)) {
            ConfigurationSection modeSection = gameModes.getConfigurationSection(gameMode);
            if (modeSection == null || !modeSection.getBoolean("enabled", false)) {
                continue;
            }

            String modeDisplayName = modeSection.getString("display-name", "");
            if (modeDisplayName.equals(displayName)) {
                teleportToGameMode(player, gameMode, modeSection);
                return;
            }
        }
    }

    /**
     * Teleport player to a game mode
     * @param player Player to teleport
     * @param gameModeName Name of the game mode
     * @param modeSection Configuration section of the game mode
     */
    private void teleportToGameMode(Player player, String gameModeName, ConfigurationSection modeSection) {
        player.closeInventory();

        // Check if player is in a party
        if (plugin.getPartyAPI().isInParty(player)) {
            // Only leader can choose game mode for party
            if (!plugin.getPartyAPI().isPartyLeader(player)) {
                player.sendMessage("§cOnly the party leader can choose game modes!");
                return;
            }

            // Teleport entire party
            for (UUID memberUUID : plugin.getPartyAPI().getPartyMembers(player)) {
                Player member = Bukkit.getPlayer(memberUUID);
                if (member != null && member.isOnline()) {
                    member.closeInventory();
                    if (gameModeName.equalsIgnoreCase("towerpvp")) {
                        teleportToTowerPvP(member, modeSection);
                    } else {
                        teleportToGenericGameMode(member, gameModeName, modeSection);
                    }
                }
            }

            String gameModeDisplay = modeSection.getString("display-name", gameModeName);
            plugin.getPartyAPI().notifyPartyMembers(plugin.getPartyAPI().getParty(player),
                "§aYour party is joining " + gameModeDisplay + "§a!");
            return;
        }

        // Solo player - teleport normally
        // Handle TowerPvP specifically
        if (gameModeName.equalsIgnoreCase("towerpvp")) {
            teleportToTowerPvP(player, modeSection);
            return;
        }

        teleportToGenericGameMode(player, gameModeName, modeSection);
    }

    /**
     * Teleport player to a generic game mode
     */
    private void teleportToGenericGameMode(Player player, String gameModeName, ConfigurationSection modeSection) {
        // Check if game mode uses command instead of teleportation
        boolean useCommand = modeSection.getBoolean("use-command", false);
        if (useCommand) {
            String command = modeSection.getString("command");
            if (command == null || command.isEmpty()) {
                player.sendMessage("§cGame mode configuration error!");
                return;
            }

            // Execute command as player
            player.performCommand(command);
            return;
        }

        // Generic teleportation for other game modes
        String worldName = modeSection.getString("target-world");
        ConfigurationSection coords = modeSection.getConfigurationSection("target-coords");

        if (worldName == null || coords == null) {
            player.sendMessage("§cGame mode configuration error!");
            return;
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage("§cWorld '" + worldName + "' not found!");
            return;
        }

        double x = coords.getDouble("x", 0.5);
        double y = coords.getDouble("y", 100.0);
        double z = coords.getDouble("z", 0.5);
        float yaw = (float) coords.getDouble("yaw", 0.0);
        float pitch = (float) coords.getDouble("pitch", 0.0);

        Location location = new Location(world, x, y, z, yaw, pitch);
        player.teleport(location);

        String message = plugin.getConfig().getString("messages.teleporting-to-gamemode", "§aTeleporting to %gamemode%...");
        message = message.replace("%gamemode%", modeSection.getString("display-name", gameModeName));
        player.sendMessage(message);
    }

    /**
     * Teleport player to TowerPvP with special handling
     * @param player Player to teleport
     * @param modeSection Configuration section of TowerPvP
     */
    private void teleportToTowerPvP(Player player, ConfigurationSection modeSection) {
        // Check if TowerPvP plugin is available
        if (!Bukkit.getPluginManager().isPluginEnabled("TowerPvP")) {
            String message = plugin.getConfig().getString("messages.plugin-not-available", "§c%plugin% is not available!");
            message = message.replace("%plugin%", "TowerPvP");
            player.sendMessage(message);
            return;
        }

        try {
            // Use TowerPvP API to remove from game if in game
            Class<?> towerPvPClass = Class.forName("pl.towerpvp.TowerPvP");
            Object towerPvPInstance = towerPvPClass.getMethod("getInstance").invoke(null);
            Object api = towerPvPClass.getMethod("getAPI").invoke(towerPvPInstance);

            Class<?> apiClass = Class.forName("pl.towerpvp.api.TowerPvPAPI");
            boolean isInGame = (boolean) apiClass.getMethod("isPlayerInGame", Player.class).invoke(api, player);

            if (isInGame) {
                player.sendMessage("§eLeaving Tower PvP game...");
                apiClass.getMethod("teleportToLobby", Player.class).invoke(api, player);
            }

            // Teleport to towerpvp_spawn
            String worldName = modeSection.getString("target-world", "towerpvp_spawn");
            ConfigurationSection coords = modeSection.getConfigurationSection("target-coords");

            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                player.sendMessage("§cWorld '" + worldName + "' not found!");
                return;
            }

            double x = coords.getDouble("x", 0.5);
            double y = coords.getDouble("y", 100.0);
            double z = coords.getDouble("z", 0.5);
            float yaw = (float) coords.getDouble("yaw", 0.0);
            float pitch = (float) coords.getDouble("pitch", 0.0);

            Location location = new Location(world, x, y, z, yaw, pitch);
            player.teleport(location);

            // Clear inventory (remove compass and other lobby items)
            player.getInventory().clear();

            // Give TowerPvP lobby items
            if (modeSection.getBoolean("give-lobby-items", true)) {
                Object lobbyItemManager = towerPvPClass.getMethod("getLobbyItemManager").invoke(towerPvPInstance);
                lobbyItemManager.getClass().getMethod("giveLobbyItems", Player.class).invoke(lobbyItemManager, player);
            }

            player.sendMessage("§aTeleporting to Tower PvP...");

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to interact with TowerPvP API: " + e.getMessage());
            player.sendMessage("§cFailed to teleport to Tower PvP!");
        }
    }

    /**
     * Check if the inventory title matches the game mode GUI
     * @param title Inventory title to check
     * @return true if it matches
     */
    public boolean isGameModeGUI(String title) {
        String guiTitle = plugin.getConfig().getString("gui.title", "§6§lGame Mode Selector");
        return title.equals(guiTitle);
    }
}
