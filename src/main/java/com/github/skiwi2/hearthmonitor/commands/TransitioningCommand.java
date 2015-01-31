package com.github.skiwi2.hearthmonitor.commands;

import com.cardshifter.modapi.base.ECSGame;
import com.github.skiwi2.hearthmonitor.logapi.zone.TransitioningLogEntry;

import java.util.Objects;

/**
 * Command to execute the TransitioningLogEntry on a game.
 *
 * @author Frank van Heeswijk
 */
public class TransitioningCommand extends AbstractCommand {
    private final ECSGame ecsGame;
    private final TransitioningLogEntry transitioningLogEntry;

    /**
     * Constructs a new TransitioningCommand instance.
     *
     * @param ecsGame   The game instance
     * @param transitioningLogEntry    The log entry
     * @throws NullPointerException   If transitioningLogEntry is null.
     */
    public TransitioningCommand(final ECSGame ecsGame, final TransitioningLogEntry transitioningLogEntry) {
        this.ecsGame = Objects.requireNonNull(ecsGame, "ecsGame");
        this.transitioningLogEntry = Objects.requireNonNull(transitioningLogEntry, "transitioningLogEntry");
    }

    @Override
    protected void executeImpl() {

    }

    @Override
    protected void undoImpl() {

    }
}
