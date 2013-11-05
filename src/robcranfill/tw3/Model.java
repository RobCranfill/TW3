/**
    Model
    The state of play. A serializable object to be sent betwixt players.

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

package robcranfill.tw3;

import java.awt.Color;
import java.util.*;

import robcranfill.tw3.gui.iGUI;

@SuppressWarnings("serial")
public class Model 
  implements java.io.Serializable {

// Constants
//
public static final int PLAYER_1_PEG  = 0; // must be 0, for player #0
public static final int PLAYER_2_PEG  = 1; // must be 1, for player #1
public static final int OPEN_PEG      = 2;
public static final int FORBIDDEN_PEG = 3;
            // for each of the above values, the colors for them
public static final Color PEG_COLORS[] = 
                {Color.red, Color.blue, new Color(128, 128, 128), Color.lightGray};
public static final int STANDARD_BOARD_SIZE = 24;	// 24 in cannonical game

// public vars

public iGUI GUI_;

public int          boardSize_;    // board size includes home rows

// the playing field:
private int         pegs_[][];
private int         homeRow_[][];   // a slight duplication: the leftmost and topmost ('home') rows

// an array of the links for each location, for fast checking:
//      0=nne; 1=ene; 2=ese; 3=sse; 
//      4=ssw; 5=wsw; 6=wnw; 7=nnw
private boolean         linkArray_[][][];

// and a list of the same links, for fast redraw:
private Vector<Link>          linkList_;

// table for determining obstruction
// * first index is which direction your proposed new link is going;
//   (You might think there should be 8 of these, but we cut the number
//   of links we need to check in half, by always going left to right;
//   so there are only 4 to check) 
// * second index are the 9(!) obstructing links you must check for
//   (the offset of the start point, and the direction to test for)
//
// - this should not be a part of the model (which we send back and forth),
//    but should be redone into a utility method in Utils. Or something.
//
private static int  CUTS_TO_CHECK = 9;
private Obstruct obstructions_[][] = {
     { // theta=0
     new Obstruct(-1,-1, 2),
     new Obstruct(-1,-2, 2),
     new Obstruct( 0,-2, 2),
     new Obstruct( 0,-2, 3),
     new Obstruct( 0,-1, 1),
     new Obstruct( 0,-1, 2),
     new Obstruct( 0,-1, 3),
     new Obstruct( 0,-3, 3),
     new Obstruct(-1, 0, 1)
     },
    { // theta=1
     new Obstruct(-1,-1, 2),
     new Obstruct( 0,-2, 3),
     new Obstruct( 0,-1, 2),
     new Obstruct( 0,-1, 3),
     new Obstruct( 0, 1, 0),
     new Obstruct( 1,-2, 3),
     new Obstruct( 1,-1, 2),
     new Obstruct( 1,-1, 3),
     new Obstruct( 1, 0, 0)
     },
    { // theta=2
     new Obstruct(-1, 1, 1),
     new Obstruct( 0,-1, 3),
     new Obstruct( 0, 1, 0),
     new Obstruct( 0, 1, 1),
     new Obstruct( 0, 2, 0),
     new Obstruct( 1, 0, 3),
     new Obstruct( 1, 1, 0),
     new Obstruct( 1, 1, 1),
     new Obstruct( 1, 2, 0)
     },
    { // theta=3
     new Obstruct(-1, 0, 2),
     new Obstruct(-1, 1, 1),
     new Obstruct(-1, 2, 1),
     new Obstruct( 0, 1, 0),
     new Obstruct( 0, 1, 1),
     new Obstruct( 0, 1, 2),
     new Obstruct( 0, 2, 0),   
     new Obstruct( 0, 2, 1),
     new Obstruct( 0, 3, 0)
     }
    }; 

private String      timeStamp_  = "-";
private String      lastComment = "";

// Set of links for each player, for determining win
// again, is this sent across? should it be?
static private Set<Link>  linkCheck_[];

private int theWinner_ = -1;	// will set it to owner if a winner - Jan05



/**
 * Constructor
 **/
public Model(iGUI theGUI)
	{
	this(theGUI, STANDARD_BOARD_SIZE);
	}


/**
 * Constructor
 **/
public Model(iGUI theGUI, int boardSize)
	{
	registerListener(theGUI);
	boardSize_ = boardSize;
	}


/**
    Start a new game.
**/
public void
newGame() {

    System.out.println("Model.newGame()");

    pegs_ = new int[boardSize_][boardSize_];
    for (int i=0; i<boardSize_; i++)
        for (int j=0; j<boardSize_; j++)
            pegs_[i][j] = OPEN_PEG;

    // the four forbidden corners
    //
    pegs_[0][0] = FORBIDDEN_PEG;
    pegs_[0][boardSize_-1] = FORBIDDEN_PEG;
    pegs_[boardSize_-1][0] = FORBIDDEN_PEG;
    pegs_[boardSize_-1][boardSize_-1] = FORBIDDEN_PEG;
    
    linkArray_ = new boolean[boardSize_][boardSize_][CUTS_TO_CHECK];
    linkList_ = new Vector<Link>(64, 16);      // left, right Points in link

    // Set of links for determining win
    //
    linkCheck_ = new HashSet[2];
    linkCheck_[0] = new HashSet<Link>();
    linkCheck_[1] = new HashSet<Link>();
    homeRow_ = new int[2][boardSize_];  // each player's home row, for win check
    for (int i=1; i<boardSize_-2; i++) {    // don't bother with corners
        homeRow_[0][i] = OPEN_PEG;
        homeRow_[1][i] = OPEN_PEG;
	    }

    theWinner_ = -1;

    touch();

	} // newGame


public void
registerListener(iGUI theGUI) {
    GUI_ = theGUI;
	}

/**
    Set the owner of the peg at c,r.
**/
public void
setPegAt(int c, int r, int player) {
    pegs_[c][r] = player;
    touch();
	}

/**
    Get the owner of the peg at c,r.
**/
public int
getPegOwner(int c, int r) {
    return pegs_[c][r];
	}


/**
    Is c,r open?
**/
public boolean
pegIsOpen(int c, int r) {
    return (pegs_[c][r] == OPEN_PEG);
	}


/**
   Is c,r OK for player?
   Player 0 moves vertically, so left- rightmost columns are unavailable to them.
   Player 1 moves horizontally, so top and bottom rows are unavailable.
**/
public boolean
pegIsAvailableToPlayer(int c, int r, int player) {

    if (player == 0) {
        if (c==0 || c==boardSize_-1)
            return false;
        else
            return true;
	    }
    else {
        if (r==0 || r==boardSize_-1)
            return false;
        else
            return true;
	    }
	}

/**
    Does c,r belong to indicated player?
**/
public boolean
pegBelongsTo(int c, int r, int player) {
    return (pegs_[c][r] == player);
	}


public boolean[]
getLinksForPeg(TWPoint p) {
    return linkArray_[p.x][p.y];
	}

/**
    Can we link from p1 to p2?
    It's assumed player owns both points -
    we're only checking for correct geometry and obstructing links here.
**/
public boolean
canLinkTo(TWPoint p1, TWPoint p2, int player) {

    System.out.println("canLinkTo checking " + p1 + " -> " + p2);

    // First, check for one-over-two-across geometry
    //
    int dx = Math.abs(p1.x - p2.x);
    int dy = Math.abs(p1.y - p2.y);
    if ( ! ((dx==1 && dy==2) || (dx==2 && dy==1)) ) {
        System.out.println("canLinkTo: false, wrong geo");
        return false;
	    }

    // Second, check for obstructing links
    //
    int thisDir = Utils.directionFromAtoB(p1, p2);
    System.out.println("canLinkTo: dir is " + thisDir);
    
    // again, don't add it if one already exists
    if (linkArray_[p1.x][p1.y][thisDir]) {
        System.out.println("canLinkTo: false, exists");
        return false;
	    }
    
    // check the nine possible blocking links
    for (int i=0; i<CUTS_TO_CHECK; i++) {

        Obstruct obs = obstructions_[thisDir][i];
//        System.out.println("canLinkTo checking obs " + i + ": " + obs);

        int checkX = p1.x + obs.dx;
        int checkY = p1.y + obs.dy;
        if (checkX>=0 && checkX<boardSize_ && checkY>=0 && checkY<boardSize_) {
            if (linkArray_[checkX][checkY][obs.theta]) {
                System.out.println("canLinkTo obstructed by " + obs);
                return false;
	            }
	        }
	    }

    System.out.println("canLinkTo: YES");
    return true;
	} // canLinkTo
	

/**
    Add the link to the lists.
    There are two lists - could they be consolidated? probably...
    One is for redrawing the board; the other for checking for win.
**/
public void
setLink(TWPoint startPoint, TWPoint endPoint, int direction, int owner) {

    // Not if it already exists, tho.
    if (linkArray_[startPoint.x][startPoint.y][direction])
        return;
    linkArray_[startPoint.x][startPoint.y][direction] = true;
    Link newLink = new Link(owner, startPoint, endPoint);
    linkList_.addElement(newLink);
    System.out.println("setLink added " + startPoint + ", " + endPoint + " @ " + direction);
    touch();

    // Add to the win-check list, and check it
    //
    linkCheck_[owner].add(newLink);
    System.out.println("Link set is now " + linkCheck_[owner]);

    boolean horizontal = (owner==PLAYER_2_PEG)?true:false;
    if (horizontal) {
        if (startPoint.x == 0) {
            int index = startPoint.y;
            System.out.println("setting home row " + index + " to " + owner);
            homeRow_[owner][index] = owner;
	        }
	    }
    else {
        boolean isHomeRow = false;
        int index = 0;
        if (startPoint.y == 0) {
            isHomeRow = true;
            index = startPoint.x;
        	}
        else 
        if (endPoint.y == 0) {
            isHomeRow = true;
            index = endPoint.x;
        	}
        if (isHomeRow) {
            System.out.println("setting home row " + index + " to " + owner);
            homeRow_[owner][index] = owner;
	        }
	    }

    boolean winner = Utils.checkForWin(linkCheck_[owner], homeRow_[owner], horizontal, boardSize_);
    if (winner) {
        
        // Jan05
        
        // GUI_.playerWins(owner);

        System.out.println("WINNER, BUT WE'RE HOLDING....");
        theWinner_ = owner;

	    }
 
	} // setLink


/**
 * Has there been a winner? Is adjusted by setLink().
 * 
 * @return 	-1 if no winner, or player index of winner.
 */
public int
getWinner() {
    return theWinner_;
	}

/**
    Should this just be a public var?
**/
public Vector<Link>
getLinks() {
    return linkList_;
	}

/**
    Update the model's timestamp
    The timestamp is just used for the 'toString' of a model
**/
private void
touch() {
    timeStamp_ = new java.util.Date().toString();
	}

/**
    Like I said....
**/
public String
toString() {
    return timeStamp_;
	}

public void
makeComment(String comment) {
    this.lastComment = comment;
	}

public int
getBoardSize() {
    return boardSize_;
	}

}   // Model

