package com.example.orcapods;

import java.util.EnumSet;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractFish;

/**
 * Chases the target, "opens its mouth" (attack swing) for a moment, then bites.
 * Also calls the pod to join a hunt on fish or cachalot whales.
 */
public class OrcaHuntGoal extends Goal {
    private static final int WINDUP_TICKS = 10;   // time between mouth opening and the bite
    private static final int COOLDOWN_TICKS = 30; // time between bites

    private final Mob mob;
    private int cooldown;
    private int repath;
    private int windup;

    public OrcaHuntGoal(Mob mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity t = mob.getTarget();
        return t != null && t.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity t = mob.getTarget();
        return t != null && t.isAlive() && mob.distanceToSqr(t) < 45 * 45;
    }

    @Override
    public void start() {
        windup = 0;
        repath = 0;
        LivingEntity t = mob.getTarget();
        if (t instanceof AbstractFish || OrcaEvents.isCachalot(t)) {
            for (Mob m : PodUtil.getPod(mob)) {
                if (m != mob && m.getTarget() == null) m.setTarget(t);
            }
        }
    }

    @Override
    public void tick() {
        LivingEntity t = mob.getTarget();
        if (t == null) return;
        mob.getLookControl().setLookAt(t, 30f, 30f);
        if (cooldown > 0) cooldown--;

        double reach = mob.getBbWidth() * 1.5 + t.getBbWidth();
        double reachSq = Math.max(6.0, reach * reach);

        if (windup > 0) {
            mob.getNavigation().stop();
            if (--windup == 0) {
                if (mob.distanceToSqr(t) <= reachSq * 2.25) bite(t);
                cooldown = COOLDOWN_TICKS;
            }
            return;
        }

        if (mob.distanceToSqr(t) <= reachSq) {
            if (cooldown <= 0) {
                windup = WINDUP_TICKS;
                mob.getNavigation().stop();
                mob.swing(InteractionHand.MAIN_HAND); // mouth-open / attack animation
            }
        } else if (--repath <= 0) {
            repath = 10;
            mob.getNavigation().moveTo(t, 1.5);
        }
    }

    private void bite(LivingEntity t) {
        if (mob.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE)) {
            mob.doHurtTarget(t);
        } else {
            t.hurt(mob.damageSources().mobAttack(mob), 6f);
        }
    }

    @Override
    public void stop() {
        windup = 0;
        mob.getNavigation().stop();
        LivingEntity t = mob.getTarget();
        if (t == null || !t.isAlive() || mob.distanceToSqr(t) >= 45 * 45) {
            mob.setTarget(null);
        }
    }
}
