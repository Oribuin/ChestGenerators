package xyz.oribuin.chestgenerators.obj;

import org.bukkit.Location;

import java.util.UUID;

public class Generator {

    private Location location;
    private boolean enabled = true;
    private ItemGenerator itemGenerator;
    private UUID owner;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public ItemGenerator getGenerator() {
        return itemGenerator;
    }

    public void setGenerator(ItemGenerator itemGenerator) {
        this.itemGenerator = itemGenerator;
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


}
