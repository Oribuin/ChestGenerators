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

import java.util.Comparator;
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
            Map<ItemStack, Integer> chanceMap = optional.get().getItemGenerator().getMaterialChances();
            final int maxChance = chanceMap.values().stream().max(Comparator.comparing(Integer::intValue)).orElse(100);

            for (ItemStack itemStack : chanceMap.keySet()) {
                final int chance = chanceMap.get(itemStack);
                if ((random.nextInt(maxChance) + 1) > chance)
                    continue;

                chest.getInventory().addItem(itemStack);
                // ooo pretty particles
                final Color color = Color.fromRGB(231, 56, 39);
                for (int i = 0; i < 3; i++)
                    chest.getWorld().spawnParticle(Particle.REDSTONE, PluginUtils.centerLocation(chest.getLocation()),
                            3,
                            0.4,
                            0.4,
                            0.4,
                            0.0,
                            new Particle.DustOptions(color, 1)
                    );

                break;
            }

        });

    }

}
