package pl.nollis.connector.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.nollis.connector.NollisPluginsConnector;

public class SpawnCommand implements CommandExecutor {

    private final NollisPluginsConnector plugin;

    public SpawnCommand(NollisPluginsConnector plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        // Remove player from all active games
        removeFromAllGames(player);

        // Teleport to spawn
        Location spawnLocation = plugin.getSpawnManager().getSpawnLocation();
        player.teleport(spawnLocation);

        // Give lobby items (compass)
        plugin.getLobbyItemManager().giveLobbyItems(player);

        // Send message
        String message = plugin.getConfig().getString("messages.teleporting-to-spawn", "§aTeleporting to spawn...");
        player.sendMessage(message);

        return true;
    }

    /**
     * Remove player from all active games (TowerPvP, CaveWars, etc.)
     * @param player Player to remove from games
     */
    private void removeFromAllGames(Player player) {
        // Remove from TowerPvP
        if (Bukkit.getPluginManager().isPluginEnabled("TowerPvP")) {
            removeFromTowerPvP(player);
        }

        // Future: Remove from CaveWars
        // if (Bukkit.getPluginManager().isPluginEnabled("CaveWars")) {
        //     removeFromCaveWars(player);
        // }
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
            // TowerPvP API not available or error occurred
            plugin.getLogger().fine("Could not remove player from TowerPvP: " + e.getMessage());
        }
    }
}
