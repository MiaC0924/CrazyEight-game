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
            //take base 5 cards then print on terminal
            System.out.println("\n\n---------- NEW ROUND ----------");
            System.out.println("Taking 5 hand cards..");
            for (int i = 0; i < 5; i++) {
                takeCard(client.receiveCard());
            }
            printHandCards();

            /** term loop */
            while (true) {
                //could add an signal to indicated if the round is ended

                playSignal = client.receiveBoolean();
                if (playSignal == false) {
                    System.out.println("This round is ended!");
                    calculateScore();
                    System.out.println("Your current score is " + playerScore);
                    client.sendInt(playerScore);
                    handCards = new CardDeck(0); //clean handCard
                    break;
                }

                if (playSignal == true) {
                    //get face card
                    Card faceCard = client.receiveCard();

                    //if face card is 2, next player may take 2 or 4 cards
                    int takeSignal = client.receiveInt(); //signal to take card
                    if (takeSignal == 4) {
                        System.out.println();
                        System.out.println("** Face card is [" + faceCard.toString() + "], take four cards");
                        for (int i = 0; i < 4; i++) {
                            takeCard(client.receiveCard());
                        }
                    } else if (takeSignal == 3) {
                        System.out.println();
                        System.out.println("** Face card is [" + faceCard.toString() + "], take cards in deck");
                        for (int i = 0; i < 3; i++) {
                            takeCard(client.receiveCard());
                        }
                    } else if (takeSignal == 2) {
                        System.out.println();
                        System.out.println("** Face card is [" + faceCard.toString() + "], take two cards");
                        for (int i = 0; i < 2; i++) {
                            takeCard(client.receiveCard());
                        }
                    } else if (takeSignal == 1) {
                        System.out.println("** Take a card");
                        takeCard(client.receiveCard());
                    }

                    /** loop for play or take card, max 3 attempt */
                    int attempt = 1;
                    boolean playerRound = true;
                    while (playerRound) {
                        System.out.println();
                        System.out.println("It's your turn. Current face card is [" + faceCard.toString() + "]");

                        Card temp = playCard(faceCard);
                        if (temp != null) {
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
                        } else if (temp == null && attempt < 4) {
                            System.out.println("Take a card from card deck");
                            client.sendBoolean(needCard = true);
                            takeCard(client.receiveCard());
                            ++attempt;
                        } else {
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

                //check if round end by server
//                endRound = client.receiveBoolean();
//                if(endRound) {
//                    calculateScore();
//                    client.sendInt(playerScore);
//                    break;
//                }

                //check if round end by player
                if(handCards.getSize() == 0){
                    System.out.println("You end this round!");
                    client.sendBoolean(endRound = true);
                    calculateScore();
                    System.out.println("sending score, your current score is " + playerScore);
                    client.sendInt(playerScore);
                    System.out.println("Finish sending score.\n");
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
                Player winner = getPlayer();
                if(winner.playerId == this.playerId){
                    System.out.println("You win!");
                }else{
                    System.out.println("The winner is " + winner.getName() + " with score " + winner.getPlayerScore());
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

