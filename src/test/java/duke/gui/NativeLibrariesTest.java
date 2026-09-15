package duke.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.Locale;

import org.junit.jupiter.api.Test;

/**
 * Checks platform selection without loading libraries for another computer.
 */
class NativeLibrariesTest {
    @Test
    void selectPlatform_supportedComputers_selectsMatchingLibraries() throws IOException {
        assertEquals("win", NativeLibraries.selectPlatform("Windows 11", "amd64"));
        assertEquals("linux", NativeLibraries.selectPlatform("Linux", "x86_64"));
        assertEquals("mac", NativeLibraries.selectPlatform("Mac OS X", "x86_64"));
        assertEquals("mac-aarch64", NativeLibraries.selectPlatform("Mac OS X", "aarch64"));
        assertEquals("mac-aarch64", NativeLibraries.selectPlatform("Mac OS X", "arm64"));
    }

    @Test
    void selectPlatform_unsupportedComputers_rejectsWrongArchitecture() {
        assertThrows(IOException.class, () -> NativeLibraries.selectPlatform("Windows 11", "aarch64"));
        assertThrows(IOException.class, () -> NativeLibraries.selectPlatform("Linux", "aarch64"));
        assertThrows(IOException.class, () -> NativeLibraries.selectPlatform("Windows 10", "x86"));
        assertThrows(IOException.class, () -> NativeLibraries.selectPlatform("Mac OS X", "ppc"));
        assertThrows(IOException.class, () -> NativeLibraries.selectPlatform("Darwin", "x86_64"));
    }

    @Test
    void selectPlatform_turkishLocale_doesNotChangePlatformNames() throws IOException {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals("win", NativeLibraries.selectPlatform("WINDOWS 11", "AMD64"));
            assertEquals("linux", NativeLibraries.selectPlatform("LINUX", "AMD64"));
        } finally {
            Locale.setDefault(original);
        }
    }
}
