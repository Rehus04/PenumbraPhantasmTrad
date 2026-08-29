package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.capability.SoulCapability;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DarkWorldFoodFlavorItem extends FlavorTooltipItem {
    int determinationGain;
    SoundEvent consumeSound;

    public DarkWorldFoodFlavorItem(Properties pProperties, int determinationGain, SoundEvent consumeSound) {
        super(pProperties);
        this.determinationGain = determinationGain;
        this.consumeSound = consumeSound;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        if (pLivingEntity instanceof ServerPlayer player) {
            SoulCapability soulCap = player.getCapability(CapabilityRegistry.SOUL).orElse(null);
            soulCap.determination = Mth.clamp(soulCap.determination + determinationGain, 0, 100);

            pLevel.playSound(null, player.getOnPos(), consumeSound, SoundSource.PLAYERS, 0.5f, 1);
        }

        return super.finishUsingItem(pStack, pLevel, pLivingEntity);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> components, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, components, pIsAdvanced);

        FoodProperties foodProperties = pStack.getFoodProperties(null);

        int nutrition = 0;
        float saturationModifier = 0f;
        if (foodProperties != null) {
            nutrition = foodProperties.getNutrition();
            saturationModifier = foodProperties.getSaturationModifier();
        }

        if (nutrition != 0) {
            components.add(Component.literal("+" + nutrition + " ")
                    .append(Component.translatable("tooltip.penumbra_phantasm.nutrition"))
                    .withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))));
        }

        if (saturationModifier != 0) {
            float saturationPoints = (nutrition * saturationModifier * 2);

            components.add(Component.literal("+" + String.format("%.1f", saturationPoints) + " ")
                    .append(Component.translatable("tooltip.penumbra_phantasm.saturation"))
                    .withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))));
        }

        if (determinationGain != 0) {
            components.add(Component.literal("+" + determinationGain + " ")
                    .append(Component.translatable("tooltip.penumbra_phantasm.soul_hearth.soul_type.1"))
                    .withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))));
        }
    }
}
