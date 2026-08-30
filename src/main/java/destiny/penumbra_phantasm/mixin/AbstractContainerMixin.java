package destiny.penumbra_phantasm.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import destiny.penumbra_phantasm.server.item.DarkWallerItem;
import destiny.penumbra_phantasm.server.registry.ItemRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMixin
{
	@Shadow @Final public NonNullList<Slot> slots;

	@ModifyVariable(method = "doClick", at = @At("HEAD"), argsOnly = true)
	public ClickType dontQuickMoveWaller(ClickType pClickType,
										 @Local(argsOnly = true) Player player,
										 @Local(argsOnly = true, ordinal = 0) int slotId,
										 @Local(argsOnly = true, ordinal = 1) int button)
	{
		if(slotId == -999)
			return pClickType;
		ItemStack carried = player.containerMenu.getCarried();
		ItemStack slotStack = this.slots.get(slotId).getItem();

		if(pClickType == ClickType.QUICK_MOVE && button == 1 && carried.isEmpty() && slotStack.is(ItemRegistry.DARK_WALLET.get()))
		{
			slotStack.getOrCreateTag().putBoolean(DarkWallerItem.SHIFTING, true);
			return ClickType.PICKUP;
		}
		else return pClickType;
	}
}
