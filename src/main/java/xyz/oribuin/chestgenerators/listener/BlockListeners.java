package xyz.oribuin.chestgenerators.listener;

import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.oribuin.chestgenerators.ChestGenPlugin;
import xyz.oribuin.chestgenerators.manager.ChestManager;
import xyz.oribuin.chestgenerators.manager.DataManager;
import xyz.oribuin.chestgenerators.manager.GeneratorManager;
import xyz.oribuin.chestgenerators.obj.Generator;

import java.util.Arrays;
import java.util.Objects;
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
    public void onGenPlace(BlockPlaceEvent event) {
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
        if (this.chestManager.getGenFromPDC(event.getBlock().getLocation(), chest.getPersistentDataContainer()).isPresent())
            return;

        // TODO Stop The ability to place another player's chest if config option is enabled & player doesnt have bypass permission

        final Generator gen = handGen.get();
        gen.setLocation(getBlockLoc(chest.getLocation()));
        this.data.createGenerator(gen);
        this.chestManager.saveGenerator(gen);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onGenBreak(BlockBreakEvent event) {

        final Player player = event.getPlayer();
        if (!(event.getBlock().getState() instanceof Chest chest))
            return;

        final Optional<Generator> generator = this.chestManager.getGenFromPDC(chest.getLocation(), chest.getPersistentDataContainer());
        if (generator.isEmpty())
            return;

        // TODO Stop The ability to place another player's chest if config option is enabled & player doesnt have bypass permission
        this.data.deleteGenerator(chest.getLocation());
        // todo save message

        chest.getWorld().dropItemNaturally(event.getBlock().getLocation(), this.chestManager.getGeneratorAsItem(generator.get(), 1));
        // Get all the items from the block that was destroyed
        Arrays.stream(chest.getBlockInventory().getContents())
                .filter(Objects::nonNull)
                .forEach(itemStack -> chest.getWorld().dropItem(chest.getLocation(), itemStack));

        event.setDropItems(false);

    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        final ItemStack item = this.chestManager.getGeneratorAsItem(new Generator(this.plugin.getManager(GeneratorManager.class).getDefaultGenerator()), 1);
        event.getPlayer().getInventory().addItem(item);
    }


}
