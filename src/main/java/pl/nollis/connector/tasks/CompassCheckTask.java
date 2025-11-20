package pl.nollis.connector.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import pl.nollis.connector.NollisPluginsConnector;

public class CompassCheckTask extends BukkitRunnable {

    private final NollisPluginsConnector plugin;

    public CompassCheckTask(NollisPluginsConnector plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // Check all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Only check players in spawn world
            if (!plugin.getSpawnManager().isSpawnWorld(player.getWorld())) {
                continue;
            }

            // Check if player has the compass on slot 4
            ItemStack slot4Item = player.getInventory().getItem(4);

            boolean hasCompass = false;
            if (slot4Item != null && plugin.getLobbyItemManager().isGameSelectorItem(slot4Item)) {
                hasCompass = true;
            }

            // If player doesn't have compass, give it
            if (!hasCompass) {
                plugin.getLobbyItemManager().giveLobbyItems(player);
                plugin.getLogger().fine("Gave compass to " + player.getName() + " (was missing in spawn world)");
            }
        }
    }
}
