package com.github.skiwi2.hearthmonitor.model;

import com.cardshifter.modapi.base.ECSGame;
import com.github.skiwi2.hearthmonitor.commands.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Structure to store the initial state of a game and a list of commands that operate on the previous state.
 *
 * @author Frank van Heeswijk
 */
public final class Game {
    private final ECSGame initialGame;
    private final List<Command> commands;

    /**
     * Constructs a new Game instance.
     *
     * @param initialGame   The initial state of a game
     * @param commands  The list of commands that operates on the previous state of the game.
     */
    public Game(final ECSGame initialGame, final List<Command> commands) {
        this.initialGame = Objects.requireNonNull(initialGame, "initialGame");
        Objects.requireNonNull(commands, "commands");
        this.commands = new ArrayList<>(commands);
    }

    /**
     * Returns the initial state of the game.
     *
     * @return  The initial state of the game.
     */
    public ECSGame getInitialGame() {
        return initialGame;
    }

    /**
     * Returns the list of commands that operate on the previous state.
     *
     * @return  The list of commands that operate on the previous state.
     */
    public List<Command> getCommands() {
        return new ArrayList<>(commands);
    }

    /**
     * Appends the command to the list of commands.
     *
     * @param command   The command to add
     * @throws java.lang.NullPointerException   If command is null.
     */
    public void addCommand(final Command command) {
        Objects.requireNonNull(command, "command");
        commands.add(command);
    }
}
