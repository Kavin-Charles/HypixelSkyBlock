package net.swofty.type.skyblockgeneric.gui.inventories.bazaar.selections;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.item.ItemType;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIQueryItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GUIBazaarOrderAmountSelection extends HypixelInventoryGUI {
    private static final DecimalFormat F = new DecimalFormat("#,###.##");

    private final boolean isBuy;
    private final boolean isInstant;    // NEW: instant-buy vs limit-order
    private final ItemType itemType;
    private final int maxAmount;
    private final double unitPrice;     // for instant this includes fee; for limit this is bestAsk or bestBid
    private CompletableFuture<Integer> future;

    public GUIBazaarOrderAmountSelection(
            HypixelInventoryGUI previous,
            ItemType itemType,
            boolean isBuy,
            boolean isInstant,
            int maxAmount,
            double unitPrice
    ) {
        super(isInstant
                        ? (isBuy ? "Cobblestone → Instant Buy" : "Cobblestone → Instant Sell")
                        : (isBuy ? "How many do you want?" : "How many to sell?"),
                InventoryType.CHEST_4_ROW);

        this.isBuy = isBuy;
        this.isInstant = isInstant;
        this.itemType = itemType;
        this.maxAmount = maxAmount;
        this.unitPrice = unitPrice;

        fill(ItemStackCreator.createNamedItemStack(Material.BLACK_STAINED_GLASS_PANE));
        set(GUIClickableItem.getGoBackItem(31, previous));
    }

    public CompletableFuture<Integer> openAmountSelection(SkyBlockPlayer player) {
        future = new CompletableFuture<>();
        open(player);

        if (isInstant) {
            buildInstantUI(player);
        } else {
            buildLimitUI(player);
        }

        updateItemStacks(getInventory(), getPlayer());
        return future;
    }

    private void buildInstantUI(SkyBlockPlayer p) {
        // exactly the 4 buttons you already liked (One, Stack, All, Custom)
        addButton(9, 1, "One", "Buy one unit", 1, p);
        addButton(11, Math.min(64, maxAmount), "Stack",
                "Buy a stack!", Math.min(64, maxAmount), p);
        addButton(13, maxAmount, "All", "Fill my inventory!", maxAmount, p);
        addCustom(15, p);
    }

    private void buildLimitUI(SkyBlockPlayer p) {
        // matches your screenshots: “Buy a stack!”, “Buy a big stack!”, “Buy a thousand!”, “Custom Amount”
        int small  = Math.min(64, maxAmount);
        int medium = Math.min(160, maxAmount);
        int large  = Math.min(1024,maxAmount);

        addLimitButton(9, small, "Buy a stack!",      "Amount: " + small + "×", p);
        addLimitButton(11, medium, "Buy a big stack!","Amount: " + medium + "×", p);
        addLimitButton(13, large, "Buy a thousand!",  "Amount: " + large + "×", p);
        addCustom(15, p);
    }

    private void addButton(int slot, int qty, String title, String subtitle, int amount, SkyBlockPlayer p) {
        set(new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                future.complete(amount);
                player.closeInventory();
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                List<String> lore = new ArrayList<>();
                lore.add("§7" + subtitle);
                lore.add("§7Per unit: §6" + F.format(unitPrice));
                lore.add(isBuy
                        ? "§7Total cost: §6" + F.format(unitPrice * qty)
                        : "§7Total rev : §6" + F.format(unitPrice * qty));
                lore.add(" ");
                lore.add("§eClick to " + (isBuy ? "buy" : "sell") + " now!");
                return ItemStackCreator.getStack(
                        (isBuy ? "§a" : "§6") + title,
                        itemType.material, qty, lore
                );
            }
        });
    }

    private void addLimitButton(int slot, int qty, String title, String amountLine, SkyBlockPlayer p) {
        set(new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                future.complete(qty);
                // *don’t* close—so your price‐selection GUI will open next
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                List<String> lore = new ArrayList<>();
                lore.add("§7Buy Order Setup");
                lore.add("§7" + amountLine);
                lore.add("§7Per unit: §6" + F.format(unitPrice));
                lore.add(" ");
                lore.add("§eClick to proceed!");
                return ItemStackCreator.getStack(
                        "§b" + title,
                        slot == 13 ? Material.CHEST : itemType.material,
                        qty, lore
                );
            }
        });
    }

    private void addCustom(int slot, SkyBlockPlayer p) {
        set(new GUIQueryItem(slot) {
            @Override
            public HypixelInventoryGUI onQueryFinish(String q, HypixelPlayer pl) {
                try {
                    int v = Integer.parseInt(q);
                    if (v < 1 || v > maxAmount) {
                        pl.sendMessage("§cEnter 1–" + maxAmount);
                        return null;
                    }
                    future.complete(v);
                } catch (NumberFormatException ex) {
                    pl.sendMessage("§cInvalid number");
                }
                return null;
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                List<String> lore = new ArrayList<>();
                lore.add(isInstant
                        ? "§7Type a custom amount"
                        : "§7Buy Order Quantity");
                lore.add("§7Up to " + maxAmount + "×");
                lore.add(" ");
                lore.add("§eClick to specify!");
                return ItemStackCreator.getStack(
                        "§eCustom Amount",
                        Material.OAK_SIGN, 1, lore
                );
            }
        });
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
        if (!future.isDone()) future.complete(0);
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }
}
