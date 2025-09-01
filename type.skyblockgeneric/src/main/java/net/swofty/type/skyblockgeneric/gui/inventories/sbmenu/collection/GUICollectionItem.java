package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.collection;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.item.ItemType;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.collection.CollectionCategories;
import net.swofty.type.skyblockgeneric.collection.CollectionCategory;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCollection;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUICollectionItem extends HypixelInventoryGUI {
    private final ItemType item;
    private final CollectionCategory category;
    private final CollectionCategory.ItemCollection collection;

    public GUICollectionItem(ItemType item) {
        super(item.getDisplayName() + " Collection", InventoryType.CHEST_6_ROW);

        this.item = item;
        this.category = CollectionCategories.getCategory(item);
        this.collection = category.getCollection(item);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStackCreator.createNamedItemStack(Material.BLACK_STAINED_GLASS_PANE));
        set(GUIClickableItem.getCloseItem(49));
        set(GUIClickableItem.getGoBackItem(48, new GUICollectionCategory(
                category,
                ((SkyBlockPlayer) getPlayer()).getCollection().getDisplay(new ArrayList<>(), category)
        )));
        set(new GUIItem(4) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return ItemStackCreator.getStack("§e" + item.getDisplayName(), item.material, 1,
                        "§7View all your " + item.getDisplayName() + " Collection",
                        "§7progress and rewards!",
                        " ",
                        "§7Total Collected: §e" + player.getCollection().get(item));
            }
        });

        int slot = 17;
        for (CollectionCategory.ItemCollectionReward reward : collection.rewards()) {
            slot++;

            set(new GUIClickableItem(slot) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    new GUICollectionReward(item, reward).open(player);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    DatapointCollection.PlayerCollection playerCollection = player.getCollection();

                    List<String> lore = new ArrayList<>();
                    lore.add(" ");
                    playerCollection.getDisplay(lore, collection, reward);
                    lore.add(" ");
                    lore.add("§eClick to view rewards!");

                    if (playerCollection.getReward(collection) == null) {
                        return ItemStackCreator.getStack(
                                "§7" + item.getDisplayName() + " " + StringUtility.getAsRomanNumeral(collection.getPlacementOf(reward) + 1),
                                Material.GREEN_STAINED_GLASS_PANE,
                                1,
                                lore
                        );
                    }

                    Material material;
                    String colour;
                    if (playerCollection.getReward(collection) == reward) {
                        material = Material.YELLOW_STAINED_GLASS_PANE;
                        colour = "§e";
                    } else if (collection.getPlacementOf(playerCollection.getReward(collection))
                            > collection.getPlacementOf(reward)) {
                        material = Material.LIME_STAINED_GLASS_PANE;
                        colour = "§a";
                    } else {
                        material = Material.RED_STAINED_GLASS_PANE;
                        colour = "§c";
                    }

                    return ItemStackCreator.getStack(
                            colour + item.getDisplayName() + " " + StringUtility.getAsRomanNumeral(collection.getPlacementOf(reward) + 1),
                            material,
                            1,
                            lore
                    );
                }
            });
        }

        updateItemStacks(getInventory(), getPlayer());
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {

    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {

    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }
}
