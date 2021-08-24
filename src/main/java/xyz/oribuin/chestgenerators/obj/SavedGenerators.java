package xyz.oribuin.chestgenerators.obj;

import java.util.List;

// This class is used to easily store a list of the generators that have been
// bought by the player, using GSON
// It's mostly useless outside of that but that's gson for you.
public class SavedGenerators {

    private List<Integer> itemGens;

    public SavedGenerators(List<Integer> itemGens) {
        this.itemGens = itemGens;
    }

    public List<Integer> getItemGens() {
        return itemGens;
    }

    public void setItemGens(List<Integer> itemGens) {
        this.itemGens = itemGens;
    }

}
