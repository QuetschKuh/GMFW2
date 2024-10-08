package de.lolsu.gmfw.api;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A utility class used to create ItemStacks with basic ItemMeta such as display name and lore in a single line instead of multiple
 * */
public class ItemStacks {

    public static ItemStack create(Material material, String displayName, String... lore) {
        return create(material, (short) 1, (short) 0, displayName, Arrays.asList(lore));
    }

    public static ItemStack create(Material material, String displayName, List<String> lore) {
        return create(material, (short) 1, (short) 0, displayName, lore);
    }

    public static ItemStack create(Material material, int amount, short damage, String displayName, String... lore) {
        return create(material, amount, damage, displayName, Arrays.asList(lore));
    }

    public static ItemStack create(Material material, int amount, short damage, String displayName, List<String> lore) {
        ItemStack is = new ItemStack(material, amount, damage);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(displayName);
        List<String> il = im.hasLore() ? im.getLore() : new ArrayList<>();
        lore.replaceAll(s -> ChatColor.RESET + s);
        il.addAll(lore);
        im.setLore(il);
        is.setItemMeta(im);
        return is;
    }

    public static ItemStack createPotion(PotionType type, int duration, int level, boolean throwable) {
        PotionEffect effect = new PotionEffect(type.getEffectType(), duration, level, true, true);
        ItemStack i = new Potion(type, 1, throwable).toItemStack(1);
        PotionMeta im = (PotionMeta) i.getItemMeta();
        im.addCustomEffect(effect, true);
        i.setItemMeta(im);
        return i;
    }

    public static ItemStack addGlint(ItemStack itemStack) {
        ItemMeta im = itemStack.getItemMeta();
        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(im);
        itemStack.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
        return itemStack;
    }

    public static ItemStack addEnchantment(ItemStack itemStack, Enchantment enchantment, int level) {
        itemStack.addUnsafeEnchantment(enchantment, level);
        return itemStack;
    }

    public static ItemStack removeEnchantments(ItemStack itemStack) {
        for (Enchantment e : itemStack.getEnchantments().keySet())
            itemStack.removeEnchantment(e);
        return itemStack;
    }

}
