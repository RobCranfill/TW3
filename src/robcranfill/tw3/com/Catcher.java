/**
    Catcher
    Runnable object for doing network 'catch'.

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
// import java.io.ObjectOutputStream;

import robcranfill.tw3.gui.NetDialog;


public class Catcher
  implements java.lang.Runnable {

private NetDialog  dialog_;
private int          port_;
private boolean      done_;


public Catcher(NetDialog dialog, int port) {
    dialog_ = dialog;
    port_ = port;
	}


/**
    Do a whole bunch of little 'accept's, with a short timeout.
    This is so we can stay responsive to the user's 'abort' request.
**/
public void
run() {

    System.out.println("Catcher listening on port " + port_);
    Socket sock = null;
    done_ = false;
    try {
        ServerSocket ss = new ServerSocket(port_);
        ss.setSoTimeout(500);      // milliseconds of timeout
        for (int i=60; i>0; i--) {
            if (done_) {
                System.out.println("Catcher is DONE!");
                break;
            	}
            if (i%10 == 0)
                System.out.println("'Catch' countdown... " + i/10 + "...");
            try {
                sock = ss.accept();
                break;
	            }

            // Timeout falls to here:
            //
            catch (java.io.InterruptedIOException iioe) {
                continue;
	            }
	        }
	    }
    catch (Exception e) {
        e.printStackTrace();
    	}

    dialog_.gotCatch(sock);  // null denotes failure
	}


public void
stopCatch() {
    done_ = true;
	}

}   // Catcher
