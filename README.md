# COMP3004 Assignment 02

Wenwen Chen 100882644

The following code contains a 3-4 players CrazyEight game

The 3-4 players connect to a central server. 
Each player gets to play a round of the CrazyEight game in a turn based game. There are 13 rounds and the rounds go in order Player 1 - 2 - 3. At the end of the game bonuses are added and a winner is displayed. At every turn the server knows the score of all the players and displays it as well as the current round number. 

Junit tests to check the game functionality use TDD approach
Cucumber tests are used in a BDD approach to test the game on three levels:
  1) Scoring and playing a single round
  2) A single player joining and ending the game
  3) A round played by three players joining the server and getting the scores

Import the game through eclipse by connecting it to the Git repository using the url. You can also download the code from Github and import the project into eclipse.

How to run the code through eclipse:
1) Start the GameServer 
2) Start the Player (add three players)
3) Game starts playing with player 1 and for 13 rounds

How to run the Cucumber tests:
  1) Start the server for testing - Server Test three times:
    a) 1 player and port 3000 - to test ending a game
    b) 1 player and port 3001 - to test starting a game
    c) 3 players and port 3002 - to test a full game
  2) Start 2 iterations of the gameNetwork.feature (This is used to replicated 2/3 players as parallel testing is difficult in    cucumber) 
  3) Run TestSuite.java as a Junit test
  
Note:For Junit tests make sure the code is built with a jUnit dependency