package gg.archipelago.aprandomizer.common.events;

import gg.archipelago.APClient.network.BouncePacket;
import gg.archipelago.aprandomizer.APRandomizer;
import gg.archipelago.aprandomizer.APStorage.APMCData;
import gg.archipelago.aprandomizer.common.DeathLinkDamage;
import gg.archipelago.aprandomizer.managers.advancementmanager.AdvancementManager;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;

@Mod.EventBusSubscriber
public class onDeath {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    static void onDeathEvent(LivingDeathEvent event) {
        if(!APRandomizer.isConnected())
            return;
        //only trigger on player death
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        if(!APRandomizer.getAP().getSlotData().deathlink)
            return;

        //dont send deathlink if the cause of this death was a deathlink
        if(event.getSource() instanceof DeathLinkDamage)
            return;

        double deathTime = (double)System.currentTimeMillis() / 1000D;
        BouncePacket deathLinkPacket = new BouncePacket();
        deathLinkPacket.tags = new String[]{"DeathLink"};
        deathLinkPacket.setData(new HashMap<>(){{
            put("cause",event.getSource().getLocalizedDeathMessage(player).getString());
            put("time", deathTime);
            put("source",player.getDisplayName().getString());
        }});
        MinecraftServer server = APRandomizer.getServer();
        GameRules.BooleanValue deathMessages = server.getGameRules().getRule(GameRules.RULE_SHOWDEATHMESSAGES);
        boolean death = deathMessages.get();
        deathMessages.set(false, server);
        for (ServerPlayer serverPlayer : APRandomizer.getServer().getPlayerList().getPlayers()) {
            if (serverPlayer != player) {
                serverPlayer.hurt(DeathLinkDamage.DEATH_LINK,Float.MAX_VALUE);
            }
        }
        deathMessages.set(death, server);
        APRandomizer.setLastDeathTimestamp(deathTime);
        APRandomizer.getAP().sendBounce(deathLinkPacket);
    }
}
