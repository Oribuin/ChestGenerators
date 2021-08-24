package xyz.oribuin.chestgenerators.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;
import xyz.oribuin.chestgenerators.ChestGenPlugin;
import xyz.oribuin.chestgenerators.obj.Generator;
import xyz.oribuin.chestgenerators.util.PluginUtils;
import xyz.oribuin.orilibrary.database.DatabaseConnector;
import xyz.oribuin.orilibrary.database.MySQLConnector;
import xyz.oribuin.orilibrary.database.SQLiteConnector;
import xyz.oribuin.orilibrary.manager.Manager;
import xyz.oribuin.orilibrary.util.FileUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class DataManager extends Manager {

    private final ChestGenPlugin plugin = (ChestGenPlugin) this.getPlugin();
    private final ChestManager chestManager = this.plugin.getManager(ChestManager.class);

    private final Map<Location, Generator> cachedGens = new HashMap<>();
    private DatabaseConnector connector = null;

    public DataManager(ChestGenPlugin plugin) {
        super(plugin);
    }

    @Override
    public void enable() {
        final FileConfiguration config = this.getPlugin().getConfig();

        if (config.getBoolean("mysql.enabled")) {
            String hostName = config.getString("mysql.host");
            int port = config.getInt("mysql.port");
            String dbname = config.getString("mysql.dbname");
            String username = config.getString("mysql.username");
            String password = config.getString("mysql.password");
            boolean ssl = config.getBoolean("mysql.ssl");

            this.connector = new MySQLConnector(this.getPlugin(), hostName, port, dbname, username, password, ssl);
        } else {
            FileUtils.createFile(this.getPlugin(), "chestgenerators.db");
            this.connector = new SQLiteConnector(this.getPlugin(), "chestgenerators.db");
        }

        this.plugin.getLogger().info("Connected to the database using " + this.connector.connectorName());

        this.async((task) -> this.connector.connect(connection -> {

            // Create the SQL Database for the chests so the plugin remembers where they are.
            final String query = "CREATE TABLE IF NOT EXISTS chestgenerators_chests (world TEXT, x DOUBLE, y DOUBLE, z DOUBLE, PRIMARY KEY(world, x, y, z))";
            connection.prepareStatement(query).executeUpdate();

            // Make sure we don't magically cache a generator twice.
            this.cachedGens.clear();

            // Get all the generators saved in the plugin.
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM chestgenerators_chests")) {
                final ResultSet result = statement.executeQuery();
                while (result.next()) {

                    // Define the location of the saved generator.
                    final World world = Bukkit.getWorld(result.getString("world"));
                    final double x = result.getDouble("x");
                    final double y = result.getDouble("y");
                    final double z = result.getDouble("z");

                    final Location loc = PluginUtils.getBlockLoc(new Location(world, x, y, z));

                    // Delete the location from the database if the block isnt a chest anymore
                    if (!(loc.getBlock().getState() instanceof Chest chest)) {
                        this.deleteGenerator(loc);
                        return;
                    }

                    // Check if the chest is a generator orn ot.
                    final Optional<Generator> optionalGen = this.chestManager.getGenFromPDC(loc, chest.getPersistentDataContainer());
                    if (optionalGen.isEmpty()) {
                        this.deleteGenerator(loc);
                        return;
                    }

                    // Cache the gen to prevent a million SQL Queries.
                    this.cachedGens.put(loc, optionalGen.get());
                }
            }

        }));
    }

    /**
     * Create & Cache a new generator from location.
     *
     * @param gen The generator being saved.
     */
    public void createGenerator(Generator gen) {
        final Location loc = gen.getLocation();
        if (loc == null || loc.getWorld() == null)
            return;

        this.cachedGens.put(PluginUtils.getBlockLoc(loc), gen);
        this.async(task -> this.connector.connect(connection -> {
            final String query = "INSERT INTO chestgenerators_chests (world, x, y, z) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, loc.getWorld().getName());
                statement.setDouble(2, loc.getX());
                statement.setDouble(3, loc.getY());
                statement.setDouble(4, loc.getZ());
                statement.executeUpdate();
            }
        }));
    }

    /**
     * Delete a generator in SQL & Cache from location.
     *
     * @param loc The location of the generator.
     */
    public void deleteGenerator(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return;

        this.cachedGens.remove(PluginUtils.getBlockLoc(loc));
        this.async(task -> this.connector.connect(connection -> {
            final String query = "DELETE FROM chestgenerators_chests WHERE world = ? AND x = ? AND y = ? AND z = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, loc.getWorld().getName());
                statement.setDouble(2, loc.getX());
                statement.setDouble(3, loc.getY());
                statement.setDouble(4, loc.getZ());
                statement.executeUpdate();
            }
        }));
    }

    @Override
    public void disable() {
        if (this.connector != null) {
            this.connector.closeConnection();
        }
    }

    public void async(Consumer<BukkitTask> callback) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(plugin, callback);
    }

    public Map<Location, Generator> getCachedGens() {
        return cachedGens;
    }

}
