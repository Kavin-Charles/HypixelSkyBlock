package net.swofty.type.skyblockgeneric.gui.inventories;

import net.minestom.server.component.DataComponents;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.AnvilCombinableComponent;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIAnvil extends HypixelInventoryGUI {

    private final int[] borderSlots = {
            45, 46, 47, 48, 50, 51, 52, 53
    };

    private final int upgradeItemSlot = 29;
    private final int[] upgradeItemSlots = {
            11, 12, 20
    };

    private final int sacrificeItemSlot = 33;
    private final int[] sacrificeItemSlots = {
            14, 15, 24
    };

    private final int resultSlot = 13;

    public GUIAnvil() {
        super("Anvil", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(Material.BLACK_STAINED_GLASS_PANE, "");

        fill(ItemStackCreator.createNamedItemStack(Material.RED_STAINED_GLASS_PANE, ""), 45, 53);

        set(GUIClickableItem.getCloseItem(49));
        set(new GUIItem(resultSlot) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return ItemStackCreator.getStack("§cAnvil", Material.BARRIER, 1, "§7Place a target item in the left slot", "§7and a sacrifice item in the right slot", "§7to combine them!");
            }
        });

        set(new GUIItem(22) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return ItemStackCreator.getStack("§aCombine Items", Material.ANVIL, 1, "§7Combine the items in the slots to the", "§7left and right below.");
            }
        });

        updateItemToUpgrade(null);
        updateItemToSacrifice(null);

        updateItemToCraft();
    }

    public void updateItemToUpgrade(SkyBlockItem item) {
        if (item == null) {
            set(new GUIClickableItem(upgradeItemSlot) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    ItemStack stack = p.getInventory().getCursorItem();

                    if (stack.get(DataComponents.CUSTOM_NAME) == null) {
                        updateItemToUpgrade(null);
                        return;
                    }

                    giveResult((Inventory) e.getInventory(), player);

                    SkyBlockItem item = new SkyBlockItem(stack);
                    updateItemToUpgrade(item);
                }

                @Override
                public boolean canPickup() {
                    return true;
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    return ItemStack.builder(Material.AIR);
                }
            });
            updateItemStacks(getInventory(), getPlayer());

            updateItemToCraft();
            return;
        }

        set(new GUIClickableItem(upgradeItemSlot) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return PlayerItemUpdater.playerUpdate(player, item.getItemStack());
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                ItemStack stack = e.getClickedItem();

                if (stack.isAir()) return;

                updateItemToUpgrade(null);
                player.addAndUpdateItem(stack);
            }
        });
        updateItemStacks(getInventory(), getPlayer());

        updateItemToCraft();
    }

    public void updateItemToUpgradeValid(Material material) {
        ItemStack.Builder stack = ItemStackCreator.getStack("§6Item to Upgrade", material, 1, "§7The item you want to upgrade should", "§7be placed in the slot on this side.");
        for (int i : upgradeItemSlots) {
            set(i, stack);
        }
    }

    public void updateItemToSacrifice(SkyBlockItem item) {
        if (item == null) {
            set(new GUIClickableItem(sacrificeItemSlot) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    ItemStack stack = p.getInventory().getCursorItem();

                    if (stack.get(DataComponents.CUSTOM_NAME) == null) {
                        updateItemToSacrifice(null);
                        return;
                    }

                    giveResult((Inventory) e.getInventory(), player);

                    SkyBlockItem item = new SkyBlockItem(stack);
                    updateItemToSacrifice(item);
                }

                @Override
                public boolean canPickup() {
                    return true;
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    return ItemStack.builder(Material.AIR);
                }
            });

            updateItemStacks(getInventory(), getPlayer());

            updateItemToCraft();
            return;
        }

        set(new GUIClickableItem(sacrificeItemSlot) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return PlayerItemUpdater.playerUpdate(player, item.getItemStack());
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                ItemStack stack = e.getClickedItem();

                if (stack.isAir()) return;

                updateItemToSacrifice(null);
                player.addAndUpdateItem(stack);
            }
        });

        updateItemStacks(getInventory(), getPlayer());

        updateItemToCraft();
    }

    public void updateItemToSacrificeValid(Material material) {
        ItemStack.Builder stack = ItemStackCreator.getStack("§6Item to Sacrifice", material, 1, "§7The item you are sacrificing in order", "§7to upgrade the item on the left", "§7should be placed in the slot on this", "§7side.");
        for (int i : sacrificeItemSlots) {
            set(i, stack);
        }
    }

    public void updateItemToCraft() {
        SkyBlockItem upgradeItem = new SkyBlockItem(getInventory().getItemStack(upgradeItemSlot));
        SkyBlockItem sacrificeItem = new SkyBlockItem(getInventory().getItemStack(sacrificeItemSlot));

        boolean isUpgradeItemValid = !(upgradeItem.isAir() || upgradeItem.isNA());
        boolean isSacrificeItemValid = !(sacrificeItem.isAir() || sacrificeItem.isNA());

        boolean canCraft = isUpgradeItemValid && isSacrificeItemValid && (sacrificeItem.hasComponent(AnvilCombinableComponent.class)) &&
                sacrificeItem.getComponent(AnvilCombinableComponent.class).canApply((SkyBlockPlayer) getPlayer(), upgradeItem, sacrificeItem);

        updateItemToSacrificeValid(canCraft || (isSacrificeItemValid && !isUpgradeItemValid) ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        updateItemToUpgradeValid(canCraft || (!isSacrificeItemValid && isUpgradeItemValid) ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);

        if (canCraft) {
            border(ItemStackCreator.createNamedItemStack(Material.LIME_STAINED_GLASS_PANE));
        } else {
            border(ItemStackCreator.createNamedItemStack(Material.RED_STAINED_GLASS_PANE));
        }

        if (!canCraft) {
            if (isUpgradeItemValid && isSacrificeItemValid) {
                set(new GUIItem(13) {
                    @Override
                    public ItemStack.Builder getItem(HypixelPlayer p) {
                        SkyBlockPlayer player = (SkyBlockPlayer) p;
                        return ItemStackCreator.getStack("§cError!", Material.BARRIER, 1, "§7You can not combine those Items");
                    }
                });
            } else {
                set(new GUIItem(13) {
                    @Override
                    public ItemStack.Builder getItem(HypixelPlayer p) {
                        SkyBlockPlayer player = (SkyBlockPlayer) p;
                        return ItemStackCreator.getStack("§cAnvil", Material.BARRIER, 1, "§7Place a target item in the left slot", "§7and a sacrifice item in the right slot", "§7to combine them!");
                    }
                });
            }

            set(new GUIItem(22) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    return ItemStackCreator.getStack("§aCombine Items", Material.ANVIL, 1, "§7Combine the items in the slots to the", "§7left and right below.");
                }
            });

            updateItemStacks(getInventory(), getPlayer());
            return;
        }

        SkyBlockItem result = new SkyBlockItem(getInventory().getItemStack(upgradeItemSlot));

        sacrificeItem.getComponent(AnvilCombinableComponent.class).apply(result, sacrificeItem);

        set(new GUIItem(13) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return PlayerItemUpdater.playerUpdate(player, result.getItemStack());
            }
        });

        int levelCost = sacrificeItem.getComponent(AnvilCombinableComponent.class).applyCostLevels(
                upgradeItem,
                sacrificeItem,
                (SkyBlockPlayer) getPlayer());

        List<String> lore = new ArrayList<>();
        lore.add("§7Combine the items in the slots to the");
        lore.add("§7left and right below.");

        if (levelCost > 0) {
            lore.add("");
            lore.add("§7Cost");
            lore.add("§9" + levelCost + " Exp Levels");
        }

        lore.add("");
        lore.add("§eClick to combine!");
        ItemStack.Builder applyItemStack = ItemStackCreator.getStack("§aCombine Items", Material.ANVIL, 1, lore);

        set(new GUIClickableItem(22) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                craftResult(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return applyItemStack;
            }
        });

        updateItemStacks(getInventory(), getPlayer());
    }

    public void craftResult(SkyBlockPlayer player) {
        SkyBlockItem sacrificeItem = new SkyBlockItem(getInventory().getItemStack(sacrificeItemSlot));
        int requiredLevels = sacrificeItem.getComponent(AnvilCombinableComponent.class).applyCostLevels(
                new SkyBlockItem(getInventory().getItemStack(upgradeItemSlot)),
                sacrificeItem,
                player
        );

        if (player.getLevel() < requiredLevels) {
            player.sendMessage("§cYou don't have enough Experience Levels!");
            return;
        }

        player.setLevel(player.getLevel() - requiredLevels);
        SkyBlockItem result = new SkyBlockItem(getInventory().getItemStack(resultSlot));

        updateItemToUpgrade(null);
        updateItemToSacrifice(null);

        set(new GUIItem(22) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return ItemStackCreator.getStack("§aAnvil", Material.OAK_SIGN, 1, "§7Claim the result item above!");
            }
        });

        set(new GUIClickableItem(resultSlot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                giveResult((Inventory) e.getInventory(), player);
                new GUIAnvil().open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return PlayerItemUpdater.playerUpdate(player, result.getItemStack());
            }
        });

        updateItemStacks(getInventory(), getPlayer());
    }

    public void giveResult(Inventory inventory, SkyBlockPlayer player) {
        if (get(resultSlot) instanceof GUIClickableItem) {
            player.addAndUpdateItem(new SkyBlockItem(inventory.getItemStack(resultSlot)));

            set(new GUIItem(resultSlot) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    return ItemStackCreator.getStack("§cAnvil", Material.BARRIER, 1, "§7Place a target item in the left slot", "§7and a sacrifice item in the right slot", "§7to combine them!");
                }
            });
        }
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
        ((SkyBlockPlayer) e.getPlayer()).addAndUpdateItem(new SkyBlockItem(e.getInventory().getItemStack(sacrificeItemSlot)));
        ((SkyBlockPlayer) e.getPlayer()).addAndUpdateItem(new SkyBlockItem(e.getInventory().getItemStack(upgradeItemSlot)));

        giveResult((Inventory) e.getInventory(), (SkyBlockPlayer) e.getPlayer());
    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer p) {
        SkyBlockPlayer player = (SkyBlockPlayer) p;
        player.addAndUpdateItem(new SkyBlockItem(inventory.getItemStack(sacrificeItemSlot)));
        player.addAndUpdateItem(new SkyBlockItem(inventory.getItemStack(upgradeItemSlot)));

        giveResult(inventory, player);
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {

    }

    @Override
    public void border(ItemStack.Builder stack) {
        for (int i : borderSlots) {
            set(i, stack);
        }
        updateItemStacks(getInventory(), getPlayer());
    }
}
