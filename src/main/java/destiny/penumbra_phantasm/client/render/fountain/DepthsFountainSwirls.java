package destiny.penumbra_phantasm.client.render.fountain;

import destiny.penumbra_phantasm.server.capability.DarkFountainCapability;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static destiny.penumbra_phantasm.client.render.fountain.FountainRenderUtil.*;
import static destiny.penumbra_phantasm.server.fountain.DarkFountain.OPENING_FINISH;

public class DepthsFountainSwirls {
	private static final Map<Long, List<Swirl>> BY_FOUNTAIN = new HashMap<>();
	private static final int MAX_PER_FOUNTAIN = 40;
	private static final float SPAWN_CHANCE = 0.22f;

	public static class Swirl {
		public final boolean small;
		public final float lifetime;
		public final float spinSpeedDeg;
		public final float startYOffset;
		public float age;
		public float yaw;

		private Swirl(boolean small, float lifetime, float spinSpeedDeg, float startYOffset, float yaw) {
			this.small = small;
			this.lifetime = lifetime;
			this.spinSpeedDeg = spinSpeedDeg;
			this.startYOffset = startYOffset;
			this.yaw = yaw;
		}

		public float progress() {
			return Mth.clamp(age / lifetime, 0f, 1f);
		}

		public float alpha() {
			float t = progress();
			float fadeIn = Mth.clamp(t / 0.15f, 0f, 1f);
			float fadeOut = Mth.clamp((1f - t) / 0.3f, 0f, 1f);

			return fadeIn * fadeOut;
		}
	}

	public static void tick(Level level, DarkFountainCapability cap) {
		RandomSource random = level.random;
		HashMap<Long, Boolean> live = new HashMap<>();

		for (DarkFountain fountain : cap.darkFountains.values()) {
			long key = fountain.getFountainPos().asLong();
			live.put(key, true);
			List<Swirl> swirls = BY_FOUNTAIN.computeIfAbsent(key, k -> new ArrayList<>());
			Iterator<Swirl> iterator = swirls.iterator();

			while (iterator.hasNext()) {
				Swirl swirl = iterator.next();

				int openingTick = fountain.openingTick;
				int sealingTick = fountain.sealingTick;

				if (sealingTick < 0) {
					swirl.age = swirl.age + 1f;
				}

				if (openingTick >= OPENING_SHADOW_DURATION_FULL && openingTick < OPENING_FINISH) {
					float openDelta = (openingTick - OPENING_SHADOW_DURATION_FULL) / (OPENING_FINISH - OPENING_SHADOW_DURATION_FULL);

					swirl.yaw = swirl.yaw + swirl.spinSpeedDeg * openDelta;
				} else if (sealingTick >= 0) {
					float sealDelta = 1f - (float) sealingTick / DEPTHS_FADE_OUT_DURATION;

					swirl.yaw = swirl.yaw + swirl.spinSpeedDeg * sealDelta;
				} else {
					swirl.yaw = swirl.yaw + swirl.spinSpeedDeg;
				}

				if (swirl.age >= swirl.lifetime) {
					iterator.remove();
				}
			}

			if (swirls.size() >= MAX_PER_FOUNTAIN) {
				continue;
			}

			if (random.nextFloat() < SPAWN_CHANCE) {
				swirls.add(createSwirl(random, random.nextBoolean()));
			}
		}

		BY_FOUNTAIN.keySet().removeIf(key -> !live.containsKey(key));
	}

	public static List<Swirl> swirlsAt(BlockPos pos) {
		List<Swirl> swirls = BY_FOUNTAIN.get(pos.asLong());

		if (swirls == null) {
			return List.of();
		}

		return swirls;
	}

	private static Swirl createSwirl(RandomSource random, boolean small) {
		float lifetime = (3f + random.nextFloat() * 2f) * 20f;
		float periodSec = 2f + random.nextFloat();
		float spinSpeedDeg = 360f / (periodSec * 20f);
		float startYOffset = 8f + random.nextFloat() * 4f;
		float yaw = random.nextFloat() * 360f;

		return new Swirl(small, lifetime, spinSpeedDeg, startYOffset, yaw);
	}
}
