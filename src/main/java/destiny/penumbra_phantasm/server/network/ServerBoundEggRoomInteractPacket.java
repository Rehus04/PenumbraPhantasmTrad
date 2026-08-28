package destiny.penumbra_phantasm.server.network;

import destiny.penumbra_phantasm.server.egg_room.CardKingdomEggRoomManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerBoundEggRoomInteractPacket {
    public ServerBoundEggRoomInteractPacket() {}

    public void encode(FriendlyByteBuf buffer) {
    }

    public static ServerBoundEggRoomInteractPacket decode(FriendlyByteBuf buffer) {
        return new ServerBoundEggRoomInteractPacket();
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                CardKingdomEggRoomManager.tryInteract(player);
            }
        });
        return true;
    }
}