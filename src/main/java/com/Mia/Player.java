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
    int endSingle = 0;
    int cantPlay = 0;

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
    public void     setPlayerScore(int i) {playerScore = i;}
    public void     setHandCards(CardDeck cd) {handCards = cd;}

    //take card 拿牌
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
                System.out.println("Please play a hand card: ");
                printHandCards();
                System.out.println("- enter '1' to play the first card.");
                System.out.println("- enter '2' to play the second card, etc.");

                selection = myObj.nextInt();
                valid = validInput(selection-1, face);
            }

            return handCards.playSelectedCard(selection-1);
        }

        System.out.println("No valid card to be played");
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
    public void sendScoreToServer() { clientConnection.sendInt(playerScore);}
    public void sendPlayerToServer()  { clientConnection.sendPlayer();}
    public void sendCardToServer(Card c) { clientConnection.sendCard(c);}
    public void sendCardDeckToServer(CardDeck cd) { clientConnection.sendCardDeck(cd);}
    public void sendInt(int i) { clientConnection.sendInt(i);}

    public boolean receiveBoolean(){ return clientConnection.receiveBoolean();}
    public boolean receivePlaySignal(){ return clientConnection.receiveBoolean();}
    public int receiveDealSignal(){ return clientConnection.receiveInt();}
    public Card receiveCard(){ return clientConnection.receiveCard();}

    public void connectToClient() { clientConnection = new Client(); }

    //Game logic for player end
    public void startGame() {
        boolean playSingle = false;
        cantPlay = 0;

        while (true) {
            //take base 5 cards
            for (int i = 0; i < 5; i++) {
                takeCard(receiveCard());
            }
            printHandCards();

            while (true) {
                //get single to start playing
                playSingle = receivePlaySignal();
                if (playSingle) {
                    //get face card
                    Card faceCard = receiveCard();

                    //if face card is 2, you will receive this
                    int takeSingle = receiveDealSignal();
                    if (takeSingle == 2) {
                        System.out.println();
                        System.out.println("** Deal two cards");
                        takeCard(receiveCard());
                        takeCard(receiveCard());
                    } else if (takeSingle == 1) {
                        System.out.println("**Deal a card");
                        takeCard(receiveCard());
                    }

                    //check if there's hand card available, max 3 attempt
                    int attempt = 1;
                    while (true) {
                        System.out.println();
                        System.out.println("It's your turn. Current face card is [" + faceCard.toString() + "]");

                        Card temp = playCard(faceCard);
                        if (temp != null) {
                            cantPlay = 0;  //can play a card
                            sendInt(0); //server.needCard = 0
                            sendInt(cantPlay);
                            sendCardToServer(temp);
                            System.out.println("You played [" + temp.toString() +"]\n");
                            break;
                        } else if (temp == null && attempt < 4) {
                            System.out.println("Get a card from dealer");
                            sendInt(1); //server.needCard = 1  -> need a card
                            takeCard(receiveCard());
                            printHandCards();
                        } else {
                            cantPlay = 1;  //can not play card
                            sendInt(0); //server.needCard = 0
                            sendInt(cantPlay);
                            sendCardToServer(temp);
                            break;
                        }
                        attempt++;
                    }
                }

//                sendCardDeckToServer(handCards);

                //check if round end by server
                boolean endRound = receiveBoolean(); //true -> end; false -> not end
                if(endRound) {
                    calculateScore();
                    sendScoreToServer(); //calculate the score from current hand cards
                    break;
                }

                //check if round end by player
                if(handCards.getSize() == 0){
                    sendInt(1); //send to server.endRound
                    calculateScore();
                    sendScoreToServer(); //calculate the current score
                    break;
                }
                sendInt(0); //send to playerEndGame

                //end of inner while loop
            }

            //check if the game is ended
            boolean endGame = false;
            endGame = receiveBoolean();
            if(endGame){
                Player winner = getPlayer();
                if(winner.playerId == this.playerId){
                    System.out.println("You win!");
                }else{
                    System.out.println("The winner is " + winner.getName() + " with score " + winner.getPlayerScore());
                }
                break;
            }
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

