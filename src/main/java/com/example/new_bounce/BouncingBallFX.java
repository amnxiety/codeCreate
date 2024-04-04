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
    private Circle border;
    private Circle ball;
    private Text collisionText;

    private Random random = new Random();
    private List<Media> mp3MediaList = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        root.setStyle("-fx-background-color: black;");
        loadMp3Files();

        border = new Circle(WIDTH / 2.0, HEIGHT / 2.0, BORDER_RADIUS);
        border.setFill(null);
        border.setStrokeWidth(15);
        border.setStroke(Color.RED);

        // Initialize ball's initial direction randomly with changeable initial velocity
        ballDX = random.nextDouble() * (INITIAL_VELOCITY_MAX - INITIAL_VELOCITY_MIN) + INITIAL_VELOCITY_MIN;
        ballDY = random.nextDouble() * (INITIAL_VELOCITY_MAX - INITIAL_VELOCITY_MIN) + INITIAL_VELOCITY_MIN;

        ball = new Circle(ballX, ballY, BALL_RADIUS);
        ball.setFill(vibrantColors[0]);

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
