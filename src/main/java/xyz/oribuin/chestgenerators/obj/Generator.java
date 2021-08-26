package xyz.oribuin.chestgenerators.obj;

import org.bukkit.Location;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Generator {

    private Location location;
    private boolean enabled;
    private ItemGenerator activeGenerator;
    private UUID owner;
    private Set<ItemGenerator> unlockedGens;

    public Generator(final ItemGenerator activeGenerator) {
        this.activeGenerator = activeGenerator;
        this.location = null;
        this.enabled = true;
        this.owner = null;
        this.unlockedGens = Set.of(activeGenerator);
    }

    public boolean hasUnlockedGenerator(ItemGenerator itemGenerator) {
        return this.getUnlockedGens().stream().map(ItemGenerator::getId).collect(Collectors.toList()).contains(itemGenerator.getId());
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

    public Set<ItemGenerator> getUnlockedGens() {
        return unlockedGens;
    }

    public void setUnlockedGens(Set<ItemGenerator> unlockedGens) {
        this.unlockedGens = unlockedGens;
    }

}
