package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
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

public class DarkWallerItem extends FlavorTooltipItem
{
	public static final String DOLLARS = "dollars";
	public static final String DIMES = "dimes";
	public static final String SHIFTING = "shifting";

	public DarkWallerItem(Properties pProperties)
	{
		super(pProperties);
	}

	public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> components, TooltipFlag pIsAdvanced)
	{
		super.appendHoverText(pStack, pLevel, components, pIsAdvanced);

		if (pStack.getTag() == null) return;

		int dollars = pStack.getTag().getInt(DOLLARS);
		int dimes = pStack.getTag().getInt(DIMES);
		int dimeDollars = dimes / 10;

		int totalMoney = dollars * 10;
		totalMoney = totalMoney + dimeDollars;

		if (totalMoney > 0) {
			components.add(Component.translatable("tooltip.penumbra_phantasm.dark_wallet.total_money")
					.append(Component.literal("" + totalMoney))
					.withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))));
		}
	}

		@Override
	public void inventoryTick(ItemStack stack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected)
	{
		if(stack.getTag() == null)
		{
			stack.setTag(new CompoundTag());
			stack.getTag().putBoolean(SHIFTING, false);
			stack.getTag().putInt(DOLLARS, 0);
			stack.getTag().putInt(DIMES, 0);
		}
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot,
											ClickAction action, Player player, SlotAccess access)
	{
		if(stack.getTag() == null)
			return false;

		if(!stack.getTag().contains(DOLLARS))
			stack.getTag().putInt(DOLLARS, 0);
		if(!stack.getTag().contains(DIMES))
			stack.getTag().putInt(DIMES, 0);

		if(otherStack.isEmpty())
		{
			int dollars = stack.getTag().getInt(DOLLARS);

			if(action.equals(ClickAction.SECONDARY) && stack.getTag().getBoolean(SHIFTING) || action.equals(ClickAction.SECONDARY) && dollars <= 0)
			{
				stack.getTag().putBoolean(SHIFTING, false);
				int dimes = stack.getTag().getInt(DIMES);
				int dimesStackSize = Math.min(dimes, 64);
				stack.getTag().putInt(DIMES, dimes-dimesStackSize);
				player.containerMenu.setCarried(new ItemStack(ItemRegistry.DARK_DIME.get(),
						dimesStackSize));
				return true;
			}
			else if(action.equals(ClickAction.SECONDARY))
			{
				int dollarStackSize = Math.min(dollars, 64);
				stack.getTag().putInt(DOLLARS, dollars-dollarStackSize);
				player.containerMenu.setCarried(new ItemStack(ItemRegistry.DARK_DOLLAR.get(),
						dollarStackSize));
				return true;
			}
		}
		else
		{
			if(otherStack.is(ItemRegistry.DARK_DOLLAR.get()))
				stack.getTag().putInt(DOLLARS, stack.getTag().getInt(DOLLARS)+otherStack.getCount());
			if(otherStack.is(ItemRegistry.DARK_DIME.get()))
				stack.getTag().putInt(DIMES, stack.getTag().getInt(DIMES)+otherStack.getCount());

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

		if(!stack.getTag().contains(DOLLARS))
			stack.getTag().putInt(DOLLARS, 0);
		if(!stack.getTag().contains(DIMES))
			stack.getTag().putInt(DIMES, 0);

		ItemStack otherStack = slot.getItem();
		if(otherStack.is(ItemRegistry.DARK_DOLLAR.get()))
		{
			stack.getTag().putInt(DOLLARS, stack.getTag().getInt(DOLLARS) + otherStack.getCount());
			otherStack.setCount(0);
			return true;
		}
		if(otherStack.is(ItemRegistry.DARK_DIME.get()))
		{
			stack.getTag().putInt(DIMES, stack.getTag().getInt(DIMES) + otherStack.getCount());
			otherStack.setCount(0);
			return true;
		}
		return false;
	}
}
