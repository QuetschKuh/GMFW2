package de.lolsu.gmfw.api;

public enum TerminalColors {

    RESET(0),
    BOLD(1),
    UNDERLINE(4),
    BLINK(5),

    COLOR_OFF(22),
    ITALIC_OFF(23),
    UNDERLINE_OFF(24),
    BLINK_OFF(25),

    RED(31),
    GREEN(32),
    YELLOW(33),
    BLUE(34),
    MAGENTA(35),
    CYAN(36),
    BRIGHT_GRAY(37),

    GRAY(90),
    BRIGHT_RED(91),
    BRIGHT_GREEN(92),
    BRIGHT_YELLOW(93),
    BRIGHT_BLUE(94),
    BRIGHT_MAGENTA(95),
    BRIGHT_CYAN(96),
    WHITE(97),
    ;

    final int code;

    TerminalColors(int code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return (char) 27 + "[" + code + "m";
    }

}
