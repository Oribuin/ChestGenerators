package xyz.oribuin.chestgenerators.manager;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import xyz.oribuin.chestgenerators.ChestGenPlugin;
import xyz.oribuin.chestgenerators.obj.Generator;
import xyz.oribuin.chestgenerators.obj.ItemGenerator;
import xyz.oribuin.chestgenerators.obj.SavedGenerators;
import xyz.oribuin.chestgenerators.util.PluginUtils;
import xyz.oribuin.gui.Item;
import xyz.oribuin.orilibrary.manager.Manager;
import xyz.oribuin.orilibrary.util.HexUtils;
import xyz.oribuin.orilibrary.util.StringPlaceholders;

import java.util.*;
import java.util.stream.Collectors;

public class ChestManager extends Manager {

    private final ChestGenPlugin plugin = (ChestGenPlugin) this.getPlugin();

    // Namespaced keys for the plugin so I don't have to keep making new instances.
    private final NamespacedKey enabled = new NamespacedKey(this.plugin, "enabled");
    private final NamespacedKey activeGen = new NamespacedKey(this.plugin, "activeGenerator");
    private final NamespacedKey owner = new NamespacedKey(this.plugin, "owner");
    private final NamespacedKey previousGens = new NamespacedKey(this.plugin, "previousGens");

    private final Gson gson = new Gson();

    public ChestManager(ChestGenPlugin plugin) {
        super(plugin);
    }

    /**
     * Save a generator block's data in the block to prevent excessive SQL Uses
     *
     * @param generator The generator being saved.
     */
    public void saveGenerator(Generator generator) {
        // Don't save a physical generator if the location isn't set.
        if (generator.getLocation() == null)
            return;

        // Check if the generator is a chest or not.
        if (!(generator.getLocation().getBlock().getState() instanceof Chest chest))
            return;

        final PersistentDataContainer container = chest.getPersistentDataContainer();
        // here is our daily: Why the fuck do I need to update each time i set a new value on a block and a block only.
        container.set(enabled, PersistentDataType.STRING, String.valueOf(generator.isEnabled()));
        chest.update();

        // Save the active generator id
        container.set(activeGen, PersistentDataType.INTEGER, generator.getActiveGenerator().getId());
        chest.update();

        if (generator.getOwner() != null)
            container.set(activeGen, PersistentDataType.STRING, generator.getOwner().toString());
        chest.update();

        container.set(previousGens, PersistentDataType.STRING, serializeItemGens(generator.getUnlockedGens()));
        chest.update();
    }

    public ItemStack getGeneratorAsItem(Generator gen, int amount) {
        // Define the itemstack's basic values
        final ItemStack item = new Item.Builder(Material.CHEST)
                .setName(this.format(gen, plugin.getConfig().getString("generator-item.name")))
                .setLore(this.format(gen, plugin.getConfig().getStringList("generator-item.lore")))
                .setFlags(ItemFlag.HIDE_ATTRIBUTES)
                .glow(plugin.getConfig().getBoolean("generator-item.glow"))
                .setAmount(amount)
                .create();

        final ItemMeta meta = item.getItemMeta();
        // The item meta cannot be null
        assert meta != null;

        final PersistentDataContainer container = meta.getPersistentDataContainer();
        // Save wether the generator is enabled or not
        container.set(enabled, PersistentDataType.STRING, String.valueOf(gen.isEnabled()));
        // Save the active generator id
        container.set(activeGen, PersistentDataType.INTEGER, gen.getActiveGenerator().getId());
        // Save the generator's owner
        if (gen.getOwner() != null)
            container.set(owner, PersistentDataType.STRING, gen.getOwner().toString());

        // Save the generator's previous generators
        container.set(previousGens, PersistentDataType.STRING, serializeItemGens(gen.getUnlockedGens()));
        item.setItemMeta(meta);
        return item;
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

        // Check if the generator for the chest even exists anymore.
        final GeneratorManager genManager = this.plugin.getManager(GeneratorManager.class);

        final Optional<ItemGenerator> generatorOptional = genManager.getGeneratorByID(container.getOrDefault(activeGen, PersistentDataType.INTEGER, genManager.getDefaultGenerator().getId()));
        if (generatorOptional.isEmpty())
            return Optional.empty();

        final Generator generator = new Generator(generatorOptional.get());
        generator.setLocation(loc);

        // Save all the generator's previous generators
        if (container.get(previousGens, PersistentDataType.STRING) != null)
            generator.setUnlockedGens(deserializeItemGens(container.get(previousGens, PersistentDataType.STRING)));

        // If the generator has an owner, save it.
        if (container.get(owner, PersistentDataType.STRING) != null)
            generator.setOwner(UUID.fromString(Objects.requireNonNull(container.get(owner, PersistentDataType.STRING))));

        return Optional.of(generator);
    }

    /**
     * Serialize a list of item generators to easily be stored in a String
     *
     * @param itemGens The list of item generators
     * @return The item generator ids
     */
    public String serializeItemGens(Set<ItemGenerator> itemGens) {
        return gson.toJson(new SavedGenerators(itemGens.stream().map(ItemGenerator::getId).collect(Collectors.toList())));
    }

    public Set<ItemGenerator> deserializeItemGens(String serialized) {
        if (serialized == null)
            return Set.of();

        final GeneratorManager genManager = this.plugin.getManager(GeneratorManager.class);

        return gson.fromJson(serialized, SavedGenerators.class).getItemGens()
                .stream()
                .map(genManager::getGeneratorByID)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

    }

    /**
     * Get the placeholders for the hopper itself,
     *
     * @param gen The hopper
     * @return The placeholders for the hopper.
     */
    public StringPlaceholders getPlaceholders(Generator gen) {
        return StringPlaceholders.builder()
                .addPlaceholder("enabled", gen.isEnabled() ? "Yes" : "No")
                .addPlaceholder("generator", HexUtils.colorify(gen.getActiveGenerator().getDisplayName()))
                .addPlaceholder("description", HexUtils.colorify(PluginUtils.formatList(gen.getActiveGenerator().getDescription())))
                .addPlaceholder("owner", gen.getOwner() != null ? Bukkit.getOfflinePlayer(gen.getOwner()).getName() : "None")
                .build();
    }

    /**
     * Format a message relating to generator with placeholders.
     *
     * @param generator The Chest Generator
     * @param message   The message
     * @return A colorified message with hopper placeholder support.
     */
    private String format(Generator generator, String message) {
        return HexUtils.colorify(this.getPlaceholders(generator).apply(message));
    }

    /**
     * Format a message list relating to generator with placeholders.
     *
     * @param generator The chest generator.
     * @param message   The message list
     * @return A colorified message list with hopper placeholder support.
     */
    private List<String> format(Generator generator, List<String> message) {
        return message.stream().map(s -> format(generator, s)).collect(Collectors.toList());
    }

}
