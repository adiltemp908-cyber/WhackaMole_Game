Whack-A-Mole (Java OOP Project)

This is a simple Whack-A-Mole game I built using Java Swing to practice Object-Oriented Programming concepts and basic game logic.

The idea is straightforward: moles appear randomly on the screen, and the player has to click them to score points before time runs out. I also added a few variations like bombs and bonus moles to make it a bit more interesting.

What this project focuses on
Applying OOP concepts in a real project
Separating UI and logic properly
Using threads for game loops
Handling user interaction through events
Managing state like score and time
Features
Grid-based Whack-A-Mole gameplay
Different types of objects:
Normal mole (gives points)
Bomb (penalty)
Bonus mole (extra points/time)
Timer-based game
Score tracking
High score system (saved locally)
Custom settings:
Grid size
Game duration
Spawn rate
Custom mode (disables leaderboard if settings are changed)
Project Structure
project-root/

game/
    Game.java        # UI and main class
    Engine.java      # Core game logic and loop

mechanics/
    Assets.java      # Loads images
    (Mole, Bomb, etc.)

score/
    HighScoreManager.java
    PlayerScore.java

exceptions/
    HighScoreException.java
    InvalidStateException.java

images/
    mole.png
    bomb.png
    bonus.png
    hole.png
    extralife.png
    slowdown.png
How it works (simple explanation)
The game runs using a separate thread (Engine)
Every second:
Timer decreases
Existing objects update themselves
New objects may spawn randomly
When the player clicks:
The engine checks what was clicked
Score is updated accordingly
The tile resets
OOP Concepts Used
1. Abstraction

All grid elements follow a common base type:

Occupant

Different types like Mole, Bomb, etc. extend this.

2. Polymorphism

The engine treats all objects the same way:

o.tick();
o.whack();

Each object behaves differently internally.

3. Encapsulation

Game data like score and timer are controlled inside classes and not accessed directly.

4. Separation of Concerns
Game → UI
Engine → logic
Mechanics → game objects
Multithreading

The game loop runs in a separate thread so the UI doesn’t freeze.

UI updates are handled safely using:

SwingUtilities.invokeLater(...)
How to Run
Requirements
Java JDK 8 or higher
Compile
javac game/*.java mechanics/*.java score/*.java exceptions/*.java
Run
java game.Game
Notes
If you change settings, scores won’t be saved (to keep leaderboard fair)
Images must be present in the images folder
The UI is built completely using Swing
What I learned
How to structure a medium-sized Java project
How to use threads properly in a GUI application
How to design reusable classes using OOP
Handling user input and updating UI dynamically
Possible Improvements
Add sound effects
Add animations
Add difficulty levels
Improve UI design
Add more mole types
