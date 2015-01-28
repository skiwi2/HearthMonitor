package com.github.skiwi2.hearthmonitor.commands;

/**
 * Used to execute a command or undo the execution of it.
 *
 * @author Frank van Heeswijk
 */
public interface Command {
    /**
     * Executes this command.
     *
     * This will set the execution status to executed.
     *
     * @throws  java.lang.IllegalStateException If the command has already been executed.
     */
    void execute();

    /**
     * Undoes the execution of this command.
     *
     * This will set the execution status to not executed.
     *
     * @throws  java.lang.IllegalStateException If the command has not been executed yet.
     */
    void undo();

    /**
     * Returns whether the command has been executed.
     *
     * @return  Whether the command has been executed.
     */
    boolean isExecuted();
}
