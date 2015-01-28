package com.github.skiwi2.hearthmonitor.commands;

/**
 * Command that can be extended for concrete instantiations.
 *
 * @author Frank van Heeswijk
 */
public abstract class AbstractCommand implements Command {
    private final Object lock = new Object();

    private boolean executed = false;

    @Override
    public void execute() {
        synchronized (lock) {
            if (executed) {
                throw new IllegalStateException("This command has already been executed");
            }
            executeImpl();
            executed = true;
        }
    }

    @Override
    public void undo() {
        synchronized (lock) {
            if (!executed) {
                throw new IllegalStateException("This command has not yet been executed");
            }
            undoImpl();
            executed = false;
        }
    }

    @Override
    public boolean isExecuted() {
        synchronized (lock) {
            return executed;
        }
    }

    /**
     * Executes this command.
     */
    protected abstract void executeImpl();

    /**
     * Undoes the execution of this command.
     */
    protected abstract void undoImpl();
}
