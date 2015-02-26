package com.github.skiwi2.hearthmonitor.commands;

/**
 * @author Frank van Heeswijk
 */
public final class EmptyCommand implements Command {
    @Override
    public void execute() {

    }

    @Override
    public void undo() {

    }

    @Override
    public boolean isExecuted() {
        return false;
    }
}
