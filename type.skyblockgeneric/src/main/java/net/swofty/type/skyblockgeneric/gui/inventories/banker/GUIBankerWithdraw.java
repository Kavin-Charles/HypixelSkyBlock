package net.swofty.type.skyblockgeneric.gui.inventories.banker;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIQueryItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.DataMutexService;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointBankData;
import net.swofty.type.skyblockgeneric.data.monogdb.CoopDatabase;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIBankerWithdraw extends HypixelInventoryGUI {

    public GUIBankerWithdraw() {
        super("Bank Withdrawal", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void setItems(InventoryGUIOpenEvent e) {
        fill(ItemStackCreator.createNamedItemStack(Material.BLACK_STAINED_GLASS_PANE));
        set(GUIClickableItem.getGoBackItem(31, new GUIBanker()));

        double bankBalance = ((SkyBlockPlayer) e.player()).getSkyblockDataHandler().get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class).getValue().getAmount();

        set(new GUIClickableItem(10) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return ItemStackCreator.getStack("§aEverything in the account", Material.DISPENSER, 64,
                        "§8Bank withdrawal",
                        " ",
                        "§7Current balance: §6" + StringUtility.decimalify(
                                bankBalance, 1
                        ),
                        "§7Amount to withdraw: §6" + StringUtility.decimalify(
                                bankBalance, 1
                        ),
                        " ",
                        "§eClick to withdraw coins!"
                );
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.closeInventory();
                attemptWithdrawal(player, bankBalance);
            }
        });

        set(new GUIClickableItem(12) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return ItemStackCreator.getStack("§aHalf of the account", Material.DISPENSER, 32,
                        "§8Bank withdrawal",
                        " ",
                        "§7Current balance: §6" + StringUtility.decimalify(
                                bankBalance, 1
                        ),
                        "§7Amount to withdraw: §6" + StringUtility.decimalify(
                                bankBalance / 2, 1
                        ),
                        " ",
                        "§eClick to withdraw coins!"
                );
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.closeInventory();
                attemptWithdrawal(player, bankBalance / 2);
            }
        });

        set(new GUIClickableItem(14) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.closeInventory();
                attemptWithdrawal(player, bankBalance / 5);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return ItemStackCreator.getStack("§a20% of the account", Material.DISPENSER, 1,
                        "§8Bank withdrawal",
                        " ",
                        "§7Current balance: §6" + StringUtility.decimalify(bankBalance, 1),
                        "§7Amount to withdraw: §6" + StringUtility.decimalify(bankBalance / 5, 1),
                        " ",
                        "§eClick to withdraw coins!"
                );
            }
        });

        set(new GUIQueryItem(16) {
            @Override
            public HypixelInventoryGUI onQueryFinish(String query, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                try {
                    double amount = Double.parseDouble(query);
                    if (amount > bankBalance) {
                        player.sendMessage("§cYou do not have that many coins to withdraw!");
                        return null;
                    }
                    if (amount <= 0) {
                        player.sendMessage("§cYou cannot withdraw that amount!");
                        return null;
                    }

                    player.closeInventory();
                    attemptWithdrawal(player, amount);
                } catch (NumberFormatException ex) {
                    player.sendMessage("§cThat is not a valid number!");
                }
                return null;
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return ItemStackCreator.getStack("§aCustom amount", Material.OAK_SIGN, 1,
                        "§8Bank withdrawal",
                        " ",
                        "§7Current balance: §6" + StringUtility.decimalify(
                                player.getSkyblockDataHandler().get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class).getValue().getAmount(), 1
                        ),
                        " ",
                        "§eClick to withdraw coins!"
                );
            }
        });
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
        if (e == null) return;
        SkyBlockPlayer player = (SkyBlockPlayer) e.getPlayer();
        player.setBankDelayed(false);
    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {
        ((SkyBlockPlayer) player).setBankDelayed(false);
    }

    private void attemptWithdrawal(SkyBlockPlayer player, double amount) {
        player.sendMessage("§8Withdrawing coins...");

        if (!player.isCoop()) {
            // Single player - no synchronization needed
            DatapointBankData.BankData bankData = player.getSkyblockDataHandler().get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class).getValue();
            if (amount > bankData.getAmount()) {
                player.sendMessage("§cYou do not have that many coins to withdraw!");
                return;
            }

            bankData.removeAmount(amount);
            bankData.addTransaction(new DatapointBankData.Transaction(
                    System.currentTimeMillis(), -amount, player.getUsername()));

            player.addCoins(amount);
            player.sendMessage("§aYou have withdrawn §6" + StringUtility.decimalify(amount, 1) +
                    " coins§a! You now have §6" + StringUtility.decimalify(bankData.getAmount(), 1) +
                    " coins§a in your account.");
            return;
        }

        // Coop scenario - use mutex service
        CoopDatabase.Coop coop = player.getCoop();
        String lockKey = "bank_data:" + player.getSkyBlockIsland().getIslandID().toString();

        DataMutexService mutexService = new DataMutexService();

        mutexService.withSynchronizedData(
                lockKey,
                coop.members(),
                SkyBlockDataHandler.Data.BANK_DATA,

                // This callback receives the LATEST bank data from across all servers
                (DatapointBankData.BankData latestBankData) -> {
                    // Validate against the most up-to-date data
                    if (amount > latestBankData.getAmount()) {
                        player.sendMessage("§cYou do not have that many coins to withdraw!");
                        return null; // Return null to indicate failure - no changes made
                    }

                    // Make the changes to the synchronized data
                    latestBankData.removeAmount(amount);
                    latestBankData.addTransaction(new DatapointBankData.Transaction(
                            System.currentTimeMillis(), -amount, player.getUsername()));

                    player.addCoins(amount);
                    player.sendMessage("§aYou have withdrawn §6" + StringUtility.decimalify(amount, 1) +
                            " coins§a! You now have §6" + StringUtility.decimalify(latestBankData.getAmount(), 1) +
                            " coins§a in your account.");

                    return latestBankData; // Return modified data to be propagated to all servers
                },

                // Failure callback
                () -> {
                    player.sendMessage("§cYou cannot withdraw coins as your coop members are currently using the bank.");
                }
        );
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }
}