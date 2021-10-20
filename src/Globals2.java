import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Globals2 {
    public static ArrayList<String> players_pos = new ArrayList<String>();
    public static ArrayList<Socket> players_sock = new ArrayList<Socket>();
    public static Integer counter = 0;

    public static ArrayList<HashMap<Integer,String>> players_sitting = new ArrayList<HashMap<Integer,String>>();
    public static Poker[] poker_games = new Poker[2];

}