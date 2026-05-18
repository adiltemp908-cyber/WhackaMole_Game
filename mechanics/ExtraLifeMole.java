package mechanics;

import javax.swing.ImageIcon;

public class ExtraLifeMole extends Occupant {

    private static final int SCORE_VALUE = 500;
    private static final int EXTRA_TIME = 15;

    @Override
    public int whack() {
        hide();
        return SCORE_VALUE;
    }

    @Override
    public ImageIcon getImage() { 
        return Assets.EXTRA_LIFE; 
    }

    @Override
    public int bonusTime() { 
        return EXTRA_TIME; 
    }
}
