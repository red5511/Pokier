import java.io.Serializable;
import java.net.Socket;
import java.util.*;

public class Poker implements Serializable {
    String card1, card2, card3, card4, card5, card6, card7, enemy_card1, enemy_card2, who_wins = "None";
    int my_table_pos = -1, level, villian_stack, my_postion, index_deck = 0, player_turn;
    Integer pot, flop_pot, turn_pot, river_pot;
    ArrayList<String> board = new ArrayList<String>();
    //ArrayList<String> players = new ArrayList<String>();
    HashMap<Integer,String> hands = new HashMap<Integer,String>();
    HashMap<Integer,String> players;
    HashMap<Integer,Integer> stacks;
    String[] deck_default  = {"2h", "2d", "2s", "2c", "3h", "3d", "3s", "3c", "4h", "4d", "4s", "4c", "5h", "5d", "5s", "5c", "6h", "6d", "6s", "6c", "7h", "7d", "7s", "7c", "8h", "8d", "8s", "8c", "9h", "9d", "9s", "9c", "Th", "Td", "Ts", "Tc", "Jh", "Jd", "Js", "Jc", "Qh", "Qd", "Qs", "Qc", "Kh", "Kd", "Ks", "Kc", "Ah", "Ad", "As", "Ac"};
    String H1a, H1b, H2a, H2b;
    List<String> deck = new ArrayList<>();
    HashMap<String,Integer> players_turn_name = new HashMap<String,Integer>();
    String[] deck_arr = {card1, card2, card3, card4, card5};
    boolean running = false, make_move = false, folded = false, called = false, all_in = false;
    Integer[] bets = {50, 100};
    Poker(int level, HashMap<Integer,String> players, int player_turn,  HashMap<Integer,Integer> stacks, HashMap<String,Integer> players_turn_name){
        this.level = level;
        this.players = players;
        this.player_turn = player_turn;
        this.stacks = stacks;
        this.players_turn_name = players_turn_name;


    }
    void flop(String card3, String card4, String card5, int move){
        this.card3 = card3;
        this.card4 = card4;
        this.card5 = card5;
        //this.move = move;
    }
    void turn(String card6){
        this.card6 = card6;
    }
    void river(String card7){
        this.card7 = card7;
    }
    void shuffle_deck(){
        index_deck = 0;
        deck = Arrays.asList(deck_default);
        Collections.shuffle(deck);
        System.out.println(deck);

        bets[0] = 50;
        bets[1] = 100;


        for (Map.Entry m : players.entrySet()) {
            hands.put((int) m.getKey(), deck.get(index_deck++) + deck.get(index_deck++));

        }
        System.out.println(index_deck + " - " +  hands);
        card1 = deck.get(index_deck++);
        card2 = deck.get(index_deck++);
        card3 = deck.get(index_deck++);
        card4 = deck.get(index_deck++);
        card5 = deck.get(index_deck++);

        deck_arr[0] = card1;
        deck_arr[1] = card2;
        deck_arr[2] = card3;
        deck_arr[3] = card4;
        deck_arr[4] = card5;

        level = 0;
        pot = bets[0] + bets[1];
        update_stacks();
    }
    void update_stacks(){
        int value, key2, buf;
        String key, value2;
        System.out.println("updadete_stacks");
        System.out.println(stacks);
        for (Map.Entry m : players_turn_name.entrySet()) {
            key = (String) m.getKey();
            value = (Integer) m.getValue();
            System.out.println(key + " " + value);
            for (Map.Entry n : players.entrySet()) {
                key2 = (Integer) n.getKey();
                value2 = (String) n.getValue();
                System.out.println(key2 + " " + value2);
                if (key.equalsIgnoreCase(value2)){

                    buf = stacks.get(key2);
                    System.out.println("elo " + buf + " " +  bets[value]);
                    stacks.put(key2, buf - bets[value]);
                    break;
                }
            }
        }
        System.out.println("new stacks" + stacks);

    }

}
