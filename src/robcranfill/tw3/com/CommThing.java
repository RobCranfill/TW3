/**
    CommThing
    Communications object.

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
import java.io.ObjectOutputStream;

import javax.swing.JOptionPane;


public class CommThing 
  implements iComm, Runnable {

private iObjHandler       handler_;
private Thread              listenerThread_;
private ObjectOutputStream  oos_;
private boolean             connected_ = false;

/**
    Constructor
**/
public CommThing(iObjHandler ilh) {
    
    handler_ = ilh;
	}


/**
    We really are threaded only to decouple from the V/C
**/
public void
run() {

    System.out.println("Comm object thread running.");
    while (true) {
        try {
            Thread.sleep(1000);
        	}
        catch (Exception e) {
	        }
	    }
	}


/**
    We've started a 'server' session.
    The comm dialog will have started the listener, 
    and if/when it gets a connection, then this method is fired.
**/
public void
processListen(Socket sock) {

    System.out.println("CommThing: processListen OK");
    try {
        oos_ = new ObjectOutputStream(sock.getOutputStream());
        startListener(sock);
    }
    catch (Exception e) {
        e.printStackTrace();
	    }
	} // processListen


/**
    Start a 'client' session.
**/
public boolean
connectTo(String host, int port) {

    System.out.println("connectTo: " + host + "@" + port + "....");
    try {
        Socket sock = new Socket(host, port);
        oos_ = new ObjectOutputStream(sock.getOutputStream());
        startListener(sock);
        System.out.println("connected!");
    	}
    catch (Exception e) {
        System.out.println("failed to connect!");
        JOptionPane.showMessageDialog(null,
                                      "Couldn't connect to " + host + ":" + port,
                                      "Connection attempt failed", JOptionPane.INFORMATION_MESSAGE);
        return false;
    	}
    return true;
	} // connectTo

/**
    Start the listener object to handle incoming objects
    in a non-blocking fashion.
**/
private void
startListener(Socket sock) {

    listenerThread_ = new Thread(new ObjHandler(sock, handler_));
    listenerThread_.start();
    
    connected_ = true;
	} // startListener


/**
    Send a Java object over the pipe.
**/
public void
sendObject(Object o) {
    
    System.out.println("sendObject: sending " + o);
    try {
        oos_.writeObject(o);
        oos_.flush();
    	}
    catch (Exception e) {
        e.printStackTrace();
	    }
	} // sendObject


/**
    Shut 'er down.
**/
public void
shutdown() {

    try {
        oos_.close();
    	}
    catch (Exception e) {
    	}
    if ( listenerThread_ != null) {
        listenerThread_.interrupt();
        listenerThread_ = null;
    	}
    connected_ = false;
	} // shutdown


/**
    accessor
**/
public boolean
isConnected() {
    return connected_;
	}

/**
    Get ready to start over.
**/
public void
reset() {
    
    shutdown();
    System.out.println("Comm object reset.");
	}


}   // CommThing
