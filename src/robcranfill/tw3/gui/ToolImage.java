/**
	ToolImage
	
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
public class ToolImage
     extends JComponent {

private Image image_;
private Image imageA_;
private Image imageB_;
private Image imageC_;
private Image imageD_;

private int currentImage_;


public void
setImageMode(int mode) {
    currentImage_ = mode;
	}


public
ToolImage() {

    // This loads images asynchronously, and faster than with getImage by itself.
    // (as per Java Dev. Almanac, p16)
    //
    image_  = loadImage("resources/legend2.png", "");
    
    imageA_ = loadImage("resources/legend2a.png", "");
    imageB_ = loadImage("resources/legend2b.png", "");
    imageC_ = loadImage("resources/legend2c.png", "");
    imageD_ = loadImage("resources/legend2d.png", "");

    if (image_ != null)
    	{
    	this.setBounds(0, 0, image_.getWidth(null), image_.getHeight(null));
		}
	}

/**
 * Nicely wrapped to handle mysterious errors.
 * @param path
 * @param description
 * @return
 */
private Image loadImage(String path, String description)
	{
	java.net.URL imgURL = getClass().getResource(path);
	ImageIcon ii = null;
	if (imgURL != null)
		{
		ii = new ImageIcon(imgURL, description);
		}
	else
		{
		System.err.println("Couldn't find file: " + path);
		return null;
		}
	int status = ii.getImageLoadStatus();
	if (status != java.awt.MediaTracker.COMPLETE)
		{
		System.out.printf("loadImage %s: getImageLoadStatus failed with java.awt.MediaTracker.%d\n", path, status);
		return null;
		}
	return ii.getImage();
	}


public void
paint2(Graphics g) {

    g.drawImage(image_, 0, 0, this);
    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

public void
paint(Graphics g) {

    Image image = image_;
    switch (currentImage_) {
	    case TW3App.STATE_PLACE_PEG:
	        image = imageB_;
	        break;
	    case TW3App.STATE_PLACE_LINK_START:
		    image = imageC_;
		    break;
		case TW3App.STATE_PLACE_LINK_END:
			image = imageD_;
			break;
//	    case TW3App.STATE_NET_START:
//		    image = imageA_;
//		    break;
	    case TW3App.STATE_NET_NOT_MY_TURN:
			image = imageA_;
			break;
    	}
    g.drawImage(image, 0, 0, this);
    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}


} // ToolImage
   