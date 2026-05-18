package mechanics;

import javax.swing.ImageIcon;

public class SlowDownMole extends Occupant {

    private static final int SCORE_VALUE = 200;
    private static final int BONUS_TIME = 10;

    @Override
    public int whack() {
        hide();
        return SCORE_VALUE;
    }

    @Override
    public ImageIcon getImage() { 
        return Assets.SLOWDOWN; 
    }

    @Override
    public int bonusTime() { 
        return BONUS_TIME; 
    }
}
