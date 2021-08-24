package xyz.oribuin.chestgenerators;

import xyz.oribuin.chestgenerators.manager.ChestManager;
import xyz.oribuin.chestgenerators.manager.DataManager;
import xyz.oribuin.chestgenerators.manager.GeneratorManager;
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


    }

    @Override
    public void disablePlugin() {

    }

}
