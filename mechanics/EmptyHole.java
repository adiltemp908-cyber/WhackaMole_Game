package mechanics;

import javax.swing.ImageIcon;

public class EmptyHole extends Occupant {

    @Override
    public int whack() { 
        return 0; 
    }

    @Override
    public ImageIcon getImage() { 
        return Assets.HOLE; 
    }
}
