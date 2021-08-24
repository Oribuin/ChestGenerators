package xyz.oribuin.chestgenerators.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import xyz.oribuin.chestgenerators.ChestGenPlugin;
import xyz.oribuin.chestgenerators.obj.Generator;
import xyz.oribuin.chestgenerators.obj.ItemGenerator;
import xyz.oribuin.chestgenerators.util.PluginUtils;
import xyz.oribuin.orilibrary.manager.Manager;
import xyz.oribuin.orilibrary.util.HexUtils;
import xyz.oribuin.orilibrary.util.StringPlaceholders;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChestManager extends Manager {

    private final ChestGenPlugin plugin = (ChestGenPlugin) this.getPlugin();

    // Namespaced keys for the plugin so I don't have to keep making new instances.
    private final NamespacedKey enabled = new NamespacedKey(this.plugin, "enabled");
    private final NamespacedKey gen = new NamespacedKey(this.plugin, "activeGenerator");
    private final NamespacedKey owner = new NamespacedKey(this.plugin, "owner");

    public ChestManager(ChestGenPlugin plugin) {
        super(plugin);
    }

    /**
     * Get a chest generator from a PersistentDataContainer.
     *
     * @param loc       The location of the generator
     * @param container The persistent data container.
     * @return An optional chest generator.
     */
    public Optional<Generator> getGenFromPDC(Location loc, PersistentDataContainer container) {
        if (!container.has(enabled, PersistentDataType.STRING))
            return Optional.empty();

        final Generator generator = new Generator();
        generator.setLocation(loc);

        // Check if the generator for the chest even exists anymore.
        final Optional<ItemGenerator> generatorOptional = this.plugin.getManager(GeneratorManager.class).getGeneratorByID(container.getOrDefault(gen, PersistentDataType.INTEGER, 0));
        if (generatorOptional.isEmpty())
            return Optional.empty();

        generator.setGenerator(generatorOptional.get());

        if (container.get(owner, PersistentDataType.STRING) != null)
            // It isnt gonna be null with the check above.
            generator.setOwner(UUID.fromString(Objects.requireNonNull(container.get(owner, PersistentDataType.STRING))));

        return Optional.of(generator);
    }

    /**
     * Get the placeholders for the hopper itself,
     *
     * @param generator The hopper
     * @return The placeholders for the hopper.
     */
    public StringPlaceholders getPlaceholders(Generator generator) {
        return StringPlaceholders.builder()
                .addPlaceholder("enabled", generator.isEnabled() ? "Yes" : "No")
                .addPlaceholder("name", HexUtils.colorify(generator.getGenerator().getDisplayName()))
                .addPlaceholder("description", PluginUtils.formatList(generator.getGenerator().getDescription()))
                .addPlaceholder("owner", Bukkit.getOfflinePlayer(generator.getOwner()).getName())
                .build();
    }

    /**
     * Format a message relating to generator with placeholders.
     *
     * @param generator The Chest Generator
     * @param message        The message
     * @return A colorified message with hopper placeholder support.
     */
    private String format(Generator generator, String message) {
        return HexUtils.colorify(this.getPlaceholders(generator).apply(message));
    }

    /**
     * Format a message list relating to generator with placeholders.
     *
     * @param generator The chest generator.
     * @param message        The message list
     * @return A colorified message list with hopper placeholder support.
     */
    private List<String> format(Generator generator, List<String> message) {
        return message.stream().map(s -> format(generator, s)).collect(Collectors.toList());
    }

}
