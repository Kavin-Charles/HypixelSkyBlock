package net.swofty.type.skyblockgeneric.gui.inventories;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.item.ItemType;
import net.swofty.type.generic.gui.inventory.HypixelPaginatedGUI;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.utility.PaginationList;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.TrackedUniqueComponent;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GUICreative extends HypixelPaginatedGUI<SkyBlockItem> {

    public GUICreative() {
        super(InventoryType.CHEST_6_ROW);
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
    public PaginationList<SkyBlockItem> fillPaged(HypixelPlayer player, PaginationList<SkyBlockItem> paged) {
        paged.addAll(Arrays.stream(ItemType.values()).map(SkyBlockItem::new).toList());

        List<SkyBlockItem> vanilla = new ArrayList<>(Material.values().stream().map(SkyBlockItem::new).toList());
        vanilla.removeIf((element) -> ItemType.isVanillaReplaced(element.getAttributeHandler().getTypeAsString()));
        paged.addAll(vanilla);

        return paged;
    }

    @Override
    public boolean shouldFilterFromSearch(String query, SkyBlockItem item) {
        return !item.getAttributeHandler().getTypeAsString().toLowerCase().contains(query.replaceAll(" ", "_").toLowerCase());
    }

    @Override
    protected void performSearch(HypixelPlayer player, String query, int page, int maxPage) {
        border(ItemStackCreator.createNamedItemStack(Material.BLACK_STAINED_GLASS_PANE, ""));
        set(GUIClickableItem.getCloseItem(50));
        set(createSearchItem(this, 48, query));

        if (page > 1) {
            set(createNavigationButton(this, 45, query, page, false));
        }
        if (page < maxPage) {
            set(createNavigationButton(this, 53, query, page, true));
        }
    }

    @Override
    public String getTitle(HypixelPlayer player, String query, int page, PaginationList<SkyBlockItem> paged) {
        return "Creative Menu | Page " + page + "/" + paged.getPageCount();
    }

    @Override
    protected GUIClickableItem createItemFor(SkyBlockItem skyBlockItem, int slot, HypixelPlayer p) {
        SkyBlockPlayer player = (SkyBlockPlayer) p;
        ItemStack.Builder itemStack = PlayerItemUpdater.playerUpdate(
                player, skyBlockItem.getItemStack()
        );

        boolean stackable = !(skyBlockItem.hasComponent(TrackedUniqueComponent.class));

        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.playSound(Sound.sound(Key.key("block.note_block.pling"), Sound.Source.PLAYER, 1.0f, 2.0f));

                if (e.getClick() instanceof Click.Right && stackable) {
                    skyBlockItem.setAmount(64);
                    player.addAndUpdateItem(skyBlockItem);
                    player.playSound(Sound.sound(Key.key("block.note_block.pling"), Sound.Source.PLAYER, 1.0f, 2.0f));
                    player.sendMessage(Component.text("§aYou have been given §7x64 " + skyBlockItem.getDisplayName() + "§a."));
                } else {
                    skyBlockItem.setAmount(1);
                    player.addAndUpdateItem(skyBlockItem);
                    player.playSound(Sound.sound(Key.key("block.note_block.pling"), Sound.Source.PLAYER, 1.0f, 2.0f));
                    player.sendMessage(Component.text("§aYou have been given §7x1 " + skyBlockItem.getDisplayName() + "§a."));
                }
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                ArrayList<String> lore = new ArrayList<>(skyBlockItem.getLore());
                lore.add(" ");
                if (stackable) lore.add("§bRight Click for §7x64 §eof this item.");
                lore.add("§eLeft Click for §7x1 §eof this item.");

                return ItemStackCreator.updateLore(itemStack, lore);
            }
        };
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
