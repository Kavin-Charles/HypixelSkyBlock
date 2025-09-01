package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bags;

import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.collection.CustomCollectionAward;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointQuiver;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;

public class GUIQuiver extends HypixelInventoryGUI {
    private static final Map<CustomCollectionAward, Integer> SLOTS_PER_UPGRADE = Map.of(
            CustomCollectionAward.QUIVER, 18,
            CustomCollectionAward.QUIVER_UPGRADE_1, 9,
            CustomCollectionAward.QUIVER_UPGRADE_2, 9
    );

    private int slotToSaveUpTo;

    public GUIQuiver() {
        super("Quiver", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        fill(ItemStackCreator.createNamedItemStack(Material.BLACK_STAINED_GLASS_PANE));
        set(GUIClickableItem.getCloseItem(40));
        set(GUIClickableItem.getGoBackItem(39, new GUIYourBags()));

        int amountOfSlots = 0;
        int rawAmountOfSlots = 0;

        for (Map.Entry<CustomCollectionAward, Integer> entry : SLOTS_PER_UPGRADE.entrySet()) {
            if (player.hasCustomCollectionAward(entry.getKey())) {
                amountOfSlots += entry.getValue();
            } else {
                for (int i = 0; i < entry.getValue(); i++) {
                    set(new GUIItem(i + rawAmountOfSlots) {
                        @Override
                        public ItemStack.Builder getItem(HypixelPlayer p) {
                            return ItemStackCreator.getStack("§cLocked", Material.RED_STAINED_GLASS_PANE,
                                    1,
                                    "§7You must have the §a" + entry.getKey().getDisplay() + " §7upgrade",
                                    "§7to unlock this slot.");
                        }
                    });
                }
            }
            rawAmountOfSlots += entry.getValue();
        }

        slotToSaveUpTo = amountOfSlots;
        DatapointQuiver.PlayerQuiver quiver = player.getQuiver();

        for (int i = 0; i < amountOfSlots; i++) {
            SkyBlockItem item = quiver.getInSlot(i);
            set(new GUIClickableItem(i) {

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    if (item == null) {
                        return ItemStack.builder(Material.AIR);
                    } else {
                        return PlayerItemUpdater.playerUpdate(player, item.getItemStack());
                    }
                }

                @Override
                public boolean canPickup() {
                    return true;
                }

                @Override
                public void runPost(InventoryClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    save(player, slotToSaveUpTo);
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                }
            });
        }

        updateItemStacks(e.inventory(), e.player());
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        SkyBlockItem cursorItem = new SkyBlockItem(e.getPlayer().getInventory().getCursorItem());
        SkyBlockItem clickedItem = new SkyBlockItem(e.getClickedItem());

        if (isItemAllowed(cursorItem) && isItemAllowed(clickedItem)) {
            save((SkyBlockPlayer) getPlayer(), slotToSaveUpTo);
            return;
        }

        e.setCancelled(true);
        getPlayer().sendMessage("§cYou cannot put this item in the Quiver!");
    }

    public boolean isItemAllowed(SkyBlockItem item) {
        if (item.isNA()) return true;
        if (item.getMaterial().equals(Material.AIR)) return true;

        return item.getMaterial() == Material.ARROW;
    }

    public void save(SkyBlockPlayer player, int slotToSaveUpTo) {
        DatapointQuiver.PlayerQuiver quiver = player.getQuiver();
        for (int i = 0; i < slotToSaveUpTo; i++) {
            SkyBlockItem item = new SkyBlockItem(getInventory().getItemStack(i));
            if (item.isNA()) {
                quiver.getQuiverMap().remove(i);
            } else {
                quiver.getQuiverMap().put(i, item);
            }
        }
    }
}
