import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * 
 */
// TODO: Should figure out how to add scrolling to the MainWindow (does it require having a JPanel within the JFrame that everything gets added to?
// TODO: Need to figure out how to add graphics to the window/panel/whatever, so that the connections are obvious

/**
 * @author Michael Lyons
 *
 */
//public class MainWindow extends JFrame {
public class MainWindow extends JPanel {
	// Required parameter
	private static final long serialVersionUID = 88L;
	
	// Constants (some will be configurable when add game options for # of players, or expansion pack rules)
	public static final int PLAYER_COUNT = 6;			// Number of players in the game
	private static final int CITY_COUNT = 42;			// Max number of cities on the map
	private static final int GAME_CITY_COUNT = 35;		// Number of cities being used in the game
	private static final int BUTTON_15 = 1;				// Array element of the 15 electro buttons to activate in Step 2
	private static final int BUTTON_20 = 2;				// Array element of the 20 electro buttons to activate in Step 3
	private static final int CITY_TRIGGER_STEP2 = 7;	// Number of cities to trigger Step 2 (actually depends on # of players)
	private static final int STEP3_CARD_NO = -1;		// Watch for this PP # to pop up from the deck to trigger Step 3
	private static final int CITY_TRIGGER_END = 14;		// Number of cities to trigger end of game (actually depends on # of players)
	private static final int PP_MARKET_ORIGIN_X = 19;	// Constants to control placement of Power Plant Market on game board
	private static final int PP_MARKET_ORIGIN_Y = 0;
	private static final int PP_MARKET_SIZE = 8;		// Max number of PPs shown in Market at one time
	
	public static final int RSC_NORM_PROD = 1;
	public static final int RSC_ALL_RSC = 2;
	public static final int RSC_LESS_THAN_5 = 3;
	public static final int RSC_LEAST_AVAIL = 4;
	
	// These fields will be moved to the City class, which will be held be an array of Regions in the MainWindow
	private Container cp;
	private JFrame gameFrame;							// JFrame that represents the main window for the game
	private City cities[];								// Array of City objects on the map being used (US only for now)
	private ArrayList<CityConnector> connectors;		// ArrayList holding all connections between cities
//	private ArrayList<Integer> connectionCosts;			// ArrayList holding cost of all connections between cities
	private ArrayList<Player> players;
	private JTextField playerOrder[];
	private ResourceMarket rm;
	private GridBagConstraints c;
	private int step = 1, phase = 2;	// The constructor will get us thru Phase 1 (Determine Player Order) before completing
	private int turn = 1;				// Keep track of turns for odd/even bot rule
	private boolean step3Pending = false;
	private JTextField stepNo, phaseNo;	// These fields display the current step and phase
	private int playerTurn = 0;			// This will track which player's turn it is; goes from 0 to PLAYER_COUNT-1 for phase 2 and PLAYER_COUNT-1 to 0 for other phases 
	private boolean firstTurn = true;	// We'll need to reorder the players after purchasing PPs in the first turn
	private ArrayList<PowerPlant> deck;
	private ArrayList<PowerPlant> ppMarket;
	private boolean ppPurchased = false;
	private JButton ppMkt[];
	private JTextField resourceType[], resourceAmount[], cityPower[];
	private JTextField instructionText;	// Displays messages informing what's happened and what to do next
	Random random = new Random();

	
	public MainWindow() {
		gameFrame = new JFrame();
//		gameFrame = GameFrame;
		// Retrieve the top-level content-pane from JFrame
	    cp = gameFrame.getContentPane();
//		cp = this;
		
	    // Content-pane sets layout
	    cp.setLayout(new GridBagLayout());

	    c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;

		setupDeck();
		
	    // Add the UI components here
	    cities = new City[CITY_COUNT];
	    setupMap();
	    
	    playerOrder = new JTextField[PLAYER_COUNT];
	    players = new ArrayList<Player>();
	    setupPlayers();
	    
	    setupScoreboard();
	    setupPPmarket();
	    
	    setupResourceMarket();

	    // Exit the program when the close-window button clicked
	    gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	    // Call some super methods to set up the MainWindow attributes
	    gameFrame.setTitle("Power Grid in Java");  // "super" JFrame sets title
	    gameFrame.setSize(1400, 700);   // "super" JFrame sets initial size
	    gameFrame.setVisible(true);    // "super" JFrame shows
	}
	
	// Create an ActionListener for ending the turn; it will just call the routine that handles all of the paperwork
	ActionListener endTurnListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof JButton) {
        		endTurn();
           }
        }
    };

	// Create an ActionListener for the player status button
	ActionListener statusListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof JButton) {
        		showPlayerStatus();
           }
        }
    };

    public Container getContentPane() {
    	return cp;
    }
    
	private void setupDeck() {
		String type;
		int resources, cities;
		PowerPlant card13 = null; // holds a special card
		
		deck = new ArrayList<PowerPlant>();
		ppMarket = new ArrayList<PowerPlant>();
		
		for (int i = 3; i <= 50; i++) {
			switch(i) {
			case 4: case 8: case 10: case 15: case 20: case 25: case 31: case 36: case 42:
				type = "Coal";
				break;
			case 3: case 7: case 9: case 16: case 26: case 32: case 35: case 40:
				type = "Oil";
				break;
			case 5: case 12: case 21: case 29: case 46:
				type = "Hybrid";
				break;
			case 6: case 14: case 19: case 24: case 30: case 38:
				type = "Garbage";
				break;
			case 11: case 17: case 23: case 28: case 34: case 39:
				type = "Uranium";
				break;
			default:
				type = "Green";
			}
			switch(i) {
			case 7: case 8: case 20: case 30: case 31: case 32: case 36: case 38: case 46:
				resources = 3;
				break;
			case 3: case 4: case 5: case 10: case 12: case 14: case 15: case 16: case 19: case 21: case 24:
			case 25: case 26: case 40: case 42:
				resources = 2;
				break;
			case 6: case 9: case 11: case 17: case 23: case 28: case 29: case 34: case 35: case 39:
				resources = 1;
				break;
			case 13: case 18: case 22: case 27: case 33: case 	37: case 44: case 50:
				resources = 0;
				break;
			case 41: case 43: case 45: case 47: case 48: case 49:
				resources = -1;
				break;
			default:
				resources = -2;
			}
			switch(i) {
			case 36: case 38: case 46:
				cities = 7;
				break;
			case 30: case 31: case 32: case 39: case 40: case 42: case 50:
				cities = 6;
				break;
			case 20: case 25: case 26: case 34: case 35: case 44:
				cities = 5;
				break;
			case 21: case 24: case 28: case 29: case 33: case 37:
				cities = 4;
				break;
			case 15: case 16: case 19: case 23: case 27:
				cities = 3;
				break;
			case 7: case 8: case 10: case 11: case 12: case 14: case 17: case 18: case 22:
				cities = 2;
				break;
			default:
				cities = 1;
			}
			PowerPlant pp = new PowerPlant(i, type, resources, cities);
			// Don't add 3-10 to the deck, they go to the market, 13 will be placed on top of the deck after shuffling, Step 3 to the bottom
			// Also, resources = -1 means there was no card
			if ((i > 10) & (i != 13) & (resources >= 0)) {
				deck.add(pp);
			} else if (i==13) {
				card13 = pp;
			} else if (i  <= 10) {
				ppMarket.add(pp);
			}
		}
		Collections.shuffle(deck);
		deck.add(0, card13);	// Put Power Plant 13 at the top of the deck
		deck.add(new PowerPlant(-1, "Step 3", 0, 0));  // Create the Step 3 card and add it to the end of the deck
		
		Collections.sort(ppMarket);
	}
	
	private void setupPPmarket() {
		// Create a generic ActionListener for the PP market buttons; this will open the auction window with the selected PP displayed
		ActionListener ppMktListener = new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            if (e.getSource() instanceof JButton) {
	            	if (getStep() == 3) {
	            		// Message that in Step 3, button press is being recorded
	            		JOptionPane.showMessageDialog(cp,
	            			    "In Step 3 and detected a PP Market button press... what is exception that fires?");
	            	}
	            	if (getPhase() == 2) {
	            		int ppNumber = Integer.parseInt(((JButton) e.getSource()).getText());
	            		if (getTurnPlayer().getElectroCount() >= ppNumber) {
	            			PowerPlant curPP = getPowerPlant(ppNumber);
	            			MainWindow gameControl = getMainWindow();
//	            			AuctionPanel auction = new AuctionPanel(curPP, gameControl); 
	            			new AuctionPanel(curPP, gameControl);
	            		} else {
		            		// Pop up a dialog box that they can't afford the selected Power Plants
		            		JOptionPane.showMessageDialog(cp,
		            			    "You do not have enough Electors to bid on the selected Power Plant.");
	            		}
	            	} else {
	            		// Pop up a dialog box that they can only select PPs during Phase 2 - Auction Power Plants
	            		JOptionPane.showMessageDialog(cp,
	            			    "You may only select power plants during Phase 2 - Auction Power Plants.");
	            	}
	            }
	        }
	    };
		
		// First place the overall label
		JTextField lblPPmkt = new JTextField("PP Market:");
		lblPPmkt.setEditable(false);
		c.gridx = PP_MARKET_ORIGIN_X;
		c.gridy = PP_MARKET_ORIGIN_Y;
	    c.gridwidth = 2;
		cp.add(lblPPmkt, c);
		
		// Now place the labels
	    c.gridwidth = 1;
		c.gridx += 2;
		JTextField lblMinBid = new JTextField("Min Bid");
		lblMinBid.setEditable(false);
		cp.add(lblMinBid, c);
		c.gridy++;
		JTextField lblRsrcTyp = new JTextField("Rsc Typ");
		lblRsrcTyp.setEditable(false);
		cp.add(lblRsrcTyp, c);
		c.gridy++;
		JTextField lblRsrcAmt = new JTextField("Rsc Amt");
		lblRsrcAmt.setEditable(false);
		cp.add(lblRsrcAmt, c);
		c.gridy++;
		JTextField lblCities = new JTextField("Cities");
		lblCities.setEditable(false);
		cp.add(lblCities, c);
		
		// Use a button to host the minimum bid field for each card in the market as that will allow a player to select it for auction
		ppMkt = new JButton[PP_MARKET_SIZE];
		resourceType = new JTextField[PP_MARKET_SIZE];
		resourceAmount = new JTextField[PP_MARKET_SIZE];
		cityPower = new JTextField[PP_MARKET_SIZE];
		
		for (int i = 0; i < PP_MARKET_SIZE; i++) {
			ppMkt[i] = new JButton(ppMarket.get(i).getMinBid() + "");
			resourceType[i] = new JTextField(ppMarket.get(i).getFuelType());
			resourceType[i].setEditable(false);
			resourceAmount[i] = new JTextField(ppMarket.get(i).getResourceCount()+"");
			resourceAmount[i].setEditable(false);
			cityPower[i] = new JTextField(ppMarket.get(i).getCitiesPowered()+"");
			cityPower[i].setEditable(false);
			// Disable any buttons beyond the first 4 positions in Steps 1 & 2
			if ((i > 3) & (step < 3)) {
				ppMkt[i].setEnabled(false);
			} else {
				ppMkt[i].setEnabled(true);
			}
			ppMkt[i].setFont(new Font("Arial", Font.PLAIN, 10));
			c.gridx++;
			c.gridy = 0;
			cp.add(ppMkt[i], c);
			
			c.gridy = 1;
			cp.add(resourceType[i], c);

			c.gridy = 2;
			cp.add(resourceAmount[i], c);

			c.gridy = 3;
			cp.add(cityPower[i], c);
		}

		// Set up ActionListeners for each PP button
		// TODO: figure out why buttons don't work during Step 3
		for (int i = 0; i < PP_MARKET_SIZE; i++) {
	        ppMkt[i].addActionListener(ppMktListener);
		}
	}
	
	public void updatePowerPlantMarketDisplay() {
		for (int i = 0; i < ppMarket.size(); i++) {
			ppMkt[i].setText(ppMarket.get(i).getMinBid() + "");
			resourceType[i].setText(ppMarket.get(i).getFuelType());
			resourceAmount[i].setText(ppMarket.get(i).getResourceCount() + "");
			cityPower[i].setText(ppMarket.get(i).getCitiesPowered() + "");
		}
	}

	private void setupScoreboard() {
		// Build and place the scoreboard
		// Show the Step # and Phase
		JTextField lblStep = new JTextField("Step:");
		lblStep.setEditable(false);
	    c.gridx = 0;
	    c.gridy = 0;
	    cp.add(lblStep, c);		
	    stepNo = new JTextField(step + "");
	    stepNo.setEditable(false);
	    c.gridx = 1;
	    cp.add(stepNo, c);
		
	    JTextField lblPhase = new JTextField("Phase:");
	    lblPhase.setEditable(false);
	    c.gridx = 2;
	    cp.add(lblPhase, c);
	    phaseNo = new JTextField(phase + "");
	    phaseNo.setEditable(false);
	    c.gridx = 3;
	    cp.add(phaseNo, c);
		
		// Show the player order
		JButton playerStatusButton = new JButton("Order");
		playerStatusButton.addActionListener(statusListener);
	    c.gridx = 5;
	    c.gridy = 0;
	    cp.add(playerStatusButton, c);

	    for (int i = 0; i < PLAYER_COUNT; i++) {
	    	playerOrder[i] = new JTextField("None");
	    	playerOrder[i].setEditable(false);
	    	c.gridx = i+6;
		    cp.add(playerOrder[i], c);
	    }
	    determinePlayerOrder(playerOrder, true);
		playerOrder[0].setForeground(Color.white);	// At the start of the game, use white font to show that it is the first player's turn

	    // Set up end turn button and instruction text area
	    c.gridx++;
		JButton endTurnButton = new JButton("End Turn");
		endTurnButton.setFont(new Font("Arial", Font.PLAIN, 10));
		endTurnButton.addActionListener(endTurnListener);
		cp.add(endTurnButton, c);
		
		c.gridx++;
		instructionText = new JTextField("Current player select PP for auction");
		instructionText.setForeground(Color.red);
	    c.gridwidth = 6;
	    cp.add(instructionText, c);
	}
	
	private void setupMap() {
		connectors = new ArrayList<CityConnector>();
	    cities[0] = new City("Seattle", 0, 0, cp, this);
	    cities[1] = new City("Portland", 0, 1, cp, this);
	    cities[2] = new City("San Fran", 0, 2, cp, this);
	    cities[3] = new City("LA", 0, 3, cp, this);
	    cities[4] = new City("Las Vegas", 0, 4, cp, this);
	    cities[5] = new City("San Diego", 0, 5, cp, this);
	    
	    cities[6] = new City("Billings", 1, 0, cp, this);
	    cities[7] = new City("Boise", 1, 1, cp, this);
	    cities[8] = new City("Denver", 1, 2, cp, this);
	    cities[9] = new City("Salt Lake", 1, 3, cp, this);
	    cities[10] = new City("Santa Fe", 1, 4, cp, this);
	    cities[11] = new City("Phoenix", 1, 5, cp, this);
	    
	    cities[12] = new City("Fargo", 2, 0, cp, this);
	    cities[13] = new City("Cheyenne", 2, 1, cp, this);
	    cities[14] = new City("OK City", 2, 3, cp, this);
	    cities[15] = new City("Dallas", 2, 4, cp, this);
	    cities[16] = new City("Houston", 2, 5, cp, this);
	    
	    cities[17] = new City("Duluth", 3, 0, cp, this);
	    cities[18] = new City("Omaha", 3, 1, cp, this);
	    cities[19] = new City("Kansas City", 3, 2, cp, this);
	    cities[20] = new City("Memphis", 3, 4, cp, this);

	    cities[21] = new City("Minneapolis", 4, 1, cp, this);
	    cities[22] = new City("Chicago", 4, 2, cp, this);
	    cities[23] = new City("St Louis", 4, 3, cp, this);
	    cities[24] = new City("Birmingham", 4, 4, cp, this);
	    cities[25] = new City("New Orleans", 4, 5, cp, this);

	    cities[26] = new City("Detroit", 5, 1, cp, this);
	    cities[27] = new City("Cincinnati", 5, 2, cp, this);
	    cities[28] = new City("Knoxville", 5, 3, cp, this);
//	    cities[35] = new City("Atlanta", 5, 4, cp, this);
//	    cities[36] = new City("Tampa", 5, 5, cp, this);

	    cities[29] = new City("Buffalo", 6, 0, cp, this);
	    cities[30] = new City("Pittsburgh", 6, 3, cp, this);		// Should be 6, 1
	    cities[31] = new City("Washington", 6, 5, cp, this);		// Should be 6, 2
//	    cities[37] = new City("Raleigh", 6, 3, cp, this);
//	    cities[38] = new City("Jacksonville", 6, 4, cp, this);

	    cities[32] = new City("Boston", 6, 1, cp, this);			// Should be 7, 0
	    cities[33] = new City("New York", 6, 2, cp, this);			// Should be 7, 1
	    cities[34] = new City("Philadelphia", 6, 4, cp, this);		// Should be 7, 2
//	    cities[39] = new City("Norfolk", 7, 3, cp, this);
//	    cities[40] = new City("Savannah", 7, 4, cp, this);
//	    cities[41] = new City("Miami", 7, 5, cp, this);

	    // Obviously I need to read this crap from a file so we can have different maps
	    connectors.add(new CityConnector(5, 2, cities[0], cities[6], 9, cp));		// Seattle to Billings
	    connectors.add(new CityConnector(5, 5, cities[0], cities[7], 12, cp));		// Seattle to Boise
	    connectors.add(new CityConnector(2, 5, cities[0], cities[1], 3, cp));		// Seattle to Portland
	    connectors.add(new CityConnector(5, 7, cities[1], cities[7], 13, cp));		// Portland to Boise
	    connectors.add(new CityConnector(2, 10, cities[1], cities[2], 24, cp));		// Portland to San Fran
	    connectors.add(new CityConnector(5, 8, cities[2], cities[7], 23, cp));		// San Fran to Boise
	    connectors.add(new CityConnector(5, 15, cities[2], cities[9], 27, cp));		// San Fran to Salt Lake
	    connectors.add(new CityConnector(0, 16, cities[2], cities[4], 14, cp));		// San Fran to Las Vegas
	    connectors.add(new CityConnector(2, 15, cities[2], cities[3], 9, cp));		// San Fran to Los Angeles
	    connectors.add(new CityConnector(2, 20, cities[3], cities[4], 9, cp));		// Los Angeles to Las Vegas
	    connectors.add(new CityConnector(0, 21, cities[3], cities[5], 3, cp));		// Los Angeles to San Diego
	    connectors.add(new CityConnector(5, 18, cities[4], cities[9], 18, cp));		// Las Vegas to Salt Lake
	    connectors.add(new CityConnector(5, 22, cities[4], cities[10], 27, cp));	// Las Vegas to Santa Fe
	    connectors.add(new CityConnector(5, 25, cities[4], cities[11], 15, cp));	// Las Vegas to Phoenix
	    connectors.add(new CityConnector(2, 25, cities[4], cities[5], 9, cp));		// Las Vegas to San Diego
	    connectors.add(new CityConnector(5, 27, cities[5], cities[11], 14, cp));	// San Diego to Phoenix
	    
	    connectors.add(new CityConnector(10, 2, cities[6], cities[12], 17, cp));		// Billings to Fargo
	    connectors.add(new CityConnector(9, 3, cities[6], cities[21], 18, cp));		// Billings to Minneapolis
	    connectors.add(new CityConnector(10, 5, cities[6], cities[13], 9, cp));		// Billings to Cheyenne
	    connectors.add(new CityConnector(7, 5, cities[6], cities[7], 12, cp));		// Billings to Boise
	    connectors.add(new CityConnector(10, 7, cities[7], cities[13], 24, cp));	// Boise to Cheyenne
	    connectors.add(new CityConnector(5, 11, cities[7], cities[9], 8, cp));		// Boise to Salt Lake
	    connectors.add(new CityConnector(10, 8, cities[8], cities[13], 0, cp));		// Denver to Cheyenne
	    connectors.add(new CityConnector(11, 12, cities[8], cities[19], 16, cp));	// Denver to Kansas City
	    connectors.add(new CityConnector(5, 16, cities[8], cities[10], 13, cp));	// Denver to Santa Fe
	    connectors.add(new CityConnector(7, 15, cities[8], cities[9], 21, cp));		// Denver to Salt Lake
	    connectors.add(new CityConnector(7, 20, cities[9], cities[10], 28, cp));	// Salt Lake to Santa Fe
	    connectors.add(new CityConnector(7, 25, cities[10], cities[11], 18, cp));	// Santa Fe to Phoenix
	    connectors.add(new CityConnector(9, 20, cities[10], cities[19], 16, cp));	// Santa Fe to Kansas City
	    connectors.add(new CityConnector(10, 18, cities[10], cities[14], 15, cp));	// Santa Fe to OK City
	    connectors.add(new CityConnector(10, 22, cities[10], cities[15], 16, cp));	// Santa Fe to Dallas
	    connectors.add(new CityConnector(10, 25, cities[10], cities[16], 21, cp));	// Santa Fe to Houston

	    connectors.add(new CityConnector(15, 2, cities[12], cities[17], 6, cp));	// Fargo to Duluth
	    connectors.add(new CityConnector(14, 3, cities[12], cities[21], 6, cp));	// Fargo to Minneapolis
	    connectors.add(new CityConnector(14, 5, cities[13], cities[21], 18, cp));	// Cheyenne to Minneapolis
	    connectors.add(new CityConnector(15, 7, cities[13], cities[18], 14, cp));	// Cheyenne to Omaha
	    connectors.add(new CityConnector(15, 14, cities[14], cities[19], 8, cp));	// OK City to Kansas City
	    connectors.add(new CityConnector(15, 20, cities[14], cities[20], 14, cp));	// OK City to Memphis
	    connectors.add(new CityConnector(12, 20, cities[14], cities[15], 3, cp));	// OK City to Dallas
	    connectors.add(new CityConnector(15, 22, cities[15], cities[20], 12, cp));	// Dallas to Memphis
	    connectors.add(new CityConnector(14, 24, cities[15], cities[25], 12, cp));	// Dallas to New Orleans
	    connectors.add(new CityConnector(12, 25, cities[15], cities[16], 5, cp));	// Dallas to Houston
	    connectors.add(new CityConnector(16, 27, cities[16], cities[25], 8, cp));	// Houston to New Orleans

	    connectors.add(new CityConnector(19, 3, cities[17], cities[26], 15, cp));	// Duluth to Detroit
	    connectors.add(new CityConnector(19, 4, cities[17], cities[22], 12, cp));	// Duluth to Chicago
	    connectors.add(new CityConnector(20, 5, cities[17], cities[21], 5, cp));	// Duluth to Minneapolis
	    connectors.add(new CityConnector(20, 7, cities[18], cities[21], 8, cp));	// Omaha to Minneapolis
	    connectors.add(new CityConnector(20, 10, cities[18], cities[22], 13, cp));	// Omaha to Chicago
	    connectors.add(new CityConnector(17, 10, cities[18], cities[19], 5, cp));	// Omaha to Kansas City
	    connectors.add(new CityConnector(20, 12, cities[19], cities[22], 8, cp));	// Kansas City to Chicago
	    connectors.add(new CityConnector(20, 15, cities[19], cities[23], 6, cp));	// Kansas City to St. Louis
	    connectors.add(new CityConnector(17, 20, cities[19], cities[20], 12, cp));	// Kansas City to Memphis
	    connectors.add(new CityConnector(20, 18, cities[20], cities[23], 7, cp));	// Memphis to St. Louis
	    connectors.add(new CityConnector(20, 22, cities[20], cities[24], 6, cp));	// Memphis to Birmingham
	    connectors.add(new CityConnector(20, 25, cities[20], cities[25], 7, cp));	// Memphis to New Orleans

	    connectors.add(new CityConnector(22, 10, cities[21], cities[22], 8, cp));	// Minneapolis to Chicago
	    connectors.add(new CityConnector(25, 8, cities[22], cities[26], 7, cp));	// Chicago to Detroit
	    connectors.add(new CityConnector(25, 12, cities[22], cities[27], 7, cp));	// Chicago to Cincinnati
	    connectors.add(new CityConnector(22, 15, cities[22], cities[23], 10, cp));	// Chicago to St. Louis
	    connectors.add(new CityConnector(25, 14, cities[23], cities[27], 12, cp));	// St. Louis to Cincinnati
//	    connectors.add(new CityConnector(0, 0, cities[23], cities[35], 12, cp));	// St. Louis to Atlanta
//	    connectors.add(new CityConnector(0, 0, cities[24], cities[35], 3, cp));		// Birmingham to Atlanta
//	    connectors.add(new CityConnector(0, 0, cities[24], cities[38], 9, cp));		// Birmingham to Jacksonville
	    connectors.add(new CityConnector(22, 25, cities[24], cities[25], 11, cp));	// Birmingham to New Orleans
//	    connectors.add(new CityConnector(0, 0, cities[25], cities[38], 16, cp));	// New Orleans to Jacksonville
	    connectors.add(new CityConnector(30, 3, cities[26], cities[29], 7, cp));	// Detroit to Buffalo
	    connectors.add(new CityConnector(29, 8, cities[26], cities[30], 6, cp));	// Detroit to Pittsburgh
	    connectors.add(new CityConnector(27, 10, cities[27], cities[26], 4, cp));	// Cincinnati to Detroit
	    connectors.add(new CityConnector(30, 15, cities[27], cities[30], 7, cp));	// Cincinnati to Pittsburgh
//	    connectors.add(new CityConnector(0, 0, cities[27], cities[37], 15, cp));	// Cincinnati to Raleigh
	    connectors.add(new CityConnector(27, 15, cities[27], cities[28], 6, cp));	// Cincinnati to Knoxville
//	    connectors.add(new CityConnector(0, 0, cities[28], cities[35], 5, cp));		// Knoxville to Atlanta
//	    connectors.add(new CityConnector(0, 0, cities[35], cities[37], 7, cp));		// Atlanta to Raleigh
//	    connectors.add(new CityConnector(0, 0, cities[35], cities[40], 7, cp));		// Atlanta to Savannah
//	    connectors.add(new CityConnector(0, 0, cities[36], cities[38], 16, cp));	// Tampa to Jacksonville
//	    connectors.add(new CityConnector(0, 0, cities[36], cities[41], 16, cp));	// Tampa to Miami

	    connectors.add(new CityConnector(30, 6, cities[29], cities[30], 7, cp));	// Buffalo to Pittsburgh
	    connectors.add(new CityConnector(30, 5, cities[29], cities[33], 8, cp));	// Buffalo to New York
	    connectors.add(new CityConnector(30, 21, cities[30], cities[31], 6, cp));	// Pittsburgh to Washington
//	    connectors.add(new CityConnector(0, 0, cities[30], cities[37], 7, cp));		// Pittsburgh to Raleigh
	    connectors.add(new CityConnector(32, 25, cities[31], cities[34], 3, cp));	// Washington to Philadelphia
//	    connectors.add(new CityConnector(0, 0, cities[31], cities[39], 5, cp));		// Washington to Norfolk
//	    connectors.add(new CityConnector(0, 0, cities[37], cities[39], 3, cp));		// Raleigh to Norfolk
//	    connectors.add(new CityConnector(0, 0, cities[37], cities[40], 7, cp));		// Raleigh to Savannah
//	    connectors.add(new CityConnector(0, 0, cities[38], cities[40], 0, cp));		// Jacksonville to Savannah

	    connectors.add(new CityConnector(32, 10, cities[32], cities[33], 3, cp));	// Boston to New York
	    connectors.add(new CityConnector(30, 16, cities[33], cities[34], 0, cp));	// New York to Philadelphia
	}

	
	private void setupPlayers() {
		players.add(new Player("Yellow", 1, true, this));
		players.add(new Player("Green", 2, true, this));
		players.add(new Player("Black", 3, true, this));
		players.add(new Player("Purple", 4, true, this));
		players.add(new Player("Red", 5, true, this));
		players.add(new Player("Blue", 6, false, this));
	}
	
	private void setupResourceMarket() {
		rm = new ResourceMarket(0, 6, cp, this);
	}

	private void displayPlayerOrder() {
		for (int i = 0; i < PLAYER_COUNT; i++) {
			playerOrder[i].setText(players.get(i).getColor());
			playerOrder[i].setBackground(players.get(i).getPaintColor());
		}
	}
	
	private void determinePlayerOrder(JTextField[] PlayerOrder, boolean FirstTurn) {
		if (FirstTurn) {
			// Set up in random order
			Collections.shuffle(players);
			displayPlayerOrder();
		} else {
			// Sort by number of cities and highest power plant
			Collections.sort(players);
			displayPlayerOrder();
		}
	}
	
	public City getCity(String CityName) {
		for (int i = 0; i < CITY_COUNT; i++) {
			if (cities[i].getName() == CityName) {
				return cities[i];
			}
		}
		return null; 
	}
	public PowerPlant getPowerPlant(int MinBid) {
		PowerPlant retVal = null;
		for (int i = 0; i < ppMarket.size(); i++) {
			if (ppMarket.get(i).getMinBid() == MinBid) {
				retVal = ppMarket.get(i);
			}
		}
		return retVal;
	}
	public void removePowerPlant(int MinBid) {
		for (int i = 0; i < PP_MARKET_SIZE; i++) {
			if (ppMarket.get(i).getMinBid() == MinBid) {
				ppMarket.remove(i);
			}
		}
	}
	public void drawPowerPlant() {
		// This routine will get the next PP from the deck, add it to the ppMarket, sort the ppMarket and remove it from the deck
		// TODO: It needs to handle the deck being empty, and drawing the Step 3 card
		if (deck.get(0).getMinBid() == STEP3_CARD_NO) {
			// If Step 3 card is found in phase 2, place it at the end of the market and shuffle the deck
			// Set an indicator to show we need to enter Step 3 at the end of phase 2 (see endBiddingPhase() method)
			ppMarket.add(deck.get(0));
			deck.remove(0);
			Collections.sort(deck);
			if (phase == 2) {
				step3Pending = true;
			} else if (phase == 4) {
				removePowerPlant(ppMarket.get(PP_MARKET_SIZE-1));
				removePowerPlant(ppMarket.get(0));
				initiateStep3();
			} else if (phase == 5) {
				removePowerPlant(ppMarket.get(PP_MARKET_SIZE-1));
				removePowerPlant(ppMarket.get(0));
				initiateStep3();
			}
			updatePowerPlantMarketDisplay();
		} else {
			ppMarket.add(deck.get(0));
			deck.remove(0);
			Collections.sort(ppMarket);
			updatePowerPlantMarketDisplay();
		}
	}
	public void removePowerPlant(PowerPlant SoldPlant) {
		for (int i = 0; i < PP_MARKET_SIZE; i++) {
			if (SoldPlant.equals(ppMarket.get(i))) {
				ppMarket.remove(i);
				return;
			}
		}
	}
	
	// This routine eliminates any Power Plants with a minimum bid lower than the number of cities for any player
	public void eliminateLowPowerPlants() {
		int maxCities = 0, cpCities;
		
		for (int i = 0; i < players.size(); i++) {
			cpCities = players.get(i).getCityCount();
			if (cpCities > maxCities) {
				maxCities = cpCities;
			}
		}
		
		while (ppMarket.get(0).getMinBid() <= maxCities) {
			removePowerPlant(ppMarket.get(0));
			drawPowerPlant();
		}
	}
	
	public MainWindow getMainWindow() {
		return this;
	}
	public ArrayList<Player> getPlayers() {
		return players;
	}
	public Player getPlayer(String Color) {
		Player retVal = null;
		for (int i = 0; i < PLAYER_COUNT; i++) {
			if (players.get(i).getColor().equals(Color)) {
				retVal = players.get(i);
			}
		}
		return retVal;
	}
	public Player getTurnPlayer() {
		return players.get(playerTurn);
	}
	public int getStep() {
		return step;
	}
	public int getPhase() {
		return phase;
	}
	public int getPlayerTurn() {
		return playerTurn;
	}
	public boolean isFirstTurn() {
		return firstTurn;
	}
	public void showPlayerStatus() {
		getTurnPlayer().showStatus();
	}
	public void setPurchase() {
		ppPurchased = true;
	}
	public ResourceMarket getResourceMarket() {
		return rm;
	}
	
	// The main endTurn routine just shuttles the work off to the correct phase turn ender
	public void endTurn() {
		switch(phase) {
			case 2:
				endBiddingTurn();
				break;
			case 3:
				endResourceTurn();
				break;
			case 4:
				endBuildingTurn();
				break;
			case 5:
				endBureaucracyTurn();
				break;
		}
	}
	
	public void endBiddingPhase() {
		// If step 3 is pending, remove the Step 3 card and the lowest card from the market and make all PPs eligible for auction
		if (step3Pending) {
			removePowerPlant(ppMarket.get(PP_MARKET_SIZE-1));
			removePowerPlant(ppMarket.get(0));
			initiateStep3();
		}
		// If no power plants were purchased this round, get rid of lowest
		if (ppPurchased==false) {
			removePowerPlant(ppMarket.get(0));
			drawPowerPlant();
		}
		// Leave turn set to the last player for buying resources first
		playerTurn = PLAYER_COUNT - 1;
		playerOrder[playerTurn].setForeground(Color.white);
		if (firstTurn) {
			Collections.sort(players);
			displayPlayerOrder();
			firstTurn = false;
		}
		// Reset everyone as eligible to bid
		for (int i = 0; i < PLAYER_COUNT; i++) {
			players.get(i).setEligibleToBid(true);
		}
		phase = 3;
		phaseNo.setText(phase + "");
		instructionText.setText("Current player select resource(s) for purchase");
		
		// For each player that is a bot, buy resources per the rule, then pause for a player
		// (any remaining bot turns can be picked up when the player hits the End Turn button
		while (getTurnPlayer().isBot()) {
			processBotResources(getTurnPlayer());
			playerOrder[playerTurn].setForeground(Color.black);
			playerTurn--;
			playerOrder[playerTurn].setForeground(Color.white);
		}
	}
	
	public void endBiddingTurn() {
		// The steps for this include:
		//   - If it's the last player's turn, move to Phase 3 - Buying Resources, but if the first turn resort players,
		//		and update player order on the display
		//   - Else, move to the next player's turn
		if (firstTurn) {
			// Make sure the player purchased a Power Plant before ending the turn
			String curColor =  playerOrder[playerTurn].getText();
			Player curPlayer = getPlayer(curColor);
			if (curPlayer.isEligibleToBid()) {
				// Msg Box that they must purchase a PP on the first turn
        		JOptionPane.showMessageDialog(cp,
        			    "You must purchase a Power Plant during your first turn.");
				return;
			}
		}
		if (playerTurn + 1 == PLAYER_COUNT) {
			endBiddingPhase();
		} else {
			getTurnPlayer().setEligibleToBid(false);
			playerOrder[playerTurn].setForeground(Color.black);
			playerTurn++;
			while (playerTurn < PLAYER_COUNT) {
				if (getTurnPlayer().isEligibleToBid()) {
					playerOrder[playerTurn].setForeground(Color.white);
					return;
				}
				playerTurn++;
			}
			// If it falls thru the while loop there are no remaining eligible bidders
			endBiddingPhase();
		}
	}
	
	public void endResourcePhase() {
		playerOrder[playerTurn].setForeground(Color.black);
		playerTurn = PLAYER_COUNT - 1;
		playerOrder[playerTurn].setForeground(Color.white);
		phase = 4;
		phaseNo.setText(phase + "");
		instructionText.setText("Current player should build cities");
		
		// For each player that is a bot, build as necessary, then pause for a player
		// (any remaining bot turns can be picked up when the player hits the End Turn button
		while (getTurnPlayer().isBot()) {
			processBotBuild(getTurnPlayer());
			playerOrder[playerTurn].setForeground(Color.black);
			playerTurn--;
			playerOrder[playerTurn].setForeground(Color.white);
		}
	}
	
	public void endResourceTurn() {
		boolean isBot = false;
		boolean endPhase = false;
	
		if (playerTurn == 0) {
			endResourcePhase();
		} else {
			playerOrder[playerTurn].setForeground(Color.black);
			playerTurn--;
			playerOrder[playerTurn].setForeground(Color.white);

			isBot = getTurnPlayer().isBot();
			while (isBot) {
				processBotResources(getTurnPlayer());
				playerOrder[playerTurn].setForeground(Color.black);
				if (playerTurn == 0) {
					isBot = false;
					endPhase = true;
				} else {
					playerTurn--;
					playerOrder[playerTurn].setForeground(Color.white);
					isBot = getTurnPlayer().isBot();
				}
			}
		}
		// Figure out how to move on from here whether it dropped out because not a bot or last player 
		if (endPhase) {
			endResourcePhase();
		} 
	}
	
	public void endBuildingPhase() {
		playerOrder[playerTurn].setForeground(Color.black);
		playerTurn = 0;
		playerOrder[playerTurn].setForeground(Color.white);
		// Check to see if any PPs need to be removed because the PP # is lower than the # of cities someone has
		eliminateLowPowerPlants();
		
		// If in Step 1, check to see if anyone has the number of cities to activate Step 2
		if (step == 1) {
			for (int i = 0; i < players.size(); i++) {
				// If we are in step 1 and anyone has built enough cities, trigger Step 2
				if (players.get(i).getCityCount() >= CITY_TRIGGER_STEP2) {
					step = 2;
					stepNo.setText(step+"");
					// Enable all of the 15 buttons
					for (int j = 0; j < GAME_CITY_COUNT; j++) {
						cities[j].enableButton(BUTTON_15);
					}
					// Remove the lowest PP from the game
					removePowerPlant(ppMarket.get(0));
					drawPowerPlant();
					break;
				}
			}
		}
		// TODO: Check to see if we have triggered game end
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).getCityCount() >= CITY_TRIGGER_END) {
				// Pop up a box to have the players manually figure out who won.
				step = 4;
				stepNo.setText(step + "");
				phase = 0;
				phaseNo.setText(phase + "");
				instructionText.setText("Manually determine the winner");
        		JOptionPane.showMessageDialog(cp, "Game end has triggered; determine who can power the most cities right now and that is the winner! " +
        				" Tiebreaker is most Electros.  You can do this by running thru the Pay Me buttons for each player and noting # of Electros before paying");
			}
		}
		phase = 5;
		phaseNo.setText(phase + "");
		// First allow the players to choose which plants they will use to power cities, remove resources and pay them
		runBureaucracy();
	}
	
	public void endBuildingTurn() {
		boolean isBot = false;
		boolean endPhase = false;
		
		if (playerTurn == 0) {
			endBuildingPhase();
		} else {
			playerOrder[playerTurn].setForeground(Color.black);
			playerTurn--;
			playerOrder[playerTurn].setForeground(Color.white);
			isBot = getTurnPlayer().isBot();
			while (isBot) {
				processBotBuild(getTurnPlayer());
				playerOrder[playerTurn].setForeground(Color.black);
				if (playerTurn == 0) {
					isBot = false;
					endPhase = true;
				} else {
					playerTurn--;
					playerOrder[playerTurn].setForeground(Color.white);
					isBot = getTurnPlayer().isBot();
				}
			}
		}
		// Figure out how to move on from here whether it dropped out because not a bot or last player 
		if (endPhase) {
			endBuildingPhase();
		} 
	}
	
	public void runBureaucracy() {
		instructionText.setText("Entering bureaucracy - pay players");
		players.get(playerTurn).selectPPs();
		for (int i = 0; i < PLAYER_COUNT; i++) {
			players.get(i).setPaidInd(false);
		}
	}
	
	public void endBureaucracyPhase() {
		// TODO: If the game end has been triggered, don't bother continuing thru this routine
		
		// Next, remove the highest PP (lowest in Step 3) from the market and add a new PP and sort
		// Finally, restock the raw materials and determine player order and set the phase back to 2 (Buy Power Plant)
		playerOrder[playerTurn].setForeground(Color.black);

		// Although the rules say to manipulate the PP market before restocking the resource market, we'll
		// restock first because if the Steo 3 card is drawn we are supposed to restock with Step 2 levels
		instructionText.setText("Restocking resource market");
		rm.restockMarket();

		instructionText.setText("Flipping PP market");
		if (step < 3) {
			deck.add(ppMarket.get(PP_MARKET_SIZE - 1));
			removePowerPlant(ppMarket.get(PP_MARKET_SIZE - 1));
		} else {
			removePowerPlant(ppMarket.get(0));
		}
		drawPowerPlant();
		// Reset the flag to track whether a power plant will be purchased in the next round
		ppPurchased = false;
		
		turn++;
		phase = 1;
		phaseNo.setText(phase + "");
		instructionText.setText("Determining player order");
		Collections.sort(players);
		displayPlayerOrder();
		
		phase = 2;
		phaseNo.setText(phase + "");
		instructionText.setText("Current player select PP for auction");
		playerTurn = 0;
		playerOrder[playerTurn].setForeground(Color.white);
}

	public void endBureaucracyTurn() {
		if (players.get(playerTurn).getPaidInd()) {
			playerOrder[playerTurn].setForeground(Color.black);
			if (playerTurn == 5) {
				endBureaucracyPhase();
			} else {
				playerTurn++;
				playerOrder[playerTurn].setForeground(Color.white);
				players.get(playerTurn).selectPPs();
			}
		} else {
			// Player was not paid, select PPs again
			players.get(playerTurn).selectPPs();
		}
	}
	
	public void initiateStep3() {
		step = 3;
		stepNo.setText(step+"");
		ppMkt[4].setEnabled(true);
		ppMkt[5].setEnabled(true);
		// Enable all of the 20 buttons
		for (int j = 0; j < GAME_CITY_COUNT; j++) {
			cities[j].enableButton(BUTTON_20);
		}
	}

	public void processBotResources(Player CurPlayer) {
		switch (CurPlayer.getResourceBotRule()) {
		case 1:
			CurPlayer.autoBuyResources(RSC_NORM_PROD);
			CurPlayer.autoBuyResources(RSC_LESS_THAN_5);
			break;
		case 2:
			CurPlayer.autoBuyResources(RSC_ALL_RSC);
			break;
		case 3:
			if (playerTurn == PLAYER_COUNT-1) {
				CurPlayer.autoBuyResources(RSC_ALL_RSC);
			} else {
				CurPlayer.autoBuyResources(RSC_NORM_PROD);
			}
			break;
		case 4:
			CurPlayer.autoBuyResources(RSC_NORM_PROD);
			break;
		case 5:
			CurPlayer.autoBuyResources(RSC_NORM_PROD);
			CurPlayer.autoBuyResources(RSC_LEAST_AVAIL);
			break;
		case 6:
			if (turn % 2 == 1) {
				CurPlayer.autoBuyResources(RSC_NORM_PROD);
			} else {
				CurPlayer.autoBuyResources(RSC_ALL_RSC);
			}
			break;
		}
	}

	public void processBotBuild(Player CurPlayer) {
		int tryCityNo, buttonNo;
		ArrayList<City> cityPair;
		boolean cityFound = false;
		boolean doneBuilding;
		int cityCount = 0;
		
		// If it's the first city, build randomly
		// If it's not the first city, find the lowest connection cost and build there
		// For now, only Building Bot #6 is implemented - All Cities
		if (CurPlayer.getCityCount() == 0) {
			while (!cityFound) {
				tryCityNo = random.nextInt(GAME_CITY_COUNT);
				if (cities[tryCityNo].isAvailable()) {
					if (CurPlayer.getElectroCount() > cities[tryCityNo].getCurrentCost()) {
						buyCity(CurPlayer, cities[tryCityNo], cities[tryCityNo].getCurrentCost(), 0);
						cityCount++;
					}
					cityFound = true;
				}
			}
		} 
		// This will stop building if the bot only builds 1 in Step 1, 2 in 2 and 3 in 3
		if ((CurPlayer.getBuildBotRule() != 5) | (cityCount < step)) {
			doneBuilding = false;
			while (doneBuilding == false) {
				cityPair = CurPlayer.getLowestCostConnection();
				if (cityPair.get(0) != null) {
					int cityCost = 0;
					// TODO: Null pointer exception on this next line - how can cityPair.get(1) be null?
					try {
					cityCost = cityPair.get(1).getCurrentCost();
					} catch (NullPointerException e) {
			            System.out.print("Caught the NullPointerException; cityPair.get(0) is " + cityPair.get(0).getName() + "\n");
			            System.out.print("Caught the NullPointerException; cityPair.get(1) must be null\n");
			            System.out.print("Caught the NullPointerException; cityPair.get(1) is " + cityPair.get(0).getName() + "\n");
					}
					int connCost = cityPair.get(0).getCostOfConnection(cityPair.get(1));
					if (CurPlayer.getElectroCount() > (cityCost + connCost)) {
						// If the bot can afford the purchase, then update the UI and make the purchase happen
						if (cityCost == 10) {
							buttonNo = 0;
						} else if (cityCost == 15) {
							buttonNo = 1;
						} else if (cityCost == 20) {
							buttonNo = 2;
						} else {
							buttonNo = -1;		// Should never get to this line, but just in case
						}
						if (buttonNo < 0) {
							// Show warning that something is wrong
			        		JOptionPane.showMessageDialog(cp,
			        			    "Warning: In MainWindow::processBotBuild() a connection was found but the city cost does not make sense: " + cityCost +
			        			    "; Cities were " + cityPair.get(0).getName() + " and " + cityPair.get(1).getName());
						} else {
							buyCity(CurPlayer, cityPair.get(1), cityCost + connCost, buttonNo);
							cityCount++;
							if ((CurPlayer.getBuildBotRule() == 5) & cityCount >= step) {
								doneBuilding = true;
							}
						}
					} else {
						doneBuilding = true;	// Can't afford any more cities so moving on
					}
				} else {
					doneBuilding = true;	// No available connection was found
					break;
				}
			}
		}
	}
	
	public void buyCity(Player BuyPlayer, City BuyCity, int PurchaseCost, int ButtonNo) {
		BuyCity.getButton(ButtonNo).setEnabled(false);
		BuyCity.getButton(ButtonNo).setBackground(BuyPlayer.getPaintColor());
		BuyCity.getButton(ButtonNo).setOpaque(true);
		
//		JOptionPane.showMessageDialog(cp,
//			    "Bot is making purchase... ");
		BuyCity.purchaseCity(BuyPlayer, PurchaseCost);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Run the GUI construction in the Event-Dispatching thread for thread-safety
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JPanel gamePanel =  new MainWindow(); // Let the constructor do the job

/*
				JFrame gameFrame = new JFrame();
//		        JPanel contentPane = new JPanel(null);
				JPanel gamePanel =  new MainWindow(gameFrame); // Let the constructor do the job
				JScrollPane scrollPane = new JScrollPane(gamePanel);
		        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		        scrollPane.setBounds(50, 30, 300, 50);
		        
				gamePanel.setPreferredSize(new Dimension(500, 400));
				gamePanel.add(scrollPane);
				gameFrame.setContentPane(gamePanel);
				gameFrame.pack();
				gameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				gameFrame.setVisible(true);
*/
				
			}
		});
	}
}
