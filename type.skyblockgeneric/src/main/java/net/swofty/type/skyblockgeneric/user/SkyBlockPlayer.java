package net.swofty.type.skyblockgeneric.user;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.UpdateHealthPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.swofty.commons.PlayerShopData;
import net.swofty.commons.SkyBlockPlayerProfiles;
import net.swofty.commons.StringUtility;
import net.swofty.commons.item.ItemType;
import net.swofty.commons.item.Rarity;
import net.swofty.commons.item.UnderstandableSkyBlockItem;
import net.swofty.commons.statistics.ItemStatistic;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.datapoints.*;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.bazaar.BazaarConnector;
import net.swofty.type.skyblockgeneric.collection.CustomCollectionAward;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.*;
import net.swofty.type.skyblockgeneric.data.monogdb.CoopDatabase;
import net.swofty.type.skyblockgeneric.event.actions.player.ActionPlayerChangeHypixelMenuDisplay;
import net.swofty.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import net.swofty.type.skyblockgeneric.event.value.ValueUpdateEvent;
import net.swofty.type.skyblockgeneric.event.value.events.MaxHealthValueUpdateEvent;
import net.swofty.type.skyblockgeneric.event.value.events.MiningValueUpdateEvent;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.AccessoryComponent;
import net.swofty.type.skyblockgeneric.item.components.ArrowComponent;
import net.swofty.type.skyblockgeneric.item.components.SackComponent;
import net.swofty.type.skyblockgeneric.item.set.ArmorSetRegistry;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.levels.CustomLevelAward;
import net.swofty.type.skyblockgeneric.levels.SkyBlockEmblems;
import net.swofty.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.noteblock.SkyBlockSongsHandler;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.region.mining.MineableBlock;
import net.swofty.type.skyblockgeneric.skill.skills.RunecraftingSkill;
import net.swofty.type.skyblockgeneric.user.statistics.PlayerStatistics;
import net.swofty.type.skyblockgeneric.utility.DeathMessageCreator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class SkyBlockPlayer extends HypixelPlayer {
    private final PlayerAbilityHandler abilityHandler = new PlayerAbilityHandler();
    @Getter
    private final PlayerStatistics statistics = new PlayerStatistics(this);
    public float health = 100;
    @Setter
    public boolean isBankDelayed = false;
    @Setter
    private float mana = 100;
    @Setter
    private boolean inLaunchpad = false;
    @Setter
    public boolean bypassBuild = false;
    @Setter
    public boolean hasAuthenticated = true;
    @Setter
    private SkyBlockIsland skyBlockIsland;

    private static final Pattern SACK_PATTERN = Pattern.compile("^(?:(SMALL|MEDIUM|LARGE|ENCHANTED)_)?(.+?)_SACK$");


    public SkyBlockPlayer(@NotNull GameProfile gameProfile, @NotNull PlayerConnection playerConnection) {
        super(playerConnection, gameProfile);
    }

	public SkyBlockDataHandler getSkyblockDataHandler() {
        return (SkyBlockDataHandler) SkyBlockDataHandler.getUser(this.getUuid());
    }

    public DatapointMuseum.MuseumData getMuseumData() {
        return getSkyblockDataHandler().get(SkyBlockDataHandler.Data.MUSEUM_DATA, DatapointMuseum.class).getValue();
    }

    public SkyBlockSongsHandler getSongHandler() {
        return new SkyBlockSongsHandler(this);
    }

    public FairySoulHandler getFairySoulHandler() {
        return new FairySoulHandler(this);
    }

    public @Nullable SkyBlockPlayerProfiles getProfiles() {
        return SkyBlockPlayerProfiles.get(this.getUuid());
    }

    public DatapointPetData.UserPetData getPetData() {
        return getSkyblockDataHandler().get(SkyBlockDataHandler.Data.PET_DATA, DatapointPetData.class).getValue();
    }

    public MissionData getMissionData() {
        MissionData data = getSkyblockDataHandler().get(SkyBlockDataHandler.Data.MISSION_DATA, DatapointMissionData.class).getValue();
        data.setSkyBlockPlayer(this);
        return data;
    }

    @Override
    public String getFullDisplayName() {
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = getSkyBlockExperience();
        SkyBlockEmblems.SkyBlockEmblem emblem = experience.getEmblem();
        return getFullDisplayName(emblem, experience.getLevel().getColor());
    }

    public String getFullDisplayName(String levelColor) {
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = getSkyBlockExperience();
        return getFullDisplayName(experience.getEmblem(), levelColor);
    }

    public String getFullDisplayName(SkyBlockEmblems.SkyBlockEmblem displayEmblem) {
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = getSkyBlockExperience();
        return getFullDisplayName(displayEmblem, experience.getLevel().getColor());
    }


    public String getFullDisplayName(SkyBlockEmblems.SkyBlockEmblem displayEmblem, String levelColor) {
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = getSkyBlockExperience();

        return "§8[" + levelColor + experience.getLevel() + "§8] " +
                (displayEmblem == null ? "" : displayEmblem.emblem() + " ") +
                getDataHandler().get(HypixelDataHandler.Data.RANK, DatapointRank.class).getValue().getPrefix() +
                this.getUsername();
    }

    public PlayerShopData getShoppingData() {
        return getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SHOPPING_DATA, DatapointShopData.class).getValue();
    }

    public DatapointCollection.PlayerCollection getCollection() {
        return getSkyblockDataHandler().get(SkyBlockDataHandler.Data.COLLECTION, DatapointCollection.class).getValue();
    }

    public DatapointSkills.PlayerSkills getSkills() {
        return getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SKILLS, DatapointSkills.class).getValue();
    }

    public boolean isOnIsland() {
        return getInstance() != null
                && getInstance() != HypixelConst.getInstanceContainer()
                && getInstance() != HypixelConst.getEmptyInstance();
    }

    public @Nullable SkyBlockItem getArrow() {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = getInventory().getItemStack(i);
            SkyBlockItem item = new SkyBlockItem(stack);
            if (item.hasComponent(ArrowComponent.class)) {
                return item;
            }
        }

        if (!hasCustomCollectionAward(CustomCollectionAward.QUIVER)) return null;

        DatapointQuiver.PlayerQuiver quiver = getQuiver();
        return quiver.getFirstItemInQuiver();
    }

    public int getAmountOfEmptySlots() {
        int amountOfEmptySlots = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = getInventory().getItemStack(i);
            if (stack.material() == Material.AIR) amountOfEmptySlots++;
        }
        return amountOfEmptySlots;
    }

    public boolean hasEmptySlots(int amount) {
        return getAmountOfEmptySlots() >= amount;
    }

    public @Nullable SkyBlockItem getAndConsumeArrow() {
        // Check Inventory first
        for (int i = 0; i < 36; i++) {
            ItemStack stack = getInventory().getItemStack(i);
            SkyBlockItem item = new SkyBlockItem(stack);
            if (item.hasComponent(ArrowComponent.class)) {
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                    getInventory().setItemStack(i, PlayerItemUpdater.playerUpdate(this, item.getItemStack()).build());
                    ActionPlayerChangeHypixelMenuDisplay.runCheck(this);
                } else {
                    getInventory().setItemStack(i, ItemStack.of(Material.AIR));
                }
                return item;
            }
        }

        if (!hasCustomCollectionAward(CustomCollectionAward.QUIVER)) return null;

        DatapointQuiver.PlayerQuiver quiver = getQuiver();
        SkyBlockItem item = quiver.getFirstItemInQuiver();
        if (item == null) return null;

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
            quiver.setFirstItemInQuiver(item);
        } else {
            quiver.setFirstItemInQuiver(null);
        }
        getSkyblockDataHandler().get(SkyBlockDataHandler.Data.QUIVER, DatapointQuiver.class).setValue(quiver);
        ActionPlayerChangeHypixelMenuDisplay.runCheck(this);
        return item;
    }

    public boolean hasTalisman(ItemType talisman) {
        return getTalismans().stream().anyMatch(talisman1 -> talisman1 == talisman);
    }

    public List<ItemType> getTalismans() {
        List<ItemType> inInventory = Stream.of(getAllInventoryItems())
                .filter(item -> item.hasComponent(AccessoryComponent.class))
                .map(item -> item.getAttributeHandler().getPotentialType()).toList();

        List<ItemType> inAccessoryBag = getAccessoryBag().getAllAccessories().stream()
                .filter(item -> item.hasComponent(AccessoryComponent.class))
                .map(item -> item.getAttributeHandler().getPotentialType()).toList();

        return Stream.concat(inInventory.stream(), inAccessoryBag.stream()).collect(Collectors.toList());
    }

    public Integer getMagicalPower() {

        int magicalPower = 0;

        for (SkyBlockItem accessory : getAccessoryBag().getUniqueAccessories()) {
            Rarity taliRarity = accessory.getAttributeHandler().getRarity();
            switch (taliRarity) {
                case COMMON -> magicalPower += 3;
                case UNCOMMON -> magicalPower += 5;
                case RARE -> magicalPower += 8;
                case EPIC -> magicalPower += 12;
                case LEGENDARY -> magicalPower += 16;
                case MYTHIC -> magicalPower += 22;
            }
        }
        return magicalPower;
    }

    public void setMuseumData(DatapointMuseum.MuseumData data) {
        getSkyblockDataHandler().get(SkyBlockDataHandler.Data.MUSEUM_DATA, DatapointMuseum.class).setValue(data);
    }

    public SkyBlockItem[] getAllInventoryItems() {
        return Stream.of(getInventory().getItemStacks())
                .map(SkyBlockItem::new)
                .toArray(SkyBlockItem[]::new);
    }

    public Map<Integer, Integer> getAllOfTypeInInventory(ItemType type) {
        Map<Integer, Integer> map = new HashMap<>();

        for (int i = 0; i < 36; i++) {
            ItemStack stack = getInventory().getItemStack(i);
            SkyBlockItem item = new SkyBlockItem(stack);
            if (item.getAttributeHandler().getPotentialType() == null) continue;

            if (item.getAttributeHandler().getPotentialType() == type) {
                map.put(i, stack.amount());
            }
        }

        return map;
    }

    public void setItemInHand(@Nullable SkyBlockItem item) {
        if (item == null) {
            setItemInHand(PlayerHand.MAIN, ItemStack.of(Material.AIR));
            return;
        }

        setItemInHand(PlayerHand.MAIN, PlayerItemUpdater.playerUpdate(this, item.getItemStack()).build());
    }

    public int getAmountInInventory(ItemType type) {
        return getAllOfTypeInInventory(type).values().stream().mapToInt(Integer::intValue).sum();
    }

    public boolean isCoop() {
        return getSkyblockDataHandler().get(SkyBlockDataHandler.Data.IS_COOP, DatapointBoolean.class).getValue();
    }

    public CoopDatabase.Coop getCoop() {
        return CoopDatabase.getFromMember(getUuid());
    }

    public @Nullable ArmorSetRegistry getArmorSet() {
        ItemType helmet = new SkyBlockItem(getHelmet()).getAttributeHandler().getPotentialType();
        ItemType chestplate = new SkyBlockItem(getChestplate()).getAttributeHandler().getPotentialType();
        ItemType leggings = new SkyBlockItem(getLeggings()).getAttributeHandler().getPotentialType();
        ItemType boots = new SkyBlockItem(getBoots()).getAttributeHandler().getPotentialType();

        return ArmorSetRegistry.getArmorSet(boots, leggings, chestplate, helmet);
    }

    public int getRuneLevel() {
        return RunecraftingSkill.getUnlockedRune(this);
    }

    public boolean isWearingItem(SkyBlockItem item) {
        SkyBlockItem[] armor = getArmor();

        for (SkyBlockItem armorItem : armor) {
            if (armorItem == null) continue;
            if (armorItem.getAttributeHandler().getPotentialType() == item.getAttributeHandler().getPotentialType()) {
                return true;
            }
        }

        return false;
    }

    public SkyBlockItem[] getArmor() {
        return new SkyBlockItem[] {
                new SkyBlockItem(getHelmet()),
                new SkyBlockItem(getChestplate()),
                new SkyBlockItem(getLeggings()),
                new SkyBlockItem(getBoots())
        };
    }

    public SkyBlockItem updateItem(PlayerItemOrigin origin, Consumer<SkyBlockItem> update) {
        ItemStack toUpdate = origin.getStack(this);
        if (toUpdate == null) return null;

        SkyBlockItem item = new SkyBlockItem(toUpdate);
        update.accept(item);

        origin.setStack(this, PlayerItemUpdater.playerUpdate(this, item.getItemStack()).build());
        return item;
    }

    public @Nullable SkyBlockRegion getRegion() {
        if (isOnIsland()) return SkyBlockRegion.getIslandRegion();
        return SkyBlockRegion.getRegionOfPosition(this.getPosition());
    }

    public void addAndUpdateItem(@Nullable List<SkyBlockItem> items) {
        if (items == null) return;
        for (SkyBlockItem item : items) {
            addAndUpdateItem(item);
        }
    }

    public void addAndUpdateItem(@Nullable SkyBlockItem item) {
        if (item == null) return;
        if (item.isNA()) return;

        ItemStack toAdd = PlayerItemUpdater.playerUpdate(
                this,
                item.getItemStack(),
                true).build();
        this.getInventory().addItemStack(toAdd);

        // TODO: add overflow into the players queue
    }

    public void addAndUpdateItem(UnderstandableSkyBlockItem item) {
        addAndUpdateItem(new SkyBlockItem(item));
    }

    public void addAndUpdateItem(ItemType item) {
        addAndUpdateItem(new SkyBlockItem(item));
    }

    public void addAndUpdateItem(ItemType item, int amount) {
        for (int i = 0; i < amount; i++) {
            addAndUpdateItem(new SkyBlockItem(item));
        }
    }

    public void addAndUpdateItem(ItemStack item) {
        addAndUpdateItem(new SkyBlockItem(item));
    }

    public int countItem(ItemType item) {
        AtomicInteger count = new AtomicInteger();
        getAllOfTypeInInventory(item).forEach((stack, meow) -> count.addAndGet(meow));
        return count.get();
    }

    public boolean takeItem(SkyBlockItem itemToTake) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = getInventory().getItemStack(i);
            SkyBlockItem item = new SkyBlockItem(stack);
            if (item.isSimilar(itemToTake)) {
                getInventory().setItemStack(i, ItemStack.of(Material.AIR));
                return true;
            }
        }

        return false;
    }

    public @Nullable List<SkyBlockItem> takeItem(ItemType type, int amount) {
        List<SkyBlockItem> consumedItems = new ArrayList<>();
        Map<Integer, Integer> map = getAllOfTypeInInventory(type);
        int total = map.values().stream().mapToInt(Integer::intValue).sum();
        if (total < amount) return null;

        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (amount <= 0) break;

            int slot = entry.getKey();
            int currentAmount = entry.getValue();

            SkyBlockItem skyBlockItem = new SkyBlockItem(getInventory().getItemStack(slot));
            skyBlockItem.setAmount(Math.min(skyBlockItem.getAmount(), amount));

            consumedItems.add(skyBlockItem);
            ItemStack item = getInventory().getItemStack(slot).consume(amount);

            getInventory().setItemStack(slot, item);
            amount -= Math.min(amount, currentAmount);
        }
        return consumedItems;
    }

    public DatapointQuiver.PlayerQuiver getQuiver() {
        return getSkyblockDataHandler().get(SkyBlockDataHandler.Data.QUIVER, DatapointQuiver.class).getValue();
    }

    public DatapointAccessoryBag.PlayerAccessoryBag getAccessoryBag() {
        return getSkyblockDataHandler().get(SkyBlockDataHandler.Data.ACCESSORY_BAG, DatapointAccessoryBag.class).getValue();
    }

    public DatapointSackOfSacks.PlayerSackOfSacks getSackOfSacks() {
        return getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SACK_OF_SACKS, DatapointSackOfSacks.class).getValue();
    }

    public DatapointItemsInSacks.PlayerItemsInSacks getSackItems() {
        return getSkyblockDataHandler().get(SkyBlockDataHandler.Data.ITEMS_IN_SACKS, DatapointItemsInSacks.class).getValue();
    }

    public BazaarConnector getBazaarConnector() {
        return new BazaarConnector(this);
    }

    public List<SkyBlockItem> getAllSacks() {
        List<SkyBlockItem> sacks = new ArrayList<>(getSackOfSacks().getAllSacks());
        for (SkyBlockItem item : getAllInventoryItems()) {
            if (item.hasComponent(SackComponent.class)) {
                sacks.add(item);
            }
        }
        return sacks;
    }

    public int getMaxSackStorage(ItemType sack) {
        int maxStorage = 0;
        String sackCategory = "";

        Matcher matcher = SACK_PATTERN.matcher(sack.name());
        if (matcher.find()) {
            sackCategory = matcher.group(2);
        } else {
            System.out.println("Invalid sack name: " + sack.name());
            return 0;
        }

        for (SkyBlockItem skyBlockItem : getAllSacks()) {
            matcher = SACK_PATTERN.matcher(skyBlockItem.getAttributeHandler().getTypeAsString());
            if (matcher.find()) {
                String otherCategory = matcher.group(2);
                if (sackCategory.equals(otherCategory) && skyBlockItem.hasComponent(SackComponent.class)) {
                    maxStorage += skyBlockItem.getComponent(SackComponent.class).getMaxCapacity();
                }
            }
        }
        return maxStorage;
    }

    public boolean canInsertItemIntoSacks(ItemType item) {
        for (SkyBlockItem sack : getAllSacks()) {
            if (sack.hasComponent(SackComponent.class)) {
                SackComponent sackInstance = sack.getComponent(SackComponent.class);
                for (ItemType linker : sackInstance.getValidItems()) {
                    if (linker == item) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public boolean canInsertItemIntoSacks(ItemType item, Integer amount) {
        if (!canInsertItemIntoSacks(item)) return false;

        List<Map<SkyBlockItem, Integer>> maxStorages = new ArrayList<>();
        for (SkyBlockItem sack : getAllSacks()) {
            if (sack.hasComponent(SackComponent.class)) {
                SackComponent sackInstance = sack.getComponent(SackComponent.class);
                for (ItemType linker : sackInstance.getValidItems()) {
                    if (linker == item) {
                        if (!maxStorages.contains(sack)) {
                            maxStorages.add(Map.of(sack, getMaxSackStorage(sack.getAttributeHandler().getPotentialType())));
                        }
                    }
                }
            }
        }

        maxStorages.sort((map1, map2) -> {
            Integer value1 = map1.values().iterator().next();
            Integer value2 = map2.values().iterator().next();
            return value2.compareTo(value1);
        });

        return maxStorages.getFirst().values().iterator().next() >= getSackItems().getAmount(item) + amount;
    }

    public String getShortenedDisplayName() {
        return StringUtility.getTextFromComponent(Component.text(this.getUsername(),
                getDataHandler().get(HypixelDataHandler.Data.RANK, DatapointRank.class).getValue().getTextColor())
        );
    }

    public float getMaxMana() {
        return (float) (100 + getStatistics().allStatistics().getOverall(ItemStatistic.INTELLIGENCE));
    }

    public boolean hasCustomCollectionAward(CustomCollectionAward award) {
        Map.Entry<ItemType, Integer> entry = CustomCollectionAward.AWARD_CACHE.get(award);
        if (entry == null) return false;

        return getCollection().get(entry.getKey()) > entry.getValue();
    }

    public boolean hasCustomLevelAward(CustomLevelAward award) {
        return getSkyBlockExperience().getLevel().asInt() >= CustomLevelAward.getLevel(award);
    }

    public DatapointFairySouls.PlayerFairySouls getFairySouls() {
        return getSkyblockDataHandler().get(SkyBlockDataHandler.Data.FAIRY_SOULS, DatapointFairySouls.class).getValue();
    }

    public DatapointSkyBlockExperience.PlayerSkyBlockExperience getSkyBlockExperience() {
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SKYBLOCK_EXPERIENCE,
                DatapointSkyBlockExperience.class
        ).getValue();
        experience.setAttachedPlayer(this);
        return experience;
    }

    public boolean hasUnlockedXPCause(SkyBlockLevelCauseAbstr cause) {
        return getSkyBlockExperience().hasExperienceFor(cause);
    }

    public PlayerEnchantmentHandler getEnchantmentHandler() {
        return new PlayerEnchantmentHandler(this);
    }

    public Double getMiningSpeed() {
        return this.getStatistics().allStatistics().getOverall(ItemStatistic.MINING_SPEED);
    }

    public double getTimeToMine(SkyBlockItem item, Block b) {
        MineableBlock block = MineableBlock.get(b);
        if (block == null) return -1;
        if (!item.getAttributeHandler().isMiningTool()) return -1;
        if (getRegion() == null) return -1;

        if (block.getMiningPowerRequirement() > item.getAttributeHandler().getBreakingPower()) return -1;
        if (block.getStrength() > 0) {
            double time = Math.round(block.getStrength() * 30) / (Math.max(getMiningSpeed(), 1));
            ValueUpdateEvent event = new MiningValueUpdateEvent(this, time, item);

            SkyBlockValueEvent.callValueUpdateEvent(event);
            time = (double) event.getValue();

            return time;
        }

        return 0;
    }

    public @NonNull Double getDefense() {
        return this.getStatistics().allStatistics().getOverall(ItemStatistic.DEFENSE);
    }

    public void playSuccessSound() {
        playSound(Sound.sound(Key.key("block.note_block.pling"), Sound.Source.PLAYER, 1.0f, 2.0f));
    }

    public double getCoins() {
        return getSkyblockDataHandler().get(SkyBlockDataHandler.Data.COINS, DatapointDouble.class).getValue();
    }

    public void setCoins(double amount) {
        getSkyblockDataHandler().get(SkyBlockDataHandler.Data.COINS, DatapointDouble.class).setValue(amount);
    }

    public void addCoins(double amount) {
        setCoins(getCoins() + amount);
    }

    public void removeCoins(double amount) {
        setCoins(getCoins() - amount);
    }

    public Integer getBits() {
        return getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BITS, DatapointInteger.class).getValue();
    }

    public void setBits(int amount) {
        getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BITS, DatapointInteger.class).setValue(amount);
    }

    public void addBits(int amount) {
        setBits(getBits() + amount);
    }

    public void removeBits(int amount) {
        setBits(getBits() - amount);
    }

    public Integer getGems() {
        return getSkyblockDataHandler().get(SkyBlockDataHandler.Data.GEMS, DatapointInteger.class).getValue();
    }

    public void setGems(int amount) {
        getSkyblockDataHandler().get(SkyBlockDataHandler.Data.GEMS, DatapointInteger.class).setValue(amount);
    }

    public void addGems(int amount) {
        setGems(getGems() + amount);
    }

    public void removeGems(int amount) {
        setGems(getGems() - amount);
    }

    public int maxItemFit(ItemType targetType) {
        int fit = 0;
        int maxStack = targetType.material.maxStackSize();

        PlayerInventory inv = getInventory();
        int size = inv.getSize();

        for (int slot = 0; slot < size; slot++) {
            ItemStack stack = inv.getItemStack(slot);
            // Interpret the stack via ItemType
            ItemType slotType = ItemType.fromMaterial(stack.material());

            if (slotType == ItemType.AIR || slotType == null) {
                fit += maxStack;
            } else if (slotType.equals(targetType)) {
                fit += (maxStack - stack.amount());
            }
        }
        return fit;
    }

    public Long getBoosterCookieExpirationDate() {
        return getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BOOSTER_COOKIE_EXPIRATION_DATE, DatapointLong.class).getValue();
    }

    public void setBoosterCookieExpirationDate(long timestamp) {
        getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BOOSTER_COOKIE_EXPIRATION_DATE, DatapointLong.class).setValue(timestamp);
    }

    public boolean isBoosterCookieActive() {
        return getBoosterCookieExpirationDate() >= System.currentTimeMillis();
    }

    public DatapointKat.PlayerKat getKatData() {
        return getSkyblockDataHandler().get(SkyBlockDataHandler.Data.KAT, DatapointKat.class).getValue();
    }

    public DatapointBestiary.PlayerBestiary getBestiaryData() {
        DatapointBestiary.PlayerBestiary bestiary = getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BESTIARY, DatapointBestiary.class).getValue();
        bestiary.setAttachedPlayer(this);
        return bestiary;
    }

    public Long getExperience() {
        return getSkyblockDataHandler().get(SkyBlockDataHandler.Data.EXPERIENCE, DatapointLong.class).getValue();
    }

    public void setExperience(long value) {
        getSkyblockDataHandler().get(SkyBlockDataHandler.Data.EXPERIENCE, DatapointLong.class).setValue(value);

        int level;
        float progressToNext;

        // Determine the level from total experience
        if (value <= 352) {
            level = (int) (Math.sqrt(value + 9) - 3);
        } else if (value <= 1507) {
            level = (int) (8.1 + Math.sqrt((2.0 / 5.0) * (value - 195.975)));
        } else {
            level = (int) (18.0555 + Math.sqrt((2.0 / 9.0) * (value - 752.9861)));
        }

        // Calculate total experience required to reach this level
        long expAtCurrentLevel;
        int expToNextLevel;

        if (level <= 16) {
            expAtCurrentLevel = (long) level * level + 6L * level;
            expToNextLevel = 2 * level + 7;
        } else if (level <= 31) {
            expAtCurrentLevel = (long) (2.5 * level * level - 40.5 * level + 360);
            expToNextLevel = 5 * level - 38;
        } else {
            expAtCurrentLevel = (long) (4.5 * level * level - 162.5 * level + 2220);
            expToNextLevel = 9 * level - 158;
        }

        long expIntoLevel = value - expAtCurrentLevel;
        progressToNext = expToNextLevel > 0 ? (float) expIntoLevel / expToNextLevel : 0f;

        setLevel(level);
        setExp(progressToNext);
    }

    public void addExperience(long value) {
        setExperience(getExperience() + value);
    }

    public void removeExperience(long value) {
        setExperience(getExperience() - value);
    }

    public DatapointDeaths.PlayerDeaths getDeathData() {
        return getSkyblockDataHandler().get(SkyBlockDataHandler.Data.DEATHS, DatapointDeaths.class).getValue();
    }

    @Override
    public void kill() {
        setHealth(getMaxHealth());
        sendTo(HypixelConst.getTypeLoader().getType());

        DeathMessageCreator creator = new DeathMessageCreator(this.lastDamage);
        sendMessage("§c☠ §7You " + creator.createPersonal());

        getDeathData().increase(this.lastDamage, 1);

        playSound(Sound.sound(Key.key("block.anvil.fall"), Sound.Source.PLAYER, 1.0f, 2.0f));

        if (HypixelConst.isIslandServer()) return;

        if (!isBoosterCookieActive()) {
            sendMessage("§cYou died and lost " + StringUtility.decimalify(getCoins() / 2, 1) + " coins!");
            setCoins(getCoins() / 2);
        }

        if (!HypixelConst.getTypeLoader().getLoaderValues().announceDeathMessages()) return;

        SkyBlockGenericLoader.getLoadedPlayers().forEach(player -> {
            if (player.getUuid().equals(getUuid())) return;
            if (player.getInstance() != getInstance()) return;

            player.sendMessage("§c☠ §7" + getFullDisplayName() + " §7" + creator.createOther());
        });
    }

    public float getMaxHealth() {
        PlayerStatistics statistics = this.getStatistics();
        float maxHealth = statistics.allStatistics().getOverall(ItemStatistic.HEALTH).floatValue();
        MaxHealthValueUpdateEvent event = new MaxHealthValueUpdateEvent(this, maxHealth);
        SkyBlockValueEvent.callValueUpdateEvent(event);
        return (float) event.getValue();
    }

    @Override
    public float getHealth() {
        return this.health;
    }

    @Override
    public void setHealth(float health) {
        if ((System.currentTimeMillis() - joined) < 3000) return;
        if (health < 0) {
            kill();
            return;
        }
        this.health = health;
        this.sendPacket(new UpdateHealthPacket((health / getMaxHealth()) * 20, 20, 20));
    }

    @Override
    public void sendMessage(@NotNull String message) {
        super.sendMessage(message.replace("&", "§"));
    }

    @Override
    public void closeInventory() {
        Inventory tempInv = (Inventory) this.getOpenInventory();
        super.closeInventory();
        if (HypixelInventoryGUI.GUI_MAP.containsKey(this.getUuid())) {
            HypixelInventoryGUI gui = net.swofty.type.generic.gui.inventory.HypixelInventoryGUI.GUI_MAP.get(this.getUuid());

            if (gui == null) return;
            if (tempInv == null) return;
            gui.onClose(new InventoryCloseEvent(tempInv, this, true), net.swofty.type.generic.gui.inventory.HypixelInventoryGUI.CloseReason.SERVER_EXITED);
            net.swofty.type.generic.gui.inventory.HypixelInventoryGUI.GUI_MAP.remove(this.getUuid());
        }
    }

    public static String getDisplayName(UUID uuid) {
        if (SkyBlockGenericLoader.getLoadedPlayers().stream().anyMatch(player -> player.getUuid().equals(uuid))) {
            return SkyBlockGenericLoader.getLoadedPlayers().stream().filter(player -> player.getUuid().equals(uuid)).findFirst().get().getFullDisplayName();
        } else {
            // Fallback for offline name display: use Hypixel account data (rank + ign)
            HypixelDataHandler account = HypixelDataHandler.getUser(uuid);
            return account.get(HypixelDataHandler.Data.RANK, DatapointRank.class).getValue().getPrefix() +
                    account.get(HypixelDataHandler.Data.IGN, DatapointString.class).getValue();
        }
    }

    public static String getRawName(UUID uuid) {
        if (SkyBlockGenericLoader.getLoadedPlayers().stream().anyMatch(player -> player.getUuid().equals(uuid))) {
            return SkyBlockGenericLoader.getLoadedPlayers().stream().filter(player -> player.getUuid().equals(uuid)).findFirst().get().getUsername();
        } else {
            HypixelDataHandler account = HypixelDataHandler.getUser(uuid);
            return account.get(HypixelDataHandler.Data.IGN, DatapointString.class).getValue();
        }
    }
}
