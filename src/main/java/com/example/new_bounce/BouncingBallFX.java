package com.example.new_bounce;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.media.Media;
import java.io.File;
import javafx.scene.media.MediaPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BouncingBallFX extends Application {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private static final int BORDER_RADIUS = 250;
    private static double BALL_RADIUS = 20;
    private double ballX = 50; // initial position of the ball
    private double ballY = 50; // initial position of the ball
    private double ballDX; // initial speed in the x direction
    private double ballDY; // initial speed in the y direction
    private int collisionCount = 0;
    private int colorIndex = 0;

    private final Color[] vibrantColors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.ORANGE, Color.PURPLE};
    private Circle border;
    private Circle ball;
    private Text collisionText;

    // Define the range for initial velocity
    private static final double INITIAL_VELOCITY_MIN = 5.0;
    private static final double INITIAL_VELOCITY_MAX = 5.0;
    private static final double GRAVITY = 0.5; // Adjust gravity as needed
    // Define the range for randomness in reflection angle
    private static final double REFLECTION_RANDOMNESS = 0.5; // Adjust as needed
    private Random random = new Random();
    private List<Media> mp3MediaList = new ArrayList<>();
    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        root.setStyle("-fx-background-color: black;");
        loadMp3Files();
        // Create a circle to represent the border of the box
        border = new Circle(WIDTH / 2.0, HEIGHT / 2.0, BORDER_RADIUS);
        border.setFill(null);
        border.setStrokeWidth(15);
        border.setStroke(Color.RED);

        // Generate random initial position for the ball within the outer circle
        Random random = new Random();
        double randomAngle = Math.toRadians(random.nextDouble() * 360); // Random angle in radians
        double randomRadius = BORDER_RADIUS * Math.sqrt(random.nextDouble()); // Random radius within the outer circle
        ballX = border.getCenterX() + randomRadius * Math.cos(randomAngle);
        ballY = border.getCenterY() + randomRadius * Math.sin(randomAngle);

        // Create a circle to represent the ball
        ball = new Circle(ballX, ballY, BALL_RADIUS);
        ball.setFill(vibrantColors[0]);

        // Create text node to display collision count
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

        // Initialize ball's initial direction randomly
        ballDX = INITIAL_VELOCITY_MIN + (INITIAL_VELOCITY_MAX - INITIAL_VELOCITY_MIN) * random.nextDouble();
        ballDY = INITIAL_VELOCITY_MIN + (INITIAL_VELOCITY_MAX - INITIAL_VELOCITY_MIN) * random.nextDouble();

        // Animation Timer to update ball position
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateBallPosition();
            }
        };
        timer.start();

        // Timeline to change ball color smoothly
        Timeline colorTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> changeBallColor()));
        colorTimeline.setCycleCount(Timeline.INDEFINITE);
        colorTimeline.play();
    }
    private boolean isColliding = false;

    private void updateBallPosition() {
        // Apply gravity
        ballDY += GRAVITY;

        // Apply damping to simulate air resistance
        double damping = 0.95; // Adjust damping factor as needed
        ballDX *= damping;
        ballDY *= damping;

        // Update ball position
        ballX += ballDX;
        ballY += ballDY;

        // Check for collisions with border
        double dx = ballX - border.getCenterX();
        double dy = ballY - border.getCenterY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance + BALL_RADIUS >= BORDER_RADIUS) {
            if (!isColliding) { // Check if this is the first frame of collision
                isColliding = true;

                // Calculate angle to the center of the outer circle
                double angleToCenter = Math.atan2(dy, dx);

                // Calculate reflection angle with randomness
                double incidenceAngle = Math.atan2(ballDY, ballDX);
                double reflectionAngle = 2 * angleToCenter - incidenceAngle + Math.PI;
                reflectionAngle += (random.nextDouble() - 0.5) * REFLECTION_RANDOMNESS; // Add randomness

                // Check if the absolute difference between reflection angle and angle to the center is too small
                if (Math.abs(reflectionAngle - angleToCenter) < 0.2) { // Adjust threshold as needed
                    reflectionAngle += Math.PI / 4; // Adjust angle by 45 degrees
                }

                // Update ball direction using reflection formula
                double speed = Math.sqrt(ballDX * ballDX + ballDY * ballDY);
                ballDX = Math.cos(reflectionAngle) * speed;
                ballDY = Math.sin(reflectionAngle) * speed;

                // Increase the size of the ball
                BALL_RADIUS += 1; // Adjust the increment as needed

                // Update collision count
                collisionCount++;
                collisionText.setText("Collisions: " + collisionCount);

                // Inside the updateBallPosition() method, after updating collision count
                // Play a random MP3 file from the loaded list
                if (!mp3MediaList.isEmpty()) {
                    Media randomMedia = mp3MediaList.get(random.nextInt(mp3MediaList.size()));
                    MediaPlayer mediaPlayer = new MediaPlayer(randomMedia);
                    mediaPlayer.play();
                }
            }
        } else {
            isColliding = false; // Reset the collision state if no longer colliding
        }

        // Ensure the ball stays inside the boundary
        if (distance + BALL_RADIUS > BORDER_RADIUS) {
            // Calculate the normalized direction vector from the center of the circle to the current position of the ball
            double normX = dx / distance;
            double normY = dy / distance;

            // Calculate the corrected position for the ball
            ballX = border.getCenterX() + normX * (BORDER_RADIUS - BALL_RADIUS);
            ballY = border.getCenterY() + normY * (BORDER_RADIUS - BALL_RADIUS);
        }

        // Update ball position
        ball.setCenterX(ballX);
        ball.setCenterY(ballY);
        ball.setRadius(BALL_RADIUS); // Set the new radius of the ball
    }


    private void changeBallColor() {
        Color currentColor = (Color) ball.getFill();
        Color nextColor = vibrantColors[(colorIndex + 1) % vibrantColors.length];

        Timeline colorTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(ball.fillProperty(), currentColor)),
                new KeyFrame(Duration.seconds(1), new KeyValue(ball.fillProperty(), nextColor))
        );
        colorTimeline.play();

        colorIndex = (colorIndex + 1) % vibrantColors.length;
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
