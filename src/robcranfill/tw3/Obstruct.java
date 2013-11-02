/**
 	Obstruct
    An item of an obstruction list
    delta x, delta y, and an 'angle'

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

@SuppressWarnings("serial")
public class Obstruct
  implements java.io.Serializable {

int dx;
int dy;
int theta;

public Obstruct(int x, int y, int t) {
    dx = x;
    dy = y;
    theta = t;
}

public String
toString() {
    return "(" + dx + "," + dy + " @ " + theta + ")";
}

}
