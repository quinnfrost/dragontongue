package com.github.quinnfrost.dragontongue.container;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.item.ItemDragonArmor;
import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.message.MessageSyncCapability;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

public class ContainerDragon extends Container {
    public EntityDragonBase dragon;
    public IInventory dragonInventory;

    public ContainerDragon(int id, PlayerInventory playerInventory) {
        this(id,playerInventory, new Inventory(5),null);
    }
    public ContainerDragon(int id,PlayerInventory playerInventory, IInventory inventory, EntityDragonBase dragon) {
        super(RegistryContainers.CONTAINER_DRAGON.get(), id);
        this.dragonInventory = inventory;
        this.dragon = dragon;
        byte b0 = 3;
        dragonInventory.openInventory(playerInventory.player);
        int i = (b0 - 4) * 18;
        this.addSlot(new Slot(dragonInventory, 0, 8, 54) {
            @Override
            public void onSlotChanged() {
                this.inventory.markDirty();
            }

            @Override
            public boolean isItemValid(ItemStack stack) {
                return super.isItemValid(stack) && stack.getItem() instanceof BannerItem;
            }
        });
        this.addSlot(new Slot(dragonInventory, 1, 8, 18) {
            @Override
            public void onSlotChanged() {
                this.inventory.markDirty();
            }

            @Override
            public boolean isItemValid(ItemStack stack) {
                return super.isItemValid(stack) && !stack.isEmpty() && stack.getItem() != null && stack.getItem() instanceof ItemDragonArmor && ((ItemDragonArmor) stack.getItem()).dragonSlot == 0;
            }
        });
        this.addSlot(new Slot(dragonInventory, 2, 8, 36) {
            @Override
            public void onSlotChanged() {
                this.inventory.markDirty();
            }

            @Override
            public boolean isItemValid(ItemStack stack) {
                return super.isItemValid(stack) && !stack.isEmpty() && stack.getItem() != null && stack.getItem() instanceof ItemDragonArmor && ((ItemDragonArmor) stack.getItem()).dragonSlot == 1;
            }
        });
        this.addSlot(new Slot(dragonInventory, 3, 153, 18) {
            @Override
            public void onSlotChanged() {
                this.inventory.markDirty();
            }

            @Override
            public boolean isItemValid(ItemStack stack) {
                return super.isItemValid(stack) && !stack.isEmpty() && stack.getItem() != null && stack.getItem() instanceof ItemDragonArmor && ((ItemDragonArmor) stack.getItem()).dragonSlot == 2;
            }
        });
        this.addSlot(new Slot(dragonInventory, 4, 153, 36) {
            @Override
            public void onSlotChanged() {
                this.inventory.markDirty();
            }

            @Override
            public boolean isItemValid(ItemStack stack) {
                return super.isItemValid(stack) && !stack.isEmpty() && stack.getItem() != null && stack.getItem() instanceof ItemDragonArmor && ((ItemDragonArmor) stack.getItem()).dragonSlot == 3;
            }
        });
        int j;
        int k;
        for (j = 0; j < 3; ++j) {
            for (k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 8 + k * 18, 150 + j * 18 + i));
            }
        }

        for (j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 208 + i));
        }

    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
//        return this.dragonInventory.isUsableByPlayer(playerIn) && this.dragon.isAlive() && this.dragon.getDistance(playerIn) < 8.0F;
        return this.dragonInventory.isUsableByPlayer(playerIn) && this.dragon.isAlive();
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index < this.dragonInventory.getSizeInventory()) {
                if (!this.mergeItemStack(itemstack1, this.dragonInventory.getSizeInventory(), this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(1).isItemValid(itemstack1) && !this.getSlot(1).getHasStack()) {
                if (!this.mergeItemStack(itemstack1, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }

            } else if (this.getSlot(2).isItemValid(itemstack1) && !this.getSlot(2).getHasStack()) {
                if (!this.mergeItemStack(itemstack1, 2, 3, false)) {
                    return ItemStack.EMPTY;
                }

            } else if (this.getSlot(3).isItemValid(itemstack1) && !this.getSlot(3).getHasStack()) {
                if (!this.mergeItemStack(itemstack1, 3, 4, false)) {
                    return ItemStack.EMPTY;
                }

            } else if (this.getSlot(4).isItemValid(itemstack1) && !this.getSlot(4).getHasStack()) {
                if (!this.mergeItemStack(itemstack1, 4, 5, false)) {
                    return ItemStack.EMPTY;
                }

            } else if (this.getSlot(0).isItemValid(itemstack1)) {
                if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.dragonInventory.getSizeInventory() <= 5 || !this.mergeItemStack(itemstack1, 5, this.dragonInventory.getSizeInventory(), false)) {
                return ItemStack.EMPTY;
            }
            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }
        return itemstack;
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);
        this.dragonInventory.closeInventory(playerIn);
    }

    /**
     * Open the dragon gui
     * This should be called on both server and client side
     *
     * @param player
     * @param referencedDragon
     */
    public static void openGui(LivingEntity player, Entity referencedDragon) {
        if (DragonTongue.isIafPresent && referencedDragon instanceof EntityDragonBase && player instanceof PlayerEntity) {
            EntityDragonBase dragon = (EntityDragonBase) referencedDragon;
            PlayerEntity playerEntity = (PlayerEntity) player;
            if (!referencedDragon.world.isRemote) {
                ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) playerEntity;
                serverPlayerEntity.openContainer(new INamedContainerProvider() {
                    @Override
                    public ITextComponent getDisplayName() {
                        return serverPlayerEntity.getDisplayName();
                    }

                    @Nullable
                    @Override
                    public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {
                        return new ContainerDragon(
                                p_createMenu_1_,
                                p_createMenu_2_,
                                dragon.inventory,
                                dragon
                        );
                    }
                });
                MessageSyncCapability.syncCapabilityToClients(dragon);

            } else {
//                ScreenDragon.referencedDragon = dragon;
//                RegistryMessages.sendToServer(new MessageCommandEntity(
//                        EnumCommandType.GUI,
//                        playerEntity.getUniqueID(),
//                        dragon.getUniqueID()
//                ));
            }
        }
    }

}
