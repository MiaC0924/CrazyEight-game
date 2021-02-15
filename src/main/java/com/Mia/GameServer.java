package com.Mia;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class GameServer implements Serializable {
    private static final long serialVersionUID = 1L;

    Server[] servers;
    Player[] players;
    int[]    winSingle;

    ServerSocket ss;

    int  highestScore, numPlayers, round;
    Card faceCard;
    Game game;

    //Constructor
    public GameServer() {
        game = new Game();
        numPlayers = 0;
        highestScore = 0;
        round = 1;

        System.out.println("Starting game server");

        try {
            ss = new ServerSocket(3333);
        } catch (IOException ex) {
            System.out.println("Server Failed to open");
        }
    }

    /**
     * -----------Networking stuff ----------
     * refer to the given example yahtzeeGame
     */
    public void acceptConnections() throws ClassNotFoundException {
        int num = 0;
        while(num != 3 && num != 4) {
            System.out.println("Please enter number of players(3/4): ");
            Scanner myObj = new Scanner(System.in);
            num = myObj.nextInt();
        }

        servers = new Server[num];
        players = new Player[num];
        winSingle = new int[players.length];

        for (int i = 0; i < winSingle.length; i++) {
            winSingle[i] = 0;
        }

        for (int i = 0; i < players.length; i++) {
            players[i] = new Player(" ");
        }

        try {
            System.out.println("Waiting for players...");
            while (numPlayers < num) {
                Socket s = ss.accept();
                numPlayers++;

                Server server = new Server(s, numPlayers);

                // send the player number
                server.dOut.writeInt(server.playerId);
                server.dOut.flush();

                // get the player name
                Player in = (Player) server.dIn.readObject();
                System.out.println("Player " + server.playerId + " ~ " + in.getName() + " ~ has joined");
                // add the player to the player list
                players[server.playerId - 1] = in;
                servers[numPlayers - 1] = server;
            }
            System.out.println(numPlayers + " players have joined the game");

            // start the server threads
            for (int i = 0; i < servers.length; i++) {
                Thread t = new Thread(servers[i]);
                t.start();
            }
            // start their threads
        } catch (IOException ex) {
            System.out.println("Could not connect" + num + "players");
        }
    }

    //TODO: finish this method
    public void gameLoop() {
        try {
            while(true){
                faceCard = game.dealCard();
                int next = 1;

                //deal base 5 cards
                for (int j = 0; j < 5; j++) {
                    for (int i = 0; i < players.length; i++) {
                        Card cc = game.dealCard();
                        servers[i].sendCard(cc);
                        players[i].getHandCards().addCard(cc);
                    }
                }

                //initial cantPlay signal
                for (int i = 0; i < players.length; i++) {
                    players[i].cantPlay = 0;
                }

                int whoPlay = (round % players.length) - 1;
                int takeSingle = 0;

                while(true){
                    int needCard = 0;

                    System.out.println();
                    System.out.println("Current face card is " + faceCard.toString());

                    servers[whoPlay].sendPlaySignal();   //send signal to player
                    servers[whoPlay].sendCard(faceCard); //send face card to player

                    //if face card is 2, take 2 card if possible
                    if(faceCard.getRank() == 2){
                        if(game.getNumOfCard() >= 2) {
                            takeSingle = 2;
                        }else if(game.getNumOfCard() == 1){
                            takeSingle = 1;
                        }
                    }

                    servers[whoPlay].sendInt(takeSingle);  //send signal to player about# of card take

                    //send card after get face card = 2
                    if(takeSingle == 2){
                        servers[whoPlay].sendCard(game.dealCard());
                        servers[whoPlay].sendCard(game.dealCard());
                        takeSingle = 0;
                    }else if(takeSingle == 1){
                        servers[whoPlay].sendCard(game.dealCard());
                        takeSingle = 0;
                    }

                    //receive card from player, check if player could play
                    //if no, the receive card = null
                    //if need card, deal a card to player
                    needCard = servers[whoPlay].receiveInt();
                    while(needCard == 1){
                        servers[whoPlay].sendCard(game.dealCard());
                        needCard = servers[whoPlay].receiveInt(); //receive one more time see if needed (current attempt+1)
                    }

                    //receive int cantPlay = 0 -> can play
                    //                     = 1 -> can not play
                    //receive Card - the card from player
                    players[whoPlay].cantPlay = servers[whoPlay].receiveInt();
                    Card temp = servers[whoPlay].receiveCard();

                    //if this player can not play a card after 3 attempts
                    if(temp == null) {
                        System.out.println("Player " + players[whoPlay].getName() + " can't play card");
                    }

                    //if this player played a card
                    if(temp != null) {
                        System.out.println("Player " + players[whoPlay].getName() + " deals a card " + temp.toString());
                        faceCard = temp;
                    }
//
//                    //update current player card deck in server
//                    players[whoPlay].setHandCards(servers[whoPlay].receiveCardDeck());

                    //check if round end by server
                    if (game.ifRoundEnd(players)) {
                        System.out.println("Round ended");
                        for (int i = 0; i < players.length; i++) {
                            servers[i].sendBoolean(true); //send to player.endRound
                            players[i].setPlayerScore(servers[i].receiveInt());
                        }
                        break;
                    }
                    servers[whoPlay].sendBoolean(false); //if not end, send false to player


                    //check if round end by player
                    int endRound = servers[whoPlay].receiveInt(); //1 -> end; 0 -> not end
                    if(endRound == 1){
                        System.out.println("Round ended");
                        players[whoPlay].setPlayerScore(servers[whoPlay].receiveInt());
                        break;
                    }

                    //if Round not end, go next player
                    //below calculate who is the next player
                    int tempIdx;
                    if (faceCard.getRank() == 11) {
                        tempIdx = (whoPlay + next*2) % players.length;
                    } else if (faceCard.getRank() == 1) {
                        next = -next;
                        tempIdx = (whoPlay + next) % players.length;
                    } else {
                        tempIdx = (whoPlay + next) % players.length;
                    }

                    if(tempIdx < 0) {
                        whoPlay = players.length + tempIdx;
                    }else {
                        whoPlay = tempIdx;
                    }

                    //end of the inner while loop
                }

                //check if the game is ended
                if(game.ifGameEnd(players)) {
                    System.out.println("-- Game ended --");
                    Player winner = game.getWinner(players);
                    for (int i = 0; i < players.length; i++) {
                        servers[i].sendBoolean(true);
                        servers[i].sendPlayer(winner);
                    }
                    break;
                }
                servers[whoPlay].sendBoolean(false);
                ++round;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Server Class
     * refer to sample game
     * */
    public class Server implements Runnable {
        private Socket socket;
        private ObjectInputStream dIn;
        private ObjectOutputStream dOut;
        private int playerId;

        public Server(Socket s, int playerid) {
            socket = s;
            playerId = playerid;
            try {
                dOut = new ObjectOutputStream(socket.getOutputStream());
                dIn = new ObjectInputStream(socket.getInputStream());
            } catch (IOException ex) {
                System.out.println("Server Connection failed");
            }
        }

        /*
         * run function for threads
         */
        public void run() {
            try {
                while (true) {
                }

            } catch (Exception ex) {
                {
                    System.out.println("Run failed");
                    ex.printStackTrace();
                }
            }
        }

        /*
         * functions to send data to the players
         */
        public void sendCard(Card c) {
            try {
                dOut.writeObject(c);
                dOut.flush();
            } catch (IOException ex) {
                System.out.println("No card sent");
                ex.printStackTrace();
            }
        }

        public void sendPlayer(Player pl) {
            try {
                dOut.writeObject(pl);
                dOut.flush();
            } catch (IOException ex) {
                System.out.println("No player sent");
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
                System.out.println("Data not sent");
                ex.printStackTrace();
            }
        }

        public void sendPlaySignal() {
            try {
                dOut.writeBoolean(true);
                dOut.flush();
            } catch (IOException ex) {
                System.out.println("Play signal not sent");
                ex.printStackTrace();
            }
        }

        /*
         * functions to receive data from players
         */
        public Player receivePlayer() {
            try {
                Player p = (Player) dIn.readObject();
                System.out.println("Receive player " + p.getName());
                return p;
            } catch (IOException e) {
                System.out.println("Player not received");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                System.out.println("Class not found");
                e.printStackTrace();
            }
            return null;
        }

        public Card receiveCard() {
            try {
                Card c = (Card) dIn.readObject();
                if(c != null) {
                    System.out.println("Received card " + c.toString() + " from player " + playerId);
                }
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

        public CardDeck receiveCardDeck() {
            try {
                CardDeck cd = (CardDeck) dIn.readObject();
                return cd;
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
    public static void main(String args[]) throws Exception {
        GameServer sr = new GameServer();

        sr.acceptConnections();
        sr.gameLoop();
    }

}
