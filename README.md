# PlanetsGame
A simple 2D pvp game. Use the gravity of the planets to your advantage to win.

2-4 players
## How to Play
### Controls
- **W**: Jump
- **A**: Move counter-clockwise around the planet you are standing on
- **S**: Move clockwise around the planet you are standing on
- **D**: When in the air, press D to fall down to the nearest planet faster
- **Space/Left Click**: Pressing will shoot a small projectile. Holding, then releasing the button will charge then release a larger projectile that will move faster
- **Mouse**: Use the mouse to aim where you want to shoot
### Playing the Game
- Hit your enemies with your projectiles to deal damage and get your enemies' health bars to 0
- Be the last person standing to win
- Both you and your projectiles will be affected by the gravity of each planet, so the key to winning is using the orbit of your projectiles to your advantage.

## Running the Game
### If you've downloaded the code
- Compile and run '/PlanetsGameServer/src/main/RunGameServer' which contains the main function for the server
- Then compile and run '/PlanetsGameClient/src/main/PlayGame' which contains the main function for the client (2-4 times depending on how many players you want)
### If you're using the .jar files
- First run "PlanetsGameServer.jar"
- Then run "PlanetsGameClient.jar" (2-4 times depending on how many players you want)
## Setting up the Game
Note: This game is only set up to run locally, so all players need to be on the same network in order to play.
### Playing on different computers
1. Find the IP address of the Host computer (this is displayed on the window that pops up when the server is run for your convenience) <br/>
<img src="https://github.com/hp-all/PlanetsGame/blob/main/Screenshots/ServerScreen.jpg" width="300"> <br/>
2. For each of the players, enter unique names into the name box, and the IP address of the host into the IP box <br/>
Use the Enter key to navigate to the next box <br/>
<img src="https://github.com/hp-all/PlanetsGame/blob/main/Screenshots/InitScreenName.png" width="400">&emsp;<img src="https://github.com/hp-all/PlanetsGame/blob/main/Screenshots/InitScreenIP.png" width="400">
3. Press enter after entering the IP address, then hit enter again to start the game
### Playing on the same computer for testing
1. The IP address is not needed for testing
2. For each of the players, enter unique names
3. Then for the IP address, enter "1", "2", "3", or "4" for each respective player. This will ensure each players' port number will be different to be able to run on the same machine
4. Press enter after entering the IP address, then hit enter again to start the game

### Waiting Screen
<img src="https://github.com/hp-all/PlanetsGame/blob/main/Screenshots/WaitingScreen.png" width="500"> <br/>
This is the Waiting screen where the designated player can set the rules of the match, and all players will indicate that they are ready
- Each player can press space to indicate that they are ready
- The player setting the rules (indicated by the colored arrows) can set the rules of the match using the arrow keys
<br/>

**Settings**
>**Map Setting**: Map can be selected randomly, or players can decide the map before each game <br/>
**Match Point**: Decides how many games a player needs to win in order to win the whole match <br/>
**Time**: How long each game will last for (the winner is whoever has the most health) <br/>
**Match Lives**: How many times a player can respawn within the same game <br/>
**Start Health**: How much health each player has when the game begins <br/>

## Screenshots

