package com.billing.cdrparser;

import java.io.FileInputStream;
import java.nio.file.*;
import java.util.List;
import java.util.Properties;

/**
 * Main entry point for the CDR (Call Detail Record) parsing application.
 * This class monitors a designated input directory for new CSV files,
 * parses them into structured records, persists them to the database, and
 * archives the processed files.
 *
 * @author seifabsalam
 * @version 1.0
 * @since 1.0
 */
public class CDRParser {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private CDRParser() {
        // Main application class; should not be instantiated.
    }

    /**
     * Starts the directory watch service and processes incoming CDR files.
     * Reads directories from {@code io.properties}, connects to the database,
     * and monitors for new file creation events.
     *
     * @param args the command-line arguments (not used)
     * @throws Exception if configuration loading, database connection, or file monitoring setup fails
     */
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        try (var fis = new FileInputStream("io.properties")) {
            props.load(fis);
        }

        String inputDirVal  = props.getProperty("input.dir");
        String outputDirVal = props.getProperty("output.dir");
        if (inputDirVal == null || outputDirVal == null) {
            throw new IllegalStateException(
                    "io.properties must define both input.dir and output.dir");
        }

        Path inputDir  = Path.of(inputDirVal);
        Path outputDir = Path.of(outputDirVal);

        var ctx = DatabaseAdaptor.connect();

        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            inputDir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
            System.out.println("Watching: " + inputDir);

            // Infinite loop runs continuously to poll the watcher for filesystem events.
            while (true) {
                WatchKey key = watcher.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    // Overflow events occur when events are lost or discarded by the OS watcher.
                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) continue;

                    Path filename = (Path) event.context();
                    // Restrict processing to CSV files to avoid processing temporary or incomplete file writes.
                    if (!filename.toString().endsWith(".csv")) continue;

                    Path file   = inputDir.resolve(filename);
                    Path target = outputDir.resolve(filename);

                    try {
                        List<LineItem> records = CsvReader.read(file.toString());
                        DatabaseWriter.write(records, ctx);
                        // Move the processed file to the archive folder to avoid parsing it again.
                        Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Processed: " + filename);
                    } catch (Exception e) {
                        System.getLogger(CDRParser.class.getName())
                                .log(System.Logger.Level.ERROR, "Failed to process: " + filename, e);
                    }
                }

                if (!key.reset()) break;
            }
        }
    }
}
