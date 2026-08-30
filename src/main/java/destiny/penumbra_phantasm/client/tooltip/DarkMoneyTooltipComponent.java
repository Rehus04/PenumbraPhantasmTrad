package destiny.penumbra_phantasm.client.tooltip;

import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.registry.ItemRegistry;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class DarkMoneyTooltipComponent implements ClientTooltipComponent, TooltipComponent
{
	public static final ResourceLocation SLOT_TEXTURE = new ResourceLocation(PenumbraPhantasm.MODID,
			"textures/gui/dark_world/dark_wallet_slot.png");

	public int dollars;
	public int dimes;

	public DarkMoneyTooltipComponent(int dollars, int dimes)
	{
		this.dollars = dollars;
		this.dimes = dimes;
	}

	@Override
	public int getHeight()
	{
		if(dollars == 0 && dimes == 0)
			return 0;

		return 18;
	}

	@Override
	public int getWidth(Font pFont)
	{
		if(dollars == 0 && dimes == 0)
			return 0;
		if(dollars == 0 ^ dimes == 0)
			return 18;
		return 38;
	}

	@Override
	public void renderImage(Font font, int pX, int pY, GuiGraphics graphics)
	{
		final PoseStack pose = graphics.pose();

		pose.pushPose();
		if(dollars != 0)
		{
			ItemStack dollarStack = new ItemStack(ItemRegistry.DARK_DOLLAR.get(), dollars);
			graphics.blit(SLOT_TEXTURE, pX, pY, 0, 0, 18, 18, 18, 18);
			graphics.renderItem(dollarStack, pX+1, pY+1);
			pose.translate(20, 0, 0);
		}
		if(dimes != 0)
		{
			ItemStack dimesStack = new ItemStack(ItemRegistry.DARK_DIME.get(), dimes);
			graphics.blit(SLOT_TEXTURE, pX, pY, 0, 0, 18, 18, 18, 18);
			graphics.renderItem(dimesStack, pX+1, pY+1);
		}
		pose.popPose();
	}
}