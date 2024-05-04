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
import java.util.*;

public class BouncingBallFX extends Application {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 1200;
    private static double BORDER_RADIUS = 450;
    private static double BALL_RADIUS = 45;
    private static double GRAVITY = 0.90;
    private static double SPEED_INCREMENT = 0.23;
    private static double SIZE_INCREMENT = 5;
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
    private static final double BORDER_SHRINK_RATE = 0.0;
    private long[] noteDurations; // Array to store note durations
    private ArrayList<Integer> notes;
    private int sizeNotes;

    @Override
    public void start(Stage primaryStage) {
        root = new Pane();
        root.setStyle("-fx-background-color: black;");

        loadMidiFile("src/main/java/com/example/new_bounce/midi/Harry Potter theme.mid");

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
        ball.setStrokeWidth(15);
        ball.setFill(Color.BLACK);
        ball.setStroke(currentColor);
//        ball.setFill(currentColor); // Set the color of the ball to currentColor

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
        Color currentBallColor = (Color) ball.getStroke();
        ball.setStroke(currentBallColor.interpolate(targetBallColor, 0.005)); // Adjust the interpolation factor as needed

        // Gradually transition border color towards the target border color
        Color currentBorderColor = (Color) border.getStroke();
        border.setStroke(currentBorderColor.interpolate(targetBorderColor, 0.005)); // Adjust the interpolation factor as needed

        // Check for collisions with border
        double dx = ballX - border.getCenterX();
        double dy = ballY - border.getCenterY();
        double distance = Math.sqrt(dx * dx + dy * dy);
//        BORDER_RADIUS -= BORDER_SHRINK_RATE;
        if (distance + BALL_RADIUS >= BORDER_RADIUS) {
            playNotesWhenAsked();

            ball.setRadius(ball.getRadius() + SIZE_INCREMENT);
            // Calculate angle to the center of the outer circle
            double angleToCenter = Math.atan2(dy, dx);

            // Calculate reflection angle
            double incidenceAngle = Math.atan2(ballDY, ballDX);
            double reflectionAngle = 2 * angleToCenter - incidenceAngle + Math.PI;

            // Update ball direction using reflection formula
            double speed = Math.sqrt(ballDX * ballDX + ballDY * ballDY);
            ballDX = Math.cos(reflectionAngle) * (speed + SPEED_INCREMENT);
            ballDY = Math.sin(reflectionAngle) * (speed + SPEED_INCREMENT);

            // Increase the size of the border
//            BORDER_RADIUS += SIZE_INCREMENT;

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

        border.setRadius(BORDER_RADIUS);
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
    private final int REVERB_LEVEL = 40;
    private void loadMidiFile(String midiFilePath) {
        try {
            try {
                synth = MidiSystem.getSynthesizer();
                synth.open();
                // Set reverb
                MidiChannel[] channels = synth.getChannels();
                for (MidiChannel channel : channels) {
                    channel.controlChange(91, REVERB_LEVEL);
                    channel.controlChange(77,64);
                }
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            }
            File midiFile = new File(midiFilePath);
            sequence = MidiSystem.getSequence(midiFile);
            notes = extractNotesFromMidi(sequence);
            noteDurations = calculateNoteDurations(sequence);
            preloadNoteVelocities(sequence);
        } catch (InvalidMidiDataException | IOException e) {
            e.printStackTrace();
        }
    }
    private int getNoteVelocity(int noteNumber, Track track) {
        for (int i = 0; i < track.size(); i++) {
            MidiEvent event = track.get(i);
            MidiMessage message = event.getMessage();
            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) message;
                if (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData1() == noteNumber) {
                    return sm.getData2(); // Return velocity of the note
                }
            }
        }
//        System.out.println("Not Found");
        return 100; // Default velocity if not found
    }
    private void preloadNoteVelocities(Sequence sequence) {
        for (int note : notes) {
            List<Integer> velocities = new ArrayList<>();
            for (Track track : sequence.getTracks()) {
                int velocity = getNoteVelocity(note, track);
                velocities.add(velocity);
            }
            noteVelocitiesMap.put(note, velocities);
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
        float ticksPerBeat = sequence.getResolution(); // Default ticks per beat

        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();

                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    if (sm.getCommand() == ShortMessage.NOTE_ON) {
                        int note = sm.getData1();
                        int velocity = sm.getData2();
                        long noteOnTick = event.getTick();

                        // Find corresponding Note Off event
                        for (int j = i + 1; j < track.size(); j++) {
                            MidiEvent offEvent = track.get(j);
                            MidiMessage offMessage = offEvent.getMessage();
                            if (offMessage instanceof ShortMessage) {
                                ShortMessage offSm = (ShortMessage) offMessage;
                                if (offSm.getCommand() == ShortMessage.NOTE_OFF && offSm.getData1() == note) {
                                    long noteOffTick = offEvent.getTick();
                                    float ticksPerMicrosecond = ticksPerBeat / (500000f / 60); // Assuming 120 BPM
                                    durations[note] = (long) (((noteOffTick - noteOnTick) / ticksPerMicrosecond) * velocity / 127.0f);
                                    break;
                                }
                            }
                        }
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
    int noteNumber=0;
    Sequence sequence;
    Map<Integer, List<Integer>> noteVelocitiesMap = new HashMap<>();

    public void playNotesWhenAsked() {
        System.out.println(noteNumber);
        int note = notes.get(noteNumber % sizeNotes);
//        if (noteNumber == 0) {
//            flag = false;
//        }
//        if (noteNumber == sizeNotes) {
//            flag = true;
//        }
//        if (flag) {
//            noteNumber -= 1;
//        } else {
//            noteNumber += 1;
//        }
//        if(noteNumber==60){
//            noteNumber=0;
//        }

        noteNumber += 1;


        System.out.println("total Tracks:"+ sequence.getTracks().length);
        List<Integer> velocities = noteVelocitiesMap.get(note);


        int maxVelocity = velocities.stream().max(Comparator.naturalOrder()).orElse(100); // Default velocity if not found
        long duration = getNoteDuration(note);
        playNoteWithDelay(note, maxVelocity, duration);
    }

    private void playNoteWithDelay(int note, int velocity, long duration) {
        try {
            if (synth != null && synth.isOpen()) {
                MidiChannel[] channels = synth.getChannels();

                // Find an available MIDI channel
                MidiChannel channel = Arrays.stream(channels).filter(ch -> !ch.getMono()).findFirst().orElse(null);
//            System.out.println(channels.length);
                if (channel != null) {
                    // Set soft attack
                    channel.controlChange(73, 64); // Adjust the value as needed

                    // Play the note with the specified velocity
                    channel.noteOn(note, velocity);

                    // Schedule note-off event after the duration
                    Timeline timeline = new Timeline(
                            new KeyFrame(Duration.ZERO, event -> {
                                // Set soft release
                                channel.controlChange(72, 64); // Adjust the value as needed
                            }),
                            new KeyFrame(Duration.millis(duration), event -> {
                                // Turn off the note
                                channel.noteOff(note);
                            })
                    );
                    timeline.play();
                }
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
