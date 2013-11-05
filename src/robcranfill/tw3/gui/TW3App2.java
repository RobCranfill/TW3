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

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Date;
import java.util.Random;

import javax.swing.*;

import robcranfill.tw3.*;
import robcranfill.tw3.com.*;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.MouseInputListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.border.BevelBorder;

public class TW3App2 implements MouseInputListener, ActionListener, // standard
																// interfaces
	iObjHandler, iGUI, iConnectHandler // our interfaces
{

// The JFrame for the app:
private JFrame				frame_;

// The object that represents the state of play:
private Model				theModel_;

// The object that draws the playing board:
private GameBoard			drawBoard_;

// The possible states:
public static final int		STATE_OTHER_TURN		= 1;
public static final int		STATE_PLACE_PEG			= 2;
public static final int		STATE_PLACE_LINK_START	= 3;
public static final int		STATE_PLACE_LINK_END	= 4;
public static final int		STATE_NET_START			= 5;
public static final int		STATE_NET_NOT_MY_TURN	= 6;
private int					currentState_			= STATE_OTHER_TURN;	// hmmm


// misc instance vars:

/*
 * First player is always red. 
 * In a non-network game, red simply goes first; in a networked game, first player to place a peg is red.
 */

// Changeable player names (IP not used now)
private PlayerInfo players_[] =
	{
	new PlayerInfo("Red", "127.0.0.1"),
	new PlayerInfo("Blue", "127.0.0.1")
	};

// Non-changeable names
private String				playerColors_[]						= { "Red", "Blue" };								

private static final int	PLAYER_1							= 0;												// indices into players[], etc

private static final int	PLAYER_2							= 1;
private int					currentPlayerIndex_					= PLAYER_1;
// for network games; does not denote order of play
private int					thisPlayerIndex_					= PLAYER_1;

// Comm
private CommThing			commControl_						= null;
private static String		SHUTDOWN_MESSAGE					= "OPPONENT IS SHUTTING DOWN. CRUMB!";
private static String		NEWGAME_MESSAGE						= "OPPONENT SAYS 'NEW GAME'!";
private static String		WIN_MESSAGE							= "OTHER PLAYER WON!";

// GUI things
private static final int	CONTROL_PANEL_WIDTH					= 140;
private static final int	BOARD_SIZE							= 392;

private static final int	MENU_FUDGE							= 56;												// and
																													// window
																													// title
																													// bar,
																													// too
private static final int	BEVEL_FUDGE							= 4;												//

private static final int	WINDOW_WIDTH						= CONTROL_PANEL_WIDTH + BOARD_SIZE + BEVEL_FUDGE;
private static final int	WINDOW_HEIGHT						= BOARD_SIZE + MENU_FUDGE;

private JButton				buttonTurnDone_, buttonMessage_;
private static String		TOOL_TIP_BUTTON_DONE_YOUR_TURN		= "Press this when you're finished with your turn";
private static String		TOOL_TIP_BUTTON_DONE_NOT_YOUR_TURN	= "(It's not your turn; wait for it)";

// private JTextArea commText_;
// private JLabel labelWhosTurnIsIt_, labelWhoYouAre_;
private JMenuItem			menuItemNetwork_;																		// so
																													// we
																													// can
																													// disable
																													// it

// pre-loaded audio
private AudioClip			acPeg_, acLinkStart_, acLinkEnd_;
private AudioClip			acBlocked_, acInitComm_, acAbout_, acIntro_;

private TWPoint				linkStartPoint_						= null;											// gotta
																													// hold
																													// it
																													// across
																													// clicks

private ToolImage			toolImage_							= null;											// Jan05

static String[]				randomNames_						= { "Bert Fegg", "Skanky", "Shakes, the Clown",
		"Arthur", "Sniggendorf", "Jocko Homo", "Beatrice", "Scrotus", "Cleetus", "Arthur 'Two Sheds' Jackson",
		"Throatwarbler Mangrove", "Bob"						};
private JLabel				labelWhosTurnIsIt_;
private JLabel				labelWhoYouAre_;
private JPanel				controlPanel_;
private JTextPane			txtpnMessages_;
private JScrollPane 	scrollPane_;

/**
 * Launch the application.
 */
public static void main(String[] args)
	{
	EventQueue.invokeLater(new Runnable()
		{
			public void run()
				{
				try
					{
					TW3App2 window = new TW3App2();
					window.frame_.setVisible(true);
					}
				catch (Exception e)
					{
					e.printStackTrace();
					}
				}
		});
	}

/**
 * Create the application.
 */
public TW3App2()
	{

	initialize();

	controlPanel_ = new JPanel();
	controlPanel_.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));

	JPanel msgPanel = new JPanel();
	msgPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));

	JPanel gamePanel = new JPanel();
	gamePanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
	GroupLayout groupLayout = new GroupLayout(frame_.getContentPane());
	groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(
		groupLayout
				.createSequentialGroup()
				.addContainerGap()
				.addComponent(controlPanel_, GroupLayout.PREFERRED_SIZE, 146, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(
					groupLayout.createParallelGroup(Alignment.LEADING)
							.addComponent(msgPanel, GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
							.addComponent(gamePanel, GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE))
				.addContainerGap()));
	groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(
		groupLayout
				.createSequentialGroup()
				.addContainerGap()
				.addGroup(
					groupLayout
							.createParallelGroup(Alignment.LEADING)
							.addComponent(controlPanel_, GroupLayout.PREFERRED_SIZE, 273,
								GroupLayout.PREFERRED_SIZE)
							.addGroup(
								groupLayout
										.createSequentialGroup()
										.addComponent(msgPanel, GroupLayout.PREFERRED_SIZE, 94,
											GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(gamePanel, GroupLayout.PREFERRED_SIZE, 383,
											GroupLayout.PREFERRED_SIZE))).addGap(62)));
	msgPanel.setLayout(new BorderLayout(0, 0));

	scrollPane_ = new JScrollPane();
	msgPanel.add(scrollPane_);

	txtpnMessages_ = new JTextPane();
	txtpnMessages_.setEditable(false);
	txtpnMessages_.setText("Messages here");

	scrollPane_.setViewportView(txtpnMessages_);

	JButton btnDone = new JButton("Done");
	btnDone.setActionCommand("Done");
	btnDone.addActionListener(new ActionListener()
		{
		public void actionPerformed(ActionEvent ev)
			{
			nextPlayerTurn();
			sendState();
			}
		});

	JButton btnSendMessage = new JButton("Send Message");
	btnSendMessage.setEnabled(false);

	labelWhosTurnIsIt_ = new JLabel("labelWhosTurnIsIt_");
	labelWhoYouAre_ = new JLabel("labelWhoYouAre_");

	JPanel toolImagePanel = new JPanel();

	GroupLayout gl_controlPanel_ = new GroupLayout(controlPanel_);
	gl_controlPanel_.setHorizontalGroup(gl_controlPanel_.createParallelGroup(Alignment.LEADING).addGroup(
		gl_controlPanel_
				.createSequentialGroup()
				.addContainerGap()
				.addGroup(
					gl_controlPanel_
							.createParallelGroup(Alignment.LEADING)
							.addComponent(toolImagePanel, GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
							.addGroup(
								gl_controlPanel_
										.createParallelGroup(Alignment.TRAILING, false)
										.addComponent(btnDone, Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
											GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(btnSendMessage, Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
											GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
							.addComponent(labelWhosTurnIsIt_).addComponent(labelWhoYouAre_)).addContainerGap()));
	gl_controlPanel_.setVerticalGroup(gl_controlPanel_.createParallelGroup(Alignment.LEADING).addGroup(
		gl_controlPanel_.createSequentialGroup().addContainerGap().addComponent(btnDone)
				.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnSendMessage)
				.addPreferredGap(ComponentPlacement.RELATED).addComponent(labelWhosTurnIsIt_)
				.addPreferredGap(ComponentPlacement.RELATED).addComponent(labelWhoYouAre_)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(toolImagePanel, GroupLayout.PREFERRED_SIZE, 138, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(112, Short.MAX_VALUE)));
	controlPanel_.setLayout(gl_controlPanel_);
	frame_.getContentPane().setLayout(groupLayout);

	frame_.setVisible(true); // we were created inviz; show ourselves.

	// postInit();
	//
	// } // cons
	//
	//
	//
	// /**
	// * After-GUI init.
	// */
	// private void postInit()
	// {

	// Create the play model
	//
	theModel_ = new Model(this);

	// fucks WBP up
	drawBoard_ = new GameBoard(theModel_);
	drawBoard_.addMouseListener(this);
	gamePanel.add(drawBoard_);

	// The tool panel that shows what state you're in.
	toolImage_ = new ToolImage();
	int by = 0;
	toolImagePanel.setLayout(new BorderLayout(0, 0));
	toolImage_.setBounds(8, by, toolImage_.getWidth(), toolImage_.getHeight());
	toolImagePanel.add(toolImage_);

	doNew();
	loadAudio();

	// Create the communications object
	//
	commControl_ = new CommThing(this); // this obj is the listener
	(new Thread(commControl_)).start();

	playIntroSound();

	}

/**
 * Initialize the contents of the frame_.
 */
private void initialize()
	{
	frame_ = new JFrame("TW3 3.0a");

	frame_.setBounds(200, 100, 465, 600);
	frame_.setSize(719, 615);

	// Center ourself on the screen.
	//
	Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	int w = frame_.getSize().width;
	int h = frame_.getSize().height;
	int x = (screen.width - w) / 2;
	int y = (screen.height - h) / 3; // 1/3rd from top, actually
	frame_.setBounds(x, y, w, h);

	frame_.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

	JMenuBar menuBar = new JMenuBar();
	frame_.setJMenuBar(menuBar);

	JMenu mnFile = new JMenu("File");
	menuBar.add(mnFile);

	JMenuItem mntmNewLocalGame = new JMenuItem("New Local Game...");
	mntmNewLocalGame.addActionListener(this);
	mntmNewLocalGame.setActionCommand("new");
	mnFile.add(mntmNewLocalGame);
    
	JMenuItem mntmNewNetworkGame = new JMenuItem("New Network Game...");
	mntmNewNetworkGame.addActionListener(this);
	mntmNewNetworkGame.setActionCommand("net");
	mnFile.add(mntmNewNetworkGame);
  
	JSeparator separator = new JSeparator();
	mnFile.add(separator);

	JMenuItem mntmExit = new JMenuItem("Exit");
	mntmExit.setActionCommand("exit");
	mntmExit.addActionListener(this);
	mnFile.add(mntmExit);
    
	JMenu mnHelp = new JMenu("Help");
	menuBar.add(mnHelp);
	
	JMenuItem mntmAboutTw = new JMenuItem("About TW3...");
	mntmAboutTw.addActionListener(this);
	mntmAboutTw.setActionCommand("about");
	mnHelp.add(mntmAboutTw);
	}


/**
 * For handling menu selections.
 **/
public void actionPerformed(ActionEvent ev)
	{
	String action = ev.getActionCommand();
	if (action.equals("exit"))
		{
//		appendMessage("exit!");
		doShutdown();
		}
	else if (action.equals("new"))
		{
//		appendMessage("new!");
		doNew();
		}
	else if (action.equals("net"))
		{
//		appendMessage("net!");
		doNetDialog();
		}
	else if (action.equals("about"))
		{
//		appendMessage("about!");
		doAbout();
		}
	else
		{
		appendMessage("Unknown menu action? %s", action);
		}
	}

/**
 * Display an incoming message. Is there a better thing to do?
 **/
public void showMessage(String message)
	{
	// commText_.setText(commText_.getText() + message + "\n");
	appendMessage("SHOW: " + message);
	}


/**
 * Send the model to 'the other side'.
 **/
public void sendState()
	{
	if (commControl_.isConnected())
		{
		commControl_.sendObject(theModel_);
		}
	}

/**
 * Handle an object(a model) coming from 'the other side'. This only happens
 * if we're in a netted game.
 **/
public void handleObject(Object o)
	{
	if (o instanceof AuxInfo)
		{

		AuxInfo info = (AuxInfo) o;

		if (info.getComment().equals(SHUTDOWN_MESSAGE))
			{
			JOptionPane.showMessageDialog(frame_, "Your opponent has dropped the connection.\nWhat did you do?!",
				"Connection dropped", JOptionPane.OK_OPTION);
			resetComm();
			}
		else if (info.getComment().equals(WIN_MESSAGE))
			{
			String otherPlayer = players_[PLAYER_2 - thisPlayerIndex_].playerName;
			JOptionPane.showMessageDialog(frame_, otherPlayer + " wins!\nTry again!", "You lose",
				JOptionPane.OK_OPTION);
			}
		else
			{
			// showMessage(">> MESSAGE: '" + info + "'");
			JOptionPane.showMessageDialog(frame_, info.getComment(), "Message", JOptionPane.OK_OPTION);
			}
		return;
		}

	// If we were waiting to start a game,
	// this means the other guy got the drop on us.
	//
	if (currentState_ == STATE_NET_START)
		{

		// we are player 2
		//
		thisPlayerIndex_ = PLAYER_2;
		currentPlayerIndex_ = PLAYER_2; // nextPlayerTurn is gonna toggle this, so set it to the wrong thing. Gurk

		appendMessage("Other guy (now PLAYER_1) got the drop!");

		showMessage("You are Blue player.");
		setYouLabel("Blue");
		}

	System.out.println(" >>> handleObject: " + o);

	theModel_ = (Model) o; // something seems wrong here....
	drawBoard_.setModel(theModel_);

	theModel_.registerListener(this);

	nextPlayerTurn();
	} // handleObject

/**
 * We've started a 'server' session.
 **/
public void handleCatch()
	{
	players_[currentPlayerIndex_].playerName = "how?";

	showMessage("Connected! You are 'Catcher'.");
	enableMessageButton(true);
	goToStateNetStart();
	}

/**
 * We've started a 'client' session.
 **/
public void handlePitch()
	{
	showMessage("Connected! You are 'Pitcher'.");
	enableMessageButton(true);
	goToStateNetStart();
	}

/**
 * Display player's name.
 **/
private void setTurnLabel(String who)
	{
	if (labelWhosTurnIsIt_ != null)
		{
		labelWhosTurnIsIt_.setText(who + "'s (" + playerColors_[currentPlayerIndex_] + ") turn");
		}
	}

/**
 * Display player's name.
 **/
private void setYouLabel(String who)
	{
	labelWhoYouAre_.setText("You are " + who);
	}

/**
 * Change players; that is, flip a/b.
 **/
private void nextPlayerTurn()
	{

	setPlayerTurn((currentPlayerIndex_ == PLAYER_1) ? PLAYER_2 : PLAYER_1);

	if (commControl_.isConnected())
		{
		if (currentPlayerIndex_ == thisPlayerIndex_)
			{
			buttonTurnDone_.setEnabled(true);
			buttonTurnDone_.setToolTipText(TOOL_TIP_BUTTON_DONE_YOUR_TURN);
			goToStatePlacePeg();
			}
		else
			{ // not my turn
			buttonTurnDone_.setEnabled(false);
			buttonTurnDone_.setToolTipText(TOOL_TIP_BUTTON_DONE_NOT_YOUR_TURN);
			goToStateNotMyTurn();
			}
		}
	else
		{ // not connected
		goToStatePlacePeg();
		}
	}

/**
 * Change players to the given player.
 **/
private void setPlayerTurn(int who)
	{
	currentPlayerIndex_ = who;
	String player = players_[currentPlayerIndex_].playerName;
	setTurnLabel(player);
	showMessage("player '" + player + "' up");
	}

/**
 * Handle a click in 'place peg' mode. If we can place a peg in the given
 * location, do so, and go to next state. Otherwise, do nothing.
 **/
private boolean tryPlacePeg(TWPoint p)
	{
	if (theModel_.pegIsOpen(p.x, p.y) && theModel_.pegIsAvailableToPlayer(p.x, p.y, currentPlayerIndex_))
		{

		theModel_.setPegAt(p.x, p.y, currentPlayerIndex_); // update the
															// model

		playPegSound();

		drawBoard_.repaint(); // make the display agree

		goToStatePlaceLinkStart(); // go to next state
		return true;
		}
	else
		{
		return false;
		}
	} // tryPlacePeg

/**
    
**/
private void tryPlaceLinkStart(TWPoint p)
	{
	if (theModel_.pegBelongsTo(p.x, p.y, currentPlayerIndex_))
		{

		// hold on to start point
		linkStartPoint_ = p;
		System.out.println("\n link start OK @ " + p.x + "/" + p.y);

		playLinkStartSound();

		goToStatePlaceLinkEnd(); // go to next state
		}

	} // tryPlaceLinkStart

/**
 * Attempt to place a link end. If OK, place the link; if not, play naughty
 * sound. Go back to place-link-start state.
 **/
private void tryPlaceLinkEnd(TWPoint p)
	{
	if (theModel_.pegBelongsTo(p.x, p.y, currentPlayerIndex_))
		{

		// always check from left to right, so swap points if necessary
		TWPoint a = linkStartPoint_;
		TWPoint b = p;
		if (a.x > b.x)
			{
			a = p;
			b = linkStartPoint_;
			}

		if (theModel_.canLinkTo(a, b, currentPlayerIndex_))
			{
			int newDir = Utils.directionFromAtoB(a, b);

			// Jan05 - this no longer makes callback to playerWins, but just
			// sets model's 'winner' state.
			//
			theModel_.setLink(a, b, newDir, currentPlayerIndex_); // update
																	// the
																	// model

			drawBoard_.repaint(); // make the display agree

			playLinkEndSound();

			// Jan05 - check the state var to see if winner, if so, DTRT.
			if (theModel_.getWinner() != -1)
				{
				playerWins(theModel_.getWinner());
				}

			goToStatePlaceLinkStart(); // go to next state
			return;
			}
		}
	playBlockedSound();
	goToStatePlaceLinkStart(); // any invalid click takes us back to
								// start-place-link

	} // tryPlaceLinkEnd

// state changes
//
/**
 * Go to anyone-can-start-game mode
 **/
private void goToStateNetStart()
	{
	menuItemNetwork_.setEnabled(false);

	System.out.println(" -> STATE_NET_START");
	setTurnLabel("ANYONE");
	currentState_ = STATE_NET_START;
	drawBoard_.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

/**
 * goToStateNotMyTurn
 **/
private void goToStateNotMyTurn()
	{
	System.out.println(" -> STATE_NET_NOT_MY_TURN");
	currentState_ = STATE_NET_NOT_MY_TURN;

	toolImage_.setImageMode(currentState_); // Jan05
	toolImage_.repaint();

	drawBoard_.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

/**
 * goToStatePlacePeg
 **/
private void goToStatePlacePeg()
	{
	System.out.println(" -> STATE_PLACE_PEG");
	currentState_ = STATE_PLACE_PEG;

	toolImage_.setImageMode(currentState_); // Jan05
	toolImage_.repaint();

	drawBoard_.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

/**
 * goToStatePlaceLinkStart
 **/
private void goToStatePlaceLinkStart()
	{
	System.out.println(" -> STATE_PLACE_LINK_START");
	currentState_ = STATE_PLACE_LINK_START;

	toolImage_.setImageMode(currentState_); // Jan05
	toolImage_.repaint();

	drawBoard_.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}

/**
 * goToStatePlaceLinkEnd
 **/
private void goToStatePlaceLinkEnd()
	{
	System.out.println(" -> STATE_PLACE_LINK_END");
	currentState_ = STATE_PLACE_LINK_END;

	toolImage_.setImageMode(currentState_); // Jan05
	toolImage_.repaint();

	drawBoard_.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
	}

// Handlers for events in the board display.
//
/**
 * mouseClicked is significant for: - placing a peg (if we're in 'place peg'
 * mode) - placing a link start (if we're in 'place link start' mode) -
 * placing a link end (if we're in 'place link end' mode)
 **/
public void mouseClicked(java.awt.event.MouseEvent me)
	{

	// System.out.println("\n mouseClicked");
	TWPoint p = drawBoard_.mapClickToRowAndCol(new TWPoint(me.getPoint()));
	// System.out.println("mouse click @ " + p.x + "/" + p.y);

	if (currentState_ == STATE_PLACE_PEG)
		{
		tryPlacePeg(p);
		}
	else if (currentState_ == STATE_PLACE_LINK_START)
		{
		tryPlaceLinkStart(p);
		}
	else if (currentState_ == STATE_PLACE_LINK_END)
		{
		tryPlaceLinkEnd(p);
		}
	else if (commControl_.isConnected() && currentState_ == STATE_NET_START)
		{

		if (tryPlacePeg(p))
			{

			// We got the drop on 'em. We go first.
			//
			appendMessage("We got the drop on 'em!");
			currentPlayerIndex_ = thisPlayerIndex_;
			setPlayerTurn(currentPlayerIndex_);
			showMessage("You are Red player.");
			setYouLabel("Red");
			sendState();
			}
		}

	} // mouseClicked

/**
 * mouseReleased is significant for: - nothing? (might be nice for dragging
 * link, but that's too complicated.....)
 **/
public void mouseReleased(java.awt.event.MouseEvent me)
	{

	/* TWPoint p = */drawBoard_.mapClickToRowAndCol(new TWPoint(me.getPoint()));
	// System.out.println("\n mouseReleased @ " + p.x + "/" + p.y);
	}

//
// These events never seem to happen! ??
//
public void mouseDragged(java.awt.event.MouseEvent me)
	{
	// System.out.println("\n mouseDragged!!!!");
	}

public void mouseEntered(java.awt.event.MouseEvent me)
	{
	// System.out.println("\n mouseEntered!!!!");
	}

public void mouseExited(java.awt.event.MouseEvent me)
	{
	// System.out.println("\n mouseExited!!!!");
	}

public void mousePressed(java.awt.event.MouseEvent me)
	{
	// System.out.println("\n mousePressed!!!!");

	}

public void mouseMoved(java.awt.event.MouseEvent me)
	{
	// System.out.println("\n mouseMoved!!!!");
	}

// Sound-playing methods
//
private void playPegSound()
	{
	acPeg_.play();
	}

private void playLinkStartSound()
	{
	acLinkStart_.play();
	}

private void playLinkEndSound()
	{
	acLinkEnd_.play();
	}

private void playBlockedSound()
	{
	acBlocked_.play();
	}

private void playInitCommSound()
	{
	acInitComm_.play();
	}

private void playAboutSound()
	{
	acAbout_.play();
	}

private void playIntroSound()
	{
	acIntro_.play();
	}

/**
 * Pre-load the audio clips, for better performance (I assume)
 **/
private void loadAudio()
	{

	acPeg_ = loadIt("file:peg.wav");
	acLinkStart_ = loadIt("file:linkStart.wav");
	acLinkEnd_ = loadIt("file:linkEnd.wav");
	acBlocked_ = loadIt("file:blocked.wav");
	acInitComm_ = loadIt("file:initComm.wav");
	acAbout_ = loadIt("file:about.wav");
	acIntro_ = loadIt("file:intro.wav");
	}

private AudioClip loadIt(String clipName)
	{

	AudioClip result = null;
	try
		{
		result = Applet.newAudioClip(new URL(clipName));
		}
	catch (Exception e)
		{
		System.out.println("*** Error loading sound clip '" + clipName + "'!");
		}
	return result;
	}

/**
 * Send a message to our opponent.
 **/
private void sendMessage()
	{

	if (commControl_.isConnected())
		{

		String message = JOptionPane.showInputDialog(frame_, "Enter your message", "Send a message",
			JOptionPane.OK_CANCEL_OPTION);

		if (message == null)
			{ // Cancel?
			return;
			}

		AuxInfo info = new AuxInfo(message);
		commControl_.sendObject(info);
		}
	}

/**
 * doShutdown Verify that they really want to quit; kiss other player
 * goodbye; close down.
 **/
private void doShutdown()
	{
	appendMessage("Buh-bye!");

	// Verify that they want to quit? Only if connected.
	// ACTUALLY, WE CAN'T STOP THE WINDOW CLOSE. DANG.
	//
	if (commControl_.isConnected())
		{
		int answer = JOptionPane.showConfirmDialog(frame_, "You are connected.\nSure you want to quit?",
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
	 * System.out.println("PRESS <ENTER> KEY TO EXIT. HA HA."); try {
	 * System.in.read(); } catch (Exception e) {}
	 */

	System.exit(0);
	} // doShutdown

/**
 * This player won! More cleanup needed here too?
 **/
public void playerWins(int playerIndex)
	{
	// Either "red" or "blue" (or actual player names, when implemented)
	//
	String message = players_[currentPlayerIndex_].playerName + " wins!";

	if (commControl_.isConnected())
		{

		// Tell the other side we won
		//
		commControl_.sendObject(new AuxInfo(WIN_MESSAGE));
		}

	JOptionPane.showMessageDialog(frame_, message, "You win!", JOptionPane.INFORMATION_MESSAGE);

	}

/**
 * Present the networking dialog.
 **/
private void doNetDialog()
	{
	appendMessage("New networked game....");

	playInitCommSound();

	// Pick a random name to populate the default player name.
	String defaultName = randomNames_[new Random().nextInt(randomNames_.length)];

	NetDialog tnd = new NetDialog(frame_, (iConnectHandler) this, (iComm) commControl_, defaultName);

	// It's modal, and won't return till done.
	//
	tnd.setVisible(true);

	String newName = tnd.getPlayerName();
	appendMessage("doNetDialog: name is %s", newName);

	players_[thisPlayerIndex_].playerName = newName;
	}



private void doAbout()
	{
	playAboutSound();

	JDialog jd = new JDialog(frame_, "About TW3", true);
	jd.setSize(new Dimension(256, 256 + 34)); // fudge for titlebar and menu

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
 * Start a new game.
 **/
private void doNew()
	{
	appendMessage("New game....");
	theModel_.newGame();
	setPlayerTurn(PLAYER_1);
	frame_.repaint();
	goToStatePlacePeg();
	}

/**
 * Reset the comm stuff after a connection has ended.
 **/
private void resetComm()
	{
	menuItemNetwork_.setEnabled(true);
	commControl_.reset();
	}

/**
 * Enable or disable the message button
 **/
private void enableMessageButton(boolean enabled)
	{
	buttonMessage_.setEnabled(enabled);
	if (enabled)
		{
		buttonMessage_.setToolTipText("Click here to send a nice message to your opponent");
		}
	else
		{
		buttonMessage_.setToolTipText("(Who would you send a message to?)");
		}
	}


/**
 * Add the text to the end of the scrolling message area.
 * 
 * @param text	If there are args, had better be printf-compatible.
 * 
 */
private void appendMessage(String text, Object... args)
	{
	System.out.printf(text, args);
	try
		{
		Document doc = txtpnMessages_.getDocument();
		doc.insertString(
			doc.getLength(), 
			String.format("\n%s: %s", new Date(), String.format(text, args)),
			null);
		scrollPane_.scrollRectToVisible(new Rectangle(0, txtpnMessages_.getBounds(null).height, 1, 1));
		}
	catch (BadLocationException e)
		{
		e.printStackTrace();
		}
	}

}
