package com.github.skiwi2.hearthmonitor.commands;

import org.junit.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class AbstractCommandTest {
    @Test
    public void testExecute() {
        AtomicInteger atomicInteger = new AtomicInteger();
        Command command = new SetIntegerCommand(atomicInteger, 7);
        assertEquals(0, atomicInteger.get());

        command.execute();
        assertEquals(7, atomicInteger.get());
    }

    @Test(expected = IllegalStateException.class)
    public void testExecuteAlreadyExecuted() {
        AtomicInteger atomicInteger = new AtomicInteger();
        Command command = new SetIntegerCommand(atomicInteger, 7);
        command.execute();  //ok
        command.execute();  //fail
    }

    @Test
    public void testUndo() {
        AtomicInteger atomicInteger = new AtomicInteger();
        Command command = new SetIntegerCommand(atomicInteger, 7);

        command.execute();
        command.undo();
        assertEquals(0, atomicInteger.get());
    }

    @Test(expected = IllegalStateException.class)
    public void testUndoAlreadyNotExecuted() {
        AtomicInteger atomicInteger = new AtomicInteger();
        Command command = new SetIntegerCommand(atomicInteger, 7);
        command.undo();
    }

    @Test
    public void testIsExecuted() {
        AtomicInteger atomicInteger = new AtomicInteger();
        Command command = new SetIntegerCommand(atomicInteger, 7);
        assertFalse(command.isExecuted());

        command.execute();
        assertTrue(command.isExecuted());

        command.undo();
        assertFalse(command.isExecuted());
    }

    @Test
    public void testTwoTimesExecuteUndo() {
        AtomicInteger atomicInteger = new AtomicInteger();
        Command command = new SetIntegerCommand(atomicInteger, 7);

        assertFalse(command.isExecuted());
        assertEquals(0, atomicInteger.get());

        for (int i = 0; i < 2; i++) {
            command.execute();

            assertTrue(command.isExecuted());
            assertEquals(7, atomicInteger.get());

            command.undo();

            assertFalse(command.isExecuted());
            assertEquals(0, atomicInteger.get());
        }
    }

    private static class SetIntegerCommand extends AbstractCommand {
        private final AtomicInteger atomicInteger;
        private final int value;

        private int oldValue;

        private SetIntegerCommand(final AtomicInteger atomicInteger, final int value) {
            this.atomicInteger = Objects.requireNonNull(atomicInteger, "atomicInteger");
            this.value = value;
        }

        @Override
        protected void executeImpl() {
            oldValue = atomicInteger.get();
            atomicInteger.set(value);
        }

        @Override
        protected void undoImpl() {
            atomicInteger.set(oldValue);
        }
    }
}