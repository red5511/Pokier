import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Message implements Serializable {
    Poker[] poker_games = {null, null};
    ArrayList<HashMap<Integer,String>> players_sitting;
    ArrayList<HashMap<String,Integer>> players_turn_name;
    ArrayList<HashMap<Integer,Integer>> stacks;

    ArrayList<String> msg;
    String[] name;
    String nick, pass;
    Integer[] pos;
    Integer[] table_index;
    boolean flag_register = false, flag_login = false, login_sucess = false;
    boolean[] flag_leave = {false, false};
    Integer cashier;
    int id;

    Message(){
        id = 0;
        name = new String[2];
        pos = new Integer[2];
        table_index = new Integer[2];
        msg = new ArrayList<String>();

    }

    void append(Poker[] poker_games){
        this.poker_games = poker_games;
    }
    void append(ArrayList<HashMap<Integer,String>> sitting, ArrayList<HashMap<Integer,Integer>> stack){
        this.players_sitting = sitting;
        stacks = stack;

    }
    void append(String name, Integer pos, int table_index){
        this.name[table_index] = name;
        this.pos[table_index] = pos;

    }
    void append(String str, Integer pos){
        flag_leave[pos] = true;
        name[pos] = str;
    }
    void append(ArrayList<HashMap<String,Integer>> turn, String xd){
        players_turn_name = turn;
    }
    void append(Poker game, int index){
        this.poker_games[index] = game;

    }
    void register(String nick, String pass){
        this.nick = nick;
        this.pass = pass;
        flag_register = true;
    }
    void login(String nick, String pass){
        this.nick = nick;
        this.pass = pass;
        flag_login = true;
    }
}
