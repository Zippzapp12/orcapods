package com.example.orcapods;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.world.entity.Mob;

/** Splits nearby orcas into pods of at most MAX_POD_SIZE. */
public class PodUtil {
    public static final int MAX_POD_SIZE = 5;

    /** The pod this orca belongs to (includes the orca itself), max 5 members. */
    public static List<Mob> getPod(Mob mob) {
        List<Mob> all = new ArrayList<>(mob.level().getEntitiesOfClass(Mob.class,
                mob.getBoundingBox().inflate(48), o -> OrcaEvents.isOrca(o) && o.isAlive()));
        all.sort(Comparator.comparingInt(Mob::getId));
        int idx = all.indexOf(mob);
        if (idx < 0) {
            List<Mob> solo = new ArrayList<>();
            solo.add(mob);
            return solo;
        }
        int start = idx / MAX_POD_SIZE * MAX_POD_SIZE;
        return new ArrayList<>(all.subList(start, Math.min(start + MAX_POD_SIZE, all.size())));
    }

    /** Leader = first member of the pod (lowest entity id). */
    public static Mob getLeader(Mob mob) {
        return getPod(mob).get(0);
    }
}
