package com.github.quinnfrost.dragontongue.iceandfire.container;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.item.ItemDragonArmor;
import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.message.MessageSyncCapability;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class ContainerDragon extends AbstractContainerMenu {
    public EntityDragonBase dragon;
    public Container dragonInventory;

    public ContainerDragon(int id, Inventory playerInventory) {
        this(id,playerInventory, new SimpleContainer(5),null);
    }
    public ContainerDragon(int id,Inventory playerInventory, Container inventory, EntityDragonBase dragon) {
        super(RegistryContainers.CONTAINER_DRAGON.get(), id);
        this.dragonInventory = inventory;
        this.dragon = dragon;
        byte b0 = 3;
        dragonInventory.startOpen(playerInventory.player);
        int i = (b0 - 4) * 18;
        this.addSlot(new Slot(dragonInventory, 0, 8, 54) {
            @Override
            public void setChanged() {
                this.container.setChanged();
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return super.mayPlace(stack) && stack.getItem() instanceof BannerItem;
            }
        });
        this.addSlot(new Slot(dragonInventory, 1, 8, 18) {
            @Override
            public void setChanged() {
                this.container.setChanged();
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return super.mayPlace(stack) && !stack.isEmpty() && stack.getItem() != null && stack.getItem() instanceof ItemDragonArmor && ((ItemDragonArmor) stack.getItem()).dragonSlot == 0;
            }
        });
        this.addSlot(new Slot(dragonInventory, 2, 8, 36) {
            @Override
            public void setChanged() {
                this.container.setChanged();
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return super.mayPlace(stack) && !stack.isEmpty() && stack.getItem() != null && stack.getItem() instanceof ItemDragonArmor && ((ItemDragonArmor) stack.getItem()).dragonSlot == 1;
            }
        });
        this.addSlot(new Slot(dragonInventory, 3, 153, 18) {
            @Override
            public void setChanged() {
                this.container.setChanged();
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return super.mayPlace(stack) && !stack.isEmpty() && stack.getItem() != null && stack.getItem() instanceof ItemDragonArmor && ((ItemDragonArmor) stack.getItem()).dragonSlot == 2;
            }
        });
        this.addSlot(new Slot(dragonInventory, 4, 153, 36) {
            @Override
            public void setChanged() {
                this.container.setChanged();
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return super.mayPlace(stack) && !stack.isEmpty() && stack.getItem() != null && stack.getItem() instanceof ItemDragonArmor && ((ItemDragonArmor) stack.getItem()).dragonSlot == 3;
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
    public boolean stillValid(Player playerIn) {
//        return this.dragonInventory.isUsableByPlayer(playerIn) && this.dragon.isAlive() && this.dragon.getDistance(playerIn) < 8.0F;
        return this.dragonInventory.stillValid(playerIn) && this.dragon.isAlive();
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < this.dragonInventory.getContainerSize()) {
                if (!this.moveItemStackTo(itemstack1, this.dragonInventory.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(1).mayPlace(itemstack1) && !this.getSlot(1).hasItem()) {
                if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }

            } else if (this.getSlot(2).mayPlace(itemstack1) && !this.getSlot(2).hasItem()) {
                if (!this.moveItemStackTo(itemstack1, 2, 3, false)) {
                    return ItemStack.EMPTY;
                }

            } else if (this.getSlot(3).mayPlace(itemstack1) && !this.getSlot(3).hasItem()) {
                if (!this.moveItemStackTo(itemstack1, 3, 4, false)) {
                    return ItemStack.EMPTY;
                }

            } else if (this.getSlot(4).mayPlace(itemstack1) && !this.getSlot(4).hasItem()) {
                if (!this.moveItemStackTo(itemstack1, 4, 5, false)) {
                    return ItemStack.EMPTY;
                }

            } else if (this.getSlot(0).mayPlace(itemstack1)) {
                if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.dragonInventory.getContainerSize() <= 5 || !this.moveItemStackTo(itemstack1, 5, this.dragonInventory.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }
            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        this.dragonInventory.stopOpen(playerIn);
    }

    /**
     * Open the dragon gui
     * This should be called on both server and client side
     *
     * @param player
     * @param referencedDragon
     */
    public static void openGui(LivingEntity player, Entity referencedDragon) {
        if (DragonTongue.isIafPresent && referencedDragon instanceof EntityDragonBase && player instanceof Player) {
            EntityDragonBase dragon = (EntityDragonBase) referencedDragon;
            Player playerEntity = (Player) player;
            if (!referencedDragon.level.isClientSide) {
                ServerPlayer serverPlayerEntity = (ServerPlayer) playerEntity;
                serverPlayerEntity.openMenu(new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return serverPlayerEntity.getDisplayName();
                    }

                    @Nullable
                    @Override
                    public AbstractContainerMenu createMenu(int p_createMenu_1_, Inventory p_createMenu_2_, Player p_createMenu_3_) {
                        return new ContainerDragon(
                                p_createMenu_1_,
                                p_createMenu_2_,
                                dragon.dragonInventory,
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
