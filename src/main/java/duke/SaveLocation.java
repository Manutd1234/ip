package duke;

import java.net.URISyntaxException;
import java.nio.file.Path;

/**
 * Keeps downloaded users' task files beside the JAR, independent of the launch folder.
 */
public final class SaveLocation {
    private SaveLocation() {
    }

    /**
     * Returns the normal save path for the packaged app or a source-code run.
     *
     * @return Text file under the JAR's folder, or under the working folder when running from source.
     */
    public static Path getDefaultFile() {
        try {
            Path codeLocation = Path.of(SaveLocation.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return resolve(codeLocation, Path.of("").toAbsolutePath());
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("The application location is not a valid file path.", exception);
        }
    }

    /** Separates path selection from the running JVM so both launch modes can be tested. */
    static Path resolve(Path codeLocation, Path workingDirectory) {
        Path appFolder = codeLocation.getFileName().toString().endsWith(".jar")
                ? codeLocation.toAbsolutePath().getParent() : workingDirectory;
        return appFolder.resolve("data").resolve("wangsa.txt");
    }
}
