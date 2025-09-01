package net.swofty.type.skyblockgeneric.item;

import net.minestom.server.color.Color;
import net.swofty.commons.ServiceType;
import net.swofty.commons.item.ItemType;
import net.swofty.commons.item.Rarity;
import net.swofty.commons.item.attribute.attributes.*;
import net.swofty.commons.item.reforge.Reforge;
import net.swofty.commons.item.reforge.ReforgeLoader;
import net.swofty.commons.protocol.objects.itemtracker.TrackedItemUpdateProtocolObject;
import net.swofty.commons.statistics.ItemStatistics;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.item.components.*;
import net.swofty.type.skyblockgeneric.minion.MinionRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ItemAttributeHandler {
    SkyBlockItem item;

    public ItemAttributeHandler(SkyBlockItem item) {
        this.item = item;
    }

    public String getTypeAsString() {
        return ((ItemAttributeType) item.getAttribute("item_type")).getValue();
    }

    public boolean shouldBeEnchanted() {
        return item.hasComponent(EnchantedComponent.class);
    }

    public @Nullable ItemAttributeSandboxItem.SandboxData getSandboxData() {
        return ((ItemAttributeSandboxItem) item.getAttribute("sandboxdata")).getValue();
    }

    public void setSandboxData(ItemAttributeSandboxItem.SandboxData data) {
        item.getAttribute("sandboxdata").setValue(data);
    }

    public int getRuneLevel() {
        if (!(item.hasComponent(RuneableComponent.class))) {
            throw new RuntimeException("Item is not a rune item " + getTypeAsString());
        }
        return ((ItemAttributeRuneLevel) item.getAttribute("rune_level")).getValue();
    }

    public void setRuneLevel(int level) {
        if (!(item.hasComponent(RuneableComponent.class))) throw new RuntimeException("Item is not a rune item");
        ((ItemAttributeRuneLevel) item.getAttribute("rune_level")).setValue(level);
    }

    public ItemAttributeHotPotatoBookData.HotPotatoBookData getHotPotatoBookData() {
        return ((ItemAttributeHotPotatoBookData) item.getAttribute("hot_potato_book_data")).getValue();
    }

    public void setHotPotatoBookData(ItemAttributeHotPotatoBookData.HotPotatoBookData data) {
        item.getAttribute("hot_potato_book_data").setValue(data);
    }

    public ItemAttributeRuneInfusedWith.RuneData getRuneData() {
        return ((ItemAttributeRuneInfusedWith) item.getAttribute("rune_infused_with")).getValue();
    }

    public void setRuneData(ItemAttributeRuneInfusedWith.RuneData data) {
        ((ItemAttributeRuneInfusedWith) item.getAttribute("rune_infused_with")).setValue(data);
    }

    public boolean isPet() {
        return item.hasComponent(PetComponent.class);
    }

    public ItemAttributePetData.PetData getPetData() {
        if (item.hasComponent(PetComponent.class)) {
            return ((ItemAttributePetData) item.getAttribute("pet_data")).getValue();
        } else {
            throw new RuntimeException("Item is not a pet");
        }
    }

    public Color getLeatherColour() {
        if (item.hasComponent(LeatherColorComponent.class)) {
            return item.getComponent(LeatherColorComponent.class).getColor();
        }
        return null;
    }

    public void setSoulBound(boolean coopAllowed) {
        item.getAttribute("soul_bound").setValue(
                new ItemAttributeSoulbound.SoulBoundData(coopAllowed)
        );
    }

    public ItemAttributeSoulbound.SoulBoundData getSoulBoundData() {
        ItemAttributeSoulbound.SoulBoundData potentialData = ((ItemAttributeSoulbound) item
                .getAttribute("soul_bound"))
                .getValue();
        if (potentialData != null) return potentialData;
        if (item.hasComponent(DefaultSoulboundComponent.class))
            return new ItemAttributeSoulbound.SoulBoundData(
                    item.getComponent(DefaultSoulboundComponent.class).isCoopAllowed());
        return null;
    }

    public @Nullable ItemAttributeGemData.GemData getGemData() {
        if (item.hasComponent(GemstoneComponent.class)) {
            return ((ItemAttributeGemData) item.getAttribute("gems")).getValue();
        } else {
            return null;
        }
    }

    public void setGemData(ItemAttributeGemData.GemData data) {
        if (item.hasComponent(GemstoneComponent.class)) {
            ((ItemAttributeGemData) item.getAttribute("gems")).setValue(data);
        } else {
            throw new RuntimeException("Item is not a gemstone item");
        }
    }

    public void setPetData(ItemAttributePetData.PetData data) {
        if (item.hasComponent(PetItemComponent.class)) {
            ((ItemAttributePetData) item.getAttribute("pet_data")).setValue(data);
        } else {
            throw new RuntimeException("Item is not a pet");
        }
    }

    public ItemAttributeBackpackData.BackpackData getBackpackData() {
        if (item.hasComponent(BackpackComponent.class)) {
            return ((ItemAttributeBackpackData) item.getAttribute("backpack_data")).getValue();
        } else {
            throw new RuntimeException("Item is not a backpack");
        }
    }

    public void setBackpackData(ItemAttributeBackpackData.BackpackData data) {
        if (item.hasComponent(BackpackComponent.class)) {
            ((ItemAttributeBackpackData) item.getAttribute("backpack_data")).setValue(data);
        } else {
            throw new RuntimeException("Item is not a backpack");
        }
    }

    public @Nullable ItemType getPotentialType() {
        try {
            return ItemType.valueOf(((ItemAttributeType) item.getAttribute("item_type")).getValue());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public Rarity getRarity() {
        return ((ItemAttributeRarity) item.getAttribute("rarity")).getValue();
    }

    public @Nullable String getUniqueTrackedID() {
        String value = ((ItemAttributeUniqueTrackedID) item.getAttribute("unique-tracked-id")).getValue();
        if (value.equals("none")) return null;
        return value;
    }

    public void setUniqueTrackedID(String uniqueTrackedID, SkyBlockPlayer player) {
        item.getAttribute("unique-tracked-id").setValue(uniqueTrackedID);

        Thread.startVirtualThread(() -> {
            ProxyService itemTracker = new ProxyService(ServiceType.ITEM_TRACKER);
            if (!itemTracker.isOnline().join()) return;

            TrackedItemUpdateProtocolObject.TrackedItemUpdateMessage message =
                    new TrackedItemUpdateProtocolObject.TrackedItemUpdateMessage(
                            UUID.fromString(uniqueTrackedID),
                            player.getUuid(),
                            player.getProfiles().getCurrentlySelected(),
                            item.getAttributeHandler().getTypeAsString()
            );

            CompletableFuture<TrackedItemUpdateProtocolObject.TrackedItemUpdateResponse> future
                    = itemTracker.handleRequest(message);
        });
    }

    public void setRarity(Rarity rarity) {
        ((ItemAttributeRarity) item.getAttribute("rarity")).setValue(rarity);
    }

    public boolean hasEnchantment(EnchantmentType type) {
        return ((ItemAttributeEnchantments) item.getAttribute("enchantments")).getValue()
                .enchantments()
                .stream()
                .anyMatch(enchantment -> new SkyBlockEnchantment(enchantment).type() == type);
    }

    public void removeEnchantment(EnchantmentType type) {
        ((ItemAttributeEnchantments) item.getAttribute("enchantments")).getValue()
                .enchantments()
                .removeIf(enchantment -> new SkyBlockEnchantment(enchantment).type() == type);
    }

    public @Nullable Reforge getReforge() {
        String reforgeName = ((ItemAttributeReforge) item.getAttribute("reforge")).getValue();
        if (reforgeName == null || reforgeName.isEmpty()) {
            return null;
        }
        return ReforgeLoader.getReforge(reforgeName);
    }

    public MinionRegistry getMinionType() {
        if (item.hasComponent(MinionComponent.class)) {
            return item.getComponent(MinionComponent.class).getMinionRegistry();
        } else {
            throw new RuntimeException("Item is not a minion");
        }
    }

    public ItemAttributeMinionData.MinionData getMinionData() {
        if (item.hasComponent(MinionComponent.class)) {
            return ((ItemAttributeMinionData) item.getAttribute("minion_tier")).getValue();
        } else {
            throw new RuntimeException("Item is not a minion");
        }
    }

    public void setMinionData(ItemAttributeMinionData.MinionData data) {
        item.getAttribute("minion_tier").setValue(data);
    }

    public void setReforge(Reforge reforge) throws IllegalArgumentException {
        if (!item.getAttributeHandler().getRarity().isReforgable())
            throw new IllegalArgumentException("The rarity " + item.getAttributeHandler().getRarity().name() + " is not reforgable.");

        String reforgeName = (reforge != null) ? reforge.getName() : null;
        item.getAttribute("reforge").setValue(reforgeName);
    }

    public void setReforge(String reforgeName) throws IllegalArgumentException {
        if (!item.getAttributeHandler().getRarity().isReforgable())
            throw new IllegalArgumentException("The rarity " + item.getAttributeHandler().getRarity().name() + " is not reforgable.");

        // Validate the reforge exists
        if (reforgeName != null && !reforgeName.isEmpty()) {
            Reforge reforge = ReforgeLoader.getReforge(reforgeName);
            if (reforge == null) {
                throw new IllegalArgumentException("Unknown reforge: " + reforgeName);
            }
        }

        item.getAttribute("reforge").setValue(reforgeName);
    }

    public @Nullable SkyBlockEnchantment getEnchantment(EnchantmentType type) {
        return ((ItemAttributeEnchantments) item.getAttribute("enchantments")).getValue()
                .enchantments()
                .stream()
                .filter(enchantment -> new SkyBlockEnchantment(enchantment).type() == type)
                .findFirst()
                .map(SkyBlockEnchantment::new)
                .orElse(null);
    }

    public Stream<SkyBlockEnchantment> getEnchantments() {
        return ((ItemAttributeEnchantments) item.getAttribute("enchantments")).getValue()
                .enchantments().stream()
                .map(SkyBlockEnchantment::new);
    }

    public void addEnchantment(SkyBlockEnchantment enchantment) {
        ((ItemAttributeEnchantments) item.getAttribute("enchantments")).getValue()
                .addEnchantment(enchantment.toUnderstandable());
    }

    public ItemStatistics getStatistics() {
        return ((ItemAttributeStatistics) item.getAttribute("statistics")).getValue().clone();
    }

    public void setStatistics(ItemStatistics statistics) {
        ((ItemAttributeStatistics) item.getAttribute("statistics")).setValue(statistics);
    }

    public void setRecombobulated(boolean value) {
        ((ItemAttributeRecombobulated) item.getAttribute("recombobulated")).setValue(value);
    }

    public boolean isRecombobulated() {
        return ((ItemAttributeRecombobulated) item.getAttribute("recombobulated")).getValue();
    }

    public boolean isMithrilInfused() {
        if (item.hasComponent(MinionComponent.class)) {
            return ((ItemAttributeMithrilInfusion) item.getAttribute("mithril_infusion")).getValue();
        } else {
            throw new RuntimeException("Item is not a minion");
        }
    }

    public void setMithrilInfused(boolean value) {
        if (item.hasComponent(MinionComponent.class)) {
            ((ItemAttributeMithrilInfusion) item.getAttribute("mithril_infusion")).setValue(value);
        } else {
            throw new RuntimeException("Item is not a minion");
        }
    }

    public int getBreakingPower() {
        return ((ItemAttributeBreakingPower) item.getAttribute("breaking-power")).getValue();
    }

    public void setBreakingPower(int breakingPower) {
        ((ItemAttributeBreakingPower) item.getAttribute("breaking-power")).setValue(breakingPower);
    }

    public boolean isMiningTool() {
        return getBreakingPower() != 0;
    }

    public SkyBlockItem asSkyBlockItem() {
        return item;
    }
}
