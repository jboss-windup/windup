package org.jboss.windup.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarInputStream;
import org.kamranzafar.jtar.TarOutputStream;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TarUtil
{
    public static void tarDirectory(Path outputFile, Path inputDirectory) throws IOException
    {
        System.out.println("Creating archive at: " + outputFile);
        // Output file stream
        FileOutputStream dest = new FileOutputStream(outputFile.toFile());
        final Path outputFileAbsolute = outputFile.normalize().toAbsolutePath();

        final Path inputDirectoryAbsolute = inputDirectory.normalize().toAbsolutePath();
        final int inputPathLength = inputDirectoryAbsolute.toString().length();

        // Create a TarOutputStream
        try (TarOutputStream out = new TarOutputStream(new BufferedOutputStream(dest)))
        {
            Files.walk(inputDirectoryAbsolute).forEach(entry -> {
                if (Files.isDirectory(entry))
                    return;

                // Don't try to compress the output.tar file into itself
                if (entry.equals(outputFileAbsolute))
                    return;

                try
                {
                    String relativeName = entry.toString().substring(inputPathLength + 1);

                    out.putNextEntry(new TarEntry(entry.toFile(), relativeName));
                    BufferedInputStream origin = new BufferedInputStream(new FileInputStream(entry.toFile()));
                    int count;
                    byte data[] = new byte[2048];

                    while ((count = origin.read(data)) != -1)
                    {
                        out.write(data, 0, count);
                    }

                    out.flush();
                    origin.close();
                }
                catch (IOException e)
                {
                    System.err.println("Failed to add tar entry due to: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }

    public static void untar(Path outputDirectory, Path inputTarFile) throws IOException
    {
        try (FileInputStream fileInputStream = new FileInputStream(inputTarFile.toFile()))
        {
            TarInputStream tarInputStream = new TarInputStream(fileInputStream);

            TarEntry entry;

            while ((entry = tarInputStream.getNextEntry()) != null)
            {
                int count;
                byte data[] = new byte[2048];
                File outputFile = new File(outputDirectory + "/" + entry.getName());
                if (!outputFile.getParentFile().isDirectory())
                    outputFile.getParentFile().mkdirs();

                FileOutputStream fos = new FileOutputStream(outputFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos);

                while ((count = tarInputStream.read(data)) != -1)
                {
                    dest.write(data, 0, count);
                }

                dest.flush();
                dest.close();
            }
        }
    }
}
