package splatbot;

import java.io.InputStream;
import java.util.Arrays;
import javax.swing.JOptionPane;
import java.lang.reflect.Constructor;
import java.awt.Font;
import javax.swing.JLabel;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.Timer;
import java.awt.FlowLayout;
import java.util.Random;
import java.lang.reflect.InvocationTargetException;
import java.awt.BorderLayout;
import javax.swing.JButton;
import java.lang.reflect.Method;
import java.awt.Graphics;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.Color;
import java.util.Iterator;
import java.util.ArrayList;

public class Splatter extends JFrame {
  private int iTurn;
  private Class<?>[] botClasses;
  private SplatBotWrapper[] bots;
  private ArrayList<Splat> splats;
  private Board board;
  private SplatterPanel panel;
  private boolean isRunning;
  private Timer timer;
  private BufferedImage[] botImages;
  private BufferedImage[] splatImages;
  private BufferedImage rockImage;;
  private JLabel redLabel;
  private JLabel blueLabel;
  private JLabel turnLabel;
  private JButton runButton;
  private JButton stepButton;
  private int delay;
  private int nTurns;

  public Splatter(Class<?> bot1, Class<?> bot2, int delayMillis, int nTurns) {
    super("Splatter");
    this.delay = delayMillis;
    this.nTurns = nTurns;
    bots = new SplatBotWrapper[2];
    botClasses = new Class<?>[2];
    botClasses[0] = bot1;
    botClasses[1] = bot2;

    redLabel = new JLabel();
    blueLabel = new JLabel();
    turnLabel = new JLabel();
    stepButton = new JButton("Step");
    runButton = new JButton("Run");
    reset();

    botImages = new BufferedImage[2];
    splatImages = new BufferedImage[2];
    try {
      InputStream in = Splatter.class.getResourceAsStream("/images/red.png");
      botImages[0] = ImageIO.read(in);
      in.close();

      in = Splatter.class.getResourceAsStream("/images/blue.png");
      botImages[1] = ImageIO.read(in);
      in.close();

      in = Splatter.class.getResourceAsStream("/images/red_splat.png");
      splatImages[0] = ImageIO.read(in);
      in.close();

      in = Splatter.class.getResourceAsStream("/images/blue_splat.png");
      splatImages[1] = ImageIO.read(in);
      in.close();

      in = Splatter.class.getResourceAsStream("/images/rock.png");
      rockImage = ImageIO.read(in);
      in.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    panel = new SplatterPanel();
    add(panel);

    stepButton.addActionListener(e -> tick());

    timer = new Timer(delay, e -> tick());

    runButton.addActionListener(e -> {
      if (isRunning) {
        // stop
        runButton.setText("Run");
        timer.stop();        
      } else {
        runButton.setText("Pause");
        timer.start();        
      }
      isRunning = !isRunning;
    });

    final JButton newGameButton = new JButton("New Game");
    newGameButton.addActionListener(e -> {
      runButton.setText("Run");
      timer.stop();
      reset();
      repaint();
    });

    redLabel.setForeground(Color.RED);
    blueLabel.setForeground(Color.BLUE);

    Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 24);
    redLabel.setFont(font);
    blueLabel.setFont(font);

    JPanel buttonPanel = new JPanel(new FlowLayout());
    buttonPanel.add(redLabel);
    buttonPanel.add(stepButton);
    buttonPanel.add(runButton);
    buttonPanel.add(newGameButton);
    buttonPanel.add(blueLabel);
    buttonPanel.add(turnLabel);

    add(buttonPanel, BorderLayout.SOUTH);

    setDefaultCloseOperation(EXIT_ON_CLOSE);
    pack();
    setVisible(true);
  }

  private void reset() {
    iTurn = 0;
    SplatBotColor[] colors = {SplatBotColor.RED, SplatBotColor.BLUE};
    int[][] locations = {
      new int[]{0, 0},
      new int[]{Board.NCOLUMNS - 1, Board.NROWS - 1}
    };
    int[][] directions = {
      new int[]{0, 1},
      new int[]{0, -1}
    };

    for (int i = 0; i < 2; ++i) {
      try {
        Constructor ctor = botClasses[i].getDeclaredConstructor(SplatBotColor.class);
        Object bot = ctor.newInstance(colors[i]);
        bots[i] = new SplatBotWrapper(bot, colors[i], locations[i], directions[i]);
      } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }

    board = new Board(5);
    splats = new ArrayList<>();
    isRunning = false;
    redLabel.setText("" + board.getRedCount());
    blueLabel.setText("" + board.getBlueCount());
    turnLabel.setText(iTurn + "/" + nTurns);
    runButton.setEnabled(true);
    stepButton.setEnabled(true);

    board.absorb(bots[0]);
    board.absorb(bots[1]);
  }

  class SplatterPanel extends JPanel {
    public static final int TILE_SIZE = 50;
    public static final int GAP = 1;

    public SplatterPanel() {
      setPreferredSize(new Dimension(Board.NCOLUMNS * TILE_SIZE, Board.NROWS * TILE_SIZE));
      setMinimumSize(getPreferredSize());
    }

    @Override
    public void paintComponent(Graphics g) {
      // Draw background tiles
      for (int r = 0; r < Board.NROWS; ++r) {
        for (int c = 0; c < Board.NCOLUMNS; ++c) {
          switch (board.getCellType(c, r)) {
            case RED:
              g.setColor(new Color(255, 150, 150));
              g.fillRect(c * TILE_SIZE + GAP, r * TILE_SIZE + GAP, TILE_SIZE - 2 * GAP, TILE_SIZE - 2 * GAP);
              break;
            case BLUE:
              g.setColor(new Color(150, 150, 255));
              g.fillRect(c * TILE_SIZE + GAP, r * TILE_SIZE + GAP, TILE_SIZE - 2 * GAP, TILE_SIZE - 2 * GAP);
              break;
            case NEUTRAL:
              g.setColor(Color.LIGHT_GRAY);
              g.fillRect(c * TILE_SIZE + GAP, r * TILE_SIZE + GAP, TILE_SIZE - 2 * GAP, TILE_SIZE - 2 * GAP);
              break;
            case ROCK:
              g.setColor(Color.LIGHT_GRAY);
              g.fillRect(c * TILE_SIZE + GAP, r * TILE_SIZE + GAP, TILE_SIZE - 2 * GAP, TILE_SIZE - 2 * GAP);
              g.drawImage(rockImage,
                          c * TILE_SIZE + GAP, r * TILE_SIZE * GAP, (c + 1) * TILE_SIZE - GAP, (r + 1) * TILE_SIZE - GAP,
                          0, 0, rockImage.getWidth(), rockImage.getHeight(), null);
              break;
          }
        }
      }

      Graphics2D g2 = (Graphics2D) g;

      // Draw splats
      for (Splat splat : splats) {
        BufferedImage splatImage = splat.getColor() == SplatBotColor.RED ? splatImages[0] : splatImages[1];
        g2.drawImage(splatImage,
                     splat.getX() * TILE_SIZE + GAP,
                     splat.getY() * TILE_SIZE + GAP,
                     (splat.getX() + 1) * TILE_SIZE - GAP,
                     (splat.getY() + 1) * TILE_SIZE - GAP,
                     0,
                     0,
                     splatImage.getWidth(),
                     splatImage.getHeight(),
                     null);
      }

      // Draw robots
      for (int i = 0; i < 2; ++i) {
        AffineTransform oldTransform = g2.getTransform();

        g2.translate(bots[i].getX() * TILE_SIZE + TILE_SIZE / 2, bots[i].getY() * TILE_SIZE + TILE_SIZE / 2);

        if (bots[i].getDirectionX() == 0 && bots[i].getDirectionY() == 1) {
          g2.rotate(Math.PI);
        } else if (bots[i].getDirectionX() == 0 && bots[i].getDirectionY() == -1) {
          // natural direction
        } else if (bots[i].getDirectionX() == -1 && bots[i].getDirectionY() == 0) {
          g2.rotate(Math.PI * -0.5);
        } else if (bots[i].getDirectionX() == 1 && bots[i].getDirectionY() == 0) {
          g2.rotate(Math.PI * 0.5);
        }

        g.drawImage(botImages[i],
                    -TILE_SIZE / 2 + GAP,
                    -TILE_SIZE / 2 + GAP,
                    TILE_SIZE / 2 - GAP,
                    TILE_SIZE / 2 - GAP,
                    0,
                    0,
                    botImages[i].getWidth(),
                    botImages[i].getHeight(),
                    null);

        g2.setTransform(oldTransform);
      }
    }
  }

  void tick() {

    // Handle robot actions.
    Action[] actions = {
      bots[0].getAction(board),
      bots[1].getAction(board)
    };

    if ((actions[0] == Action.MOVE_FORWARD || actions[0] == Action.MOVE_BACKWARD) &&
        (actions[1] == Action.MOVE_FORWARD || actions[1] == Action.MOVE_BACKWARD) &&
        Arrays.equals(bots[0].getNextLocation(), bots[1].getNextLocation())) {
      // Don't do anything when the robots try to move to the same location.
    } else {
      for (int i = 0; i < 2; ++i) {
        SplatBotWrapper thisBot = bots[i]; 
        SplatBotWrapper thatBot = bots[(i + 1) % 2]; 

        switch (actions[i]) {
          case MOVE_FORWARD:
            {
              int[] newLocation = thisBot.getNextLocation();
              if (board.isMovableLocation(newLocation) && (newLocation[0] != thatBot.getX() || newLocation[1] != thatBot.getY())) {
                thisBot.move();
                Iterator<Splat> isplat = splats.iterator(); 
                while (isplat.hasNext()) {
                  Splat splat = isplat.next();
                  if (splat.collides(thisBot) && splat.getColor() != thisBot.getTrueColor()) {
                    thisBot.setColor(thatBot.getTrueColor());
                    isplat.remove();
                  }
                }
              }
            }
            break;
          case MOVE_BACKWARD:
            {
              int[] newLocation = thisBot.getNextBackwardLocation();
              if (board.isMovableLocation(newLocation) && (newLocation[0] != thatBot.getX() || newLocation[1] != thatBot.getY())) {
                thisBot.moveBackward();
                Iterator<Splat> isplat = splats.iterator(); 
                while (isplat.hasNext()) {
                  Splat splat = isplat.next();
                  if (splat.collides(thisBot) && splat.getColor() != thisBot.getTrueColor()) {
                    thisBot.setColor(thatBot.getTrueColor());
                    isplat.remove();
                  }
                }
              }
            }
            break;
          case TURN_LEFT:
            thisBot.turnLeft();
            break;
          case TURN_RIGHT:
            thisBot.turnRight();
            break;
          case PASS:
            break;
          case SPLAT:
            if (thisBot.canSplat()) {
              splats.add(thisBot.splat());
            }
            break;
          case SURVEY:
            thisBot.survey(board);
            break;
        }
        board.absorb(thisBot);
        thisBot.tick();
      }

      redLabel.setText("" + board.getRedCount());
      blueLabel.setText("" + board.getBlueCount());
    }
  
    // Handle splats.
    Iterator<Splat> isplat = splats.iterator(); 
    while (isplat.hasNext()) {
      Splat splat = isplat.next();
      splat.move();
      if (!board.isMovableLocation(splat)) {
        isplat.remove();
      } else if (splat.collides(bots[0])) {
        bots[0].setColor(bots[1].getTrueColor());
      } else if (splat.collides(bots[1])) {
        bots[1].setColor(bots[0].getTrueColor());
      }
    }

    repaint();

    ++iTurn;
    turnLabel.setText(iTurn + "/" + nTurns);
    if (iTurn == nTurns) {
      runButton.setEnabled(false);
      stepButton.setEnabled(false);
      timer.stop();

      String message;
      if (board.getRedCount() > board.getBlueCount()) {
        message = "Red";
      } else if (board.getBlueCount() > board.getRedCount()) {
        message = "Blue";
      } else {
        message = "No one";
      }
      JOptionPane.showMessageDialog(null, message + " wins!");
    }
  }

  class Board {
    private Cell[][] board;

    public static final int NROWS = 10;
    public static final int NCOLUMNS = 10;

    public Board(int nrocks) {
      board = new Cell[NROWS][NCOLUMNS];
      for (int r = 0; r < NROWS; ++r) {
        for (int c = 0; c < NCOLUMNS; ++c) {
          board[r][c] = Cell.NEUTRAL;
        }
      }

      // Randomly place rocks.
      /* nrocks = 0; */
      long seed = System.currentTimeMillis();
      /* System.out.println("seed: " + seed); */
      Random generator = new Random(seed);
      for (int i = 0; i < 7; ++i) {
        int r = generator.nextInt(NROWS);
        int c = generator.nextInt(NCOLUMNS);
        if ((r == 0 && c == 0) ||
            (r == NROWS - 1 && c == NCOLUMNS - 1) ||
            board[r][c] == Cell.ROCK) {
          --i;
        } else {
          board[r][c] = Cell.ROCK;
        }
      }
    }

    public Cell[][] clone() {
      Cell[][] cells = new Cell[NROWS][NCOLUMNS];
      for (int r = 0; r < NROWS; ++r) {
        for (int c = 0; c < NCOLUMNS; ++c) {
          cells[r][c] = getCell(c, r);
        }
      }
      return cells;
    }

    public boolean isMovableLocation(Movable thing) {
      return isMovableLocation(new int[]{thing.getX(), thing.getY()});
    }

    public boolean isMovableLocation(int[] location) {
      return location[0] >= 0 && location[1] >= 0 && location[0] < NCOLUMNS && location[1] < NROWS && board[location[1]][location[0]] != Cell.ROCK;
    }

    private int nReds;
    private int nBlues;

    public int getRedCount() {
      return nReds;
    }

    public int getBlueCount() {
      return nBlues;
    }

    public void absorb(SplatBotWrapper bot) {
      if (isMovableLocation(bot)) {
        Cell cell = board[bot.getY()][bot.getX()];
        if (bot.getCurrentCellColor() == cell) {
          // no change
        } else {
          if (cell == Cell.NEUTRAL) {
            if (bot.getCurrentCellColor() == Cell.RED) {
              ++nReds;
            } else {
              ++nBlues;
            }
          } else if (cell == Cell.RED && bot.getCurrentCellColor() == Cell.BLUE) {
            --nReds;
            ++nBlues;
          } else if (cell == Cell.BLUE && bot.getCurrentCellColor() == Cell.RED) {
            ++nReds;
            --nBlues;
          } else {
            System.out.println("uh oh");
          }
          board[bot.getY()][bot.getX()] = bot.getCurrentCellColor();
        }
      }
    }

    public Cell getCell(int c, int r) {
      if (c < 0 || c >= NCOLUMNS || r < 0 || r >= NROWS) {
        return Cell.WALL;
      } else if (c == bots[0].getX() && r == bots[0].getY()) {
        return Cell.RED_ROBOT;
      } else if (c == bots[1].getX() && r == bots[1].getY()) {
        return Cell.BLUE_ROBOT;
      } else {
        return board[r][c];
      }
    }

    public Cell getCellType(int c, int r) {
      if (c < 0 || c >= NCOLUMNS || r < 0 || r >= NROWS) {
        return Cell.WALL;
      } else {
        return board[r][c];
      }
    }

    public boolean isRed(int c, int r) {
      return board[r][c] == Cell.RED;
    }

    public boolean isBlue(int c, int r) {
      return board[r][c] == Cell.BLUE;
    }
  }

  class SplatBotWrapper extends Movable {
    private Object bot;
    private SplatBotColor currentColor;
    private SplatBotColor trueColor;
    private int nFramesTillClean;
    private int nFramesTillSurvey;
    private int nFramesTillSplat;
    private Method getActionMethod;
    private Method surveyMethod;

    public static final int HIT_PENALTY = 10;
    public static final int SURVEY_PENALTY = 5;
    public static final int SPLAT_PENALTY = 5;

    public SplatBotWrapper(Object bot, SplatBotColor color, int[] location, int[] direction) {
      super(location, direction);
      this.bot = bot;
      currentColor = color;
      trueColor = color;
      nFramesTillClean = 0;
      nFramesTillSurvey = 0;
      nFramesTillSplat = 0;

      try {
        getActionMethod = bot.getClass().getMethod("getAction", Cell.class, Cell.class, Cell.class);
        surveyMethod = bot.getClass().getMethod("survey", Cell[][].class);
      } catch (NoSuchMethodException | SecurityException e) {
        throw new RuntimeException(e);
      }
    }

    public Splat splat() {
      nFramesTillSplat = SPLAT_PENALTY;
      return new Splat(new int[]{getX(), getY()}, new int[]{getDirectionX(), getDirectionY()}, getCurrentColor());
    }

    public boolean isTrueColor() {
      return currentColor.equals(trueColor);
    }

    public SplatBotColor getCurrentColor() {
      return currentColor;
    }

    public Cell getCurrentCellColor() {
      return getCurrentColor() == SplatBotColor.RED ? Cell.RED : Cell.BLUE;
    }

    public SplatBotColor getTrueColor() {
      return trueColor;
    }

    public void setColor(SplatBotColor color) {
      currentColor = color;
      nFramesTillClean = HIT_PENALTY;
    }

    public boolean canSplat() {
      return nFramesTillSplat == 0;
    }

    public void tick() {
      if (nFramesTillClean > 0) {
        --nFramesTillClean;
        if (nFramesTillClean == 0) {
          currentColor = trueColor;
        } 
      }

      if (nFramesTillSurvey > 0) {
        --nFramesTillSurvey;
      }

      if (nFramesTillSplat > 0) {
        --nFramesTillSplat;
      }
    }

    public Action getAction(Board board) {
      try {
        return (Action) getActionMethod.invoke(bot,
                                               board.getCell(getX() + getDirectionY(), getY() - getDirectionX()),
                                               board.getCell(getX() + getDirectionX(), getY() + getDirectionY()),
                                               board.getCell(getX() - getDirectionY(), getY() + getDirectionX()));
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }

    public void survey(Board board) {
      if (nFramesTillSurvey == 0) {
        nFramesTillSurvey = SURVEY_PENALTY;
        try {
          surveyMethod.invoke(bot, (Object) board.clone());
        } catch (IllegalAccessException | InvocationTargetException e) {
          throw new RuntimeException(e);
        }
      }
    }

    public String toString() {
      return String.format("[%d %d] -> [%d %d]", getX(), getY(), getDirectionX(), getDirectionY());
    }
  }

  class Movable {
    private int[] location;
    private int[] direction;

    public Movable(int[] location, int[] direction) {
      this.location = location;
      this.direction = direction;
    }

    public void move() {
      location[0] += getDirectionX();
      location[1] += getDirectionY();
    }

    public void moveBackward() {
      location[0] -= getDirectionX();
      location[1] -= getDirectionY();
    }

    public int[] getNextLocation() {
      return new int[]{location[0] + getDirectionX(), location[1] + getDirectionY()};
    }

    public int[] getNextBackwardLocation() {
      return new int[]{location[0] - getDirectionX(), location[1] - getDirectionY()};
    }

    public int getDirectionX() {
      return direction[0];
    }

    public int getDirectionY() {
      return direction[1];
    }

    public int getX() {
      return location[0];
    }

    public int getY() {
      return location[1];
    }

    public boolean collides(Movable that) {
      return getX() == that.getX() && getY() == that.getY();
    }

    public void turnLeft() {
      int tmpX = direction[0];
      int tmpY = direction[1];
      direction[0] = tmpY;
      direction[1] = -tmpX;
    }

    public void turnRight() {
      int tmpX = direction[0];
      int tmpY = direction[1];
      direction[0] = -tmpY;
      direction[1] = tmpX;
    }
  }

  class Splat extends Movable {
    private SplatBotColor color;

    public Splat(int[] location, int[] direction, SplatBotColor color) {
      super(location, direction);
      this.color = color;
    }

    public SplatBotColor getColor() {
      return color;
    }
  }
}
