package splatbot;

import java.util.Random;
import java.util.ArrayList;
import splatbot.Action;
import splatbot.Cell;
import splatbot.SplatBotColor;

public class RobotXJ9000 {
  private SplatBotColor color;
  private SplatBotColor notColor;
  private boolean justMovedBackward;
  private boolean lastTurnWasLeft;
  private boolean justSurveyed;
  private Cell[][] lastSurvey;
  private int nlefts;
  private int i;
  private ArrayList<Action> actions;
  private Random generator;
  private boolean isRockFree;
  private boolean justSplatted;

  public RobotXJ9000(SplatBotColor color) {
    this.color = color;
    notColor = color == SplatBotColor.RED ? SplatBotColor.BLUE : SplatBotColor.RED;
    justMovedBackward = false;
    lastTurnWasLeft = false;
    justSurveyed = false;
    isRockFree = true;
    justSplatted = false;
    nlefts = 0;
    i = 0;
    actions = new ArrayList<>();
    generator = new Random();
  }

  // ABCBCABCAABCA
  public boolean isCycling() {
    // walk backward from end
    // if I run into the terminal element, see if [i + 1, -1] also appears here
    // require that suffix some turns in it and at least 5 elements?
    // stop at half
    for (int i = actions.size() - 20; i >= actions.size() / 2; --i) {
      boolean isSuffixHereToo = true;
      for (int j = 0; isSuffixHereToo && j < actions.size() - i; ++j) {
        isSuffixHereToo = actions.get(i - j) == actions.get(actions.size() - 1 - j);
      }
      if (isSuffixHereToo) {
        return true;
      }
    }
    return false;
  }

  public Action getAction(Cell left, Cell forward, Cell right) {
    Action action = getActionHelper(left, forward, right);
    actions.add(action);
    return action;
  }

  private boolean isMine(Cell cell) {
    return (cell == Cell.RED && color == SplatBotColor.RED) ||
           (cell == Cell.BLUE && color == SplatBotColor.BLUE);
  }

  private boolean isTheirs(Cell cell) {
    return (cell == Cell.BLUE && color == SplatBotColor.RED) ||
           (cell == Cell.RED && color == SplatBotColor.BLUE);
  }

  private boolean isObstacle(Cell cell) {
    return cell == Cell.ROCK || cell == Cell.WALL;
  }

  private Action left() {
    justSplatted = false;
    justMovedBackward = false;
    return Action.TURN_LEFT;
  }

  private Action right() {
    justSplatted = false;
    justMovedBackward = false;
    return Action.TURN_RIGHT;
  }

  private Action forward() {
    justSplatted = false;
    justMovedBackward = false;
    return Action.MOVE_FORWARD;
  }

  private Action backward() {
    justSplatted = false;
    justMovedBackward = true;
    return Action.MOVE_BACKWARD;
  }

  private Action splat() {
    justSplatted = true;
    justMovedBackward = false;
    return Action.SPLAT;
  }

  private Action getActionHelper(Cell left, Cell forward, Cell right) {
    if (justMovedBackward) {
      justMovedBackward = false;
    }

    ++i;
    if (i == 1) {
      return Action.SURVEY;
    } else if (isRockFree && i == 2 && left != Cell.ROCK) {
      return left();
    } else if (isRockFree && i == 3) {
      return splat();
    } else if (isRockFree && i == 4) {
      return right();
    } else if (isCycling()) {
      switch (generator.nextInt(4)) {
        case 0:
          return left();
        case 1:
          return right();
        case 2:
          return forward();
        default:
          return backward();
      }
    } else if (isObstacle(left) && isObstacle(forward) && isObstacle(right)) {
      nlefts = 0;
      return backward();
    } else if ((isMine(forward) || isObstacle(forward)) && isTheirs(left)) {
      return left();
    } else if ((isMine(forward) || isObstacle(forward)) && isTheirs(right)) {
      return right();
    } else if ((isMine(forward) || isObstacle(forward)) && left == Cell.NEUTRAL) {
      return left();
    } else if ((isMine(forward) || isObstacle(forward)) && right == Cell.NEUTRAL) {
      return right();
    } else if (forward != Cell.WALL &&
               forward != Cell.ROCK &&
               forward != Cell.RED_ROBOT &&
               forward != Cell.BLUE_ROBOT &&
               !isMine(forward)) {
      nlefts = 0;
      return forward();
    } else if (!justSplatted && (forward == Cell.RED_ROBOT || forward == Cell.BLUE_ROBOT)) {
      return splat();
    } else if (isMine(forward) && left == Cell.NEUTRAL) {
      return left();
    } else if (isMine(forward) && right == Cell.NEUTRAL) {
      return right();
    } else {
      ++nlefts;
      if (nlefts < 2) {
        return left();
      } else {
        if (forward != Cell.ROCK && forward != Cell.WALL) {
          if (forward == Cell.RED_ROBOT || forward == Cell.BLUE_ROBOT) {
            if (!isObstacle(left) && !isObstacle(right)) {
              if (generator.nextBoolean()) {
                return left();
              } else {
                return right();
              }
            } else if (isObstacle(right)) {
              return left();
            } else {
              return right();
            }
          } else {
            return forward();
          }
        } else if (left != Cell.ROCK && left != Cell.WALL) {
          return left();
        } else if (right != Cell.ROCK && right != Cell.WALL) {
          return right();
        } else {
          return Action.PASS;
        }
      }
    }
  }

  public void survey(Cell[][] window) {
    lastSurvey = window;
    for (int i = 0; i < window[0].length; ++i) {
      if (window[0][i] == Cell.ROCK || window[i][window.length - 1] == Cell.ROCK) {
        isRockFree = false;
      }
    }
  }
}
