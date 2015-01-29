package com.github.skiwi2.hearthmonitor;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.github.skiwi2.hearthmonitor.commands.Command;
import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.CreateGameLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.CreateGameLogEntry.PlayerLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.CloseableLogReader;
import com.github.skiwi2.hearthmonitor.logreader.LogReader;
import com.github.skiwi2.hearthmonitor.logreader.NotReadableException;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.LogLineUtils;
import com.github.skiwi2.hearthmonitor.logreader.logreaders.FileLogReader;
import com.github.skiwi2.hearthmonitor.model.Game;
import com.github.skiwi2.hearthmonitor.model.HearthStoneMod;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Main application.
 *
 * @author Frank van Heeswijk
 */
public class HearthMonitor {
    private final Path logPath;

    private final List<Game> games = new ArrayList<>();

    private HearthMonitor(final Path logPath) {
        this.logPath = Objects.requireNonNull(logPath, "logPath");
    }

    private void init() throws Exception {
        try (CloseableLogReader logReader = new FileLogReader(
            Files.newBufferedReader(logPath, StandardCharsets.UTF_8),
            EntryParsers.getHearthStoneEntryParsers(),
            LogLineUtils::isFromNamedLogger)
        ) {
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
                    Game game = new Game(initialGame, commands);
                    games.add(game);
                }
                else {
                    //unexpected, drop entry
                }
            }
        }
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

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Invalid syntax, expected: <HEARTHSTONE_LOG_FILE_PATH>");
        }
        Path path = Paths.get(args[0]);
        new HearthMonitor(path).init();
    }
}
