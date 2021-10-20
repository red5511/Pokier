//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
//import java.net.Socket;


class Server_Game_Thread extends Thread
{
    Socket sock1;
    Socket sock2;
    Integer pos1, pos2;
    BufferedReader sockReader;

    Server_Game_Thread() throws IOException{
        //this.sock = sock;
        //this.sockReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
    }

    public void run(){
        System.out.println("odpalilem Server_Game_Thread");

        while (true){
            if (Globals.players_sock.size() == 2){
                sock1 = Globals.players_sock.get(0);
                pos1 = Integer.parseInt(Globals.players_pos.get(0));
                sock2 = Globals.players_sock.get(1);
                pos2 = Integer.parseInt(Globals.players_pos.get(1));

                Globals.players_sock.clear();
                Globals.players_pos.clear();
            }
            if (sock1 != null){
                System.out.println("Dziala ten if???");

            }
        }
    }
}

class Player_Thread extends Thread
{
    private Socket sock;
    private BufferedReader sockReader;
    //Server_Game_Thread thread;

    private InputStream inputStream;
    private ObjectInputStream objectInputStream;
    HashMap<Integer,String> players_sitting;
    private OutputStream outputStream;
    private ObjectOutputStream objectOutputStream;
    private PrintWriter outp;
    String  new_player, player_name, cashier_name;
    Integer table_index, pos, size, balance;
    int counter = 0;
    private Lock lock = new Lock();
    private Globals global;
    Message msg, recived_msg;
    boolean reseted_game = false, running = true;
    boolean[] all_in = {false, false};
    Connection con = null;
    Statement st = null;
    ResultSet rs = null;


    Player_Thread(Socket sock, Globals glob) throws IOException, ClassNotFoundException{

        global = glob;

        this.sock = sock;
        this.sockReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));

        outputStream = sock.getOutputStream();
        objectOutputStream = new ObjectOutputStream(outputStream);
        inputStream = sock.getInputStream();
        objectInputStream = new ObjectInputStream(inputStream);
        outp = new PrintWriter(sock.getOutputStream());

    }

    public void run() {
        System.out.println("odpalilem tgreada");
        recived_msg = new Message();

        while (running){
            System.out.println("My Name is :- " + Thread.currentThread().getName());
            System.out.println("Counter = " + counter++);
            System.out.println("Globals list " + global.players_sitting);
            System.out.println("Globals player runt list " + global.players_turn_name);
            System.out.println("Globlas stack " + global.stacks);

            for (int i = 0; i < 2; i++){
                    try{
                    System.out.println("eyy For i " + i);
                    System.out.println("Poker0game staks " + global.poker_games[i].stacks);
                    }catch (Exception e){;}
                }


            msg = new Message();



            if (recived_msg.flag_register){
                try{
                    con = DriverManager.getConnection(
                            "jdbc:mysql://192.168.56.102:3306/testConnection",
                            "test", "password123");
                    System.out.println("Connected");

                    st = con.createStatement();
                    st.executeUpdate("INSERT INTO test " + "VALUES ('" + recived_msg.nick + "', '" + recived_msg.pass + "', " + 500000 + ")");
               //     rs = st.executeQuery("INSERT INTO test (nick, password, cashier) " +
                 //           "VALUES (" + recived_msg.nick + ", " + recived_msg.pass + ", " + 500000 + ")");

                }catch (Exception e){
                    System.out.println("Bledzik xdd");
                    e.printStackTrace();

                }
                finally{
                    try{ con.close();}
                    catch (Exception e){;}
                }
            }

            if (recived_msg.flag_login){
                try{
                    con = DriverManager.getConnection(
                            "jdbc:mysql://192.168.56.102:3306/testConnection",
                            "test", "password123");
                    System.out.println("Connected");

                    st = con.createStatement();
                  //  st.executeUpdate("INSERT INTO test " + "VALUES ('" + recived_msg.nick + "', '" + recived_msg.pass + "', " + 500000 + ")");
                    rs = st.executeQuery("select * from test where nick = '" + recived_msg.nick + "' and password = '" + recived_msg.pass + "'");

                    msg.flag_login = true;
                    if (rs.next()) {
                        System.out.println("Nick " + rs.getString(1) + " password " + rs.getString(2) + " Cashier " + rs.getInt(3));
                        msg.login_sucess = true;
                        msg.cashier = rs.getInt(3);
                        balance = rs.getInt(3);
                    }
                    else {
                        System.out.println("nieam xddddddd");
                        msg.login_sucess = false;

                    }

                }catch (Exception e){
                    System.out.println("Bledzik xdd");
                    e.printStackTrace();

                }
                finally{
                    try{ con.close();}
                    catch (Exception e){;}
                }
            }

            update_tables();
            update_games();

            send_tables();

            //  System.out.println(Globals.players_sitting);
            send_game();
            try {
                System.out.println("wysyalm");
                System.out.println(msg.players_sitting);
                objectOutputStream.writeUnshared(msg);
                objectOutputStream.reset();
                System.out.println("odieram");
                recived_msg = (Message) objectInputStream.readObject();
                Thread.sleep(888);
            } catch (Exception e) {
                System.out.println("DC");
                update_cashier_flow();
                System.out.println("flow cehck " + global.cashier_flow);
                global.cashier_flow.remove(cashier_name);
                System.out.println("stackxx " + global.stacks);
                System.out.println("flow " + global.cashier_flow);
                try {
                    sock.close();
                    sockReader.close();
                    outputStream.close();
                    objectOutputStream.close();
                    inputStream.close();
                    objectInputStream.close();
                    outp.close();
                    running = false;
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    running = false;
                }
                e.printStackTrace();
            }


        }
    }

    void send_game() {
        try {
            lock.lock();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Send_game()");
        System.out.println("My Name is :- " + Thread.currentThread().getName());
        for (int i = 0; i < 2; i++) {
            System.out.println("For i = " + i);
            System.out.println("Size = " + global.players_sitting.get(i).size());
            if (global.players_sitting.get(i).size() > 1) {
                System.out.println("W ifie");
                if (global.poker_games[i] == null) {
                    global.poker_games[i] = new Poker(0, global.players_sitting.get(i), global.players_turn[i], global.stacks.get(i), global.players_turn_name.get(i));

                    System.out.println("First entry IF");
                    global.poker_games[i].running = true;
                    global.poker_games[i].shuffle_deck();

                    global.stacks.set(i,  global.poker_games[i].stacks);
                    System.out.println("PLZZZ " + global.players_turn_name);
                    //msg.append(global.players_turn_name, "juzles");
                }
            }

        }

        msg.append(global.players_turn_name, "juzles");

        for (int i = 0; i < 2; i++){
            if (global.poker_games[i] != null){
                global.poker_games[i].stacks  = global.stacks.get(i);
                global.poker_games[i].players_turn_name =  global.players_turn_name.get(i);

            }

        }
        msg.append(global.poker_games);
        lock.unlock();
    }

    void send_tables(){
        try {
            lock.lock();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Send_tables()");
        System.out.println("My Name is :- " + Thread.currentThread().getName());
        msg.append(global.players_sitting, global.stacks);
        System.out.println();

        lock.unlock();
    }

    void update_tables() {
        try {
            lock.lock();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Update_tables()");
        System.out.println("My Name is :- " + Thread.currentThread().getName());

        for (int i = 0; i < 2; i++) {
            System.out.println("for i = " + i);
            System.out.println("++++++ name " + recived_msg.name[i]);

            if (recived_msg.name[i] != null) {
                if (recived_msg.flag_leave[i]){
                    System.out.println("Leave_flag");
                    if (global.players_sitting.get(i).get(0) != null && global.players_sitting.get(i).get(0).equalsIgnoreCase(recived_msg.name[i])){
                        System.out.println("do usuniecia");
                        System.out.println(global.cashier_flow);
                        System.out.println("stackxx " + global.stacks);
                        Integer buff = global.cashier_flow.get(recived_msg.name[i]) + global.stacks.get(i).get(0);
                        global.cashier_flow.put(recived_msg.name[i], buff);
                        global.stacks.get(i).remove(0);
                        global.players_sitting.get(i).remove(0);
                        if (global.players_turn_name.get(i).containsKey(recived_msg.name[i])) global.players_turn_name.get(i).remove(recived_msg.name[i]);
                        global.poker_games[i] = null;
                        System.out.println(global.cashier_flow);
                        System.out.println("stackxx " + global.stacks);

                    }
                    else if (global.players_sitting.get(i).get(1) != null && global.players_sitting.get(i).get(1).equalsIgnoreCase(recived_msg.name[i])){
                        System.out.println("do usuniecia2");
                        System.out.println(global.cashier_flow);
                        System.out.println("stackxx " + global.stacks);
                        Integer buff = global.cashier_flow.get(recived_msg.name[i]) + global.stacks.get(i).get(1);
                        global.cashier_flow.put(recived_msg.name[i], buff);
                        global.stacks.get(i).remove(1);
                        global.players_sitting.get(i).remove(1);
                        if (global.players_turn_name.get(i).containsKey(recived_msg.name[i])) global.players_turn_name.get(i).remove(recived_msg.name[i]);
                        global.poker_games[i] = null;
                        System.out.println(global.cashier_flow);
                        System.out.println("stackxx " + global.stacks);
                        System.out.println("new_sitting " + global.players_sitting);


                    }

                }
                else {
                    global.players_sitting.get(i).put(recived_msg.pos[i], recived_msg.name[i]);
                    global.stacks.get(i).put(recived_msg.pos[i], 10000);
                    if (!global.cashier_flow.containsKey(recived_msg.name[i])) {
                        global.cashier_flow.put(recived_msg.name[i], -10000);
                        cashier_name = recived_msg.name[i];
                    }
                    else{
                        global.cashier_flow.put(recived_msg.name[i],  global.cashier_flow.get(recived_msg.name[i]) - 10000);
                    }


                    players_turn_dict(i);
                    System.out.println("XDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD player_tunr_anme " + global.players_turn_name.get(i));
                }

            }
        }

        lock.unlock();
    }

    void update_games() {
        try {
            lock.lock();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Update_games()");
        System.out.println("My Name is :- " + Thread.currentThread().getName());

        for (int i = 0; i < 2; i++) {
            System.out.println("for i = " + i);
        //    System.out.println("++++++ name " + recived_msg.poker_games[i]);
            System.out.println("WWWWWWWWWWWWWWW");
            if (recived_msg.poker_games[i] != null){
                //System.out.println("FOld " + recived_msg.poker_games[i].folded + " Mk mv " + recived_msg.poker_games[i].make_move + " global.players_turn[i] " + global.players_turn[i] + " recived_msg.poker_games[i].my_postion " + recived_msg.poker_games[i].my_postion);
                System.out.println("no jest czy nie " +  recived_msg.poker_games[i].all_in);
            }

            if (all_in[i]){
                System.out.println("ALLLEK");
                System.out.println("lvl " + global.poker_games[i].player_turn);
                global.poker_games[i].level += 1;
                if ( global.poker_games[i].level == 4 || global.poker_games[i].level == 5) {
                    System.out.println("44");
                    String board = global.poker_games[i].card1 + " " + global.poker_games[i].card2 + " " + global.poker_games[i].card3 + " " + global.poker_games[i].card4 + " " + global.poker_games[i].card5;
                    String h1a = global.poker_games[i].hands.get(0).substring(0, 2);
                    String h1b = global.poker_games[i].hands.get(0).substring(2);

                    String h2a = global.poker_games[i].hands.get(1).substring(0, 2);
                    String h2b = global.poker_games[i].hands.get(1).substring(2);

                    Card[] hand1 = Hand.fromString(board + " " + h1a + " " + h1b);
                    Card[] hand2 = Hand.fromString(board + " " + h2a + " " + h2b);

                    int value1 = Hand.evaluate(hand1);
                    int value2 = Hand.evaluate(hand2);
                    System.out.println("board " + board);
                    System.out.println("karty1 " + value1 + " " + h1a + " " + h1b);
                    System.out.println("karty2 " + value2 + " " + h2a + " " + h2b);

                    int xd;


                    if (value1 < value2) xd = 0;
                    else if (value1 > value2) xd = 1;
                    else xd = 9;

                    String buf = global.players_sitting.get(i).get(xd);
                    System.out.println("buf " + buf);

                   // global.poker_games[i].pot = global.poker_games[i].river_pot + 2 * global.poker_games[i].bets[0];

                    int buff = global.poker_games[i].stacks.get(xd);
                    if (xd != 9) global.poker_games[i].stacks.put(xd, buff +  global.poker_games[i].pot);
                    else {
                        buff = global.poker_games[i].stacks.get(0);
                        global.poker_games[i].stacks.put(0, buff + (int)(global.poker_games[i].pot/2 - (global.poker_games[i].pot/2 % 1)));
                        buff = global.poker_games[i].stacks.get(1);
                        global.poker_games[i].stacks.put(1, buff +  (int)(global.poker_games[i].pot/2 - (global.poker_games[i].pot/2 % 1)));
                    }

                    reset_game(i);

                    global.poker_games[i].who_wins = buf;
                    global.poker_games[i].player_turn = global.players_turn[i];
                    global.poker_games[i].called = false;
                    global.poker_games[i].all_in = false;
                    all_in[i] = false;
                    global.poker_games[i].player_turn = global.players_turn[i];
                }
            }
            else if (recived_msg.poker_games[i] != null && global.players_turn[i] == recived_msg.poker_games[i].my_postion &&  recived_msg.poker_games[i].called && (recived_msg.poker_games[i].stacks.get(0) == 0 || recived_msg.poker_games[i].stacks.get(1) == 0)){
                System.out.println("mam allina");
                System.out.println("lewel " + recived_msg.poker_games[i].level);
                global.poker_games[i] = recived_msg.poker_games[i];

                global.stacks.set(i, global.poker_games[i].stacks);
                global.poker_games[i].make_move = false;
                global.poker_games[i].player_turn = 11;
                if (global.poker_games[i].bets[0] > global.poker_games[i].bets[1]){
                    int value, key2, buf;
                    String key, value2;
                    for (Map.Entry m :  global.poker_games[i].players_turn_name.entrySet()) {
                        key = (String) m.getKey();
                        value = (Integer) m.getValue();
                        System.out.println(key + " " + value);
                        if (value != 0) continue;
                        for (Map.Entry n :  global.poker_games[i].players.entrySet()) {
                            key2 = (Integer) n.getKey();
                            value2 = (String) n.getValue();
                            System.out.println(key2 + " " + value2);
                            if (key.equalsIgnoreCase(value2)){
                                buf =  global.poker_games[i].bets[0] - global.poker_games[i].bets[1];
                                global.poker_games[i].bets[0] -= buf;
                                // buf =  global.poker_games[i].stacks.get(key2);
                                System.out.println("elo " + buf + " " +   global.poker_games[i].bets[value]);
                                global.poker_games[i].stacks.put(key2, buf + global.poker_games[i].stacks.get(key2));
                                break;
                            }
                        }
                    }
                }
                else if (global.poker_games[i].bets[1] > global.poker_games[i].bets[0]) {
                    int value, key2, buf;
                    String key, value2;
                    for (Map.Entry m : global.poker_games[i].players_turn_name.entrySet()) {
                        key = (String) m.getKey();
                        value = (Integer) m.getValue();
                        System.out.println(key + " " + value);
                        if (value != 1) continue;
                        for (Map.Entry n : global.poker_games[i].players.entrySet()) {
                            key2 = (Integer) n.getKey();
                            value2 = (String) n.getValue();
                            System.out.println(key2 + " " + value2);
                            if (key.equalsIgnoreCase(value2)) {
                                buf = global.poker_games[i].bets[1] - global.poker_games[i].bets[0];
                                global.poker_games[i].bets[1] -= buf;
                                // buf =  global.poker_games[i].stacks.get(key2);
                                System.out.println("elo " + buf + " " + global.poker_games[i].bets[value]);
                                global.poker_games[i].stacks.put(key2, buf + global.poker_games[i].stacks.get(key2));
                                break;
                            }
                        }
                    }
                }


                if (global.poker_games[i].level == 0){

                    global.poker_games[i].pot = global.poker_games[i].bets[1] + global.poker_games[i].bets[0];
                    global.poker_games[i].level = 1;
                    global.poker_games[i].bets[0] = 0;
                    global.poker_games[i].bets[1] = 0;
                    global.poker_games[i].all_in = true;
                    all_in[i] = true;
                    System.out.println("check "  + global.poker_games[i].all_in);
                }
                else if (global.poker_games[i].level == 1){
                    global.poker_games[i].pot =  global.poker_games[i].flop_pot + global.poker_games[i].bets[1] + global.poker_games[i].bets[0];
                    global.poker_games[i].level = 2;
                    global.poker_games[i].bets[0] = 0;
                    global.poker_games[i].bets[1] = 0;
                    global.poker_games[i].all_in = true;
                    all_in[i] = true;
                    System.out.println("check2 "  + global.poker_games[i].all_in);
                }
                else if (global.poker_games[i].level == 2){
                    global.poker_games[i].pot =  global.poker_games[i].turn_pot + global.poker_games[i].bets[1] + global.poker_games[i].bets[0];
                    global.poker_games[i].level = 3;
                    global.poker_games[i].bets[0] = 0;
                    global.poker_games[i].bets[1] = 0;
                    global.poker_games[i].all_in = true;
                    all_in[i] = true;
                    System.out.println("check3 "  + global.poker_games[i].all_in);
                }
                else if (global.poker_games[i].level == 3){
                    global.poker_games[i].pot =  global.poker_games[i].river_pot + global.poker_games[i].bets[1] + global.poker_games[i].bets[0];
                    global.poker_games[i].level = 3;
                    global.poker_games[i].bets[0] = 0;
                    global.poker_games[i].bets[1] = 0;
                    global.poker_games[i].all_in = true;
                    all_in[i] = true;
                    System.out.println("check4 "  + global.poker_games[i].all_in);
                }
            }
            else if (recived_msg.poker_games[i] != null && recived_msg.poker_games[i].make_move && global.players_turn[i] == recived_msg.poker_games[i].my_postion) {
                global.stacks.set(i, recived_msg.poker_games[i].stacks);
                recived_msg.poker_games[i].make_move = false;
                System.out.println("global " + global.poker_games[i] + " recived " +  recived_msg.poker_games[i]);
                global.players_turn[i] = (global.players_turn[i] + 1) % 2;
                if (recived_msg.poker_games[i].folded){
                    global.poker_games[i] = recived_msg.poker_games[i];
                    int value, key2, buf;
                    String key, value2;
                    for (Map.Entry m : global.poker_games[i].players_turn_name.entrySet()) {
                        key = (String) m.getKey();
                        value = (Integer) m.getValue();
                        System.out.println(key + " " + value);
                        if (value == global.poker_games[i].my_postion) continue;
                        for (Map.Entry n : global.poker_games[i].players.entrySet()) {
                            key2 = (Integer) n.getKey();
                            value2 = (String) n.getValue();
                            System.out.println(key2 + " " + value2);
                            if (key.equalsIgnoreCase(value2)) {
                                buf = global.poker_games[i].pot + global.poker_games[i].stacks.get(key2);
                                System.out.println("elo " + buf + " " + global.poker_games[i].bets[value]);
                                global.poker_games[i].stacks.put(key2, buf);
                                break;
                            }
                        }
                    }
                    reset_game(i);
                    global.poker_games[i].player_turn = global.players_turn[i];


                }
                else if (recived_msg.poker_games[i].called ){
                    if (recived_msg.poker_games[i].level == 0 && recived_msg.poker_games[i].my_postion == 0 && recived_msg.poker_games[i].bets[1] > 100) {
                        System.out.println("111");
                        global.poker_games[i] = recived_msg.poker_games[i];
                        global.poker_games[i].player_turn = 1;
                        global.poker_games[i].called = false;
                        global.poker_games[i].level = 1;
                        System.out.println(global.poker_games[i].pot + " b1" +  global.poker_games[i].bets[0] + " b2" + global.poker_games[i].bets[1]);
                        global.poker_games[i].pot = global.poker_games[i].bets[1] + global.poker_games[i].bets[0];
                        System.out.println("pot " + global.poker_games[i].pot );
                        global.poker_games[i].flop_pot = global.poker_games[i].pot;
                        global.poker_games[i].bets[0] = 0;
                        global.poker_games[i].bets[1] = 0;
                        global.players_turn[i] = 1;
                      //  global.poker_games[i].bets[
                    }
                    else if (recived_msg.poker_games[i].level == 0 && recived_msg.poker_games[i].my_postion == 1) {
                        System.out.println("222");
                        global.poker_games[i] = recived_msg.poker_games[i];
                        global.poker_games[i].player_turn = 1;
                        global.poker_games[i].called = false;
                        global.poker_games[i].level = 1;
                        System.out.println(global.poker_games[i].pot + " b1" +  global.poker_games[i].bets[0] + " b2" + global.poker_games[i].bets[1]);
                        global.poker_games[i].pot = global.poker_games[i].bets[0] + global.poker_games[i].bets[1];
                        System.out.println("pot " + global.poker_games[i].pot );
                        global.poker_games[i].flop_pot = global.poker_games[i].pot;
                        global.poker_games[i].bets[0] = 0;
                        global.poker_games[i].bets[1] = 0;
                        global.players_turn[i] = 1;

                    }
                    else if (recived_msg.poker_games[i].level == 0){//tylko wtedy gedy z button klikam call i akcja idzie na BB?!
                        System.out.println("333");
                        global.poker_games[i] = recived_msg.poker_games[i];
                        global.poker_games[i].player_turn = global.players_turn[i];
                        global.poker_games[i].called = false;
                        global.poker_games[i].pot += 50;

                    }
                    else if (recived_msg.poker_games[i].level == 1 && recived_msg.poker_games[i].my_postion == 0 ){
                        System.out.println("444");
                        global.poker_games[i] = recived_msg.poker_games[i];
                        global.poker_games[i].player_turn = 1;
                        global.poker_games[i].called = false;
                        global.poker_games[i].level = 2;
                        System.out.println(global.poker_games[i].pot + " b1" +  global.poker_games[i].bets[0] + " b2" + global.poker_games[i].bets[1]);
                        global.poker_games[i].pot = global.poker_games[i].flop_pot + 2 * global.poker_games[i].bets[0];
                        global.poker_games[i].turn_pot = global.poker_games[i].pot;
                        System.out.println("pot " +  global.poker_games[i].pot);
                        global.poker_games[i].bets[0] = 0;
                        global.poker_games[i].bets[1] = 0;
                        global.players_turn[i] = 1;

                    }
                    else if (recived_msg.poker_games[i].level == 1 && recived_msg.poker_games[i].my_postion == 1 && recived_msg.poker_games[i].bets[0] > 0) {// TU MZIANANAN BYLA BETS[0] z bets[1]
                        System.out.println("555");
                        global.poker_games[i] = recived_msg.poker_games[i];
                        global.poker_games[i].player_turn = 1;
                        global.poker_games[i].called = false;
                        global.poker_games[i].level = 2;
                        System.out.println(global.poker_games[i].pot + " b1" +  global.poker_games[i].bets[0] + " b2" + global.poker_games[i].bets[1]);
                        global.poker_games[i].pot = global.poker_games[i].flop_pot + 2 * global.poker_games[i].bets[0];
                        global.poker_games[i].turn_pot = global.poker_games[i].pot;
                        System.out.println("pot " +  global.poker_games[i].pot);
                        global.poker_games[i].bets[0] = 0;
                        global.poker_games[i].bets[1] = 0;
                        global.players_turn[i] = 1;
                        //  global.poker_games[i].bets[
                    }
                    else if (recived_msg.poker_games[i].level == 2 && recived_msg.poker_games[i].my_postion == 0 ){
                        System.out.println("666");
                        global.poker_games[i] = recived_msg.poker_games[i];
                        global.poker_games[i].player_turn = 1;
                        global.poker_games[i].called = false;
                        global.poker_games[i].level = 3;
                        System.out.println(global.poker_games[i].pot + " b1" +  global.poker_games[i].bets[0] + " b2" + global.poker_games[i].bets[1]);
                        global.poker_games[i].pot = global.poker_games[i].turn_pot + 2 * global.poker_games[i].bets[0];
                        global.poker_games[i].river_pot =  global.poker_games[i].pot;
                        System.out.println("pot " +  global.poker_games[i].pot);
                        global.poker_games[i].bets[0] = 0;
                        global.poker_games[i].bets[1] = 0;
                        global.players_turn[i] = 1;
                    }
                    else if (recived_msg.poker_games[i].level == 2 && recived_msg.poker_games[i].my_postion == 1 && recived_msg.poker_games[i].bets[0] > 0) {// same as above
                        System.out.println("777");
                        global.poker_games[i] = recived_msg.poker_games[i];
                        global.poker_games[i].player_turn = 1;
                        global.poker_games[i].called = false;
                        global.poker_games[i].level = 3;
                        System.out.println(global.poker_games[i].pot + " b1" +  global.poker_games[i].bets[0] + " b2" + global.poker_games[i].bets[1]);
                        global.poker_games[i].pot = global.poker_games[i].turn_pot + 2 * global.poker_games[i].bets[0];
                        global.poker_games[i].river_pot =  global.poker_games[i].pot;
                        System.out.println("pot " +  global.poker_games[i].pot);
                        global.poker_games[i].bets[0] = 0;
                        global.poker_games[i].bets[1] = 0;
                        global.players_turn[i] = 1;
                        //  global.poker_games[i].bets[
                    }
                    else if (recived_msg.poker_games[i].level == 3 && recived_msg.poker_games[i].my_postion == 0 ){
                        global.poker_games[i] =  recived_msg.poker_games[i];
                        global.poker_games[i].player_turn = 11;
                        global.poker_games[i].level = 4;
                        global.poker_games[i].all_in = true;
                        global.poker_games[i].pot = global.poker_games[i].river_pot + 2 * global.poker_games[i].bets[0];
                        global.poker_games[i].bets[0] = 0;
                        global.poker_games[i].bets[1] = 0;

                        all_in[i] = true;
                    }
                    else if (recived_msg.poker_games[i].level == 4 && recived_msg.poker_games[i].my_postion == 1 && recived_msg.poker_games[i].bets[0] > 0){
                        global.poker_games[i] =  recived_msg.poker_games[i];
                        global.poker_games[i].level = 4;
                        global.poker_games[i].player_turn = 11;
                        global.poker_games[i].all_in = true;
                        global.poker_games[i].pot = global.poker_games[i].river_pot + 2 * global.poker_games[i].bets[0];
                        global.poker_games[i].bets[0] = 0;
                        global.poker_games[i].bets[1] = 0;

                        all_in[i] = true;
                    }
                    else if (recived_msg.poker_games[i].level == 4 && recived_msg.poker_games[i].my_postion == 0 ){
                        System.out.println("AAA");

                        String board = recived_msg.poker_games[i].card1 + " " + recived_msg.poker_games[i].card2 + " " + recived_msg.poker_games[i].card3 + " " + recived_msg.poker_games[i].card4 + " " + recived_msg.poker_games[i].card5;
                        String h1a = recived_msg.poker_games[i].hands.get(0).substring(0, 2);
                        String h1b = recived_msg.poker_games[i].hands.get(0).substring(2);

                        String h2a = recived_msg.poker_games[i].hands.get(1).substring(0, 2);
                        String h2b = recived_msg.poker_games[i].hands.get(1).substring(2);

                        Card[] hand1 = Hand.fromString(board + " " + h1a + " " + h1b);
                        Card[] hand2 = Hand.fromString(board + " " + h2a + " " + h2b);

                        int value1 = Hand.evaluate(hand1);
                        int value2 = Hand.evaluate(hand2);
                        System.out.println("board " + board);
                        System.out.println("karty1 " + value1 + " " + h1a + " " + h1b);
                        System.out.println("karty2 " + value2 + " " + h2a + " " + h2b);

                        int xd;


                        if (value1 < value2) xd = 0;
                        else if (value1 > value2) xd = 1;
                        else xd = 9;

                        String buf = global.players_sitting.get(i).get(xd);
                        System.out.println("buf " + buf);

                        global.poker_games[i] =  recived_msg.poker_games[i];
                        global.poker_games[i].pot = global.poker_games[i].river_pot + 2 * global.poker_games[i].bets[0];

                        int buff = global.poker_games[i].stacks.get(xd);
                        if (xd != 9) global.poker_games[i].stacks.put(xd, buff +  global.poker_games[i].pot);
                        else {
                            buff = global.poker_games[i].stacks.get(0);
                            global.poker_games[i].stacks.put(0, buff + (int)(global.poker_games[i].pot/2 - (global.poker_games[i].pot/2 % 1)));
                            buff = global.poker_games[i].stacks.get(1);
                            global.poker_games[i].stacks.put(1, buff +  (int)(global.poker_games[i].pot/2 - (global.poker_games[i].pot/2 % 1)));
                        }

                        reset_game(i);

                        global.poker_games[i].who_wins = buf;
                        global.poker_games[i].player_turn = global.players_turn[i];
                        global.poker_games[i].called = false;
                    }
                    else if (recived_msg.poker_games[i].level == 4 && recived_msg.poker_games[i].my_postion == 1 && recived_msg.poker_games[i].bets[0] > 0) {// same as above
                        System.out.println("SSS");
                        String board = recived_msg.poker_games[i].card1 + " " + recived_msg.poker_games[i].card2 + " " + recived_msg.poker_games[i].card3 + " " + recived_msg.poker_games[i].card4 + " " + recived_msg.poker_games[i].card5;
                        String h1a = recived_msg.poker_games[i].hands.get(0).substring(0, 2);
                        String h1b = recived_msg.poker_games[i].hands.get(0).substring(2);

                        String h2a = recived_msg.poker_games[i].hands.get(1).substring(0, 2);
                        String h2b = recived_msg.poker_games[i].hands.get(1).substring(2);

                        Card[] hand1 = Hand.fromString(board + " " + h1a + " " + h1b);
                        Card[] hand2 = Hand.fromString(board + " " + h2a + " " + h2b);

                        int value1 = Hand.evaluate(hand1);
                        int value2 = Hand.evaluate(hand2);
                        System.out.println("board " + board);
                        System.out.println("karty1 " + value1 + " " + h1a + " " + h1b);
                        System.out.println("karty2 " + value2 + " " + h2a + " " + h2b);

                        int xd;


                        if (value1 < value2) xd = 0;
                        else if (value1 > value2) xd = 1;
                        else xd = 9;

                        String buf = global.players_sitting.get(i).get(xd);
                        System.out.println("buf " + buf);

                        global.poker_games[i] =  recived_msg.poker_games[i];

                        int buff = global.poker_games[i].stacks.get(xd);
                        if (xd != 9) global.poker_games[i].stacks.put(xd, buff +  global.poker_games[i].pot);
                        else {
                            buff = global.poker_games[i].stacks.get(0);
                            global.poker_games[i].stacks.put(0, buff + (int)(global.poker_games[i].pot/2 - (global.poker_games[i].pot/2 % 1)));
                            buff = global.poker_games[i].stacks.get(1);
                            global.poker_games[i].stacks.put(1, buff +  (int)(global.poker_games[i].pot/2 - (global.poker_games[i].pot/2 % 1)));
                        }


                        reset_game(i);

                        global.poker_games[i].who_wins = buf;
                        global.poker_games[i].player_turn = global.players_turn[i];
                        global.poker_games[i].called = false;
                    }
                    else{
                        System.out.println("888");
                        global.poker_games[i] = recived_msg.poker_games[i];
                        global.poker_games[i].player_turn = global.players_turn[i];
                        global.poker_games[i].called = false;

                    }


                }
                else{
                    global.poker_games[i] = recived_msg.poker_games[i];
                    global.poker_games[i].player_turn = global.players_turn[i];
                    global.poker_games[i].called = false;
                    if ( global.poker_games[i].level == 0) global.poker_games[i].pot = global.poker_games[i].bets[0] + global.poker_games[i].bets[1];
                    else if ( global.poker_games[i].level == 1)  global.poker_games[i].pot = global.poker_games[i].flop_pot +  global.poker_games[i].bets[0] + global.poker_games[i].bets[1];
                    else if ( global.poker_games[i].level == 2)  global.poker_games[i].pot = global.poker_games[i].turn_pot +  global.poker_games[i].bets[0] + global.poker_games[i].bets[1];
                    else if ( global.poker_games[i].level == 3)  global.poker_games[i].pot = global.poker_games[i].river_pot +  global.poker_games[i].bets[0] + global.poker_games[i].bets[1];
                    System.out.println("else pot");
                    System.out.println(recived_msg.poker_games[i].my_postion + " pox " + global.poker_games[i].pot + " bet 0 " + global.poker_games[i].bets[0] + " bet 0 " + global.poker_games[i].bets[1]);

                }


            }
        }

        lock.unlock();
    }

    void reset_game(int i){
        System.out.println("reset-game()");
        reseted_game = true;
        System.out.println(" 1 " + global.players_turn_name.get(i));
        global.players_turn[i] = 0;
        global.poker_games[i].folded = false;

        for (Map.Entry<String, Integer> entry :  global.players_turn_name.get(i).entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            value = (value + 1 ) % 2;

            global.players_turn_name.get(i).put(key, value);
        }
        global.poker_games[i].players_turn_name = global.players_turn_name.get(i);

        for (Map.Entry<Integer, Integer> entry :  global.stacks.get(i).entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            if (value < 10000){
                String nazwa1 = global.players_sitting.get(i).get(key);
                Integer buf1 = global.cashier_flow.get(nazwa1) - (10000 - value);
                global.cashier_flow.put(nazwa1, buf1);
                global.poker_games[i].stacks.put(key, 10000);
            }
        }

        global.poker_games[i].shuffle_deck();
        global.stacks.set(i,  global.poker_games[i].stacks);
        System.out.println(" HANDS " + global.poker_games[i].hands);



        //global.players_turn_name = new HashMap<String, Integer>(global.bufor_players_turn_name); yee trza zrobic deep copy dla wicej niz 2 graczy

    }

    void players_turn_dict(int i){
        System.out.println("players_turn_dict()");
        System.out.println("My Name is :- " + Thread.currentThread().getName());
        System.out.println("size " + global.players_sitting.get(i).size() );
        if (global.players_turn_name.get(i).size() == 0){
            global.players_turn_name.get(i).put(recived_msg.name[i], 0);
            global.bufor_players_turn_name.get(i).put(recived_msg.name[i], 0);
            if (global.poker_games[i] != null) global.poker_games[i].players_turn_name.put(recived_msg.name[i], 0);
        }
        else if (global.players_turn_name.get(i).size() == 1){
            global.players_turn_name.get(i).put(recived_msg.name[i], 1);
            global.bufor_players_turn_name.get(i).put(recived_msg.name[i], 1);
            if (global.poker_games[i] != null) global.poker_games[i].players_turn_name.put(recived_msg.name[i], 1);
        }
        else{
            System.out.println("w elsei");
            int min_key = -1;
            int max_key = -1;
            for (Integer key : global.players_sitting.get(i).keySet()) {
                if (key >  recived_msg.pos[i]){
                    if (min_key == -1) {
                        max_key = key;
                    }
                    break;
                }

                min_key = key;
            }
            String name;
            int buf;
            if (min_key != -1){
                name =  global.players_sitting.get(i).get(min_key);
                buf = global.players_turn_name.get(i).get(name);

                for (Map.Entry<String, Integer> entry :  global.players_turn_name.get(i).entrySet()) {
                    String key = entry.getKey();
                    Integer value = entry.getValue();

                    if (value < buf)global.bufor_players_turn_name.get(i).put(key, value);
                    else global.bufor_players_turn_name.get(i).put(key, value + 1);
                }
                global.bufor_players_turn_name.get(i).put(recived_msg.name[i], buf);


            }
            else global.bufor_players_turn_name.get(i).put(recived_msg.name[i], global.bufor_players_turn_name.get(i).size());

        }
        System.out.println("player_tunr_anme " + global.players_turn_name.get(i));
        System.out.println("byfor_player_tunr_anme " + global.bufor_players_turn_name.get(i));

    }

    void update_cashier_flow(){
        System.out.println("stackxx " + global.stacks);
        System.out.println("flow " + global.cashier_flow);
        for (int i = 0; i < 2; i++){
            int key, key2, value2;
            String value;
            for (Map.Entry m :  global.players_sitting.get(i).entrySet()) {
                key = (int) m.getKey();
                value = (String) m.getValue();
                System.out.println(key + " " + value);
                if (value != cashier_name) continue;
                for (Map.Entry n :  global.stacks.get(i).entrySet()) {
                    key2 = (int) n.getKey();
                    value2 = (Integer) n.getValue();
                    System.out.println("key2 " + key2 + " " + value2);
                    boolean xd = key == key2;
                    System.out.println("xdd " + xd);
                    if (key == key2){
                        Integer buf = global.cashier_flow.get(cashier_name) + value2;
                        System.out.println("buf" + buf);
                        global.cashier_flow.put(cashier_name, buf);
                        System.out.println("xxx" + global.cashier_flow.get(cashier_name));
                        global.stacks.get(i).remove(key2);
                        global.players_sitting.get(i).remove(key);
                        global.poker_games[i] = null;
                        break;
                    }
                }
            }

        }

        try{
            con = DriverManager.getConnection(
                    "jdbc:mysql://192.168.56.102:3306/testConnection",
                    "test", "password123");
            System.out.println("Connected");
            Integer kaska =  balance + global.cashier_flow.get(cashier_name);
            System.out.println("Bede wysylal " + kaska);
            st = con.createStatement();
            st.executeUpdate("UPDATE test " +
                                  "SET cashier = " + kaska + " WHERE nick = '" + cashier_name + "'");
            //     rs = st.executeQuery("INSERT INTO test (nick, password, cashier) " +
            //           "VALUES (" + recived_msg.nick + ", " + recived_msg.pass + ", " + 500000 + ")");

        }catch (Exception e){
            System.out.println("Bledzik xdd");
            e.printStackTrace();

        }
        finally{
            try{ con.close();}
            catch (Exception e){;}
        }

    }
}

public class Serwer {
    //Socket[][] tab_tables = new Socket[4][2];


    //= new ArrayList<String>();

    // Map<Integer, String>[] aray = {player_sitting1, player_sitting2};

    public static final int PORT = 50007;
    public static void main(String args[]) throws IOException, ClassNotFoundException
    {
        Globals global = new Globals();
        global.players_sitting.add(new HashMap<>());
        global.players_sitting.add(new HashMap<>());

        global.players_turn_name.add(new HashMap<>());
        global.players_turn_name.add(new HashMap<>());

        global.bufor_players_turn_name.add(new HashMap<>());
        global.bufor_players_turn_name.add(new HashMap<>());

        global.stacks.add(new HashMap<>());
        global.stacks.add(new HashMap<>());
        //Globals.players_sitting.get(0).put(0, "kasda6");


        ServerSocket serv = new ServerSocket(PORT);
        while (true) {
            int table_number = 0;
            System.out.println("Nasluchuje: " + serv);
            Socket sock;
            sock = serv.accept();

            System.out.println("Jest polaczenie: " + sock);
            new Player_Thread(sock, global).start();



/*
            PrintWriter outp;
            outp=new PrintWriter(sock.getOutputStream());
            try {
                Thread.sleep(10000);

                outp.println("msg");
                outp.flush();

                Thread.sleep(1000);

                outp.println("msg");
                outp.flush();
            }
            catch (Exception ee){
                System.out.println("error");
            }

 */



            //Player_Thread gracz1 = new Player_Thread(sock).start();
            //gracz1.start();
        }
    }
}
