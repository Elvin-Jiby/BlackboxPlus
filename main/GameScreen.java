package main;

import inputs.MouseInputs;

import javax.swing.*;
import java.awt.*;

public class GameScreen extends JPanel {
    // Application Variables
    private Game game;
    private MouseInputs mouseInputs;
    private JTextField arrowNumberInputField;
    private JTextField playerNameInputField;
    private JLabel resultLabel;
    private JLabel playerNameLabel;
    private JLabel scoreLabel;
    private int score = 100;
    private int value = 1;
    private String playerName = "user34567";

    public GameScreen(Game game) {
        this.game = game;
        setPreferredSize(new Dimension(1280, 720)); // setting the resolution of the game
        setBackground(Color.BLACK);

        scoreLabel = new JLabel("Score: " + score);
        scoreLabel.setForeground(Color.WHITE);
//        scoreLabel.setBounds(1100, 650, 150, 30);

        mouseInputs = new MouseInputs(this); // add mouse input support
        addMouseListener(mouseInputs); // needed to perform mouse actions

        arrowNumberInputField = new JTextField(10);
        arrowNumberInputField.setText("1");
//        textField.setBounds(50, 50, 250, 250);
        arrowNumberInputField.addActionListener(e -> validateInput());
        resultLabel = new JLabel("Valid Integer: " + value);
        resultLabel.setForeground(Color.WHITE);

        playerNameInputField = new JTextField(20);
        playerNameInputField.setText("user34567");
//        textField.setBounds(50, 50, 250, 250);
        playerNameInputField.addActionListener(e -> updateUsername());

        playerNameLabel = new JLabel("Player Name: " + playerName);
        playerNameLabel.setForeground(Color.WHITE);

        JLabel label = new JLabel("Enter an integer between 1 and 59: ");
        label.setForeground(Color.WHITE);

        add(playerNameInputField);
        add(playerNameLabel);
        add(label);
        add(arrowNumberInputField);
        add(resultLabel);
        add(scoreLabel);


        JLabel colorWindow = new JLabel();
        colorWindow.setBackground(Color.WHITE);
        colorWindow.setText("Sample Text");
        colorWindow.setFont(new Font("Comic Sans", Font.BOLD, 20));
        colorWindow.setOpaque(true);

        JButton button = new JButton("Pick a colour");
        button.addActionListener(e -> {
            if (e.getSource() == button) {
                JColorChooser colorChooser = new JColorChooser();

                Color color = JColorChooser.showDialog(null, "Choose a colour!", Color.BLACK);
                colorWindow.setForeground(color);
            }
        });

        add(button);
        add(colorWindow);

        setVisible(true);
    }

    private void addSpotButtons(GameScreen gameScreen) {
        int[][] buttonPosArray = new int[][]{
                {425, 39}, {603, 18}, {665, 25}, {842, 39}, {911, 172}, {958, 219}, {1030, 366}, {941, 498}, {917, 558},
                {826, 682}, {658, 690}, {596, 689}, {438, 675}, {356, 549}, {338, 515}, {245, 352}, {316, 214}, {352, 174},
        };

        for (int i = 0; i < buttonPosArray.length; i++) {
            int xPos = buttonPosArray[i][0];
            int yPos = buttonPosArray[i][1];

            JButton button = new JButton();
            button.setBounds(xPos, yPos, 50, 50);
            button.setFocusable(false);

            int buttonNo = i + 1;
            button.setText(String.valueOf(buttonNo));
            gameScreen.add(button);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        game.render(g);
    }

    private void validateInput() {
        try {
            value = Integer.parseInt(arrowNumberInputField.getText());
            if (value < 1 || value > 59) {
                JOptionPane.showMessageDialog(this, "Enter a number between 1 and 59", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                arrowNumberInputField.setText("1"); // Reset to default value
                value = 1;
                resultLabel.setText("Valid Integer: " + value);
            } else resultLabel.setText("Valid Integer: " + value);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid integer.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            arrowNumberInputField.setText("1"); // Reset to default value
            resultLabel.setText("Valid Integer: ");
        }
    }

    private void updateUsername() {
        playerName = playerNameInputField.getText();
        playerNameLabel.setText(playerName);
    }

    public Game getGame() {
        return game;
    } // accessor
}
