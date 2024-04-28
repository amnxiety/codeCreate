package com.example.new_bounce;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MusicPlayer {
    int noteNumber = 0;
    boolean flag;
    private ArrayList<Integer> notes;
    private long[] noteDurations;
    private int sizeNotes;
    private Synthesizer synth;

    public MusicPlayer(String midiFilePath) {
        loadMidiFile(midiFilePath);
    }

    public void playNotesWhenAsked() {
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
        playNoteWithDelay(notes.get(noteNumber % sizeNotes), getNoteDuration(notes.get(noteNumber % sizeNotes)));
    }

    private void loadMidiFile(String midiFilePath) {
        try {
            try {
                synth = MidiSystem.getSynthesizer();
                synth.open();
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            }
            File midiFile = new File(midiFilePath);
            Sequence sequence = MidiSystem.getSequence(midiFile);
            notes = extractNotesFromMidi(sequence);
            noteDurations = calculateNoteDurations(sequence);
        } catch (InvalidMidiDataException | IOException e) {
            e.printStackTrace();
        }
    }

    private void playNoteWithDelay(int note, long duration) {
        try {
            if (synth != null && synth.isOpen()) {
                MidiChannel channel = synth.getChannels()[0];

                // Play the note after a delay
                channel.noteOn(note, 000);

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
        for (Track track : sequence.getTracks()) {
            int currentNote = -1;
            boolean noteOn = false;
            long noteOnTime = 0;

            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();

                if (message instanceof ShortMessage sm) {
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

    public int getNoteAt(int index) {
        return notes.get(index);
    }

    public long getNoteDuration(int noteNumber) {
        return noteDurations[noteNumber];
    }
}

