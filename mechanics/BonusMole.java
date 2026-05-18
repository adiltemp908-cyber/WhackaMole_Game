package mechanics;

import javax.swing.ImageIcon;

public class BonusMole extends Occupant {
    private static final int BONUS_SCORE = 500;
     private static final int BONUS_TIME = 0;

    @Override
    public int whack() {
        hide();
        return BONUS_SCORE;
    }

    @Override
    public ImageIcon getImage() { 
        return Assets.BONUS; 
     }

         @Override
    public int bonusTime() { 
        return BONUS_TIME;
    }
}
