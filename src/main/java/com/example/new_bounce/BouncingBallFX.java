package com.example.new_bounce;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BouncingBallFX extends Application {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 1000;
    private static final int BORDER_RADIUS = 450;
    private static double BALL_RADIUS = 25;
    private static double GRAVITY = 0.1;
    private static double SPEED_INCREMENT = 0.05;
    private static double SIZE_INCREMENT = 2.3;
    private static double INITIAL_VELOCITY_MIN = 0.5; // Changeable initial velocity range
    private static double INITIAL_VELOCITY_MAX = 0.5; // Changeable initial velocity range

    private double ballX = 150; // initial position of the ball
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

    ArrayList<Integer> notes;
    private void loadMidiFile(){
        // Load MIDI file
        File midiFile = new File("src/main/java/com/example/new_bounce/midi/meme.mid");

        // Extract notes from MIDI file
        notes = extractNotesFromMidi(midiFile);

    }

    private ArrayList<Integer> extractNotesFromMidi(File midiFile) {
        ArrayList<Integer> notes = new ArrayList<>();

        try {
            Sequence sequence = MidiSystem.getSequence(midiFile);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        sizeNotes = notes.size();
        return notes;
    }
    int sizeNotes;
    private void playNotesWhenAsked(int noteNumber) {
            playNoteWithDelay(notes.get(noteNumber % sizeNotes));
    }

    private Synthesizer synth;
    // Method to play a single note
    // Method to play a single note with a delay
    // Modified playNoteWithDelay method to reuse the Synthesizer instance
    private void playNoteWithDelay(int note) {
        try {
            if (synth != null && synth.isOpen()) {
                MidiChannel channel = synth.getChannels()[0];

                // Play the note after a delay
                channel.noteOn(note, 500);
                long duration = calculateNoteDuration(note);
                // Schedule note-off event after a delay
                Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(duration), event -> {
                    channel.noteOff(note);
                }));
                timeline.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long calculateNoteDuration(int noteNumber) throws InvalidMidiDataException, IOException {
        long duration = 0;
        File midiFile = new File("src/main/java/com/example/new_bounce/midi/meme.mid");
        Sequence sequence = MidiSystem.getSequence(midiFile);

        int currentNote = -1;
        boolean noteOn = false;
        long noteOnTime = 0;

        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();

                // Check if the message is a ShortMessage (i.e., a MIDI message)
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    int command = sm.getCommand();
                    int data1 = sm.getData1();

                    // If it's a note-on event and matches the note we're interested in
                    if (command == ShortMessage.NOTE_ON && data1 == noteNumber) {
                        if (!noteOn) {
                            noteOn = true;
                            currentNote = data1;
                            noteOnTime = event.getTick();
                        }
                    }

                    // If it's a note-off event for the current note
                    if (command == ShortMessage.NOTE_OFF && data1 == currentNote && noteOn) {
                        duration = event.getTick() - noteOnTime;
                        noteOn = false;
                        break; // Exit the loop since we found the duration
                    }
                }
            }
        }

        // Convert ticks to milliseconds (assuming default tempo)
        return duration * 60000 / 120 / sequence.getResolution();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
