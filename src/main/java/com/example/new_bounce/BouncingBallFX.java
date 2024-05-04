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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BouncingBallFX extends Application {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 1200;
    private static final int BORDER_RADIUS = 450;
    private static double BALL_RADIUS = 25;
    private static double GRAVITY = 1.18;
    private static double SPEED_INCREMENT = 0.18;
    private static double SIZE_INCREMENT = 2.0;
    private static double INITIAL_VELOCITY_MIN = 0.5; // Changeable initial velocity range
    private static double INITIAL_VELOCITY_MAX = 0.5; // Changeable initial velocity range

    private double ballX = 50; // initial position of the ball
    private double ballY = 150; // initial position of the ball
    private double ballDX; // initial speed in the x direction
    private double ballDY; // initial speed in the y direction
    private int collisionCount = 0;

    private final Color[] vibrantColors = {Color.RED, Color.GREEN, Color.BLUE, Color.VIOLET};
    private Color currentColor = Color.RED; // Initialize with any color
    private Circle border;
    private Circle ball;
    private List<Circle> tail = new ArrayList<>(); // List to hold the circles forming the tail
    private Text collisionText;

    private Random random = new Random();
    private Color tailColor = Color.GRAY; // Default tail color
    private Pane root;

    private Color targetBallColor = Color.RED; // Initial target ball color
    private Color targetBorderColor = Color.RED; // Initial target border color

    private long[] noteDurations; // Array to store note durations
    private ArrayList<Integer> notes;
    private int sizeNotes;

    @Override
    public void start(Stage primaryStage) {
        root = new Pane();
        root.setStyle("-fx-background-color: black;");

        loadMidiFile();

        // Initialize Synthesizer instance when the application starts
        try {
            synth = MidiSystem.getSynthesizer();
            synth.open();
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }

        // Other initialization code...

        primaryStage.setOnCloseRequest(event -> {
            // Close the Synthesizer instance when the application exits
            if (synth != null && synth.isOpen()) {
                synth.close();
            }
        });
        Button startButton = new Button("Start");
        startButton.setTranslateX(450);
        startButton.setTranslateY(1000);



        // Event handler for the button


        root.getChildren().add(startButton);

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
        collisionText.setFont(Font.font("Arial", 50));
        collisionText.setFill(Color.WHITE);
        collisionText.setTranslateX(370);
        collisionText.setTranslateY(80);

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
        startButton.setOnAction(event -> {
            // Hide the button after it's pressed
            startButton.setVisible(false);

            // Schedule the start of the game after a delay
            Timeline gameStartTimeline = new Timeline(new KeyFrame(Duration.seconds(3), e ->  timer.start()));
            gameStartTimeline.play();
        });
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
            playNotesWhenAsked(collisionCount);
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

    private void loadMidiFile() {
        // Load MIDI file
        try {
            File midiFile = new File("src/main/java/com/example/new_bounce/midi/meme.mid");
            Sequence sequence = MidiSystem.getSequence(midiFile);
            notes = extractNotesFromMidi(sequence);
            noteDurations = calculateNoteDurations(sequence);
        } catch (InvalidMidiDataException | IOException e) {
            e.printStackTrace();
            // Handle loading error according to your application logic
        }
    }

    private ArrayList<Integer> extractNotesFromMidi(Sequence sequence) {
        ArrayList<Integer> notes = new ArrayList<>();
        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    if (sm.getCommand() == ShortMessage.NOTE_ON) {
                        int note = sm.getData1();
                        notes.add(note);
                    }
                }
            }
        }
        sizeNotes = notes.size();
        return notes;
    }

    private long[] calculateNoteDurations(Sequence sequence) {
        long[] durations = new long[128]; // Assuming MIDI note numbers range from 0 to 127
        for (Track track : sequence.getTracks()) {
            int currentNote = -1;
            boolean noteOn = false;
            long noteOnTime = 0;

            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();

                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    int command = sm.getCommand();
                    int data1 = sm.getData1();

                    if (command == ShortMessage.NOTE_ON && data1 >= 0 && data1 < durations.length) {
                        if (!noteOn) {
                            noteOn = true;
                            currentNote = data1;
                            noteOnTime = event.getTick();
                        }
                    }

                    if (command == ShortMessage.NOTE_OFF && data1 == currentNote && noteOn) {
                        durations[currentNote] = event.getTick() - noteOnTime;
                        noteOn = false;
                    }
                }
            }
        }
        return durations;
    }

    private long getNoteDuration(int noteNumber) {
        if (noteNumber < 0 || noteNumber >= noteDurations.length) {
            System.err.println("Invalid note number!");
            return -1; // Return default value or handle the error accordingly
        }
        return noteDurations[noteNumber];
    }

    private void playNotesWhenAsked(int noteNumber) {
        playNoteWithDelay(notes.get(noteNumber % sizeNotes), getNoteDuration(notes.get(noteNumber % sizeNotes)));
    }

    private void playNoteWithDelay(int note, long duration) {
        try {
            if (synth != null && synth.isOpen()) {
                MidiChannel channel = synth.getChannels()[0];

                // Play the note after a delay
                channel.noteOn(note, 500);

                // Schedule note-off event after a delay
                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(duration), event -> {
                    channel.noteOff(note);
                }));
                timeline.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Synthesizer synth;

    public static void main(String[] args) {
        launch(args);
    }
}
