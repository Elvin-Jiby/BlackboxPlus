package main;

import computations.Board;
import computations.Lists;
import entities.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

/**
 * Controls Blackbox's game logic and state
 */
public class Game {
    // Application Variables
    private final GameWindow gameWindow;
    private final GameScreen gameScreen;

    // Game Variables
    private static final Image bgImage = (new ImageIcon(Objects.requireNonNull(Game.class.getResource("/Board Layouts/yellow-clear-all.png"))).getImage());
    private static final Image boardCover = (new ImageIcon(Objects.requireNonNull(Game.class.getResource("/Board Layouts/yellow-clear-background.png"))).getImage());
    private static final Image boardBoxNumber = (new ImageIcon(Objects.requireNonNull(Game.class.getResource("/Board Layouts/transparent-hexagon-numbered.PNG"))).getImage());

    private static final Random rand = new Random();
    private int numIncorrectGuesses = 0;
    private int numMarkersUsed = 0;

    private final ArrayList<HexagonalBox> hexagonalBoxes; // Arraylist that contains all the hexagonal boxes
    private final ArrayList<Atom> atomList; // Arraylist that contains all the atoms
    private final ArrayList<Marker> markersList = new ArrayList<>(); // Arraylist that contains all the markers and their colour and coords
    private final ArrayList<ArrayList<Ray>> rayPathList = new ArrayList<>(); // Arraylist that contains a list of each Ray and their own paths
    private ArrayList<ExitPoint> exitPointsList = new ArrayList<>(); // Arraylist that contains the coordinates of each exit point
    private final ArrayList<Integer> atomBoxNumbers = new ArrayList<>(); // Arraylist that stores the hexagonal box numbers that contain an atom
    private final Board raySystem = (new Lists()).createboard(); // instance of the Board class, used to handle the path of any ray

    // Game Settings
    private final int NUM_OF_ATOMS = 6;
    private Boolean seeAtomsAndRays = false; // debug setting to show internal atoms (default: false)
    private Boolean enableNumberedBoard = false; // debug setting to show a version of the board with numbered boxes
    private String playerName = "user" + rand.nextInt(99999);

    /**
     * Default constructor that creates the game's window and screen
     * Additionally initialises the atoms, exit points and hexagonal box lists
     */
    public Game() {
        gameScreen = new GameScreen(this); // creates a new screen
        gameWindow = new GameWindow(gameScreen, this); // creates a new window
        gameScreen.setFocusable(true); // used if we have input, so if we accidentally minimise, we can just click the window again to refocus
        gameScreen.requestFocus();

        hexagonalBoxes = loadHexagonalBoxes();
        atomList = generateAtoms();
        exitPointsList = loadExitPointCoords();
    }

    /**
     * Method to handle the drawing of the game's visuals
     * @param g A graphics instance to be drawn
     */
    public void render(Graphics g) {
        // draw background board
        g.drawImage(bgImage, 0, 0, 1280, 720, null);
        if (atomList != null) { // draw atoms
            for (Atom atom : atomList) g.drawImage(Atom.getAtomImage(), atom.getX(), atom.getY(), 50, 50, null);
        } else {
            JOptionPane.showMessageDialog(null, "Error: AtomList arraylist is null.", null, JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
        Graphics2D g2d = (Graphics2D) g;
        for (ArrayList<Ray> rayPath : rayPathList) for (Ray ray : rayPath) { // draw rays
                g2d.setStroke(new BasicStroke(5));
                g2d.setColor(Color.WHITE);
                g2d.drawLine(ray.getX1(), ray.getY1(), ray.getX2(), ray.getY2());
            }
        // hide internal atoms and rays if setting is false
        if (!seeAtomsAndRays) g.drawImage(boardCover, 0, 0, 1280, 720, null);
        if (enableNumberedBoard) g.drawImage(boardBoxNumber, 0, 0, 1280, 720, null);
        for (Marker marker : markersList) { // draw markers
            g2d.setColor(marker.getMarkerColour());
            g2d.fillOval(marker.getX(), marker.getY(), 12, 12);
        }
    }

    /**
     * Method to determine the outcome of a ray shot through a specified exit number
     * Ray path is created and added to the list, as well as updating the ray status and score
     * @param entry An exit number (represented by the arrow on the board)
     */
    public void shootRay(int entry) {
        ArrayList<Integer> boxNumList = raySystem.iterate(entry);
        ArrayList<Ray> newRayPath = new ArrayList<>();

        // Absorbed Ray Case
        boolean isRayAbsorbed = boxNumList.contains(-1);
        int pathLength = boxNumList.size();

        // Reflected Ray Case
        boolean isRayReflected = boxNumList.getFirst().equals(boxNumList.getLast());

        ExitPoint startPoint = exitPointsList.get(boxNumList.getFirst() - 1);
        newRayPath.add(new Ray(
                startPoint.getX(),
                startPoint.getY(),
                hexagonalBoxes.get(boxNumList.get(1) - 1).getX(),
                hexagonalBoxes.get(boxNumList.get(1) - 1).getY()));

        int i;
        for (i = 1; i < pathLength - 2; i++) {
            newRayPath.add(new Ray(
                    hexagonalBoxes.get(boxNumList.get(i) - 1).getX(),
                    hexagonalBoxes.get(boxNumList.get(i) - 1).getY(),
                    hexagonalBoxes.get(boxNumList.get(i + 1) - 1).getX(),
                    hexagonalBoxes.get(boxNumList.get(i + 1) - 1).getY()));
        }

        // set final ray towards atom for absorption case
        if (isRayAbsorbed) {
            newRayPath.add(new Ray(
                    hexagonalBoxes.get(boxNumList.get(i) - 1).getX(),
                    hexagonalBoxes.get(boxNumList.get(i) - 1).getY(),
                    hexagonalBoxes.get(boxNumList.get(i) - 1).getX(),
                    hexagonalBoxes.get(boxNumList.get(i) - 1).getY()));
            boxNumList.remove(boxNumList.getLast());
        } else { // in all other cases, set the final ray towards the exit point
            ExitPoint endPoint = exitPointsList.get(boxNumList.getLast() - 1);
            newRayPath.add(new Ray(
                    hexagonalBoxes.get(boxNumList.get(i) - 1).getX(),
                    hexagonalBoxes.get(boxNumList.get(i) - 1).getY(),
                    endPoint.getX(),
                    endPoint.getY()));
            // this is to make sure a ray can't be shot from the final exit point
            gameWindow.addVisitedBox(boxNumList.getLast());
        }

        /* Marker Cases */
        // Normal case - Ray goes straight through with no reflection of absorption
        if (!isRayAbsorbed && !isRayReflected) {
            // User can choose the colour of the marker
            Color colorChoice = gameWindow.askMarkerColor();

            // Marker at first entry point of ray path
            int startX = exitPointsList.get(boxNumList.getFirst() - 1).getX();
            int startY = exitPointsList.get(boxNumList.getFirst() - 1).getY();
            markersList.add(new Marker(startX, startY, colorChoice));

            // Marker at last entry point of ray path
            int endX = exitPointsList.get(boxNumList.getLast() - 1).getX();
            int endY = exitPointsList.get(boxNumList.getLast() - 1).getY();
            markersList.add(new Marker(endX, endY, colorChoice));

            numMarkersUsed += 2; // add 2 markers to the counter
            gameWindow.setLastRayStatus("normal/deflected");
        }
        // Absorbed case - Ray absorbed by atom
        else if (isRayAbsorbed) {
            // Black (Gray) marker at first entry point of ray path
            int startX = exitPointsList.get(boxNumList.getFirst() - 1).getX();
            int startY = exitPointsList.get(boxNumList.getFirst() - 1).getY();
            markersList.add(new Marker(startX, startY, Color.GRAY));

            numMarkersUsed++; // increment marker counter
            gameWindow.setLastRayStatus("absorbed");
        }
        // Reflected case = Ray deflects and exits at the same point of entry
        else {
            // White marker at first entry point of ray path
            int startX = exitPointsList.get(boxNumList.getFirst() - 1).getX();
            int startY = exitPointsList.get(boxNumList.getFirst() - 1).getY();
            markersList.add(new Marker(startX, startY, Color.WHITE));

            numMarkersUsed++; // increment marker counter
            gameWindow.setLastRayStatus("reflected");
        }

        // Add the newly created ray path to a path list
        rayPathList.add(newRayPath);
    }

    /**
     * Method to initialise the exitPoints list with all their respective coordinates
     * @return An array list containing the coordinates of all the exit points
     */
    public static ArrayList<ExitPoint> loadExitPointCoords() {
        ArrayList<ExitPoint> exitPoints = new ArrayList<>();

        // Hardcoded coordinates for each exit point (where the number is)
        // 1 - 10
        exitPoints.add(new ExitPoint(458, 50));
        exitPoints.add(new ExitPoint(429, 100));
        exitPoints.add(new ExitPoint(420, 112));
        exitPoints.add(new ExitPoint(389, 165));
        exitPoints.add(new ExitPoint(383, 174));

        exitPoints.add(new ExitPoint(358, 230));
        exitPoints.add(new ExitPoint(348, 240));
        exitPoints.add(new ExitPoint(318, 295));
        exitPoints.add(new ExitPoint(308, 308));
        exitPoints.add(new ExitPoint(280, 361));

        // 11 - 20
        exitPoints.add(new ExitPoint(309, 415));
        exitPoints.add(new ExitPoint(321, 428));
        exitPoints.add(new ExitPoint(346, 480));
        exitPoints.add(new ExitPoint(355, 490));
        exitPoints.add(new ExitPoint(383, 541));

        exitPoints.add(new ExitPoint(392, 552));
        exitPoints.add(new ExitPoint(420, 605));
        exitPoints.add(new ExitPoint(430, 620));
        exitPoints.add(new ExitPoint(461, 671));
        exitPoints.add(new ExitPoint(523, 670));

        // 21 - 30
        exitPoints.add(new ExitPoint(533, 671));
        exitPoints.add(new ExitPoint(598, 671));
        exitPoints.add(new ExitPoint(605, 672));
        exitPoints.add(new ExitPoint(670, 671));
        exitPoints.add(new ExitPoint(682, 672));

        exitPoints.add(new ExitPoint(746, 672));
        exitPoints.add(new ExitPoint(755, 672));
        exitPoints.add(new ExitPoint(819, 670));
        exitPoints.add(new ExitPoint(854, 620));
        exitPoints.add(new ExitPoint(858, 608));

        // 31 - 40
        exitPoints.add(new ExitPoint(893, 554));
        exitPoints.add(new ExitPoint(898, 543));
        exitPoints.add(new ExitPoint(927, 491));
        exitPoints.add(new ExitPoint(933, 479));
        exitPoints.add(new ExitPoint(962, 428));

        exitPoints.add(new ExitPoint(970, 415));
        exitPoints.add(new ExitPoint(999, 360));
        exitPoints.add(new ExitPoint(969, 306));
        exitPoints.add(new ExitPoint(959, 294));
        exitPoints.add(new ExitPoint(933, 240));

        // 41 - 50
        exitPoints.add(new ExitPoint(927, 230));
        exitPoints.add(new ExitPoint(895, 177));
        exitPoints.add(new ExitPoint(890, 167));
        exitPoints.add(new ExitPoint(858, 113));
        exitPoints.add(new ExitPoint(852, 100));

        exitPoints.add(new ExitPoint(822, 47));
        exitPoints.add(new ExitPoint(755, 47));
        exitPoints.add(new ExitPoint(747, 46));
        exitPoints.add(new ExitPoint(683, 48));
        exitPoints.add(new ExitPoint(672, 46));

        // 51 - 54
        exitPoints.add(new ExitPoint(606, 48));
        exitPoints.add(new ExitPoint(598, 47));
        exitPoints.add(new ExitPoint(533, 48));
        exitPoints.add(new ExitPoint(526, 46));

        return exitPoints;
    }

    /**
     * Method to initialise all the game's atom positions (as in, which hexagonal box they occupy)
     * @return An arraylist of the atoms
     */
    private ArrayList<Atom> generateAtoms() {
        if (hexagonalBoxes == null) {
            JOptionPane.showMessageDialog(null, "Error: HexagonalBoxes arraylist is null.", null, JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
        if (hexagonalBoxes.size() != 61) {
            JOptionPane.showMessageDialog(null, "Error: HexagonalBoxes arraylist length is not 61 as expected.", null, JOptionPane.ERROR_MESSAGE);
        }
        int hexagonalBoxesLength = hexagonalBoxes.size();
        int atomPosIndex = rand.nextInt(0, hexagonalBoxesLength);

        ArrayList<Atom> atoms = new ArrayList<>();
        for (int i = 0; i < NUM_OF_ATOMS; i++) {
            while (hexagonalBoxes.get(atomPosIndex).HasAtom()) { // ensures that atoms do not generate in the same box
                atomPosIndex = rand.nextInt(0, hexagonalBoxesLength);

            }
            raySystem.setrandom(raySystem, atomPosIndex + 1);
            atomBoxNumbers.add(atomPosIndex + 1);
            Atom atom = new Atom(hexagonalBoxes.get(atomPosIndex).getX(), hexagonalBoxes.get(atomPosIndex).getY());
            hexagonalBoxes.get(atomPosIndex).setHasAtom(true); // that box now has an atom present. set respective boolean hasAtom to true.
            atoms.add(atom);
        }
        return atoms;
    }

    /**
     * Method to initialise the coordinates for all the hexagonal boxes on the board
     * @return An array list of the constructed hexagonal boxes and their coordinates
     */
    public static ArrayList<HexagonalBox> loadHexagonalBoxes() {
        ArrayList<HexagonalBox> hexagonalBoxArrayList = new ArrayList<>();

        //row 1
        constructBoxes_Row1and9(hexagonalBoxArrayList, 103);
        //row 2
        constructBoxes_Row2and8(hexagonalBoxArrayList, 168);
        //row 3
        constructBoxes_Row3and7(hexagonalBoxArrayList, 231);
        //row 4
        constructBoxes_Row4and6(hexagonalBoxArrayList, 296);
        //row 5
        constructBoxes_Row5(hexagonalBoxArrayList);
        //row 6
        constructBoxes_Row4and6(hexagonalBoxArrayList, 424);
        //row 7
        constructBoxes_Row3and7(hexagonalBoxArrayList, 489);
        //row 8
        constructBoxes_Row2and8(hexagonalBoxArrayList, 552);
        //row 9
        constructBoxes_Row1and9(hexagonalBoxArrayList, 615);

        return hexagonalBoxArrayList;
    }

    private static void constructBoxes_Row1and9(ArrayList<HexagonalBox> hexagonalBoxArrayList, int y) {
        hexagonalBoxArrayList.add(new HexagonalBox(493, y));
        hexagonalBoxArrayList.add(new HexagonalBox(568, y));
        hexagonalBoxArrayList.add(new HexagonalBox(641, y));
        hexagonalBoxArrayList.add(new HexagonalBox(715, y));
        hexagonalBoxArrayList.add(new HexagonalBox(790, y));
    }

    private static void constructBoxes_Row2and8(ArrayList<HexagonalBox> hexagonalBoxArrayList, int y) {
        hexagonalBoxArrayList.add(new HexagonalBox(454, y));
        hexagonalBoxArrayList.add(new HexagonalBox(529, y));
        hexagonalBoxArrayList.add(new HexagonalBox(604, y));
        hexagonalBoxArrayList.add(new HexagonalBox(677, y));
        hexagonalBoxArrayList.add(new HexagonalBox(752, y));
        hexagonalBoxArrayList.add(new HexagonalBox(826, y));
    }

    private static void constructBoxes_Row3and7(ArrayList<HexagonalBox> hexagonalBoxArrayList, int y) {
        hexagonalBoxArrayList.add(new HexagonalBox(417, y));
        hexagonalBoxArrayList.add(new HexagonalBox(490, y));
        hexagonalBoxArrayList.add(new HexagonalBox(564, y));
        hexagonalBoxArrayList.add(new HexagonalBox(639, y));
        hexagonalBoxArrayList.add(new HexagonalBox(714, y));
        hexagonalBoxArrayList.add(new HexagonalBox(785, y));
        hexagonalBoxArrayList.add(new HexagonalBox(863, y));
    }

    private static void constructBoxes_Row4and6(ArrayList<HexagonalBox> hexagonalBoxArrayList, int y) {
        hexagonalBoxArrayList.add(new HexagonalBox(380, y));
        hexagonalBoxArrayList.add(new HexagonalBox(457, y));
        hexagonalBoxArrayList.add(new HexagonalBox(529, y));
        hexagonalBoxArrayList.add(new HexagonalBox(602, y));
        hexagonalBoxArrayList.add(new HexagonalBox(677, y));
        hexagonalBoxArrayList.add(new HexagonalBox(751, y));
        hexagonalBoxArrayList.add(new HexagonalBox(826, y));
        hexagonalBoxArrayList.add(new HexagonalBox(900, y));
    }

    private static void constructBoxes_Row5(ArrayList<HexagonalBox> hexagonalBoxArrayList) {
        hexagonalBoxArrayList.add(new HexagonalBox(344, 361));
        hexagonalBoxArrayList.add(new HexagonalBox(418, 361));
        hexagonalBoxArrayList.add(new HexagonalBox(492, 361));
        hexagonalBoxArrayList.add(new HexagonalBox(566, 361));
        hexagonalBoxArrayList.add(new HexagonalBox(639, 361));
        hexagonalBoxArrayList.add(new HexagonalBox(715, 361));
        hexagonalBoxArrayList.add(new HexagonalBox(789, 361));
        hexagonalBoxArrayList.add(new HexagonalBox(862, 361));
        hexagonalBoxArrayList.add(new HexagonalBox(937, 361));
    }

    public void toggleInternalBoardSetting() {
        seeAtomsAndRays = !seeAtomsAndRays;
        gameScreen.repaint();
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Boolean isAtomLocationGuessCorrect(int boxNumber) {
        return atomBoxNumbers.contains(boxNumber);
    }

    public void addIncorrectAtomGuess() {
        numIncorrectGuesses++;
    }

    public int getNumAtoms() {
        return NUM_OF_ATOMS;
    }

    public int getScore() {
        return getNumMarkersUsed() + (getNumIncorrectGuesses() * 5);
    }

    public int getNumIncorrectGuesses() {
        return numIncorrectGuesses;
    }

    public int getNumMarkersUsed() {
        return numMarkersUsed;
    }

    public void setEnableNumberedBoard(Boolean enableNumberedBoard) {
        this.enableNumberedBoard = enableNumberedBoard;
    }

}
