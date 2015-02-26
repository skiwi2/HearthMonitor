package com.github.skiwi2.hearthmonitor.commands;

import com.cardshifter.modapi.base.ECSGame;

import java.util.Objects;

/**
 * @author Frank van Heeswijk
 */
public class CommandContext {
    private final ECSGame ecsGame;

    public CommandContext(final ECSGame ecsGame) {
        this.ecsGame = Objects.requireNonNull(ecsGame, "ecsGame");
    }

    public ECSGame getEcsGame() {
        return ecsGame;
    }
}
