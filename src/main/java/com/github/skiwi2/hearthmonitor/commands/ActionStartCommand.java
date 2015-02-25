package com.github.skiwi2.hearthmonitor.commands;

import com.cardshifter.modapi.base.ECSGame;
import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.ActionStartLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.FullEntityLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.ShowEntityLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.TagChangeLogEntry;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Command to execute the ActionStartCommand in the game.
 *
 * @author Frank van Heeswijk
 */
public class ActionStartCommand extends AbstractCommand {
    private static final Map<Class<?>, BiFunction<ECSGame, LogEntry, Command>> COMMAND_MAP = new HashMap<>();
    static {
        COMMAND_MAP.put(FullEntityLogEntry.class, (ecsGame, logEntry) -> new FullEntityCommand(ecsGame, (FullEntityLogEntry)logEntry));
        COMMAND_MAP.put(TagChangeLogEntry.class, (ecsGame, logEntry) -> new TagChangeCommand(ecsGame, (TagChangeLogEntry)logEntry));
        //disabled TransitioningLogEntry as it should be covered by tag updates
//        COMMAND_MAP.put(TransitioningLogEntry.class, (ecsGame, logEntry) -> new TransitioningCommand(ecsGame, (TransitioningLogEntry)logEntry));
        COMMAND_MAP.put(ShowEntityLogEntry.class, (ecsGame, logEntry) -> new ShowEntityCommand(ecsGame, (ShowEntityLogEntry)logEntry));
        COMMAND_MAP.put(ActionStartLogEntry.class, (ecsGame, logEntry) -> new ActionStartCommand(ecsGame, (ActionStartLogEntry)logEntry));
    }
    //TODO refactor to a common class to hold the COMMAND_MAP

    private final ECSGame ecsGame;
    private final ActionStartLogEntry actionStartLogEntry;

    private List<Command> logEntryCommands;

    /**
     * Constructs a new ActionStartCommand instance.
     *
     * @param ecsGame   The game instance
     * @param actionStartLogEntry   The log entry
     * @throws  java.lang.NullPointerException  If actionStartLogEntry is null.
     */
    public ActionStartCommand(final ECSGame ecsGame, final ActionStartLogEntry actionStartLogEntry) {
        this.ecsGame = Objects.requireNonNull(ecsGame, "ecsGame");
        this.actionStartLogEntry = Objects.requireNonNull(actionStartLogEntry, "actionStartLogEntry");
    }

    @Override
    protected void executeImpl() {
        List<LogEntry> logEntries = actionStartLogEntry.getLogEntries();
        logEntryCommands = logEntries.stream()
            .map(logEntry -> {
                if (!COMMAND_MAP.containsKey(logEntry.getClass())) {
                    //ignore
                    System.out.println("No mapping has been found for " + logEntry.getClass());
                    return null;
                }
                return COMMAND_MAP.get(logEntry.getClass()).apply(ecsGame, logEntry);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        logEntryCommands.forEach(Command::execute);
    }

    @Override
    protected void undoImpl() {
        Collections.reverse(logEntryCommands);
        logEntryCommands.forEach(Command::undo);
    }
}
