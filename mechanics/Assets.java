package mechanics;

import javax.swing.ImageIcon;

public final class Assets {

    private Assets() {}

    private static ImageIcon load(String path) { 
        return new ImageIcon(path); 
    }

    public static final ImageIcon MOLE = load("images/mole.png");
    public static final ImageIcon BOMB = load("images/bomb.png");
    public static final ImageIcon BONUS = load("images/bonus.png");
    public static final ImageIcon HOLE = load("images/hole.png");
    public static final ImageIcon EXTRA_LIFE = load("images/extralife.png");
    public static final ImageIcon SLOWDOWN = load("images/slowdown.png");
}
