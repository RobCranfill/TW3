/**
 	NetDialog
    'Start network game' dialog.
    
    Not implemented: display this machine's IP (so catcher can tell pitcher where to go)
    
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

import java.awt.*;
import java.awt.event.*;
import java.net.Socket;

import javax.swing.*;
import javax.swing.border.*;

import robcranfill.tw3.com.Catcher;
import robcranfill.tw3.com.iComm;
import robcranfill.tw3.com.iConnectHandler;

@SuppressWarnings("serial")
public class NetDialog
     extends JDialog {

private iConnectHandler twController_;
private iComm           twComm_;

public final static int DEFAULT_PORT = 1234;

private final static int	DIALOG_WIDTH    = 320;
private final static int	DIALOG_HEIGHT   = 296;
private final static int	BUTTON_WIDTH    =  96;
private final static int	BUTTON_HEIGHT   =  18;
private final static int	TITLEBAR_FUDGE  =  22;
private final static int	PANEL_NAME_HEIGHT   =  32;
private final static int	PANEL_CATCH_HEIGHT  =  64;
private final static int	PANEL_PITCH_HEIGHT  =  96;

private JTextField  fieldName_, fieldIP_, fieldPort_;
private JButton     buttonPitch_, buttonCatch_, buttonOK_;
private boolean     doingCatch_;
private boolean     cancelling_;
private Catcher   twc_ = null;
private Thread      th_;

private String		playerName_;

/**
 * 
 * @param pFrame
 * @param parent
 * @param commObj
 * @param player	'red' or 'blue' as default name
 */
public NetDialog(JFrame pFrame, iConnectHandler parent, iComm commObj, String player) {

    super(pFrame, "Start network connection", true);

    JLabel  jl;
    JPanel  jp;
    int y1, y2;		// y1 for top-level items, y2 for sub items
    TitledBorder tb;
    
    twController_ = parent;
    twComm_ = commObj;

    setSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

    // Position ourself w/r/t the parent window.
    //
    Rectangle frame = pFrame.getBounds();

    int w = this.getSize().width;
    int h = this.getSize().height;
    int x = frame.x + (frame.width  - w) / 2;
    int y = frame.y + (frame.height - h) / 3;   // 1/3rd from top
    setBounds(x, y, w, h);
    setResizable(false);

    getContentPane().setLayout(null);


    // 'catch' panel
    //
    // y1 += PANEL_NAME_HEIGHT + 8;
    y1 = 8;
    jp = new JPanel(null);
    jp.setBounds(8, y1, DIALOG_WIDTH-16, PANEL_CATCH_HEIGHT);
    tb = BorderFactory.createTitledBorder(" Catch ");
    jp.setBorder(tb);
    getContentPane().add(jp);

    y2 = 16;    //  relative position in panel
    jl = new JLabel("Start a network game as receiver.");
    jp.add(jl);
    jl.setBounds(16, y2, DIALOG_WIDTH-96, BUTTON_HEIGHT);

    buttonCatch_ = new JButton("Catch");
    jp.add(buttonCatch_);
    buttonCatch_.setBounds(DIALOG_WIDTH-BUTTON_WIDTH-48, PANEL_CATCH_HEIGHT-BUTTON_HEIGHT-8, 
                 BUTTON_WIDTH, BUTTON_HEIGHT);
    buttonCatch_.setMnemonic('C');
    buttonCatch_.setToolTipText("Waits for a connection on the port specified");
    buttonCatch_.addActionListener(
        new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                doCatch();
                }
            }
        );

    // 'pitch' panel
    //
    y1 += PANEL_CATCH_HEIGHT + 8;
    jp = new JPanel(null);
    jp.setBounds(8, y1, DIALOG_WIDTH-16, PANEL_PITCH_HEIGHT);
    tb = BorderFactory.createTitledBorder(" Pitch ");
    jp.setBorder(tb);
    getContentPane().add(jp);

    y2 = 16;
    jl = new JLabel("Start a network game as initiator.");
    jp.add(jl);
    jl.setBounds(16, y2, DIALOG_WIDTH-96, BUTTON_HEIGHT);

    // IP of Catcher
    y2 += 24;
    jl = new JLabel("IP: ");
    jp.add(jl);
    jl.setBounds(16, y2, 64, BUTTON_HEIGHT);

    fieldIP_ = new JTextField("127.0.0.1");
    jp.add(fieldIP_);
    fieldIP_.setBounds(64+8, y2, 96, BUTTON_HEIGHT);
    fieldIP_.setToolTipText("IP address of the other computer");

    
    buttonPitch_ = new JButton("Pitch");
    jp.add(buttonPitch_);
    buttonPitch_.setBounds(DIALOG_WIDTH-BUTTON_WIDTH-48, PANEL_PITCH_HEIGHT-BUTTON_HEIGHT-8, 
                 BUTTON_WIDTH, BUTTON_HEIGHT);
// how?   buttonPitch_.setAccelerator(KeyStroke.getKeyStroke('P', java.awt.Event.ALT_MASK, false));
    buttonPitch_.setMnemonic('P');
    buttonPitch_.setToolTipText("Attempts to connect to the host and port specified");
    buttonPitch_.addActionListener(
        new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                doPitch();
                }
            }
        );
 
    // The port is common to pitch and catch, so it goes here
    //
    y1 += PANEL_PITCH_HEIGHT;
    jl = new JLabel("Port (1024-32767) :");
    getContentPane().add(jl);
    jl.setBounds(16, y1, 112, BUTTON_HEIGHT);

    fieldPort_ = new JTextField("" + DEFAULT_PORT);
    getContentPane().add(fieldPort_);
    fieldPort_.setBounds(16+112+8, y1, 48, BUTTON_HEIGHT);
    fieldPort_.setToolTipText("Port number for connection (catch AND pitch)");


    // Jan05 - Name
    //
    y1 += BUTTON_HEIGHT;
    jl = new JLabel("Name :");

    int x1 = 16 + 8;
 
    getContentPane().add(jl);
    jl.setBounds(x1 + 61, y1, 112, BUTTON_HEIGHT);

    fieldName_ = new JTextField(player);
    getContentPane().add(fieldName_);
    fieldName_.setBounds(x1 + 112, y1, DIALOG_WIDTH-(16+112+32), BUTTON_HEIGHT);
    fieldName_.setToolTipText("Your name, please");

    
    // OK button
    //
    buttonOK_ = new JButton("OK");
    getContentPane().add(buttonOK_);
    buttonOK_.setBounds(DIALOG_WIDTH-BUTTON_WIDTH-16, DIALOG_HEIGHT-(2*BUTTON_HEIGHT)-TITLEBAR_FUDGE, 
                 BUTTON_WIDTH, BUTTON_HEIGHT);
    buttonOK_.addActionListener(
        new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                doOK();
                }
            }
        );

    
    doingCatch_ = false;
	}


/**
    Handle click on 'OK' button. Also called when 'catch' or 'pitch' connects.
**/
private void
doOK() {

    playerName_ = fieldName_.getText();
    System.out.println("doOK: setting name '" + playerName_ + "'");
    
    this.setVisible(false);
    this.dispose();
	}


public String
getPlayerName() {
    return playerName_;
	}


/**
    Handle a click on the 'catch'/'cancel' button.
    Start a threaded object that will call our gotCatch method when done.
**/
private void
doCatch() {

    // while we're doing a catch, this button is really 'cancel' catch
    //
    if (doingCatch_) {
        
        cancelling_ = true;
        twc_.stopCatch();   // will eventually cause our gotCatch to fire, with null
        return;
	    }
    System.out.println("NetDialog handling catch!");
    
    cancelling_ = false;
    setCatchState(false);

    twc_ = new Catcher(this, Integer.parseInt(fieldPort_.getText()));
    th_ = new Thread(twc_);
    th_.start();
    doingCatch_ = true;
	}


/**
    The Catcher either got a catch, or timed out.
    'sock' will be the socket, or null.
**/
public void
gotCatch(Socket sock) {

    System.out.println("NetDialog gotCatch!");
    setCatchState(true);
    doingCatch_ = false;

    if (sock == null) {
        if ( cancelling_ ) {
            twc_ = null;
            th_  = null;
            setCatchState(true);
            System.out.println("NetDialog cancelled!");
	        }
        else
            JOptionPane.showMessageDialog(
                            this, 
                            "The 'catch' didn't work.\nCheck port number, and your IP.",
                            "'Catch' timeout", 
                            JOptionPane.OK_OPTION);
	    }
    else {
        twComm_.processListen(sock);    // let the comm object take it,
        twController_.handleCatch();    // and tell the GUI we're connected.
        doOK();                         // we're gone
	    }
 
	} // gotCatch


/**
    Set the buttons accordingly
**/
private void
setCatchState(boolean buttonsEnabled) {

    buttonPitch_.setEnabled(buttonsEnabled);
    buttonOK_.setEnabled(buttonsEnabled);
    if (buttonsEnabled)
        buttonCatch_.setText("Catch");
    else
        buttonCatch_.setText("CANCEL");
	}


/**
    Handle a click on the 'pitch' button
**/
private void
doPitch() {

    System.out.println("pitch! to " + fieldIP_.getText() + ":" + fieldPort_.getText());
    if ( twComm_.connectTo(fieldIP_.getText(), Integer.parseInt(fieldPort_.getText())) ) {
        twController_.handlePitch();
        doOK();                         // we're gone
	    }
	}


}   // NetDialog
