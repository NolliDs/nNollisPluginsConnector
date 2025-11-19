package pl.nollis.connector.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.nollis.connector.NollisPluginsConnector;

public class PlayerJoinListener implements Listener {

    private final NollisPluginsConnector plugin;

    public PlayerJoinListener(NollisPluginsConnector plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Teleport to spawn
        Location spawnLocation = plugin.getSpawnManager().getSpawnLocation();
        player.teleport(spawnLocation);

        // Give lobby items (compass on slot 4)
        plugin.getLobbyItemManager().giveLobbyItems(player);
    }
}