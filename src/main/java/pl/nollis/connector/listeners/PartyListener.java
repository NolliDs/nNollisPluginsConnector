package pl.nollis.connector.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.nollis.connector.NollisPluginsConnector;

public class PartyListener implements Listener {

    private final NollisPluginsConnector plugin;

    public PartyListener(NollisPluginsConnector plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Remove player from party if in one
        plugin.getPartyManager().removePlayer(player);
    }
}
