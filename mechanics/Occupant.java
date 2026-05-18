package mechanics;

import javax.swing.ImageIcon;

public abstract class Occupant {
    protected boolean visible;
    protected int timeRemaining;

    public Occupant() {
        this.visible = false;
        this.timeRemaining = 0;
    }

    public abstract int whack();
    public abstract ImageIcon getImage();

    public int bonusTime() { 
        return 0; 
    }

    public void hide() {
        visible = false;
        timeRemaining = 0;
    }

    public void show(int duration) {
        visible = true;
        timeRemaining = duration;
    }

    public void tick() {
        if (timeRemaining > 0) {
            timeRemaining--;
            if (timeRemaining == 0) {
                hide();
            }
        }
    }

    public boolean isVisible() {
        return visible;
    }
}
