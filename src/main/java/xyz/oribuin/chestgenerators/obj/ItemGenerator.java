package xyz.oribuin.chestgenerators.obj;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemGenerator {

    private final int id;
    private String displayName;
    private List<String> description;
    private Map<ItemStack, Integer> materialChances;

    public ItemGenerator(final int id) {
        this.id = id;
        this.displayName = String.valueOf(id);
        this.description = new ArrayList<>();
        this.materialChances = new HashMap<>();
    }

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public Map<ItemStack, Integer> getMaterialChances() {
        return materialChances;
    }

    public void setMaterialChances(Map<ItemStack, Integer> materialChances) {
        this.materialChances = materialChances;
    }

}
