package vectorwing.farmersdelight.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootFunction;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.conditions.ILootCondition;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.blocks.CookingPotTileEntity;

public class CopyMeal extends LootFunction
{
	private CopyMeal(ILootCondition[] conditions)
	{
		super(conditions);
	}

	public static LootFunction.Builder<?> builder()
	{
		return builder(CopyMeal::new);
	}

	@Override
	protected ItemStack doApply(ItemStack stack, LootContext context)
	{
		TileEntity tile = context.get(LootParameters.BLOCK_ENTITY);
		if (tile instanceof CookingPotTileEntity)
		{
			CompoundNBT tag = ((CookingPotTileEntity) tile).writeMealNbt(new CompoundNBT());
			if (!tag.isEmpty())
			{
				stack.setTagInfo("BlockEntityTag", tag);
			}
		}
		return stack;
	}

	public static class Serializer extends LootFunction.Serializer<CopyMeal>
	{
		public Serializer()
		{
			super(new ResourceLocation(FarmersDelight.MODID, "copy_meal"), CopyMeal.class);
		}

		@Override
		public CopyMeal deserialize(JsonObject json, JsonDeserializationContext context, ILootCondition[] conditions)
		{
			return new CopyMeal(conditions);
		}
	}
}