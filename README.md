# COMP3004 Assignment 02

Wenwen Chen 100882644

------
Introduction

The following code contains a 3-4 players CrazyEight game

The 3-4 players connect to a central server. 
The test case is for 4 players.
The game consists of several rounds, each round concluding by computing the score of each player for that round and updating their game scores accordingly.

You could also refer to video https://www.youtube.com/watch?v=iDQjn3k76Mw for game description of each round.

A round is over as soon as one player plays their last card. Or no one could play card anymore. After the round ended, each player scores the total value of the cards in their hand as follows: 
- 8s score 50 points
- kings, queens and jacks are worth 10 points
- all other cards are worth their face value. 

And, the scores for the current round are added to those of the previous rounds. 

The game is over once any of the players reaches 100 points. 
The winner is the player with the lowest score and the game is over.

------
Testing

Import the game through eclipse / intelliJ by connecting it to the Git repository using the url.
You can also download the code from Github and import the project into eclipse / intelliJ.

How to run the code:
1) Start the GameServer 
2) Start the Player (add 3 or 4 players)
3) Game starts playing with player 1 and change to next player every round
