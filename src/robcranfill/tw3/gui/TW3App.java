/**
    TW3App.java
    The view/controller object, as opposed to the model.

    Copyright (C) 2013 robcranfill@robcranfill.net
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
**/

package robcranfill.tw3.gui;

import java.applet.Applet;	// We use this for audio - OK?
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.Random;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import robcranfill.tw3.*;
import robcranfill.tw3.com.CommThing;
import robcranfill.tw3.com.iComm;
import robcranfill.tw3.com.iConnectHandler;
import robcranfill.tw3.com.iObjHandler;


@SuppressWarnings("serial")
public class TW3App
     extends JFrame
  implements javax.swing.event.MouseInputListener, ActionListener,
             iObjHandler, iGUI, iConnectHandler
{

// The object that represents the state of play:
private Model         theModel_;

// The object that draws the playing board:
private GameBoard     drawBoard_;

// The possible states:
public static final int        STATE_OTHER_TURN        = 1;
public static final int        STATE_PLACE_PEG         = 2;
public static final int        STATE_PLACE_LINK_START  = 3;
public static final int        STATE_PLACE_LINK_END    = 4;
public static final int        STATE_NET_START         = 5;
public static final int        STATE_NET_NOT_MY_TURN   = 6;

private             int        currentState_ = STATE_OTHER_TURN;    // hmmm

// misc instance vars:

/*
    first player is always red.
    in a non-netted game, red simply goes first.
    in a networked game, first player to place a peg is red.
*/
private PlayerInfo    players_[] = { // Changeable player names (IP not used now)
                            new PlayerInfo("Red",  "127.0.0.1"), 
                            new PlayerInfo("Blue", "127.0.0.1")};
private String playerColors_[]  = {"Red", "Blue"};	// Non-changeable names

private static final int    PLAYER_1         = 0;   // indices into players[], etc
private static final int    PLAYER_2         = 1;
private int                 currentPlayerIndex_ = PLAYER_1;
private int                 thisPlayerIndex_    = PLAYER_1; // for netted games; does not denote order of play

// Comm
private CommThing              commControl_ = null;
private static String       SHUTDOWN_MESSAGE = "OPPONENT IS SHUTTING DOWN. CRUMB!";
private static String       NEWGAME_MESSAGE  = "OPPONENT SAYS 'NEW GAME'!";
private static String       WIN_MESSAGE = "OTHER GUY WON!";

// GUI things
private static final int   CONTROL_PANEL_WIDTH = 140;
private static final int   BOARD_SIZE          = 392;

private static final int   MENU_FUDGE          =  56;    // and window title bar, too
private static final int   BEVEL_FUDGE         =   4;    // 

private static final int   WINDOW_WIDTH = CONTROL_PANEL_WIDTH + BOARD_SIZE + BEVEL_FUDGE;
private static final int   WINDOW_HEIGHT = BOARD_SIZE + MENU_FUDGE;

private JButton     buttonTurnDone_, buttonMessage_;
private static String TOOL_TIP_BUTTON_DONE_YOUR_TURN     = "Press this when you're finished with your turn";
private static String TOOL_TIP_BUTTON_DONE_NOT_YOUR_TURN = "(It's not your turn; wait for it)";

//private JTextArea   commText_;
private JLabel      labelWhosTurnIsIt_, labelWhoYouAre_;
private JMenuItem   menuItemNetwork_;   // so we can disable it

// pre-loaded audio
private AudioClip   acPeg_, acLinkStart_, acLinkEnd_;
private AudioClip   acBlocked_, acInitComm_, acAbout_, acIntro_;

private TWPoint     linkStartPoint_ = null; // gotta hold it across clicks

private ToolImage toolImage_ = null; // Jan05

static String[] randomNames_ = 
	{"Bert Fegg", "Skanky", "Shakes, the Clown", "Arthur", "Sniggendorf",
     "Jocko Homo", "Beatrice", "Scrotus", "Cleetus", "Arthur 'Two Sheds' Jackson",
     "Throatwarbler Mangrove", "Bob"};


//////////////////////////////////////////////////////////////////////
// Da Code
//

/**
    Main entry point for the application.
    Support command-line arguments? What would they do?
**/
public static void
main(String args[]) {

    // The view/controller is the main thing. har.
    //
    new TW3App();
}


/**
    Constructor. Extends JFrame.
    Creates its own model, and everything else.
**/
public 
TW3App() {

    super("TW3 v2.0a");    // The Window That Will Trounce Wookies
    this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    this.setResizable(false);

    // Create the play model
    //
    theModel_ = new Model(this);

    // A window handler can't prevent closing ("Are you sure?"), so 
    // let's disable the close box (get rid of it?).
    // We'll allow exits only via the menu....
    //
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

    // Create the menu.
    //
    createMenus();

    // Create the GUI
    //
	JPanel panelPlay = new JPanel();
	BevelBorder bevelBorder1 = new BevelBorder(BevelBorder.RAISED);

	JPanel panelControl = new JPanel();

    getContentPane().setLayout(null);
    panelPlay.setBorder(bevelBorder1);
    panelPlay.setLayout(null);
    getContentPane().add(panelPlay);
    panelPlay.setBounds(CONTROL_PANEL_WIDTH, 0, BOARD_SIZE, BOARD_SIZE);

    drawBoard_ = new GameBoard(theModel_);
    drawBoard_.addMouseListener(this);
    panelPlay.add(drawBoard_);
    drawBoard_.setBounds(0, 0, BOARD_SIZE, BOARD_SIZE);

    panelControl.setBorder(bevelBorder1);
    panelControl.setLayout(null);
    getContentPane().add(panelControl);
    panelControl.setBounds(0, 0, CONTROL_PANEL_WIDTH, BOARD_SIZE);

    // The "who's turn is it" label
    //
    labelWhosTurnIsIt_ = new JLabel();
    panelControl.add(labelWhosTurnIsIt_);
    labelWhosTurnIsIt_.setBounds(12, 12, 96, 16);

    // The "who you are" label (for network games)
    //
    labelWhoYouAre_ = new JLabel();
    panelControl.add(labelWhoYouAre_);
    labelWhoYouAre_.setBounds(12,32,96,16);

    // The "my turn is done" button
    //
    int by = 56;    // button y loc
	buttonTurnDone_ = new JButton();
    buttonTurnDone_.setText("Done");
    buttonTurnDone_.setActionCommand("Done");
    buttonTurnDone_.addActionListener(
        new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                nextPlayerTurn();
                sendState();
                }
            }
        );
    panelControl.add(buttonTurnDone_);
    buttonTurnDone_.setBounds(12, by, 96, 24);
    buttonTurnDone_.setToolTipText(TOOL_TIP_BUTTON_DONE_YOUR_TURN);
 
    by += 36;
	buttonMessage_ = new JButton();
    buttonMessage_.setText("Message");
    buttonMessage_.setActionCommand("Message");
    buttonMessage_.addActionListener(
        new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                sendMessage();
                }
            }
        );
    panelControl.add(buttonMessage_);
    buttonMessage_.setBounds(12, by, 96, 24);
    enableMessageButton(false);

    // The cursor palette
    //
    by += 32;
    // Jan05
//    ToolImage toolImage_ = new ToolImage();
    toolImage_ = new ToolImage();
    toolImage_.setBounds(8, by, toolImage_.getWidth(), toolImage_.getHeight());
    panelControl.add(toolImage_);

    // Other stuff
    //
    doNew();
    loadAudio();

    // Create the communications object
    //
    commControl_ = new CommThing(this);    // this obj is the listener
    (new Thread(commControl_)).start();

    // Center ourself on the screen.
    //
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    int w = this.getSize().width;
    int h = this.getSize().height;
    int x = (screen.width - w) / 2;
    int y = (screen.height - h) / 3;   // 1/3rd from top, actually
    this.setBounds(x, y, w, h);
    this.setVisible(true);        // we were created inviz; show ourselves.

    playIntroSound();

	} // TW3App constructor


/**
    Enable or disable the message button
**/
private void
enableMessageButton(boolean enabled) {
    
    buttonMessage_.setEnabled(enabled);
    if (enabled) {
        buttonMessage_.setToolTipText("Click here to send a nice message to your opponent");
    	}
    else {
        buttonMessage_.setToolTipText("(Who would you send a message to?)");
    	}
	}

/**
    Create the menus
**/
private void
createMenus() {

    JMenuBar menubar = new JMenuBar();

    JMenu menu = new JMenu("File");
    menu.setMnemonic('F');
    menubar.add(menu);

    JMenuItem item = new JMenuItem("Exit");
    item.setActionCommand("exit");
    item.addActionListener(this);
    item.setAccelerator(KeyStroke.getKeyStroke('X', java.awt.Event.ALT_MASK, false));
    item.setMnemonic('X');
    menu.add(item);

    menu = new JMenu("Game");
    menu.setMnemonic('G');
    menubar.add(menu);
    item = new JMenuItem("New Game");
    item.setActionCommand("new");
    item.addActionListener(this);
    item.setAccelerator(KeyStroke.getKeyStroke('N', java.awt.Event.ALT_MASK, false));
    item.setMnemonic('N');
    menu.add(item);

    menu.addSeparator();

    menuItemNetwork_ = new JMenuItem("Network Connection...");
    menuItemNetwork_.setActionCommand("net");
    menuItemNetwork_.addActionListener(this);
    menuItemNetwork_.setAccelerator(KeyStroke.getKeyStroke('C', java.awt.Event.ALT_MASK, false));
    menuItemNetwork_.setMnemonic('C');
    menu.add(menuItemNetwork_);

    menu = new JMenu("Help");
    menu.setMnemonic('H');
    menubar.add(menu);

    item = new JMenuItem("About TW3...");
    item.setActionCommand("about");
    item.addActionListener(this);
    item.setAccelerator(KeyStroke.getKeyStroke('A', java.awt.Event.ALT_MASK, false));
    item.setMnemonic('A');
    menu.add(item);

    this.setJMenuBar(menubar);
	}


/**
    For handling menu selections.
**/
public void
actionPerformed(ActionEvent ev) {

    String action = ev.getActionCommand();
    if (action.equals("exit")) {
        doShutdown();
    	}
    else 
    if (action.equals("new")) {
        doNew();
   		}
    else
    if (action.equals("net")) {
        doNetDialog();
    	}
    else
    if (action.equals("about")) {
        doAbout();
    	}
    else {
        System.out.println("Unknown menu action? " + action);
	    }
	}

/**
    Display an incoming message.
    Is there a better thing to do?
**/
public void
showMessage(String message) {

    // commText_.setText(commText_.getText() + message + "\n");
    System.out.println("SHOW: " + message);
	
	}


/**
    Send the model to 'the other side'.
**/
public void
sendState() {

    if (commControl_.isConnected()) {
        commControl_.sendObject(theModel_);
    	}
	}


/**
    Handle an object(a model) coming from 'the other side'.
    This only happens if we're in a netted game.
**/
public void
handleObject(Object o) {
    
    if (o instanceof AuxInfo) {
 
        AuxInfo info = (AuxInfo)o;
 
        if (info.getComment().equals(SHUTDOWN_MESSAGE)) {
            JOptionPane.showMessageDialog(this, "Your opponent has dropped the connection.\nWhat did you do?!",
                                          "Connection dropped", JOptionPane.OK_OPTION);
            resetComm();
        	}
        else
        if (info.getComment().equals(WIN_MESSAGE)) {
            String otherPlayer = players_[PLAYER_2-thisPlayerIndex_].playerName;
            JOptionPane.showMessageDialog(this, otherPlayer + " wins!\nTry again!", "You lose", JOptionPane.OK_OPTION);
        	}
        else {
  //          showMessage(">> MESSAGE: '" + info + "'");
            JOptionPane.showMessageDialog(this, info.getComment(), "Message", JOptionPane.OK_OPTION);   
        	}
        return;
    	}

    // If we were waiting to start a game,
    // this means the other guy got the drop on us.
    //
    if (currentState_ == STATE_NET_START) {

        // we are player 2
        //
        thisPlayerIndex_    = PLAYER_2;
        currentPlayerIndex_ = PLAYER_2; // nextPlayerTurn is gonna toggle this, so set it to the wrong thing. Gurk

        System.out.println("Other guy (now PLAYER_1) got the drop!");

        showMessage("You are Blue player.");
        setYouLabel("Blue");
    	}

    System.out.println(" >>> handleObject: " + o);

    theModel_ = (Model) o;                        // something seems wrong here....
    drawBoard_.setModel(theModel_);

    theModel_.registerListener(this);

    nextPlayerTurn();
	} // handleObject


/**
    We've started a 'server' session.
**/
public void
handleCatch() {

    players_[currentPlayerIndex_].playerName = "how?";

    showMessage("Connected! You are 'Catcher'.");
    enableMessageButton(true);
    goToStateNetStart();
	}


/**
    We've started a 'client' session.
**/
public void
handlePitch() {

    showMessage("Connected! You are 'Pitcher'.");
    enableMessageButton(true);
    goToStateNetStart();
	}


/**
    Display player's name.
**/
private void
setTurnLabel(String who) {
    labelWhosTurnIsIt_.setText(who + "'s (" + playerColors_[currentPlayerIndex_] + ") turn");
	}

/**
    Display player's name.
**/
private void
setYouLabel(String who) {
    labelWhoYouAre_.setText("You are " + who);
	}



/**
    Change players; that is, flip a/b.
**/
private void
nextPlayerTurn() {

    setPlayerTurn( (currentPlayerIndex_==PLAYER_1)?PLAYER_2:PLAYER_1 );

    if (commControl_.isConnected()) {
        if (currentPlayerIndex_ == thisPlayerIndex_) {
            buttonTurnDone_.setEnabled(true);
            buttonTurnDone_.setToolTipText(TOOL_TIP_BUTTON_DONE_YOUR_TURN);
            goToStatePlacePeg();
            }
        else { // not my turn
            buttonTurnDone_.setEnabled(false);
            buttonTurnDone_.setToolTipText(TOOL_TIP_BUTTON_DONE_NOT_YOUR_TURN);
            goToStateNotMyTurn();
            }
        }
    else { // not connected
            goToStatePlacePeg();
        }
    }

/**
    Change players to the given player.
**/
private void
setPlayerTurn(int who) {

    currentPlayerIndex_ = who;
    String player = players_[currentPlayerIndex_].playerName;
    setTurnLabel(player);
    showMessage("player '" + player + "' up");
	}


/**
    Handle a click in 'place peg' mode.
    If we can place a peg in the given location,
    do so, and go to next state. Otherwise, do nothing.
**/
private boolean
tryPlacePeg(TWPoint p) {

    if (theModel_.pegIsOpen(p.x, p.y) && 
        theModel_.pegIsAvailableToPlayer(p.x, p.y, currentPlayerIndex_)) {
        
        theModel_.setPegAt(p.x, p.y, currentPlayerIndex_);  // update the model
        
        playPegSound();
        
        drawBoard_.repaint();                               // make the display agree

        goToStatePlaceLinkStart();                  // go to next state
        return true;
    	}
    else {
        return false;
    	}
	} // tryPlacePeg


/**
    
**/
private void
tryPlaceLinkStart(TWPoint p) {

    if (theModel_.pegBelongsTo(p.x, p.y, currentPlayerIndex_)) {
        
        // hold on to start point
        linkStartPoint_ = p;
        System.out.println("\n link start OK @ " + p.x + "/" + p.y);

        playLinkStartSound();
        
        goToStatePlaceLinkEnd();                  // go to next state
	    }
	
	} // tryPlaceLinkStart


/**
    Attempt to place a link end.
    If OK, place the link; if not, play naughty sound.
    Go back to place-link-start state.
**/
private void
tryPlaceLinkEnd(TWPoint p) {

    if (theModel_.pegBelongsTo(p.x, p.y, currentPlayerIndex_)) {

        // always check from left to right, so swap points if necessary
        TWPoint a = linkStartPoint_;
        TWPoint b = p;
        if (a.x > b.x) {
            a = p;
            b = linkStartPoint_;
        	}
 
        if (theModel_.canLinkTo(a, b, currentPlayerIndex_)) {
            int newDir = Utils.directionFromAtoB(a, b);
            
            // Jan05 - this no longer makes callback to playerWins, but just sets model's 'winner' state.
            //
            theModel_.setLink(a, b, newDir, currentPlayerIndex_);  // update the model
            
            drawBoard_.repaint();                               // make the display agree

            playLinkEndSound();

            // Jan05 - check the state var to see if winner, if so, DTRT.
            if (theModel_.getWinner() != -1) {

                playerWins(theModel_.getWinner());
            	}

            goToStatePlaceLinkStart();                  // go to next state
            return;
	        }
	    }
    playBlockedSound();
    goToStatePlaceLinkStart();    // any invalid click takes us back to start-place-link
	    
	} // tryPlaceLinkEnd


// state changes
//
/**
    Go to anyone-can-start-game mode
**/
private void
goToStateNetStart() {

    menuItemNetwork_.setEnabled(false);

    System.out.println(" -> STATE_NET_START");
    setTurnLabel("ANYONE");
    currentState_ = STATE_NET_START;
    drawBoard_.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }


/**
    goToStateNotMyTurn
**/
private void
goToStateNotMyTurn() {
    
    System.out.println(" -> STATE_NET_NOT_MY_TURN");
    currentState_ = STATE_NET_NOT_MY_TURN;

    toolImage_.setImageMode(currentState_);	// Jan05
    toolImage_.repaint();

    drawBoard_.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }


/**
    goToStatePlacePeg
**/
private void
goToStatePlacePeg() {

    System.out.println(" -> STATE_PLACE_PEG");
    currentState_ = STATE_PLACE_PEG;

    toolImage_.setImageMode(currentState_);	// Jan05
    toolImage_.repaint();

    drawBoard_.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }


/**
    goToStatePlaceLinkStart
**/
private void
goToStatePlaceLinkStart() {
    
    System.out.println(" -> STATE_PLACE_LINK_START");
    currentState_ = STATE_PLACE_LINK_START;

    toolImage_.setImageMode(currentState_);	// Jan05
    toolImage_.repaint();

    drawBoard_.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

/**
    goToStatePlaceLinkEnd
**/
private void
goToStatePlaceLinkEnd() {
    
    System.out.println(" -> STATE_PLACE_LINK_END");
    currentState_ = STATE_PLACE_LINK_END;
    
    toolImage_.setImageMode(currentState_);	// Jan05
    toolImage_.repaint();

    drawBoard_.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
    }


//   Handlers for events in the board display.
//
/**
    mouseClicked is significant for:
        - placing a peg (if we're in 'place peg' mode)
        - placing a link start (if we're in 'place link start' mode)
        - placing a link end   (if we're in 'place link end' mode)
**/
public void
mouseClicked(java.awt.event.MouseEvent me) {
    
//    System.out.println("\n mouseClicked");
    TWPoint p = drawBoard_.mapClickToRowAndCol(new TWPoint(me.getPoint()));
//    System.out.println("mouse click @ " + p.x + "/" + p.y);

    if (currentState_ == STATE_PLACE_PEG) {
        tryPlacePeg(p);
    	}
    else
    if (currentState_ == STATE_PLACE_LINK_START) {
        tryPlaceLinkStart(p);
    	}
    else
    if (currentState_ == STATE_PLACE_LINK_END) {
        tryPlaceLinkEnd(p);
    	}
    else
    if (commControl_.isConnected() && currentState_ == STATE_NET_START) {
       
        if (tryPlacePeg(p)) {
            
            // We got the drop on 'em. We go first.
            //
            System.out.println("We got the drop on 'em!");
            currentPlayerIndex_ = thisPlayerIndex_;
            setPlayerTurn(currentPlayerIndex_);
            showMessage("You are Red player.");
            setYouLabel("Red");
            sendState();
	        }
	    }

	} // mouseClicked


/**
    mouseReleased is significant for:
        - nothing?
         (might be nice for dragging link, but that's too complicated.....)
         
**/
public void mouseReleased(java.awt.event.MouseEvent me) {

    /* TWPoint p = */ drawBoard_.mapClickToRowAndCol(new TWPoint(me.getPoint()));
//    System.out.println("\n mouseReleased @ " + p.x + "/" + p.y);
	}

//
// These events never seem to happen! ??
//
public void mouseDragged(java.awt.event.MouseEvent me){
//    System.out.println("\n mouseDragged!!!!");
    }

public void mouseEntered(java.awt.event.MouseEvent me){
//    System.out.println("\n mouseEntered!!!!");
    }

public void mouseExited (java.awt.event.MouseEvent me){
//    System.out.println("\n mouseExited!!!!");
    }

public void mousePressed(java.awt.event.MouseEvent me){
//    System.out.println("\n mousePressed!!!!");
    
    }

public void mouseMoved(java.awt.event.MouseEvent me){
//    System.out.println("\n mouseMoved!!!!");
    }

// Sound-playing methods
//
private void
playPegSound() {
    acPeg_.play();
	}

private void
playLinkStartSound() {
    acLinkStart_.play();
	}

private void
playLinkEndSound() {
    acLinkEnd_.play();
	}

private void
playBlockedSound() {
    acBlocked_.play();
	}

private void
playInitCommSound() {
    acInitComm_.play();
	}

private void
playAboutSound() {
    acAbout_.play();
	}

private void
playIntroSound() {
    acIntro_.play();
	}


/**
    Pre-load the audio clips, for better performance (I assume)
**/
private void
loadAudio() {

    acPeg_       = loadIt("file:peg.wav");
    acLinkStart_ = loadIt("file:linkStart.wav");
    acLinkEnd_   = loadIt("file:linkEnd.wav");
    acBlocked_   = loadIt("file:blocked.wav");
    acInitComm_  = loadIt("file:initComm.wav");
    acAbout_     = loadIt("file:about.wav");
    acIntro_     = loadIt("file:intro.wav");
	}


private AudioClip
loadIt(String clipName) {

    AudioClip result = null;
    try {
        result = Applet.newAudioClip(new URL(clipName));
    	}
    catch (Exception e) {
        System.out.println("*** Error loading sound clip '" + clipName + "'!");
    	}
    return result;
	}


/**
    Send a message to our opponent.
**/
private void
sendMessage() {

    if (commControl_.isConnected()) {

        String message = JOptionPane.showInputDialog(
                			this, "Enter your message", 
                            "Send a message", JOptionPane.OK_CANCEL_OPTION);

        if (message == null) { // Cancel?
            return;
        	}
 
        AuxInfo info = new AuxInfo(message);
        commControl_.sendObject(info);
	    }
	}


/**
    doShutdown
    Verify that they really want to quit; kiss other player goodbye; close down.
**/
private void
doShutdown() {
    
    // Verify that they want to quit? Only if connected.
    // ACTUALLY, WE CAN'T STOP THE WINDOW CLOSE. DANG.
    //
    if (commControl_.isConnected()) {

        int answer = JOptionPane.showConfirmDialog(
                			this, "You are connected.\nSure you want to quit?",
                            "Drop connection?", JOptionPane.OK_CANCEL_OPTION);
        
        if (answer == JOptionPane.CANCEL_OPTION) 
            return;

        // Notify other player?
        //
        commControl_.sendObject(new AuxInfo(SHUTDOWN_MESSAGE));

        commControl_.shutdown();
    	}

    commControl_ = null;

/*
    System.out.println("PRESS <ENTER> KEY TO EXIT. HA HA.");
    try {
        System.in.read();
    }
    catch (Exception e) {}
*/

    System.exit(0);
	} // doShutdown


/**
    This player won!
    More cleanup needed here too?
**/
public void 
playerWins(int playerIndex) {

    // Either "red" or "blue" (or actual player names, when implemented)
    //
    String message = players_[currentPlayerIndex_].playerName + " wins!";
    
    if (commControl_.isConnected()) {
        
        // Tell the other side we won
        //
        commControl_.sendObject(new AuxInfo(WIN_MESSAGE));
        
    	}

    JOptionPane.showMessageDialog(this, message, "You win!", JOptionPane.INFORMATION_MESSAGE);

	}
    
/**
    Present the networking dialog.
**/
private void
doNetDialog() {

    playInitCommSound();

    //  Pick a random name to populate the default player name.
    String defaultName = randomNames_[new Random().nextInt(randomNames_.length)];

    NetDialog tnd = new NetDialog(
					        (JFrame)this, 
					        (iConnectHandler)this, 
					        (iComm)commControl_, 
					        defaultName
					        );

    // It's modal, and won't return till done.
    //
    tnd.setVisible(true);

    String newName = tnd.getPlayerName();
    System.out.println("doNetDialog: name is " + newName);
    players_[thisPlayerIndex_].playerName = newName;
	}



private void
doAbout() {

    playAboutSound();

    JDialog jd = new JDialog(this, "About TW3", true);
    jd.setSize(new Dimension(256, 256+34));     // fudge for titlebar and menu

    // Center ourself on the screen.
    //
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    int w = jd.getSize().width;
    int h = jd.getSize().height;
    int x = (screen.width - w) / 2;
    int y = (screen.height - h) / 3;
    jd.setBounds(x, y, w, h);
    jd.setResizable(false);

    jd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    SplashImage si = new SplashImage();
    jd.getContentPane().add(si);

    jd.setVisible(true);
	}

/**
    Start a new game.
**/
private void
doNew() {
    
    theModel_.newGame();
    setPlayerTurn(PLAYER_1);
    repaint();
    goToStatePlacePeg();
	}

/**
    Reset the comm stuff after a connection has ended.
**/
private void
resetComm() {

    menuItemNetwork_.setEnabled(true);
    commControl_.reset();
	}


}   // TW3App

