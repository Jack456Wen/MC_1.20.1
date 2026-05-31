package com.net.wenwen.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class InventorySorter {

    public enum SortType {
        NAME, CATEGORY, MOD, ID
    }

    public static void sortInventory(Player player, SortType sortType) {
        NonNullList<ItemStack> mainInventory = player.getInventory().items;
        sortItems(mainInventory, 9, 27, sortType);
        player.getInventory().setChanged();
    }

    public static void sortContainer(Player player, SortType sortType) {
        AbstractContainerMenu containerMenu = player.containerMenu;
        if (containerMenu != null) {
            // 只对箱子类型的容器进行整理
            String containerClassName = containerMenu.getClass().getName();
            if (containerClassName.contains("Chest") || containerClassName.contains("Shulker") || containerClassName.contains("Barrel") || containerClassName.contains("AbstractHandler")) {
                // 整理容器的物品，跳过玩家的物品栏部分
                int containerSlots = containerMenu.slots.size() - 36; // 36 是玩家的物品栏大小
                if (containerSlots > 0) {
                    NonNullList<ItemStack> items = NonNullList.withSize(containerSlots, ItemStack.EMPTY);
                    for (int i = 0; i < containerSlots; i++) {
                        items.set(i, containerMenu.slots.get(i).getItem());
                    }
                    sortItems(items, 0, containerSlots, sortType);
                    for (int i = 0; i < containerSlots; i++) {
                        containerMenu.slots.get(i).set(items.get(i));
                    }
                    containerMenu.broadcastChanges();
                }
            }
        }
    }

    private static void sortItems(NonNullList<ItemStack> items, int startSlot, int slotCount, SortType sortType) {
        List<ItemStack> stacks = new ArrayList<>();
        
        for (int i = 0; i < slotCount; i++) {
            addStackWithMerge(stacks, items.get(startSlot + i));
        }

        stacks.sort(Comparator.comparing(stack -> getSortString(stack, sortType)));
        
        if (stacks.size() == 0) return;
        
        for (int i = 0; i < slotCount; i++) {
            items.set(startSlot + i, i < stacks.size() ? stacks.get(i) : ItemStack.EMPTY);
        }
    }

    private static void addStackWithMerge(List<ItemStack> stacks, ItemStack newStack) {
        if (newStack.getItem() == Items.AIR) return;
        
        ItemStack stackToAdd = newStack.copy();
        
        if (stackToAdd.isStackable() && stackToAdd.getCount() != stackToAdd.getMaxStackSize()) {
            for (int j = stacks.size() - 1; j >= 0; j--) {
                ItemStack oldStack = stacks.get(j);
                if (canMergeItems(stackToAdd, oldStack)) {
                    combineStacks(stackToAdd, oldStack);
                    if (oldStack.getItem() == Items.AIR || oldStack.getCount() == 0) {
                        stacks.remove(j);
                    }
                }
            }
        }
        
        if (stackToAdd.getItem() != Items.AIR && stackToAdd.getCount() > 0) {
            stacks.add(stackToAdd);
        }
    }

    private static void combineStacks(ItemStack stack, ItemStack stack2) {
        if (stack.getMaxStackSize() >= stack.getCount() + stack2.getCount()) {
            stack.grow(stack2.getCount());
            stack2.setCount(0);
        } else {
            int maxInsertAmount = Math.min(stack.getMaxStackSize() - stack.getCount(), stack2.getCount());
            stack.grow(maxInsertAmount);
            stack2.shrink(maxInsertAmount);
        }
    }

    private static boolean canMergeItems(ItemStack itemStack1, ItemStack itemStack2) {
        if (!itemStack1.isStackable() || !itemStack2.isStackable())
            return false;
        if (itemStack1.getCount() == itemStack1.getMaxStackSize() || itemStack2.getCount() == itemStack2.getMaxStackSize())
            return false;
        if (itemStack1.getItem() != itemStack2.getItem())
            return false;
        if (itemStack1.getDamageValue() != itemStack2.getDamageValue())
            return false;
        return ItemStack.isSameItemSameTags(itemStack1, itemStack2);
    }

    private static String getSortString(ItemStack stack, SortType sortType) {
        if (stack.isEmpty()) {
            return "";
        }

        String itemName = getSpecialCaseString(stack);

        switch (sortType) {
            case CATEGORY:
                CreativeModeTab group = getItemGroup(stack);
                return (group != null ? group.getDisplayName().getString() : "zzz") + itemName;
            case MOD:
                ResourceLocation itemLocation = ForgeRegistries.ITEMS.getKey(stack.getItem());
                return (itemLocation != null ? itemLocation.getNamespace() : "unknown") + itemName;
            case NAME:
                if (stack.hasCustomHoverName()) {
                    return stack.getHoverName().getString() + itemName;
                }
            case ID:
            default:
                return itemName;
        }
    }

    private static CreativeModeTab getItemGroup(ItemStack stack) {
        for (CreativeModeTab tab : CreativeModeTabs.allTabs()) {
            if (tab.contains(stack)) {
                return tab;
            }
        }
        return null;
    }

    private static String getSpecialCaseString(ItemStack stack) {
        CompoundTag tag = stack.getTag();

        if (tag != null && tag.contains("SkullOwner")) {
            return getPlayerHeadString(stack);
        }
        if (stack.getCount() != stack.getMaxStackSize()) {
            return getStackSizeString(stack);
        }
        if (stack.getItem() instanceof EnchantedBookItem) {
            return getEnchantedBookString(stack);
        }
        if (stack.getMaxDamage() > 0) {
            return getToolDurabilityString(stack);
        }

        return stack.getItem().toString();
    }

    private static String getPlayerHeadString(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        CompoundTag skullOwner = tag.getCompound("SkullOwner");
        String ownerName = skullOwner.getString("Name");

        String count = "";
        if (stack.getCount() != stack.getMaxStackSize()) {
            count = Integer.toString(stack.getCount());
        }

        return stack.getItem().toString() + " " + ownerName + count;
    }

    private static String getStackSizeString(ItemStack stack) {
        return stack.getItem().toString() + stack.getCount();
    }

    private static String getEnchantedBookString(ItemStack stack) {
        List<Enchantment> enchantments = new ArrayList<>(EnchantmentHelper.getEnchantments(stack).keySet());
        List<String> names = new ArrayList<>();
        StringBuilder enchantNames = new StringBuilder();

        for (Enchantment enchant : enchantments) {
            ResourceLocation enchantId = ForgeRegistries.ENCHANTMENTS.getKey(enchant);
            if (enchantId != null) {
                int level = EnchantmentHelper.getTagEnchantmentLevel(enchant, stack);
                names.add(enchant.getFullname(level).getString());
            }
        }

        Collections.sort(names);
        for (String enchant : names) {
            enchantNames.append(enchant).append(" ");
        }

        return stack.getItem().toString() + " " + enchantments.size() + " " + enchantNames;
    }

    private static String getToolDurabilityString(ItemStack stack) {
        return stack.getItem().toString() + stack.getDamageValue();
    }
}