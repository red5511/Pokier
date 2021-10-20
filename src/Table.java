import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.*;


class Game_Thread extends Thread
{
    BufferedReader sockReader;
    Socket sock;
    Table game;
    Integer player_pos;

    public Game_Thread(Socket sock, Table game, Integer pos) throws IOException
    {
        this.sock=sock;
        this.game = game;
        player_pos = pos;
        this.sockReader=new BufferedReader(new InputStreamReader(sock.getInputStream()));
        PrintWriter outp;
        outp = new PrintWriter(sock.getOutputStream());
        outp.println(player_pos.toString());
        outp.flush();
        System.out.println("Init");

    }

    public void run() {
        System.out.println("Startuje threada Game");
        String str;
        try {
            while ((str = sockReader.readLine()) != null){

                System.out.println("<dostalem w GAme msg:> " + str);

                if (str.equalsIgnoreCase("exit")){
                    sockReader.close();
                    sock.close();
                }
                else if (str.equalsIgnoreCase("condition1")){
                    System.out.println("No zrobi cos z game");
                }
            }
        } catch (IOException e) {
            System.out.println("Zakonczono polaczenie");
        }
    }
}

class Table extends JFrame implements MouseListener, ChangeListener
{
    Socket sock;
    JFrame frame;
    Poker poker_game, poker_game_send = null;
    ImageIcon icon_game, icon_seat1, icon_seat2, icon_player1, icon_player2, button_hover, button_empty;
    JLabel game, seat1, seat2, button_fold, button_call, button_raise, chips1, text1, chips2, text2, pot, pot2, who, guzik;
    JTextField slider_size;
    JSlider slider;
    JLabel[] names = new JLabel[2];
    JLabel[] cashes = new JLabel[2];
    JLabel[] small_buttons = new JLabel[4];
    JLabel[] seat_empty = new JLabel[2];
    JLabel[] seat_taken = new JLabel[2];
    JLabel[] cards = new JLabel[52];
    JLabel[] covered_cards = new JLabel[4];
    int[] empty_coords = {420, 430, 420, 40};
    int[] taken_coords = {480 - 94, 422, 480 - 94, 34};
    int [] arr_index_my_cards = new int[4];
    int [] arr_index_board = new int[5];
    int  index, weight_game = 960, height_game = 640, weight_seat = 121, height_seat = 52,  weight_player = 193, height_player = 68, my_turn_pos = -1, game_turn = -1, game_level = -1, bet1 = -1, bet2 = -1;
    Integer table, my_pos = -1, stack;
    HashMap<Integer,String> players_sitting;
    HashMap<String,Integer> players_turn_name = new HashMap<String,Integer>();;
    HashMap<Integer,String> bufor_sitting = new HashMap<Integer,String>();
    HashMap<Integer,String> hands = new HashMap<Integer,String>();
    HashMap<Integer,Integer> stacks = new HashMap<Integer,Integer>();
    String str, my_turn_name, name;
    String[] deck_default  = {"2h", "2d", "2c", "2s", "3h", "3d", "3c", "3s", "4h", "4d", "4c", "4s", "5h", "5d", "5c", "5s", "6h", "6d", "6c", "6s", "7h", "7d", "7c", "7s", "8h", "8d", "8c", "8s", "9h", "9d", "9c", "9s", "Th", "Td", "Tc", "Ts", "Jh", "Jd", "Jc", "Js", "Qh", "Qd", "Qc", "Qs", "Kh", "Kd", "Kc", "Ks", "Ah", "Ad", "Ac", "As"};
    Plansza plansza;
    boolean ingame = false, redraw_buttons = true, redraw_cards = true, closed = false;

    Table(String name, HashMap<Integer,String> sitting, HashMap<Integer,Integer> stacks, int table, Plansza plansz)
    {
        plansza = plansz;

        guzik = new JLabel();
        guzik.setBounds(350, 320, 33, 27);
        guzik.setVisible(false);
        guzik.setIcon( new ImageIcon("JAVA_Stars/BTN.png"));

        this.stacks = stacks;

        names[0] = new JLabel();
        names[0].setBounds(430, 435, 70, 20);
        names[0].setForeground(Color.white);
        names[0].setFont(new Font("Verdana", Font.PLAIN, 15));


        names[1] = new JLabel();
        names[1].setBounds(430, 48, 70, 20);
        names[1].setForeground(Color.white);
        names[1].setFont(new Font("Verdana", Font.PLAIN, 15));


        cashes[0] = new JLabel();
        cashes[0].setBounds(425, 460, 70, 20);
        cashes[0].setForeground(Color.white);
        cashes[0].setFont(new Font("Verdana", Font.PLAIN, 15));
        cashes[0].setText("$1 xd");
        cashes[0].setVisible(false);

        cashes[1] = new JLabel();
        cashes[1].setBounds(425, 73, 70, 20);
        cashes[1].setForeground(Color.white);
        cashes[1].setFont(new Font("Verdana", Font.PLAIN, 15));
        cashes[1].setText("$2 xd");
        cashes[1].setVisible(false);



        this.name = name;

        who = new JLabel();
        who.setBounds(160, 225, 275, 66);
        who.setIcon(button_empty);
        who.setVisible(false);
        who.setFont(new Font("Verdana", Font.PLAIN, 20));
        who.setForeground(Color.white);


        slider_size = new JTextField("200");
        slider_size.setBounds(620, 470, 55, 40);
        slider_size.setVisible(false);


        slider = new JSlider(200,10000,200);
        //slider.setPreferredSize(new Dimension(240,30));
        slider.setPaintTicks(true);
        slider.setMinorTickSpacing(500);
        slider.setPaintTrack(true);
        slider.setMajorTickSpacing(1000);
        slider.setBounds(675, 470, 260, 40);
        slider.setVisible(false);
        slider.addChangeListener(this);

        players_sitting =  sitting;
        this.table = table;
        this.sock = sock;
        frame = new JFrame();


        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void  windowClosing(java.awt.event.WindowEvent e) {
                if (ingame){
                    closed = true;
                }
              //  System.exit(0);
            }
        });



        frame.add(guzik);
        frame.add(names[0]);
        frame.add(names[1]);
        frame.add(cashes[0]);
        frame.add(cashes[1]);

        frame.add(slider);
        frame.add(slider_size);
        frame.add(who);

        for (int i = 0; i < 4; i++){
            small_buttons[i] = new JLabel();
            small_buttons[i].setBounds(620 + i * 78,435, 73, 28);
            small_buttons[i].setIcon( new ImageIcon("JAVA_Stars/mini_button_empty.png"));
            small_buttons[i].addMouseListener(this);
            small_buttons[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            small_buttons[i].setVisible(false);
            small_buttons[i].setFont(new Font("Verdana", Font.PLAIN, 14));
            small_buttons[i].setForeground(Color.white);
            small_buttons[i].setHorizontalTextPosition(JLabel.CENTER);
            small_buttons[i].setVerticalTextPosition(JLabel.CENTER);
            frame.add(small_buttons[i]);
        }
        small_buttons[0].setText("2BB");
        small_buttons[1].setText("2.5BB");
        small_buttons[2].setText("POT");
        small_buttons[3].setText("MAX");

        icon_game = new ImageIcon("JAVA_Stars/Table.png");
        icon_seat1 = new ImageIcon("JAVA_Stars/Take_Seat.png");
        icon_seat2 = new ImageIcon("JAVA_Stars/Take_Seat_dolny.png");
        icon_player1 = new ImageIcon("JAVA_Stars/Player_avatar_gora.png");
        icon_player2 = new ImageIcon("JAVA_Stars/Player_avatar_dol.png");

        button_hover = new ImageIcon("JAVA_Stars/hower_button.png");
        button_empty = new ImageIcon("JAVA_Stars/button_empty.png");

        game = new JLabel();
        game.setBounds(0, 0, weight_game, height_game);
        game.setIcon(icon_game);

        for (int i = 0; i < 2; i++){
            seat_empty[i] = new JLabel();
            seat_empty[i].setBounds(empty_coords[2*i], empty_coords[2*i+1], weight_seat, height_seat);
            seat_empty[i].setIcon(icon_seat1);
            seat_empty[i].addMouseListener(this);
            seat_empty[i].setCursor(new Cursor(Cursor.HAND_CURSOR));

            seat_taken[i] = new JLabel();
            seat_taken[i].setBounds(taken_coords[2*i], taken_coords[2*i+1], 195, 70);
            seat_taken[i].setIcon(icon_player2);
            seat_taken[i].setVisible(false);
            seat_taken[i].setForeground(Color.WHITE);
            seat_taken[i].setHorizontalTextPosition(JLabel.CENTER);
            seat_taken[i].setVerticalTextPosition(JLabel.CENTER);
            seat_taken[i].setCursor(new Cursor(Cursor.HAND_CURSOR));

            frame.add(seat_empty[i]);
            frame.add(seat_taken[i]);
        }
        button_fold = new JLabel();
        button_fold.setBounds(460, 520, 153, 59);
        button_fold.setIcon(button_empty);
        button_fold.setVisible(false);
        button_fold.setFont(new Font("Verdana", Font.PLAIN, 18));
        button_fold.setForeground(Color.white);
        button_fold.setHorizontalTextPosition(JLabel.CENTER);
        button_fold.setVerticalTextPosition(JLabel.CENTER);
        button_fold.setText("Fold");
        button_fold.addMouseListener(this);
        frame.add(button_fold);

        button_call = new JLabel();
        button_call.setBounds(620, 520, 153, 59);
        button_call.setIcon(button_empty);
        button_call.setVisible(false);
        button_call.setFont(new Font("Verdana", Font.PLAIN, 18));
        button_call.setForeground(Color.white);
        button_call.setHorizontalTextPosition(JLabel.CENTER);
        button_call.setVerticalTextPosition(JLabel.CENTER);
        button_call.setText("Call");
        button_call.addMouseListener(this);
        frame.add(button_call);



        button_raise = new JLabel();
        button_raise.setBounds(780, 520, 153, 59);
        button_raise.setText("Raise to");
        button_raise.setIcon(button_empty);
        button_raise.setVisible(false);
        button_raise.setFont(new Font("Verdana", Font.PLAIN, 18));
        button_raise.setForeground(Color.white);
        button_raise.setHorizontalTextPosition(JLabel.CENTER);
        button_raise.setVerticalTextPosition(JLabel.CENTER);
        button_raise.addMouseListener(this);
        frame.add(button_raise);

        chips1 = new JLabel();
        chips1.setIcon(new ImageIcon("JAVA_Stars/chips.png"));
        chips1.setBounds(525, 310, 29, 33);
        chips1.setVisible(false);
        text1 = new JLabel("$50");
        text1.setForeground(Color.WHITE);
        text1.setFont(new Font("Verdana", Font.PLAIN, 14));
        text1.setBounds(557, 310, 66, 33);
        text1.setVisible(false);
        frame.add(chips1);
        frame.add(text1);

        chips2 = new JLabel();
        chips2.setIcon(new ImageIcon("JAVA_Stars/chips.png"));
        chips2.setBounds(525, 130, 29, 33);
        chips2.setVisible(false);
        text2 = new JLabel("$100");
        text2.setForeground(Color.WHITE);
        text2.setFont(new Font("Verdana", Font.PLAIN, 14));
        text2.setBounds(557, 130, 66, 33);
        text2.setVisible(false);
        frame.add(chips2);
        frame.add(text2);

        pot = new JLabel("150");
        pot.setForeground(Color.WHITE);
        pot.setFont(new Font("Verdana", Font.PLAIN, 18));
        pot.setBounds(425, 180, 125, 33);
        pot.setVisible(false);

        pot2 = new JLabel("Pot = ");
        pot2.setForeground(Color.WHITE);
        pot2.setFont(new Font("Verdana", Font.PLAIN, 18));
        pot2.setBounds(370, 180, 60, 33);
        pot2.setVisible(false);
        frame.add(pot2);

        frame.add(pot);





        //wczytanie ikon kart
        for (int i = 0; i < 52; i++){
            cards[i] = new JLabel();
            cards[i].setIcon(new ImageIcon("JAVA_Stars/cards/" + deck_default[i] + ".png"));
            cards[i].setVisible(false);
            frame.add(cards[i]);
        }

        for (int i = 0; i < 4; i++){
            covered_cards[i] = new JLabel();
            covered_cards[i].setIcon(new ImageIcon("JAVA_Stars/cards/card_back.png"));
            covered_cards[i].setVisible(false);
            frame.add(covered_cards[i]);
        }


        frame.add(game);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(weight_game, height_game);
        frame.setLayout(null);
        frame.setVisible(true);


        for (Map.Entry m : players_sitting.entrySet()) {
            index = (int) m.getKey();
            str = (String) m.getValue();

           // seat_taken[index].setText(str);
            names[index].setText(str);
            cashes[index].setVisible(true);
            cashes[index].setText(stacks.get(index).toString());

            seat_taken[index].setVisible(true);
            seat_taken[index].setCursor(null);

            seat_empty[index].setVisible(false);
        }

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    void update_my_pos(){
        System.out.println("update position");

        if (poker_game != null){
            if (poker_game.level > 0){
                small_buttons[0].setText("33%");
                small_buttons[1].setText("50%");
                small_buttons[2].setText("75%");
                small_buttons[3].setText("100%");

            }
            else{
                small_buttons[0].setText("2BB");
                small_buttons[1].setText("2.5BB");
                small_buttons[2].setText("POT");
                small_buttons[3].setText("MAX");
            }
            System.out.println("z");
            if (poker_game.bets[0] == poker_game.bets[1]) button_call.setText("Check");
            else button_call.setText("Call");
            System.out.println("x");


            System.out.println("Update_my_pos");
            System.out.println("xd" + players_turn_name);
            System.out.println("game_tunr " + game_turn + " poker_g_t " + poker_game.player_turn + " my_turn_pos " + my_turn_pos);

            if (game_turn != poker_game.player_turn && poker_game.player_turn != my_turn_pos){
                redraw_buttons = true;
                redraw_cards = true;
            }

            if (game_level != poker_game.level && poker_game.player_turn == my_turn_pos){
                redraw_buttons = true;
                redraw_cards = true;
            }

            game_turn = poker_game.player_turn;
            game_level = poker_game.level;


            for (Map.Entry<String, Integer> entry :  players_turn_name.entrySet()) {

                String key = entry.getKey();
                Integer value = entry.getValue();

                if (key.equalsIgnoreCase(my_turn_name)) {
                    if (my_turn_pos != value){
                        redraw_buttons = true;
                        redraw_cards = true;
                        cards[arr_index_my_cards[0]].setVisible(false);
                        cards[arr_index_my_cards[1]].setVisible(false);
                        cards[arr_index_my_cards[2]].setVisible(false);
                        cards[arr_index_my_cards[3]].setVisible(false);
                        covered_cards[0].setVisible(false);
                        covered_cards[1].setVisible(false);
                        covered_cards[2].setVisible(false);
                        covered_cards[3].setVisible(false);
                        for (int i = 0 ; i < 5; i++){
                            try{
                                cards[arr_index_board[i]].setVisible(false);
                            }catch (Exception e2){break;}
                        }
                        try {
                            Thread.sleep(444);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    my_turn_pos = value;
                    poker_game.my_postion = my_turn_pos;
                }
            }
            if (bet1 != poker_game.bets[0] || poker_game.bets[1] != bet2)
            {
                Integer[] copiedArray = new Integer[2];
                System.arraycopy(poker_game.bets, 0, copiedArray, 0, 2);
                Arrays.sort(copiedArray);
                System.out.println("kkkkkk");
                if (2 * copiedArray[1] < stacks.get(my_pos) + poker_game.bets[my_turn_pos]){
                    slider.setMinimum(2 * copiedArray[1]);
                    slider.setMaximum(stacks.get(my_pos) + poker_game.bets[my_turn_pos]);
                }
                else{
                    slider.setMinimum(stacks.get(my_pos) +  poker_game.bets[my_turn_pos]);
                    slider.setMaximum(stacks.get(my_pos) + poker_game.bets[my_turn_pos]);

                }
                System.out.println("qqqqqq");

            }
            bet1 = poker_game.bets[0];
            bet2 = poker_game.bets[1];
        }
    }


    void update_drawing_table(){

        System.out.println("Update_drawing_table");
        update_my_pos();
        System.out.println("my pos " + my_turn_pos);
        System.out.println("1 Etap Update_drawing_table");
        if (poker_game != null) System.out.println("Stacks " + poker_game.stacks);

        if (!slider_size.isFocusOwner()) slider.setValue(Integer.parseInt(slider_size.getText()));

        for (int i = 0; i < 2; i++){
            System.out.println("For " + i);
            if (ingame) {
                if (i - my_pos < 0) index = 2 - my_pos;
                else index = i - my_pos;
            }
            else index = i;
            System.out.println("AA");
            //System.out.println("i=" + i + " inedex=" + index);
            if (players_sitting.get(i) != null){
                System.out.println("BB");

                //  System.out.println("W ifie - " + players_sitting.get(i));
               // seat_taken[index].setText(players_sitting.get(i));
                names[index].setText(players_sitting.get(i));
                System.out.println("Rysuje Name " + index + " i - " + i);
                cashes[index].setText(stacks.get(i).toString());
                cashes[index].setVisible(true);
                names[index].setVisible(true);

                System.out.println("Rysuje cashes " + stacks);
                seat_taken[index].setVisible(true);
                seat_taken[index].setCursor(null);

                seat_empty[index].setVisible(false);

            }
            else {
              //  System.out.println("W else");
                System.out.println("CC");
                cashes[index].setVisible(false);
                names[index].setVisible(false);



                seat_taken[index].setVisible(false);
                seat_empty[index].setVisible(true);
                if (ingame) seat_empty[index].removeMouseListener(this);
            }

            }

        System.out.println("EYYYYYYY poker" + poker_game);
        if (poker_game != null){

            if (my_turn_pos == 0){
                guzik.setBounds(350, 320, 33, 27);
                guzik.setVisible(true);
            }
            else{
                guzik.setBounds(350, 150, 33, 27);
                guzik.setVisible(true);
            }

            if (poker_game.player_turn == my_turn_pos && redraw_buttons){
                button_fold.setVisible(true);
                button_call.setVisible(true);
                button_raise.setVisible(true);

                slider.setVisible(true);
                slider_size.setVisible(true);

                small_buttons[0].setVisible(true);
                small_buttons[1].setVisible(true);
                small_buttons[2].setVisible(true);
                small_buttons[3].setVisible(true);
            }

            who.setText("Last Win \n" + poker_game.who_wins);
            who.setVisible(true);

            if (poker_game.level == 0 || poker_game.level == 1 || poker_game.level == 2 || poker_game.level == 3 || poker_game.level == 4) {
                System.out.println("poker.player_turn " + poker_game.player_turn + " my_turn_pos " + my_turn_pos);
                System.out.println("bets " + poker_game.bets[0] + " " + poker_game.bets[1]);
                if (poker_game.player_turn == my_turn_pos) {
                    System.out.println("no elooo");

                    if (poker_game.bets[my_turn_pos] != 0){
                        text1.setText(poker_game.bets[my_turn_pos].toString());
                        text1.setVisible(true);
                        chips1.setVisible(true);
                    }
                    else{
                        text1.setVisible(false);
                        chips1.setVisible(false);

                    }

                    if (poker_game.bets[(my_turn_pos + 1) % 2] != 0){
                        text2.setText(poker_game.bets[(my_turn_pos + 1) % 2].toString());
                        text2.setVisible(true);
                        chips2.setVisible(true);
                    }
                    else{
                        text2.setVisible(false);
                        chips2.setVisible(false);
                    }

                    pot.setVisible(true);
                    pot.setText(poker_game.pot.toString());

                    pot2.setVisible(true);

                } else {
                    text1.setText(poker_game.bets[my_turn_pos].toString());
                    text2.setText(poker_game.bets[(my_turn_pos + 1) % 2].toString());

                    if (poker_game.bets[my_turn_pos] != 0){
                        text1.setText(poker_game.bets[my_turn_pos].toString());
                        text1.setVisible(true);
                        chips1.setVisible(true);
                    }
                    else{
                        text1.setVisible(false);
                        chips1.setVisible(false);
                    }

                    if (poker_game.bets[(my_turn_pos + 1) % 2] != 0){
                        text2.setText(poker_game.bets[(my_turn_pos + 1) % 2].toString());
                        text2.setVisible(true);
                        chips2.setVisible(true);
                    }
                    else{
                        text2.setVisible(false);
                        chips2.setVisible(false);
                    }
                    pot.setVisible(true);
                    pot.setText(poker_game.pot.toString());


                }
            }

        }
        else{
            pot.setVisible(false);
            text2.setVisible(false);
            chips2.setVisible(false);
            text1.setVisible(false);
            chips1.setVisible(false);
            pot2.setVisible(false);
            who.setVisible(false);
            button_fold.setVisible(false);
            button_call.setVisible(false);
            button_raise.setVisible(false);
            slider.setVisible(false);
            slider_size.setVisible(false);
            small_buttons[0].setVisible(false);
            small_buttons[1].setVisible(false);
            small_buttons[2].setVisible(false);
            small_buttons[3].setVisible(false);
            guzik.setVisible(false);


        }
        System.out.println("redraw " + redraw_buttons + " redraw_card " + redraw_cards);
        System.out.println("skonczylem update_drawing_table()");

        //this.repaint();
      //  repaint();
    }

    void update_drawing_cards(){
        System.out.println("Update_drawing_cards");
        if (poker_game != null && redraw_cards) {
            int key, index1, index2;
            String value;
            for (Map.Entry m : hands.entrySet()) {
                key = (int) m.getKey();
                value = (String) m.getValue();
                if (my_pos == key) {
                    String test = value.substring(0, 2);
                    String test2 = value.substring(2);
                    if (test.contains("T")) index1 = 32;
                    else if (test.contains("J")) index1 = 36;
                    else if (test.contains("Q")) index1 = 40;
                    else if (test.contains("K")) index1 = 44;
                    else if (test.contains("A")) index1 = 48;
                    else index1 = (Character.getNumericValue(value.charAt(0)) - 2) * 4;

                    if (test2.contains("T")) index2 = 32;
                    else if (test2.contains("J")) index2 = 36;
                    else if (test2.contains("Q")) index2 = 40;
                    else if (test2.contains("K")) index2 = 44;
                    else if (test2.contains("A")) index2 = 48;
                    else index2 = (Character.getNumericValue(value.charAt(2)) - 2) * 4;


                    //index2 = Character.getNumericValue(value.charAt(2));


                    switch (value.charAt(1)) {
                        case 'd':
                            index1 += 1;
                            break;
                        case 'c':
                            index1 += 2;
                            break;
                        case 's':
                            index1 += 3;
                            break;
                    }
                    switch (value.charAt(3)) {
                        case 'd':
                            index2 += 1;
                            break;
                        case 'c':
                            index2 += 2;
                            break;
                        case 's':
                            index2 += 3;
                            break;
                    }

                    cards[index1].setBounds(408, 360, 60, 84);
                    cards[index1].setVisible(true);
                    cards[index2].setBounds(468, 360, 60, 84);
                    cards[index2].setVisible(true);
                    arr_index_my_cards[0] = index1;
                    arr_index_my_cards[1] = index2;

                }
                else{
                    if (poker_game.all_in){
                        String test = value.substring(0, 2);
                        String test2 = value.substring(2);
                        if (test.contains("T")) index1 = 32;
                        else if (test.contains("J")) index1 = 36;
                        else if (test.contains("Q")) index1 = 40;
                        else if (test.contains("K")) index1 = 44;
                        else if (test.contains("A")) index1 = 48;
                        else index1 = (Character.getNumericValue(value.charAt(0)) - 2) * 4;

                        if (test2.contains("T")) index2 = 32;
                        else if (test2.contains("J")) index2 = 36;
                        else if (test2.contains("Q")) index2 = 40;
                        else if (test2.contains("K")) index2 = 44;
                        else if (test2.contains("A")) index2 = 48;
                        else index2 = (Character.getNumericValue(value.charAt(2)) - 2) * 4;


                        //index2 = Character.getNumericValue(value.charAt(2));


                        switch (value.charAt(1)) {
                            case 'd':
                                index1 += 1;
                                break;
                            case 'c':
                                index1 += 2;
                                break;
                            case 's':
                                index1 += 3;
                                break;
                        }
                        switch (value.charAt(3)) {
                            case 'd':
                                index2 += 1;
                                break;
                            case 'c':
                                index2 += 2;
                                break;
                            case 's':
                                index2 += 3;
                                break;
                        }

                        cards[index1].setBounds(408, 5, 60, 84);
                        cards[index1].setVisible(true);
                        cards[index2].setBounds(468, 5, 60, 84);
                        cards[index2].setVisible(true);
                        arr_index_my_cards[2] = index1;
                        arr_index_my_cards[3] = index2;
                        if (poker_game.level == 3 || poker_game.level == 4){
                            try {
                                Thread.sleep(511);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else {
                        covered_cards[2 * key].setBounds(408, 5, 60, 84);
                        covered_cards[2 * key].setVisible(true);
                        covered_cards[2 * key + 1].setBounds(468, 5, 60, 84);
                        covered_cards[2 * key + 1].setVisible(true);
                    }


                }
            }
        }
        else{
            cards[arr_index_my_cards[0]].setVisible(false);
            cards[arr_index_my_cards[1]].setVisible(false);
            cards[arr_index_my_cards[2]].setVisible(false);
            cards[arr_index_my_cards[3]].setVisible(false);
            covered_cards[0].setVisible(false);
            covered_cards[1].setVisible(false);
            covered_cards[2].setVisible(false);
            covered_cards[3].setVisible(false);

            for (int i = 0 ; i < 5; i++){
                try{
                    cards[arr_index_board[i]].setVisible(false);
                }catch (Exception e2){break;}
            }
        }
        System.out.println("poker_game.level " + poker_game.level);
        if (poker_game.level == 1 && redraw_cards){
            System.out.println("w ifie");
            try {
                for (int i = 0; i < 3; i++) {
                   // System.out.println("For " + i);
                    String test = poker_game.deck_arr[i];
                  //  System.out.println("test " + test);

                    int index1 = 0;
                    if (test.contains("T")) index1 = 32;
                    else if (test.contains("J")) index1 = 36;
                    else if (test.contains("Q")) index1 = 40;
                    else if (test.contains("K")) index1 = 44;
                    else if (test.contains("A")) index1 = 48;
                    else index1 = (Character.getNumericValue(test.charAt(0)) - 2) * 4;

                    switch (test.charAt(1)) {
                        case 'd':
                            index1 += 1;
                            break;
                        case 'c':
                            index1 += 2;
                            break;
                        case 's':
                            index1 += 3;
                            break;
                    }
                    System.out.println("index " + index1);
                    cards[index1].setBounds(330 + i * 63, 220, 60, 84);
                    cards[index1].setVisible(true);
                    arr_index_board[i] = index1;
                    //arr_index_my_cards[1] = index2;

                }
            }catch (Exception ee){
                ee.printStackTrace();
            }
        }
        else if (poker_game.level == 2 && redraw_cards){
            int index1 = 0;
            String test = poker_game.deck_arr[3];
            if (test.contains("T")) index1 = 32;
            else if (test.contains("J")) index1 = 36;
            else if (test.contains("Q")) index1 = 40;
            else if (test.contains("K")) index1 = 44;
            else if (test.contains("A")) index1 = 48;
            else index1 = (Character.getNumericValue(test.charAt(0)) - 2) * 4;

            switch (test.charAt(1)) {
                case 'd':
                    index1 += 1;
                    break;
                case 'c':
                    index1 += 2;
                    break;
                case 's':
                    index1 += 3;
                    break;
            }
            System.out.println("index " + index1);
            cards[index1].setBounds(519, 220, 60, 84);
            cards[index1].setVisible(true);
            arr_index_board[3] = index1;

        }
        else if (poker_game.level == 3  && redraw_cards){
            int index1 = 0;
            String test = poker_game.deck_arr[4];
            if (test.contains("T")) index1 = 32;
            else if (test.contains("J")) index1 = 36;
            else if (test.contains("Q")) index1 = 40;
            else if (test.contains("K")) index1 = 44;
            else if (test.contains("A")) index1 = 48;
            else index1 = (Character.getNumericValue(test.charAt(0)) - 2) * 4;

            switch (test.charAt(1)) {
                case 'd':
                    index1 += 1;
                    break;
                case 'c':
                    index1 += 2;
                    break;
                case 's':
                    index1 += 3;
                    break;
            }
            System.out.println("index " + index1);
            cards[index1].setBounds(582, 220, 60, 84);
            cards[index1].setVisible(true);
            arr_index_board[4] = index1;

        }

    }

    void deep_copy_poker(){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = null;
            oos = new ObjectOutputStream(bos);
            oos.writeObject(poker_game);
            oos.flush();
            oos.close();
            bos.close();
            byte[] byteData = bos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(byteData);
            poker_game_send = (Poker) new ObjectInputStream(bais).readObject();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }


    @Override
    public void mousePressed(MouseEvent e) {

        if (e.getSource() == button_fold) {
            System.out.println("CLIK FOLD");
            poker_game.make_move = true;
            poker_game.folded = true;
            poker_game.my_postion = my_turn_pos;
            button_fold.setVisible(false);
            button_call.setVisible(false);
            button_raise.setVisible(false);
            slider.setVisible(false);
            slider_size.setVisible(false);
            small_buttons[0].setVisible(false);
            small_buttons[1].setVisible(false);
            small_buttons[2].setVisible(false);
            small_buttons[3].setVisible(false);
            cards[arr_index_my_cards[0]].setVisible(false);
            cards[arr_index_my_cards[1]].setVisible(false);
            for (int i = 0 ; i < 5; i++){
                try{
                    cards[arr_index_board[i]].setVisible(false);
                }catch (Exception e2){break;}
            }
            covered_cards[0].setVisible(false);
            covered_cards[1].setVisible(false);
            covered_cards[2].setVisible(false);
            covered_cards[3].setVisible(false);
            redraw_buttons = false;
            redraw_cards = false;
            System.out.println("heh " + poker_game.make_move);
            deep_copy_poker();

        }
        else if (e.getSource() == button_raise){
            System.out.println("CLIK Raise");
            poker_game.make_move = true;
            poker_game.my_postion = my_turn_pos;
            System.out.println("old stacks " + poker_game.stacks);
            int buf = poker_game.stacks.get(my_pos);
            poker_game.stacks.put(my_pos, buf - Integer.parseInt(slider_size.getText()) + poker_game.bets[my_turn_pos]);
            System.out.println("new stacks " + poker_game.stacks);
            //poker_game.player_turnplayer_turn = (poker_game.player_turn + 1) % 2;
            poker_game.bets[my_turn_pos] = Integer.parseInt(slider_size.getText());
            button_fold.setVisible(false);
            button_call.setVisible(false);
            button_raise.setVisible(false);
            small_buttons[0].setVisible(false);
            small_buttons[1].setVisible(false);
            small_buttons[2].setVisible(false);
            small_buttons[3].setVisible(false);
            slider.setVisible(false);
            slider_size.setVisible(false);
            redraw_buttons = false;
            redraw_cards = true;
            deep_copy_poker();

        }
        else if (e.getSource() == button_call){
            Integer[] copiedArray = new Integer[2];
            System.arraycopy(poker_game.bets, 0, copiedArray, 0, 2);
            Arrays.sort(copiedArray);
            System.out.println("CLIK Call");
            int buf = poker_game.stacks.get(my_pos);

            if (copiedArray[1] > poker_game.stacks.get(my_pos) + poker_game.bets[my_turn_pos]){
                poker_game.bets[my_turn_pos] = buf + poker_game.bets[my_turn_pos];
                poker_game.stacks.put(my_pos, 0);

            }
            else{
                poker_game.stacks.put(my_pos, buf - poker_game.bets[(my_turn_pos + 1 ) % 2] + poker_game.bets[my_turn_pos]);
                poker_game.bets[my_turn_pos] = copiedArray[1];
            }
            poker_game.make_move = true;
            poker_game.my_postion = my_turn_pos;
            button_fold.setVisible(false);
            button_call.setVisible(false);
            button_raise.setVisible(false);
            small_buttons[0].setVisible(false);
            small_buttons[1].setVisible(false);
            small_buttons[2].setVisible(false);
            small_buttons[3].setVisible(false);
            slider.setVisible(false);
            slider_size.setVisible(false);
            redraw_buttons = false;
            redraw_cards = true;
            poker_game.called = true;
            deep_copy_poker();

        }
        else if (e.getSource() == small_buttons[0]){
            if (small_buttons[0].getText().equalsIgnoreCase("2BB")) slider.setValue(200);
            else{
                double xd =  Double.parseDouble(pot.getText()) * 0.33;
                slider.setValue((int) xd);
            }
        }
        else if (e.getSource() == small_buttons[1]){
            if (small_buttons[1].getText().equalsIgnoreCase("2.5BB")) slider.setValue(250);
            else{
                double xd =  Double.parseDouble(pot.getText()) * 0.5;
                slider.setValue((int) xd);
            }
        }
        else if (e.getSource() == small_buttons[2]){
            if (small_buttons[2].getText().equalsIgnoreCase("POT")) slider.setValue(Integer.parseInt(pot.getText()) * 3);
            else
            {
                double xd =  Double.parseDouble(pot.getText()) * 0.75;
                slider.setValue((int) xd);
            }

        }
        else if (e.getSource() == small_buttons[3]){
            slider.setValue(Integer.parseInt(pot.getText()));
        }
        else if (e.getSource() == seat_empty[0]){
            if (e.getClickCount() == 2) {
                ingame = true;
                my_pos = 0;
                my_turn_name = name;
                seat_taken[0].setVisible(true);
               // seat_taken[0].setText(name);
                //names[0].setText(name);
              //  cashes[0].setText("8888");
              //  cashes[0].setVisible(true);
                seat_taken[0].setCursor(null);
                seat_empty[0].setVisible(false);

                seat_empty[1].removeMouseListener(this);
                bufor_sitting.put(0, name);
               // players_sitting.put(0, name);
             //   stacks.put(0, 9999);


            }
        }
        else if (e.getSource() == seat_empty[1]){
            if (e.getClickCount() == 2) {
                ingame = true;
                my_pos = 1;
                my_turn_name = name;
               // players_sitting.put(1, name);
              //  stacks.put(1, 9999);
                System.out.println("Sitting" + players_sitting);


                for (int i = 0; i < 2; i++){
                    if (i - 1 < 0) index = 2 - i - 1;
                    else index = i - 1;
                    //  System.out.println("i=" + i + " inedex=" + index);
                    if (players_sitting.get(i) != null){
                        //  System.out.println("W ifie - " + players_sitting.get(i));
                       // seat_taken[index].setText(players_sitting.get(i));
                      //  names[index].setText(players_sitting.get(i));
                     //   cashes[index].setVisible(true);
                     //   cashes[index].setText(stacks.get(i).toString());


                        seat_taken[index].setVisible(true);
                        seat_taken[index].setCursor(null);

                        seat_empty[index].setVisible(false);

                    }
                    else {
                        //     System.out.println("W else");
                        seat_taken[index].setVisible(false);

                        seat_empty[index].setVisible(true);
                        seat_empty[index].removeMouseListener(this);
                    }
                    bufor_sitting.put(1, name);
                }
            }

        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (e.getSource() == button_fold){
            button_fold.setIcon(button_hover);
            button_fold.setVisible(true);
        }
        else if (e.getSource() == button_call) button_call.setIcon(button_hover);
        else if (e.getSource() == button_raise) button_raise.setIcon(button_hover);

    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (e.getSource() == button_fold) button_fold.setIcon(button_empty);
        else if (e.getSource() == button_call) button_call.setIcon(button_empty);
        else if (e.getSource() == button_raise) button_raise.setIcon(button_empty);

    }

    @Override
    public void stateChanged(ChangeEvent e) {
        Integer xd =  slider.getValue();
        slider_size.setText(xd.toString());
    }
}