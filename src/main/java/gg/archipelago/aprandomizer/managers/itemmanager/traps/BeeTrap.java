package gg.archipelago.aprandomizer.managers.itemmanager.traps;

import gg.archipelago.aprandomizer.APRandomizer;
import gg.archipelago.aprandomizer.common.Utils.Utils;
import gg.archipelago.aprandomizer.managers.itemmanager.Trap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.phys.Vec3;

public class BeeTrap implements Trap {

    final private int numberOfBees;

    public BeeTrap(int numberOfBees) {
        this.numberOfBees = numberOfBees;
    }
    @Override
    public void trigger(ServerPlayer player) {
        APRandomizer.getServer().execute(() -> {
            ServerLevel world = player.getLevel();
            Vec3 pos = player.position();
            for (int i = 0; i < numberOfBees; i++) {
                Bee bee = EntityType.BEE.create(world);
                Vec3 offset = Utils.getRandomPosition(pos, 5);
                bee.moveTo(offset);
                bee.setPersistentAngerTarget(player.getUUID());
                bee.setRemainingPersistentAngerTime(1200);
                world.addFreshEntity(bee);
            }

        });
    }
}
