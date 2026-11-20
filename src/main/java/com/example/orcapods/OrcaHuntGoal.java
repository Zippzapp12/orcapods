package com.example.orcapods;

import java.util.EnumSet;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractFish;

/** Chases the current target and bites it. Also calls the pod to join a fish hunt. */
public class OrcaHuntGoal extends Goal {
    private final Mob mob;
    private int cooldown;
    private int repath;

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
        LivingEntity t = mob.getTarget();
        repath = 0;
        if (t instanceof AbstractFish) {
            for (Mob m : mob.level().getEntitiesOfClass(Mob.class, mob.getBoundingBox().inflate(32),
                    o -> o != mob && OrcaEvents.isOrca(o) && o.getTarget() == null)) {
                m.setTarget(t);
            }
        }
    }

    @Override
    public void tick() {
        LivingEntity t = mob.getTarget();
        if (t == null) return;
        mob.getLookControl().setLookAt(t, 30f, 30f);
        if (--repath <= 0) {
            repath = 10;
            mob.getNavigation().moveTo(t, 1.5);
        }
        if (cooldown > 0) cooldown--;
        double reach = mob.getBbWidth() * 1.5 + t.getBbWidth();
        if (cooldown <= 0 && mob.distanceToSqr(t) <= Math.max(6.0, reach * reach)) {
            float dmg = mob.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE)
                    ? (float) mob.getAttributeValue(Attributes.ATTACK_DAMAGE) : 6f;
            t.hurt(mob.damageSources().mobAttack(mob), Math.max(dmg, 2f));
            cooldown = 20;
        }
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
        LivingEntity t = mob.getTarget();
        if (t == null || !t.isAlive() || mob.distanceToSqr(t) >= 45 * 45) {
            mob.setTarget(null);
        }
    }
}
