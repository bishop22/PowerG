import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;

import javax.swing.JLabel;

public class CityConnector {
	int x, y;	// grid layout placement of the Connector label
	City city1;
	City city2;
	int cost;
	Container CP;
	private GridBagConstraints c;
	JLabel connLabel;

	public CityConnector(int X, int Y, City City1, City City2, int Cost, Container CP) {
		city1 = City1;
		city2 = City2;
		cost = Cost;
		x = X;
		y = Y;
		c = new GridBagConstraints();
		connLabel = new JLabel(Cost + "");
		connLabel.setToolTipText(city1.getName() + " to " + city2.getName() + " connection");
		c.gridx = x;
		c.gridy = y;
		connLabel.setForeground(Color.pink);	// Use purple font for the Connections
		CP.add(connLabel, c);
		
		City1.addConnection(City2, Cost);
		City2.addConnection(City1, Cost);
	}
}
