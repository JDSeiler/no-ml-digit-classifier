package org.ru.pso;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A class for writing the results of image comparisons to a CSV file.
 */
public class OutputWriter {
    private final PrintWriter outFile;

    /**
     * Opens a new OutputWriter that will output to `destination`.
     *
     * @param destination The location of the file to dump output.
     *                    If the file does not already exist, it will be created.
     *                    If the file does exist, it will be truncated.
     * @throws IOException
     */
    public OutputWriter(String destination) throws IOException {
        Path outputFileDestination = Paths.get(destination).toAbsolutePath();
        // See: https://docs.oracle.com/javase/7/docs/api/java/io/BufferedWriter.html
        // This is kinda absurd...
        this.outFile = new PrintWriter(new BufferedWriter(new FileWriter(outputFileDestination.toString())));
    }

    /**
     * Closes the internal file handle for this OutputWriter and cleans
     * up related resources.
     */
    public void close() {
        this.outFile.close();
    }

    public void write(String line) {
        this.outFile.println(line);
    }
}
