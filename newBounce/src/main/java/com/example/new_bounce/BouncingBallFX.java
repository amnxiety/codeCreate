package com.example.new_bounce;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class BouncingBallFX extends Application {

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 1200;
    private static final double SPAWNED_BORDER_RADIUS = 450;

    private static double BORDER_RADIUS = 450;
    private static final double BALL_RADIUS = 25;
    private static final double GRAVITY = 0.2;
    private static final double SPEED_INCREMENT = 0.08;
    private static final double BORDER_SHRINK_RATE = 0.55;
    private static final double REFLECTION_RANDOMNESS = 0.5;

    private double ballX = 50;
    private double ballY = 150;
    private double ballDX;
    private double ballDY;
    private int collisionCount = 0;

    private final Color[] vibrantColors = {Color.RED, Color.GREEN, Color.BLUE, Color.VIOLET};
    private Color targetBallColor = Color.RED;
    private Color targetBorderColor = Color.RED;

    private Circle border;
    private Circle ball;
    private List<Circle> spawnedBorders = new ArrayList<>();
    private List<Circle> tail = new ArrayList<>();
    private Text collisionText;
    private Color currentColor = Color.RED;

    private Random random = new Random();
    private Color tailColor = Color.GRAY;
    private Pane root;

    private long[] noteDurations;
    private ArrayList<Integer> notes;
    private int sizeNotes;

    private Synthesizer synth;

    @Override
    public void start(Stage primaryStage) {
        initializeStage(primaryStage);
        initializeMidi();
        initializeUI();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateBallPosition();
                updateTail();
                checkCollisionWithBorder();
                shrinkBorders();
            }
        };

        initializeButton(timer);
        startBorderSpawning();
    }

    private void initializeStage(Stage primaryStage) {
        root = new Pane();
        root.setStyle("-fx-background-color: black;");
        primaryStage.setOnCloseRequest(event -> closeSynthesizer());
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Bouncing Ball (JavaFX)");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void initializeMidi() {
        try {
            synth = MidiSystem.getSynthesizer();
            synth.open();
            loadMidiFile();
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void initializeUI() {
        border = createCircle(WIDTH / 2.0, HEIGHT / 2.0, BORDER_RADIUS, null, currentColor);
        ball = createCircle(ballX, ballY, BALL_RADIUS, currentColor, null);

        collisionText = new Text("Collisions: " + collisionCount);
        collisionText.setFont(Font.font("Arial", 50));
        collisionText.setFill(Color.WHITE);
        collisionText.setTranslateX(370);
        collisionText.setTranslateY(80);

        root.getChildren().addAll(border, ball, collisionText);
    }

    private Circle createCircle(double x, double y, double radius, Color fill, Color stroke) {
        Circle circle = new Circle(x, y, radius);
        circle.setFill(fill);
        circle.setStrokeWidth(15);
        circle.setStroke(stroke);
        return circle;
    }

    private void initializeButton(AnimationTimer timer) {
        Button startButton = new Button("Start");
        startButton.setTranslateX(450);
        startButton.setTranslateY(1000);
        startButton.setOnAction(event -> startGame(timer));
        root.getChildren().add(startButton);
    }

    private void startGame(AnimationTimer timer) {
        ((Button) root.getChildren().get(root.getChildren().size() - 1)).setVisible(false);
        Timeline gameStartTimeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> timer.start()));
        gameStartTimeline.play();
    }

    private void startBorderSpawning() {
        Timeline spawnBorderTimeline = new Timeline(
                new KeyFrame(Duration.seconds(3), e -> spawnNewBorder())
        );
        spawnBorderTimeline.setCycleCount(Timeline.INDEFINITE);
        spawnBorderTimeline.play();
    }

    private void spawnNewBorder() {
        Circle newBorder = createCircle(WIDTH / 2.0, HEIGHT / 2.0, SPAWNED_BORDER_RADIUS, null, currentColor);
        spawnedBorders.add(newBorder);
        root.getChildren().add(newBorder);
    }

    private void checkCollisionWithBorder() {
        Iterator<Circle> iterator = spawnedBorders.iterator();
        while (iterator.hasNext()) {
            Circle borderCircle = iterator.next();
            double dx = ballX - borderCircle.getCenterX();
            double dy = ballY - borderCircle.getCenterY();
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance + BALL_RADIUS >= borderCircle.getRadius()) {
                handleCollision(borderCircle);
                iterator.remove();
                root.getChildren().remove(borderCircle);
            }
        }
    }

    private void handleCollision(Circle borderCircle) {
        playNotesWhenAsked(collisionCount);

        double dx = ballX - borderCircle.getCenterX();
        double dy = ballY - borderCircle.getCenterY();
        double angleToCenter = Math.atan2(dy, dx);
        double incidenceAngle = Math.atan2(ballDY, ballDX);
        double reflectionAngle = 2 * angleToCenter - incidenceAngle + Math.PI;
        reflectionAngle += (random.nextDouble() - 0.5) * REFLECTION_RANDOMNESS;

        double speed = Math.sqrt(ballDX * ballDX + ballDY * ballDY);
        ballDX = Math.cos(reflectionAngle) * (speed + SPEED_INCREMENT);
        ballDY = Math.sin(reflectionAngle) * (speed + SPEED_INCREMENT);

        collisionCount++;
        collisionText.setText("Collisions: " + collisionCount);

        targetBallColor = vibrantColors[(collisionCount + 1) % vibrantColors.length];
        targetBorderColor = vibrantColors[(collisionCount + 1) % vibrantColors.length];
    }

    private void updateBallPosition() {
        ballDY += GRAVITY;
        ballX += ballDX;
        ballY += ballDY;

        Color currentBallColor = (Color) ball.getFill();
        ball.setFill(currentBallColor.interpolate(targetBallColor, 0.005));

        Color currentBorderColor = (Color) border.getStroke();
        border.setStroke(currentBorderColor.interpolate(targetBorderColor, 0.005));

        ball.setCenterX(ballX);
        ball.setCenterY(ballY);
    }

    private void updateTail() {
        Circle newTailPiece = createCircle(ballX, ballY, BALL_RADIUS * 0.9, tailColor, null);
        if (tail.size() > 20) {
            root.getChildren().remove(tail.remove(0));
        }

        double transparency = 1.0;
        for (int i = tail.size() - 1; i >= 0; i--) {
            Circle tailPiece = tail.get(i);
            tailPiece.setOpacity(transparency);
            transparency *= 0.8;
        }

        root.getChildren().add(0, newTailPiece);
        tail.add(newTailPiece);
    }

    private void shrinkBorders() {
        BORDER_RADIUS -= BORDER_SHRINK_RATE;
        border.setRadius(BORDER_RADIUS);

        Iterator<Circle> iterator = spawnedBorders.iterator();
        while (iterator.hasNext()) {
            Circle circle = iterator.next();
            double currentRadius = circle.getRadius();
            if (currentRadius > BALL_RADIUS) {
                circle.setRadius(currentRadius - BORDER_SHRINK_RATE);
            } else {
                root.getChildren().remove(circle);
                iterator.remove();
            }
        }
    }

    private void loadMidiFile() {
        try {
            File midiFile = new File("src/main/java/com/example/new_bounce/midi/fur.mid");
            Sequence sequence = MidiSystem.getSequence(midiFile);
            notes = extractNotesFromMidi(sequence);
            noteDurations = calculateNoteDurations(sequence);
        } catch (InvalidMidiDataException | IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Integer> extractNotesFromMidi(Sequence sequence) {
        ArrayList<Integer> notes = new ArrayList<>();
        // ... (implementation to extract notes)
        return notes;
    }

    private long[] calculateNoteDurations(Sequence sequence) {
        long[] durations = new long[128];
        // ... (implementation to calculate note durations)
        return durations;
    }

    private void closeSynthesizer() {
        if (synth != null && synth.isOpen()) {
            synth.close();
        }
    }

    private void playNotesWhenAsked(int noteNumber) {
        playNoteWithDelay(notes.get(noteNumber % sizeNotes), getNoteDuration(notes.get(noteNumber % sizeNotes)));
    }

    private void playNoteWithDelay(int note, long duration) {
        try {
            if (synth != null && synth.isOpen()) {
                MidiChannel channel = synth.getChannels()[0];
                channel.noteOn(note, 0);
                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(duration), event -> channel.noteOff(note)));
                timeline.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long getNoteDuration(int noteNumber) {
        if (noteNumber < 0 || noteNumber >= noteDurations.length) {
            System.err.println("Invalid note number!");
            return -1;
        }
        return noteDurations[noteNumber];
    }

    public static void main(String[] args) {
        launch(args);
    }
}
