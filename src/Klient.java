import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import static java.awt.Cursor.*;


class Plansza extends JFrame implements MouseListener
{
    private Socket sock;
    Table table;
    Table[] tables = new Table[2];
    Poker[] poker = new Poker[2];
    JFrame frame;
    JPanel login_panel, sign_panel;
    ImageIcon icon_table, icon_lobby, icon_login, icon_login_button, icon_empty, icon_player;
    JLabel table_preview, table_preview2, lobby, opaque, opaque2, login_banner, login_button, sign_up, sign_background, register_button, register_cancel, reg_sucess, login_waiting, cash, nick;
    JLabel[] name_empty = new JLabel[4];
    JLabel[] name_player = new JLabel[4];
    int empty_coords[] = {153, 320, 153, 195, 495, 320, 495, 195};
    JTextField login, password, register_password, register_login;
    JCheckBox remember_me;
    int height = 720;
    int weight = 1210;
    private BufferedReader sockReader;
    private InputStream inputStream;
    private ObjectInputStream objectInputStream;
    HashMap<Integer,String> players_sitting = new HashMap<Integer,String>();
    HashMap<Integer,String> players_sitting2 = new HashMap<Integer,String>();
    ArrayList<HashMap<Integer,String>> table_list = new ArrayList<HashMap<Integer,String>>();
    private OutputStream outputStream;
    private ObjectOutputStream objectOutputStream;
    private PrintWriter outp;
    boolean flag_register = false, flag_login = false;
    boolean[] opened_windows = {false, false};
    int counter = 0;
    Message recived_msg, send_msg;
    Integer cashier;
    String name;

    Plansza(Socket sock) throws IOException
    {

        cash = new JLabel();
        cash.setBounds(1100, 145,  90 ,33);
        cash.setForeground(Color.ORANGE);
        cash.setHorizontalTextPosition(JLabel.CENTER);
        cash.setVerticalTextPosition(JLabel.CENTER);


        nick = new JLabel();
        nick.setBounds(1115, 85,  85 ,27);
        nick.setForeground(Color.ORANGE);
        nick.setHorizontalTextPosition(JLabel.CENTER);
        nick.setVerticalTextPosition(JLabel.CENTER);
        cash.setFont(new Font("Verdana", Font.PLAIN, 15));


        table_list.add(players_sitting);
        table_list.add(players_sitting2);

        outputStream = sock.getOutputStream();
        objectOutputStream = new ObjectOutputStream(outputStream);
        outp = new PrintWriter(sock.getOutputStream());

        this.sockReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        inputStream = sock.getInputStream();
        objectInputStream = new ObjectInputStream(inputStream);
        this.sock = sock;

        remember_me = new JCheckBox();
        remember_me.setLocation(450, 333);
        remember_me.setVisible(true);
        remember_me.setBounds(450, 333, 20, 20);

        login = new JTextField();
        login.setBounds(450, 213, 330, 30);

        password = new JTextField();
        password.setBounds(450, 290, 330, 30);

        icon_table = new ImageIcon("JAVA_Stars/Table_preview.png");
        icon_lobby = new ImageIcon("JAVA_Stars/Lobby_GG.png");
        icon_login = new ImageIcon("JAVA_Stars/login.png");
        icon_login_button = new ImageIcon("JAVA_Stars/login_button.png");
        icon_empty = new ImageIcon("JAVA_Stars/Player_empty.png");
        icon_player = new ImageIcon("JAVA_Stars/PlayerName_empty.png");

        opaque = new JLabel();
        opaque.setBounds(0, 0,  weight ,height);
        opaque.setBackground(new Color(20,20,20,232));
        opaque.setOpaque(true);

        login_banner = new JLabel();
        login_banner.setBounds(365, 105,  478 ,505);
        login_banner.setIcon(icon_login);

        login_button = new JLabel();
        login_button.setBounds(365, 395,  478 , 88);
        login_button.setIcon(icon_login_button);
        login_button.addMouseListener(this);
        Cursor cursor = new Cursor(Cursor.HAND_CURSOR);
        login_button.setCursor(cursor);

        login_waiting = new JLabel();
        login_waiting.setBounds(470, 423,  150 , 33);
        login_waiting.setIcon(icon_login_button);
        login_waiting.setVisible(false);
        login_waiting.setIcon(new ImageIcon("JAVA_Stars/login_waiting.png"));



        lobby = new JLabel();
        lobby.setBounds(0, 0,  weight ,height);
        lobby.setIcon(icon_lobby);

        table_preview = new JLabel();
        table_preview.setBounds(15 , 125 , 310, 355);
        table_preview.setIcon(icon_table);
        table_preview.setCursor(new Cursor(Cursor.HAND_CURSOR));
        table_preview.addMouseListener(this);

        table_preview2 = new JLabel();
        table_preview2.setBounds(350 , 125 , 310, 355);
        table_preview2.setIcon(icon_table);
        table_preview2.setCursor(new Cursor(Cursor.HAND_CURSOR));
        table_preview2.addMouseListener(this);

        sign_up = new JLabel();
        sign_up.setBounds(670, 550, 100, 33);
        sign_up.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sign_up.addMouseListener(this);

        reg_sucess = new JLabel("Konto zostalo utworzone");
        reg_sucess.setBounds(450, 370, 330, 30);
        reg_sucess.setForeground(Color.RED);
        reg_sucess.setVisible(false);


        login_panel = new JPanel();
        login_panel.setBounds(0, 0 , weight, height);
        login_panel.setLayout(null);
        login_panel.add(reg_sucess);
        login_panel.add(sign_up);
        login_panel.add(remember_me);
        login_panel.add(login);
        login_panel.add(password);
        login_panel.add(login_waiting);
        login_panel.add(login_button);
        login_panel.add(login_banner);
        login_panel.add(opaque);

        sign_background = new JLabel();
        sign_background.setIcon(new ImageIcon("JAVA_Stars/Sign_up_panel.png"));
        sign_background.setBounds(200, 55, 789, 606);

        register_login = new JTextField();
        register_login.setBounds(378, 306, 200, 22);

        register_password = new JTextField();
        register_password.setBounds(378, 427, 200, 22);

        opaque2 = new JLabel();
        opaque2.setBounds(0, 0,  weight ,height);
        opaque2.setBackground(new Color(20,20,20,232));
        opaque2.setOpaque(true);

        register_button = new JLabel();
        register_button.setBounds(830, 605, 140, 50);
        register_button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        register_button.addMouseListener(this);

        register_cancel = new JLabel();
        register_cancel.setBounds(220, 605, 140, 50);
        register_cancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        register_cancel.addMouseListener(this);

        sign_panel = new JPanel();
        sign_panel.setBounds(0, 0 , weight, height);
        sign_panel.setLayout(null);
        sign_panel.setVisible(false);
        sign_panel.add(register_button);
        sign_panel.add(register_cancel);
        sign_panel.add(sign_background);
        sign_panel.add(register_login);
        sign_panel.add(register_password);
        sign_panel.add(opaque2);



        /*
        frame.add(remember_me);
        frame.add(login);
        frame.add(password);
        frame.add(login_button);
        frame.add(login_banner);
        frame.add(opaque);

         */

        frame = new JFrame();

        frame.add(sign_panel);
        frame.add(login_panel);

        for (int i = 0; i < 4; i++){
            name_empty[i] = new JLabel();
            name_empty[i].setVisible(true);
            name_empty[i].setBounds(empty_coords[i*2], empty_coords[i*2+1], 30, 26);
            name_empty[i].setIcon(icon_empty);
            frame.add(name_empty[i]);
        }
        for (int i = 0; i < 4; i++){
            name_player[i] = new JLabel();
            name_player[i].setForeground(Color.WHITE);
            name_player[i].setVisible(false);
            name_player[i].setBounds(empty_coords[i*2] - 20, empty_coords[i*2+1], 77, 27);
            name_player[i].setIcon(icon_player);
            name_player[i].setHorizontalTextPosition(JLabel.CENTER);
            name_player[i].setVerticalTextPosition(JLabel.CENTER);
            frame.add(name_player[i]);
        }

        frame.add(cash);
        frame.add(nick);
        frame.add(table_preview);
        frame.add(table_preview2);
        frame.add(lobby);



        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(weight, height);
        frame.setLayout(null);
        frame.setVisible(true);

        int x;
        while(true) {
            try {
                x = login_waiting.getX() + 25;
                if (x > 545) x = 490;
                login_waiting.setLocation(x, login_waiting.getY());
                //System.out.println("GO test");
                System.out.println("________________________________________________________________");
                System.out.println("Counter = " + counter++);
                System.out.println("odbieram");


                /*
                for (int i = 0; i < 2; i++){
                    try{
                        System.out.println(i + " bufor " + tables[i].bufor_sitting);
                    }catch (Exception e){;}
                }


                 */
                recived_msg = (Message) objectInputStream.readObject();
                send_msg = new Message();

                if (flag_register){
                    System.out.println("SQL_send");
                    flag_register = false;
                    send_msg.register(register_login.getText(), register_password.getText());
                }
                if (flag_login){
                    System.out.println("SQL_send2");
                    flag_login = false;
                    send_msg.login(login.getText(), password.getText());
                }
                if (recived_msg.flag_login){
                    System.out.println("SQL_reciv");
                    if (recived_msg.login_sucess){
                        login_panel.setVisible(false);
                        frame.remove(login_panel);
                        cashier = recived_msg.cashier;
                        name = login.getText();
                        cash.setText("$ " + cashier.toString());
                        nick.setText(name);

                        // frame.repaint();
                     //   login_panel.setVisible(false);
                    }
                    else {
                        reg_sucess.setText("Niepoprawny login, i/lub haslo");
                        reg_sucess.setVisible(true);
                        login_waiting.setVisible(false);
                    }
                }


                download_tables();
                draw_tables();

                download_game();

                if (tables[1] != null){
                    System.out.println("hands " + tables[1].hands);
                    if (tables[1].poker_game != null) System.out.println("turn " + tables[1].poker_game.players_turn_name);

                }
                update_tables();
                update_games();

                for (int i = 0; i < 2; i++) {
                    try {
                        System.out.println("Wwhilu update i " + i + " player_sitting " +  tables[i].players_sitting);
                        tables[i].update_drawing_table();
                    }catch (Exception e){;}

                    try {
                        tables[i].update_drawing_cards(); //ROWNIEZ PRZESYLAM W TEJ FUNC DICT HANDS Z CLASS POKER
                    }catch (Exception e){ ; }
                }

                //objectInputStream.reset();

                Thread.sleep(888);
                System.out.println("wysyalm");
                objectOutputStream.writeUnshared(send_msg);
                /*
                for (int i = 0; i < 2; i++){
                    try{
                        System.out.println(i + " bufor " + tables[i].bufor_sitting);
                    }catch (Exception e){;}
                }

                 */

               // System.out.println("Table " + recived_msg.players_sitting);

            }catch (Exception e) {
                e.printStackTrace();
            };


        }

    }

    void download_game(){
        String new_games;
        System.out.println("download_game()");

        //poker = recived_msg.poker_games;

        for (int i = 0; i < 2; i++) {
            System.out.println("For i " + i);
            try {
                tables[i].poker_game = recived_msg.poker_games[i];
                tables[i].hands = recived_msg.poker_games[i].hands;//WAZNA LINIJKA
                System.out.println(tables[i].poker_game);
                System.out.println(",.......");
                tables[i].players_turn_name = recived_msg.players_turn_name.get(i);
                System.out.println("2 etap");
                System.out.println("hands tables[i]" + tables[i].hands);
                System.out.println("TURN tables[i]" + tables[i].players_turn_name);

            }catch (Exception e){
                System.out.println("jakies problemy w doowlaod game");
                //e.printStackTrace();
            }

        }

    }


    void download_tables(){
        System.out.println("download_tables()");

       // table_list = recived_msg.players_sitting;
        System.out.println("TAble list " + recived_msg.players_sitting);

        for (int i = 0; i < 2; i++) {
            try {
                System.out.println("Etap 1 ");
                tables[i].players_sitting = recived_msg.players_sitting.get(i); // updatowanie stolow w game aby dobrzy rysowaly
                tables[i].stacks = recived_msg.stacks.get(i); // updatowanie stolow w game aby dobrzy rysowaly

                System.out.println("Etap 2 ");
                System.out.println("Table[i] " + tables[i].players_sitting);
                System.out.println("Stacks[i] " + tables[i].stacks);


            }catch (Exception ee){;}
        }

    }

    void update_tables(){
        System.out.println("update_tables()");
       // System.out.println("wazne " + tables[0].players_sitting + " " + tables[1].players_sitting);
        Integer size;
        for (int i = 0; i < 2; i++) {
            if (opened_windows[i]){
                System.out.println("Opened " + i);
                if (tables[i].closed){
                    System.out.println("no zamknalem");
                    Integer var = 2*tables[i].table + tables[i].my_pos;
                    name_player[var].setVisible(false);
                    name_empty[var].setVisible(true);
                    send_msg.append(name, i);
                    opened_windows[i] = false;
                    tables[i] = null;
                }
            }
            if (tables[i] != null) {
                for (Map.Entry m : tables[i].bufor_sitting.entrySet()) {
                    send_msg.append(m.getValue().toString(), (Integer) m.getKey(), i);
                    tables[i].bufor_sitting.remove(m.getKey());
                }
                if (tables[i].closed){

                }
            }
        }
        System.out.println("end update_tables()");

    }

    void update_games(){
        System.out.println("update_games()");
        for (int i = 0; i < 2; i++) {
            System.out.println("For i = " + i);
            if (tables[i] != null) {
                System.out.println("WAZNE " + tables[i].poker_game_send);
                if (tables[i].poker_game != null) System.out.println("shit " + tables[i].poker_game.all_in);
                if (tables[i].poker_game_send != null) {
                    System.out.println("shit2 " + tables[i].poker_game_send.all_in);
                    send_msg.append(tables[i].poker_game_send, i);
                    tables[i].poker_game_send = null;

                }
                //else send_msg.append(tables[i].poker_game, i);


            }
        }
        System.out.println("END update_games()");

    }

    void draw_tables(){
        System.out.println("draw_tables()");
        System.out.println(recived_msg.players_sitting);

        int index = 0, var;
        for (int i = 0; i < 2; i++){
            // int index = 0, var;
            for (int j = 0; j < 2; j++){
                var = 2*index + j;
                if (recived_msg.players_sitting.get(i).containsKey(j)) {
                    name_player[var].setText(recived_msg.players_sitting.get(i).get(j));
                    name_player[var].setVisible(true);
                    name_empty[var].setVisible(false);
                }
                else{
                    name_player[var].setVisible(false);
                    name_empty[var].setVisible(true);
                }
            }
            /*
            for (Map.Entry m : recived_msg.players_sitting.get(i).entrySet()) {
                var = 2*index + (int) m.getKey();
                name_player[var].setText((String) m.getValue());
                name_player[var].setVisible(true);
                name_empty[var].setVisible(false);
            }

             */
            System.out.println("Narysowalem");
            index += 1;
        }
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == table_preview) {
            if (e.getClickCount() == 2) {
                tables[0] = new Table(name, recived_msg.players_sitting.get(0), recived_msg.stacks.get(0), 0, this);
                opened_windows[0] = true;
                System.out.println("mouse clicked tab0");
            }
        }
        else if (e.getSource() == table_preview2) {
            if (e.getClickCount() == 2) {
                tables[1] = new Table(name, recived_msg.players_sitting.get(1), recived_msg.stacks.get(1), 1, this);
                opened_windows[1] = true;
                System.out.println("mouse clicked tab1");

            }
        }
        else if (e.getSource() == login_button){
            System.out.println(login.getText() + " ss " + password.getText());
            if (!login.getText().isEmpty() && !password.getText().isEmpty()) {
                flag_login = true;
                login_waiting.setVisible(true);
            }

                /*
            if (login.getText().isEmpty() && password.getText().isEmpty()){
                System.out.println("no loginegk");
                frame.remove(login_panel);
                frame.repaint();
            }

             */
        }
        else if (e.getSource() == sign_up){
            System.out.println(login.getText() + " ss " + password.getText());
            login_panel.setVisible(false);
            sign_panel.setVisible(true);

        }
        else if (e.getSource() == register_cancel){
            System.out.println(login.getText() + " ss " + password.getText());
            login_panel.setVisible(true);
            sign_panel.setVisible(false);

        }
        else if (e.getSource() == register_button){
            System.out.println("no register");


            System.out.println(login.getText() + " ss " + password.getText());
            if (!register_login.getText().isEmpty() && !register_password.getText().isEmpty()){
                System.out.println("no register");
                login_panel.setVisible(true);
                sign_panel.setVisible(false);
                reg_sucess.setVisible(true);
                reg_sucess.setText("Konto zostalo utworzone");
                flag_register = true;
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}

public class Klient
{
    public static final int PORT =50007;
    public static final String HOST = "0.0.0.0";// "139.162.187.12";

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket sock;
        sock=new Socket(HOST,PORT);
        System.out.println("Nawiazalem polaczenie: "+sock);



        Plansza p;
        p = new Plansza(sock);
        /*
        JFrame jf=new JFrame();
        jf.add(p);

        jf.setTitle("Test grafiki");
        jf.setSize(1400,900);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);
        //nawiazanie polaczenia z serwerem
*/

        //tworzenie watka odbierajacego
        //new Odbior2(sock).start();


/*
        BufferedReader klaw;
        klaw=new BufferedReader(new InputStreamReader(System.in));
        PrintWriter outp;
        outp=new PrintWriter(sock.getOutputStream());

        String str;
        while ((str = klaw.readLine()) != null){
            if (str.equalsIgnoreCase("exit") || sock.isClosed()) // 5.1 // 5.2
            {
                outp.println(str);
                outp.flush();
                System.out.println("Koniec polaczenia");
                klaw.close();
                outp.close();
                sock.close();
                break;

            }
            else
            {
                System.out.print("<Wysylamy:> ");
                outp.println(str);
                outp.flush();
            }
        }

 */
    }



    //zamykanie polaczenia

}






