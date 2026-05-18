package game;

import mechanics.*;
import score.*;
import exceptions.HighScoreException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;



class RoundedButton extends JButton {
    private Color backgroundColor;
    private final int cornerRadius = 25;

    public RoundedButton(String text) {
        super(text);
        setOpaque(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (getModel().isPressed())       g2.setColor(backgroundColor.darker());
        else if (getModel().isRollover()) g2.setColor(backgroundColor.brighter());
        else                              g2.setColor(backgroundColor);

        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));

        g2.setColor(getForeground());
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth()  - fm.stringWidth(getText())) / 2;
        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(getText(), x, y);
        g2.dispose();
    }

    public void setBackgroundColor(Color color) { this.backgroundColor = color; }
}

class RoundedGamePanel extends JPanel {

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int inset = 10, frame = 16, arcOuter = 40, arcInner = 32;
        int x = inset, y = inset;
        int w = getWidth() - inset * 2, h = getHeight() - inset * 2;

        g2.setColor(new Color(42, 61, 79));
        g2.fillRoundRect(x, y, w, h, arcOuter, arcOuter);

        int ix = x + frame, iy = y + frame;
        int iw = w - frame * 2, ih = h - frame * 2;

        g2.setColor(new Color(154, 200, 210));
        g2.fillRoundRect(ix, iy, iw, ih, arcInner, arcInner);

        g2.setColor(new Color(0, 0, 0, 55));
        Stroke old = g2.getStroke();
        g2.setStroke(new BasicStroke(4f));
        g2.drawRoundRect(ix + 2, iy + 2, iw - 4, ih - 4, arcInner - 6, arcInner - 6);
        g2.setStroke(old);

        g2.dispose();
        super.paintComponent(g);
    }

    @Override public boolean isOpaque() { return false; }
}



public class Game extends JFrame {

    // ── Default settings constants ────────────────────────────────────────────
    private static final int DEFAULT_ROWS         = 3;
    private static final int DEFAULT_COLS         = 5;
    private static final int DEFAULT_DURATION     = 45;
    private static final int DEFAULT_SPAWN_CHANCE = 80;

    // ── Current settings (start at defaults) ─────────────────────────────────
    private int settingRows        = DEFAULT_ROWS;
    private int settingCols        = DEFAULT_COLS;
    private int settingDuration    = DEFAULT_DURATION;
    private int settingSpawnChance = DEFAULT_SPAWN_CHANCE;

    /** True whenever any setting differs from the defaults. */
    private boolean isCustomMode() {
        return settingRows        != DEFAULT_ROWS
            || settingCols        != DEFAULT_COLS
            || settingDuration    != DEFAULT_DURATION
            || settingSpawnChance != DEFAULT_SPAWN_CHANCE;
    }

    // ── UI ───────────────────────────────────────────────────────────────────
    private JLabel scoreLabel, highScoreLabel, timerLabel;
    private JLabel customModeBanner;
    private JButton[] buttons;
    private JPanel gridPanel;
    private RoundedGamePanel mainPanel;
    private RoundedButton startButton;

    // ── Overlay ───────────────────────────────────────────────────────────────
    private JPanel overlayPanel;
    private JLabel gameOverScoreLabel, gameOverHighScoreLabel;

    // ── Engine ────────────────────────────────────────────────────────────────
    private Engine engine;
    private Thread thread;

    // ── Scores ────────────────────────────────────────────────────────────────
    private HighScoreManager scoreManager;
    private ArrayList<PlayerScore> highScores;
    private int highScore = 0;

    // ─────────────────────────────────────────────────────────────────────────
    //  Constructor
    // ─────────────────────────────────────────────────────────────────────────

    public Game() {
        setTitle("Whack-A-Mole");
        setUndecorated(true);
        setSize(975, 606);
        setResizable(false);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        scoreManager = new HighScoreManager();
        try {
            highScores = new ArrayList<>(scoreManager.loadScores());
            if (!highScores.isEmpty()) highScore = highScores.get(0).getScore();
        } catch (Exception e) {
            highScores = new ArrayList<>();
        }

        // ── Main panel ────────────────────────────────────────────────────────
        mainPanel = new RoundedGamePanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // ── NORTH: stats bar + custom-mode banner ─────────────────────────────
        JPanel top = new JPanel(new GridLayout(1, 4));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        top.setBackground(new Color(42, 61, 79));

        JLabel titleLabel = makeLabel("Whack-A-Mole", 24, Font.BOLD);
        top.add(titleLabel);

        scoreLabel = makeLabel("Score: 0", 20, Font.PLAIN);
        top.add(scoreLabel);

        highScoreLabel = makeLabel("High Score: " + highScore, 20, Font.PLAIN);
        top.add(highScoreLabel);

        timerLabel = makeLabel("Time: " + settingDuration + "s", 20, Font.PLAIN);
        top.add(timerLabel);

        // Amber banner shown only when custom settings are active
        customModeBanner = new JLabel(
            "\u2699  Custom Mode \u2014 Scores won\u2019t be saved to the leaderboard",
            SwingConstants.CENTER);
        customModeBanner.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 13));
        customModeBanner.setForeground(new Color(42, 61, 79));
        customModeBanner.setBackground(new Color(255, 190, 60));
        customModeBanner.setOpaque(true);
        customModeBanner.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        customModeBanner.setVisible(false);

        JPanel northWrapper = new JPanel();
        northWrapper.setLayout(new BoxLayout(northWrapper, BoxLayout.Y_AXIS));
        northWrapper.setOpaque(false);
        northWrapper.add(top);
        northWrapper.add(customModeBanner);
        mainPanel.add(northWrapper, BorderLayout.NORTH);

        // ── CENTER: grid (built by buildGrid()) ───────────────────────────────
        buildGrid();

        // ── SOUTH: action buttons ─────────────────────────────────────────────
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        bottom.setBackground(new Color(42, 61, 79));

        startButton = makeBtn("Start Game", new Color(110, 162, 72), 150);
        startButton.addActionListener(e -> startGame());
        bottom.add(startButton);

        RoundedButton settingsBtn = makeBtn("Settings", new Color(70, 120, 160), 150);
        settingsBtn.addActionListener(e -> showSettings());
        bottom.add(settingsBtn);

        RoundedButton scoreBtn = makeBtn("High Scores", new Color(120, 120, 120), 150);
        scoreBtn.addActionListener(e -> showScores());
        bottom.add(scoreBtn);

        RoundedButton exitBtn = makeBtn("Exit", new Color(120, 120, 120), 150);
        exitBtn.addActionListener(e -> System.exit(0));
        bottom.add(exitBtn);

        mainPanel.add(bottom, BorderLayout.SOUTH);

        // ── Glass-pane overlay (game over) ────────────────────────────────────
        overlayPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0, 180));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        overlayPanel.setLayout(new GridBagLayout());
        overlayPanel.setOpaque(false);
        overlayPanel.setVisible(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel gameOverTitle = new JLabel("GAME OVER");
        gameOverTitle.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 65));
        gameOverTitle.setForeground(Color.WHITE);
        gbc.gridy = 0; overlayPanel.add(gameOverTitle, gbc);

        gameOverScoreLabel = new JLabel("Your Score: 0");
        gameOverScoreLabel.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 28));
        gameOverScoreLabel.setForeground(Color.WHITE);
        gbc.gridy = 1; overlayPanel.add(gameOverScoreLabel, gbc);

        gameOverHighScoreLabel = new JLabel("High Score: 0");
        gameOverHighScoreLabel.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 28));
        gameOverHighScoreLabel.setForeground(Color.WHITE);
        gbc.gridy = 2; overlayPanel.add(gameOverHighScoreLabel, gbc);

        setGlassPane(overlayPanel);

        // ── Background ────────────────────────────────────────────────────────
        ImageIcon woodBg = new ImageIcon("images/wood_frame.png");
        JLabel background = new JLabel(woodBg);
        background.setLayout(new BorderLayout());
        background.add(mainPanel, BorderLayout.CENTER);
        setContentPane(background);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (engine != null) engine.stop();
                if (thread != null && thread.isAlive()) thread.interrupt();
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Grid
    // ─────────────────────────────────────────────────────────────────────────

  
    private void buildGrid() {
        if (gridPanel != null) mainPanel.remove(gridPanel);

        int gridSize = settingRows * settingCols;
        int hPad = Math.max(20, 110 - settingCols * 10);
        int vPad = Math.max(8,  40  - settingRows * 5);

        gridPanel = new JPanel(new GridLayout(settingRows, settingCols, 18, 18));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(vPad, hPad, vPad, hPad));
        gridPanel.setOpaque(false);

        buttons = new JButton[gridSize];

        for (int i = 0; i < gridSize; i++) {
            final int idx = i;
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(110, 110));
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setOpaque(false);

            ImageIcon holeIcon = Assets.HOLE;
            if (holeIcon != null && holeIcon.getIconWidth() > 0) btn.setIcon(holeIcon);

            btn.addActionListener(e -> handle(idx));
            buttons[i] = btn;
            gridPanel.add(btn);
        }

        mainPanel.add(gridPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Game flow
    // ─────────────────────────────────────────────────────────────────────────

    private void startGame() {
        if (engine != null) engine.stop();
        if (thread != null && thread.isAlive()) thread.interrupt();

        buildGrid();   // rebuilds to match current settings

        engine = new Engine(this, settingRows, settingCols, settingDuration, settingSpawnChance);
        thread = new Thread(engine);
        thread.start();

        scoreLabel.setText("Score: 0");
        timerLabel.setText("Time: " + settingDuration + "s");
        overlayPanel.setVisible(false);

        startButton.setText("Play Again");
        startButton.repaint();
    }

    private void handle(int idx) {
        if (engine != null) engine.handleClick(idx);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Settings dialog
    // ─────────────────────────────────────────────────────────────────────────

    private void showSettings() {
        // Stop the game — grid dimensions may change
        if (engine != null) engine.stop();
        if (thread != null && thread.isAlive()) thread.interrupt();

        JDialog dialog = new JDialog(this, true);
        dialog.setUndecorated(true);
        dialog.setSize(460, 440);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(42, 61, 79));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 32, 20, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(7, 10, 7, 10);

        // Title
        JLabel title = new JLabel("\u2699  Game Settings", SwingConstants.CENTER);
        title.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        // Warning
        JLabel warning = new JLabel(
            "<html><center>\u26A0  Custom settings disable leaderboard saving.<br>"
            + "Use <b>Reset to Defaults</b> to re-enable it.</center></html>",
            SwingConstants.CENTER);
        warning.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 13));
        warning.setForeground(new Color(255, 200, 80));
        gbc.gridy = 1;
        panel.add(warning, gbc);

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 60));
        gbc.gridy = 2; gbc.insets = new Insets(2, 10, 2, 10);
        panel.add(sep, gbc);
        gbc.insets = new Insets(7, 10, 7, 10);

        // Spinners
        SpinnerNumberModel rowsModel     = new SpinnerNumberModel(settingRows,        1,  3,  1);
        SpinnerNumberModel colsModel     = new SpinnerNumberModel(settingCols,        2,  7,  1);
        SpinnerNumberModel durationModel = new SpinnerNumberModel(settingDuration,   15, 120,  5);
        SpinnerNumberModel spawnModel    = new SpinnerNumberModel(settingSpawnChance, 10, 100,  5);

        addSpinnerRow(panel, gbc, 3, "Rows (1 \u2013 3):",            rowsModel);
        addSpinnerRow(panel, gbc, 4, "Columns (2 \u2013 7):",         colsModel);
        addSpinnerRow(panel, gbc, 5, "Duration (15 \u2013 120 s):",   durationModel);
        addSpinnerRow(panel, gbc, 6, "Spawn Chance (10 \u2013 100 %):", spawnModel);

        // Reset to Defaults
        gbc.gridy = 7; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 10, 4, 10);
        RoundedButton resetBtn = makeBtn("\u21BA  Reset to Defaults", new Color(160, 95, 25), 230);
        resetBtn.addActionListener(ev -> {
            rowsModel.setValue(DEFAULT_ROWS);
            colsModel.setValue(DEFAULT_COLS);
            durationModel.setValue(DEFAULT_DURATION);
            spawnModel.setValue(DEFAULT_SPAWN_CHANCE);
        });
        panel.add(resetBtn, gbc);

        // Save / Cancel
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnRow.setOpaque(false);
        RoundedButton saveBtn   = makeBtn("Save",   new Color(110, 162, 72), 120);
        RoundedButton cancelBtn = makeBtn("Cancel", new Color(150, 50, 50),  120);
        btnRow.add(saveBtn);
        btnRow.add(cancelBtn);
        gbc.gridy = 8; gbc.insets = new Insets(4, 10, 8, 10);
        panel.add(btnRow, gbc);

        dialog.add(panel);

        final boolean[] saved = {false};

        saveBtn.addActionListener(ev -> {
            settingRows        = (int) rowsModel.getValue();
            settingCols        = (int) colsModel.getValue();
            settingDuration    = (int) durationModel.getValue();
            settingSpawnChance = (int) spawnModel.getValue();
            saved[0] = true;
            dialog.dispose();
        });
        cancelBtn.addActionListener(ev -> dialog.dispose());

        dialog.setVisible(true);   // blocks until disposed

        if (saved[0]) {
            customModeBanner.setVisible(isCustomMode());
            mainPanel.revalidate();
            mainPanel.repaint();
            // Return to ready state so player hits Start Game
            overlayPanel.setVisible(true);
            startButton.setText("Start Game");
            startButton.repaint();
        }
    }

    /** Adds one label + spinner pair to a GridBagLayout panel. */
    private void addSpinnerRow(JPanel panel, GridBagConstraints gbc,
                               int gridy, String labelText, SpinnerNumberModel model) {
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = gridy;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 15));
        lbl.setForeground(Color.WHITE);
        panel.add(lbl, gbc);

        JSpinner spinner = new JSpinner(model);
        spinner.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 15));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField()
                .setHorizontalAlignment(JTextField.CENTER);
        }
        gbc.gridx = 1;
        panel.add(spinner, gbc);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Engine callbacks
    // ─────────────────────────────────────────────────────────────────────────

    public void updateScore(int score) {
        SwingUtilities.invokeLater(() -> {
            scoreLabel.setText("Score: " + score);
            if (score > highScore) {
                highScore = score;
                highScoreLabel.setText("High Score: " + highScore);
            }
        });
    }

    public void updateTimer(int time) {
        SwingUtilities.invokeLater(() -> timerLabel.setText("Time: " + time + "s"));
    }

    public void updateHole(int index, Occupant o) {
        SwingUtilities.invokeLater(() -> {
            if (index < 0 || index >= buttons.length) return;
            if (o.isVisible() && !(o instanceof EmptyHole)) {
                ImageIcon icon = o.getImage();
                if (icon != null && icon.getIconWidth() > 0) buttons[index].setIcon(icon);
            } else {
                ImageIcon hole = Assets.HOLE;
                buttons[index].setIcon((hole != null && hole.getIconWidth() > 0) ? hole : null);
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Game over
    // ─────────────────────────────────────────────────────────────────────────

    public void gameOver() {
        SwingUtilities.invokeLater(() -> {
            int finalScore = engine.getScore();

            if (isCustomMode()) {
                showCustomGameOverDialog(finalScore);
            } else {
                showNormalGameOverDialog(finalScore);
            }

            gameOverScoreLabel.setText("Your Score: " + finalScore);
            gameOverHighScoreLabel.setText("High Score: " + highScore);
            overlayPanel.setVisible(true);
        });
    }

    /** Custom mode */
    private void showCustomGameOverDialog(int finalScore) {
        JDialog dialog = new JDialog(this, true);
        dialog.setUndecorated(true);
        dialog.setSize(400, 250);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(42, 61, 79));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.insets = new Insets(12, 24, 8, 24);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel t = new JLabel("Game Over", SwingConstants.CENTER);
        t.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 26));
        t.setForeground(Color.WHITE);
        gbc.gridy = 0; panel.add(t, gbc);

        JLabel s = new JLabel("Your Score: " + finalScore, SwingConstants.CENTER);
        s.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 20));
        s.setForeground(new Color(255, 230, 120));
        gbc.gridy = 1; panel.add(s, gbc);

        JLabel note = new JLabel(
            "<html><center>\u2699 Custom Mode \u2014 Score not saved<br>to the leaderboard</center></html>",
            SwingConstants.CENTER);
        note.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        note.setForeground(new Color(255, 170, 70));
        gbc.gridy = 2; panel.add(note, gbc);

        RoundedButton ok = makeBtn("OK", new Color(110, 162, 72), 100);
        ok.addActionListener(ev -> dialog.dispose());
        gbc.gridy = 3; gbc.insets = new Insets(4, 24, 14, 24);
        panel.add(ok, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    /** Normal mode: prompt for name and save to leaderboard. */
    private void showNormalGameOverDialog(int finalScore) {
        JDialog nameDialog = new JDialog(this, true);
        nameDialog.setUndecorated(true);
        nameDialog.setSize(420, 300);
        nameDialog.setResizable(false);
        nameDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(42, 61, 79));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.insets = new Insets(12, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel title = new JLabel("Save Score", SwingConstants.CENTER);
        title.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        gbc.gridy = 0; panel.add(title, gbc);

        JLabel scoreInfo = new JLabel("Your Score: " + finalScore, SwingConstants.CENTER);
        scoreInfo.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 18));
        scoreInfo.setForeground(new Color(255, 230, 120));
        gbc.gridy = 1; panel.add(scoreInfo, gbc);

        JLabel prompt = new JLabel("Enter your name for the leaderboard:", SwingConstants.CENTER);
        prompt.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        prompt.setForeground(Color.WHITE);
        gbc.gridy = 2; panel.add(prompt, gbc);

        JTextField nameField = new JTextField(15);
        nameField.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 16));
        nameField.setHorizontalAlignment(JTextField.CENTER);
        nameField.setBackground(new Color(240, 240, 240));
        gbc.gridy = 3; panel.add(nameField, gbc);

        JPanel buttonRow = new JPanel(new FlowLayout());
        buttonRow.setOpaque(false);
        RoundedButton saveBtn   = makeBtn("Save", new Color(110, 162, 72), 120);
        RoundedButton cancelBtn = makeBtn("Skip", new Color(150, 50, 50),  120);
        buttonRow.add(saveBtn);
        buttonRow.add(cancelBtn);
        gbc.gridy = 4; panel.add(buttonRow, gbc);

        nameDialog.add(panel);

        final String[] enteredName = {null};
        saveBtn.addActionListener(ev -> {
            if (!nameField.getText().trim().isEmpty()) {
                enteredName[0] = nameField.getText().trim();
                nameDialog.dispose();
            }
        });
        cancelBtn.addActionListener(ev -> nameDialog.dispose());
        nameDialog.setVisible(true);

        String name = enteredName[0];
        if (name != null && !name.trim().isEmpty()) {
            highScores.add(new PlayerScore(name.trim(), finalScore));
            highScores.sort((a, b) -> b.getScore() - a.getScore());
            if (highScores.size() > 10)
                highScores = new ArrayList<>(highScores.subList(0, 10));

            try {
                scoreManager.saveScores(highScores);
            } catch (HighScoreException e) {
                JOptionPane.showMessageDialog(this,
                    "Oops! Could not save your score.",
                    "Save Error", JOptionPane.ERROR_MESSAGE);
            }

            if (!highScores.isEmpty()) {
                highScore = highScores.get(0).getScore();
                highScoreLabel.setText("High Score: " + highScore);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  High-scores dialog
    // ─────────────────────────────────────────────────────────────────────────

    private void showScores() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(42, 61, 79));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 20, 20));

        JLabel title = new JLabel("High Scores", SwingConstants.CENTER);
        title.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 30));
        title.setForeground(Color.WHITE);
        panel.add(title, BorderLayout.NORTH);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setOpaque(false);
        area.setFont(new Font("Consolas", Font.PLAIN, 16));
        area.setForeground(Color.WHITE);

        StringBuilder msg = new StringBuilder();
        if (highScores.isEmpty()) {
            msg.append("No scores yet...");
        } else {
            msg.append("--------------------------------------------------\n\n");
            for (int i = 0; i < highScores.size(); i++) {
                PlayerScore ps = highScores.get(i);
                msg.append(String.format("%2d. %-18s %6d\n", i + 1, ps.getName(), ps.getScore()));
            }
        }
        area.setText(msg.toString());

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        panel.add(scroll, BorderLayout.CENTER);

        JDialog dialog = new JDialog(this, "High Scores", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(panel);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Small factory helpers
    // ─────────────────────────────────────────────────────────────────────────

    private JLabel makeLabel(String text, int size, int style) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Arial Rounded MT Bold", style, size));
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    private RoundedButton makeBtn(String text, Color bg, int width) {
        RoundedButton btn = new RoundedButton(text);
        btn.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 18));
        btn.setForeground(Color.WHITE);
        btn.setBackgroundColor(bg);
        btn.setPreferredSize(new Dimension(width, 45));
        return btn;
    }


    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(Game::new);
    }
}