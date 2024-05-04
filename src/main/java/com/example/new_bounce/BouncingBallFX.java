package com.example.new_bounce;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class BouncingBallFX extends Application {

    private static final int WIDTH = 1500;
    private static final int HEIGHT = 1600;
    private final Random random = new Random();
    private final int collisionCount = 0;
    private final StaticConstants staticConstants = new StaticConstants();
    private final Color targetBorderColor = Color.RED;
    private Ball ball;
    private Border border;
    private Pane root;
    private Text collisionText;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        root = new Pane();
        root.setStyle("-fx-background-color: black;");

        setCollisionCounter();

        border = new Border(WIDTH / 2.0, HEIGHT / 2.0, 650, Color.MEDIUMVIOLETRED); // Initial border color
        ball = new Ball(WIDTH/2.5 , HEIGHT / 2.5, 2, -2, 10, staticConstants.rgbColors.getFirst(), collisionText, root.getChildren());

        Button startButton = new Button("Start");
        startButton.setTranslateX(450);
        startButton.setTranslateY(450);

        root.getChildren().addAll(border.getCircle(), collisionText, startButton);

        handleStartButton(startButton);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Bouncing Ball (JavaFX)");
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    private void handleStartButton(Button startButton) {

        startButton.setOnAction(event -> {
            startButton.setVisible(false);

            // Create a PauseTransition for 3 seconds
            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(e -> {


                // This code will be executed after the delay
                final int[] colorNumber = {1};
                AnimationTimer timer = new AnimationTimer() {
                    @Override
                    public void handle(long now) {
                        if (ball.radius + 30 < 600) {
                            ball.updatePosition(1.10, border.getCircle());
                            ball.updateTail(root.getChildren());
                            if (border.getCircle().getRadius() <= ball.radius + 30) {
                                root.getChildren().remove(border.getCircle());
//                                ball.addLayerToCircle(border.currentColor);
                                colorNumber[0] += 1;
                                border = new Border(WIDTH / 2.0, HEIGHT / 2.0, 600, staticConstants.rgbColors.get(colorNumber[0] % staticConstants.rgbColors.size()));
                                root.getChildren().addAll(border.getCircle());
                            }
                        }
                    }
                };
                timer.start();
            });

            // Start the delay
            delay.play();
        });
    }

    private void setCollisionCounter() {
        collisionText = new Text("Collisions: " + collisionCount);
        collisionText.setFont(Font.font("Arial", 50));
        collisionText.setFill(Color.WHITE);
        collisionText.setTranslateX(610);
        collisionText.setTranslateY(95);
    }
}

//So in my code, I have a ball and border, everytime ball colides with the border I am reducing the border size, eventually when ball size is closely equals to the border radius
// I increase radius of the ball by the border width, and create a new border with original size. Now as I want it to loom like my ball got the layer of the border like it stick on the
// ball, how can I do that?

