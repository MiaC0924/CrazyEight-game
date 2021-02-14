package com.Mia;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer implements Serializable {
    private static final long serialVersionUID = 1L;

    Server[] servers = new Server[4];
    Player[] players = new Player[4];
    int[]    winSingle = new int[players.length];

    ServerSocket ss;

    int  highestScore;
    int  numPlayers;
    Card faceCard;
    Game game;

    //Constructor
    public GameServer() {
        game = new Game();
        numPlayers = 0;
        highestScore = 0;

        for (int i = 0; i < winSingle.length; i++) {
            winSingle[i] = 0;
        }

        System.out.println("Starting game server");

        // initialize the players list with new players
        for (int i = 0; i < players.length; i++) {
            players[i] = new Player(" ");
        }

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
        try {
            System.out.println("Waiting for players...");
            while (numPlayers < 4) {
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
            System.out.println("Four players have joined the game");

            // start the server threads
            for (int i = 0; i < servers.length; i++) {
                Thread t = new Thread(servers[i]);
                t.start();
            }
            // start their threads
        } catch (IOException ex) {
            System.out.println("Could not connect 4 players");
        }
    }

    //TODO: finish this method
    public void gameLoop() {
        faceCard = game.dealCard();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < players.length; j++) {
                servers[j].sendCard(game.dealCard());
            }
        }

//        try {
//            while (highestScore < 100) {
//
//
//            }
//        }

//
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

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
                System.out.println("Received card " + c.toString() + " from player " + playerId);
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
    public static void main(String args[]) throws Exception {
        GameServer sr = new GameServer();

        sr.acceptConnections();
        sr.gameLoop();
    }

}
