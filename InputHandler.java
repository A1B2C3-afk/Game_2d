package controllers;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import java.util.HashSet;
import java.util.Set;

public class InputHandler {

    // We use a Set because a key is either pressed or not (no duplicates)
    private Set<KeyCode> activeKeys = new HashSet<>();

    public InputHandler(Scene scene) {
        // 1. When a key is pressed, add it to the list
        scene.setOnKeyPressed(event -> {
            activeKeys.add(event.getCode());
        });

        // 2. When a key is released, remove it from the list
        scene.setOnKeyReleased(event -> {
            activeKeys.remove(event.getCode());
        });
    }

    // The GameLoop will call this 60 times a second to ask: "Is W pressed?"
    public boolean isKeyPressed(KeyCode key) {
        return activeKeys.contains(key);
    }
    
    // Optional: Clear keys (useful when game ends or pauses)
    public void clear() {
        activeKeys.clear();
    }
}