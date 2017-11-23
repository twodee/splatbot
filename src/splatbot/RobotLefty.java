package splatbot;

import splatbot.Action;
import splatbot.Cell;
import splatbot.SplatBotColor;

public class RobotLefty {
  private SplatBotColor color;
  private boolean justMovedBackward;
  private boolean lastTurnWasLeft;
  private int nLefts;

  public RobotLefty(SplatBotColor color) {
    this.color = color;
    justMovedBackward = false;
    lastTurnWasLeft = false;
    nLefts = 0;
  }

  public Action getAction(Cell left, Cell forward, Cell right) {
    if (justMovedBackward) {
      justMovedBackward = false;
      nLefts = 0;
      return Action.TURN_LEFT;
    } else if ((left == Cell.WALL || left == Cell.ROCK) &&
        (forward == Cell.WALL || forward == Cell.ROCK) &&
        (right == Cell.WALL || right == Cell.ROCK)) {
      justMovedBackward = true;
      nLefts = 0;
      return Action.MOVE_BACKWARD;
    } else if (forward != Cell.WALL &&
        forward != Cell.ROCK &&
        forward != Cell.RED_ROBOT &&
        forward != Cell.BLUE_ROBOT &&
        ((forward != Cell.RED && color == SplatBotColor.RED) ||
         (forward != Cell.BLUE && color == SplatBotColor.BLUE))) {
      nLefts = 0;
      return Action.MOVE_FORWARD;
    } else {
      ++nLefts;
      if (nLefts > 4 && forward != Cell.ROCK && forward != Cell.WALL) {
        return Action.MOVE_FORWARD;
      } else {
        return Action.TURN_LEFT;
      }
    }
  }

  public void survey(Cell[][] window) {
  }
}
