package duke;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests stable save paths for downloaded and source-code launches. */
class SaveLocationTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void resolve_jarInFolderWithSpaces_usesJarFolderNotWorkingFolder() {
        Path appFolder = temporaryDirectory.resolve("My tasks 王");
        Path workingFolder = temporaryDirectory.resolve("unrelated");

        assertEquals(appFolder.resolve("data/wangsa.txt"),
                SaveLocation.resolve(appFolder.resolve("Wangsa.jar"), workingFolder));
    }

    @Test
    void resolve_sourceRun_usesProjectWorkingFolder() {
        assertEquals(temporaryDirectory.resolve("data/wangsa.txt"),
                SaveLocation.resolve(temporaryDirectory.resolve("build/classes/java/main"), temporaryDirectory));
    }
}
