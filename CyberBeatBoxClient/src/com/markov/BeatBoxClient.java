package com.markov;

import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class BeatBoxClient {

    private JFrame theFrame;
    private JList<String> incomingList;
    private JTextField userMessage;
    private ArrayList<JCheckBox> checkboxList;
    private int nextNum;
    private Vector<String> listVector = new Vector<>();
    private String userName;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Map<String, boolean[]> otherSeqsMap = new HashMap<>();

    private Sequencer sequencer;
    private Sequence sequence;
    private Track track;

    private String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal",
            "Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga",
            "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo", "Open Hi Conga"};
    private int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public void startUp(String userName) {
        this.userName = userName;
        //open connection to the server
        try {
            Socket socket = new Socket("127.0.0.1", 4242);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            Thread remote = new Thread(new RemoteReader());
            remote.start();
        } catch (IOException e) {
            System.out.println("Couldn't connect with the server");
        }

        setUpMidi();
        buildGUI();
    }

    private void setUpMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();

            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildGUI() {
        theFrame = new JFrame("Cyber BeatBox");
        theFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        checkboxList = new ArrayList<>();

        Box buttonBox = new Box(BoxLayout.Y_AXIS);
        buttonBox.getInsets().set(5, 5, 5, 5);

        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyTempoUpListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new MyTempoDownListener());
        buttonBox.add(downTempo);

        JButton save = new JButton("Save");
        save.addActionListener(new MySaveListener());
        buttonBox.add(save);

        JButton restore = new JButton("Restore");
        restore.addActionListener(new MyRestoreListener());
        buttonBox.add(restore);

        JButton sendIt = new JButton("Send It");
        sendIt.addActionListener(new MySendListener());
        buttonBox.add(sendIt);


        userMessage = new JTextField();
        buttonBox.add(userMessage);

        incomingList = new JList<>();
        incomingList.addListSelectionListener(new MyListSelectionListener());
        incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane theList = new JScrollPane(incomingList);
        buttonBox.add(theList);
        incomingList.setListData(listVector);

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for(String str : instrumentNames) {
            nameBox.add(new Label(str));
        }

        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);

        theFrame.getContentPane().add(background);
        GridLayout grid = new GridLayout(16, 16, 2, 1);

        JPanel mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);

        for(int i = 0; i < 256; i++) {
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            mainPanel.add(c);
            checkboxList.add(c);
        }

        theFrame.setBounds(50, 50, 300, 300);
        theFrame.pack();
        theFrame.setVisible(true);
    }

    private void buildTrackAndStart() {
        ArrayList<Integer> trackList;
        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for(int i = 0; i < 16; i++) {
            trackList = new ArrayList<>();

            for(int j = 0; j < 16; j++) {
                JCheckBox jc = checkboxList.get(j + (i * 16));
                if(jc.isSelected()) {
                    int key = instruments[i];
                    trackList.add(key);
                } else {
                    trackList.add(null);
                }
            }
            makeTracks(trackList);
        }
        track.add(makeEvent(192, 9, 1, 0, 15));

        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.setTempoInBPM(120);
            sequencer.start();
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    private void changeSequence(boolean[] checkboxState) {
        for(int i = 0; i < 256; i++) {
            JCheckBox check = checkboxList.get(i);
            if(checkboxState[i]) {
                check.setSelected(true);
            } else {
                check.setSelected(false);
            }
        }
    }

    private void makeTracks(ArrayList<Integer> tracks) {
        Iterator<Integer> it = tracks.iterator();
        for(int i = 0; i < 16; i++) {
            Integer num = it.next();
            if(num != null) {
                int numKey = num;
                track.add(makeEvent(144, 9, numKey, 100, i));
                track.add(makeEvent(128, 9, numKey, 100, i + 1));
            }
        }
    }

    private MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
        MidiEvent event = null;

        try {
            ShortMessage a = new ShortMessage(comd, chan, one, two);
            event = new MidiEvent(a, tick);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
        return event;
    }


    private class RemoteReader implements Runnable {
        boolean[] checkboxState;
        String nameToShow;
        Object obj;
        @Override
        public void run() {
            try {
                while((obj = in.readObject()) != null) {
                    nameToShow = (String) obj;
                    checkboxState = (boolean[]) in.readObject();
                    otherSeqsMap.put(nameToShow, checkboxState);
                    listVector.add(nameToShow);
                    incomingList.setListData(listVector);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class MyStartListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            buildTrackAndStart();
        }
    }

    private class MyStopListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            sequencer.stop();
        }
    }

    private class MyTempoUpListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor * 1.03));
        }
    }

    private class MyTempoDownListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor * 0.97));
        }
    }

    private class MySendListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // make an array list of just the state of the checkboxes
            boolean[] checkboxState = new boolean[256];

            for(int i = 0; i < 256; i++) {
                JCheckBox check = checkboxList.get(i);
                if(check.isSelected()) {
                    checkboxState[i] = true;
                }
            }

            try {
                if(out != null) {
                    out.writeObject(userName + nextNum++ + ": " + userMessage.getText());
                    out.writeObject(checkboxState);
                }
            } catch (IOException e1) {
                System.out.println("Error with sending message to the server: " + e1.getMessage());
            }
            userMessage.setText("");
        }
    }

    private class MySaveListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            storeCurrPattern();
        }
    }

    private class MyRestoreListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try (ObjectInputStream in = new ObjectInputStream(
                    new FileInputStream(new File("Checkbox.dat")))) {
                boolean[] checkboxState = (boolean[]) in.readObject();

                if(checkboxState != null) {
                    for(int i = 0; i < 256; i++) {
                        if(checkboxState[i]) {
                            checkboxList.get(i).setSelected(true);
                        } else {
                            checkboxList.get(i).setSelected(false);
                        }
                    }
                }
                sequencer.stop();
                buildTrackAndStart();

            } catch (Exception e1) {
                System.out.println("Error with restoring the pattern");
            }
        }
    }

    private class MyListSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent le) {
            if(!le.getValueIsAdjusting()) {
                String selected = incomingList.getSelectedValue();
                if(selected != null) {
                    if(showDialog()) {
                        storeCurrPattern();
                    }
                    // now go to the map and change the sequence
                    boolean[] selectedState = otherSeqsMap.get(selected);
                    changeSequence(selectedState);
                    sequencer.stop();
                    buildTrackAndStart();
                }
            }
        }
    }

    private boolean showDialog() {
        JOptionPane optionPane = new JOptionPane(
                "Do you want to save your current pattern?",
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION
        );

        JDialog dialog = new JDialog(theFrame, "Choose an option", true);
        dialog.setContentPane(optionPane);
        optionPane.addPropertyChangeListener(
                (evt -> {
                    String prop = evt.getPropertyName();

                    if(dialog.isVisible() && evt.getSource() == optionPane
                            && prop.equals(JOptionPane.VALUE_PROPERTY)) {
                        dialog.setVisible(false);
                    }
                })
        );
        dialog.setBounds(200, 200, 150, 50);
        dialog.pack();
        dialog.setVisible(true);

        if(optionPane.getValue().equals("uninitializedValue")) {
            return false;
        }
        int value = ((Integer) optionPane.getValue());

        return value == JOptionPane.YES_OPTION;
    }

    private void storeCurrPattern() {
        boolean[] checkboxState = new boolean[256];

        for(int i = 0; i < 256; i++) {
            JCheckBox check = checkboxList.get(i);
            if(check.isSelected()) {
                checkboxState[i] = true;
            }
        }

        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(new File("Checkbox.dat")))) {

            File file = new File("Checkbox.dat");
            out.writeObject(checkboxState);
        } catch (IOException e) {
            System.out.println("Error with saving the pattern");
        }
    }
}
