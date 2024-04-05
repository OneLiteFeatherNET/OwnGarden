package fr.skyost.owngarden.util;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ZipUtils {

    private static final ComponentLogger logger = ComponentLogger.logger(ZipUtils.class.getSimpleName());

    private ZipUtils() throws IllegalAccessException {
        throw new IllegalAccessException("");
    }

    /**
     * Extracts the samples to the specified directory.
     */
    public static void extractZip(final InputStream file, final Path destination) {

        byte[] buffer = new byte[1024];

        try (final ZipInputStream zis = new ZipInputStream(file)) {

            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                final Path newFile = newFile(destination, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!Files.isDirectory(newFile)) {
                        Files.createDirectories(newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    final Path parent = newFile.getParent();
                    if (!Files.isDirectory(parent)) {
                        Files.createDirectories(parent);
                    }

                    // write file content
                    try (OutputStream fos = Files.newOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }

            zis.closeEntry();
        } catch (IOException e) {
            logger.error("Can't unzip file to " + destination.toString(), e);
        }
    }

    private static Path newFile(final Path destinationDir, final ZipEntry zipEntry) throws IOException {
        final Path destFile = Path.of(destinationDir.toString(), zipEntry.getName());

        if (!destFile.toString().startsWith(destinationDir.toString() + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
