package vectorwing.farmersdelight.setup;

import com.google.common.collect.Sets;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.DeferredWorkQueue;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.init.ModBlocks;
import vectorwing.farmersdelight.init.ModItems;
import vectorwing.farmersdelight.loot.functions.CopyMealFunction;
import net.minecraft.block.ComposterBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.TableLootEntry;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vectorwing.farmersdelight.world.CropPatchGeneration;

import java.util.Set;

@Mod.EventBusSubscriber(modid = FarmersDelight.MODID)
public class CommonEventHandler
{
	private static final ResourceLocation SHIPWRECK_SUPPLY_CHEST = LootTables.CHESTS_SHIPWRECK_SUPPLY;
	private static final Set<ResourceLocation> VILLAGE_HOUSE_CHESTS = Sets.newHashSet(
			LootTables.CHESTS_VILLAGE_VILLAGE_PLAINS_HOUSE,
			LootTables.CHESTS_VILLAGE_VILLAGE_SAVANNA_HOUSE,
			LootTables.CHESTS_VILLAGE_VILLAGE_SNOWY_HOUSE,
			LootTables.CHESTS_VILLAGE_VILLAGE_TAIGA_HOUSE,
			LootTables.CHESTS_VILLAGE_VILLAGE_DESERT_HOUSE);
	private static final String[] SCAVENGING_ENTITIES = new String[] { "cow", "chicken", "rabbit", "horse", "donkey", "mule", "llama", "shulker" };

	public static void init(final FMLCommonSetupEvent event)
	{
		ComposterBlock.CHANCES.put(ModItems.CABBAGE_SEEDS.get(), 0.3F);
		ComposterBlock.CHANCES.put(ModItems.TOMATO_SEEDS.get(), 0.3F);
		ComposterBlock.CHANCES.put(ModItems.CABBAGE.get(), 0.65F);
		ComposterBlock.CHANCES.put(ModItems.ONION.get(), 0.65F);
		ComposterBlock.CHANCES.put(ModItems.TOMATO.get(), 0.65F);

		LootFunctionManager.registerFunction(new CopyMealFunction.Serializer());

		DeferredWorkQueue.runLater(CropPatchGeneration::generateCrop);
	}

	@SubscribeEvent
	public static void onHoeUse(UseHoeEvent event) {
		ItemUseContext context = event.getContext();
		BlockPos pos = context.getPos();
		World world = context.getWorld();
		BlockState state = world.getBlockState(pos);

		if (context.getFace() != Direction.DOWN && world.isAirBlock(pos.up()) && state.getBlock() == ModBlocks.MULCH.get()) {
			world.playSound(event.getPlayer(), pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
			world.setBlockState(pos, ModBlocks.MULCH_FARMLAND.get().getDefaultState(), 11);
			event.setResult(Event.Result.ALLOW);
		}
	}

	@SubscribeEvent
	public static void onLootLoad(LootTableLoadEvent event)
	{
		for (String entity : SCAVENGING_ENTITIES) {
			if (event.getName().equals(new ResourceLocation("minecraft", "entities/" + entity))) {
				event.getTable().addPool(LootPool.builder().addEntry(TableLootEntry.builder(new ResourceLocation(FarmersDelight.MODID, "inject/" + entity))).name(entity + "_fd_drops").build());
			}
		}

		if (Configuration.CROPS_ON_SHIPWRECKS.get() && event.getName().equals(SHIPWRECK_SUPPLY_CHEST)) {
			event.getTable().addPool(LootPool.builder().addEntry(TableLootEntry.builder(new ResourceLocation(FarmersDelight.MODID, "inject/shipwreck_supply")).weight(1).quality(0)).name("supply_fd_crops").build());
		}

		if (Configuration.CROPS_ON_VILLAGE_HOUSES.get() && VILLAGE_HOUSE_CHESTS.contains(event.getName())) {
			event.getTable().addPool(LootPool.builder().addEntry(
							TableLootEntry.builder(new ResourceLocation(FarmersDelight.MODID, "inject/crops_villager_houses")).weight(1).quality(0)).name("villager_houses_fd_crops").build());
		}
	}
}
