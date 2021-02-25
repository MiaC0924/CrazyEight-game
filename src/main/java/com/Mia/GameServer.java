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
    int[]    winSignal;

    ServerSocket ss;

    int  highestScore, numPlayers, round, takeSignal, direction;
    Card faceCard;
    Game game;

    //Constructor
    public GameServer() {
        game = new Game();
        numPlayers = highestScore = 0;
        round = direction = 1;

        System.out.println("Starting game server..");

        try {
            ss = new ServerSocket(3333);
        } catch (IOException ex) {
            System.out.println("Server Failed to open");
        }
    }

    /**
     * -----------Networking stuff ----------
     * refer to the given example yahtzeeGame
     * initial server to match number of players
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
        winSignal = new int[players.length];

        for (int i = 0; i < winSignal.length; i++) {
            winSignal[i] = 0;
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

    /**
     * Game Logic of Server
     */
    public void gameLoop() {
        try {
            /** Whole game while loop */
            while(true){
                game = new Game();

                boolean endRound;
                faceCard = game.takeCard();
                direction = 1;

                //take send 5 cards to each player at the beginning of the round
                for (int j = 0; j < 5; j++) {
                    for (int i = 0; i < players.length; i++) {
                        Card cc = game.takeCard();
                        servers[i].sendCard(cc);
                        players[i].getHandCards().addCard(cc);
                    }
                }

                //initial signals
                takeSignal = 0; // 0,1,2,3,4 - # of cards to take

                for (int i = 0; i < players.length; i++) {
                    players[i].canPlay = true;
                }

                //set the starting player by round
                int whoPlay = (round % players.length) - 1;
                if (whoPlay < 0) whoPlay += players.length;
                int prvPlay = (whoPlay + 1) % players.length;

                System.out.println("\n\n---------- NEW ROUND ----------");
                System.out.println("----------  ROUND " + round + "  ----------");

                /** Whole round while loop */
                while(true){
                    boolean needCard = false;
                    int getTwo = 0;

                    System.out.println();
                    System.out.println("Current face card is " + faceCard.toString());
                    System.out.println("Current player is player number " + (whoPlay+1) );

                    /** Single player while loop */
                    while(true){
                        servers[whoPlay].sendBoolean(true);   //send playSignal to player
                        servers[whoPlay].sendCard(faceCard); //send face card to player

                        //if face card is 2, take 2 card if possible
                        if(faceCard.getRank() == 2){
                            ++getTwo;
                            if(game.getNumOfCard() >= 4 && getTwo == 2 && players[prvPlay].canPlay){
                                takeSignal = 4;
                                getTwo = 0;
                            }else if(game.getNumOfCard() < 4 && getTwo == 2 && players[prvPlay].canPlay){
                                System.out.println("Remain " + game.getNumOfCard() + " cards in deck");
                                takeSignal = game.getNumOfCard();
                                getTwo = 0;
                            }else if(game.getNumOfCard() >= 2) {
                                takeSignal = 2;
                            }else if(game.getNumOfCard() == 1){
                                System.out.println("Remain " + game.getNumOfCard() + " cards in deck");
                                takeSignal = 1;
                            }
                        }

                        servers[whoPlay].sendInt(takeSignal);  //send signal to player about# of card take

                        //send card after get face card = 2
                        if(takeSignal == 4) {
                            for (int i = 0; i < 4; i++) {
                                servers[whoPlay].sendCard(game.takeCard());
                            }
                            takeSignal = 0;
                        }else if(takeSignal == 3){
                            for (int i = 0; i < 3; i++) {
                                servers[whoPlay].sendCard(game.takeCard());
                            }
                            takeSignal = 0;
                        }else if(takeSignal == 2){
                            for (int i = 0; i < 2; i++) {
                                servers[whoPlay].sendCard(game.takeCard());
                            }
                            takeSignal = 0;
                        }else if(takeSignal == 1){
                            servers[whoPlay].sendCard(game.takeCard());
                            takeSignal = 0;
                        }

                        //receive card from player, check if player could play
                        //if no, the receive card = null
                        //if need card, deal a card to player
                        needCard = servers[whoPlay].receiveBoolean();
                        while(needCard == true){
                            servers[whoPlay].sendCard(game.takeCard());
                            needCard = servers[whoPlay].receiveBoolean(); //receive one more time see if needed (current attempt+1)
                        }

                        //receive boolean canPlay = true -> can play
                        //                        = false -> can not play
                        //receive Card - the card from player
                        players[whoPlay].canPlay = servers[whoPlay].receiveBoolean();
                        Card temp = servers[whoPlay].receiveCard();

                        //if this player can not play a card after 3 attempts
                        if(temp == null) {
                            System.out.println("Player " + players[whoPlay].getName() + " can't play card");
                            break;
                        }

                        //if this player played a card
                        if(temp != null) {
                            System.out.println("Player " + players[whoPlay].getName() + " deals a card " + temp.toString());
                            faceCard = temp;
                            break;
                        }

                        /** end of player round loop */
                    }

                    //check if round end by server

//                    if (game.ifRoundEnd(players)) {
//                        System.out.println("Round ended");
//                        for (int i = 0; i < players.length; i++) {
//                            servers[i].sendBoolean(endRound = true);
//                            players[i].setPlayerScore(servers[i].receiveInt());
//                        }
//                        break;
//                    }
//                    servers[whoPlay].sendBoolean(endRound = false);


                    //check if round end by player
                    endRound = servers[whoPlay].receiveBoolean();
                    if(endRound){
                        System.out.println("\n**** Round ended by player number " + whoPlay + " ****");
                        break;
                    }

                    //if not ended, go next player
                    if(faceCard.getRank() == 1) direction = -direction;
                    int next = game.whosNext(whoPlay, direction, players.length, faceCard);
                    servers[whoPlay].sendInt(next); //tell current player who is the next
                    servers[whoPlay].sendInt(direction); //tell current play the direction
                    prvPlay = whoPlay;
                    whoPlay = next;

                    /** end of the round while loop */
                }

                if(endRound){
                    //tell all players the round is ended
                    for (int i = 0; i < players.length; i++) {
                        if (i != whoPlay) {
                            servers[i].sendBoolean(false); //playSignal is false
                        }
                    }

                    for (int i = 0; i < players.length; i++) {
                        players[i].setPlayerScore(servers[i].receiveInt());
                        System.out.println("Score of player " + (i+1) + " is " + players[i].getPlayerScore());
                    }

                    //send winner to who losers
                    for (int i = 0; i < players.length; i++) {
                        if (i != whoPlay) {
                            servers[i].sendInt(whoPlay); //playSignal is false
                        }
                    }

                    //send player scores
                    for (int i = 0; i < players.length; i++) {
                        servers[i].sendInt(players.length);
                        for (int j = 0; j < players.length; j++) {
                            servers[i].sendInt(players[j].getPlayerScore());
                        }
                    }

                }

                //check if the game is ended
                if(game.ifGameEnd(players)) {
                    System.out.println("\n\n-- Game ended --");
                    int winner = 100;
                    int temp = 100;
                    game.printFinalResult(players);

                    for (int i = 0; i < players.length; i++) {
                        if(players[i].getPlayerScore() < temp) {
                            temp = players[i].getPlayerScore();
                            winner = i;
                        }
                    }

                    for (int i = 0; i < players.length; i++) {
                        servers[i].sendBoolean(true); //send signal endGame = true to player

                        if(i == winner){
                            servers[i].sendBoolean(true); //youWin = true
                        }else{
                            servers[i].sendBoolean(false); //youWin = false
                            servers[i].sendInt(winner);
                        }
                    }
                    break;

                }else{ //if the game is not ended
                    //servers[whoPlay].sendBoolean(false); //send signal endGame = false to player
                    for (int i = 0; i < players.length; i++) {
                        servers[i].sendBoolean(false); //send signal endGame = false to player
                    }
                    ++round;
                }

                /** end of the game while loop */
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

        /*
         * functions to receive data from players
         */
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

        public int receiveInt() {
            try {
                return dIn.readInt();
            } catch (IOException e) {
                System.out.println("Data not received");
                e.printStackTrace();
            }
            return 0;
        }

        public boolean receiveBoolean() {
            try {
                return dIn.readBoolean();
            } catch (IOException e) {
                System.out.println("Boolean not received");
                e.printStackTrace();
            }
            return false;
        }


    }

    /** main */
    public static void main(String args[]) throws Exception {
        GameServer sr = new GameServer();
        sr.acceptConnections();
        sr.gameLoop();
    }

}
