package com.example.orcapods;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class OrcaEvents {
    public static final ResourceLocation ORCA_ID = new ResourceLocation("alexsmobs", "orca");

    public static boolean isOrca(Entity e) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(e.getType());
        return ORCA_ID.equals(key);
    }

    // Goals are not saved, so we add them every time an orca enters the world.
    @SubscribeEvent
    public void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof Mob mob && isOrca(mob)) {
            mob.goalSelector.addGoal(2, new OrcaHuntGoal(mob));
            mob.goalSelector.addGoal(3, new PodFollowGoal(mob));
            mob.targetSelector.addGoal(2,
                    new NearestAttackableTargetGoal<>(mob, AbstractFish.class, 10, true, false, f -> f.isInWater()));
        }
    }

    // When an orca is hurt, the whole pod goes after the attacker.
    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide() || !isOrca(victim)) return;
        Entity src = event.getSource().getEntity();
        if (src instanceof LivingEntity attacker && attacker.isAlive() && !isOrca(attacker)) {
            for (Mob m : victim.level().getEntitiesOfClass(Mob.class,
                    victim.getBoundingBox().inflate(32), o -> o != victim && isOrca(o))) {
                m.setTarget(attacker);
            }
        }
    }
}
