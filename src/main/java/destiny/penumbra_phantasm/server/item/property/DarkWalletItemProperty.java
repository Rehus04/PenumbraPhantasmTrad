package destiny.penumbra_phantasm.server.item.property;

import destiny.penumbra_phantasm.server.item.DarkWallerItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class DarkWalletItemProperty implements ClampedItemPropertyFunction {
    @Override
    public float unclampedCall(ItemStack stack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i) {
        if (stack.getTag() != null) {
            int dollars = stack.getTag().getInt(DarkWallerItem.DOLLARS);
            int dimes = stack.getTag().getInt(DarkWallerItem.DIMES);

            if (dollars > 0 || dimes > 0) return 1;
        }

        return 0;
    }
}
