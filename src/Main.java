import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main extends JFrame {
    private JPanel mainPanel;
    private JButton settingsButton;
    private JButton closeButton;
    private JComboBox<Integer> speedComboBox;
    private JButton startButton;
    private JButton stopButton;
    private JRadioButton onTimeRadioButton;
    private JRadioButton countdownRadioButton;
    private JSpinner timeSpinner;
    private JColorChooser colorChooser;
    private Timer timer;
    private Color selectedColor;
    private boolean isRed = false;
    private Color previousColor = null;

    public Main() {
        setTitle("Settings");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 2));

        settingsButton = new JButton("Color Settings");
        closeButton = new JButton("Close");
        speedComboBox = new JComboBox<>(new Integer[]{1000, 2000, 3000, 4000, 5000});
        startButton = new JButton("Start Countdown");
        stopButton = new JButton("Stop");
        onTimeRadioButton = new JRadioButton("On Time");
        countdownRadioButton = new JRadioButton("Countdown");
        timeSpinner = new JSpinner(new SpinnerDateModel());
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm:ss"));

        ButtonGroup radioButtonGroup = new ButtonGroup();
        radioButtonGroup.add(onTimeRadioButton);
        radioButtonGroup.add(countdownRadioButton);

        add(settingsButton);
        add(closeButton);
        add(new JLabel("Choose Color", JLabel.CENTER));
        add(new JLabel("", JLabel.CENTER));
        add(new JLabel("Speed", JLabel.CENTER));
        add(speedComboBox);
        add(onTimeRadioButton);
        add(countdownRadioButton);
        add(new JLabel("Time (HH:mm:ss)", JLabel.CENTER));
        add(timeSpinner);
        add(startButton);
        add(stopButton);

        closeButton.addActionListener(e -> dispose());
        settingsButton.addActionListener(e -> showSettingsDialog());
        startButton.addActionListener(e -> startCountdown());
        stopButton.addActionListener(e -> stopCountdown());

        setVisible(true);
    }

    private void showSettingsDialog() {
        JFrame settingsFrame = new JFrame("Color Settings");
        settingsFrame.setSize(400, 300);
        settingsFrame.setLocationRelativeTo(null);
        settingsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel settingsPanel = new JPanel(new BorderLayout());

        colorChooser = new JColorChooser(Color.WHITE);
        colorChooser.getSelectionModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                selectedColor = colorChooser.getColor();
            }
        });
        colorChooser.setPreviewPanel(new JPanel());
        AbstractColorChooserPanel[] colorPanels = colorChooser.getChooserPanels();
        for (AbstractColorChooserPanel colorPanel : colorPanels) {
            if (!colorPanel.getDisplayName().equals("RGB")) {
                colorChooser.removeChooserPanel(colorPanel);
            }
        }
        settingsPanel.add(colorChooser, BorderLayout.CENTER);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                settingsFrame.dispose();
            }
        });
        settingsPanel.add(okButton, BorderLayout.SOUTH);

        settingsFrame.add(settingsPanel);
        settingsFrame.setVisible(true);
    }
    private void startCountdown() {
        disableControls();
        int speed = (int) speedComboBox.getSelectedItem();
        if (countdownRadioButton.isSelected()) {
            try {
                Date selectedTime = (Date) timeSpinner.getValue();
                long currentTime = System.currentTimeMillis();
                long targetTime = selectedTime.getTime();
                int totalTimeInSeconds = (int) ((targetTime - currentTime) / 1000);
                timeSpinner.setEnabled(false);
                timer = new Timer(speed, new ActionListener() {
                    int timeLeft = totalTimeInSeconds;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFrame colorFrame = new JFrame();
                        colorFrame.setSize(200, 200);
                        colorFrame.setLocationRelativeTo(null);
                        colorFrame.getContentPane().setBackground(isRed ? Color.RED : selectedColor != null ? selectedColor : previousColor != null ? previousColor : Color.WHITE);
                        isRed = !isRed;
                        colorFrame.setVisible(true);

                        timeLeft--;
                        int hoursLeft = timeLeft / 3600;
                        int minutesLeft = (timeLeft - hoursLeft * 3600) / 60;
                        int secondsLeft = timeLeft % 60;
                        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                        try {
                            timeSpinner.setValue(formatter.parse(String.format("%02d:%02d:%02d", hoursLeft, minutesLeft, secondsLeft)));
                        } catch (ParseException ex) {
                            throw new RuntimeException(ex);
                        }
                        if (timeLeft == 0) {
                            timer.stop();
                            colorFrame.setVisible(false);
                            enableControls();
                        }
                    }
                });
                timer.start();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid time in HH:mm:ss format.", "Invalid Time", JOptionPane.ERROR_MESSAGE);
                enableControls();
            }
        } else {
            timeSpinner.setEnabled(true);
            timer = new Timer(speed, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFrame colorFrame = new JFrame();
                    colorFrame.setSize(200, 200);
                    colorFrame.setLocationRelativeTo(null);
                    colorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    colorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    colorFrame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            previousColor = colorFrame.getContentPane().getBackground();
                            timer.stop();
                            enableControls();

                        }
                    });
                    colorFrame.getContentPane().setBackground(isRed ? Color.RED : selectedColor != null ? selectedColor : previousColor != null ? previousColor : Color.WHITE);
                    isRed = !isRed;
                    colorFrame.setVisible(true);
                }
            });
            timer.start();
        }
    }

    private void stopCountdown() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        enableControls();

    }

    private void disableControls() {
        settingsButton.setEnabled(false);
        closeButton.setEnabled(false);
        colorChooser.setEnabled(false);
        speedComboBox.setEnabled(false);
        onTimeRadioButton.setEnabled(false);
        countdownRadioButton.setEnabled(false);
        timeSpinner.setEnabled(false);
        startButton.setEnabled(false);
    }

    private void enableControls() {
        settingsButton.setEnabled(true);
        closeButton.setEnabled(true);
        colorChooser.setEnabled(true);
        speedComboBox.setEnabled(true);
        onTimeRadioButton.setEnabled(true);
        countdownRadioButton.setEnabled(true);
        timeSpinner.setEnabled(true);
        startButton.setEnabled(true);
    }
    public static void main(String[] args) {

        int dialogResult = JOptionPane.showOptionDialog(null, "Welcome to my application!", "Color Timer", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"OK", "Close"}, "default");
        if (dialogResult == JOptionPane.NO_OPTION || dialogResult == JOptionPane.CLOSED_OPTION) {
            return;
        }


        Main main = new Main();
        main.setVisible(true);


    }
}


