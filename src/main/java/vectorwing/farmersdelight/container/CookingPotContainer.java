package vectorwing.farmersdelight.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntArray;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vectorwing.farmersdelight.blocks.CookingPotTileEntity;
import vectorwing.farmersdelight.init.ModBlocks;
import vectorwing.farmersdelight.init.ModContainerTypes;

import java.util.Objects;

public class CookingPotContainer extends Container
{
	public final CookingPotTileEntity tileEntity;
	private final IIntArray cookingPotData;
	private final IWorldPosCallable canInteractWithCallable;

	public CookingPotContainer(final int windowId, final PlayerInventory playerInventory, final CookingPotTileEntity tileEntity, IIntArray cookingPotDataIn)
	{
		super(ModContainerTypes.COOKING_POT.get(), windowId);
		this.tileEntity = tileEntity;
		this.cookingPotData = cookingPotDataIn;
		this.canInteractWithCallable = IWorldPosCallable.of(tileEntity.getWorld(), tileEntity.getPos());

		// Ingredient Slots - 2 Rows x 3 Columns
		int startX = 8;
		int startY = 18;
		int inputStartX = 30;
		int inputStartY = 17;
		int borderSlotSize = 18;
		for (int row = 0; row < 2; ++row) {
			for (int column = 0; column < 3; ++column) {
				this.addSlot(new Slot(tileEntity, (row * 3) + column,
						inputStartX + (column * borderSlotSize),
						inputStartY + (row * borderSlotSize)));
			}
		}

		// Meal Display
		this.addSlot(new CookingPotMealSlot(tileEntity, 6, 124, 26));

		// Bowl Input
		this.addSlot(new Slot(tileEntity, 7, 92, 55));

		// Bowl Output
		this.addSlot(new CookingPotResultSlot(tileEntity, 8, 124, 55));

		// Main Player Inventory
		int startPlayerInvY = startY * 4 + 12;
		for (int row = 0; row < 3; ++row) {
			for (int column = 0; column < 9; ++column) {
				this.addSlot(new Slot(playerInventory, 9 + (row * 9) + column, startX + (column * borderSlotSize),
						startPlayerInvY + (row * borderSlotSize)));
			}
		}

		// Hotbar
		for (int column = 0; column < 9; ++column) {
			this.addSlot(new Slot(playerInventory, column, startX + (column * borderSlotSize), 142));
		}

		this.trackIntArray(cookingPotDataIn);
	}

	private static CookingPotTileEntity getTileEntity(final PlayerInventory playerInventory, final PacketBuffer data) {
		Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
		Objects.requireNonNull(data, "data cannot be null");
		final TileEntity tileAtPos = playerInventory.player.world.getTileEntity(data.readBlockPos());
		if (tileAtPos instanceof CookingPotTileEntity) {
			return (CookingPotTileEntity) tileAtPos;
		}
		throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
	}

	public CookingPotContainer(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
		this(windowId, playerInventory, getTileEntity(playerInventory, data), new IntArray(4));
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return isWithinUsableDistance(canInteractWithCallable, playerIn, ModBlocks.COOKING_POT.get());
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		int indexContainerInput = 7;
		int indexOutput = 8;
		int startPlayerInv = indexOutput + 1;
		int endPlayerInv = startPlayerInv + 36;
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (index == indexOutput) {
				if (!this.mergeItemStack(itemstack1, startPlayerInv, endPlayerInv, true)) {
					return ItemStack.EMPTY;
				}
			} else if (index > indexOutput) {
				if (itemstack1.getItem() == Items.BOWL && !this.mergeItemStack(itemstack1, indexContainerInput, indexContainerInput+1, false)) {
					return ItemStack.EMPTY;
				} else if (!this.mergeItemStack(itemstack1, 0, indexOutput, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack1, startPlayerInv, endPlayerInv, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, itemstack1);
		}
		return itemstack;
	}

	@OnlyIn(Dist.CLIENT)
	public int getCookProgressionScaled() {
		int i = this.cookingPotData.get(0);
		int j = this.cookingPotData.get(1);
		return j != 0 && i != 0 ? i * 24 / j : 0;
	}

	@OnlyIn(Dist.CLIENT)
	public boolean isHeated() {
		return this.tileEntity.isAboveLitHeatSource();
	}
}
