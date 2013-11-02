/**
 * This class isn't used ???
 * 
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

import javax.swing.*;

import java.awt.Dimension;
import java.awt.Toolkit;

@SuppressWarnings("serial")
public class CatchDialog 
      extends JDialog {

public
CatchDialog() {
    
//    super("Waiting for connect", true);  // no parent, modal
    this.setSize(250, 150);
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    int w = this.getSize().width;
    int h = this.getSize().height;
    int x = (dim.width - w) / 2;
    int y = (dim.height - h) / 3;
    this.setBounds(x, y, w, h);
    this.setVisible(true);        // we were created inviz; show ourselves.
	} // CatchDialog constructor

} // CatchDialog
      
