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
    public static final ResourceLocation CACHALOT_ID = new ResourceLocation("alexsmobs", "cachalot_whale");

    /** Orcas attack a cachalot whale only if their pod has at least this many members. Set to 1 to remove the limit. */
    public static final int MIN_PACK_FOR_WHALE = 2;

    public static boolean isOrca(Entity e) {
        return ORCA_ID.equals(ForgeRegistries.ENTITY_TYPES.getKey(e.getType()));
    }

    public static boolean isCachalot(Entity e) {
        return CACHALOT_ID.equals(ForgeRegistries.ENTITY_TYPES.getKey(e.getType()));
    }

    // Goals are not saved, so we add them every time an orca enters the world.
    @SubscribeEvent
    public void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof Mob mob && isOrca(mob)) {
            mob.goalSelector.addGoal(2, new OrcaHuntGoal(mob));
            mob.goalSelector.addGoal(3, new PodSpacingGoal(mob));
            mob.goalSelector.addGoal(4, new PodFollowGoal(mob));
            mob.targetSelector.addGoal(2,
                    new NearestAttackableTargetGoal<>(mob, AbstractFish.class, 10, true, false, f -> f.isInWater()));
            mob.targetSelector.addGoal(3,
                    new NearestAttackableTargetGoal<>(mob, Mob.class, 10, true, false,
                            e -> isCachalot(e) && PodUtil.getPod(mob).size() >= MIN_PACK_FOR_WHALE));
        }
    }

    // When an orca is hurt, its whole pod (max 5) goes after the attacker.
    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide() || !(victim instanceof Mob victimMob) || !isOrca(victim)) return;
        Entity src = event.getSource().getEntity();
        if (src instanceof LivingEntity attacker && attacker.isAlive() && !isOrca(attacker)) {
            for (Mob m : PodUtil.getPod(victimMob)) {
                if (m != victim) m.setTarget(attacker);
            }
        }
    }
}
