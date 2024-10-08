package de.lolsu.gmfw.main;

import de.lolsu.gmfw.api.TerminalColors;
import de.lolsu.gmfw.config.ConfVar;
import org.bukkit.ChatColor;

/**
 * A basic utility class, used for logging info, warnings, and errors.<br>
 * Chooses the correct output stream and prefix based on the action.
 * */
public class Logger {

    // Variables
    /** Throws {@link RuntimeException Runtime Exceptions} when set to true (default) */
    public static boolean throwErrors = true;

    public static String infoPrefix = TerminalColors.BLUE + "GMINFO | ";
    public static String warnPrefix = TerminalColors.YELLOW + "GMWARN | ";
    public static String errorPrefix = TerminalColors.RED + "GMERROR | ";

    /**
     * Prints a blue informational message to {@link System#out stdout}.
     * @param message An easy-to-read but precise message that explains what happened
     * */
    public static void info(String message) {
        System.out.println(infoPrefix + message.replace("\n", "\n" + infoPrefix) + TerminalColors.RESET);
    }

    /**
     * Prints a yellow warning message to {@link System#err stderr}.
     * @param message An easy-to-read but precise message that explains what happened
     * */
    public static void warn(String message) {
        System.err.println(warnPrefix + message.replace("\n", "\n" + warnPrefix) + TerminalColors.RESET);
    }

    /**
     * Prints a red error message to {@link System#err stderr}.
     * @param message An easy-to-read but precise message that explains what happened
     * */
    public static void error(String message) {
        System.err.println(errorPrefix + message.replace("\n", "\n" + errorPrefix) + TerminalColors.RESET);
        if(throwErrors) throw new RuntimeException();
    }

    /**
     * Prints a red error message to {@link System#err stderr}.<br>
     * Includes the given {@code error} in the stacktrace.
     * @param message An easy-to-read but precise message that explains what happened
     * @param error The {@link Exception} that caused this fiasco
     * */
    public static void error(String message, Exception error) {
        error(message + "\n" + error);
        if(throwErrors && error instanceof RuntimeException) throw (RuntimeException) error;
    }

    public static void logvars() {
        System.out.println("Variables:");
        for(ConfVar var : ConfVar.values()) {
            System.out.println(var.getObjectType() + " : " + var.getObject());
        }
    }

}
