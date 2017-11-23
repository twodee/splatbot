package splatbot;

import splatbot.Action;
import splatbot.Cell;
import splatbot.SplatBotColor;

public class RobotXJ9000 {
  private SplatBotColor color;
  private boolean justMovedBackward;
  private boolean lastTurnWasLeft;
  private int nlefts;
  private int i;

  public RobotXJ9000(SplatBotColor color) {
    this.color = color;
    justMovedBackward = false;
    lastTurnWasLeft = false;
    nlefts = 0;
    i = 0;
  }

  public Action getAction(Cell left, Cell forward, Cell right) {
    ++i;
    if (i == 1 && left != Cell.ROCK) {
      return Action.TURN_LEFT;
    } else if (i == 2) {
      return Action.MOVE_FORWARD;
    } else if (i == 3) {
      return Action.TURN_RIGHT;
    } else if (justMovedBackward) {
      justMovedBackward = false;
      nlefts = 0;
      return Action.TURN_LEFT;
    } else if ((left == Cell.WALL || left == Cell.ROCK) &&
        (forward == Cell.WALL || forward == Cell.ROCK) &&
        (right == Cell.WALL || right == Cell.ROCK)) {
      justMovedBackward = true;
      nlefts = 0;
      return Action.MOVE_BACKWARD;
    } else if (forward != Cell.WALL &&
        forward != Cell.ROCK &&
        forward != Cell.RED_ROBOT &&
        forward != Cell.BLUE_ROBOT &&
        ((forward != Cell.RED && color == SplatBotColor.RED) ||
         (forward != Cell.BLUE && color == SplatBotColor.BLUE))) {
      nlefts = 0;
      return Action.MOVE_FORWARD;
    } else {
      ++nlefts;
      if (nlefts < 4) {
        return Action.TURN_LEFT;
      } else {
        if (forward != Cell.ROCK && forward != Cell.WALL) {
          return Action.MOVE_FORWARD;
        } else if (left != Cell.ROCK && left != Cell.WALL) {
          return Action.TURN_LEFT;
        } else if (right != Cell.ROCK && right != Cell.WALL) {
          return Action.TURN_RIGHT;
        } else {
          return Action.PASS;
        }
      }
    }
  }

  public void survey(Cell[][] window) {
  }
}
