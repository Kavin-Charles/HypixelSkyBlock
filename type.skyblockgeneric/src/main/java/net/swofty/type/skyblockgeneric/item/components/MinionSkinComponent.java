package net.swofty.type.skyblockgeneric.item.components;

import lombok.Getter;
import net.minestom.server.color.Color;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Unit;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MinionSkinComponent extends SkyBlockItemComponent {
    @Getter
    private final MinionArmorPiece helmet;
    @Getter
    private final MinionArmorPiece chestplate;
    @Getter
    private final MinionArmorPiece leggings;
    @Getter
    private final MinionArmorPiece boots;
    @Getter
    private final String skinName;

    public MinionSkinComponent(String skinName, MinionArmorPiece helmet, MinionArmorPiece chestplate,
                               MinionArmorPiece leggings, MinionArmorPiece boots) {
        this.skinName = skinName;
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;

        addInheritedComponent(new ExtraRarityComponent("COSMETIC"));
        addInheritedComponent(new LoreUpdateComponent(getSkinLore(skinName), false));
    }

    public List<String> getSkinLore(String name) {
        return new ArrayList<>(List.of(
                "§7This Minion skin changes your",
                "§7minion's appearance to a",
                "§e" + name + "§7.",
                " ",
                "§7You can place this item in any",
                "§7minion of your choice!"
        ));
    }

    public ItemStack getHelmetStack() {
        return helmet.toItemStack();
    }

    public ItemStack getChestplateStack() {
        return chestplate.toItemStack();
    }

    public ItemStack getLeggingsStack() {
        return leggings.toItemStack();
    }

    public ItemStack getBootsStack() {
        return boots.toItemStack();
    }

    public record MinionArmorPiece(Material material, @Nullable String skullTexture, @Nullable Color leatherColor) {
        public ItemStack toItemStack() {
            ItemStack.Builder builder = ItemStack.builder(material);

            if (skullTexture != null && material == Material.PLAYER_HEAD) {
                return ItemStackCreator.getStackHead(skullTexture).build();
            }

            if (leatherColor != null && material.name().startsWith("LEATHER_")) {
                builder.set(DataComponents.DYED_COLOR, leatherColor);
                builder = ItemStackCreator.clearAttributes(builder);
            }

            return builder.build();
        }

        public static MinionArmorPiece fromConfig(Map<String, Object> config) {
            String materialStr = (String) config.get("material");
            Material material = Material.values().stream()
                    .filter(m -> materialStr.equals(m.key().value()))
                    .findFirst()
                    .orElse(Material.AIR);

            String skullTexture = (String) config.get("skull_texture");

            Map<String, Object> colorConfig = (Map<String, Object>) config.get("leather_color");
            Color leatherColor = null;
            if (colorConfig != null) {
                int r = (int) colorConfig.get("r");
                int g = (int) colorConfig.get("g");
                int b = (int) colorConfig.get("b");
                leatherColor = new Color(r, g, b);
            }

            return new MinionArmorPiece(material, skullTexture, leatherColor);
        }
    }
}
