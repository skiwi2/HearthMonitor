package com.github.skiwi2.hearthmonitor.commands;

import com.cardshifter.modapi.base.ECSGame;
import com.github.skiwi2.hearthmonitor.logapi.power.FullEntityLogEntry;

import java.util.Objects;

/**
 * Command to execute the FullEntityLogEntry on a game.
 *
 * @author Frank van Heeswijk
 */
public class FullEntityCommand extends AbstractCommand {
    private final ECSGame ecsGame;
    private final FullEntityLogEntry fullEntityLogEntry;

    /**
     * Constructs a new FullEntityCommand instance.
     *
     * @param ecsGame   The game instance
     * @param fullEntityLogEntry    The log entry
     * @throws java.lang.NullPointerException   If fullEntityLogEntry is null.
     */
    public FullEntityCommand(final ECSGame ecsGame, final FullEntityLogEntry fullEntityLogEntry) {
        this.ecsGame = Objects.requireNonNull(ecsGame, "ecsGame");
        this.fullEntityLogEntry = Objects.requireNonNull(fullEntityLogEntry, "fullEntityLogEntry");
    }

    @Override
    protected void executeImpl() {

    }

    @Override
    protected void undoImpl() {

    }
}
