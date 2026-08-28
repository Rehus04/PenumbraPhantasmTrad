package destiny.penumbra_phantasm.server.network;

import destiny.penumbra_phantasm.client.network.ClientBoundTextBoxPacket;
import destiny.penumbra_phantasm.server.capability.SoulCapability;
import destiny.penumbra_phantasm.server.egg_room.CardKingdomEggRoomManager;
import destiny.penumbra_phantasm.server.egg_room.CardKingdomEggRoomUtil;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.ItemRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class ServerBoundTextBoxChoicePacket {
	public final String scriptId;
	public final boolean choice;

	public ServerBoundTextBoxChoicePacket(String scriptId, boolean choice) {
		this.scriptId = scriptId;
		this.choice = choice;
	}

	public ServerBoundTextBoxChoicePacket(FriendlyByteBuf buf) {
		this.scriptId = buf.readUtf();
		this.choice = buf.readBoolean();
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeUtf(scriptId);
		buf.writeBoolean(choice);
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			if (player != null) {
				handleEggChoice(player, scriptId, choice);
				handleFountainSealChoice(player, scriptId, choice);
			}
		});
		return true;
	}

	public static void handleEggChoice(ServerPlayer player, String scriptId, boolean yes) {
		if (!ClientBoundTextBoxPacket.CARD_KINGDOM_EGG_ROOM_TREE_BEHIND.equals(scriptId)) {
			return;
		}

		SoulCapability cap = player.getCapability(CapabilityRegistry.SOUL).orElse(null);
		if (cap.hasEggRoomManGone(CardKingdomEggRoomUtil.CARD_KINGDOM_BIT)) {
			return;
		}

		cap.setEggRoomManGone(CardKingdomEggRoomUtil.CARD_KINGDOM_BIT);

		if (yes) {
			if (!cap.hasEggObtained(CardKingdomEggRoomUtil.CARD_KINGDOM_BIT)) {
				cap.setEggObtained(CardKingdomEggRoomUtil.CARD_KINGDOM_BIT);

				if (!player.addItem(new ItemStack(ItemRegistry.EGG.get()))) {
					player.drop(new ItemStack(ItemRegistry.EGG.get()), false);
				}
			}

			PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
					new ClientBoundTextBoxPacket(ClientBoundTextBoxPacket.EGG_ROOM_RECEIVED_EGG));
		} else {
			PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
					new ClientBoundTextBoxPacket(ClientBoundTextBoxPacket.CARD_KINGDOM_EGG_ROOM_CHOICE_DENY));
		}
	}

	private static void handleFountainSealChoice(ServerPlayer player, String scriptId, boolean choice) {
		if (!ClientBoundTextBoxPacket.SOUL_HEARTH_SEALING_FOUNTAIN_CHOICE.equals(scriptId)) {
			return;
		}

		if (choice) {
			PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
					new ClientBoundTextBoxPacket(ClientBoundTextBoxPacket.SOUL_HEARTH_SEALING_FOUNTAIN_CHOICE_CONFIRM));
		} else {
			PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
					new ClientBoundTextBoxPacket(ClientBoundTextBoxPacket.SOUL_HEARTH_SEALING_FOUNTAIN_CHOICE_DENY));
		}
	}
}
