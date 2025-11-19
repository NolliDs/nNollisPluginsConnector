package pl.nollis.connector.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
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
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) {
            return;
        }

        // Check if it's the game selector compass
        if (plugin.getLobbyItemManager().isGameSelectorItem(item)) {
            // Cancel ALL interactions with the compass to prevent teleporting
            event.setCancelled(true);

            // Only open GUI on right-click
            if (event.getAction() == Action.RIGHT_CLICK_AIR ||
                event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                plugin.getGameModeGUIManager().openGameModeGUI(player);
            }
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
            // Prevent moving the compass in spawn world
            ItemStack currentItem = event.getCurrentItem();
            ItemStack cursorItem = event.getCursor();

            if (plugin.getSpawnManager().isSpawnWorld(player.getWorld())) {
                if ((currentItem != null && plugin.getLobbyItemManager().isGameSelectorItem(currentItem)) ||
                    (cursorItem != null && plugin.getLobbyItemManager().isGameSelectorItem(cursorItem))) {
                    event.setCancelled(true);
                    return;
                }
            }
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

    /**
     * Prevent dropping the compass in spawn world
     */
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getSpawnManager().isSpawnWorld(player.getWorld())) {
            return;
        }

        ItemStack item = event.getItemDrop().getItemStack();
        if (plugin.getLobbyItemManager().isGameSelectorItem(item)) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevent swapping compass to offhand in spawn world
     */
    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getSpawnManager().isSpawnWorld(player.getWorld())) {
            return;
        }

        ItemStack mainHand = event.getMainHandItem();
        ItemStack offHand = event.getOffHandItem();

        if ((mainHand != null && plugin.getLobbyItemManager().isGameSelectorItem(mainHand)) ||
            (offHand != null && plugin.getLobbyItemManager().isGameSelectorItem(offHand))) {
            event.setCancelled(true);
        }
    }
}
