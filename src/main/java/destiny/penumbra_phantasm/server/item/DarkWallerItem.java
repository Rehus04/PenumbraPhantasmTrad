package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
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

		int totalMoney = dollars + dimeDollars;

		if (totalMoney > 0) {
			components.add(Component.translatable("tooltip.penumbra_phantasm.dark_wallet.total_money")
					.append(Component.literal("" + totalMoney))
					.withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))));
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
		ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);

		if (dropContents(itemstack, pPlayer)) {
			this.playDropContentsSound(pPlayer);

			pPlayer.awardStat(Stats.ITEM_USED.get(this));

			return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
		} else {
			return InteractionResultHolder.fail(itemstack);
		}
	}

	private static boolean dropContents(ItemStack pStack, Player pPlayer) {
		if (pStack.getTag() == null) return false;

		if (pPlayer instanceof ServerPlayer) {
			int dollars = pStack.getTag().getInt(DOLLARS);
			int dimes = pStack.getTag().getInt(DIMES);

			if (dimes <= 0 && dollars <= 0) return false;

			if (dollars > 0) {
				ItemStack dollarStack = new ItemStack(ItemRegistry.DARK_DOLLAR.get(), dollars);
				pPlayer.drop(dollarStack, true);
				pStack.getTag().putInt(DOLLARS, 0);
			}
			if (dimes > 0) {
				ItemStack dimeStack = new ItemStack(ItemRegistry.DARK_DIME.get(), dimes);
				pPlayer.drop(dimeStack, true);
				pStack.getTag().putInt(DIMES, 0);
			}
		}

		return true;
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
			int dimes = stack.getTag().getInt(DIMES);

			if((action.equals(ClickAction.SECONDARY) && stack.getTag().getBoolean(SHIFTING)) || (action.equals(ClickAction.SECONDARY) && dimes <= 0))
			{
				int dollars = stack.getTag().getInt(DOLLARS);
				int dollarStackSize = Math.min(dollars, 64);
				stack.getTag().putInt(DOLLARS, dollars-dollarStackSize);
				player.containerMenu.setCarried(new ItemStack(ItemRegistry.DARK_DOLLAR.get(),
						dollarStackSize));
				this.playRemoveOneSound(player);
				return true;
			}
			else if(action.equals(ClickAction.SECONDARY))
			{
				stack.getTag().putBoolean(SHIFTING, false);
				int dimesStackSize = Math.min(dimes, 64);
				stack.getTag().putInt(DIMES, dimes-dimesStackSize);
				player.containerMenu.setCarried(new ItemStack(ItemRegistry.DARK_DIME.get(),
						dimesStackSize));
				this.playRemoveOneSound(player);
				return true;
			}
		}
		else {
			if (otherStack.is(ItemRegistry.DARK_DOLLAR.get())) {
				stack.getTag().putInt(DOLLARS, stack.getTag().getInt(DOLLARS) + otherStack.getCount());
				this.playInsertSound(player);

				otherStack.setCount(0);
				return true;
			}

			if (otherStack.is(ItemRegistry.DARK_DIME.get())) {
				stack.getTag().putInt(DIMES, stack.getTag().getInt(DIMES) + otherStack.getCount());
				this.playInsertSound(player);

				otherStack.setCount(0);
				return true;
			}
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

	private void playRemoveOneSound(Entity pEntity) {
		pEntity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + pEntity.level().getRandom().nextFloat() * 0.4F);
	}

	private void playInsertSound(Entity pEntity) {
		pEntity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + pEntity.level().getRandom().nextFloat() * 0.4F);
	}

	private void playDropContentsSound(Entity pEntity) {
		pEntity.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + pEntity.level().getRandom().nextFloat() * 0.4F);
	}
}
