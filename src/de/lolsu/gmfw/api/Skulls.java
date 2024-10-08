package de.lolsu.gmfw.api;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * A utility class used to generate skull ItemStacks using either player skins, custom textures or even custom urls entirely
 * */
public class Skulls {

    /** Gets you the skull of a certain player
     * @param name The display name of the skull
     * @param lore The lore of the skull
     * @param playerName The player whose skin should be applied to the skull
     * @return An itemstack of type skull with the skin of the given player applied to it
     * */
    public static ItemStack getPlayerSkull(String name, List<String> lore, String playerName) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setDisplayName(name);
        skullMeta.setLore(lore);
        skullMeta.setOwner(playerName);
        skull.setItemMeta(skullMeta);
        return skull;
    }

    /** Gets you a skull using Base64 encoded data
     * @param name The display name of the skull
     * @param lore The lore of the skull
     * @param b64 The data that should be applied to the skull
     * @return An itemstack of type skull with the texture encoded in the b64 String
     * */
    public static ItemStack getCustomSkull(String name, List<String> lore, String b64) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setDisplayName(name);
        skullMeta.setLore(lore);

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", b64));

        try {
            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullMeta, profile);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }

        skull.setItemMeta(skullMeta);
        return skull;
    }

    /** Gets you a skull using data fetched from the given url
     * @param name The display name of the skull
     * @param lore The lore of the skull
     * @param url The url of the texture on the textures.minecraft.net/texture/ server
     * @param prependMCT Whether the minecraft texture server url should be prepended
     * @return An itemstack of type skull with the texture on the given url
     * */
    public static ItemStack getTexturedSkull(String name, List<String> lore, String url, boolean prependMCT) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setDisplayName(name);
        skullMeta.setLore(lore);

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        if(prependMCT) url = "http://textures.minecraft.net/texture/" + url;
        byte[] data = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(data)));

        try {
            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullMeta, profile);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }

        skull.setItemMeta(skullMeta);
        return skull;
    }

    public enum Textures {
        WHITE_ARROW_LEFT("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2RjOWU0ZGNmYTQyMjFhMWZhZGMxYjViMmIxMWQ4YmVlYjU3ODc5YWYxYzQyMzYyMTQyYmFlMWVkZDUifX19", "cdc9e4dcfa4221a1fadc1b5b2b11d8beeb57879af1c42362142bae1edd5"),
        WHITE_ARROW_RIGHT("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTU2YTM2MTg0NTllNDNiMjg3YjIyYjdlMjM1ZWM2OTk1OTQ1NDZjNmZjZDZkYzg0YmZjYTRjZjMwYWI5MzExIn19fQ==", "956a3618459e43b287b22b7e235ec699594546c6fcd6dc84bfca4cf30ab9311"),
        WHITE_ARROW_UP("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWFkNmM4MWY4OTlhNzg1ZWNmMjZiZTFkYzQ4ZWFlMmJjZmU3NzdhODYyMzkwZjU3ODVlOTViZDgzYmQxNGQifX19", "1ad6c81f899a785ecf26be1dc48eae2bcfe777a862390f5785e95bd83bd14d"),
        WHITE_ARROW_DOWN("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODgyZmFmOWE1ODRjNGQ2NzZkNzMwYjIzZjg5NDJiYjk5N2ZhM2RhZDQ2ZDRmNjVlMjg4YzM5ZWI0NzFjZTcifX19", "882faf9a584c4d676d730b23f8942bb997fa3dad46d4f65e288c39eb471ce7"),
        VILLAGER_HOUSE("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTk0Yzc5ZjAzZTE5MWI5MzQ3N2Y3YzE5NTU3NDA4ZjdhZjRmOTY2MGU1ZGZiMDY4N2UzYjhlYjkyZmJkM2FlMSJ9fX0=", "594c79f03e191b93477f7c19557408f7af4f9660e5dfb0687e3b8eb92fbd3ae1"),
        GREEN_PLUS("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZmMzE0MzFkNjQ1ODdmZjZlZjk4YzA2NzU4MTA2ODFmOGMxM2JmOTZmNTFkOWNiMDdlZDc4NTJiMmZmZDEifX19", "5ff31431d64587ff6ef98c0675810681f8c13bf96f51d9cb07ed7852b2ffd1")
        ;

        public final String b64;
        public final String url;
        Textures(String b64, String url) {
            this.b64 = b64;
            this.url = url;
        }

        public ItemStack getSkullItem(String name, String... lore) {
            return getTexturedSkull(name, Arrays.asList(lore), url, true);
        }
    }

}