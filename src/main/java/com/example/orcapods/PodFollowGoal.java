package com.example.orcapods;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

/** Orcas swim after the pod leader but keep a comfortable distance. */
public class PodFollowGoal extends Goal {
    private static final double START_DIST = 14.0; // start following if farther than this
    private static final double STOP_DIST = 8.0;   // stop following when this close

    private final Mob mob;
    private Mob leader;
    private int repath;

    public PodFollowGoal(Mob mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        if (mob.getTarget() != null || !mob.isInWater()) return false;
        if (mob.getRandom().nextInt(10) != 0) return false;
        Mob l = PodUtil.getLeader(mob);
        if (l == mob || !l.isInWater()) return false;
        if (mob.distanceToSqr(l) <= START_DIST * START_DIST) return false;
        leader = l;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return leader != null && leader.isAlive() && mob.getTarget() == null
                && mob.isInWater() && mob.distanceToSqr(leader) > STOP_DIST * STOP_DIST;
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
