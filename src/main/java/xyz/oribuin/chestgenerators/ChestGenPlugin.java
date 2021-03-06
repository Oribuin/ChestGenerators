package xyz.oribuin.chestgenerators;

import xyz.oribuin.chestgenerators.listener.BlockListeners;
import xyz.oribuin.chestgenerators.listener.PlayerListeners;
import xyz.oribuin.chestgenerators.manager.ChestManager;
import xyz.oribuin.chestgenerators.manager.DataManager;
import xyz.oribuin.chestgenerators.manager.GeneratorManager;
import xyz.oribuin.chestgenerators.task.GenerateTask;
import xyz.oribuin.orilibrary.OriPlugin;

public class ChestGenPlugin extends OriPlugin {

    @Override
    public void enablePlugin() {

        // Load Managers Asynchronously
        this.getServer().getScheduler().runTaskAsynchronously(this, () -> {
            this.getManager(GeneratorManager.class);
            this.getManager(ChestManager.class);
            this.getManager(DataManager.class);
        });

        // Register Plugin Listeners.
        new BlockListeners(this);
        new PlayerListeners(this);

        // Load & Run the scheduled generator task.
        new GenerateTask(this).runTaskTimer(this, 0, this.getConfig().getInt("generation-delay"));

    }

    @Override
    public void disablePlugin() {

    }

}
