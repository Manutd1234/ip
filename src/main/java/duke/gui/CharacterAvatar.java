package duke.gui;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/** Displays the user-supplied character artwork, decoding each shared image only once. */
final class CharacterAvatar extends StackPane {
    // Reused artwork: https://in.pinterest.com/pin/890164682599959089/ (see docs/CREDITS.md).
    private static final Image CHARIZARD = loadImage("charizard.jpg");

    // Reused artwork: https://ru.pinterest.com/pin/729442470942034338/ (see docs/CREDITS.md).
    private static final Image ASH = loadImage("ash.jpeg");

    /** Creates a framed character portrait while preserving the supplied picture's proportions. */
    CharacterAvatar(boolean isUser) {
        double width = 72;
        double height = 80;
        setMinSize(width, height);
        setPrefSize(width, height);
        setMaxSize(width, height);
        getStyleClass().addAll("character-avatar", isUser ? "ash-avatar" : "charizard-avatar");
        ImageView portrait = new ImageView(isUser ? ASH : CHARIZARD);
        portrait.setFitWidth(width - 10);
        portrait.setFitHeight(height - 10);
        portrait.setPreserveRatio(true);
        portrait.setSmooth(true);
        setAccessibleText(isUser ? "Ash avatar" : "Charizard avatar");
        getChildren().add(portrait);
    }

    /** Loads a suitably sized version for crisp high-density displays without repeated full-size decoding. */
    private static Image loadImage(String filename) {
        return new Image(CharacterAvatar.class.getResource("assets/" + filename).toExternalForm(),
                256, 256, true, true);
    }
}
