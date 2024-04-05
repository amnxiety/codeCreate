package com.example.new_bounce;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BouncingBallFX extends Application {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private static final int BORDER_RADIUS = 250;
    private static double BALL_RADIUS = 20;
    private static double GRAVITY = 0.2;
    private static double SPEED_INCREMENT = 0.05;
    private static double SIZE_INCREMENT = 1.0;
    private static double INITIAL_VELOCITY_MIN = 1.0; // Changeable initial velocity range
    private static double INITIAL_VELOCITY_MAX = 1.0; // Changeable initial velocity range

    private double ballX = 50; // initial position of the ball
    private double ballY = 50; // initial position of the ball
    private double ballDX; // initial speed in the x direction
    private double ballDY; // initial speed in the y direction
    private int collisionCount = 0;

    private final Color[] vibrantColors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.ORANGE, Color.PURPLE};
    private Color currentColor = Color.RED; // Initialize with any color
    private Circle border;
    private Circle ball;
    private List<Circle> tail = new ArrayList<>(); // List to hold the circles forming the tail
    private Text collisionText;

    private Random random = new Random();
    private Color tailColor = Color.GRAY; // Default tail color
    private List<Media> mp3MediaList = new ArrayList<>();
    private Pane root;

    private Color targetBallColor = Color.RED; // Initial target ball color
    private Color targetBorderColor = Color.RED; // Initial target border color

    @Override
    public void start(Stage primaryStage) {
        root = new Pane();
        root.setStyle("-fx-background-color: black;");
        loadMp3Files();

        border = new Circle(WIDTH / 2.0, HEIGHT / 2.0, BORDER_RADIUS);
        border.setFill(null);
        border.setStrokeWidth(15);
        border.setStroke(currentColor); // Set the color of the border to currentColor

        // Initialize ball's initial direction randomly with changeable initial velocity
        ballDX = random.nextDouble() * (INITIAL_VELOCITY_MAX - INITIAL_VELOCITY_MIN) + INITIAL_VELOCITY_MIN;
        ballDY = random.nextDouble() * (INITIAL_VELOCITY_MAX - INITIAL_VELOCITY_MIN) + INITIAL_VELOCITY_MIN;

        ball = new Circle(ballX, ballY, BALL_RADIUS);
        ball.setFill(currentColor); // Set the color of the ball to currentColor

        collisionText = new Text("Collisions: " + collisionCount);
        collisionText.setFont(Font.font("Arial", 30));
        collisionText.setFill(Color.WHITE);
        collisionText.setTranslateX(220);
        collisionText.setTranslateY(30);

        root.getChildren().addAll(border, ball, collisionText);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Bouncing Ball (JavaFX)");
        primaryStage.setResizable(false);
        primaryStage.show();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateBallPosition();
                updateTail();
            }
        };
        timer.start();
    }

    private void updateBallPosition() {
        // Apply gravity
        ballDY += GRAVITY;

        // Update ball position
        ballX += ballDX;
        ballY += ballDY;

        // Gradually transition ball color towards the target ball color
        Color currentBallColor = (Color) ball.getFill();
        ball.setFill(currentBallColor.interpolate(targetBallColor, 0.005)); // Adjust the interpolation factor as needed

        // Gradually transition border color towards the target border color
        Color currentBorderColor = (Color) border.getStroke();
        border.setStroke(currentBorderColor.interpolate(targetBorderColor, 0.005)); // Adjust the interpolation factor as needed

        // Check for collisions with border
        double dx = ballX - border.getCenterX();
        double dy = ballY - border.getCenterY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance + BALL_RADIUS >= BORDER_RADIUS) {
            // Calculate angle to the center of the outer circle
            double angleToCenter = Math.atan2(dy, dx);

            // Calculate reflection angle
            double incidenceAngle = Math.atan2(ballDY, ballDX);
            double reflectionAngle = 2 * angleToCenter - incidenceAngle + Math.PI;

            // Update ball direction using reflection formula
            double speed = Math.sqrt(ballDX * ballDX + ballDY * ballDY);
            ballDX = Math.cos(reflectionAngle) * (speed + SPEED_INCREMENT);
            ballDY = Math.sin(reflectionAngle) * (speed + SPEED_INCREMENT);

            // Increase the size of the ball
            BALL_RADIUS += SIZE_INCREMENT;

            // Update collision count
            collisionCount++;
            collisionText.setText("Collisions: " + collisionCount);

            // Play a random MP3 file from the loaded list
            if (!mp3MediaList.isEmpty()) {
                Media randomMedia = mp3MediaList.get(random.nextInt(mp3MediaList.size()));
                MediaPlayer mediaPlayer = new MediaPlayer(randomMedia);
                mediaPlayer.play();
            }

            // Change the target colors
            targetBallColor = vibrantColors[(collisionCount + 1) % vibrantColors.length];
            targetBorderColor = vibrantColors[(collisionCount + 1) % vibrantColors.length];
        }

        // Ensure the ball stays inside the boundary
        if (distance + BALL_RADIUS > BORDER_RADIUS) {
            double normX = dx / distance;
            double normY = dy / distance;
            ballX = border.getCenterX() + normX * (BORDER_RADIUS - BALL_RADIUS);
            ballY = border.getCenterY() + normY * (BORDER_RADIUS - BALL_RADIUS);
        }

        ball.setCenterX(ballX);
        ball.setCenterY(ballY);
        ball.setRadius(BALL_RADIUS);
    }

    private void updateTail() {
        // Create a new circle for the tail piece
        Circle newTailPiece = new Circle(ball.getCenterX(), ball.getCenterY(), BALL_RADIUS * 0.9); // Adjust the size of the tail pieces as needed
        newTailPiece.setFill(tailColor); // Set the color of the tail piece

        // Limit the number of tail pieces
        if (tail.size() > 20) { // Adjust the number of tail pieces as needed
            root.getChildren().remove(tail.remove(0)); // Remove the oldest tail piece from the root pane and the tail list
        }

        // Set transparency based on position in the tail
        double transparency = 1.0;
        for (int i = tail.size(); i > 0; i--) {
            tail.get(i - 1).setOpacity(transparency);
            transparency *= 0.8; // Adjust the rate of transparency decrease as needed
        }

        // Add the new tail piece to the root pane and the tail list
        root.getChildren().add(0, newTailPiece); // Add the tail piece before the ball
        tail.add(newTailPiece);
    }

    private void loadMp3Files() {
        File keyNotesFolder = new File("src/main/java/com/example/new_bounce/keyNotes");
        File[] mp3Files = keyNotesFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
        if (mp3Files != null) {
            for (File mp3File : mp3Files) {
                Media media = new Media(mp3File.toURI().toString());
                mp3MediaList.add(media);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
