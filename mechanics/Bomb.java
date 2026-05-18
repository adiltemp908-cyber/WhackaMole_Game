package mechanics;

import javax.swing.ImageIcon;

public class Bomb extends Occupant {
    private static final int PENALTY = -500;
    

    @Override
    public int whack() {
        hide();
        return PENALTY;
    }

    @Override
    public ImageIcon getImage() { 
        return Assets.BOMB; 
   
    }
        
}
