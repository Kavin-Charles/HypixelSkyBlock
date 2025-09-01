package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bags;

import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.event.inventory.InventoryCloseEvent;
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
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAccessoryBag;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.AccessoryComponent;
import net.swofty.type.skyblockgeneric.item.components.TieredTalismanComponent;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelCause;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class GUIAccessoryBag extends HypixelInventoryGUI {
    private static final SortedMap<CustomCollectionAward, Integer> SLOTS_PER_UPGRADE = new TreeMap<>(Map.of(
            CustomCollectionAward.ACCESSORY_BAG, 3,
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_1, 9,
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_2, 15,
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_3, 21,
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_4, 27,
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_5, 33,
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_6, 39,
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_7, 45,
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_8, 51,
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_9, 57
    ));

    @Setter
    private int page = 1;
    private int slotToSaveUpTo;

    public GUIAccessoryBag() {
        super("Accessory Bag", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStackCreator.createNamedItemStack(Material.BLACK_STAINED_GLASS_PANE));
        set(GUIClickableItem.getCloseItem(49));
        set(GUIClickableItem.getGoBackItem(48, new GUIYourBags()));

        SkyBlockPlayer player = (SkyBlockPlayer) e.player();

        int totalSlots = getTotalSlots(player);
        int slotsPerPage = 45;
        int totalPages = (int) Math.ceil((double) totalSlots / slotsPerPage);

        getInventory().setTitle(Component.text("Accessory Bag (" + page + "/" + totalPages + ")"));

        int startIndex = (page - 1) * slotsPerPage;
        int endSlot = Math.min(totalSlots - startIndex, slotsPerPage);
        this.slotToSaveUpTo = endSlot;

        for (int i = 0; i < endSlot; i++) {
            SkyBlockItem item = player.getAccessoryBag().getInSlot(i + startIndex);

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
                    save(player);
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                }
            });
        }

        for (int i = endSlot; i < slotsPerPage; i++) {
            int slotIndex = i + startIndex;
            CustomCollectionAward nextUpgrade = getUpgradeNeededForSlotIndex(slotIndex);
            if (nextUpgrade != null) {
                set(new GUIItem(i) {
                    @Override
                    public ItemStack.Builder getItem(HypixelPlayer p) {
                        SkyBlockPlayer player = (SkyBlockPlayer) p;
                        return ItemStackCreator.getStack("§cLocked", Material.RED_STAINED_GLASS_PANE,
                                1,
                                "§7You need to unlock the",
                                "§a" + nextUpgrade.getDisplay() + " §7upgrade",
                                "§7to use this slot.");
                    }
                });
            }
        }

        if (page > 1) {
            set(new GUIClickableItem(45) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    return ItemStackCreator.getStack("§aPrevious Page", Material.ARROW, 1);
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    GUIAccessoryBag gui = new GUIAccessoryBag();
                    gui.setPage(page - 1);
                    gui.open(player);
                }
            });
        }

        if (page < totalPages) {
            set(new GUIClickableItem(53) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    return ItemStackCreator.getStack("§aNext Page", Material.ARROW, 1);
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    GUIAccessoryBag gui = new GUIAccessoryBag();
                    gui.setPage(page + 1);
                    gui.open(player);
                }
            });
        }

        updateItemStacks(e.inventory(), e.player());
    }

    @Override
    public boolean allowHotkeying() {
        return true;
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
        save((SkyBlockPlayer) getPlayer());
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        SkyBlockItem cursorItem = new SkyBlockItem(e.getPlayer().getInventory().getCursorItem());
        SkyBlockItem clickedItem = new SkyBlockItem(e.getClickedItem());

        if (isItemAllowed(cursorItem) && isItemAllowed(clickedItem)) {
            save((SkyBlockPlayer) getPlayer());
            return;
        }

        e.setCancelled(true);
        getPlayer().sendMessage("§cYou cannot put this item in the Accessory Bag!");
        save((SkyBlockPlayer) getPlayer());
    }

    private int getTotalSlots(SkyBlockPlayer player) {
        int totalSlots = 0;
        for (CustomCollectionAward entry : SLOTS_PER_UPGRADE.keySet()) {
            if (player.hasCustomCollectionAward(entry)) {
                totalSlots = Math.max(totalSlots, SLOTS_PER_UPGRADE.get(entry));
            }
        }
        return totalSlots;
    }

    private CustomCollectionAward getUpgradeNeededForSlotIndex(int slotIndex) {
        for (CustomCollectionAward entry : SLOTS_PER_UPGRADE.keySet()) {
            if (slotIndex < SLOTS_PER_UPGRADE.get(entry)) {
                return entry;
            }
        }
        return null;
    }

    private void save(SkyBlockPlayer player) {
        DatapointAccessoryBag.PlayerAccessoryBag accessoryBag = player.getAccessoryBag();
        for (int i = 0; i < this.slotToSaveUpTo; i++) {
            int slot = i + ((page - 1) * 45);
            SkyBlockItem item = new SkyBlockItem(getInventory().getItemStack(i));
            if (item.isNA() || item.getMaterial() == Material.AIR) {
                accessoryBag.removeFromSlot(slot);
            } else {
                accessoryBag.setInSlot(slot, item);
            }
        }

        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.ACCESSORY_BAG, DatapointAccessoryBag.class).setValue(
                accessoryBag
        );
    }

    public boolean isItemAllowed(SkyBlockItem item) {
        if (item.isNA()) return true;
        if (item.getMaterial().equals(Material.AIR)) return true;

        SkyBlockPlayer player = (SkyBlockPlayer) getPlayer();
        if (item.hasComponent(AccessoryComponent.class) || item.hasComponent(TieredTalismanComponent.class)) {
            DatapointAccessoryBag.PlayerAccessoryBag accessoryBag = player.getAccessoryBag();
            accessoryBag.addDiscoveredAccessory(item.getAttributeHandler().getPotentialType());

            player.getSkyBlockExperience().addExperience(
                    SkyBlockLevelCause.getAccessoryCause(item.getAttributeHandler().getPotentialType())
            );

            return true;
        } else {
            return false;
        }
    }
}