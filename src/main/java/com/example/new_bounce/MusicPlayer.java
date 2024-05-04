package com.example.new_bounce;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class MusicPlayer {
    int noteNumber = 0;
    boolean flag;
    private ArrayList<Integer> notes;
    private long[] noteDurations;
    private int sizeNotes;
    private Synthesizer synth;
    Sequence sequence;
    private Map<Integer, List<Integer>> noteVelocitiesMap = new HashMap<>();

    // Add more reverb for a more spacious sound
    private final int REVERB_LEVEL = 40;

    public MusicPlayer(String midiFilePath) {
        loadMidiFile(midiFilePath);
    }

    public void playNotesWhenAsked() {
        System.out.println(noteNumber);
        int note = notes.get(noteNumber % sizeNotes);
        if (noteNumber == 0) {
            flag = false;
        }
        if (noteNumber == sizeNotes) {
            flag = true;
        }
        if (flag) {
            noteNumber -= 1;
        } else {
            noteNumber += 1;
        }
//        if(noteNumber==60){
//            noteNumber=0;
//        }

//        noteNumber += 1;

        System.out.println("total Tracks:"+ sequence.getTracks().length);
        List<Integer> velocities = noteVelocitiesMap.get(note);
        int maxVelocity = velocities.stream().max(Comparator.naturalOrder()).orElse(100); // Default velocity if not found
        long duration = getNoteDuration(note);
        playNoteWithDelay(note, maxVelocity, duration);
    }

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

    private void playNoteWithDelay(int note, int velocity, long duration) {
        try {
            if (synth != null && synth.isOpen()) {
                MidiChannel[] channels = synth.getChannels();

                // Find an available MIDI channel
                MidiChannel channel = Arrays.stream(channels).filter(ch -> !ch.getMono()).findFirst().orElse(null);
//                System.out.println(channels.length);
                if (channel != null) {
                    // Set soft attack
                    channel.controlChange(73, 64); // Adjust the value as needed

                    // Play the note with the specified velocity after a delay
                    channel.noteOn(note, velocity);

                    // Schedule note-off event after a delay
                    Timeline timeline = new Timeline(new KeyFrame(Duration.millis(duration), event -> {
                        // Set soft release
                        channel.controlChange(72, 64); // Adjust the value as needed
                        channel.noteOff(note);
                    }));
                    timeline.play();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private ArrayList<Integer> extractNotesFromMidi(Sequence sequence) {
        ArrayList<Integer> notes = new ArrayList<>();
        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage sm) {
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
    private int getNoteVelocityFromAllTracks(int noteNumber) {
        int maxVelocity = 0;
        for (Track track : sequence.getTracks()) {
            int velocity = getNoteVelocity(noteNumber, track);
            if (velocity > maxVelocity) {
                maxVelocity = velocity;
            }
        }
        return maxVelocity;
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

    public int getNoteAt(int index) {
        return notes.get(index);
    }

    public long getNoteDuration(int noteNumber) {
        return noteDurations[noteNumber];
    }
}

