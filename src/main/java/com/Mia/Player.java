package com.Mia;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Scanner;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    static  Client client;

    int playerId = 0;

    private String   name;
    private int      playerScore = 0;
    private CardDeck handCards;
    boolean canPlay, needCard;

    //constructor
    public Player(String n){
        name      = n;
        handCards = new CardDeck(5);
    }

    //getters
    public Player   getPlayer()      {return this;}
    public String   getName()        {return name;}
    public int      getPlayerScore() {return playerScore;}
    public CardDeck getHandCards()   {return handCards;}
    public int      getNumOfCards()  {return handCards.getSize();}
    public void     setPlayerScore(int i) {playerScore = i;}

    //take card 拿一张牌
    public void takeCard(Card c){
        if (c != null) handCards.addCard(c);
    }

    //play a card from hand 出牌 - 考虑了是否可以出牌，是整个出牌环节，包括输入读取
    public Card playCard(Card face) {
        Scanner myObj = new Scanner(System.in);
        boolean valid = false;
        int selection = -1;

        if(canPlay(face)){
            //ask player to play a card
            while (valid == false) {
                System.out.println("You have available cards");
                System.out.println("Please play a hand card, enter index 1/2/3/... or enter 0 for not playing a card: ");
                printHandCards();

                selection = myObj.nextInt();
                if(selection == 0) return null;

                valid = validInput(selection-1, face);
            }
            return handCards.playSelectedCard(selection-1);
        }

        System.out.println("You don't have valid card to play");
        return null;
    }

    public Card playCard2(Card face) {
        Scanner myObj = new Scanner(System.in);
        boolean valid = false;
        int selection = -1;

        if(canPlay(face)){
            //ask player to play a card
            while (valid == false) {
                System.out.println("Please select a hand card, enter index 1/2/3/...: ");
                printHandCards();

                selection = myObj.nextInt();
                if(selection == 0) return null;

                valid = validInputForce(selection-1, face);
            }
            return handCards.playSelectedCard(selection-1);
        }

        System.out.println("You don't have valid card to play");
        return null;
    }

    //ask player to select a suit
    public String selectSuit(){
        char c;
        while (true) {
            System.out.println("Please select a suit (enter S/H/D/C) :");
            Scanner myObj = new Scanner(System.in);
            c = myObj.nextLine().charAt(0);

            if (c == 'S' || c == 'H' || c == 'D' || c == 'C')
                System.out.println("Replace the suit as " + c);
                return Character. toString(c);
        }
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
        if(i < -1 || i >= handCards.getSize()){
            System.out.println("The index is invalid");
            return false;
        }else if(!handCards.cards.get(i).match(face)){
            System.out.println("The selected card does not match");
            return false;
        }
        return true;
    }

    private boolean validInputForce(int i, Card face){
        if(i < 0 || i >= handCards.getSize()){
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
        System.out.println("----- Your hand cards -----");
        for (int i = 0; i < this.getNumOfCards()-1; i++) {
            System.out.print("[" + this.getHandCards().cards.get(i).toString() + "], ");
        }
        System.out.print("[" + this.getHandCards().cards.get(this.getNumOfCards()-1).toString() + "]\n");
    }


    /**
     * ----------Network Stuff------------
     */

    public void connectToClient() { client = new Client(); }

    /**
     *  Player Game Logic
     * */
    public void startGame() {
        boolean playSignal;

        /** round loop */
        while (true) {
            boolean skip2 = false;

            //take base 5 cards then print on terminal
            System.out.println("\n\n-------------------- NEW ROUND --------------------");
            System.out.println("Taking 5 hand cards..");
            for (int i = 0; i < 5; i++) {
                takeCard(client.receiveCard());
            }
            printHandCards();

            /** term loop */
            while (true) {
                playSignal = client.receiveBoolean();

                //round end by other player
                if (playSignal == false) {
                    System.out.println("\nThis round is ended!");

                    //calculate and send score to server
                    calculateScore();
                    client.sendInt(playerScore);

                    //receive winner
                    int winner = client.receiveInt() + 1;
                    System.out.println("Winner of this round is player number" + winner + "\n");
                    System.out.println("\n*** *** *** *** ***");

                    //receive and print scores
                    int numPlayer = client.receiveInt();
                    for (int i = 0; i < numPlayer; i++) {
                        int currentPlayerScore = client.receiveInt();
                        System.out.println("Score of player " + (i+1) + " is " + currentPlayerScore);
                    }

                    handCards = new CardDeck(0); //clean handCard
                    break;
                }

                //ask to play
                if (playSignal == true) {
                    //get face card
                    Card faceCard = client.receiveCard();

                    /** loop for play or take card, max 3 attempt */
                    int attempt = 1;
                    boolean playerRound = true;
                    boolean taken = false;
                    Card lastTake = null;
                    boolean couldBreakByHandle2 = false;

                    while (playerRound) {
                        System.out.println();
                        System.out.println("It's your turn. Current face card is [" + faceCard.toString() + "]");

                        /**if face card is 2, player play 2/4 cards if possible*/
                        if(faceCard.getRank() == 2 && skip2 == false){
                            int available = 0;
                            skip2 = true;

                            for (int i = 0; i < handCards.getSize(); i++) {
                                if(handCards.cards.get(i).match(faceCard))
                                    ++available;
                            }
                            client.sendInt(available);

                            int plySignal = client.receiveInt();

                            if (plySignal == 2){
                                //first 2, and player can play 2 cards immediately
                                System.out.println("You have 2 available cards");
                                Card temp1 = playCard2(faceCard);
                                client.sendCard(temp1);
                                System.out.println("Please select one more available card");
                                Card temp2 = playCard2(faceCard);
                                client.sendCard(temp2);
                            }else if (plySignal == 4){
                                //second 2, and player can play 4 cards immediately
                                System.out.println("You have 4 available cards");
                                Card temp1 = playCard2(faceCard);
                                client.sendCard(temp1);
                                System.out.println("Please select the second available card");
                                Card temp2 = playCard2(faceCard);
                                client.sendCard(temp2);
                                System.out.println("Please select the third available card");
                                Card temp3 = playCard2(faceCard);
                                client.sendCard(temp3);
                                System.out.println("Please select the fourth available card");
                                Card temp4 = playCard2(faceCard);
                                client.sendCard(temp4);
                            }else if (plySignal == 22){
                                System.out.println();
                                System.out.println("** Face card is [" + faceCard.toString() + "], " +
                                        "you don't have two available cards, take two cards");
                                for (int i = 0; i < 2; i++) {
                                    takeCard(client.receiveCard());
                                }
                                printHandCards();
                            }else if (plySignal == 44){
                                System.out.println();
                                System.out.println("** Face card is [" + faceCard.toString() + "], " +
                                        "you don't have four available cards, take four cards");
                                for (int i = 0; i < 4; i++) {
                                    takeCard(client.receiveCard());
                                }
                                printHandCards();
                            }
                            couldBreakByHandle2 = client.receiveBoolean();
                        }

                        if(couldBreakByHandle2) break;

                        /**if doesn't end by 2*/
                        if(attempt == 1){
                            Card temp = playCard(faceCard);

                            if (temp != null){
                                client.sendBoolean(needCard = false);
                                client.sendBoolean(canPlay = true);

                                if(temp.getRank() == 8){
                                    String suit = selectSuit();
                                    temp.setCard(suit, 8);
                                }

                                client.sendCard(temp);
                                System.out.println("You played [" + temp.toString() +"]");

                                if(temp.getRank() == 1){
                                    System.out.println("Change direction by playing 'A'.");
                                }

                                if(temp.getRank() == 12){
                                    System.out.println("Skip one player by playing 'Q'.");
                                }

                                playerRound = false;
                            }else{
                                System.out.println("Take a card from card deck");
                                client.sendBoolean(needCard = true);
                                lastTake = client.receiveCard();
                                takeCard(lastTake);
                                ++attempt;
                            }
                        }else if(attempt > 1 && attempt < 5){
                            if(lastTake != null && lastTake.match(faceCard)){
                                printHandCards();
                                System.out.println("Last taken card [" + lastTake.toString() + "] match face card, must play it.");
                                client.sendBoolean(needCard = false);
                                client.sendBoolean(canPlay = true);

                                Card played = handCards.playSelectedCard(handCards.getSize()-1);

                                if(played.getRank() == 8){
                                    String suit = selectSuit();
                                    played.setCard(suit, 8);
                                }

                                System.out.println("You played [" + played.toString() +"]");
                                client.sendCard(played);

                                if(played.getRank() == 1){
                                    System.out.println("Change direction by playing 'A'.");
                                }

                                if(played.getRank() == 12){
                                    System.out.println("Skip one player by playing 'Q'.");
                                }

                                playerRound = false;
                            }else{
                                if(lastTake != null) {
                                    System.out.println("Taken card [" + lastTake.toString() + "] doesn't match the face card. Take a card from card deck");
                                }else{
                                    System.out.println("Table card deck empty");
                                }
                                client.sendBoolean(needCard = true);
                                lastTake = client.receiveCard();
                                takeCard(lastTake);
                                ++attempt;
                            }
                        }else{
                            printHandCards();
                            client.sendBoolean(needCard = false);
                            client.sendBoolean(canPlay = false);
                            client.sendCard(null);
                            playerRound = false;
                        }

                        /** play/take loop end */
                    }
                }

                boolean endRound;

                //check if round end by player
                if(handCards.getSize() == 0){
                    System.out.println("\n**** You end this round! ****\n");

                    //send server the round is ended
                    client.sendBoolean(endRound = true);

                    //calculate and send score to server
                    calculateScore();
                    client.sendInt(playerScore);

                    //receive and print scores
                    int numPlayer = client.receiveInt();
                    for (int i = 0; i < numPlayer; i++) {
                        int currentPlayerScore = client.receiveInt();
                        System.out.println("Score of player " + (i+1) + " is " + currentPlayerScore);
                    }

                    break;
                }
                client.sendBoolean(endRound = false);

                //if not ended, print who is the next player
                int next = client.receiveInt() + 1;
                int direction = client.receiveInt();
                if(direction == 1) {
                    System.out.println("\nGo left. The next player is player number " + next);
                } else {
                    System.out.println("\nGo right. The next player is player number " + next);
                }

                /**end of term loop*/
            }

            //check if the game is ended
            boolean endGame = client.receiveBoolean();
            if(endGame){
                boolean youWin = client.receiveBoolean();
                if(youWin){
                    System.out.println("\n**** You win! ****");
                }else{
                    int winner = client.receiveInt() + 1;
                    System.out.println("\nThe winner of the game is player number " + winner);
                }
                break;
            }

            /** end of game loop */
        }
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

        public void sendCardDeck(CardDeck cd) {
            try {
                dOut.writeObject(cd);
                dOut.flush();
            } catch (IOException ex) {
                System.out.println("Card deck not sent");
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

        public void sendBoolean(boolean b) {
            try {
                dOut.writeBoolean(b);
                dOut.flush();
            } catch (IOException ex) {
                System.out.println("Boolean not sent");
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

        public Boolean receiveBoolean() {
            try {
                return dIn.readBoolean();
            } catch (IOException e) {
                //System.out.println("Data not received");
                e.printStackTrace();
            }
            return false;
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
        myObj.close();
    }
}

