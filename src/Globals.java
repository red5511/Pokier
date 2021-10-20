import java.net.Socket;
import java.util.*;

public class Globals {
    public static ArrayList<String> players_pos = new ArrayList<String>();
    public static ArrayList<Socket> players_sock = new ArrayList<Socket>();
    public static Integer counter = 0;

    public static ArrayList<HashMap<Integer,String>> players_sitting = new ArrayList<HashMap<Integer,String>>();
    public static Poker[] poker_games = new Poker[2];
    public static int[] players_turn = {0 , 0};
    //public static HashMap<String,Integer>[] players_turn_name = new HashMap<String,Integer>[2];
    public static ArrayList<HashMap<String,Integer>> players_turn_name = new ArrayList<HashMap<String,Integer>>();
    public static ArrayList<HashMap<String,Integer>> bufor_players_turn_name = new ArrayList<HashMap<String,Integer>>();
    public static ArrayList<HashMap<Integer,Integer>> stacks = new ArrayList<HashMap<Integer,Integer>>();
    public static HashMap<String,Integer> cashier_flow = new HashMap<String,Integer>();







    //ArrayList<String>[] players_turn_name = new ArrayList[2];


}