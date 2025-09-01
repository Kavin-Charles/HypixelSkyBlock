package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.levels.emblem;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.HypixelPaginatedGUI;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.utility.PaginationList;
import net.swofty.type.skyblockgeneric.levels.SkyBlockEmblems;
import net.swofty.type.skyblockgeneric.levels.abstr.CauseEmblem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIEmblem extends HypixelPaginatedGUI<SkyBlockEmblems.SkyBlockEmblem> {
    private final SkyBlockEmblems emblemCategory;

    public GUIEmblem(SkyBlockEmblems emblemCategory) {
        super(InventoryType.CHEST_5_ROW);

        this.emblemCategory = emblemCategory;
    }

    @Override
    public boolean allowHotkeying() {
        return false;
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
        };
    }

    @Override
    public PaginationList<SkyBlockEmblems.SkyBlockEmblem> fillPaged(HypixelPlayer player, PaginationList<SkyBlockEmblems.SkyBlockEmblem> paged) {
        paged.addAll(emblemCategory.getEmblems());
        return paged;
    }

    @Override
    public boolean shouldFilterFromSearch(String query, SkyBlockEmblems.SkyBlockEmblem item) {
        return !item.displayName().toLowerCase().contains(query.toLowerCase());
    }

    @Override
    public void performSearch(HypixelPlayer player, String query, int page, int maxPage) {
        border(ItemStackCreator.createNamedItemStack(Material.BLACK_STAINED_GLASS_PANE, ""));
        set(GUIClickableItem.getCloseItem(40));
        set(createSearchItem(this, 41, query));
        set(GUIClickableItem.getGoBackItem(39, new GUIEmblems()));

        if (page > 1) {
            set(createNavigationButton(this, 36, query, page, false));
        }
        if (page < maxPage) {
            set(createNavigationButton(this, 44, query, page, true));
        }
    }

    @Override
    public String getTitle(HypixelPlayer player, String query, int page, PaginationList<SkyBlockEmblems.SkyBlockEmblem> paged) {
        return "Emblems - " + emblemCategory.toString() + " (" + page + "/" + paged.getPageCount() + ")";
    }

    @Override
    public GUIClickableItem createItemFor(SkyBlockEmblems.SkyBlockEmblem item, int slot, HypixelPlayer player) {
        boolean unlocked = ((SkyBlockPlayer) player).hasUnlockedXPCause(item.cause());
        CauseEmblem causeEmblem = (CauseEmblem) item.cause();

        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                if (!unlocked) {
                    player.sendMessage("§cYou have not unlocked this emblem yet!");
                    return;
                }

                player.getSkyBlockExperience().setEmblem(emblemCategory, item);
                player.sendMessage("§aYou have selected the " + item.displayName() + " emblem!");
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                String name = (unlocked ? "§a" : "§c") + item.displayName() + " " + item.emblem();

                List<String> lore;
                if (unlocked) {
                    lore = new ArrayList<>(List.of(
                            "§8" + causeEmblem.emblemEisplayName(),
                            " ",
                            "§7Preview: " + player.getFullDisplayName(item),
                            " ",
                            "§eClick to select!"
                    ));
                } else {
                    lore = new ArrayList<>(List.of(
                            "§8Locked",
                            " ",
                            "§c" + causeEmblem.getEmblemRequiresMessage()
                    ));
                }

                return ItemStackCreator.getStack(name,
                        unlocked ? item.displayMaterial() : Material.GRAY_DYE,
                        1, lore);
            }
        };
    }
}
