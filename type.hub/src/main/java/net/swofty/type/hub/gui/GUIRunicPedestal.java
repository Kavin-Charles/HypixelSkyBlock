package net.swofty.type.hub.gui;

import net.minestom.server.component.DataComponents;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.item.ItemType;
import net.swofty.commons.item.Rarity;
import net.swofty.commons.item.attribute.attributes.ItemAttributeRuneInfusedWith;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.RuneComponent;
import net.swofty.type.skyblockgeneric.item.components.RuneableComponent;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.swofty.type.generic.user.HypixelPlayer;

public class GUIRunicPedestal extends HypixelInventoryGUI {
    private static final int MAX_RUNE_LEVEL = 3;
    private static final int[] BOTTOM_SLOTS = {45, 46, 47, 48, 50, 51, 52, 53};
    private static final int[] LEFT_RUNIC_SLOTS = {10, 11, 12};
    private static final int[] RIGHT_RUNIC_SLOTS = {14, 15, 16};
    private static final int[] CONNECTOR_RUNIC_SLOTS = {22};

    private SkyBlockItem itemOnLeft = null;
    private SkyBlockItem itemOnRight = null;
    private SkyBlockItem outputItem = null;
    private boolean isFusing = false;
    private CompletableFuture<Boolean> fusingAnimation = null;

    public GUIRunicPedestal() {
        super("Runic Pedestal", InventoryType.CHEST_6_ROW);
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(Material.BLACK_STAINED_GLASS_PANE, "");
        setGlassPanes(BOTTOM_SLOTS, Material.WHITE_STAINED_GLASS_PANE);
        setGlassPanes(LEFT_RUNIC_SLOTS, Material.WHITE_STAINED_GLASS_PANE);
        setGlassPanes(RIGHT_RUNIC_SLOTS, Material.WHITE_STAINED_GLASS_PANE);
        setGlassPanes(CONNECTOR_RUNIC_SLOTS, Material.WHITE_STAINED_GLASS_PANE);
        set(GUIClickableItem.getCloseItem(49));

        set(new GUIClickableItem(44) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p; 
                new GUIRuneRemoval().open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p; 
                return ItemStackCreator.getStack(
                        "§eRune Removal", Material.CAULDRON, 1,
                        "§7Sometimes, simplicity is most beautiful."
                );
            }
        });
        set(GUIClickableItem.getCloseItem(49));

        updateFromItem(null, null);
    }

    public void updateFromItem(SkyBlockItem itemPutOnLeft, SkyBlockItem itemPutOnRight) {
        this.itemOnLeft = itemPutOnLeft;
        this.itemOnRight = itemPutOnRight;

        setGlassPanes(BOTTOM_SLOTS, Material.WHITE_STAINED_GLASS_PANE);
        setGlassPanes(LEFT_RUNIC_SLOTS, Material.WHITE_STAINED_GLASS_PANE);
        setGlassPanes(RIGHT_RUNIC_SLOTS, Material.WHITE_STAINED_GLASS_PANE);
        setGlassPanes(CONNECTOR_RUNIC_SLOTS, Material.WHITE_STAINED_GLASS_PANE);

        if (itemOnLeft == null) {
            set(new GUIClickableItem(19) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    ItemStack stack = p.getInventory().getCursorItem();

                    if (stack.get(DataComponents.CUSTOM_NAME) == null) {
                        updateFromItem(new SkyBlockItem(stack), itemOnRight);
                        return;
                    }

                    updateFromItem(new SkyBlockItem(stack), itemOnRight);
                }

                public boolean canPickup() {
                    return true;
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStack.builder(Material.AIR);
                }
            });
        } else {
            setGlassPanes(LEFT_RUNIC_SLOTS, Material.PURPLE_STAINED_GLASS_PANE);
            set(new GUIClickableItem(19) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    if (isFusing) {
                        player.sendMessage("§cYou cannot remove the item on the left while fusing!");
                        return;
                    }

                    player.addAndUpdateItem(itemOnLeft);
                    updateFromItem(null, itemOnRight);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    return PlayerItemUpdater.playerUpdate(player, itemOnLeft.getItemStack());
                }
            });
        }

        if (itemOnRight == null) {
            set(new GUIClickableItem(25) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    ItemStack stack = p.getInventory().getCursorItem();

                    if (stack.get(DataComponents.CUSTOM_NAME) == null) {
                        updateFromItem(itemOnLeft, new SkyBlockItem(stack));
                        return;
                    }

                    updateFromItem(itemOnLeft, new SkyBlockItem(stack));
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
        } else {
            setGlassPanes(RIGHT_RUNIC_SLOTS, Material.PURPLE_STAINED_GLASS_PANE);
            set(new GUIClickableItem(25) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p; 
                    if (isFusing) {
                        player.sendMessage("§cYou cannot remove the item on the right while fusing!");
                        return;
                    }

                    player.addAndUpdateItem(itemOnRight);
                    updateFromItem(itemOnLeft, null);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p; 
                    return PlayerItemUpdater.playerUpdate(player, itemOnRight.getItemStack());
                }
            });
        }

        if (itemOnLeft == null || itemOnLeft.isAir()
                || itemOnRight == null || itemOnRight.isAir()) {
            set(new GUIItem(13) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p; 
                    return ItemStackCreator.getStack(
                            "§aApply a Rune or Fuse Two Runes", Material.END_PORTAL_FRAME, 1,
                            "§7Add the rune to your provided item",
                            "§7or provide two runes to attempt to",
                            "§7fuse them."
                    );
                }
            });
            set(new GUIItem(31) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p; 
                    return ItemStackCreator.getStack(
                            "§cRunic Pedestal", Material.BARRIER, 1,
                            "§7Place a target item in the left slot",
                            "§7and a sacrifice rune in the right slot",
                            "§7to add the two runes effect's to the item",
                            "§7or add two runes to attempt to fuse",
                            "§7them!"
                    );
                }
            });
            updateItemStacks(getInventory(), getPlayer());
            return;
        }

        RunicMerge runicMerge = null;
        for (RunicMerge merge : RunicMerge.getMerges()) {
            if (merge.isValidType(itemOnLeft, itemOnRight)) {
                runicMerge = merge;
                break;
            }
        }

        if (runicMerge == null) {
            set(new GUIItem(13) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p; 
                    return ItemStackCreator.getStack(
                            "§aApply a Rune or Fuse Two Runes", Material.END_PORTAL_FRAME, 1,
                            "§7Add the rune to your provided item",
                            "§7or provide two runes to attempt to",
                            "§7fuse them."
                    );
                }
            });
            set(new GUIItem(31) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p; 
                    return ItemStackCreator.getStack(
                            "§cError!", Material.BARRIER, 1,
                            "§7You cannot combine those items!"
                    );
                }
            });
        } else {
            RunicMerge finalRunicMerge = runicMerge;
            setGlassPanes(BOTTOM_SLOTS, Material.PURPLE_STAINED_GLASS_PANE);
            setGlassPanes(CONNECTOR_RUNIC_SLOTS, Material.PURPLE_STAINED_GLASS_PANE);

            set(new GUIClickableItem(13) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p; 
                    if (isFusing) {
                        player.sendMessage("§cYou cannot apply a rune while fusing!");
                        return;
                    }

                    isFusing = true;
                    outputItem = finalRunicMerge.merge(itemOnLeft, itemOnRight);
                    itemOnLeft = null;
                    itemOnRight = null;

                    startFusingAnimation().thenAccept((hasDisconnected) -> {
                        if (hasDisconnected)
                            return;

                        isFusing = false;
                        updateFromItem(null, null);
                        player.addAndUpdateItem(outputItem);
                        finalRunicMerge.onSuccess(player, outputItem);
                        outputItem = null;
                    });
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p; 
                    return ItemStackCreator.getStack(
                            "§aCombine Items", Material.END_PORTAL_FRAME, 1,
                            "§7Combine the provided items.",
                            " ",
                            "§eClick to combine!"
                    );
                }
            });
            set(new GUIClickableItem(31) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p; 
                    player.sendMessage("§cYou must click the Portal Frame above to combine the two items!");
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p; 
                    SkyBlockItem mergedItem = finalRunicMerge.merge(itemOnLeft, itemOnRight);
                    ArrayList<String> lore = new ArrayList<>(mergedItem.getLore());
                    lore.add("§8§m-------------------");
                    lore.add("§aThis is the item you will get.");
                    lore.add("§aClick the §cPORTAL FRAME ABOVE §ato");
                    lore.add("§acombine.");

                    return ItemStackCreator.getStack(
                            mergedItem.getDisplayName(), mergedItem.getMaterial(), 1,
                            lore.toArray(new String[0])
                    );
                }
            });
        }

        updateItemStacks(getInventory(), getPlayer());
    }

    public static abstract class RunicMerge {
        public abstract SkyBlockItem merge(SkyBlockItem itemOnLeft, SkyBlockItem itemOnRight);
        public abstract boolean isValidType(SkyBlockItem itemOnLeft, SkyBlockItem itemOnRight);
        public abstract void onSuccess(SkyBlockPlayer player, SkyBlockItem outputItem);

        public static List<RunicMerge> getMerges() {
            return List.of(new RuneRunicMerge(), new RuneWithItemRunicMerge());
        }
    }

    public static class RuneRunicMerge extends RunicMerge {
        @Override
        public SkyBlockItem merge(SkyBlockItem itemOnLeft, SkyBlockItem itemOnRight) {
            int runeLevelOnLeft = itemOnLeft.getAttributeHandler().getRuneLevel();
            int runeLevelOnRight = itemOnRight.getAttributeHandler().getRuneLevel();

            SkyBlockItem toReturn = itemOnLeft.clone();
            toReturn.getAttributeHandler().setRuneLevel(runeLevelOnLeft + runeLevelOnRight);
            return toReturn;
        }

        @Override
        public boolean isValidType(SkyBlockItem itemOnLeft, SkyBlockItem itemOnRight) {
            if (!(itemOnLeft.hasComponent(RuneComponent.class)) || !(itemOnRight.hasComponent(RuneComponent.class))) {
                return false;
            }
            String runeTypeOnLeft = itemOnLeft.getAttributeHandler().getTypeAsString();
            String runeTypeOnRight = itemOnRight.getAttributeHandler().getTypeAsString();
            if (!runeTypeOnLeft.equals(runeTypeOnRight))
                return false;

            int runeLevelOnLeft = itemOnLeft.getAttributeHandler().getRuneLevel();
            int runeLevelOnRight = itemOnRight.getAttributeHandler().getRuneLevel();

            return runeLevelOnLeft + runeLevelOnRight <= MAX_RUNE_LEVEL;
        }

        @Override
        public void onSuccess(SkyBlockPlayer player, SkyBlockItem outputItem) {
            int level = outputItem.getAttributeHandler().getRuneLevel();

            player.getSkills().increase(player, SkillCategories.RUNECRAFTING, 15D);

            sendSuccessMessage(player, "Combining Runes to Level " + level);
        }
    }

    public static class RuneWithItemRunicMerge extends RunicMerge {
        @Override
        public SkyBlockItem merge(SkyBlockItem itemOnLeft, SkyBlockItem itemOnRight) {
            SkyBlockItem runeItem = itemOnLeft.hasComponent(RuneComponent.class) ? itemOnLeft : itemOnRight;
            SkyBlockItem item = itemOnLeft.hasComponent(RuneComponent.class) ? itemOnRight : itemOnLeft;

            SkyBlockItem toReturn = item.clone();
            ItemAttributeRuneInfusedWith.RuneData runeData = toReturn.getAttributeHandler().getRuneData();
            runeData.setRuneType(runeItem.getAttributeHandler().getPotentialType());
            runeData.setLevel(runeItem.getAttributeHandler().getRuneLevel());
            toReturn.getAttributeHandler().setRuneData(runeData);
            return toReturn;
        }

        @Override
        public boolean isValidType(SkyBlockItem itemOnLeft, SkyBlockItem itemOnRight) {
            if (!(itemOnLeft.hasComponent(RuneComponent.class)) && !(itemOnLeft.hasComponent(RuneableComponent.class)))
                return false;
            if (!(itemOnRight.hasComponent(RuneComponent.class)) && !(itemOnRight.hasComponent(RuneableComponent.class)))
                return false;
            if (itemOnLeft.hasComponent(RuneComponent.class) && itemOnRight.hasComponent(RuneComponent.class))
                return false;
            if (itemOnLeft.hasComponent(RuneableComponent.class) && itemOnRight.hasComponent(RuneableComponent.class))
                return false;

            SkyBlockItem runableItem = itemOnLeft.hasComponent(RuneableComponent.class) ? itemOnLeft : itemOnRight;
            ItemAttributeRuneInfusedWith.RuneData runeData = runableItem.getAttributeHandler().getRuneData();

            return !runeData.hasRune();
        }

        @Override
        public void onSuccess(SkyBlockPlayer player, SkyBlockItem outputItem) {
            player.getSkills().increase(player, SkillCategories.RUNECRAFTING, 15D);

            ItemType appliedRune = outputItem.getAttributeHandler().getRuneData().getRuneType();
            Rarity rarity = appliedRune.rarity;

            sendSuccessMessage(player, "Applying " + StringUtility.toNormalCase(appliedRune.name()) +
                    " (" + rarity.getDisplay() + "§d)");
        }
    }

    private static void sendSuccessMessage(SkyBlockPlayer player, String action) {
        player.sendMessage("§d-§515 §dRunecrafting XP §7- §d" + action);
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {}

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.getPlayer();
        player.addAndUpdateItem(itemOnLeft);
        player.addAndUpdateItem(itemOnRight);
        player.addAndUpdateItem(outputItem);
        if (fusingAnimation != null && !fusingAnimation.isDone()) {
            fusingAnimation.complete(true);
        }
    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {
        ((SkyBlockPlayer) player).addAndUpdateItem(itemOnLeft);
        ((SkyBlockPlayer) player).addAndUpdateItem(itemOnRight);
        ((SkyBlockPlayer) player).addAndUpdateItem(outputItem);
        if (fusingAnimation != null && !fusingAnimation.isDone()) {
            fusingAnimation.complete(true);
        }
    }

    public CompletableFuture<Boolean> startFusingAnimation() {
        fusingAnimation = new CompletableFuture<>();
        Thread.startVirtualThread(() -> {
            // Incrementally change the colors of the slots from purple to pink and back multiple times over the duration
            int duration = 1500; // Duration in milliseconds
            int interval = 30; // Interval between each color update in milliseconds
            int cycles = 5; // Number of times to transition from purple to pink and back

            int totalSteps = duration / interval;
            int stepsPerCycle = totalSteps / cycles;

            Material[] colors = {
                    Material.PURPLE_STAINED_GLASS_PANE,
                    Material.MAGENTA_STAINED_GLASS_PANE,
                    Material.PINK_STAINED_GLASS_PANE
            };

            for (int currentStep = 0; currentStep < totalSteps; currentStep++) {
                int cycleIndex = currentStep / stepsPerCycle;
                double progress = (double) (currentStep % stepsPerCycle) / (stepsPerCycle - 1);

                if (cycleIndex % 2 == 0) {
                    // Purple to pink
                    int colorIndex = (int) Math.floor(progress * (colors.length - 1));
                    setGlassPanes(BOTTOM_SLOTS, colors[colorIndex]);
                    setGlassPanes(CONNECTOR_RUNIC_SLOTS, colors[colorIndex]);
                    setGlassPanes(LEFT_RUNIC_SLOTS, colors[colorIndex]);
                    setGlassPanes(RIGHT_RUNIC_SLOTS, colors[colorIndex]);
                } else {
                    // Pink to purple
                    int colorIndex = (int) Math.floor((1 - progress) * (colors.length - 1));
                    setGlassPanes(BOTTOM_SLOTS, colors[colorIndex]);
                    setGlassPanes(CONNECTOR_RUNIC_SLOTS, colors[colorIndex]);
                    setGlassPanes(LEFT_RUNIC_SLOTS, colors[colorIndex]);
                    setGlassPanes(RIGHT_RUNIC_SLOTS, colors[colorIndex]);
                }

                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    fusingAnimation.complete(true);
                    return;
                }
            }

            // Animation completed successfully
            fusingAnimation.complete(false);
        });
        return fusingAnimation;
    }

    private void setGlassPanes(int[] slots, Material material) {
        for (int slot : slots) {
            set(slot, ItemStackCreator.createNamedItemStack(material));
            getInventory().setItemStack(slot, ItemStackCreator.createNamedItemStack(material).build());
        }
    }
}
