package destiny.penumbra_phantasm.server.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class TensionCapability implements INBTSerializable<CompoundTag> {
    public static final String SEEN_INTRO = "seenIntro";

    @Override
    public CompoundTag serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {

    }
}
