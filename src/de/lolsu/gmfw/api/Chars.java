package de.lolsu.gmfw.api;

import org.bukkit.ChatColor;

/**
 * Here we store special characters, emoticons, and general shortcuts for commonly used long strings or string arrays
 * */
public class Chars {

    public static String[] blackCircledNumbers = {"⓿", "❶", "❷", "❸", "❹", "❺", "❻", "❼", "❽", "❾", "❿", "⓫", "⓬", "⓭", "⓮", "⓯", "⓰", "⓱", "⓲", "⓳", "⓴"};
    public static String shrug = "¯\\\\_(ツ)_/¯";
    public static String divider = "" + ChatColor.RESET + ChatColor.GRAY + ChatColor.STRIKETHROUGH + "--------------------";
    public static String[] placements = {"zeroth", "first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eighth", "ninth", "tenth", "eleventh", "twelfth", "thirteenth", "fourteenth", "fifteenth", "sixteenth", "seventeenth", "eighteenth", "nineteenth", "twentieth"};

    /**
     * Turns the integer into a placement abbreviation. (e.g. 1 -> "1st", 12 -> "12th", 1042 -> "1042nd")
     * @param placement The placement you want to have abbreviated
     * @return The number with an appended st, nd, rd or th depending on the number
     * */
    public static String getPlacementNumerical(int placement) {
        if(placement % 10 == 1 && placement % 100 != 11)
            return placement + "st";
        else if(placement % 10 == 2 && placement % 100 != 12)
            return placement + "nd";
        else if(placement % 10 == 3 && placement % 100 != 13)
            return placement + "rd";
        else return placement + "th";
    }

}
