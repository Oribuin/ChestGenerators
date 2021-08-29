package xyz.oribuin.chestgenerators.gui;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import xyz.oribuin.chestgenerators.ChestGenPlugin;
import xyz.oribuin.chestgenerators.manager.ChestManager;
import xyz.oribuin.chestgenerators.manager.GeneratorManager;
import xyz.oribuin.chestgenerators.obj.Generator;
import xyz.oribuin.chestgenerators.obj.ItemGenerator;
import xyz.oribuin.gui.Item;
import xyz.oribuin.gui.PaginatedGui;
import xyz.oribuin.orilibrary.util.HexUtils;
import xyz.oribuin.orilibrary.util.StringPlaceholders;

import java.util.*;
import java.util.stream.Collectors;

import static xyz.oribuin.orilibrary.util.HexUtils.colorify;

public class ChestGUI {

    private final ChestGenPlugin plugin;
    private final Generator generator;
    private final ChestManager chestManager;
    private final GeneratorManager genManager;

    public ChestGUI(final ChestGenPlugin plugin, Generator generator) {
        this.plugin = plugin;
        this.generator = generator;
        this.chestManager = this.plugin.getManager(ChestManager.class);
        this.genManager = this.plugin.getManager(GeneratorManager.class);
    }

    public void createGui(Player player) {

        // Define all the page slots.
        final List<Integer> pageSlots = new ArrayList<>();
        for (int i = 9; i < 36; i++)
            pageSlots.add(i);

        final PaginatedGui gui = new PaginatedGui(45, HexUtils.colorify(generator.getActiveGenerator().getDisplayName()), pageSlots);
        gui.setDefaultClickFunction(event -> {
            event.setResult(Event.Result.DENY);
            event.setCancelled(true);
            ((Player) event.getWhoClicked()).updateInventory();
        });

        gui.setPersonalClickAction(event -> gui.getDefaultClickFunction().accept(event));
        gui.setCloseAction(event -> this.chestManager.saveGenerator(generator));
        gui.setItems(Arrays.asList(0, 8, 36, 44), Item.filler(Material.CYAN_STAINED_GLASS_PANE));
        gui.setItems(Arrays.asList(1, 2, 6, 7, 37, 38, 42, 43), Item.filler(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
        gui.setItems(Arrays.asList(3, 4, 5, 39, 40, 41), Item.filler(Material.GRAY_STAINED_GLASS_PANE));

        gui.setItem(38, new Item.Builder(Material.PAPER)
                .setName(colorify("#FF4F58&lBack Page"))
                .setLore(colorify("&7Click to go to"), colorify("&7the previous page!"))
                .create(), event -> gui.previous(event.getWhoClicked()));

        gui.setItem(42, new Item.Builder(Material.PAPER)
                .setName(colorify("#FF4F58&lNext Page"))
                .setLore(colorify("&7Click to go to"), colorify("&7the next page!"))
                .create(), event -> gui.next(event.getWhoClicked()));

        switchGenerator(gui);

        gui.open(player);
    }

    public void switchGenerator(PaginatedGui gui) {

        final String chanceMapping = this.plugin.getConfig().getString("chance-mapping");
        final List<ItemGenerator> generators = new ArrayList<>(this.genManager.getGeneratorMap().values());
        generators.sort(Comparator.comparing(ItemGenerator::getCost));

        generators.forEach(gen -> {
            final List<String> description = gen.getDescription().stream()
                    .map(HexUtils::colorify)
                    .collect(Collectors.toList());


            final List<Map.Entry<ItemStack, Integer>> chanceMap = gen.getMaterialChances()
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .collect(Collectors.toList());

            if (chanceMapping == null || chanceMapping.equalsIgnoreCase("cancel"))
                chanceMap.forEach(entry -> {
                    final ItemStack itemStack = entry.getKey();

                    final StringPlaceholders plc = StringPlaceholders.builder("item", WordUtils.capitalizeFully(itemStack.getType().name().toLowerCase().replace("_", " ")))
                            .addPlaceholder("amount", itemStack.getAmount())
                            .addPlaceholder("chance", entry.getValue())
                            .build();

                    description.add(HexUtils.colorify(plc.apply(chanceMapping)));
                });

            final ItemStack item = new Item.Builder(gen.getDisplayItem())
                    .setName(colorify(gen.getDisplayName()))
                    .setLore(description)
                    .glow(generator.getActiveGenerator() == gen)
                    .create();

            gui.addPageItem(item, event -> {
                //                if (genUnlocked) {
                generator.setActiveGenerator(gen);
                generator.getUnlockedGens().add(gen);
                chestManager.saveGenerator(generator);
                event.getWhoClicked().closeInventory();
                // Switch generator if unlocked.
                //                    return;
                //                }
                // TODO, Add requirement to buy the generator
            });
        });
    }

}
