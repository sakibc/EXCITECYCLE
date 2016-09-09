package com.sakibchowdhury.excitecycle;

/**
 * @(#)Block.java
 *
 * The block object holds all the data relevant to one point on the grid.
 *
 * @author Sakib Chowdhury <sakib_c@outlook.com>
 * @version 1.00 2012/2/24
 */
import java.util.*;
import java.awt.*;

class Block {
    private static Random rg = new Random();	//random generator

    private Color random = null;	//defined later only if necessary. defining it now lead to a huge slowdown, but i fixed that.
    private Color colour = black;	//default colour is black

    private static Color black = Color.black;	//already defined, so why not use it
    private static Color gold = new Color(255,255,0);	//used for money
    private static Color speed = new Color(0,255,0);	//used for speed-ups
    private static Color breaker = new Color(0,255,255);	//used for break-ups

    private int darkenCount = 0;	//the number of times this block has been darkend
    //if the block is a part of the tail, then we have this really cool fading effect
    //but i mustn't dim it too much, otherwise the tail will become invisible
    private int frameTimer = 0;			//self explanatory
    private boolean powerUp = false; 	//is this a powerup? yes. yes it is.

    private String blockType;	//the type of block this is
    private int playerNum;		//the player this block belongs to

    Block(String blockType) {	//constructor, if only the blocktype is specified
        this.blockType = blockType;		//save our block type

        switch (blockType) {
            case "MONEY":    //if i am money
                this.colour = gold;                //then my colour is golden

                powerUp = true;                    //and i am a power up

                break;
            case "BREAK":    //if i am a breaker powerup
                this.colour = breaker;                //then i am a breaker colour

                powerUp = true;                    //and i am a powerup

                break;
            case "SPEED":    //if i'm a speedboost
                this.colour = speed;    //yeah, you get the idea

                powerUp = true;
                break;
            case "STAR":    //if i'm invincible
                this.colour = random;                //randomize my colour

                powerUp = true;    //i am a powerup

                break;
        }
    }

    Block(String blockType, Color colour, int playerNum) {	//called if block is made by a player
        this.blockType = blockType;	//save our blocktype
        this.colour = colour;	//save our colour
        this.playerNum = playerNum;	//save our player number
    }

    int getPlayer() {	//returns the player this block belongs to
        return this.playerNum;
    }

    String getType() {	//returns the type of block this is
        return this.blockType;
    }

    boolean pickUp() {	//returns if we are a powerup
        return this.powerUp;
    }

    Color getColour() {	//returns this block's colour
        return this.colour;
    }

    private Color dim(Color colour) {	//dims the colour slightly

        int r = colour.getRed();	//get the rgb of this colour
        int g = colour.getGreen();
        int b = colour.getBlue();

        r-=20;	//subtract 20 from each channel
        g-=20;
        b-=20;

        int[] values = {r,g,b};	//put 'em in an array

        for (int i = 0; i < 3; i++) {	//make sure none are below zero
            if (values[i] < 0) {
                values[i] = 0;
            }
        }

        return new Color(values[0],values[1],values[2]);	//aaand return our new colour
    }

    void update() {	//updates the block's state/colour
        if (!this.blockType.equals("EMPTY") && darkenCount < 5) {	//if this block isn't empty and hasn't been darkened too much
            this.frameTimer += 1;	//add one to the counter

            if (powerUp) {	//if we are a powerup
                if (this.blockType.equals("STAR") && frameTimer % 10 == 0) {	//if we are invincible,	make us a random colour

                    random = new Color(rg.nextInt(256),rg.nextInt(256),rg.nextInt(256));
                    this.colour = random;
                }

                if (frameTimer == 900) {	//fifteen seconds to pickup this item, or you're screwed
                    reset();	//change this block back to an empty block after 15 seconds
                }
            }

            else {	//if we aren't a powerup,
                if (this.frameTimer % 4 == 0) {	//after a few frames,
                    this.blockType = "DEATH";	//make this block deadly

                    this.colour = dim(this.colour);	//dim this block slightly; looks coolr
                    this.darkenCount += 1;	//add one to the darken count
                }
            }

        }
    }

    void reset() {	//reset this block
        this.colour = black;	//its colour is black
        powerUp = false;	//its powerup status is revoked
        this.blockType = "EMPTY";	//and its name is changed to empty
    }

}