import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

public class Player implements Comparable<Player>{
	
	final private static int ELECTRO_START = 50;
	final private static int MAX_POWER_PLANTS = 3;	// This is also in the MainWindow class
	final private static int NUM_RESOURCE_TYPES = 5;
	final private static int cityPayoffs[] = {10, 22, 33, 44, 54, 64, 73, 82, 90, 98, 105,
			112, 118, 124, 129, 134, 138, 142, 145, 148, 150}; 
	
	private String color;
	private Color paintColor;
	private int playerNo;
	private int electroCount;
	private boolean botInd;
	private Random random;
	private int firstCityBotRule, auctionBotRule, resourceBotRule, buildBotRule;
	private MainWindow gameControl;
	private JPanel panel;
	private boolean bidEligible;				// This is to keep track of who is still eligible to bid on PPs (set true at start of phase 2; set false if pass)
	private boolean paidInd;					// This is to keep track of whether I have been paid in Phase 5 of each turn
	private ArrayList<PowerPlant> myPlants;
	private ArrayList<City> myCities;
	private int coalInv, oilInv, garbageInv, uraniumInv;
	
	// Power Plant selection attributes
	HashMap<String, Integer> dictionary;
	JFrame myFrame;							// Window for player to choose power plants to run in current round
	private JCheckBox ppCheckboxes[];
	private JButton payMeButton;

	public Player(String MyColor, int PlayerNo, boolean BotInd, MainWindow GameControl) {
		// TODO Auto-generated constructor stub
		color = MyColor;
		switch (color) {
			case "Red":
				paintColor = Color.RED;
				break;
			case "Yellow":
				paintColor = Color.YELLOW;
				break;
			case "Green":
				paintColor = Color.GREEN;
				break;
			case "Blue":
				paintColor = Color.BLUE;
				break;
			case "Purple":
				paintColor = Color.ORANGE;
				break;
			case "Black":
				paintColor = Color.BLACK;
				break;
			default:
				paintColor = Color.PINK;
		}
		playerNo = PlayerNo;
		botInd = BotInd;
		random = new Random();
		if (botInd) {
			determineBotRules();
		}
		gameControl = GameControl;
		panel = gameControl.getPanel();
		electroCount = ELECTRO_START;
		bidEligible = true;
		paidInd = false;
		myPlants = new ArrayList<PowerPlant>();
		myCities = new ArrayList<City>();
		coalInv = 0;
		oilInv = 0;
		garbageInv = 0;
		uraniumInv = 0;
		
		// Power Plant selection setup
		ppCheckboxes = new JCheckBox[MAX_POWER_PLANTS];
		for (int i = 0; i < MAX_POWER_PLANTS; i++) {
			ppCheckboxes[i] = new JCheckBox();
		}
		payMeButton = new JButton("PayMe");
		payMeButton.setPreferredSize(new Dimension(50, 30));
		payMeButton.addActionListener(payMeListener);
		payMeButton.setMnemonic(KeyEvent.VK_P);
		dictionary = new HashMap<String, Integer>();
		dictionary.put("Coal", 0);
		dictionary.put("Oil", 1);
		dictionary.put("Hybrid", 2);
		dictionary.put("Garbage", 3);
		dictionary.put("Uranium", 4);
	}

	// Access methods
	public String getColor() {
		return color;
	}
	public Color getPaintColor() {
		return paintColor;
	}
	public int getPlayerNo() {
		return playerNo;
	}
	public int getElectroCount() {
		return electroCount;
	}
	public int getCityCount() {
		return myCities.size();
	}
	public ArrayList<PowerPlant> getMyPlants() {
		return myPlants;
	}
	public ArrayList<City> getMyCities() {
		return myCities;
	}
	public boolean isEligibleToBid() {
		return bidEligible;
	}
	public void setEligibleToBid(boolean NewValue) {
		bidEligible = NewValue;
		return;
	}
	public boolean isBot() {
		return botInd;
	}
	public int getResourceBotRule() {
		return resourceBotRule;
	}
	public int getBuildBotRule() {
		return buildBotRule;
	}
	public boolean getPaidInd() {
		return paidInd;
	}
	public void setPaidInd(boolean NewValue) {
		paidInd = NewValue;
	}
	public int getMaxPP() {
		int maxPP = 0;
		for (int i = 0; i < myPlants.size(); i++) {
			if (myPlants.get(i).getMinBid() > maxPP) {
				maxPP = myPlants.get(i).getMinBid();
			}
		}
		return maxPP;
	}
	// Return the index of the lowest value Power Plant so it can be removed, for example
	public int getMinPlantNo() {
		int minPP = 99;
		int minPlant = -1;
		for (int i = 0; i < myPlants.size(); i++) {
			if (myPlants.get(i).getMinBid() < minPP) {
				minPP = myPlants.get(i).getMinBid();
				minPlant = i;
			}
		}
		return minPlant;
	}
	public void earnElectros(int Amount) {
		electroCount += Amount;
	}
	public void spendElectros(int Amount) {
		electroCount -= Amount;
	}
	public void addPowerPlant(PowerPlant NewPlant) {
		myPlants.add(NewPlant);
	}
	public int getCoalInventory() {
		return coalInv;
	}
	public int getOilInventory() {
		return oilInv;
	}
	public int getGarbageInventory() {
		return garbageInv;
	}
	public int getUraniumInventory() {
		return uraniumInv;
	}
	public int increaseCoalInventory (int IncrAmt) {
		coalInv += IncrAmt;
		return coalInv;
	}
	public int increaseOilInventory (int IncrAmt) {
		oilInv += IncrAmt;
		return oilInv;
	}
	public int increaseGarbageInventory (int IncrAmt) {
		garbageInv += IncrAmt;
		return garbageInv;
	}
	public int increaseUraniumInventory (int IncrAmt) {
		uraniumInv += IncrAmt;
		return uraniumInv;
	}
	public int getCapacity(String FuelType) {
		int cap = 0;
		for (int i = 0; i < myPlants.size(); i++) {
			if (myPlants.get(i).getFuelType().equals(FuelType)) {
				cap += myPlants.get(i).getResourceCount();
			}
		}
		return 2*cap;	// Player can store twice the resources required to power cities for one turn
	}
	public int getHybridCapacity() {
		int cap = 0;
		for (int i = 0; i < myPlants.size(); i++) {
			String fuelType = myPlants.get(i).getFuelType();
			if (fuelType.equals("Hybrid")) {
				cap += myPlants.get(i).getResourceCount();
			}
		}
		return 2*cap;	// Player can store twice the resources required to power cities for one turn
	}
	public void addCity(City NewCity) {
		myCities.add(NewCity);
	}
	public PowerPlant getMostCitiesPP() {
		int maxCity = 0;
		PowerPlant retVal = null;
		for (int i = 0; i < myPlants.size(); i++) {
			if (myPlants.get(i).getCitiesPowered() > maxCity) {
				maxCity = myPlants.get(i).getCitiesPowered();
				retVal = myPlants.get(i);
			}
		}
		return retVal;
	}
	public PowerPlant getFewestCitiesPP() {
		int minCity = 99;
		PowerPlant retVal = null;
		for (int i = 0; i < myPlants.size(); i++) {
			if (myPlants.get(i).getCitiesPowered() < minCity) {
				minCity = myPlants.get(i).getCitiesPowered();
				retVal = myPlants.get(i);
			}
		}
		return retVal;
	}
	public PowerPlant getPPexcept(ArrayList<PowerPlant> ExclusionList) {
		boolean found = false;
		PowerPlant retVal = null;
		for (int i = 0; i < myPlants.size(); i++) {
			found = false;
			retVal = myPlants.get(i);
			for (int j = 0; j < ExclusionList.size(); j++) {
				if (myPlants.get(i).equals(ExclusionList.get(j))) {
					found = true;
				}
			}
			if (!found) {
				return retVal;
			}
		}
		// TODO: Get rid of this testing code
		// Warning: getPPexcept is about to return null, which should not happen
		String inputList = "";
		for (int i = 0; i < ExclusionList.size(); i++) {
			inputList += ExclusionList.get(i).getMinBid() + " ";
		}
		JOptionPane.showMessageDialog(panel,
			    "Warning: getPPexcept() is about to return null, which should not happen.  Input PPs were " + inputList);
		return null;
	}
	
	/**
	 * This routine determines the lowest cost connection available to the player and returns the city
	 * pair that make up that connection.  This has to ensure the player does not already have a plant
	 * in the proposed new destination
	 * <p>
	 * @return ArrayList<City> which contains the origin and destination of the lowest cost connection 
	 * 			available for the player (each of city pair will be null if nothing is available)
	 */
	public ArrayList<City> getLowestCostConnection() {
		// TODO: Should allow for multiple connections having the same cost and randomly choosing
		City curOriginCity, originCity = null, curDestCity, destCity = null;
		ArrayList<City> retVal = new ArrayList<City>();
		int lowestCost = 999, curConnCost, curCityCost;
		
		// First loop through all of my cities
		for (int i = 0; i < myCities.size(); i++) {
			curOriginCity = myCities.get(i);
			// Now look at each connection for each of my cities (making sure the connected city is available)
			for (int j = 0; j < curOriginCity.getConnections().size(); j++) {
				if (curOriginCity.getConnections().get(j).isAvailable()) {
					boolean alreadyInCity = false;
					curDestCity = curOriginCity.getConnections().get(j);
					for (int k = 0; k < 3; k++) {
						try {
							if (curDestCity.getButton(k).getBackground() == paintColor) {
								alreadyInCity = true;
								// destCity = null;
							}
						} catch (NullPointerException e) {
				            System.out.print("Caught the NullPointerException; k is " + k + " and j is " + j + "\n");
				            System.out.print("Current origin city is " + curOriginCity.getName() + "\n");
				            System.out.print("Current connection is " + curOriginCity.getConnections().get(j).getName() + "\n");
				            System.out.print("First button had background color of (via connection) " + curOriginCity.getConnections().get(j).getButton(k).getBackground() + "\n");
				            System.out.print("First button had background color of (via destCity) " + destCity.getButton(0).getBackground() + "\n");
				            System.out.print("This will fail: destination is " + destCity.getName());
				        }
					}
					// If I'm not already in the connected city, then we can check whether it is lower cost than previously found
					if (!alreadyInCity) {
						curConnCost = curOriginCity.getConnectionCosts().get(j);
						curCityCost = curOriginCity.getConnections().get(j).getCurrentCost();
						if (curCityCost >= 0) {
							if (curConnCost + curCityCost < lowestCost) {
								lowestCost = curConnCost + curCityCost;
								originCity = curOriginCity;
								destCity = curDestCity;
							}
						}
					}
				}
			}
		}
		retVal.add(originCity);
		retVal.add(destCity);
		return retVal;
	}
	
	public void determineBotRules() {
		// Rules for buying resources
		// Rule 1 is normal production plus any under 5 Electros TODO: Stop changing this to 2
		// Rule 2 is all resources
		// Rule 3 is all resources if last, else normal production
		// Rule 4 is normal production
		// Rule 5 is normal production and least available resources (1 uranium = 3) where tie equals don't buy TODO: Stop changing this to 2
		// Rule 6 is odd turn: normal production; even turn: all resources
		resourceBotRule = random.nextInt(6) + 1;
		if (resourceBotRule == 1 | resourceBotRule == 5) {
			resourceBotRule = 2;
		}
				
		// TODO: Add other rules for Building
		// Rule 5 is build 1 in Step 1; 2 in Step 2 and 3 in Step 3
		// Rule 6 is build all cities
		buildBotRule = random.nextInt(2) + 5;
	}

	public void autoBuyResources(int PurchaseType) {
		int coalNorm = 0, oilNorm = 0, garbageNorm = 0, uraniumNorm = 0;
		PowerPlant firstPP = null, secondPP = null, thirdPP = null;
		switch (PurchaseType) {
			case MainWindow.RSC_NORM_PROD:
				firstPP = getMostCitiesPP();
				switch (firstPP.getFuelType()) {
				case "Coal":
					coalNorm += firstPP.getResourceCount();
					break;
				case "Oil":
					oilNorm += firstPP.getResourceCount();
					break;
				case "Garbage":
					garbageNorm += firstPP.getResourceCount();
					break;
				case "Uranium":
					uraniumNorm += firstPP.getResourceCount();
					break;
				}
				buyResourcesForPP(firstPP, firstPP.getResourceCount());
				if (myPlants.size() == 2) {
					secondPP = getFewestCitiesPP();
					switch (secondPP.getFuelType()) {
					case "Coal":
						coalNorm += secondPP.getResourceCount();
						buyResourcesForPP(secondPP, coalNorm);
						break;
					case "Oil":
						oilNorm += secondPP.getResourceCount();
						buyResourcesForPP(secondPP, oilNorm);
						break;
					case "Garbage":
						garbageNorm += secondPP.getResourceCount();
						buyResourcesForPP(secondPP, garbageNorm);
						break;
					case "Uranium":
						uraniumNorm += secondPP.getResourceCount();
						buyResourcesForPP(secondPP, uraniumNorm);
						break;
					default:
						// Hybrid or Green - do nothing for now
						break;
					}
				} else if (myPlants.size() == 3) {
					thirdPP = getFewestCitiesPP();
					ArrayList<PowerPlant> exclPPs = new ArrayList<PowerPlant>();
					exclPPs.add(firstPP);
					exclPPs.add(thirdPP);
					secondPP = getPPexcept(exclPPs);
					// TODO: Figure out how we get a null pointer exception on the next line...
					try {
						switch (secondPP.getFuelType()) {
						case "Coal":
							coalNorm += secondPP.getResourceCount();
							buyResourcesForPP(secondPP, coalNorm);
							break;
						case "Oil":
							oilNorm += secondPP.getResourceCount();
							buyResourcesForPP(secondPP, oilNorm);
							break;
						case "Garbage":
							garbageNorm += secondPP.getResourceCount();
							buyResourcesForPP(secondPP, garbageNorm);
							break;
						case "Uranium":
							uraniumNorm += secondPP.getResourceCount();
							buyResourcesForPP(secondPP, uraniumNorm);
							break;
						default:
							// Hybrid or Green - do nothing for now
							break;
						}
					} catch (NullPointerException e) {
			            System.out.print("Caught the NullPointerException; first excluded PPs was " + firstPP.getMinBid() + "\n");
			            System.out.print("Caught the NullPointerException; second excluded PPs was " + thirdPP.getMinBid() + "\n");
//			            System.out.print("Caught the NullPointerException; second actual PP was " + secondPP.getMinBid() + "\n");
			        }
					switch (thirdPP.getFuelType()) {
					case "Coal":
						coalNorm += thirdPP.getResourceCount();
						buyResourcesForPP(thirdPP, coalNorm);
						break;
					case "Oil":
						oilNorm += thirdPP.getResourceCount();
						buyResourcesForPP(thirdPP, oilNorm);
						break;
					case "Garbage":
						garbageNorm += thirdPP.getResourceCount();
						buyResourcesForPP(thirdPP, garbageNorm);
						break;
					case "Uranium":
						uraniumNorm += thirdPP.getResourceCount();
						buyResourcesForPP(thirdPP, uraniumNorm);
						break;
					default:
						// Hybrid or Green - do nothing for now
						break;
					}
				}
				break;
			case MainWindow.RSC_ALL_RSC:
				autoBuyResources(MainWindow.RSC_NORM_PROD);
				firstPP = getMostCitiesPP();
				switch (firstPP.getFuelType()) {
				case "Coal":
					coalNorm += firstPP.getResourceCount();
					buyResourcesForPP(firstPP, coalNorm);
					break;
				case "Oil":
					oilNorm += firstPP.getResourceCount();
					buyResourcesForPP(firstPP, oilNorm);
					break;
				case "Garbage":
					garbageNorm += firstPP.getResourceCount();
					buyResourcesForPP(firstPP, garbageNorm);
					break;
				case "Uranium":
					uraniumNorm += firstPP.getResourceCount();
					buyResourcesForPP(firstPP, uraniumNorm);
					break;
				default:
					// Hybrid or Green - do nothing for now
					break;
				}
				if (myPlants.size() == 2) {
					secondPP = getFewestCitiesPP();
					switch (secondPP.getFuelType()) {
					case "Coal":
						coalNorm += secondPP.getResourceCount();
						buyResourcesForPP(secondPP, coalNorm);
						break;
					case "Oil":
						oilNorm += secondPP.getResourceCount();
						buyResourcesForPP(secondPP, oilNorm);
						break;
					case "Garbage":
						garbageNorm += secondPP.getResourceCount();
						buyResourcesForPP(secondPP, garbageNorm);
						break;
					case "Uranium":
						uraniumNorm += secondPP.getResourceCount();
						buyResourcesForPP(secondPP, uraniumNorm);
						break;
					default:
						// Hybrid or Green - do nothing for now
						break;
					}
				} else if (myPlants.size() == 3) {
					thirdPP = getFewestCitiesPP();
					ArrayList<PowerPlant> exclPPs = new ArrayList<PowerPlant>();
					exclPPs.add(firstPP);
					exclPPs.add(thirdPP);
					secondPP = getPPexcept(exclPPs);
					switch (secondPP.getFuelType()) {
					case "Coal":
						coalNorm += secondPP.getResourceCount();
						buyResourcesForPP(secondPP, coalNorm);
						break;
					case "Oil":
						oilNorm += secondPP.getResourceCount();
						buyResourcesForPP(secondPP, oilNorm);
						break;
					case "Garbage":
						garbageNorm += secondPP.getResourceCount();
						buyResourcesForPP(secondPP, garbageNorm);
						break;
					case "Uranium":
						uraniumNorm += secondPP.getResourceCount();
						buyResourcesForPP(secondPP, uraniumNorm);
						break;
					default:
						// Hybrid or Green - do nothing for now
						break;
					}
					switch (thirdPP.getFuelType()) {
					case "Coal":
						coalNorm += thirdPP.getResourceCount();
						buyResourcesForPP(thirdPP, coalNorm);
						break;
					case "Oil":
						oilNorm += thirdPP.getResourceCount();
						buyResourcesForPP(thirdPP, oilNorm);
						break;
					case "Garbage":
						garbageNorm += thirdPP.getResourceCount();
						buyResourcesForPP(thirdPP, garbageNorm);
						break;
					case "Uranium":
						uraniumNorm += thirdPP.getResourceCount();
						buyResourcesForPP(thirdPP, uraniumNorm);
						break;
					default:
						// Hybrid or Green - do nothing for now
						break;
					}
				}
				break;
			case MainWindow.RSC_LESS_THAN_5:
				// TODO: implement purchasing resources less than 5 electros
				break;
			case MainWindow.RSC_LEAST_AVAIL:
				// TODO: implement purchasing least available resources
				break;
		}
	}
	public void buyResourcesForPP (PowerPlant CurPlant, int MaxCount) {
		int resCost;
		ResourceMarket rm = gameControl.getResourceMarket();

		switch (CurPlant.getFuelType()) {
		case "Coal":
			for (int i = coalInv; i < MaxCount; i++) {
				JToggleButton nextButton = rm.getNextCoalButton();
				// nextButton can be null if market is empty
				if (nextButton != null) {
					resCost = Integer.parseInt(nextButton.getText());
					if (electroCount >= resCost) {
						nextButton.setEnabled(false);
						nextButton.setBackground(Color.LIGHT_GRAY);
						rm.purchaseCoal(this, resCost);
					} else {
						// Don't bother trying to buy more resources since can't afford
						break;
					}
				}
			}
			break;
		case "Oil":
			for (int i = oilInv; i < MaxCount; i++) {
				JToggleButton nextButton = rm.getNextOilButton();
				// nextButton can be null if market is empty
				if (nextButton != null) {
					resCost = Integer.parseInt(nextButton.getText());
					if (electroCount >= resCost) {
						nextButton.setEnabled(false);
						nextButton.setBackground(Color.LIGHT_GRAY);
						rm.purchaseOil(this, resCost);
					} else {
						// Don't bother trying to buy more resources since can't afford
						break;
					}
				}
			}
			break;
		case "Hybrid":
			if (Integer.parseInt(rm.getNextCoalButton().getText()) <= Integer.parseInt(rm.getNextOilButton().getText())) {
				for (int i = coalInv; i < MaxCount; i++) {
					JToggleButton nextButton = rm.getNextCoalButton();
					// nextButton can be null if market is empty
					if (nextButton != null) {
						resCost = Integer.parseInt(nextButton.getText());
						if (electroCount >= resCost) {
							nextButton.setEnabled(false);
							nextButton.setBackground(Color.LIGHT_GRAY);
							rm.purchaseCoal(this, resCost);
						} else {
							// Don't bother trying to buy more resources since can't afford
							break;
						}
					}
				}
			} else {
				for (int i = oilInv; i < MaxCount; i++) {
					JToggleButton nextButton = rm.getNextOilButton();
					// nextButton can be null if market is empty
					if (nextButton != null) {
						resCost = Integer.parseInt(nextButton.getText());
						if (electroCount >= resCost) {
							nextButton.setEnabled(false);
							nextButton.setBackground(Color.LIGHT_GRAY);
							rm.purchaseOil(this, resCost);
						} else {
							// Don't bother trying to buy more resources since can't afford
							break;
						}
					}
				}
			}
			break;
		case "Garbage":
			for (int i = garbageInv; i < MaxCount; i++) {
				JToggleButton nextButton = rm.getNextGarbageButton();
				// nextButton can be null if market is empty
				if (nextButton != null) {
					resCost = Integer.parseInt(nextButton.getText());
					if (electroCount >= resCost) {
						nextButton.setEnabled(false);
						nextButton.setBackground(Color.LIGHT_GRAY);
						rm.purchaseGarbage(this, resCost);
					} else {
						// Don't bother trying to buy more resources since can't afford
						break;
					}
				}
			}
			break;
		case "Uranium":
			for (int i = uraniumInv; i < MaxCount; i++) {
				JToggleButton nextButton = rm.getNextUraniumButton();
				// nextButton can be null if market is empty
				if (nextButton != null) {
					resCost = Integer.parseInt(nextButton.getText());
					if (electroCount >= resCost) {
						nextButton.setEnabled(false);
						nextButton.setBackground(Color.LIGHT_GRAY);
						rm.purchaseUranium(this, resCost);
					} else {
						// Don't bother trying to buy more resources since can't afford
						break;
					}
				}
			}
			break;
		}
	}
	
	public void showStatus() {
		JPanel statusForm = new JPanel();
		JFrame myFrame = new JFrame("Player Status");
		myFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  
		myFrame.setSize(270, 500);
		myFrame.setVisible(true);
		GridLayout layout = new GridLayout(0, 2);
		Container cp = myFrame.getContentPane();
		JLabel lblColor = new JLabel("Color: ");
		JLabel lblElectros = new JLabel("Electros: ");
		JLabel lblCities = new JLabel("Cities: ");
		JLabel lblCoal = new JLabel("Coal: ");
		JLabel lblOil = new JLabel("Oil: ");
		JLabel lblGarbage = new JLabel("Garbage: ");
		JLabel lblUranium = new JLabel("Uranium: ");
		cp.setLayout(layout);
		cp.add(lblColor);
		cp.add(new JTextField(color));
		cp.add(lblElectros);
		cp.add(new JTextField(electroCount+""));
		cp.add(lblCities);
		cp.add(new JTextField(myCities.size()+""));
		cp.add(lblCoal);
		cp.add(new JTextField(coalInv+""));
		cp.add(lblOil);
		cp.add(new JTextField(oilInv+""));
		cp.add(lblGarbage);
		cp.add(new JTextField(garbageInv+""));
		cp.add(lblUranium);
		cp.add(new JTextField(uraniumInv+""));
		
		for (int i = 0; i < myPlants.size(); i++) {
			JLabel lblCards = new JLabel("Card " + (i+1) + ": ");
			JLabel lblNull = new JLabel(" ");
			JLabel lblMinBid = new JLabel("Power Plant #: ");
			JTextField txtMinBid = new JTextField(myPlants.get(i).getMinBid()+"");
			JLabel lblRsrcTyp = new JLabel("Resource Type: ");
			JTextField txtRsrcTyp = new JTextField(myPlants.get(i).getFuelType());
			JLabel lblRsrcAmt = new JLabel("Resource Amount: ");
			JTextField txtRsrcAmt = new JTextField(myPlants.get(i).getResourceCount()+"");
			JLabel lblCitiesPow = new JLabel("Cities Powered: ");
			JTextField txtCitiesPow = new JTextField(myPlants.get(i).getCitiesPowered()+"");
			
			cp.add(lblCards);
			cp.add(lblNull);
			cp.add(lblMinBid);
			cp.add(txtMinBid);
			cp.add(lblRsrcTyp);
			cp.add(txtRsrcTyp);
			cp.add(lblRsrcAmt);
			cp.add(txtRsrcAmt);
			cp.add(lblCitiesPow);
			cp.add(txtCitiesPow);
		}
		// If I'm a bot, show my rules
		if (botInd) {
			JLabel lblBotRules = new JLabel("Bot Rules ");
			JLabel lblNull = new JLabel(" ");
			JLabel lblBotResRule = new JLabel("Resource Rule: ");
			JTextField txtBotResRule = new JTextField(resourceBotRule + "");
			JLabel lblBotBldRule = new JLabel("Build Rule: ");
			JTextField txtBotBldRule = new JTextField(buildBotRule + "");
			cp.add(lblBotRules);
			cp.add(lblNull);
			cp.add(lblBotResRule);
			cp.add(txtBotResRule);
			cp.add(lblBotBldRule);
			cp.add(txtBotBldRule);
		}
		// Add the close button
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(closeListener);
		closeButton.setMnemonic(KeyEvent.VK_C);
		cp.add(closeButton);
	}

	public void selectPPs() {
		JPanel selectPPForm = new JPanel();
		myFrame = new JFrame("Select Power Plants");
		myFrame.setContentPane(selectPPForm);
		myFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  
		myFrame.setSize(300,250);
		myFrame.setVisible(true);
		GridLayout layout = new GridLayout(0, 1);
		myFrame.setLayout(layout);
		Container cp = myFrame.getContentPane();
		
		JLabel foo = new JLabel("Power up to " + myCities.size() + " cities");
		cp.add(foo);
		for (int i = 0; i < MAX_POWER_PLANTS; i++) {
			ppCheckboxes[i] = null;
		}
		for (int i = 0; i < myPlants.size(); i++ ) {
			PowerPlant curPP = myPlants.get(i);
			int ppNo = curPP.getMinBid();
			int resReq = curPP.getResourceCount();
			String fType = curPP.getFuelType();
			int cPow = curPP.getCitiesPowered();

			ppCheckboxes[i] = new JCheckBox();
			ppCheckboxes[i].setText("PP #" + ppNo + " - " + resReq + " " + fType + " to power " + cPow + " cities");
			ppCheckboxes[i].setVisible(true);
			cp.add(ppCheckboxes[i]);
		}
		cp.add(payMeButton);
		payMeButton.setBackground(paintColor);
		payMeButton.setOpaque(true);
	}
	
	public void closeWindow() {
		myFrame.dispose();
	}

	// Action listener for the close button
	ActionListener closeListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof JButton) {
            	closeWindow();
            }
        }
	};

	// Create an ActionListener for completing the selection of Power Plants
	// This routine needs to confirm there are enough resources to power the selected plants before 
	// moving on to pay the player for the powered cities
	ActionListener payMeListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
        	Integer reqResources[];
        	reqResources = new Integer[NUM_RESOURCE_TYPES];
        	int poweredCities = 0;
        	
        	for (int i = 0; i < NUM_RESOURCE_TYPES; i++) {
        		reqResources[i] = 0;
        	}
            if (e.getSource() instanceof JButton) {
        		// Figure out whether the player has resources to power the selected cities
            	// If so, pay him; if not, make him try again
            	for (int i = 0; i < myPlants.size(); i++) {
            		if (ppCheckboxes[i].isSelected()) {
            			if (myPlants.get(i).getFuelType() != "Green") {
            				reqResources[dictionary.get(myPlants.get(i).getFuelType())] += myPlants.get(i).getResourceCount();
            			}
                		poweredCities += myPlants.get(i).getCitiesPowered();
            		}
            	}
            	if (coalInv < reqResources[dictionary.get("Coal")]) {
        			// Dialog box that the player does not have enough coal
            		JOptionPane.showMessageDialog(panel,
            			    "Can't power selected plants: required coal is " + reqResources[dictionary.get("Coal")] + "; inventory is " + coalInv);
            		return;
            	}
            	if (oilInv < reqResources[dictionary.get("Oil")]) {
        			// Dialog box that the player does not have enough oil
            		JOptionPane.showMessageDialog(panel,
            			    "Can't power selected plants: required oil is " + reqResources[dictionary.get("Oil")] + "; inventory is " + oilInv);
            		return;
            	}
            	if (coalInv + oilInv < (reqResources[dictionary.get("Hybrid")] + reqResources[dictionary.get("Coal")] + reqResources[dictionary.get("Oil")])) {
        			// Dialog box that the player does not have enough coal and oil
            		JOptionPane.showMessageDialog(panel,
            			    "Can't power selected plants: required coal is " + reqResources[dictionary.get("Coal")] +
            			    "oil is " + reqResources[dictionary.get("Oil")] + 
            			    "hybrid is " + reqResources[dictionary.get("Hybrid")] + "; total inventory is " + (coalInv + oilInv));
            		return;
            	}
            	if (garbageInv < reqResources[dictionary.get("Garbage")]) {
        			// Dialog box that the player does not have enough garbage
            		JOptionPane.showMessageDialog(panel,
            			    "Can't power selected plants: required garbage is " + reqResources[dictionary.get("Garbage")] + "; inventory is " + garbageInv);
            		return;
            	}
            	if (uraniumInv < reqResources[dictionary.get("Uranium")]) {
        			// Dialog box that the player does not have enough uranium
            		JOptionPane.showMessageDialog(panel,
            			    "Can't power selected plants: required uranium is " + reqResources[dictionary.get("Uranium")] + "; inventory is " + uraniumInv);
            		return;
            	}
            	// Enough resources exist, so reduce the resources and pay the player
            	coalInv -= reqResources[dictionary.get("Coal")];
            	oilInv -= reqResources[dictionary.get("Oil")];
            	if (oilInv > coalInv) {
            		if (oilInv >= reqResources[dictionary.get("Hybrid")]) {
            			oilInv -= reqResources[dictionary.get("Hybrid")];
            		} else {
            			reqResources[dictionary.get("Hybrid")] -= oilInv;
            			oilInv = 0;
            			coalInv -= reqResources[dictionary.get("Hybrid")];
            		}
            	} else {
            		if (coalInv >= reqResources[dictionary.get("Hybrid")]) {
            			coalInv -= reqResources[dictionary.get("Hybrid")];
            		} else {
            			reqResources[dictionary.get("Hybrid")] -= coalInv;
            			coalInv = 0;
            			oilInv -= reqResources[dictionary.get("Hybrid")];
            		}
            	}
            	garbageInv -= reqResources[dictionary.get("Garbage")];
            	uraniumInv -= reqResources[dictionary.get("Uranium")];
            	
            	if (poweredCities > myCities.size()) {
            		poweredCities = myCities.size();
            	}
            	paidInd = true;
            	earnElectros(cityPayoffs[poweredCities]);
            	closeWindow();
            }
        }
    };
    
	@Override
	public int compareTo(Player CompPlayer) {
		// TODO Auto-generated method stub
		int otherCities = CompPlayer.getCityCount();
		if (this.getCityCount() == otherCities) {
			int otherMaxPP = CompPlayer.getMaxPP();
			return otherMaxPP - this.getMaxPP();
		} else {
			return otherCities - this.getCityCount();
		}
	}
}
