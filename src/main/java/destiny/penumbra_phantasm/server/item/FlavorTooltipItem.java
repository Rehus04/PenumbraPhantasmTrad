package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FlavorTooltipItem extends Item {
    public FlavorTooltipItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> components, TooltipFlag pIsAdvanced) {
        ResourceLocation itemLocation = ForgeRegistries.ITEMS.getKey(pStack.getItem());

        if (itemLocation == null) return;

        String itemName = itemLocation.getNamespace();
        itemName = itemName + "." + itemLocation.getPath();

        String flavorText = Component.translatable("flavor_text." + itemName).getString();

        List<String> words = List.of(flavorText.split(" "));

        List<String> lines = new ArrayList<>();
        for (String word : words) {
            int linesIndex = lines.size() - 1;

            if (lines.isEmpty()) {
                lines.add(word);
            } else if (lines.get(linesIndex).length() < 40) {
                lines.set(linesIndex, lines.get(linesIndex) + " " + word);
            } else {
                lines.add(word);
            }
        }

        for (int i = 0; i < lines.size(); i++) {
            MutableComponent component = Component.empty();

            if (i == 0) {
                component.append(Component.literal("'"));
            }

            component.append(Component.literal(lines.get(i)));

            if (i == lines.size() - 1) {
                component.append(Component.literal("'"));
            }

            component.withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator")).withColor(ChatFormatting.DARK_GRAY));

            components.add(component);
        }
    }
}
