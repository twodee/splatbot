package splatbot;

public class Runner {
  public static void main(String[] args) throws ClassNotFoundException {
    int delay = Integer.parseInt(args[0]);
    Class<?> redClass = Class.forName(args[1]);
    Class<?> blueClass = Class.forName(args[2]);
    new Splatter(redClass, blueClass, delay, 500);
  }  
}
