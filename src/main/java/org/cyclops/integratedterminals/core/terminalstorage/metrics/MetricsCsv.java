package org.cyclops.integratedterminals.core.terminalstorage.metrics;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Appends CSV rows to files under the metrics output directory.
 *
 * Debug-only helper for measuring the cost of opening a storage terminal.
 * Only used when {@link org.cyclops.integratedterminals.GeneralConfig#debugTerminalOpenMetrics} is set.
 */
public final class MetricsCsv {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Output directory, overridable so a dedicated server and a client can write to distinct locations.
     */
    private static final Path DIRECTORY = Paths.get(
            System.getProperty("integratedterminals.debugTerminalOpenMetricsDir", "run/metrics"));

    private static final Map<String, Writer> WRITERS = new ConcurrentHashMap<>();

    private MetricsCsv() {
    }

    /**
     * Append one row to the given CSV file, writing the header first if the file is new.
     * @param fileName The file name, without directories.
     * @param header The comma-separated header, used only when the file is created.
     * @param row The comma-separated row.
     */
    public static void append(String fileName, String header, String row) {
        try {
            // Re-open when the file has been removed underneath us, so clearing results
            // between runs does not silently send rows to an unlinked file.
            if (!Files.exists(DIRECTORY.resolve(fileName))) {
                Writer stale = WRITERS.remove(fileName);
                if (stale != null) {
                    try {
                        stale.close();
                    } catch (IOException ignored) {
                        // Nothing useful to do, we are about to replace it.
                    }
                }
            }
            Writer writer = WRITERS.computeIfAbsent(fileName, name -> open(name, header));
            synchronized (writer) {
                writer.write(row);
                writer.write('\n');
                writer.flush();
            }
        } catch (UncheckedIOException | IOException e) {
            LOGGER.error("Failed to write terminal open metrics to {}", fileName, e);
        }
    }

    private static Writer open(String fileName, String header) {
        try {
            Files.createDirectories(DIRECTORY);
            Path file = DIRECTORY.resolve(fileName);
            boolean isNew = !Files.exists(file);
            Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            if (isNew) {
                writer.write(header);
                writer.write('\n');
                writer.flush();
            }
            LOGGER.info("Terminal open metrics: writing to {}", file.toAbsolutePath());
            return writer;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * @return The value of the {@code integratedterminals.debugTerminalOpenMetricsTag} property,
     *         used to label rows with the scenario they belong to.
     */
    public static String tag() {
        return System.getProperty("integratedterminals.debugTerminalOpenMetricsTag", "untagged");
    }
}
