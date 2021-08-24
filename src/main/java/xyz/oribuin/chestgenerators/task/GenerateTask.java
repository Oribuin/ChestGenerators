package xyz.oribuin.chestgenerators.task;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.oribuin.chestgenerators.ChestGenPlugin;
import xyz.oribuin.chestgenerators.manager.ChestManager;
import xyz.oribuin.chestgenerators.manager.DataManager;
import xyz.oribuin.chestgenerators.obj.Generator;

import java.util.*;

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
            Map<Material, Integer> chanceMap = optional.get().getGenerator().getMaterialChances();
            final int maxChance = chanceMap.values().stream().max(Comparator.comparing(Integer::intValue)).orElse(100);

            for (Material material : chanceMap.keySet()) {
                final int chance = chanceMap.get(material);
                if ((random.nextInt(maxChance) + 1) > chance)
                    continue;

                chest.getInventory().addItem(new ItemStack(material));
                break;
            }

        });

    }

}
