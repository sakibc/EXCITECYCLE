package com.sakibchowdhury.excitecycle;

/**
 * @(#)PlayerCycle.java
 *
 * This class creates player objects which may then be moved by the main class. The player holds all of its relevant
 * information, like colour, direction it's going in, and the speed it's going in, as well as moving itself
 * given the appropriate input.
 *
 * @author Sakib Chowdhury <sakib_c@outlook.com>
 * @version 1.00 2012/2/17
 */
import javax.sound.sampled.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

class PlayerCycle {
    private static Color speed = new Color(0,255,0);			//the colour of the player when sped up
    private static Color breakerColour = new Color(0,255,255);	//when the player has the brick breaker powerup
    private static Color gangster = new Color(255,0,144);		//when the player is invincible; a sort of magenta colour
    private Color normal = new Color(255,0,0);		//the normal colour of the player; if this is player 2 then the colour will be changed to orange in the constructor

    private Color colour = normal;	//default colour is normal

    private String direction;	//direction, string like "WEST", "NORTH", etc...
    int x, y;	//x and y position of the player

    private int dist = 3;	//distance covered in one frame	//original speed was 2, too slow, 5, too fast. So I made it 3, but on every third frame 4 is moved instead
    private int framecount = 0;	//frame counter

    private boolean toChange = false;	//whether to change direction when possible
    private int toChangeCode = 0;	//the keycode we will change the snake's direction to
    private boolean breaker = false; //whether to break through the player's tail
    private boolean star = false; 	//whether the player is invincible

    private int score = 0;			//player's score
    private int winCount = 0;		//how many time the player's won
    private boolean timer = false;	//whether a timer is running; one runs when powerups are active
    private int time = 0;		//time that has passed with the timer
    private int limit = 0;	//time limit

    private boolean collision = false;	//have we collided?

    private Clip coin;		//sound played when money collected
    private Clip powerUp;	//sound played when regular powerup collected
    private Clip superStar;	//invincibility collected
    private Clip explosion;	//hit something

    private int playerNum;	//what number this player is; can be 1 or 2

    private Block[][] grid;	//where the player has been

    void resetScore() {	//resets the score and win count
        this.score = 0;
        this.winCount = 0;
    }

    private void loadSounds() {	//tutorial used:
        try {					//http://www3.ntu.edu.sg/home/ehchua/programming/java/J8c_PlayingSound.html
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File("sounds/coin.wav"));	//comments relevant to this are in the main class
            coin = AudioSystem.getClip();
            coin.open(audioIn);

            AudioInputStream audioIn2 = AudioSystem.getAudioInputStream(new File("sounds/pickupNorm.wav"));
            powerUp = AudioSystem.getClip();
            powerUp.open(audioIn2);

            AudioInputStream audioIn3 = AudioSystem.getAudioInputStream(new File("sounds/pickupStar.wav"));
            superStar = AudioSystem.getClip();
            superStar.open(audioIn3);

            AudioInputStream audioIn4 = AudioSystem.getAudioInputStream(new File("sounds/explosion.wav"));
            explosion = AudioSystem.getClip();
            explosion.open(audioIn4);

        }
        catch (UnsupportedAudioFileException | LineUnavailableException | IOException ex) {
            //System.out.println(ex);
        }
    }

    private void play(Clip clip) {	//shamelessly ripped from the tutorial
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);
        clip.start();
    }

    PlayerCycle(String direction, int x, int y, Block[][] grid, int playerNum) {
        this.playerNum = playerNum;		//whatever number the player is
        this.x = x;		//the x position of the player
        this.y = y;		//the y position of the player
        this.direction = direction;		//the direction the player is facing
        this.grid = grid;		//the grid of blocks on the level

        if (this.playerNum == 2) {		//if we are player 2
            this.normal = new Color(255,140,0);		//then our colour is orange
        }


        loadSounds();		//load the sounds
    }

    private void time() {		//checks to see if a timer is running, if it is, then see how long is left
        if (this.timer) {		//if a timer is running,
            this.time += 1;		//make it go up by 1

            if (this.limit - this.time < 60 && this.time % 12 == 0) {	//alternates between colours if time is almost up
                if (this.colour.equals(normal)) {						//sort of a useless warning to the user
                    if (this.breaker) {
                        this.colour = breakerColour;
                    }
                    if (this.dist == 5) {
                        this.colour = speed;
                    }
                    if (this.star) {
                        this.colour = gangster;
                    }
                }
                else {
                    this.colour = normal;
                }
            }
        }

        if (this.time >= this.limit && onGrid()) {	//if we've hit the time limit
            this.time = 0;		//reset the timer, and shut it off
            this.timer = false;
            if (this.dist == 5) {	//if we're sped up,
                this.dist = 3;		//then slow down
            }
            if (this.breaker) {		//if we're breaking stuff
                this.breaker = false;	//stop breaking stuff
            }
            if (this.star) {		//if we're invincible
                this.star = false;	//become mortal
            }
            this.colour = normal;	//reset our colour
        }
    }

    void move() {		//moves the player
        int dist = this.dist;	//the distance to move

        framecount += 1;	//increment the framecounter
        if (framecount % 3 == 0 && dist == 3) {	//because of the way the grid works, every few frames we must land
            dist = 4;							//on a multiple of ten. 3+3+3 is 9, but 3+3+4 is 10. So every three frames
        }										//we move 4 pixels instead of 3. Now if only this game didn't lag on school computers...

        if (this.toChange) {	//if we have to change the direction
            changeDirection(this.toChangeCode);	//try to change it (only works on grid points), if we can't then try again next time...
        }
        if (Objects.equals(this.direction, "NORTH")) {		//if we're facing north, go north
            this.y -= dist;
        }
        else if (Objects.equals(this.direction, "SOUTH")) {	//south, go south
            this.y += dist;
        }
        else if (Objects.equals(this.direction, "WEST")) {	//etc, etc...
            this.x -= dist;
        }
        else if (Objects.equals(this.direction, "EAST")) {
            this.x += dist;
        }

        if (this.x >= 1280) {		//when the cycle goes offscreen, loop back around
            this.x = 10;
        }
        else if (this.x <= 0) {
            this.x = 1270;
        }
        if (this.y >= 720) {
            this.y = 40;
        }
        else if (this.y <= 30) {
            this.y = 710;
        }

        if (onGrid()) {		//if we're on a gridpoint,
            int[] gridPos = gridPos();	//get what gridpoint we're on,

            if (collision()) {	//and check for collisions
                this.collision = true;
            }

            Block block = new Block("TAIL", this.colour, playerNum);	//leave a trail
            grid[gridPos[0]][gridPos[1]] = block;

        }

        time();	//runs the timer, if needed

    }

    boolean getCollision() {	//return whether or not we have collided
        return this.collision;
    }

    int getScore() {	//get the score
        return this.score;
    }

    void winning() {	//add one to our wincount
        this.winCount += 1;
    }
    int winnings() {	//get our wincount
        return this.winCount;
    }

    void reset() {		//reset the player to its starting position
        this.y = 360;	//we start at the vertical centre
        this.collision = false;	//we have not collided
        if (this.playerNum == 1) {	//if we are player one
            this.direction = "WEST";	//we shall go west
            this.x = 980;				//starting from this point
        }
        else {
            this.direction = "EAST";	//if we are player two, go east
            this.x = 300;				//starting from this point
        }
        this.colour = normal;	//normal colour, no breaking bricks, no invincibility, speed is normal
        this.breaker = false;
        this.star = false;
        this.dist = 3;
    }

    private boolean collision() {	//checks if we have collided, if so, yeah...
        int[] gridPos = gridPos();	//get our current position on the grid, will come in handy later

        if (!grid[gridPos[0]][gridPos[1]].pickUp()) {	//if this grid square is not a powerup,
            if (grid[gridPos[0]][gridPos[1]].getType().equals("TAIL") && grid[gridPos[0]][gridPos[1]].getPlayer() != this.playerNum) {
                //if we are colliding with a bike head on, we must make sure it is not us
                return true;	//this is a crappy fix for when both bikes hit each other head on, they pass through each other
            }					//i'm assuming its fixed, but impossible to replicate alone as i lack the coordination to make it happen again


            if (!grid[gridPos[0]][gridPos[1]].getType().equals("TAIL") && !grid[gridPos[0]][gridPos[1]].getType().equals("EMPTY")) {
                if (this.breaker) {		//if we have the breaker powerup						//if we are not on an empty square/our tail,
                    play(explosion);

                    //break through stuff as we hit it
                    switch (this.direction) {
                        case "WEST":    //will clear blocks to the left if going west
                            grid[gridPos[0] - 1][gridPos[1] - 1] = new Block("EMPTY");    //upper left

                            grid[gridPos[0]][gridPos[1] - 1] = new Block("EMPTY");    //upper middle

                            grid[gridPos[0] - 1][gridPos[1]] = new Block("EMPTY");    //centre left

                            grid[gridPos[0] - 1][gridPos[1] + 1] = new Block("EMPTY");    //bottom left

                            grid[gridPos[0]][gridPos[1] + 1] = new Block("EMPTY");    //bottom middle

                            break;
                        case "EAST":    //clear blocks to the right going east
                            grid[gridPos[0]][gridPos[1] - 1] = new Block("EMPTY");    //upper middle

                            grid[gridPos[0] + 1][gridPos[1] - 1] = new Block("EMPTY");    //upper right

                            grid[gridPos[0] + 1][gridPos[1]] = new Block("EMPTY");    //centre right

                            grid[gridPos[0]][gridPos[1] + 1] = new Block("EMPTY");    //bottom middle

                            grid[gridPos[0] + 1][gridPos[1] + 1] = new Block("EMPTY");    //bottom right

                            break;
                        case "NORTH":    //above going north
                            grid[gridPos[0] - 1][gridPos[1] - 1] = new Block("EMPTY");    //upper left

                            grid[gridPos[0]][gridPos[1] - 1] = new Block("EMPTY");    //upper middle

                            grid[gridPos[0] + 1][gridPos[1] - 1] = new Block("EMPTY");    //upper right

                            grid[gridPos[0] - 1][gridPos[1]] = new Block("EMPTY");    //centre left

                            grid[gridPos[0] + 1][gridPos[1]] = new Block("EMPTY");    //centre right

                            break;
                        case "SOUTH":    //below going south
                            grid[gridPos[0] - 1][gridPos[1]] = new Block("EMPTY");    //centre left

                            grid[gridPos[0] + 1][gridPos[1]] = new Block("EMPTY");    //centre right

                            grid[gridPos[0] - 1][gridPos[1] + 1] = new Block("EMPTY");    //bottom left

                            grid[gridPos[0]][gridPos[1] + 1] = new Block("EMPTY");    //bottom middle

                            grid[gridPos[0] + 1][gridPos[1] + 1] = new Block("EMPTY");    //bottom right

                            break;
                    }
                }

                else {	//!@#$!@#!! We hit something! Call a whaaaambulance!
                    this.score = 0;	//reset our score
                    return true;	//return true...
                }
            }
        }
        else {	//if this grid square IS a powerup,
            switch (grid[gridPos[0]][gridPos[1]].getType()) {
                case "MONEY":
                    play(coin);        //if it's money, yay.

                    this.score += 10;
                    break;
                case "SPEED":
                    play(powerUp);        //if it's a speedup (all powerups play sound on collision)

                    this.colour = speed;    //change our colour to that of speed

                    this.dist = 5;            //increase our speed

                    this.limit = 480;    //set a time limit

                    this.time = 0;        //reset the current timer

                    this.timer = true;    //activate the timer; speed boost is only temporary

                    break;
                case "BREAK":
                    play(powerUp);        //if it's a breaker

                    this.colour = breakerColour;    //change our colour

                    this.breaker = true;    //we WILL break stuff

                    this.limit = 240;    //time limit

                    this.time = 0;        //reset timer

                    this.timer = true;    //activate the timer; speed boost is only temporary

                    break;
                case "STAR":
                    play(superStar);    //if it's invincibility

                    this.colour = gangster;    //pimp out our lightcycle

                    this.star = true;        //invincibility is true

                    this.breaker = true;    //so is break-ability

                    this.dist = 5;            //speed up too

                    this.limit = 240;        //looong time limit, because it's worth it

                    this.time = 0;        //reset the timer

                    this.timer = true;    //activate the timer; speed boost is only temporary

                    break;
            }
        }

        return false;	//we haven't hit anything! yay.
    }

    Color getColour() {	//return our colour, used to draw the scores in team colours
        return this.colour;
    }

    private boolean onGrid() {	//determines if the bike is perfectly on a grid square; if it is we can change directions, else, meh
        //grid squares are 10 by 10 pixels
        return this.x % 10 == 0 && this.y % 10 == 0;
    }

    void changeDirection(int keycode) {	//changes our direction, if it can
        if (onGrid()) {	//if we are on a grid square (very important; we mustn't change direction if we aren't)
            this.toChange = false;	//toChange is false. toChange is set to true if we aren't on a grid square,
            //so that the next frame we will know we must change
            if (keycode == 38 && !Objects.equals(this.direction, "SOUTH")) {	//if the "up" button is pressed (may be down arrow or w)
                this.direction = "NORTH";	//set our direction to north
            }
            else if (keycode == 40 && !Objects.equals(this.direction, "NORTH")) {	//the != "OPPOSITE DIRECTION" is so that we don't turn around
                this.direction = "SOUTH";							//into our own tail, killing us instantly
            }
            else if (keycode == 37 && !Objects.equals(this.direction, "EAST")) {
                this.direction = "WEST";
            }
            else if (keycode == 39 && !Objects.equals(this.direction, "WEST")) {
                this.direction = "EAST";
            }
        }
        else {
            this.toChange = true;	//wait to change direction until we are on a grid point
            this.toChangeCode = keycode;	//code to change our direction to
        }
    }
    private int[] gridPos() {	//what is our position on the grid?
        return new int[]{this.x/10, this.y/10};	//creative nonsensical variable name
    }

}
