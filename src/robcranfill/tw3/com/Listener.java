/**
    Listener
    Given a socket to handle, 
    wait for each serialized object to come down it,
    and hand it to our subscriber (the comm object).

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

package robcranfill.tw3.com;

import java.net.*;
import java.io.ObjectInputStream;

public class Listener
  implements Runnable {

private Socket              theSocket_;
private iListenerHandler  ourListnerHandler_;


/**
    Constructor
**/
public Listener(Socket sock, iListenerHandler ilh) {
    theSocket_ = sock;
    ourListnerHandler_ = ilh;
	}


/**
    The whole point of the Runnable interface.
**/
public void
run() {

    // Create the object input stream to read from.
    //
    ObjectInputStream ois = null;
    try {
        ois = new ObjectInputStream(theSocket_.getInputStream());
	    }
    catch (Exception e) {
        e.printStackTrace();
        System.exit(1);
	    }

    // Until the stream dries up, read from it, 
    // and pass the object to our handler.
    //
    boolean done = false;
    System.out.println("Listener object listener running....");
    while ( !done ) {
        try {
            Object o = ois.readObject();
//            System.out.println("Listener got " + o);
            ourListnerHandler_.handleObject(o);
        	}
        catch (Exception e) {   // could also be InterruptedException
            // if (e.equals()
            // ???
            done = true;
            System.out.println("Listener object listener done.");
	        }
	    } // while
	}   // run


}   // Listener
