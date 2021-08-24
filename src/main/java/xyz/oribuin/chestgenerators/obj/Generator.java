package xyz.oribuin.chestgenerators.obj;

import org.bukkit.Location;

import java.util.UUID;

public class Generator {

    private Location location = null;
    private boolean enabled = true;
    private ItemGenerator itemGenerator;
    private UUID owner = null;

    public Generator(final ItemGenerator itemGen) {
        this.itemGenerator = itemGen;
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

    public ItemGenerator getItemGenerator() {
        return itemGenerator;
    }

    public void setItemGenerator(ItemGenerator itemGenerator) {
        this.itemGenerator = itemGenerator;
    }

}
