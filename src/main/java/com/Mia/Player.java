package com.Mia;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Scanner;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    static  Client clientConnection;

    int playerId = 0;
    int winSingle = 0;

    private String   name;
    private int      playerScore = 0;
    private CardDeck handCards;

    //constructor
    public Player(String n){
        name      = n;
        handCards = new CardDeck(5);
    }

    //getter
    public Player   getPlayer()      {return this;}
    public String   getName()        {return name;}
    public int      getPlayerScore() {return playerScore;}
    public CardDeck getHandCards()   {return handCards;}
    public int      getNumOfCards()  {return handCards.getSize();}

    //take card 拿牌
    public void takeCard(Card c){
        handCards.addCard(c);
    }

    //play a card from hand 出牌 - 考虑了是否可以出牌，是整个出牌环节，包括输入读取
    public Card playCard(Card face) {
        Scanner myObj = new Scanner(System.in);
        boolean valid = false;
        int selection = -1;

        if(canPlay(face)){
            //ask player to play a card
            while (valid == false) {
                System.out.println("Please select a hand card to play: ");
                System.out.println("- enter '1' to play the first card.");
                System.out.println("- enter '2' to play the second card, etc.");

                selection = myObj.nextInt();
                valid = validInput(selection, face);
            }

            return handCards.playSelectedCard(selection);
        }

        System.out.println("No valid card to be played, please get a card from dealer");
        return null;
    }

    //helper - check if any hand card can be played 检测出牌状态
    private boolean canPlay(Card face) {
        for(Card c: handCards.cards) {
            if (c.match(face)) return true;
        }
        return false;
    }

    //helper - check if the input is valid selection
    private boolean validInput(int i, Card face){
        if(i < 1 || i >= handCards.getSize()){
            System.out.println("The index is invalid");
            return false;
        }else if(!handCards.cards.get(i).match(face)){
            System.out.println("The selected card does not match");
            return false;
        }
        return true;
    }

    //calculate score 计算目前总分，无返回值
    public void calculateScore() {
        if(handCards.cards == null || handCards.cards.size() == 0) return;
        for(Card c: handCards.cards){
            playerScore += c.getScore();
        }
    }

    //print hand cards 显示手上的卡牌
    public void printHandCards() {
        if(this.getNumOfCards() == 0){
            System.out.println("You have no card on hand.");
        }
        System.out.println("----- Your hand cards ----- \n" + "[");
        for (int i = 0; i < this.getNumOfCards()-1; i++) {
            System.out.println(this.getHandCards().cards.get(i-2).toString() + ", ");
        }
        System.out.println(this.getHandCards().cards.get(this.getNumOfCards()-1).toString() + "]\n");
    }


    /**
     * ----------Network Stuff------------
     */
    public void sendScoreToServer() { clientConnection.sendInt(playerScore);}
    public void sendPlayerToServer()  { clientConnection.sendPlayer();}
    public void sendCardToServer(Card c) { clientConnection.sendCard(c);}
    public void sendWinSignal() { clientConnection.sendInt(winSingle);}

    public void receiveWinSignal(){ clientConnection.receiveInt();}

    public void connectToClient() { clientConnection = new Client(); }
    public void connectToClient(int port) { clientConnection = new Client(port);}

    //TODO: complete method
    public void startGame() {
        //winSingle = 0;
        for (int i = 0; i < 5; i++) {
            takeCard(clientConnection.receiveCard());
        }
        printHandCards();
//        while (true) {
//            System.out.println("\n \n \n ********Round Number " + round + "********");
//            int[][] pl = clientConnection.receiveScores();
//            for (int i = 0; i < 3; i++) {
//                players[i].setScoreSheet(pl[i]);
//            }
//            printPlayerScores(players);
//            int[] dieRoll = game.rollDice();
//            clientConnection.sendScores(playRound(dieRoll));
//        }

    }

    public Player returnWinner() {
        try{
            winSingle = clientConnection.receiveInt();
            if(winSingle == 1){
                System.out.println("You win!");
            }else {
                Player win = (Player) clientConnection.dIn.readObject();
                System.out.println("The winner is " + win.getName() + " with score " + win.getPlayerScore() + "\n");
                return win;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *  Client class for socket connection
     *  Please refer to the given example yahtzeeGame
     * */
    private class Client {
        Socket socket;
        private ObjectInputStream dIn;
        private ObjectOutputStream dOut;

        //Ctor
        public Client() {
            try {
                socket = new Socket("localhost", 3333);
                dOut = new ObjectOutputStream(socket.getOutputStream());
                dIn = new ObjectInputStream(socket.getInputStream());

                playerId = dIn.readInt();

                System.out.println("Connected as " + playerId);
                sendPlayer();

            } catch (IOException ex) {
                System.out.println("Client failed to open");
            }
        }

        public Client(int portId) {
            try {
                socket = new Socket("localhost", portId);
                dOut = new ObjectOutputStream(socket.getOutputStream());
                dIn = new ObjectInputStream(socket.getInputStream());

                playerId = dIn.readInt();

                System.out.println("Connected as " + playerId);
                sendPlayer();

            } catch (IOException ex) {
                System.out.println("Client failed to open");
            }
        }

        /*
         * functions to send data to the server
         */
        public void sendPlayer() {
            try {
                dOut.writeObject(getPlayer());
                dOut.flush();
            } catch (IOException ex) {
                System.out.println("Player not sent");
                ex.printStackTrace();
            }
        }

        public void sendCard(Card c) {
            try {
                dOut.writeObject(c);
                dOut.flush();
            } catch (IOException ex) {
                System.out.println("Card not sent");
                ex.printStackTrace();
            }
        }

        public void sendInt(int i) {
            try {
                dOut.writeInt(i);
                dOut.flush();
            } catch (IOException ex) {
                System.out.println("Data not sent");
                ex.printStackTrace();
            }
        }

        /*
         * functions to receive data from server
         */
        public Card receiveCard() {
            try {
                Card c = (Card) dIn.readObject();
                return c;
            } catch (IOException e) {
                System.out.println("Card not received");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                System.out.println("Class not found");
                e.printStackTrace();
            }
            return null;
        }

        public int receiveInt() {
            try {
                return dIn.readInt();
            } catch (IOException e) {
                System.out.println("Data not received");
                e.printStackTrace();
            }
            return 0;
        }

    }

    /** main */
    public static void main(String args[]) {
        Scanner myObj = new Scanner(System.in);

        System.out.print("What is your name ? ");
        String name = myObj.next();
        Player p = new Player(name);

        p.connectToClient();
        p.startGame();
        //p.returnWinner();
        myObj.close();
    }
}

