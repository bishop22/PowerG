import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GridBagConstraints;

import javax.swing.JLabel;

public class CityConnector {
//	int x, y;	// grid layout placement of the Connector label
	City city1;
	City city2;
	int cost;
	Container CP;
	private GridBagConstraints c;
	JLabel connLabel;

//	public CityConnector(int X, int Y, City City1, City City2, int Cost, Container CP) {
	public CityConnector(City City1, City City2, int Cost, Container CP) {
		city1 = City1;
		city2 = City2;
		cost = Cost;
//		x = X;			// We will calculate the GridBag layout location instead
//		y = Y;
		c = new GridBagConstraints();
		connLabel = new JLabel(Cost + "");
		connLabel.setToolTipText(city1.getName() + " to " + city2.getName() + " connection");
		c.gridx = (city1.getActualX() + city2.getActualX() + 1) / 2;
		c.gridy = (city1.getActualY() + city2.getActualY() + 1) / 2;
		connLabel.setForeground(Color.pink);	// Use pink font for the Connections
		CP.add(connLabel, c);
		
		City1.addConnection(City2, Cost);
		City2.addConnection(City1, Cost);
	}
	
    protected void drawConnection(Graphics g) {
    	int p1 = city1.getX();
    	int p2 = city1.getY();
    	int p3 = city2.getX();
    	int p4 = city2.getY();
    	
    	if (p3 >= p1 + 2*City.BTN_WIDTH) {
    		if (p4 >= p2) {
    			// Second city is lower right, add width to p1 and height to p2
    			p1 += 3*City.BTN_WIDTH;
    			p2 += City.BTN_HEIGHT;
    		} else {
    			// Second city is upper right, add width to p1 and height to p4
    			p1 += 3*City.BTN_WIDTH;
    			p4 += City.BTN_HEIGHT;
    		}
    	} else if (p3 <= p1 - 2*City.BTN_WIDTH) {
    		if (p4 >= p2) {
    			// Second city is lower left, add height to p2 and width to p3
    			p3 += 3*City.BTN_WIDTH;
    			p2 += City.BTN_HEIGHT;
    		} else {
    			// Second city is upper left, do height to p4 and width to p3
    			p3 += 3*City.BTN_WIDTH;
    			p4 += City.BTN_HEIGHT;
    		}
    	} else {
    		if (p4 >= p2) {
    			// Second city overlaps horizontally below, add 1/2 width to p1 and p3 and height to p2
    			p1 += 1.5*City.BTN_WIDTH;
    			p3 += 1.5*City.BTN_WIDTH;
    			p2 += City.BTN_HEIGHT;
    		} else {
    			// Second city overlaps horizontally above, add 1/2 width to p1 and p3 and height to p4
    			p1 += 1.5*City.BTN_WIDTH;
    			p3 += 1.5*City.BTN_WIDTH;
    			p4 += City.BTN_HEIGHT;
    		}
    	}
        g.drawLine(p1, p2, p3, p4);
    }
}
