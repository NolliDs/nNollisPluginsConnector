package pl.nollis.connector.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import pl.nollis.connector.NollisPluginsConnector;

public class SecureLoginListener implements Listener {

    private final NollisPluginsConnector plugin;

    public SecureLoginListener(NollisPluginsConnector plugin) {
        this.plugin = plugin;
    }

    /**
     * Listen for SecureLogin's PlayerLoginEvent (when player successfully logs in)
     * This event is fired by SecureLogin plugin after player authentication
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onSecureLogin(org.bukkit.event.Event event) {
        // Check if this is SecureLogin's PlayerLoginEvent
        if (!event.getClass().getName().equals("pl.nollis.securelogin.events.PlayerLoginEvent")) {
            return;
        }

        try {
            // Use reflection to get the player from the event
            Player player = (Player) event.getClass().getMethod("getPlayer").invoke(event);

            // Teleport to spawn after successful login
            Location spawnLocation = plugin.getSpawnManager().getSpawnLocation();
            player.teleport(spawnLocation);

            // Give lobby items (compass on slot 4)
            plugin.getLobbyItemManager().giveLobbyItems(player);

            plugin.getLogger().fine("Player " + player.getName() + " logged in and teleported to spawn");

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to handle SecureLogin event: " + e.getMessage());
        }
    }
}
