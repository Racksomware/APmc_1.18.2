package gg.archipelago.aprandomizer.common.events;

import gg.archipelago.aprandomizer.APRandomizer;
import gg.archipelago.aprandomizer.managers.itemmanager.ItemManager;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

@Mod.EventBusSubscriber
public class onPlayerInteract {

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    static void onPlayerBlockInteract(PlayerInteractEvent event) {
        //stop all right click interactions if game has not started.
        if(APRandomizer.isJailPlayers())
            event.setCanceled(true);
    }

    @SubscribeEvent
    static void onPlayerBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if(!event.getItemStack().getItem().equals(Items.COMPASS) || !event.getItemStack().hasTag()) {
            return;
        }

        BlockState block = event.getWorld().getBlockState(event.getHitVec().getBlockPos());
        if(event.getItemStack().getTag().get("structure") != null && block.is(Blocks.LODESTONE))
            event.setCanceled(true);

        event.getPlayer().getServer().execute(() -> {
            event.getPlayer().getInventory().setChanged();
            event.getPlayer().inventoryMenu.broadcastChanges();
        });
    }

    @SubscribeEvent
    static void onPlayerInteractEvent(PlayerInteractEvent.RightClickItem event) {
        if(event.getSide().isClient())
            return;
        if(event.getItemStack().getItem().equals(Items.COMPASS) && event.getItemStack().hasTag()) {
            ItemStack compass = event.getItemStack();
            if(!compass.hasTag())
                return;
            CompoundTag nbt = compass.getOrCreateTag();
            if(nbt.get("structure") == null)
                return;

            //fetch our current compass list.
            ArrayList<TagKey<ConfiguredStructureFeature<?,?>>> compasses = APRandomizer.getItemManager().getCompasses();

            TagKey<ConfiguredStructureFeature<?, ?>> tagKey = TagKey.create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, new ResourceLocation(nbt.getString("structure")));
            //get our current structures index in that list, increase it by one, wrapping it to 0 if needed.
            int index = compasses.indexOf(tagKey) + 1;
            if(index >= compasses.size())
                index = 0;

            TagKey<ConfiguredStructureFeature<?,?>> structure = compasses.get(index);

            ItemManager.updateCompassLocation(structure,event.getPlayer(),compass);

        }
    }
}
