/**
	SplashImage
	Our vanity on display.
	
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

import javax.swing.*;


@SuppressWarnings("serial")
public class SplashImage
     extends JComponent {

private Image image_;

public SplashImage() {
    
    // this load it asynchronously, and faster than with getImage by itself....
    // (as per Java Dev. Almanac, p16)
    //
    image_ = new ImageIcon("resources/splash.gif").getImage();
	}

public void
paint(Graphics g) {

    g.drawImage(image_, 0, 0, this);
    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

} // SplashImage
   