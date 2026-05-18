package mechanics;

import javax.swing.ImageIcon;

public class Mole extends Occupant {
    private static final int SCORE_VALUE = 100;

    @Override
    public int whack() {
        hide();
        return SCORE_VALUE;
        
    }

    @Override
    public ImageIcon getImage() { 
        return Assets.MOLE; }
    
    
    }
