package xyz.oribuin.chestgenerators.manager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import xyz.oribuin.chestgenerators.ChestGenPlugin;
import xyz.oribuin.chestgenerators.obj.ItemGenerator;
import xyz.oribuin.orilibrary.manager.Manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GeneratorManager extends Manager {

    private final ChestGenPlugin plugin = (ChestGenPlugin) this.getPlugin();
    private final Map<Integer, ItemGenerator> generatorMap = new HashMap<>();

    public GeneratorManager(ChestGenPlugin plugin) {
        super(plugin);
    }

    @Override
    public void enable() {

        final ConfigurationSection section = this.plugin.getConfig().getConfigurationSection("generators");
        // If there are no generators in the plugin configuration, Disable it because the plugin won't work without them.
        if (section == null) {
            this.plugin.getLogger().severe("Unable to find any generators in the config.yml... Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this.plugin);
            return;
        }

        // Get all the generators added to the config
        section.getKeys(false).forEach(s -> {

            // Define basic generator values
            final ItemGenerator itemGenerator = new ItemGenerator(Integer.parseInt(s));
            itemGenerator.setDisplayName(section.getString(s + ".name"));
            itemGenerator.setDescription(section.getStringList(s + ".description"));
            itemGenerator.setCost(section.getDouble(s + ".cost"));

            // Get all the generator's materials to see if they exist.
            final ConfigurationSection materialSection = section.getConfigurationSection("materials");
            if (materialSection == null)
                return;

            // Add all the generator materials to the generator type's materials + chances
            materialSection.getKeys(false).forEach(material -> {
                // Get the material of the item, If the material doesn't exist, skip.
                Material matchedMaterial = Material.matchMaterial(material.toUpperCase());

                if (matchedMaterial == null)
                    return;

                // Add the item to the material chances map.
                itemGenerator.getMaterialChances().put(new ItemStack(matchedMaterial, materialSection.getInt(material + ".amount")), materialSection.getInt(material + ".chance"));
            });

            // Cache the generator
            this.generatorMap.put(itemGenerator.getId(), itemGenerator);
        });

    }

    /**
     * Get a generator type by the id of the generator
     *
     * @param id The id of the generator.
     * @return The optional generator.
     */
    public Optional<ItemGenerator> getGeneratorByID(int id) {
        return Optional.ofNullable(this.generatorMap.get(id));
    }

    public ItemGenerator getDefaultGenerator() {
        // The plugin literally has to have atleast one generator or else it shuts down, so this should always be present.
        return this.generatorMap.values().stream().findFirst().get();
    }

    @Override
    public void disable() {
        generatorMap.clear();
    }

    public Map<Integer, ItemGenerator> getGeneratorMap() {
        return generatorMap;
    }

}
