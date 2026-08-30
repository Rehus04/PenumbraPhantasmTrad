package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.server.registry.ItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WallerItem extends FlavorTooltipItem
{
	public WallerItem(Properties pProperties)
	{
		super(pProperties);
	}

	public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> components, TooltipFlag pIsAdvanced)
	{
		super.appendHoverText(pStack, pLevel, components, pIsAdvanced);
		//just add component here.
	}

		@Override
	public void inventoryTick(ItemStack stack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected)
	{
		if(stack.getTag() == null)
		{
			stack.setTag(new CompoundTag());
			stack.getTag().putBoolean("shifting", false);
			stack.getTag().putInt("dollars", 0);
			stack.getTag().putInt("dimes", 0);
		}
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot,
											ClickAction action, Player player, SlotAccess access)
	{
		if(stack.getTag() == null)
			return false;

		if(!stack.getTag().contains("dollars"))
			stack.getTag().putInt("dollars", 0);
		if(!stack.getTag().contains("dimes"))
			stack.getTag().putInt("dimes", 0);

		if(otherStack.isEmpty())
		{
			if(action.equals(ClickAction.SECONDARY) && stack.getTag().getBoolean("shifting"))
			{
				stack.getTag().putBoolean("shifting", false);
				int dimes = stack.getTag().getInt("dimes");
				int dimesStackSize = Math.min(dimes, 64);
				stack.getTag().putInt("dimes", dimes-dimesStackSize);
				player.containerMenu.setCarried(new ItemStack(ItemRegistry.DARK_DIME.get(),
						dimesStackSize));
				return true;
			}
			else if(action.equals(ClickAction.SECONDARY))
			{
				int dollars = stack.getTag().getInt("dollars");
				int dollarStackSize = Math.min(dollars, 64);
				stack.getTag().putInt("dollars", dollars-dollarStackSize);
				player.containerMenu.setCarried(new ItemStack(ItemRegistry.DARK_DOLLAR.get(),
						dollarStackSize));
				return true;
			}
		}
		else
		{
			if(otherStack.is(ItemRegistry.DARK_DOLLAR.get()))
				stack.getTag().putInt("dollars", stack.getTag().getInt("dollars")+otherStack.getCount());
			if(otherStack.is(ItemRegistry.DARK_DIME.get()))
				stack.getTag().putInt("dimes", stack.getTag().getInt("dimes")+otherStack.getCount());

			otherStack.setCount(0);
			return true;
		}

		return false;
	}

	@Override
	public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player)
	{
		if(stack.getTag() == null)
			return false;

		if(!stack.getTag().contains("dollars"))
			stack.getTag().putInt("dollars", 0);
		if(!stack.getTag().contains("dimes"))
			stack.getTag().putInt("dimes", 0);

		ItemStack otherStack = slot.getItem();
		if(otherStack.is(ItemRegistry.DARK_DOLLAR.get()))
		{
			stack.getTag().putInt("dollars", stack.getTag().getInt("dollars") + otherStack.getCount());
			otherStack.setCount(0);
			return true;
		}
		if(otherStack.is(ItemRegistry.DARK_DIME.get()))
		{
			stack.getTag().putInt("dimes", stack.getTag().getInt("dimes") + otherStack.getCount());
			otherStack.setCount(0);
			return true;
		}
		return false;
	}
}
