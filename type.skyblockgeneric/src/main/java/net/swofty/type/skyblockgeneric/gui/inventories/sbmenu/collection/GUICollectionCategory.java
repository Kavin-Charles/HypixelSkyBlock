package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.collection;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.HypixelPaginatedGUI;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.utility.PaginationList;
import net.swofty.type.skyblockgeneric.collection.CollectionCategory;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCollection;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GUICollectionCategory extends HypixelPaginatedGUI<CollectionCategory.ItemCollection> {
    private final CollectionCategory type;
    private final List<String> display;

    public GUICollectionCategory(CollectionCategory category, List<String> display) {
        super(InventoryType.CHEST_6_ROW);

        this.type = category;
        this.display = display;
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

    @Override
    public int[] getPaginatedSlots() {
        return new int[]{
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };
    }

    @Override
    public PaginationList<CollectionCategory.ItemCollection> fillPaged(HypixelPlayer player, PaginationList<CollectionCategory.ItemCollection> paged) {
        paged.addAll(Arrays.asList(type.getCollections()));
        return paged;
    }

    @Override
    protected boolean shouldFilterFromSearch(String query, CollectionCategory.ItemCollection item) {
        return false;
    }

    @Override
    public void performSearch(HypixelPlayer player, String query, int page, int maxPage) {
        border(ItemStackCreator.createNamedItemStack(Material.BLACK_STAINED_GLASS_PANE, ""));
        set(GUIClickableItem.getCloseItem(49));
        set(GUIClickableItem.getGoBackItem(48, new GUICollections()));
        set(new GUIItem(4) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                List<String> lore = new ArrayList<>(List.of(
                        "§7View your " + type.getName() + " Collections!",
                        " "
                ));

                lore.addAll(display);

                return ItemStackCreator.getStack("§a" + type.getName() + " Collections",
                        Material.STONE_PICKAXE, 1, lore);
            }
        });

        if (page > 1) {
            set(createNavigationButton(this, 45, query, page, false));
        }
        if (page < maxPage) {
            set(createNavigationButton(this, 53, query, page, true));
        }
    }

    @Override
    public String getTitle(HypixelPlayer player, String query, int page, PaginationList<CollectionCategory.ItemCollection> paged) {
        return type.getName() + " Collections";
    }

    @Override
    public GUIClickableItem createItemFor(CollectionCategory.ItemCollection item, int slot, HypixelPlayer p) {
        SkyBlockPlayer player = (SkyBlockPlayer) p;
        DatapointCollection.PlayerCollection collection = player.getCollection();

        if (!collection.unlocked(item.type())) {
            return new GUIClickableItem(slot) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    player.sendMessage("§cYou haven't found this item yet!");
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStackCreator.getStack(
                            "§c" + item.type().getDisplayName(), Material.GRAY_DYE, 1,
                            "§7Find this item to add it to your",
                            "§7collection and unlock collection",
                            "§7rewards!");
                }
            };
        }

        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                new GUICollectionItem(item.type()).open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                List<String> lore = new ArrayList<>(List.of(
                        "§7View all your " + item.type().getDisplayName() + " Collection",
                        "§7progress and rewards!",
                        " "
                ));

                collection.getDisplay(lore, item);

                lore.add(" ");
                lore.add("§eClick to view!");

                return ItemStackCreator.getStack("§e" + item.type().getDisplayName(), item.type().material, 1, lore);
            }
        };
    }
}
