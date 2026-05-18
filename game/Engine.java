package game;

import mechanics.*;
import exceptions.InvalidStateException;
import java.util.Random;

public class Engine implements Runnable {

    private final Game game;
    private final Occupant[] grid;
    private int score, timeRemaining;
    private boolean running;
    private final Random random;

    private final int rows;
    private final int cols;
    private final int GAME_DURATION;
    private final int GRID_SIZE;
    private final int spawnChance;

    public int getRows()     { return rows; }
    public int getCols()     { return cols; }
    public int getGridSize() { return GRID_SIZE; }
    public int getDuration() { return GAME_DURATION; }

    public Engine(Game game, int rows, int cols, int duration, int spawnChance) {
        this.game          = game;
        this.random        = new Random();
        this.rows          = rows;
        this.cols          = cols;
        this.GRID_SIZE     = rows * cols;
        this.GAME_DURATION = duration;
        this.spawnChance   = spawnChance;

        this.grid = new Occupant[GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) grid[i] = new EmptyHole();

        this.score         = 0;
        this.timeRemaining = GAME_DURATION;
        this.running       = true;
    }

    @Override
    public void run() {
        while (running && timeRemaining > 0) {
            try {
                Thread.sleep(1000);
                timeRemaining--;
                game.updateTimer(timeRemaining);

                for (int i = 0; i < GRID_SIZE; i++) {
                    grid[i].tick();
                    if (!grid[i].isVisible()) {
                        game.updateHole(i, grid[i]);
                    }
                }

                try {
                    spawn();
                } catch (InvalidStateException e) {
                    // occupied hole — skip silently
                }

            } catch (InterruptedException ex) {
                running = false;
                break;
            }
        }

        if (timeRemaining <= 0) game.gameOver();
    }

    private void spawn() {
        if (random.nextInt(100) < spawnChance) {
            int idx = random.nextInt(GRID_SIZE);

            if (grid[idx].isVisible() && !(grid[idx] instanceof EmptyHole)) {
                throw new InvalidStateException("Attempted to spawn over occupied hole");
            }
            
            int roll = random.nextInt(100);
            Occupant o =
                roll < 65 ? new Mole()          // 65%
              : roll < 85 ? new Bomb()           // 20%
              : roll < 100 ? new BonusMole()      // 15%
              : roll < 100 ? new ExtraLifeMole()  // 0%
              :             new SlowDownMole();   // 0%

            o.show(3);
            grid[idx] = o;
            game.updateHole(idx, o);
        }
    }

    public void handleClick(int index) {
        if (index < 0 || index >= GRID_SIZE) return;

        Occupant o = grid[index];
        if (o.isVisible()) {
            score += o.whack();
            game.updateScore(score);

            int extra = o.bonusTime();
            if (extra > 0) timeRemaining += extra;

            grid[index] = new EmptyHole();
            game.updateHole(index, grid[index]);
        }
    }

    public void stop()    { running = false; }
    public int getScore() { return score; }
}