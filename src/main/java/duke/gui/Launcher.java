package duke.gui;

import javafx.application.Application;

/** Launches the JavaFX application without relying on the Application subclass as the entry point. */
public final class Launcher {
    private Launcher() {
    }

    /** Starts Wangsa's JavaFX application.
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }
}
