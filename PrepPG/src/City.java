import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class City {

	private GridBagConstraints c;
	private String cityName;
	private JButton[] gridLoc;
	private JTextField nameLoc;
	private ArrayList<City> connections;
	private ArrayList<Integer> connectionCosts;
	private MainWindow gameControl;
	private Container pane;
	
	// Create a generic ActionListener for now; later it will have to figure out what color to label the space
	ActionListener listener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
        	JButton curButton = (JButton)e.getSource();
            if (e.getSource() instanceof JButton) {
            	if (gameControl.getPhase() == 4) {
        			boolean cityConnected = false;
    				int lowestConnectionCost = 999;
            		Player curPlayer = gameControl.getTurnPlayer();
            		int cityCost = Integer.parseInt(((AbstractButton) e.getSource()).getText());

            		if (!alreadyInCity(curPlayer)) {
	            		// Check to see if the player can afford the city (and the connection(s)) 
	            		// Of course if the city count is zero, there is no connection cost for the first city
	            		if (curPlayer.getCityCount() > 0) {
	            			// For now, purchase must be a direct connection to an existing city - save the hard problem for later
	            			// For each city connected to this city, check if the current player has that city
	            			for (int i = 0; i < connections.size(); i++) {
	            				City connectedCity = connections.get(i);
	            				for (int j = 0; j < curPlayer.getMyCities().size(); j++) {
	            					if (connectedCity.equals(curPlayer.getMyCities().get(j))) {
	            						cityConnected = true;
	            						if (connectionCosts.get(i) < lowestConnectionCost) { 
	            							lowestConnectionCost = connectionCosts.get(i);
	            						}
	            					}
	            				}
	            			}
	            			if (cityConnected) {
	            				cityCost += lowestConnectionCost;
	            			}
	            		}
	            		if ((curPlayer.getCityCount() == 0) | cityConnected) {
		            		if (curPlayer.getElectroCount() >= cityCost) {
	//	            			((AbstractButton) e.getSource()).setText(curPlayer.getColor());
		            			((AbstractButton) e.getSource()).setEnabled(false);
		            			((AbstractButton) e.getSource()).setBackground(curPlayer.getPaintColor());
		            			((AbstractButton) e.getSource()).setOpaque(true);
		            			purchaseCity(curPlayer, cityCost);
		            		} else {
		            			// Dialog box that can't afford the city and connection
		                		JOptionPane.showMessageDialog(pane,
		                			    "You can't afford city plus connection cost of " + cityCost + ".");
		            		}
	            		} else {
	            			// Dialog box that the city is not connected to player's network
	                		JOptionPane.showMessageDialog(pane,
	                			    "Selected city is not connected to your network (note that building through cities is not yet implemented).");
	            		}
	        		} else {
	        			// Can't build twice in the same city
	            		JOptionPane.showMessageDialog(pane,
	            			    "You can't build in a city that you are already in");
	            		return;
	        		}
            	} else {
            		// Pop up a dialog box that they can only select cities during Phase 4 - Building
            		JOptionPane.showMessageDialog(pane,
            			    "You may only select cities during Phase 4 - Building.");
            	}
            }
        }
    };
    
    // Constructor for a new city
	public City(String CityName, int MapX, int MapY, Container Pane, MainWindow GameControl) {
		// Set up a GridBagConstraints object for the city components to use to be plotted correctly
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		
		// Set the passed member variables
		cityName = CityName;
		pane = Pane;
		gameControl = GameControl;
		
		connections = new ArrayList<City>();
		connectionCosts = new ArrayList<Integer>();
		
		// Create the button array and each of the buttons
		gridLoc = new JButton[3];
		gridLoc[0] = new JButton("10");
		gridLoc[1] = new JButton("15");
		gridLoc[2] = new JButton("20");
		
		// Disable the 15 and 20 buttons while in Step 1, enable them later
		gridLoc[1].setEnabled(false);
		gridLoc[2].setEnabled(false);
		
		// Set the preferred button sizes
		for (int i = 0; i < 3; i++) {
//			gridLoc[i].setPreferredSize(new Dimension(30, 30));
			gridLoc[i].setFont(new Font("Arial", Font.PLAIN, 10));
		}

		// Determine where to place the 10 electro button
		c.gridx = 5*MapX + 1;
	    c.gridy = 5*MapY + 1;
	    Pane.add(gridLoc[0], c);
		
		// Determine where to place the 15 electro button
		c.gridx = 5*MapX + 2;
	    c.gridy = 5*MapY + 1;
	    Pane.add(gridLoc[1], c);

		// Determine where to place the 20 electro button
		c.gridx = 5*MapX + 3;
	    c.gridy = 5*MapY + 1;
	    Pane.add(gridLoc[2], c);

	    // Build and place the city name
	    nameLoc = new JTextField(CityName);
	    nameLoc.setEditable(false);
		nameLoc.setToolTipText(CityName);
	    c.gridx = 5*MapX + 1;
	    c.gridy = 5*MapY + 2;
	    c.gridwidth = 3;
	    Pane.add(nameLoc, c);
	    
	    // Create a couple of buffers so that cities are spaced horizontally and vertically
	    c.gridwidth = 1;	// Set the gridwidth back to 1
	    JLabel buffer1 = new JLabel("    ");
	    c.gridx = 5*MapX + 0;
	    c.gridy = 5*MapY + 0;
	    Pane.add(buffer1, c);
	    JLabel buffer2 = new JLabel("    ");
	    c.gridx = 5*MapX + 4;
	    c.gridy = 5*MapY + 4;
	    Pane.add(buffer2, c);

	    // Set up ActionListeners for each button
		for (int i = 0; i <= 2; i++) {
	        gridLoc[i].addActionListener(listener);
		}
	}
	
	// Routine to add a connecting city to me
	public void addConnection(City ConnCity, int Cost) {
		connections.add(ConnCity);
		connectionCosts.add(Cost);
	}
	
	public void purchaseCity(Player Purchaser, int Cost) {
		//TODO add code here
		Purchaser.spendElectros(Cost);
		Purchaser.addCity(this);
	}
	
	// public access methods
	public String getName() {
		return cityName;
	}
	public int getX() {
		return nameLoc.getX();
	}
	public int getY() {
		return nameLoc.getY();
	}
	public ArrayList<City> getConnections() {
		return connections;
	}
	public ArrayList<Integer> getConnectionCosts() {
		return connectionCosts;
	}
	public void enableButton(int ButtonNo) {
		gridLoc[ButtonNo].setEnabled(true);
	}
	public JButton getButton(int ButtonNo) {
		return gridLoc[ButtonNo];
	}
	public boolean isConnected(City OtherCity) {
		boolean isCon = false;
		for (int i = 0; i < connections.size(); i++) {
			if (OtherCity.equals(connections.get(i))) {
				isCon = true;
			}
		}
		return isCon;
	}
	
	/**
	 * Routine the identifies whether the city is available for selecting.  This means
	 * having the "10" spot open in any Step, the "15" spot in Step 2, or the "20" spot
	 * in Step 3
	 * <p>
	 * @return boolean indicating whether the city is available for selecting
	 */
	public boolean isAvailable() {
		if (gridLoc[0].isEnabled()) {
			return(true);
		} else if (gameControl.getStep() == 2) {
			if (gridLoc[1].isEnabled()) {
				return(true);
			}
		} else if (gameControl.getStep() == 3) {
			// We should only have to check gridLoc[2] but nothing prevents someone from 
			// selecting the 20 spot in Step 3, even if the 15 is still open
			if (gridLoc[1].isEnabled() | gridLoc[2].isEnabled()) {
				return(true);
			}
		}
		return(false);
	}
	
	public boolean alreadyInCity (Player CurPlayer) {
		Color curPlayerColor = CurPlayer.getPaintColor();
		return ((curPlayerColor == gridLoc[0].getBackground()) | (curPlayerColor == gridLoc[1].getBackground()) | (curPlayerColor == gridLoc[2].getBackground()));
	}
	
	public int getCurrentCost() {
		if (gridLoc[0].isEnabled()) {
			return 10;
		} else if (gridLoc[1].isEnabled()) {
			return 15;
		} else if (gridLoc[2].isEnabled()) {
			return 20;
		}
		// No available spot in this city, so return -1
		return -1;
	}
	
	public int getCostOfConnection(City OtherCity) {
		for (int i = 0; i < connections.size(); i++) {
			if (connections.get(i).equals(OtherCity)) {
				return connectionCosts.get(i);
			}
		}
		return -1;		// No connection exists, so return a negative value
	}
}
