/**
    Utils

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

import java.awt.Point;
import java.util.*;


public class Utils {

private static int verbosity_ = 0;  // for checkForWin: 0=no output; 1=some; 2=lots

/**
    Given two points in a valid link - a 'from' and a 'to' - return the "theta" direction
    from a to b. Where 'direction' is one of eight possible values:

      0=nne; 1=ene; 2=ese; 3=sse; 
      4=ssw; 5=wsw; 6=wnw; 7=nnw
      
  or to put it another way:
  
         7 0
       6     1
       5     2
         4 3

**/
public static int
directionFromAtoB(TWPoint a, TWPoint b) {

    int dx = b.x - a.x;
    int dy = b.y - a.y;
    
    int direction = -1;

    if (dx==1  && dy==-2)
        direction = 0;
    else
    if (dx==2  && dy==-1)
        direction = 1;
    else
    if (dx==2  && dy==1)
        direction = 2;
    else
    if (dx==1  && dy==2)
        direction = 3;
    else
    if (dx==-1 && dy==2)
        direction = 4;
    else
    if (dx==-2 && dy==1)
        direction = 5;
    else
    if (dx==-2 && dy==-1)
        direction = 6;
    else
    if (dx==-1 && dy==-2)
        direction = 7;
 
    if (direction == -1)
        System.out.println("error! bogus direction!");
 
    return direction;
	} // directionFromAtoB


/**
    linkSet is list of non-directed links (p1/p2 order doesn't matter);
    homeRow is vector of points in home row;
    horizontal is flag for 'we are checking for horizontal win', as opposed to vertical;
    boardSize is size of board.
    
    return true iff won.

    ideas not implemented (and maybe they don't need to be, if this thing's fast enuf already):
        - if spreading leads to a home row peg, remove it from home row list.
        - it'd be nice to know what the winning path is. I don't see how to find it.
**/
public static boolean
checkForWin(Set<Link> linkSet, /* fark Vector homeRow */ int homeRow[], boolean horizontal, int boardSize) {

    if (verbosity_>0) System.out.println("checkForWin: starting; " + (horizontal?"horiz":"vert"));
    
    // We only need to check any given point once. Either its links win, or they don't.
    //
    Set<Point> checkedPoints = new HashSet<Point>();

    int track = 1; // for info output

    // For each home row peg, see if we can walk the links to the other side.
    //
/* fark
    Enumeration homeRowPegs = homeRow.elements();
    while (homeRowPegs.hasMoreElements()) {
*/
    for (int i=1;i<boardSize-2;i++) {
        if (homeRow[i] == Model.OPEN_PEG)
            continue;

        if (verbosity_>1) System.out.println(" *** Begin track " + track + " at " + i);
        
// fark        TWPoint homeRowPeg = (TWPoint)homeRowPegs.nextElement();
        TWPoint homeRowPeg = null;
        if (horizontal)
            homeRowPeg = new TWPoint(0, i);
        else
            homeRowPeg = new TWPoint(i, 0);

        if (verbosity_>1) System.out.println("checkForWin: home row peg " + homeRowPeg);

        // A growing list of points to check; do any of *them* get us there?
        // Start with just the row peg.
        //
        Set<TWPoint> pointsBeingChecked = new HashSet<TWPoint>();
        pointsBeingChecked.add(homeRowPeg);

        int ply = 1; // more info output
        
        // Repeat until we have no more points to check (or we win)...
        //
        while ( !pointsBeingChecked.isEmpty()) {
            if (verbosity_>1) System.out.println("\ncheckForWin: ply " + track + "." + ply + 
                                                 "; pBC is now " + pointsBeingChecked);

            TWPoint toCheck = (TWPoint)pointsBeingChecked.iterator().next();
            if (verbosity_>1) System.out.println("checkForWin: next is " + toCheck);

            if (inHomeRow2(toCheck, horizontal, boardSize)) {
                if (verbosity_>0) System.out.println("\n **** WIN! on track " + track + "\n");
                return true;            // win!
            	}

            // This one didn't get us there. Remove it from to-check list, add to 'checked' list,
            // and then add all its (unchecked) linked-to points to the to-check list.
            //
            pointsBeingChecked.remove(toCheck);
            checkedPoints.add(toCheck);
            if (verbosity_>1) System.out.println("checkForWin: moved " + toCheck + " to checked");

            // Get the set of points this one links to.
            //
            Set<TWPoint> linkedTo = getLinkedTo(linkSet, toCheck);            
            while ( ! linkedTo.isEmpty() ) {

                // If the new point has already been checked, skip it;
                // otherwise, add it to the list to be checked.
                //
                TWPoint newCheck = (TWPoint)linkedTo.iterator().next();
                if ( ! checkedPoints.contains(newCheck) ) {

                    pointsBeingChecked.add(newCheck);
                    if (verbosity_>1) System.out.println("checkForWin: added " + newCheck + " to pBC");

                    // Unimplemented: to speed things up, could remove link from link list;
                    // it'll never be pertinent again

                	}
                linkedTo.remove(newCheck);
            	}
            ply++;
        	}   // no more leading from same home peg

	    if (verbosity_>1) System.out.println("\n *** Track " + track + " failed.\n");
	    track++;
    }   // end check of all home row pegs
    
    return false;
	}   // checkForWin


/**
    Return the set of all points linked to the given point.
    We don't care that at least one of these links may be 'back' to where we came,
    (that is, a link that has already been dealt with; the one we traversed to get to this point)
    - the user of this method will handle that.
**/
public static Set<TWPoint>
getLinkedTo(Set<Link> links, TWPoint p) {

    Set<TWPoint> result = new HashSet<TWPoint>();

    Object linkArray[] = links.toArray();   // trying to cast here causes death. hmm.
    int s = links.size();

    for (int i=0; i<s; i++) {
        Link thisLink = (Link)linkArray[i];
        if (thisLink.left.equals(p)) {
            result.add(thisLink.right);
        	}
        else
        if (thisLink.right.equals(p)) {
            result.add(thisLink.left);
	        }
	    }

    if (verbosity_>1) System.out.println("getLinkedTo: links for point " + p + ": " + result);    
    return result;
	} // getLinkedTo


/**
    Are we there yet?
**/
public static boolean
inHomeRow2(TWPoint p, boolean checkHorizontal, int boardSize) {
    
    if (checkHorizontal)
        return p.x==(boardSize-1) ? true : false;
    else
        return p.y==(boardSize-1) ? true : false;
	}


}   // Utils
