import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class BrickBreakerGame extends JFrame implements ActionListener, KeyListener {
  private String playerName;
  private int ballX;
  private int ballY;
  private int ballSpeedX = 4;
  private int ballSpeedY = 4;
  private int BALL_SIZE = 20;
  private int WINDOW_WIDTH = 500;
  private int WINDOW_HEIGHT = 550;
  private int PADDLE_WIDTH = 80;
  private int PADDLE_HEIGHT = 10;
  private int paddleX;
  private int BRICK_ROWS;
  private int BRICK_COLS;
  private int BRICK_WIDTH = 50;
  private int BRICK_HEIGHT = 20;
  private boolean[][] bricks;
  private BufferedImage buffer;
  private int currentLevel = 1;
  private int pointerCounter = 0;
  private ArrayList<ScoreEntry> scores = new ArrayList<>();
  private boolean bombBallUsed = false;
  private boolean bombBallActive = false;
  private boolean paddleSizePowerUpActive = false;
  private boolean paddleSizePowerUpUsed = false;
  private int bombBallRow = -1;

  public BrickBreakerGame() {
    showOptions();
  }

  private void showOptions() {
    String[] options = { "Play Game", "Show Leaderboard" };
    int choice = JOptionPane.showOptionDialog(null,
        "Choose an option:", "Game Options",
        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
        null, options, options[0]);

    if (choice == 0) {
      getPlayerName();
      showLevelChooserDialog();
    } else if (choice == 1) {
      showLeaderboard();
    } else {
      System.exit(0);
    }
  }

  private void getPlayerName() {
    playerName = JOptionPane.showInputDialog("Enter your name:");
    if (playerName == null || playerName.trim().isEmpty()) {
      System.exit(0);
    }
  }

  private void showLevelChooserDialog() {
    String[] levels = { "Easy", "Medium", "Hard" };
    String selectedLevel = (String) JOptionPane.showInputDialog(
        this,
        "Choose a level:",
        "Level Chooser",
        JOptionPane.QUESTION_MESSAGE,
        null,
        levels,
        levels[0]);

    if (selectedLevel == null) {
      System.exit(0);
    } else {
      if ("Easy".equals(selectedLevel)) {
        setLevelParameters(1);
      } else if ("Medium".equals(selectedLevel)) {
        setLevelParameters(2);
      } else if ("Hard".equals(selectedLevel)) {
        setLevelParameters(3);
      }
    }
  }

  private void setLevelParameters(int level) {
    switch (level) {
      case 1:
        ballSpeedX = 2;
        ballSpeedY = 2;
        BRICK_ROWS = 4;
        BRICK_COLS = 10;
        break;
      case 2:
        ballSpeedX = 4;
        ballSpeedY = 4;
        BRICK_ROWS = 5;
        BRICK_COLS = 12;
        break;
      case 3:
        ballSpeedX = 5;
        ballSpeedY = 5;
        BRICK_ROWS = 6;
        BRICK_COLS = 14;
        break;
    }
    currentLevel = level;
    startGame();
  }

  private void startGame() {
    ballX = WINDOW_WIDTH / 2 - BALL_SIZE / 2;
    ballY = WINDOW_HEIGHT - PADDLE_HEIGHT - BALL_SIZE - 1;
    paddleX = WINDOW_WIDTH / 2 - PADDLE_WIDTH / 2;

    Timer timer = new Timer(10, this);
    timer.start();
    addKeyListener(this);
    setFocusable(true);
    setFocusTraversalKeysEnabled(false);

    initializeBricks();
    buffer = new BufferedImage(WINDOW_WIDTH, WINDOW_HEIGHT, BufferedImage.TYPE_INT_RGB);
  }

  private void initializeBricks() {
    bricks = new boolean[BRICK_ROWS][BRICK_COLS];
    for (int i = 0; i < BRICK_ROWS; i++) {
      for (int j = 0; j < BRICK_COLS; j++) {
        bricks[i][j] = true;
      }
    }
  }

  private void showGameOverScreen() {
    JOptionPane.showMessageDialog(this, "Game Over!\nYour Score: " + pointerCounter, "Sorry!",
        JOptionPane.INFORMATION_MESSAGE);
    saveScoreToFile();
    System.exit(0);
  }

  private void saveScoreToFile() {
    scores.add(new ScoreEntry(playerName, currentLevel, pointerCounter));
    try {
      FileWriter fileWriter = new FileWriter("scores.txt", true);
      BufferedWriter writer = new BufferedWriter(fileWriter);

      for (ScoreEntry entry : scores) {
        writer.write(entry.toString());
        writer.newLine();
      }

      writer.close();
      fileWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void restartGame() {
    pointerCounter = 0; // Reset the score
    setLevelParameters(currentLevel); // Reset the level
    startGame();
  }

  private void showLeaderboard() {
    readScoresFromFile();
    Collections.sort(scores, Comparator.comparing(ScoreEntry::getScore).reversed());
    StringBuilder leaderboard = new StringBuilder("Leaderboard:\n");

    // Display top 3 scores for each difficulty level
    for (int level = 1; level <= 3; level++) {
      leaderboard.append("Difficulty Level ").append(level).append(":\n");
      int count = 0;
      for (ScoreEntry entry : scores) {
        if (entry.getLevel() == level) {
          leaderboard.append("Player: ").append(entry.getPlayerName()).append(", Score: ").append(entry.getScore())
              .append("\n");
          count++;
          if (count == 3) {
            break;
          }
        }
      }
      leaderboard.append("\n");
    }

    System.out.println(leaderboard.toString());
    System.exit(0);
  }

  private void readScoresFromFile() {
    try (BufferedReader reader = new BufferedReader(new FileReader("scores.txt"))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(",");
        if (parts.length == 3) {
          String name = parts[0];
          int level = Integer.parseInt(parts[1]);
          int score = Integer.parseInt(parts[2]);
          scores.add(new ScoreEntry(name, level, score));
        }
      }
    } catch (IOException | NumberFormatException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ballX += ballSpeedX;
    ballY += ballSpeedY;

    if (ballX <= 0 || ballX >= WINDOW_WIDTH - BALL_SIZE) {
      ballSpeedX *= -1;
    }

    if (ballY <= 0) {
      ballSpeedY *= -1;
    }

    if (ballY >= WINDOW_HEIGHT - BALL_SIZE - PADDLE_HEIGHT && ballX >= paddleX && ballX <= paddleX + PADDLE_WIDTH) {
      ballSpeedY *= -1;
    }

    for (int i = 0; i < BRICK_ROWS; i++) {
      for (int j = 0; j < BRICK_COLS; j++) {
        if (bricks[i][j]) {
          int brickX = j * BRICK_WIDTH;
          int brickY = i * BRICK_HEIGHT + 30;
          if (ballX + BALL_SIZE >= brickX && ballX <= brickX + BRICK_WIDTH &&
              ballY + BALL_SIZE >= brickY && ballY <= brickY + BRICK_HEIGHT) {
            if (bombBallActive && i == bombBallRow) {
              // Bomb ball hit, remove the entire row
              for (int k = 0; k < BRICK_COLS; k++) {
                bricks[i][k] = false;
              }
              bombBallActive = false; // Deactivate bomb ball after hitting
              pointerCounter += BRICK_COLS; // Update the score by the number of bricks destroyed in the row
            } else {
              ballSpeedY *= -1;
              bricks[i][j] = false;
              pointerCounter++;

              // Check if all bricks are hit
              if (pointerCounter == BRICK_ROWS * BRICK_COLS) {
                showGameOverScreen();
              }
            }
            break;
          }
        }
      }
    }

    if (ballY >= WINDOW_HEIGHT) {
      showGameOverScreen();
    }

    repaint();
  }

  @Override
  public void paint(Graphics g) {
    Graphics offscreen = buffer.getGraphics();
    super.paint(offscreen);

    offscreen.setColor(Color.BLACK);
    offscreen.drawRect(0, 0, WINDOW_WIDTH - 1, WINDOW_HEIGHT - 1);
    offscreen.fillRect(paddleX, WINDOW_HEIGHT - PADDLE_HEIGHT, PADDLE_WIDTH, PADDLE_HEIGHT);

    offscreen.setColor(Color.RED);
    offscreen.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);
    if (bombBallActive) {
      offscreen.setColor(Color.BLACK);
      offscreen.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);
      offscreen.setColor(Color.GREEN); // Set color back to normal
    }
    offscreen.setColor(Color.GREEN);
    for (int i = 0; i < BRICK_ROWS; i++) {
      for (int j = 0; j < BRICK_COLS; j++) {
        if (bricks[i][j]) {
          int brickX = j * BRICK_WIDTH;
          int brickY = i * BRICK_HEIGHT + 30;
          offscreen.fillRect(brickX, brickY, BRICK_WIDTH, BRICK_HEIGHT);
          offscreen.setColor(Color.BLACK);
          offscreen.drawRect(brickX, brickY, BRICK_WIDTH, BRICK_HEIGHT);
          offscreen.setColor(Color.GREEN);
        }
      }
    }

    // Draw the offscreen buffer onto the JFrame
    g.drawImage(buffer, 0, 0, this);
  }

  public void keyPressed(KeyEvent e) {
    int key = e.getKeyCode();
    if (key == KeyEvent.VK_LEFT && paddleX > 0) {
      paddleX -= 20;
    } else if (key == KeyEvent.VK_RIGHT && paddleX < WINDOW_WIDTH - PADDLE_WIDTH) {
      paddleX += 20;
    } else if (key == KeyEvent.VK_SPACE && !bombBallUsed) {
      bombBallUsed = true;
      activateBombBall();
      pointerCounter += BRICK_COLS; // Update the score by the number of bricks destroyed in the row
    } else if (key == KeyEvent.VK_SHIFT && !paddleSizePowerUpUsed) {
      paddleSizePowerUpUsed = true;
      increasePaddleSize();
      Timer powerUpTimer = new Timer(5000, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          paddleSizePowerUpUsed = true;
          decreasePaddleSize();
        }
      });
      powerUpTimer.setRepeats(false);
      powerUpTimer.start();
    }
  }

  @Override
  public void keyTyped(KeyEvent e) {
  }

  @Override
  public void keyReleased(KeyEvent e) {
  }

  private void activateBombBall() {
    // Find the lowest row with at least one brick
    for (int i = BRICK_ROWS - 1; i >= 0; i--) {
      for (int j = 0; j < BRICK_COLS; j++) {
        if (bricks[i][j]) {
          bombBallRow = i;
          bombBallActive = true;
          return;
        }
      }
    }
  }

  private static class ScoreEntry {
    private String playerName;
    private int level;
    private int score;

    public ScoreEntry(String playerName, int level, int score) {
      this.playerName = playerName;
      this.level = level;
      this.score = score;
    }

    public String getPlayerName() {
      return playerName;
    }

    public int getLevel() {
      return level;
    }

    public int getScore() {
      return score;
    }

    @Override
    public String toString() {
      return playerName + "," + level + "," + score;
    }
  }

  private void increasePaddleSize() {
    PADDLE_WIDTH *= 2;
  }

  private void decreasePaddleSize() {
    PADDLE_WIDTH /= 2;
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      BrickBreakerGame game = new BrickBreakerGame();
      game.setTitle("Brick Breaker Game");
      game.setSize(500, 550);
      game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      game.setLocationRelativeTo(null);
      game.setResizable(false);
      game.setVisible(true);
    });
  }
}