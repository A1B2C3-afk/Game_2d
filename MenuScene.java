package view;
import controllers.InputHandler; // Import the Controller
import main.GameLoop;           // Import the Engine
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

// You will also need to import ArenaView here for the transition logic
import view.ArenaView; 

public class MenuScene {

    private Stage stage;
    private final String ASSET_ROOT = "/imagesandstyles/";

    public MenuScene(Stage stage) {
        this.stage = stage;
    }

    public Scene getScene() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double width = screenBounds.getWidth();
        double height = screenBounds.getHeight();

        Image bgImage = new Image(getClass().getResourceAsStream(ASSET_ROOT + "1.png"));
        ImageView bgView = new ImageView(bgImage);
        
        // Ensure image fits the stage size
        bgView.fitWidthProperty().bind(stage.widthProperty());
        bgView.fitHeightProperty().bind(stage.heightProperty());

        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);

        // --- PHASE 1: ENTER BUTTON ---
        Button enterButton = new Button("⚔ ENTER ARENA ⚔");
        
        // --- PHASE 2: SELECTION ELEMENTS (Defined here, added to VBox later) ---
        
        Label p1Label = new Label("Player 1 Character");
        p1Label.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 2, 1, 0, 0);");
        
        ComboBox<String> p1Select = new ComboBox<>();
        p1Select.getItems().addAll("Warrior", "Mage", "Sniper");
        p1Select.getSelectionModel().selectFirst();

        Label p2Label = new Label("Player 2 Character");
        p2Label.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 2, 1, 0, 0);");

        ComboBox<String> p2Select = new ComboBox<>();
        p2Select.getItems().addAll("Warrior", "Mage", "Sniper");
        p2Select.getSelectionModel().selectFirst();

        Button fightButton = new Button("FIGHT!");

        // 4. Overlay & Animation
        Rectangle overlay = new Rectangle(width, height, Color.BLACK);
        overlay.setOpacity(0);
        overlay.setMouseTransparent(true);
        overlay.widthProperty().bind(stage.widthProperty());
        overlay.heightProperty().bind(stage.heightProperty());

        // --- LOGIC 1: ENTER ARENA -> Show Dropdowns ---
        enterButton.setOnAction(e -> {
            overlay.setMouseTransparent(false);
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.8), overlay);
            fadeOut.setFromValue(0);
            fadeOut.setToValue(1);

            fadeOut.setOnFinished(event -> {
                contentBox.getChildren().clear();
                // Add all selection controls now
                contentBox.getChildren().addAll(p1Label, p1Select, p2Label, p2Select, fightButton);
                
                FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.8), overlay);
                fadeIn.setFromValue(1);
                fadeIn.setToValue(0);
                fadeIn.setOnFinished(ev -> overlay.setMouseTransparent(true));
                fadeIn.play();
            });
            fadeOut.play();
        });

        // --- LOGIC 2: FIGHT! -> Start Game Transition ---

     // --- LOGIC: FIGHT BUTTON ---
       fightButton.setOnAction(e -> {
            
            // 1. Fade Out
            FadeTransition fadeToBlack = new FadeTransition(Duration.seconds(1.0), overlay);
            fadeToBlack.setFromValue(0.0);
            fadeToBlack.setToValue(1.0);

            fadeToBlack.setOnFinished(event -> {
                String p1Type = p1Select.getValue();
                String p2Type = p2Select.getValue();

                // --- CRITICAL PART STARTS HERE ---
                
                // 1. Create the Visuals
                ArenaView gameView = new ArenaView(stage, p1Type, p2Type);

                // 2. Create the Input Handler (Listen to keys on the NEW scene)
                // You must import controller.InputHandler for this to work!
                InputHandler input = new InputHandler(gameView.getScene());

                // 3. Create the Engine
                // You must import main.GameLoop for this to work!
                GameLoop gameLoop = new GameLoop(gameView, input);

                // 4. Switch Scene
                stage.setScene(gameView.getScene());

                // 5. START THE GAME LOOP
                gameLoop.start(); 
                
                // 6. Force Focus (So keys work immediately)
                gameView.getScene().getRoot().requestFocus();
                
                // --- CRITICAL PART ENDS HERE ---
            });

            fadeToBlack.play();
        });
        // Add only the initial button
        contentBox.getChildren().add(enterButton);

        StackPane root = new StackPane();
        root.getChildren().addAll(bgView, contentBox, overlay);

        // 5. Create Scene and Link CSS
        Scene scene = new Scene(root, width, height);
        
        // Corrected CSS path: ASSET_ROOT + "uistyle.css"
        scene.getStylesheets().add(getClass().getResource(ASSET_ROOT + "uistyle.css").toExternalForm());
        return scene;
    }
}