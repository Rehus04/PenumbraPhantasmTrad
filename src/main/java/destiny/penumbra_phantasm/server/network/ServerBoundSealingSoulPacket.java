package destiny.penumbra_phantasm.server.network;

import destiny.penumbra_phantasm.server.advancement.TriggerCriterions;
import destiny.penumbra_phantasm.server.capability.SoulCapability;
import destiny.penumbra_phantasm.server.entity.SealingSoulEntity;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.EntityRegistry;
import destiny.penumbra_phantasm.server.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerBoundSealingSoulPacket {
    public ResourceKey<Level> spawnLevel;
    public int soulType;
    public double x;
    public double y;
    public double z;

    public ServerBoundSealingSoulPacket(ResourceKey<Level> spawnLevel, int soulType, double x, double y, double z) {
        this.spawnLevel = spawnLevel;
        this.soulType = soulType;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceKey(spawnLevel);
        buffer.writeInt(soulType);
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
    }

    public static ServerBoundSealingSoulPacket decode(FriendlyByteBuf buffer) {
        ResourceKey<Level> spawnLevel = buffer.readResourceKey(Registries.DIMENSION);
        int soulType = buffer.readInt();
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();

        return new ServerBoundSealingSoulPacket(spawnLevel, soulType, x, y, z);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();

            if (player == null) return;

            ServerLevel originLevel = player.getServer().getLevel(spawnLevel);
            BlockPos spawnPos = new BlockPos((int) x, (int) y, (int) z);

            if (originLevel != null && originLevel.isLoaded(spawnPos)) {
                SealingSoulEntity soulEntity = new SealingSoulEntity(EntityRegistry.SEALING_SOUL.get(), originLevel);

                soulEntity.setSoulType(soulType);
                soulEntity.setPos(x, y, z);

                originLevel.addFreshEntity(soulEntity);

                TriggerCriterions.DARK_FOUNTAIN_SEAL.trigger(player);

                player.getCooldowns().addCooldown(ItemRegistry.SOUL_HEARTH.get(), 10 * 20);

                if (!player.isCreative()) {
                    SoulCapability soulCapability = null;
                    LazyOptional<SoulCapability> soulCapabilityLazy = player.getCapability(CapabilityRegistry.SOUL);
                    if(soulCapabilityLazy.isPresent() && soulCapabilityLazy.resolve().isPresent())
                        soulCapability = soulCapabilityLazy.resolve().get();

                    if (soulCapability == null) {
                        return;
                    }

                    soulCapability.determination = 0;
                }
            }
        });

        return true;
    }
}