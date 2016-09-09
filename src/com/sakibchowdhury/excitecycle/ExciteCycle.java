package com.sakibchowdhury.excitecycle;

/**
 * @(#)ExciteCycle.java
 *
 * ExciteCycle is a lightcycle-style game where two players drive around, leaving trails behind. Whoever collides into a trail first loses.
 * There are several powerups, and attempts have been made to make the game aesthetically pleasing. Enjoy.
 *
 * May run a bit slow on less-powerful machines
 *
 * The main game class. Run this to play.
 *
 * @author Sakib Chowdhury <sakib_c@outlook.com>
 * @version 1.02 2016/9/9
 */

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import javax.sound.sampled.*;	//required for sound
import java.util.*;
import java.io.*;


public class ExciteCycle extends JFrame implements KeyListener {
    private static Random rg = new Random();		//the random generator

    private Color random = new Color(rg.nextInt(256),rg.nextInt(256),rg.nextInt(256));	//this is a random colour;
    //anything that wants to be flashing randomly uses this colour
    private int opacity = 255;	//opacity of the white flash on game over. starts at 100%
    private int collidex, collidey;			//the location of the collision of either player; used to display an "ouch." message over that point

    private boolean[] keys;		//the array of keys
    private Image dbImage;		//the image that's drawn to the screen
    private Graphics dbg;		//the buffer

    private int frameNo = 0;	//frame counter; comes in handy on occasion

    private Font bigFont;		//big font size				//these three will be loaded from file later
    private Font smallFont;		//small font size
    private Font titleFont;		//title (huge) font size

    private Block[][] grid = new Block[128][72];		//the grid of blocks; isn't she beautiful

    private PlayerCycle player = new PlayerCycle("WEST", 980,360, grid, 1);	//player 1
    private PlayerCycle player2 = new PlayerCycle("EAST", 300,360, grid, 2);	//player 2

    private String page = "MENU";	//the current page of the program

    private Clip explosion;			//explosion sound effect
    private Clip polka;	//title music; shamelessly stolen from RainbowCrash88 on YouTube
    private Clip superStar;			//sound effect played when the game begins/restarts


    private ExciteCycle() {			//the constructor class
        //super("excitecycle");		//i could randomize this, actually...yeah, i think i'll do that
        super("Welcome to the Future!"); //now the title is passed in as a parameter, and "hilarity" ensues!

        addKeyListener(this);		//listen to the keys; everybody has something to say
        keys = new boolean[1000];	//if only we slowed down once in awhile to hear 'em...[not relevant to the code]

        setSize(1280, 720);		//the window size, 720p
        setVisible(true);		//so that the user can see the monster we've become
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);		//kill that monster

        for (int i = 0; i < 128; i++) {				//fill the grid with empty blocks
            for (int j = 0; j < 72; j++) {
                grid[i][j] = new Block("EMPTY");	//the block takes in a string as its blockType. EMPTY spawns an empty block
            }
        }

        loadFonts();	//load the fonts
        loadSounds();	//load the sounds

    }

    private static void delay(long len) {	//delay for len milliseconds
        try {
            Thread.sleep(len);
        }
        catch (InterruptedException ex) {
            //System.out.println(ex);
        }
    }

    private void loadFonts() {	//god praise the internets!			//this method loads the fonts
        try {					//i'd link the tutorial, but i forget what it's called
            Font font = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/pixelfont.ttf"));	//create a font object using the physical font file
            this.bigFont = font.deriveFont(48f);	//derive a big font from it
            this.titleFont = font.deriveFont(148f);	//derive a small font from it
            this.smallFont = font.deriveFont(30f);	//derive a gigantic font from it

        }
        catch (IOException | FontFormatException ex) {
            System.out.println(ex);
            System.out.println("Auto-loading fonts failed. Go to the fonts folder and manually install it.");	//java on windows xp seems to have a problem with
        }																										//loading fonts sometimes
    }

    private void loadSounds() {	//tutorial used:				//loads all of the sound effects
        try {					//http://www3.ntu.edu.sg/home/ehchua/programming/java/J8c_PlayingSound.html
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File("sounds/explosion.wav"));	//the explosion sound
            explosion = AudioSystem.getClip();	//creates a clip object, I think
            explosion.open(audioIn);			//and assigns it to this sound

            AudioInputStream pixelpeeker = AudioSystem.getAudioInputStream(new File("sounds/Pixel Peeker Polka - slower.wav"));
            polka = AudioSystem.getClip();		//I hate parasprites.
            polka.open(pixelpeeker);		//this song plays on the title screen

            AudioInputStream audioIn2 = AudioSystem.getAudioInputStream(new File("sounds/pickupStar.wav"));
            superStar = AudioSystem.getClip();	//normally played when invincibility is picked up
            superStar.open(audioIn2);			//here it will be used as the game start sound
        }
        catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            System.out.println(ex);
        }
    }
    private void play(Clip clip) {	//shamelessly ripped from the tutorial
        if (clip.isRunning()) {		//rewinds and plays a clip
            clip.stop();
        }
        clip.setFramePosition(0);
        clip.start();
    }

    private void reset() {		//resets everything ingame, besides the scores
        player.reset();			//put the players back in their place
        player2.reset();

        for (int i = 0; i < 128; i++) {		//resets the blocks
            for (int j = 0; j < 72; j++) {
                grid[i][j].reset();
            }
        }
    }

    public void keyTyped(KeyEvent e){}		//apparently this needs to be overwritten

    public void keyPressed(KeyEvent e) {	//marks a key as pressed
        keys[e.getKeyCode()] = true;
    }
    public void keyReleased(KeyEvent e) {	//marks a key as released
        keys[e.getKeyCode()] = false;
    }

    private void playerMove() {			//moves the player and deals with their collisions; should probably be broken up
        if (keys[37]) {				//player 1 keys
            player.changeDirection(37);		//feed the player the keycode, so it can respond accordingly
        }
        else if (keys[39]) {
            player.changeDirection(39);
        }
        else if (keys[38]) {
            player.changeDirection(38);
        }
        else if (keys[40]) {
            player.changeDirection(40);
        }


        if (keys[65]) {				//player 2 keys
            player2.changeDirection(37);
        }
        else if (keys[68]) {
            player2.changeDirection(39);
        }
        else if (keys[87]) {
            player2.changeDirection(38);
        }
        else if (keys[83]) {
            player2.changeDirection(40);
        }


        if (keys[27]) {		//if escape, go to the menu
            page = "MENU";
        }
        if (keys[80]) {		//if P, pause the game
            page = "PAUSE";
        }


        player.move();		//move the player
        player2.move();		//move the player2

        boolean reset = false;	//whether or not to reset the game

        if (player.getCollision()) {	//if player 1 collides,
            player2.winning();		//let player2 know it's winning
            collidex = player.x;	//get the x and y of collision
            collidey = player.y;
            reset = true;			//mark ourselves for reset-tion
        }
        if (player2.getCollision()) {	//if player 2 collides,
            player.winning();			//same as before
            collidex = player2.x;
            collidey = player2.y;
            reset = true;		//if they collide head on, both of the players are considered to be winning but their score is reset
        }

        if (reset) {		//if we must reset, then, yeah.
            gameOver();
        }

    }

    private void gameOver() {	//if game over,
        play(explosion);		//play the explosion sound
        opacity = 255;			//set the opacity of the flash to 100%
        page = "GAMEOVER";		//go to the game over page
    }

    private void blockUpdate() {			//goes to each block in the grid and calls its update method
        for (int i = 0; i < 128; i++) {
            for (int j = 0; j < 72; j++) {
                grid[i][j].update();
            }
        }
    }

    private void pickUps() {			//creates the pickups
        int generate = rg.nextInt(300);		//there is a 1/300 chance every 1/60 of a frame
        if (generate == 42) {				//if i've found the answer to the universe, then it's powerup time

            int x = rg.nextInt(128);	//put the powerup at a random x
            int y = rg.nextInt(72);		//put the powerup at a random y

            while (!grid[x][y].getType().equals("EMPTY")) {	//if the x and y aren't an empty grid square,
                x = rg.nextInt(128);						//then try again...
                y = rg.nextInt(72);
            }

            String[] options = {"MONEY","MONEY","MONEY", "MONEY","MONEY","MONEY","SPEED","SPEED","SPEED","BREAK","BREAK","STAR"};	//array of options to pick from; kind of a lazy way to do this
            int pick = rg.nextInt(12);		//pick a random item from the array
            String picked = options[pick];

            grid[x][y] = new Block(picked);	//place the powerup

        }
    }

    public void paint(Graphics g) {
        frameNo += 1;	//increment the framecounter

        if (dbImage == null) {	//create the dbImage
            dbImage = createImage(1280, 720);
            dbg = dbImage.getGraphics();
        }

        dbg.setColor(new Color(0,0,0));		//fill the background with black
        dbg.fillRect(0,0,1280,720);

        if (page.equals("PLAY") || page.equals("GAMEOVER") || page.equals("PAUSE")) {	//if the game is in play, (and if we're on the pause or gameover screen,
            //							since we still want to draw the game in the background)
            for (int i = 0; i < 128; i++) {				//for every block on the screen, get its colour then draw it with that colour
                for (int j = 0; j < 72; j++) {
                    dbg.setColor(grid[i][j].getColour());
                    dbg.fillRect(i*10,j*10,10,10);
                }
            }

            dbg.setColor(player.getColour());			//get the colour of the player, then draw him/her
            dbg.fillRect(player.x,player.y,10,10);

            dbg.setFont(smallFont);		//use the small font to draw the
            dbg.drawString("SCORE: " + player.getScore(), 1080, 80);	//it seems java is "intelligent" enough to make the
            //origin of fonts the bottom left instead of the top left...yay.
            dbg.setColor(player2.getColour());			//get the colour of the second player, then draw him/her
            dbg.fillRect(player2.x,player2.y,10,10);

            dbg.drawString("SCORE: " + player2.getScore(), 60, 80);	//draw player2's score in the same colour as its sprite

            dbg.setColor(new Color(255,255,255));		//draw the number of times each player has won
            dbg.drawString("Won: " + player.winnings(), 1080, 110);
            dbg.drawString("Won: " + player2.winnings(), 60, 110);

        }
        else if (page.equals("MENU")) {		//if we're on the menu, then draw the menu
            drawMenu();
        }

        if (page.equals("GAMEOVER")) {		//if we're on the gameover screenm
            dbg.setColor(random);		//draw a continue message in flashy colours, because it looks cool
            dbg.setFont(bigFont);
            dbg.drawString("press space to continue", 330,680);

            dbg.setColor(new Color(0,0,0));	//draw a message at the point of collision
            dbg.setFont(smallFont);
            dbg.drawString("ouch.", collidex-28, collidey+15);	//this is where collidex and collidey come in
            dbg.setColor(new Color(255,255,255));				//do it twice; once black and once white offset a bit so we get a slight shadow
            dbg.drawString("ouch.", collidex-30, collidey+13);

            Color transparentWhite = new Color(255, 255, 255, opacity);
            dbg.setColor(transparentWhite);
            dbg.fillRect(0,0,1280,720);

            opacity -= 20;		//reduce the transparency of the flash each frame until it reaches 0; it's a cool effect
            if (opacity < 0) {
                opacity = 0;
            }
        }
        if (page.equals("PAUSE")) {	//if we're on the pause screen,
            dbg.setColor(random);	//display a message about pausing
            dbg.setFont(bigFont);
            dbg.drawString("press space to unpause, escape for the menu", 30,680);
        }



        g.drawImage(dbImage,0,0,this);	//draw from the buffer to the screen
    }

    private void drawMenu() {		//drawing the menu
        dbg.setFont(titleFont);

        dbg.setColor(random);
        dbg.drawString("EXCITECYCLE", 168,365);		//the title, in a random colour

        dbg.setColor(new Color(255,255,255));
        dbg.drawString("EXCITECYCLE", 163,360);		//the title in white, offset slightly. Cool effect.

        dbg.setFont(smallFont);
        dbg.drawString("The 2 player fun-machine!", 163, 400);		//a message

        dbg.drawString("Player 1 [W] [A] [S] [D]", 163, 480);
        dbg.drawString("Player 2 [UP] [LEFT] [DOWN] [RIGHT]", 163, 510);	//instructions for how to play

        dbg.drawString("(C) 1983 Generic Game Company, LLC", 370, 80);		//copyright information

        dbg.setFont(bigFont);
        dbg.setColor(random);				//draw a message saying how to start the game
        dbg.drawString("press space to start", 354,680);	//positions are hand-tweaked to perfection; too lazy to learn
    }														//font metrics

    private void resetGame() {		//resets the entire game; called when we start the game from the main menu
        player.reset();		//reset the players
        player2.reset();
        player.resetScore();	//reset their scores
        player2.resetScore();

        for (int i = 0; i < 128; i++) {		//clear the grid
            for (int j = 0; j < 72; j++) {
                grid[i][j].reset();
            }
        }
    }

    private void menuUpdate() {					//update the menu
        if (!polka.isRunning()) {			//if the menu music isn't playing, god forbid...
            polka.setFramePosition(0);	//rewind and start playing!
            polka.start();
        }

        if (keys[32]) {				//if the player starts the game, stop the music, start the game
            polka.stop();
            play(superStar);
            resetGame();
            page = "PLAY";
        }
    }
    private void gameOverUpdate() {	//if the players have crashed,
        if (keys[32]) {		//wait for the user to press space
            reset();		//then reset, and play a silly sound clip
            play(superStar);
            page = "PLAY";
        }
        if (keys[27]) {		//if the user hits escape, go back to the menu
            page = "MENU";
        }
    }

    private void update() {			//update loop
        switch (page) {
            case "PLAY":        //if the current page is the main game
                playerMove();            //move the players

                blockUpdate();            //update the blocks

                pickUps();                //create some powerups

                break;
            case "MENU":    //if we're on the menu, do menu stuff
                menuUpdate();
                break;
            case "GAMEOVER":    //if the player's crashed, do game over stuff
                gameOverUpdate();
                break;
            case "PAUSE":    //if the page is the pause screen, do pause stuff
                pauseUpdate();
                break;
        }

        if (frameNo % 10 == 0) {		//every ten frames, generate a new random colour, since, you know, we use these quite often
            random = new Color(rg.nextInt(256),rg.nextInt(256),rg.nextInt(256));//changing the random colour too often may result in a seizure for some...
        }
    }

    private void pauseUpdate() {	//pause update
        if (keys[32]) {			//space bar, play
            page = "PLAY";
        }
        if (keys[27]) {
            page = "MENU";	//escape, go to menu
        }
    }

    public static void main(String[] args) {
        ExciteCycle frame = new ExciteCycle();	//create the game
        //noinspection InfiniteLoopStatement
        while (true) {
            frame.update();
            frame.repaint();
            delay(10);	//60 frames per second
        }
    }


}
