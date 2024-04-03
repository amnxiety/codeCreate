package com.example.new_bounce;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BouncingBallFX extends Application {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private static final int BORDER_THICKNESS = 15;
    private static final int BORDER_SIZE = 100;
    private static double BALL_SIZE = 20; // Initial size of the ball
    private static final double BALL_VELOCITY = 1.2;
    private static final double SIZE_INCREMENT = 0.31; // Size increment of the ball
    private static final double SPEED_INCREMENT = 0.05; // Speed increment of the ball
    private static final Duration FADE_DURATION = Duration.seconds(1); // Duration for background fade

    private double ballX = BORDER_SIZE + BALL_SIZE / 2;
    private double ballY = BORDER_SIZE + BALL_SIZE / 2;
    private double ballVelX = BALL_VELOCITY;
    private double ballVelY = BALL_VELOCITY + 1;

    private final Color[] vibrantColors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.ORANGE, Color.PURPLE};
    private final List<String> wavFilePaths = new ArrayList<>();
    private final Random random = new Random();
    private int collisionCount = 0;

    private Rectangle background; // Declare background as a class member variable
    private Timeline fadeTimeline; // Timeline for fading the background color back to black
    private Text collisionText; // Text node to display collision count

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        root.setStyle("-fx-background-color: black;");

        // Create a rectangle to represent the border of the box
        Rectangle border = new Rectangle(BORDER_SIZE, BORDER_SIZE, WIDTH - 2 * BORDER_SIZE, HEIGHT - 2 * BORDER_SIZE);
        border.setFill(null);
        border.setStrokeWidth(BORDER_THICKNESS);
        border.setStroke(Color.RED);

        // Create a rectangle to cover the area outside the box
        background = new Rectangle(0, 0, WIDTH, HEIGHT); // Initialize background
        background.setFill(Color.BLACK);
        root.getChildren().addAll(background, border); // Add both rectangles to the root pane

        Rectangle ball = new Rectangle(ballX, ballY, BALL_SIZE, BALL_SIZE);
        ball.setFill(vibrantColors[0]);

        root.getChildren().addAll(ball);

        // Create text node to display collision count
        collisionText = new Text("Collisions: 0");
        collisionText.setFont(Font.font("Arial", 20));
        collisionText.setFill(Color.WHITE);
        collisionText.setTranslateX(10);
        collisionText.setTranslateY(30);
        root.getChildren().add(collisionText);

        loadWavFiles(); // Load WAV files

        // Timeline to change border color
        Timeline borderTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(border.strokeProperty(), Color.RED)),
                new KeyFrame(Duration.seconds(1), new KeyValue(border.strokeProperty(), Color.BLUE))
        );
        borderTimeline.setCycleCount(Timeline.INDEFINITE);
        borderTimeline.setAutoReverse(true);
        borderTimeline.play();

        // Timeline to change ball color
        KeyValue[] colorKeyValues = new KeyValue[vibrantColors.length];
        for (int i = 0; i < vibrantColors.length; i++) {
            colorKeyValues[i] = new KeyValue(ball.fillProperty(), vibrantColors[i]);
        }

        // Create key frames for smooth color transition
        KeyFrame[] colorKeyFrames = new KeyFrame[vibrantColors.length];
        for (int i = 0; i < vibrantColors.length; i++) {
            colorKeyFrames[i] = new KeyFrame(Duration.seconds(i), colorKeyValues[i]);
        }

        // Timeline to change ball color
        Timeline ballColorTimeline = new Timeline(colorKeyFrames);
        ballColorTimeline.setCycleCount(Timeline.INDEFINITE);
        ballColorTimeline.setAutoReverse(true);
        ballColorTimeline.play();

        // Animation for moving the ball
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                moveBall(ball);
            }
        }.start();

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Bouncing Ball (JavaFX)");
        primaryStage.setResizable(false);
        primaryStage.show();

        // Initialize and start fade timeline
        fadeTimeline = new Timeline();
        fadeTimeline.setCycleCount(1);
        fadeTimeline.setAutoReverse(true);
    }

    // Load WAV files
    private void loadWavFiles() {
        File folder = new File("src/main/java/com/example/new_bounce/keyNotes");
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                wavFilePaths.add(file.getPath());
            }
        }
    }

    // Play a random WAV file
    private void playRandomWav() {
        if (!wavFilePaths.isEmpty()) {
            try {
                File randomWavFile = new File(wavFilePaths.get(random.nextInt(wavFilePaths.size())));
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(randomWavFile);

                // Get the audio format of the file
                AudioFormat format = audioInputStream.getFormat();
                if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED || format.getSampleSizeInBits() != 16) {
                    // Convert the audio format to a supported one (PCM_SIGNED, 16-bit)
                    AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                            format.getSampleRate(), 16, format.getChannels(),
                            format.getChannels() * 2, format.getSampleRate(), false);
                    audioInputStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
                }

                // Open and play the converted audio stream
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
            } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void moveBall(Rectangle ball) {
        double fullThickness = BORDER_THICKNESS - 5;

        ballX += ballVelX;
        ballY += ballVelY;

        // Check collision with walls
        if (ballX < BORDER_SIZE + fullThickness) {
            ballX = BORDER_SIZE + fullThickness;
            ballVelX *= -1.0 - SPEED_INCREMENT;
            BALL_SIZE += SIZE_INCREMENT; // Increase ball size
            playRandomWav();
            background.setFill(getRandomColorWithTransparency(0.5)); // Change background color with 50% transparency
            fadeTimeline.getKeyFrames().setAll(
                    new KeyFrame(Duration.ZERO, new KeyValue(background.fillProperty(), Color.BLACK)),
                    new KeyFrame(FADE_DURATION, new KeyValue(background.fillProperty(), getRandomColorWithTransparency(0.5)))
            );
            fadeTimeline.playFromStart(); // Start the fade timeline
            collisionCount++;
        } else if (ballX > WIDTH - BORDER_SIZE - BALL_SIZE - fullThickness) {
            ballX = WIDTH - BORDER_SIZE - BALL_SIZE - fullThickness;
            ballVelX *= -1.0 - SPEED_INCREMENT;
            BALL_SIZE += SIZE_INCREMENT; // Increase ball size
            playRandomWav();
            background.setFill(getRandomColorWithTransparency(0.5)); // Change background color with 50% transparency
            fadeTimeline.getKeyFrames().setAll(
                    new KeyFrame(Duration.ZERO, new KeyValue(background.fillProperty(), Color.BLACK)),
                    new KeyFrame(FADE_DURATION, new KeyValue(background.fillProperty(), getRandomColorWithTransparency(0.5)))
            );
            fadeTimeline.playFromStart(); // Start the fade timeline
            collisionCount++;
        }

        if (ballY < BORDER_SIZE + fullThickness) {
            ballY = BORDER_SIZE + fullThickness;
            ballVelY *= -1.0 - SPEED_INCREMENT;
            BALL_SIZE += SIZE_INCREMENT; // Increase ball size
            playRandomWav();
            background.setFill(getRandomColorWithTransparency(0.5)); // Change background color with 50% transparency
            fadeTimeline.getKeyFrames().setAll(
                    new KeyFrame(Duration.ZERO, new KeyValue(background.fillProperty(), Color.BLACK)),
                    new KeyFrame(FADE_DURATION, new KeyValue(background.fillProperty(), getRandomColorWithTransparency(0.5)))
            );
            fadeTimeline.playFromStart(); // Start the fade timeline
            collisionCount++;
        } else if (ballY > HEIGHT - BORDER_SIZE - BALL_SIZE - fullThickness) {
            ballY = HEIGHT - BORDER_SIZE - BALL_SIZE - fullThickness;
            ballVelY *= -1.0 - SPEED_INCREMENT;
            BALL_SIZE += SIZE_INCREMENT; // Increase ball size
            playRandomWav();
            background.setFill(getRandomColorWithTransparency(0.5)); // Change background color with 50% transparency
            fadeTimeline.getKeyFrames().setAll(
                    new KeyFrame(Duration.ZERO, new KeyValue(background.fillProperty(), Color.BLACK)),
                    new KeyFrame(FADE_DURATION, new KeyValue(background.fillProperty(), getRandomColorWithTransparency(0.5)))
            );
            fadeTimeline.playFromStart(); // Start the fade timeline
            collisionCount++;
        }

        // Adjust ball size to stay within wall area
        if (BALL_SIZE > WIDTH - 2 * (BORDER_SIZE + fullThickness)) {
            BALL_SIZE = (int) Math.min(WIDTH - 2 * (BORDER_SIZE + fullThickness), HEIGHT - 2 * (BORDER_SIZE + fullThickness));
        }

        // Update ball properties
        ball.setX(ballX);
        ball.setY(ballY);
        ball.setWidth(BALL_SIZE);
        ball.setHeight(BALL_SIZE);

        // Update collision count text
        collisionText.setText("Collisions: " + collisionCount);
    }

    // Generate a random color with transparency
    private Color getRandomColorWithTransparency(double transparency) {
        Color randomColor = vibrantColors[random.nextInt(vibrantColors.length)];
        return new Color(randomColor.getRed(), randomColor.getGreen(), randomColor.getBlue(), transparency);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
