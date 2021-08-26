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
    private Material displayItem;
    private List<String> description;
    private Map<ItemStack, Integer> materialChances;
    private double cost;
    private int globalChance;

    public ItemGenerator(final int id) {
        this.id = id;
        this.displayName = String.valueOf(id);
        this.displayItem = Material.BEDROCK;
        this.description = new ArrayList<>();
        this.materialChances = new HashMap<>();
        this.cost = 0.0;
        this.globalChance = 20;
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

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public Material getDisplayItem() {
        return displayItem;
    }

    public void setDisplayItem(Material displayItem) {
        this.displayItem = displayItem;
    }

    public int getGlobalChance() {
        return globalChance;
    }

    public void setGlobalChance(int globalChance) {
        this.globalChance = globalChance;
    }
}
