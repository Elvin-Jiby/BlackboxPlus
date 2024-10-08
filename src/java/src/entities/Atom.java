package entities;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class Atom extends Entity {
    private static final Image atomImage = (new ImageIcon(Objects.requireNonNull(Atom.class.getResource("/Miscellaneous/atomImage.png"))).getImage());
    // (x,y) coordinate of an atom on a board

    public Atom(int x, int y) {
        super(x-25,y-25);
    }

    @Override
    public String toString() {
        return "Atom: Position ("+getX()+","+getY()+")";
    }

    public int getCenterX() {
        return getX()+25;
    }
    public int getCenterY() {
        return getY()+25;
    }

    public static Image getAtomImage() {
        return atomImage;
    }
}
