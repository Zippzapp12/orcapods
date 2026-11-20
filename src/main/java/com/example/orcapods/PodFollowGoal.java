package com.example.orcapods;

import java.util.Comparator;
import java.util.List;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

/** Orcas swim after the pod leader (the orca with the lowest entity id within 48 blocks). */
public class PodFollowGoal extends Goal {
    private final Mob mob;
    private Mob leader;
    private int repath;

    public PodFollowGoal(Mob mob) {
        this.mob = mob;
    }

    private Mob findLeader() {
        List<Mob> pod = mob.level().getEntitiesOfClass(Mob.class, mob.getBoundingBox().inflate(48),
                o -> OrcaEvents.isOrca(o) && o.isAlive());
        return pod.stream().min(Comparator.comparingInt(Mob::getId)).orElse(null);
    }

    @Override
    public boolean canUse() {
        if (mob.getTarget() != null || !mob.isInWater()) return false;
        if (mob.getRandom().nextInt(10) != 0) return false;
        Mob l = findLeader();
        if (l == null || l == mob || !l.isInWater()) return false;
        if (mob.distanceToSqr(l) < 36) return false; // already within 6 blocks
        leader = l;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return leader != null && leader.isAlive() && mob.getTarget() == null
                && mob.isInWater() && mob.distanceToSqr(leader) > 16;
    }

    @Override
    public void start() {
        repath = 0;
    }

    @Override
    public void tick() {
        if (--repath <= 0) {
            repath = 10;
            mob.getNavigation().moveTo(leader, 1.1);
        }
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
        leader = null;
    }
}
