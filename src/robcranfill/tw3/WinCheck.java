/**
    WinCheck
    Test of an algorithm to detect winningness.
    Not used in the game.

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

import java.util.*;
import java.awt.Point;


public class WinCheck {

//private static boolean doingHorizontal_;
private static Vector<Point>  homeRow_;

/*
    'links_' is a set of Link objects, 1 per real link.
    This will be constructed as links are formed during play.
    The 'from' and 'to' (now called 'p1' and 'p2' to emphasize this) are meaningless.
*/
private static Set<Link>     links_;

private static int     verbosity_ = 0;



/**
    Tester tester
**/
public static void
main(String[] args) {
    
    System.out.println("Test case 1a");
    verbosity_ = 2;

    homeRow_ = new Vector<Point>();
    homeRow_.add(new Point(0,1));

    links_ = new HashSet<Link>();
    links_.add(new Link(Model.PLAYER_1_PEG, new TWPoint(2,2), new TWPoint(0,1)));
    links_.add(new Link(Model.PLAYER_1_PEG, new TWPoint(2,2), new TWPoint(4,1)));
    links_.add(new Link(Model.PLAYER_1_PEG, new TWPoint(2,2), new TWPoint(4,3)));
//    links_.add(new Link(new TWPoint(4,3), new TWPoint(5,4)));

    homeRow_.add(new Point(0,3));
    links_.add(new Link(Model.PLAYER_1_PEG, new TWPoint(0,3), new TWPoint(1,1)));
    links_.add(new Link(Model.PLAYER_1_PEG, new TWPoint(0,3), new TWPoint(2,4)));
    links_.add(new Link(Model.PLAYER_1_PEG, new TWPoint(3,2), new TWPoint(1,1)));
    links_.add(new Link(Model.PLAYER_1_PEG, new TWPoint(3,2), new TWPoint(2,4)));
    links_.add(new Link(Model.PLAYER_1_PEG, new TWPoint(5,1), new TWPoint(3,2)));


    boolean result = WinCheck.checkForWin(true);
    System.out.println("Test case 1a result: " + result);

    pause("\nDone. Press <enter>");
}


/**
    horizontal is flag for we are checking for horizontal win
    
    return true iff won

    ideas not implemented:
        - if spreading leads to a home row peg, remove it from home row list.
        
**/
public static boolean
checkForWin(boolean horizontal) {

//    doingHorizontal_ = horizontal;
    
    Set<Point> checkedPoints = new HashSet<Point>();
    Enumeration<Point> homeRowPegs = getHomeRow(horizontal);
    
    if (verbosity_>0) System.out.println("checkForWin: starting");

    int track = 1; // ditto
    while (homeRowPegs.hasMoreElements()) {
        
        if (verbosity_>1) System.out.println(" *** Begin track " + track);
        
        Point homeRowPeg = (Point)homeRowPegs.nextElement();
        if (verbosity_>1) System.out.println("checkForWin: home row peg " + homeRowPeg);

        // create a spreading list of points to check; do they get us there?
        Set<Point> pointsBeingChecked = new HashSet<Point>();
        pointsBeingChecked.add(homeRowPeg);

        int ply  = 1; // just for fun
        
        while ( ! pointsBeingChecked.isEmpty()) {
            if (verbosity_>1) System.out.println("\ncheckForWin: ply " + track + "." + ply + "; pBC is now " + pointsBeingChecked);

            Point toCheck = (Point)pointsBeingChecked.iterator().next();
            if (verbosity_>1) System.out.println("checkForWin: next is " + toCheck);

            if (inHomeRow2(toCheck, horizontal)) {
                if (verbosity_>0) System.out.println("\n **** WIN on track " + track + "\n");
                return true;            // win!
            }

            pointsBeingChecked.remove(toCheck);
            checkedPoints.add(toCheck);

            Set<Point> linkedTo = getLinkedTo(toCheck);            
            while ( ! linkedTo.isEmpty() ) {
                
                // if the new point has already been checked, skip it;
                // otherwise, add it to the list to be checked.
                //
                Point newCheck = (Point)linkedTo.iterator().next();
                if ( ! checkedPoints.contains(newCheck) ) {
                    pointsBeingChecked.add(newCheck);
                    
                    // to speed things up, could remove link from link list; 
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
    our user will take care of this.
**/
public static Set<Point>
getLinkedTo(Point p) {

    Set<Point> result = new HashSet<Point>();

    Object linkArray[] = links_.toArray();
    int s = links_.size();

    for (int i=0; i<s; i++) {			// Jan05 - fixed from 'p1' & 'p2' (which made more sense!)
        Link thisLink = (Link)linkArray[i];
        if (thisLink.left.equals(p)) {
            result.add(thisLink.right);
        }
        else
        if (thisLink.right.equals(p)) {
            result.add(thisLink.left);
        }
    }

    if (verbosity_>1) System.out.println("getLinkedTo: point " + p + " = " + result);    
    return result;
}


/**
    needs to be made more general
**/
public static boolean
inHomeRow2(Point p, boolean checkHorizontal) {
    
    if (checkHorizontal)
        return p.x==5 ? true : false;
    else
        return p.y==5 ? true : false;
}


/**
    get a vector of the Points in the indicated home row
**/
public static Enumeration<Point>
getHomeRow(boolean horizontal) {

    // cheat
    return homeRow_.elements();
}


public static void
pause(String message) {
    try {
        System.out.println(message);
        System.in.read();
    }
    catch (Exception e) {}
}

}


