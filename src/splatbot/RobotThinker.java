package splatbot;

import splatbot.Action;
import splatbot.SplatBotColor;
import splatbot.Cell;
import splatbot.Splatter;

public class RobotThinker {
  public RobotThinker(SplatBotColor color) {
    // hold on to it, if strategy calls for that... 
  }  

  private int i;
  public Action getAction(Cell left, Cell forward, Cell right) {
    ++i;
    if (i % 2 == 0) {
      return Action.MOVE_FORWARD;
    } else {
      return Action.PASS;
    }
  }

  public void survey(Cell[][] board) {

  }

  public static void main(String[] args) {
    new Splatter(RobotThinker.class, RobotThinker.class, 100, 500);
  }
}
