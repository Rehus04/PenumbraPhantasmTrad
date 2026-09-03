package destiny.penumbra_phantasm.client.render.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.RenderBlitUtil;
import destiny.penumbra_phantasm.client.render.menu.DarkWorldInventoryMenu;
import destiny.penumbra_phantasm.client.render.screen.component.DarkWorldRecipeBookComponent;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.common.MinecraftForge;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class DarkWorldInventoryScreen extends EffectRenderingInventoryScreen<DarkWorldInventoryMenu> implements RecipeUpdateListener {
    private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/recipe_button.png");
    private static final int NARROW_SCREEN_WIDTH = 379;
    public static final ResourceLocation DARK_WORLD_INVENTORY_LOCATION = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/container/inventory.png");
    public static final ResourceLocation DARK_WORLD_INVENTORY_GLOW_LOCATION = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/container/inventory_glow.png");
    private static final ResourceLocation EMPTY_EQUIPMENT_SLOT_1 = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/container/empty_equipment_slot_1.png");
    private static final ResourceLocation EMPTY_EQUIPMENT_SLOT_2 = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/container/empty_equipment_slot_2.png");
    private static final ResourceLocation EMPTY_EQUIPMENT_SLOT_3 = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/container/empty_equipment_slot_3.png");
    private static final ResourceLocation EMPTY_EQUIPMENT_SLOT_4 = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/container/empty_equipment_slot_4.png");
    private static final ResourceLocation EMPTY_EQUIPMENT_SLOT_5 = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/container/empty_equipment_slot_5.png");
    public static final int GLOW_TICKER_UPPER_BOUND = 5 * 20;

    private float xMouse;
    private float yMouse;
    private final DarkWorldRecipeBookComponent recipeBookComponent = new DarkWorldRecipeBookComponent();
    private boolean widthTooNarrow;
    private boolean buttonClicked;
    private final Player player;
    private final AbstractContainerMenu previousMenu;
    public Component inventoryLabel = Component.translatable("gui.penumbra_phantasm.dark_world_inventory_title");
    public Component craftingLabel = Component.translatable("gui.penumbra_phantasm.dark_world_inventory_crafting");
    public Component equippedLabel = Component.translatable("gui.penumbra_phantasm.dark_world_inventory_equipped");
    public int equippedLabelX = 57;
    public int equippedLabelY = 4;

    private int glowTicker;

    public DarkWorldInventoryScreen(Player pPlayer) {
        super(new DarkWorldInventoryMenu(pPlayer.getInventory(), pPlayer), pPlayer.getInventory(), Component.translatable("container.crafting"));
        this.imageWidth = 188;
        this.imageHeight = 185;
        this.inventoryLabelX = 57;
        this.inventoryLabelY = 87;
        this.titleLabelX = 137;
        this.titleLabelY = 4;
        this.player = pPlayer;
        this.previousMenu = pPlayer.containerMenu;
    }

    @Override
    public void containerTick() {
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(new DarkWorldCreativeModeInventoryScreen(this.minecraft.player, this.minecraft.player.connection.enabledFeatures(), this.minecraft.options.operatorItemsTab().get()));
        } else {
            this.recipeBookComponent.tick();
        }

        this.glowTicker++;
        if (this.glowTicker >= GLOW_TICKER_UPPER_BOUND) {
            this.glowTicker = 0;
        }
    }

    @Override
    protected void init() {
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(new DarkWorldCreativeModeInventoryScreen(this.minecraft.player, this.minecraft.player.connection.enabledFeatures(), this.minecraft.options.operatorItemsTab().get()));
        } else {
            this.player.containerMenu = this.menu;
            super.init();
            this.widthTooNarrow = this.width < NARROW_SCREEN_WIDTH;
            this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            this.addRenderableWidget(new ImageButton(this.leftPos + 104, this.height / 2 - 22, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, (p_289631_) -> {
                this.recipeBookComponent.toggleVisibility();
                this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
                p_289631_.setPosition(this.leftPos + 104, this.height / 2 - 22);
                this.buttonClicked = true;
            }));
            this.addWidget(this.recipeBookComponent);
            this.setInitialFocus(this.recipeBookComponent);

            Random random = new Random();
            this.glowTicker = random.nextInt(0, 21);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        this.drawCenteredString(pGuiGraphics, this.font, this.equippedLabel, this.equippedLabelX, this.equippedLabelY, -1, false);
        this.drawCenteredString(pGuiGraphics, this.font, this.craftingLabel, this.titleLabelX, this.titleLabelY, -1, false);
        this.drawCenteredString(pGuiGraphics, this.font, this.inventoryLabel, this.inventoryLabelX, this.inventoryLabelY, -1, false);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.renderBg(pGuiGraphics, pPartialTick, pMouseX, pMouseY);
            this.recipeBookComponent.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick, 0);
        } else {
            this.recipeBookComponent.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick, 0);
            renderVanilla(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            this.renderEffects(pGuiGraphics, pMouseX, pMouseY);
            this.recipeBookComponent.renderGhostRecipe(pGuiGraphics, this.leftPos, this.topPos, true, pPartialTick);
        }

        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
        this.recipeBookComponent.renderTooltip(pGuiGraphics, this.leftPos, this.topPos, pMouseX, pMouseY);
        this.xMouse = pMouseX;
        this.yMouse = pMouseY;
    }

    public void renderVanilla(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int i = this.leftPos;
        int j = this.topPos;
        this.renderBg(pGuiGraphics, pPartialTick, pMouseX, pMouseY);
        MinecraftForge.EVENT_BUS.post(new ContainerScreenEvent.Render.Background(this, pGuiGraphics, pMouseX, pMouseY));
        RenderSystem.disableDepthTest();
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().translate((float)i, (float)j, 0.0F);
        this.hoveredSlot = null;

        for(int k = 0; k < this.menu.slots.size(); ++k) {
            if (k >= 5 && k < 9) {
                continue;
            }

            Slot slot = this.menu.slots.get(k);
            if (slot.isActive()) {
                this.renderSlot(pGuiGraphics, slot);
            }

            if (this.isHovering(slot, pMouseX, pMouseY) && slot.isActive()) {
                this.hoveredSlot = slot;
                int l = slot.x;
                int i1 = slot.y;
                if (this.hoveredSlot.isHighlightable()) {
                    renderSlotHighlight(pGuiGraphics, l, i1, 0, this.getSlotColor(k));
                }
            }
        }

        this.renderLabels(pGuiGraphics, pMouseX, pMouseY);
        MinecraftForge.EVENT_BUS.post(new ContainerScreenEvent.Render.Foreground(this, pGuiGraphics, pMouseX, pMouseY));
        ItemStack itemstack = this.draggingItem.isEmpty() ? this.menu.getCarried() : this.draggingItem;
        if (!itemstack.isEmpty()) {
            int l1 = 8;
            int i2 = this.draggingItem.isEmpty() ? 8 : 16;
            String s = null;
            if (!this.draggingItem.isEmpty() && this.isSplittingStack) {
                itemstack = itemstack.copyWithCount(Mth.ceil((float)itemstack.getCount() / 2.0F));
            } else if (this.isQuickCrafting && this.quickCraftSlots.size() > 1) {
                itemstack = itemstack.copyWithCount(this.quickCraftingRemainder);
                if (itemstack.isEmpty()) {
                    s = ChatFormatting.YELLOW + "0";
                }
            }

            this.renderFloatingItem(pGuiGraphics, itemstack, pMouseX - i - 8, pMouseY - j - i2, s);
        }

        if (!this.snapbackItem.isEmpty()) {
            float f = (float)(Util.getMillis() - this.snapbackTime) / 100.0F;
            if (f >= 1.0F) {
                f = 1.0F;
                this.snapbackItem = ItemStack.EMPTY;
            }

            int j2 = this.snapbackEnd.x - this.snapbackStartX;
            int k2 = this.snapbackEnd.y - this.snapbackStartY;
            int j1 = this.snapbackStartX + (int)((float)j2 * f);
            int k1 = this.snapbackStartY + (int)((float)k2 * f);
            this.renderFloatingItem(pGuiGraphics, this.snapbackItem, j1, k1, null);
        }

        pGuiGraphics.pose().popPose();
        RenderSystem.enableDepthTest();
    }

    private void renderEffects(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        int i = this.leftPos + this.imageWidth + 2;
        int j = this.width - i;
        Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
        if (!collection.isEmpty() && j >= 32) {
            boolean flag = j >= 120;
            ScreenEvent.RenderInventoryMobEffects event = ForgeHooksClient.onScreenPotionSize(this, j, !flag, i);
            if (event.isCanceled()) {
                return;
            }

            flag = !event.isCompact();
            i = event.getHorizontalOffset();
            int k = 33;
            if (collection.size() > 5) {
                k = 132 / (collection.size() - 1);
            }

            Iterable<MobEffectInstance> iterable = collection.stream().filter(ForgeHooksClient::shouldRenderEffect).sorted().collect(Collectors.toList());
            this.renderBackgrounds(pGuiGraphics, i, k, iterable, flag);
            this.renderIcons(pGuiGraphics, i, k, iterable, flag);
            if (flag) {
                this.renderLabels(pGuiGraphics, i, k, iterable);
            } else if (pMouseX >= i && pMouseX <= i + 33) {
                int l = this.topPos;
                MobEffectInstance mobeffectinstance = null;

                for(MobEffectInstance mobeffectinstance1 : iterable) {
                    if (pMouseY >= l && pMouseY <= l + k) {
                        mobeffectinstance = mobeffectinstance1;
                    }

                    l += k;
                }

                if (mobeffectinstance != null) {
                    List<Component> list = List.of(this.getEffectName(mobeffectinstance), MobEffectUtil.formatDuration(mobeffectinstance, 1.0F));
                    pGuiGraphics.renderTooltip(this.font, list, Optional.empty(), pMouseX, pMouseY);
                }
            }
        }

    }

    private void renderBackgrounds(GuiGraphics pGuiGraphics, int pRenderX, int pYOffset, Iterable<MobEffectInstance> pEffects, boolean pIsSmall) {
        int i = this.topPos;

        for(MobEffectInstance mobeffectinstance : pEffects) {
            if (pIsSmall) {
                pGuiGraphics.blit(INVENTORY_LOCATION, pRenderX, i, 0, 166, 120, 32);
            } else {
                pGuiGraphics.blit(INVENTORY_LOCATION, pRenderX, i, 0, 198, 32, 32);
            }

            i += pYOffset;
        }

    }

    private void renderIcons(GuiGraphics pGuiGraphics, int pRenderX, int pYOffset, Iterable<MobEffectInstance> pEffects, boolean pIsSmall) {
        MobEffectTextureManager mobeffecttexturemanager = this.minecraft.getMobEffectTextures();
        int i = this.topPos;

        for(MobEffectInstance mobeffectinstance : pEffects) {
            IClientMobEffectExtensions renderer = IClientMobEffectExtensions.of(mobeffectinstance);
            if (renderer.renderInventoryIcon(mobeffectinstance, this, pGuiGraphics, pRenderX + (pIsSmall ? 6 : 7), i, 0)) {
                i += pYOffset;
            } else {
                MobEffect mobeffect = mobeffectinstance.getEffect();
                TextureAtlasSprite textureatlassprite = mobeffecttexturemanager.get(mobeffect);
                pGuiGraphics.blit(pRenderX + (pIsSmall ? 6 : 7), i + 7, 0, 18, 18, textureatlassprite);
                i += pYOffset;
            }
        }

    }

    private void renderLabels(GuiGraphics pGuiGraphics, int pRenderX, int pYOffset, Iterable<MobEffectInstance> pEffects) {
        int i = this.topPos;

        for(MobEffectInstance mobeffectinstance : pEffects) {
            IClientMobEffectExtensions renderer = IClientMobEffectExtensions.of(mobeffectinstance);
            if (renderer.renderInventoryText(mobeffectinstance, this, pGuiGraphics, pRenderX, i, 0)) {
                i += pYOffset;
            } else {
                Component component = this.getEffectName(mobeffectinstance);
                pGuiGraphics.drawString(this.font, component, pRenderX + 10 + 18, i + 6, 16777215);
                Component component1 = MobEffectUtil.formatDuration(mobeffectinstance, 1.0F);
                pGuiGraphics.drawString(this.font, component1, pRenderX + 10 + 18, i + 6 + 10, 8355711);
                i += pYOffset;
            }
        }

    }

    private Component getEffectName(MobEffectInstance pEffect) {
        MutableComponent mutablecomponent = pEffect.getEffect().getDisplayName().copy();
        if (pEffect.getAmplifier() >= 1 && pEffect.getAmplifier() <= 9) {
            MutableComponent var10000 = mutablecomponent.append(CommonComponents.SPACE);
            int var10001 = pEffect.getAmplifier();
            var10000.append(Component.translatable("enchantment.level." + (var10001 + 1)));
        }

        return mutablecomponent;
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        float t = (float) this.glowTicker / (float) GLOW_TICKER_UPPER_BOUND;
        float glow = Mth.sin(t * Mth.PI);

        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null && DarkWorldUtil.isDepths(player.level())) {
            glow = 0;
        }

        int i = this.leftPos;
        int j = this.topPos;
        pGuiGraphics.blit(DARK_WORLD_INVENTORY_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);
        RenderBlitUtil.blitGui(pGuiGraphics, DARK_WORLD_INVENTORY_GLOW_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight, glow, glow, glow, 1);
        renderEntityInInventoryFollowsMouse(pGuiGraphics, i + 57, j + 81, 30, (float)(i + 57) - this.xMouse, (float)(j + 81 - 50) - this.yMouse, this.minecraft.player);
    }

    public static void renderEntityInInventoryFollowsMouse(GuiGraphics pGuiGraphics, int pX, int pY, int pScale, float pMouseX, float pMouseY, LivingEntity pEntity) {
        float f = (float)Math.atan((pMouseX / 40.0F));
        float f1 = (float)Math.atan((pMouseY / 40.0F));
        renderEntityInInventoryFollowsAngle(pGuiGraphics, pX, pY, pScale, f, f1, pEntity);
    }

    public static void renderEntityInInventoryFollowsAngle(GuiGraphics pGuiGraphics, int pX, int pY, int pScale, float angleXComponent, float angleYComponent, LivingEntity pEntity) {
        Quaternionf quaternionf = (new Quaternionf()).rotateZ((float)Math.PI);
        Quaternionf quaternionf1 = (new Quaternionf()).rotateX(angleYComponent * 20.0F * ((float)Math.PI / 180F));
        quaternionf.mul(quaternionf1);
        float f = pEntity.yBodyRot;
        float f1 = pEntity.getYRot();
        float f2 = pEntity.getXRot();
        float f3 = pEntity.yHeadRotO;
        float f4 = pEntity.yHeadRot;
        pEntity.yBodyRot = 180.0F + angleXComponent * 20.0F;
        pEntity.setYRot(180.0F + angleXComponent * 40.0F);
        pEntity.setXRot(-angleYComponent * 20.0F);
        pEntity.yHeadRot = pEntity.getYRot();
        pEntity.yHeadRotO = pEntity.getYRot();
        renderEntityInInventory(pGuiGraphics, pX, pY, pScale, quaternionf, quaternionf1, pEntity);
        pEntity.yBodyRot = f;
        pEntity.setYRot(f1);
        pEntity.setXRot(f2);
        pEntity.yHeadRotO = f3;
        pEntity.yHeadRot = f4;
    }

    public static void renderEntityInInventory(GuiGraphics pGuiGraphics, int pX, int pY, int pScale, Quaternionf pPose, @Nullable Quaternionf pCameraOrientation, LivingEntity pEntity) {
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().translate(pX, pY, 50.0D);
        pGuiGraphics.pose().mulPoseMatrix((new Matrix4f()).scaling((float)pScale, (float)pScale, (float)(-pScale)));
        pGuiGraphics.pose().mulPose(pPose);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (pCameraOrientation != null) {
            pCameraOrientation.conjugate();
            entityrenderdispatcher.overrideCameraOrientation(pCameraOrientation);
        }

        entityrenderdispatcher.setRenderShadow(false);
        RenderSystem.runAsFancy(() -> entityrenderdispatcher.render(pEntity, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, pGuiGraphics.pose(), pGuiGraphics.bufferSource(), 15728880));
        pGuiGraphics.flush();
        entityrenderdispatcher.setRenderShadow(true);
        pGuiGraphics.pose().popPose();
        Lighting.setupFor3DItems();
    }

    @Override
    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(pX, pY, pWidth, pHeight, pMouseX, pMouseY);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.recipeBookComponent.mouseClicked(pMouseX, pMouseY, pButton)) {
            this.setFocused(this.recipeBookComponent);
            return true;
        }
        return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (this.buttonClicked) {
            this.buttonClicked = false;
            return true;
        }
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        return this.recipeBookComponent.keyPressed(pKeyCode, pScanCode, pModifiers) || super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        return this.recipeBookComponent.charTyped(pCodePoint, pModifiers) || super.charTyped(pCodePoint, pModifiers);
    }

    @Override
    protected boolean hasClickedOutside(double pMouseX, double pMouseY, int pGuiLeft, int pGuiTop, int pMouseButton) {
        boolean flag = pMouseX < (double)pGuiLeft || pMouseY < (double)pGuiTop || pMouseX >= (double)(pGuiLeft + this.imageWidth) || pMouseY >= (double)(pGuiTop + this.imageHeight);
        return this.recipeBookComponent.hasClickedOutside(pMouseX, pMouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, pMouseButton) && flag;
    }

    @Override
    protected void slotClicked(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType) {
        super.slotClicked(pSlot, pSlotId, pMouseButton, pType);
        this.recipeBookComponent.slotClicked(pSlot);
    }

    @Override
    public void recipesUpdated() {
        this.recipeBookComponent.recipesUpdated();
    }

    @Override
    public DarkWorldRecipeBookComponent getRecipeBookComponent() {
        return this.recipeBookComponent;
    }

    @Override
    public void removed() {
        this.player.containerMenu = this.previousMenu != null ? this.previousMenu : this.player.inventoryMenu;
        super.removed();
    }

    @Override
    public void renderSlot(GuiGraphics pGuiGraphics, Slot pSlot) {
        int x = pSlot.x;
        int y = pSlot.y;
        ItemStack itemstack = pSlot.getItem();
        boolean flag = false;
        boolean flag1 = pSlot == this.clickedSlot && !this.draggingItem.isEmpty() && !this.isSplittingStack;
        ItemStack itemstack1 = this.menu.getCarried();
        String s = null;

        if (pSlot == this.clickedSlot && !this.draggingItem.isEmpty() && this.isSplittingStack && !itemstack.isEmpty()) {
            itemstack = itemstack.copyWithCount(itemstack.getCount() / 2);
        } else if (this.isQuickCrafting && this.quickCraftSlots.contains(pSlot) && !itemstack1.isEmpty()) {
            if (this.quickCraftSlots.size() == 1) {
                return;
            }

            if (AbstractContainerMenu.canItemQuickReplace(pSlot, itemstack1, true) && this.menu.canDragTo(pSlot)) {
                flag = true;
                int k = Math.min(itemstack1.getMaxStackSize(), pSlot.getMaxStackSize(itemstack1));
                int l = pSlot.getItem().isEmpty() ? 0 : pSlot.getItem().getCount();
                int i1 = AbstractContainerMenu.getQuickCraftPlaceCount(this.quickCraftSlots, this.quickCraftingType, itemstack1) + l;
                if (i1 > k) {
                    i1 = k;
                    s = ChatFormatting.YELLOW + String.valueOf(k);
                }

                itemstack = itemstack1.copyWithCount(i1);
            } else {
                this.quickCraftSlots.remove(pSlot);
                this.recalculateQuickCraftRemaining();
            }
        }

        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().translate(0.0F, 0.0F, 100.0F);

        if (!flag1) {
            ResourceLocation emptySlotTexture = this.getEmptySlotTexture(pSlot, itemstack);
            if (emptySlotTexture != null) {
                pGuiGraphics.blit(emptySlotTexture, x, y, 0, 0, 16, 16, 16, 16);
            }

            if (flag) {
                pGuiGraphics.fill(x, y, x + 16, y + 16, -2130706433);
            }

            pGuiGraphics.renderItem(itemstack, x, y, x + y * this.imageWidth);
            pGuiGraphics.renderItemDecorations(this.font, itemstack, x, y, s);
        }

        pGuiGraphics.pose().popPose();
    }

    public void drawCenteredString(GuiGraphics graphics, Font pFont, Component pText, int pX, int pY, int pColor, boolean dropShadow) {
        FormattedCharSequence formattedcharsequence = pText.getVisualOrderText();
        this.drawString(graphics, pFont, formattedcharsequence, pX - pFont.width(formattedcharsequence) / 2f, pY, pColor, dropShadow);
    }

    public void drawString(GuiGraphics graphics, Font font, FormattedCharSequence charSequence, float textX, float textY, int color, boolean dropShadow) {
        PoseStack poseStack = graphics.pose();
        MultiBufferSource bufferSource = graphics.bufferSource();
        font.drawInBatch(charSequence, textX, textY, color, dropShadow, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
    }

    private ResourceLocation getEmptySlotTexture(Slot pSlot, ItemStack pStack) {
        if (!pStack.isEmpty() || pSlot.container != this.player.getInventory()) {
            return null;
        }

        return switch (pSlot.getSlotIndex()) {
            case 39 -> EMPTY_EQUIPMENT_SLOT_1;
            case 38 -> EMPTY_EQUIPMENT_SLOT_2;
            case 37 -> EMPTY_EQUIPMENT_SLOT_3;
            case 36 -> EMPTY_EQUIPMENT_SLOT_4;
            case 40 -> EMPTY_EQUIPMENT_SLOT_5;
            default -> null;
        };
    }
}