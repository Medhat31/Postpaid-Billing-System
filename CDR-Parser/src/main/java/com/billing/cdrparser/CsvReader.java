package com.billing.cdrparser;

import com.opencsv.bean.CsvToBeanBuilder;
import java.io.FileReader;
import java.util.List;

/**
 * Utility class for reading Call Detail Records (CDRs) from CSV format files.
 * This class uses OpenCSV to parse CSV records and map them directly into
 * structured {@link LineItem} objects.
 *
 * @author seifabsalam
 * @version 1.0
 * @since 1.0
 */
public class CsvReader {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private CsvReader() {
        // Utility class has only static methods and should not be instantiated.
    }

    /**
     * Reads and parses a CSV file into a list of {@code LineItem} objects.
     * The CSV file columns must map to the annotated fields in {@code LineItem}.
     *
     * @param filePath the path to the CSV source file to read
     * @return a list of parsed {@code LineItem} records
     * @throws Exception if the file path is invalid, inaccessible, or if parsing fails due to malformed content
     */
    public static List<LineItem> read(String filePath) throws Exception {
        try (FileReader reader = new FileReader(filePath)) {
            return new CsvToBeanBuilder<LineItem>(reader)
                    .withType(LineItem.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build()
                    .parse();
        }
    }
}
