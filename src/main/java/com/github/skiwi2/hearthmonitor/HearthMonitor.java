package com.github.skiwi2.hearthmonitor;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.github.skiwi2.hearthmonitor.commands.Command;
import com.github.skiwi2.hearthmonitor.commands.FullEntityCommand;
import com.github.skiwi2.hearthmonitor.commands.TagChangeCommand;
import com.github.skiwi2.hearthmonitor.commands.TransitioningCommand;
import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.CreateGameLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.FullEntityLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.PlayerLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.TagChangeLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.zone.TransitioningLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.CloseableLogReader;
import com.github.skiwi2.hearthmonitor.logreader.NotReadableException;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.LogLineUtils;
import com.github.skiwi2.hearthmonitor.logreader.logreaders.FileLogReader;
import com.github.skiwi2.hearthmonitor.model.Game;
import com.github.skiwi2.hearthmonitor.model.HearthStoneMod;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Main application.
 *
 * @author Frank van Heeswijk
 */
public class HearthMonitor {
    private static final Map<Class<?>, BiFunction<ECSGame, LogEntry, Command>> COMMAND_MAP = new HashMap<>();
    static {
        COMMAND_MAP.put(FullEntityLogEntry.class, (ecsGame, logEntry) -> new FullEntityCommand(ecsGame, (FullEntityLogEntry)logEntry));
        COMMAND_MAP.put(TagChangeLogEntry.class, (ecsGame, logEntry) -> new TagChangeCommand(ecsGame, (TagChangeLogEntry)logEntry));
        COMMAND_MAP.put(TransitioningLogEntry.class, (ecsGame, logEntry) -> new TransitioningCommand(ecsGame, (TransitioningLogEntry)logEntry));
    }

    public static List<Game> readGamesFromLog(final Path logFile) throws Exception {
        List<Game> games = new ArrayList<>();
        try (CloseableLogReader logReader = new FileLogReader(
            Files.newBufferedReader(logFile, StandardCharsets.UTF_8),
            EntryParsers.getHearthStoneEntryParsers(),
            LogLineUtils::isFromNamedLogger)
        ) {
            Game lastGame = null;
            while (logReader.hasNextEntry()) {
                LogEntry logEntry;
                try {
                    logEntry = logReader.readNextEntry();
                } catch (NotReadableException ex) {
                    continue;
                }
                if (logEntry instanceof CreateGameLogEntry) {
                    ECSGame initialGame = createInitialGame((CreateGameLogEntry)logEntry);
                    List<Command> commands = new ArrayList<>();
                    lastGame = new Game(initialGame, commands);
                    games.add(lastGame);
                } else {
                    if (lastGame == null) {
                        //ignore
                        System.out.println("Ignoring log entry " + logEntry + ", no game has been created yet");
                    }
                    else {
                        if (!COMMAND_MAP.containsKey(logEntry.getClass())) {
                            //ignore
                            System.out.println("No mapping has been found for " + logEntry.getClass());
                        }
                        Command command = COMMAND_MAP.get(logEntry.getClass()).apply(lastGame.getInitialGame(), logEntry);
                        lastGame.addCommand(command);
                    }
                }
            }
        }
        return games;
    }

    private static ECSGame createInitialGame(final CreateGameLogEntry createGameLogEntry) {
        HearthStoneMod hearthStoneMod = new HearthStoneMod();
        ECSGame initialGame = new ECSGame();
        hearthStoneMod.setupGame(initialGame);

        Set<PlayerLogEntry> playerLogEntries = createGameLogEntry.getPlayerLogEntries();
        PlayerLogEntry self = playerLogEntries.stream().filter(playerLogEntry -> Objects.equals(playerLogEntry.getTagValue("FIRST_PLAYER"), "1")).findFirst().get();
        PlayerLogEntry opponent = playerLogEntries.stream().filter(playerLogEntry -> Objects.isNull(playerLogEntry.getTagValue("FIRST_PLAYER"))).findFirst().get();

        convertPlayerLogEntryToEntity(initialGame::newEntity, self);
        convertPlayerLogEntryToEntity(initialGame::newEntity, opponent);

        return initialGame;
    }

    private static void convertPlayerLogEntryToEntity(final Supplier<Entity> newEntitySupplier, final PlayerLogEntry playerLogEntry) {
        Entity entity = newEntitySupplier.get();
        String playerId = playerLogEntry.getTagValue("PLAYER_ID");
        PlayerComponent playerComponent = new PlayerComponent(Integer.parseInt(playerId), "PlayerID " + playerId);
        //at a later point we set the actual player name
        entity.addComponent(playerComponent);
    }
}
