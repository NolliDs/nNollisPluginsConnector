package pl.nollis.connector.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import pl.nollis.connector.NollisPluginsConnector;

public class SpawnManager {

    private final NollisPluginsConnector plugin;

    public SpawnManager(NollisPluginsConnector plugin) {
        this.plugin = plugin;
    }

    /**
     * Get the spawn location from config.yml
     * @return Spawn location
     */
    public Location getSpawnLocation() {
        ConfigurationSection spawnSection = plugin.getConfig().getConfigurationSection("spawn");

        if (spawnSection == null) {
            plugin.getLogger().warning("Spawn section not found in config.yml! Using default values.");
            World world = Bukkit.getWorld("spawn");
            if (world == null) {
                world = Bukkit.getWorlds().get(0); // Fallback to first world
            }
            return new Location(world, 0.5, 100.0, 0.5, 0.0f, 0.0f);
        }

        String worldName = spawnSection.getString("world", "spawn");
        double x = spawnSection.getDouble("x", 0.5);
        double y = spawnSection.getDouble("y", 100.0);
        double z = spawnSection.getDouble("z", 0.5);
        float yaw = (float) spawnSection.getDouble("yaw", 0.0);
        float pitch = (float) spawnSection.getDouble("pitch", 0.0);

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("World '" + worldName + "' not found! Using first available world.");
            world = Bukkit.getWorlds().get(0);
        }

        return new Location(world, x, y, z, yaw, pitch);
    }

    /**
     * Get the spawn world name from config
     * @return Spawn world name
     */
    public String getSpawnWorldName() {
        return plugin.getConfig().getString("spawn.world", "spawn");
    }

    /**
     * Check if the given world is the spawn world
     * @param world World to check
     * @return true if it's the spawn world
     */
    public boolean isSpawnWorld(World world) {
        if (world == null) return false;
        return world.getName().equals(getSpawnWorldName());
    }
}