package pl.nollis.connector.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import pl.nollis.connector.NollisPluginsConnector;

public class SecureLoginListener implements Listener {

    private final NollisPluginsConnector plugin;

    public SecureLoginListener(NollisPluginsConnector plugin) {
        this.plugin = plugin;
    }

    /**
     * Register this listener dynamically to listen for SecureLogin's PlayerLoginEvent
     */
    public void register() {
        try {
            // Get SecureLogin's PlayerLoginEvent class
            Class<?> loginEventClass = Class.forName("pl.nollis.securelogin.events.PlayerLoginEvent");

            // Create event executor
            EventExecutor executor = (listener, event) -> {
                if (loginEventClass.isInstance(event)) {
                    handleLoginEvent((Event) event);
                }
            };

            // Register the event
            Bukkit.getPluginManager().registerEvent(
                    (Class<? extends Event>) loginEventClass,
                    this,
                    EventPriority.MONITOR,
                    executor,
                    plugin,
                    false
            );

            plugin.getLogger().info("Successfully registered SecureLogin listener!");

        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("Could not find SecureLogin PlayerLoginEvent class: " + e.getMessage());
        }
    }

    /**
     * Handle SecureLogin login event
     */
    private void handleLoginEvent(Event event) {
        try {
            // Use reflection to get the player from the event
            Player player = (Player) event.getClass().getMethod("getPlayer").invoke(event);

            plugin.getLogger().info("Player " + player.getName() + " logged in through SecureLogin");

            // Teleport to spawn after successful login
            Location spawnLocation = plugin.getSpawnManager().getSpawnLocation();
            player.teleport(spawnLocation);

            // Give lobby items (compass on slot 4)
            plugin.getLobbyItemManager().giveLobbyItems(player);

            plugin.getLogger().info("Teleported " + player.getName() + " to spawn and gave lobby items");

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to handle SecureLogin event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}