package duke.gui;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Presents one conversation entry, keeping message styling separate from command execution. */
final class ChatMessage extends HBox {
    /** Identifies the speaker and whether an assistant reply needs error emphasis. */
    enum Role {
        USER, ASSISTANT, ERROR
    }

    /** Builds an accessible, wrapping message bubble with an original compact avatar. */
    ChatMessage(String message, Role role) {
        super(10);
        boolean isUser = role == Role.USER;
        setAlignment(isUser ? Pos.TOP_RIGHT : Pos.TOP_LEFT);
        setMinWidth(0);
        setMaxWidth(Double.MAX_VALUE);
        getStyleClass().add("message-row");

        VBox bubble = new VBox(6);
        bubble.setMinWidth(0);
        bubble.maxWidthProperty().bind(Bindings.max(0,
                Bindings.min(isUser ? 560 : 720, widthProperty().subtract(94))));
        bubble.getStyleClass().add(isUser ? "user-bubble" : "assistant-bubble");
        if (role == Role.ERROR) {
            bubble.getStyleClass().add("error-bubble");
        }
        Label speaker = new Label(isUser ? "TRAINER" : "WANGSA");
        speaker.getStyleClass().add("message-speaker");
        Label body = new Label(message);
        body.setWrapText(true);
        body.setMinWidth(0);
        body.setMaxWidth(Double.MAX_VALUE);
        body.getStyleClass().add("message-body");
        bubble.getChildren().addAll(speaker, body);
        if (role == Role.ERROR) {
            Label metadata = new Label("ACTION NEEDED");
            metadata.getStyleClass().add("message-metadata");
            bubble.getChildren().add(metadata);
        }

        CharacterAvatar avatar = new CharacterAvatar(isUser);
        if (isUser) {
            getChildren().addAll(bubble, avatar);
        } else {
            getChildren().addAll(avatar, bubble);
        }
    }
}
