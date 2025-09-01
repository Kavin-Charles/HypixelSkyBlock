package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.storage;

import net.minestom.server.component.DataComponents;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.utility.MathUtility;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointBackpacks;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.BackpackComponent;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.atomic.AtomicInteger;

public class GUIStorageBackpackPage extends HypixelInventoryGUI {
    public int page;
    public int slots;
    public SkyBlockItem item;

    public GUIStorageBackpackPage(int page, SkyBlockItem item) {
        super(StringUtility.getTextFromComponent(new NonPlayerItemUpdater(item).getUpdatedItem().build()
                        .get(DataComponents.CUSTOM_NAME))
                        + " (Slot #" + page + ")",
                MathUtility.getFromSize(9 + item.getComponent(BackpackComponent.class).getRows() * 9));

        this.slots = item.getComponent(BackpackComponent.class).getRows() * 9;
        this.page = page;
        this.item = item;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) getPlayer();
        DatapointBackpacks.PlayerBackpacks data = player.getSkyblockDataHandler().get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.BACKPACKS, DatapointBackpacks.class).getValue();

        fill(ItemStackCreator.createNamedItemStack(Material.BLACK_STAINED_GLASS_PANE), 0, 8);

        set(GUIClickableItem.getCloseItem(0));
        set(GUIClickableItem.getGoBackItem(1, new GUIStorage()));

        if (page != data.getHighestBackpackSlot()) {
            set(new GUIClickableItem(8) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    new GUIStorageBackpackPage(data.getHighestBackpackSlot(),
                            new SkyBlockItem(data.getBackpacks().get(data.getHighestBackpackSlot()))
                    ).open(player);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStackCreator.getStackHead("§eLast Page >>",
                            "1ceb50d0d79b9fb790a7392660bc296b7ad2f856c5cbe1c566d99cfec191e668");
                }
            });

            if (data.getBackpacks().containsKey(page + 1))
                set(new GUIClickableItem(7) {
                    @Override
                    public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                        SkyBlockPlayer player = (SkyBlockPlayer) p;
                        new GUIStorageBackpackPage(page + 1, new SkyBlockItem(data.getBackpacks().get(page + 1))).open(player);
                    }

                    @Override
                    public ItemStack.Builder getItem(HypixelPlayer p) {
                        return ItemStackCreator.getStackHead("§aNext Page >>",
                                "848ca732a6e35dafd15e795ebc10efedd9ef58ff2df9b17af6e3d807bdc0708b");
                    }
                });
        }
        if (page != data.getLowestBackpackSlot()) {
            set(new GUIClickableItem(5) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    new GUIStorageBackpackPage(data.getLowestBackpackSlot(), new SkyBlockItem(data.getBackpacks().get(data.getLowestBackpackSlot()))).open(player);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStackCreator.getStackHead("§e< First Page",
                            "8af22a97292de001079a5d98a0ae3a82c427172eabc370ed6d4a31c7e3a0024f");
                }
            });

            if (data.getBackpacks().containsKey(page - 1))
                set(new GUIClickableItem(6) {
                    @Override
                    public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                        SkyBlockPlayer player = (SkyBlockPlayer) p;
                        new GUIStorageBackpackPage(page - 1, new SkyBlockItem(data.getBackpacks().get(page - 1))).open(player);
                    }

                    @Override
                    public ItemStack.Builder getItem(HypixelPlayer p) {
                        SkyBlockPlayer player = (SkyBlockPlayer) p;
                        return ItemStackCreator.getStackHead("§a< Previous Page",
                                "9c042597eda9f061794fe11dacf78926d247f9eea8ddef39dfbe6022989b8395");
                    }
                });
        }

        AtomicInteger slot = new AtomicInteger(9);
        item.getAttributeHandler().getBackpackData().items().forEach(item -> {
            set(new GUIItem(slot.getAndIncrement()) {
                @Override
                public boolean canPickup() {
                    return true;
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    if (item == null || new SkyBlockItem(item).isNA())
                        return ItemStackCreator.createNamedItemStack(Material.AIR);
                    return PlayerItemUpdater.playerUpdate(player, new SkyBlockItem(item).getItemStack());
                }
            });
        });

        updateItemStacks(getInventory(), player);
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
        item.getAttributeHandler().getBackpackData().items().clear();

        for (int i = 9; i < slots + 9; i++) {
            item.getAttributeHandler().getBackpackData().items().add(new SkyBlockItem(getInventory().getItemStack(i)).toUnderstandable());
        }

        ((SkyBlockPlayer) getPlayer()).getSkyblockDataHandler().get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.BACKPACKS, DatapointBackpacks.class).getValue().getBackpacks().put(page, item.toUnderstandable());
    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {
        item.getAttributeHandler().getBackpackData().items().clear();

        for (int i = 9; i < slots + 9; i++) {
            item.getAttributeHandler().getBackpackData().items().add(new SkyBlockItem(getInventory().getItemStack(i)).toUnderstandable());
        }

        ((SkyBlockPlayer) getPlayer()).getSkyblockDataHandler().get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.BACKPACKS, DatapointBackpacks.class).getValue().getBackpacks().put(page, item.toUnderstandable());
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {

    }
}
