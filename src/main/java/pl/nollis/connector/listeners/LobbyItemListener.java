package pl.nollis.connector.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import pl.nollis.connector.NollisPluginsConnector;

public class LobbyItemListener implements Listener {

    private final NollisPluginsConnector plugin;

    public LobbyItemListener(NollisPluginsConnector plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle player clicking the compass to open GUI
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Check if player right-clicked or left-clicked
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
            event.getAction() != Action.RIGHT_CLICK_BLOCK &&
            event.getAction() != Action.LEFT_CLICK_AIR &&
            event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        if (item == null) {
            return;
        }

        // Check if it's the game selector compass
        if (plugin.getLobbyItemManager().isGameSelectorItem(item)) {
            event.setCancelled(true);
            plugin.getGameModeGUIManager().openGameModeGUI(player);
        }
    }

    /**
     * Handle player clicking items in the GUI
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String title = event.getView().getTitle();

        // Check if it's our game mode GUI
        if (!plugin.getGameModeGUIManager().isGameModeGUI(title)) {
            return;
        }

        // Cancel the event to prevent taking items
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        // Ignore filler items (glass panes)
        if (clicked.getType() == Material.BLACK_STAINED_GLASS_PANE) {
            return;
        }

        // Handle game mode click
        plugin.getGameModeGUIManager().handleGameModeClick(player, clicked);
    }
}
