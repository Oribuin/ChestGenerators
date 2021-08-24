package xyz.oribuin.chestgenerators.listener;

import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.oribuin.chestgenerators.ChestGenPlugin;
import xyz.oribuin.chestgenerators.manager.ChestManager;
import xyz.oribuin.chestgenerators.manager.DataManager;
import xyz.oribuin.chestgenerators.obj.Generator;

import java.util.Optional;

import static xyz.oribuin.chestgenerators.util.PluginUtils.getBlockLoc;

public class BlockListeners implements Listener {

    private final ChestGenPlugin plugin;
    private final DataManager data;
    private final ChestManager chestManager;
    // TODO Add Message Manager

    public BlockListeners(final ChestGenPlugin plugin) {
        this.plugin = plugin;
        this.data = this.plugin.getManager(DataManager.class);
        this.chestManager = this.plugin.getManager(ChestManager.class);

        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();

        if (!(event.getBlock().getState() instanceof Chest chest))
            return;

        final ItemStack item = event.getItemInHand();
        final ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;

        // Check if the item in the hand is a generator, Don't make all chests generator chests.
        final Optional<Generator> handGen = this.chestManager.getGenFromPDC(null, meta.getPersistentDataContainer());
        if (handGen.isEmpty())
            return;

        // TODO, Add protection plugin checks.
        if (this.chestManager.getGenFromPDC(getBlockLoc(event.getBlock().getLocation()), chest.getPersistentDataContainer()).isPresent())
            return;

        // TODO, Stop chest from merging with another one

        // TODO Stop The ability to place another player's chest if config option is enabled & player doesnt have bypass permission

        final Generator gen = handGen.get();
        gen.setLocation(getBlockLoc(chest.getLocation()));
        this.data.createGenerator(gen);
        this.chestManager.saveGenerator(gen);
    }


}
