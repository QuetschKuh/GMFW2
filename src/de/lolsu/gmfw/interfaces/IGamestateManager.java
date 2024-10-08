package de.lolsu.gmfw.interfaces;

public abstract class IGamestateManager {

    public static State state = State.PREGAME;

    /** There are two phases to initialization. First we create the classes and send in our references.<br>
     * Once that's done we call all the functions and stuff */
    public abstract void onStart();

    /** Called when (or instead of) there are enough players for the game to begin and the countdown starts, waiting for more players to join */
    public abstract void onCountdownStart();

    /** Called when (or instead of) there are enough players for the game to be started immediately and the countdown is reduced */
    public abstract void onCountdownQuick();

    /** Cancels the countdown, usually called when too few players */
    public abstract void onCountdownCancel();

    /** Called 10 seconds before the game starts, after (or instead of) voting ending */
    public abstract void onVotingEnd();

    /** Called after (or instead of) the players have being teleported to the game world */
    public abstract void onGameStart();

    /** Called after (or instead of) the 'watch' period ending, when players are allowed to move freely again */
    public abstract void onWatchEnd();

    /** Called after (or instead of) the warmup ending and pvp being enabled, not relevant when  */
    public abstract void onWarmupEnd();

    /** Called when the second-to-last player dies and the game is considered over */
    public abstract void onGameEnd();

    /** Called after a short delay from the game being over, after (or instead of) the podium being initialized */
    public abstract void onPodiumStart();

    /** Called right before server restart in case you need to save something or shut systems down properly */
    public abstract void onShutdown();

    public enum State {
        PREGAME, INGAME, POSTGAME
    }

}
