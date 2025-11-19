package pl.nollis.connector.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import pl.nollis.connector.NollisPluginsConnector;

public class TowerPvPCommand implements CommandExecutor {

    private final NollisPluginsConnector plugin;

    public TowerPvPCommand(NollisPluginsConnector plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        // Check if TowerPvP plugin is available
        if (!Bukkit.getPluginManager().isPluginEnabled("TowerPvP")) {
            String message = plugin.getConfig().getString("messages.plugin-not-available", "§c%plugin% is not available!");
            message = message.replace("%plugin%", "TowerPvP");
            player.sendMessage(message);
            return true;
        }

        // Remove player from TowerPvP game if in game
        removeFromTowerPvP(player);

        // Teleport to towerpvp_spawn
        teleportToTowerPvP(player);

        return true;
    }

    /**
     * Remove player from TowerPvP game using API
     * @param player Player to remove
     */
    private void removeFromTowerPvP(Player player) {
        try {
            Class<?> towerPvPClass = Class.forName("pl.towerpvp.TowerPvP");
            Object towerPvPInstance = towerPvPClass.getMethod("getInstance").invoke(null);
            Object api = towerPvPClass.getMethod("getAPI").invoke(towerPvPInstance);

            Class<?> apiClass = Class.forName("pl.towerpvp.api.TowerPvPAPI");
            boolean isInGame = (boolean) apiClass.getMethod("isPlayerInGame", Player.class).invoke(api, player);

            if (isInGame) {
                player.sendMessage("§eLeaving Tower PvP game...");
                apiClass.getMethod("teleportToLobby", Player.class).invoke(api, player);
            }
        } catch (Exception e) {
            plugin.getLogger().fine("Could not remove player from TowerPvP: " + e.getMessage());
        }
    }

    /**
     * Teleport player to TowerPvP spawn
     * @param player Player to teleport
     */
    private void teleportToTowerPvP(Player player) {
        // Get TowerPvP configuration
        ConfigurationSection modeSection = plugin.getConfig().getConfigurationSection("game-modes.towerpvp");
        if (modeSection == null) {
            player.sendMessage("§cTowerPvP configuration not found!");
            return;
        }

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
            giveTowerPvPLobbyItems(player);
        }

        player.sendMessage("§aTeleporting to Tower PvP...");
    }

    /**
     * Give TowerPvP lobby items to player
     * @param player Player to give items to
     */
    private void giveTowerPvPLobbyItems(Player player) {
        try {
            Class<?> towerPvPClass = Class.forName("pl.towerpvp.TowerPvP");
            Object towerPvPInstance = towerPvPClass.getMethod("getInstance").invoke(null);
            Object lobbyItemManager = towerPvPClass.getMethod("getLobbyItemManager").invoke(towerPvPInstance);
            lobbyItemManager.getClass().getMethod("giveLobbyItems", Player.class).invoke(lobbyItemManager, player);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to give TowerPvP lobby items: " + e.getMessage());
        }
    }
}