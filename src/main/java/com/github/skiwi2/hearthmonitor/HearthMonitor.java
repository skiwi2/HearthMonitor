package com.github.skiwi2.hearthmonitor;

import com.github.skiwi2.hearthmonitor.logreader.CloseableLogReader;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.LogLineUtils;
import com.github.skiwi2.hearthmonitor.logreader.logreaders.FileLogReader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main application.
 *
 * @author Frank van Heeswijk
 */
public class HearthMonitor {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Invalid syntax, expected: <HEARTHSTONE_LOG_FILE_PATH>");
        }

        Path path = Paths.get(args[0]);

        try (CloseableLogReader logReader = new FileLogReader(
            Files.newBufferedReader(path, StandardCharsets.UTF_8),
            EntryParsers.getHearthStoneEntryParsers(),
            LogLineUtils::isFromNamedLogger)
        ) {
            //TODO implement processing
        }
    }
}
