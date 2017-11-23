package splatbot;

import splatbot.Action;
import splatbot.Cell;
import splatbot.SplatBotColor;

public class RobotForward {
  private SplatBotColor color;

  public RobotForward(SplatBotColor color) {
    this.color = color;
  }

  private int i = 0;
  public Action getAction(Cell left, Cell forward, Cell right) {
    return Action.MOVE_FORWARD;
    /* ++i; */
    /* if (i % 10 == 5) { */
      /* return Action.SPLAT; */
    /* } else { */
      /* return Action.PASS; */
      /* return Action.SURVEY; */
    /* } */
  }

  public void survey(Cell[][] window) {
  }
}
