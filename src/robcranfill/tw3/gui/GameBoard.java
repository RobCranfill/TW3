/**
 	GameBoard
    Draw a representation of the model we are constructed on.

    (c)1999 rob cranfill; all rights reserved
    @author robcranfill@comcast.net
 **/
package robcranfill.tw3.gui;

import java.awt.*;

import javax.swing.border.EtchedBorder;

import robcranfill.tw3.Link;
import robcranfill.tw3.Model;
import robcranfill.tw3.TWPoint;

@SuppressWarnings("serial")
public class GameBoard extends javax.swing.JComponent
	{

	// The model we need to reflect:
private Model	ourModel_;

private int		FIELD_BORDER	= 8;	// gap between edge and outermost holes
private int		CELL_SIZE		= 16;	// distance 'twixt holes

/**
 * Zero-arg constructor for IDE use.
 */
public GameBoard()
	{
	this(new Model(null, Model.STANDARD_BOARD_SIZE));
	}

/**
 * 
 * @param theModel
 */
public GameBoard(Model theModel_)
	{
	ourModel_ = theModel_;
	this.setBorder(new EtchedBorder());
	}

/**
 * This means a standard 24x24 board is 400 pixels square, exactly.
 * 
 * If this method doesn't exist, nothing gets painted. Weird.
 **/
public Dimension getPreferredSize()
	{
	return new Dimension(
		FIELD_BORDER + ourModel_.getBoardSize() * CELL_SIZE, 
		FIELD_BORDER + ourModel_.getBoardSize() * CELL_SIZE);
	}

public Dimension getMinimumSize()
	{
	return new Dimension(
		FIELD_BORDER + ourModel_.getBoardSize() * CELL_SIZE,
		FIELD_BORDER + ourModel_.getBoardSize() * CELL_SIZE);
	}

public Dimension getSize()
	{
	return new Dimension(
		FIELD_BORDER + ourModel_.getBoardSize() * CELL_SIZE, 
		FIELD_BORDER + ourModel_.getBoardSize() * CELL_SIZE);
	}

public void setModel(Model model)
	{
	ourModel_ = model;
	this.repaint();
	}

/**
 * The method that is called by Java in order to repaint the board's display.
 * Use the state of the model that we're based on.
 **/
public void paint(Graphics gr)
	{
	System.out.println("model is " + ourModel_);

	// How wide the world is
	int modelSize = ourModel_.getBoardSize();

	// The home lines
	//
	int small = FIELD_BORDER + CELL_SIZE / 2;
	int big = (CELL_SIZE * modelSize) - small;

	gr.setColor(Color.red);
	gr.drawLine(small + 1, small, big - 1, small); // top line
	gr.drawLine(small + 1, big, big - 1, big); // bottom
	gr.setColor(Color.blue);
	gr.drawLine(small, small + 1, small, big - 1); // left
	gr.drawLine(big, small + 1, big, big - 1); // right

	// The links
	// Draw the links first, and the pegs second - looks better that way.
	//
	java.util.List<Link> links = ourModel_.getLinks();
	for (Link link : links)
		{
		drawLink(gr, link);
		}

	// Draw the holes and pegs.
	//
	for (int i = 0; i < modelSize; i++)
		{
		for (int j = 0; j < modelSize; j++)
			{
			drawPegAt(gr, i, j);
			}
		}

	} // paint

/**
 * Draw a link at the given location. These are in game coordinates, not screen coords.
 **/
private void drawLink(Graphics g, Link link)
	{

	// map to drawing coords
	//
	int x1 = FIELD_BORDER + link.left.x * CELL_SIZE;
	int y1 = FIELD_BORDER + link.left.y * CELL_SIZE;
	int x2 = FIELD_BORDER + link.right.x * CELL_SIZE;
	int y2 = FIELD_BORDER + link.right.y * CELL_SIZE;

	Graphics2D g2d = (Graphics2D) g;
	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	// set the color to that of the link's owner
	//
	Color theColor = Model.PEG_COLORS[link.owner];
	g2d.setColor(theColor);

	g2d.setStroke(new BasicStroke(3.0f));
	g.drawLine(x1, y1, x2, y2);

	g2d.setColor(Color.white);
	g2d.setStroke(new BasicStroke(1.0f));
	g.drawLine(x1, y1, x2, y2);

	} // drawLink


/**
 * Draw a peg (or an empty 'hole') at the given location. 
 * Could be done more prettily - icon? bitmap? These are in game coordinates, not screen coords.
 **/
private void drawPegAt(Graphics g, int r, int c)
	{
	int x = FIELD_BORDER + r * CELL_SIZE;
	int y = FIELD_BORDER + c * CELL_SIZE;

	int pegOwner = ourModel_.getPegOwner(r, c);

	Graphics2D g2d = (Graphics2D) g;
	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	g2d.setColor(Model.PEG_COLORS[pegOwner]);

	if (pegOwner == Model.OPEN_PEG)
		{ // a little x
		// g2d.drawLine(x, y, x+1, y+1);
		// g2d.drawLine(x+1, y, x, y+1);
		g2d.fillOval(x, y, 3, 3);
		}
	else
		{
		g2d.fillOval(x - 2, y - 2, 5, 5);
		}

	} // drawPegAt


/**
 * Given an x,y coordinate from a mouseclick, what row & col most closely correspond?
 **/
public TWPoint mapClickToRowAndCol(TWPoint clickPoint)
	{

	int x1 = clickPoint.x;
	int y1 = clickPoint.y;
	// System.out.println("cp is " + x1 + "," + y1);

	// an offset of 0 works well here.... but might not be quite right.
	int x2 = (x1 - 0) / CELL_SIZE;
	int y2 = (y1 - 0) / CELL_SIZE;

	TWPoint p = new TWPoint(x2, y2);
	return p;
	} // mapClickToRowAndCol

} // GameBoard

