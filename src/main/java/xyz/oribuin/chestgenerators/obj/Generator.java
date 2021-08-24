package xyz.oribuin.chestgenerators.obj;

import org.bukkit.Location;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Generator {

    private Location location;
    private boolean enabled;
    private ItemGenerator activeGenerator;
    private UUID owner;
    private List<ItemGenerator> unlockedGens;

    public Generator(final ItemGenerator activeGenerator) {
        this.activeGenerator = activeGenerator;
        this.location = null;
        this.enabled = true;
        this.owner = null;
        this.unlockedGens = Collections.singletonList(activeGenerator);
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ItemGenerator getActiveGenerator() {
        return activeGenerator;
    }

    public void setActiveGenerator(ItemGenerator activeGenerator) {
        this.activeGenerator = activeGenerator;
    }

    public List<ItemGenerator> getUnlockedGens() {
        return unlockedGens;
    }

    public void setUnlockedGens(List<ItemGenerator> unlockedGens) {
        this.unlockedGens = unlockedGens;
    }

}
