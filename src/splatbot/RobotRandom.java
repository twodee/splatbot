package splatbot;

import splatbot.Action;
import java.util.Random;
import java.util.Arrays;
import splatbot.Cell;
import splatbot.SplatBotColor;

public class RobotRandom {
  private Random generator;
  private SplatBotColor color;

  public RobotRandom(SplatBotColor color) {
    long seed = System.currentTimeMillis();
    /* System.out.println("robot seed: " + seed); */
    generator = new Random(seed);
    this.color = color;
  }

  public Action getAction(Cell left, Cell forward, Cell right) {
    int i = generator.nextInt(100);
    if (i < 30) {
      return Action.MOVE_FORWARD;
    } else if (i < 40) {
      return Action.TURN_LEFT;
    } else if (i < 50) {
      return Action.TURN_RIGHT;
    } else if (i < 60) {
      return Action.SPLAT;
    } else if (i < 70) {
      return Action.SURVEY;
    } else if (i < 80) {
      return Action.MOVE_BACKWARD;
    } else {
      return Action.SPLAT;
    }
  }

  public void survey(Cell[][] window) {
    /* System.out.println(Arrays.deepToString(window)); */
  }
}
