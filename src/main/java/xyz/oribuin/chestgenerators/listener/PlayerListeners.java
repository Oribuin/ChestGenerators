package xyz.oribuin.chestgenerators.listener;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.oribuin.chestgenerators.ChestGenPlugin;
import xyz.oribuin.chestgenerators.gui.ChestGUI;
import xyz.oribuin.chestgenerators.manager.ChestManager;
import xyz.oribuin.chestgenerators.obj.Generator;

import java.util.Optional;

public class PlayerListeners implements Listener {

    private final ChestGenPlugin plugin;
    private final ChestManager chestManager;
    // add message manager

    public PlayerListeners(final ChestGenPlugin plugin) {
        this.plugin = plugin;
        this.chestManager = this.plugin.getManager(ChestManager.class);
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onChestOpen(PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (!event.hasBlock() || event.getAction() != Action.LEFT_CLICK_BLOCK)
            return;

        final Block block = event.getClickedBlock();
        if (block == null || !(block.getState() instanceof Chest chest))
            return;

        final Optional<Generator> generator = this.chestManager.getGenFromPDC(block.getLocation(), chest.getPersistentDataContainer());
        if (generator.isEmpty())
            return;

        if (player.isSneaking())
            return;

        event.setCancelled(true);
        // TODO, Add protection plugin support

        new ChestGUI(plugin, generator.get()).createGui(player);
    }
}
