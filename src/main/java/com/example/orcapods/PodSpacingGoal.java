package com.example.orcapods;

import java.util.Comparator;
import java.util.EnumSet;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/** If another orca is too close, swim a few blocks away so pod members don't crowd each other. */
public class PodSpacingGoal extends Goal {
    private static final double MIN_DIST = 4.0;

    private final Mob mob;
    private Mob neighbor;
    private int ticks;

    public PodSpacingGoal(Mob mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (mob.getTarget() != null || !mob.isInWater()) return false;
        if (mob.getRandom().nextInt(8) != 0) return false;
        neighbor = mob.level().getEntitiesOfClass(Mob.class, mob.getBoundingBox().inflate(MIN_DIST),
                        o -> o != mob && OrcaEvents.isOrca(o) && o.isAlive())
                .stream().min(Comparator.comparingDouble(mob::distanceToSqr)).orElse(null);
        return neighbor != null && mob.distanceToSqr(neighbor) < MIN_DIST * MIN_DIST;
    }

    @Override
    public boolean canContinueToUse() {
        return ticks > 0 && mob.getTarget() == null && neighbor != null && neighbor.isAlive();
    }

    @Override
    public void start() {
        ticks = 20;
        push();
    }

    @Override
    public void tick() {
        ticks--;
        if (ticks == 10) push();
    }

    private void push() {
        double dx = mob.getX() - neighbor.getX();
        double dz = mob.getZ() - neighbor.getZ();
        if (dx * dx + dz * dz < 0.01) {
            dx = mob.getRandom().nextDouble() - 0.5;
            dz = mob.getRandom().nextDouble() - 0.5;
        }
        Vec3 away = new Vec3(dx, 0, dz).normalize().scale(6);
        mob.getNavigation().moveTo(mob.getX() + away.x, mob.getY(), mob.getZ() + away.z, 1.0);
    }

    @Override
    public void stop() {
        neighbor = null;
        ticks = 0;
    }
}
