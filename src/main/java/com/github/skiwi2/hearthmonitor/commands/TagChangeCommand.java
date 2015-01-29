package com.github.skiwi2.hearthmonitor.commands;

import com.cardshifter.modapi.base.ECSGame;
import com.github.skiwi2.hearthmonitor.logapi.power.TagChangeLogEntry;

import java.util.Objects;

/**
 * Command to execute the TagChangeLogEntry on a game.
 *
 * @author Frank van Heeswijk
 */
public class TagChangeCommand extends AbstractCommand {
    private final ECSGame ecsGame;
    private final TagChangeLogEntry tagChangeLogEntry;

    /**
     * Constructs a new TagChangeCommand instance.
     *
     * @param ecsGame   The game instance
     * @param tagChangeLogEntry    The log entry
     * @throws NullPointerException   If tagChangeLogEntry is null.
     */
    public TagChangeCommand(final ECSGame ecsGame, final TagChangeLogEntry tagChangeLogEntry) {
        this.ecsGame = Objects.requireNonNull(ecsGame, "ecsGame");
        this.tagChangeLogEntry = Objects.requireNonNull(tagChangeLogEntry, "tagChangeLogEntry");
    }

    @Override
    protected void executeImpl() {

    }

    @Override
    protected void undoImpl() {

    }
}
