package xyz.oribuin.chestgenerators.task;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.oribuin.chestgenerators.ChestGenPlugin;
import xyz.oribuin.chestgenerators.manager.ChestManager;
import xyz.oribuin.chestgenerators.manager.DataManager;
import xyz.oribuin.chestgenerators.obj.Generator;
import xyz.oribuin.chestgenerators.util.PluginUtils;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class GenerateTask extends BukkitRunnable {

    private final DataManager data;
    private final ChestManager chestManager;
    private final Random random;

    public GenerateTask(final ChestGenPlugin plugin) {
        this.data = plugin.getManager(DataManager.class);
        this.chestManager = plugin.getManager(ChestManager.class);
        this.random = new Random();
    }

    @Override
    public void run() {
        this.data.getCachedGens().forEach((loc, gen) -> {

            // Don't generate chunks items in a chunk that is loaded
            if (!loc.getChunk().isLoaded())
                return;

            // Don't generate items for disabled generators.
            if (!gen.isEnabled())
                return;

            if (!(loc.getBlock().getState() instanceof Chest chest))
                return;

            final Optional<Generator> optional = this.chestManager.getGenFromPDC(chest.getLocation(), chest.getPersistentDataContainer());
            if (optional.isEmpty())
                return;

            // Create a new instance of the map so we don't alter the current map
            Map<ItemStack, Integer> chanceMap = optional.get().getActiveGenerator().getMaterialChances();

            // https://stackoverflow.com/a/28711505
            // okay so in attempt to figure out how this works the way it does
            // i can't work it out :) It just does I guess
            int sumOfPercentages = chanceMap.values().stream().reduce(0, Integer::sum);
            int current = 0;
            int randomNumber = random.nextInt(sumOfPercentages);
            for (Map.Entry<ItemStack, Integer> entry : chanceMap.entrySet()) {
                current += entry.getValue();
                if (randomNumber > current)
                    continue;

                chest.getInventory().addItem(entry.getKey());
                // ooo pretty particles
                final Color color = Color.fromRGB(231, 56, 39);
                for (int i = 0; i < 3; i++)
                    chest.getWorld().spawnParticle(Particle.REDSTONE, PluginUtils.centerLocation(chest.getLocation()), 1, 0.3, 0.3, 0.3, 0.0, new Particle.DustOptions(color, 1));

                break;
            }

        });

    }

}
