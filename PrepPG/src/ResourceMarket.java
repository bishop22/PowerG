import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import java.util.*;

public class ResourceMarket {

	private static final int MAX_COAL = 24;				// Max number of resources in game
	private static final int MAX_OIL = 24;
	private static final int MAX_GARBAGE = 24;
	private static final int MAX_URANIUM = 12;
	private static final int coalRestock[] = {7, 9, 9};		// Amount of resources to restock in Steps 1, 2 and 3
	private static final int oilRestock[] = {5, 6, 7};		// This actually depends on number of players
	private static final int garbageRestock[] = {3, 5, 6};
	private static final int uraniumRestock[] = {2, 3, 3};

	private Container pane;
	private MainWindow gameControl;
	private GridBagConstraints c;
	private JTextField lblCoal, lblOil, lblGarbage, lblUranium;
	private List<JToggleButton> coalBox, oilBox, garbageBox, uraniumBox;

	// Document the state for the beginning of the game
	int marketCoalBal = 24;
	int marketOilBal = 18;
	int marketGarbageBal = 12;
	int marketUraniumBal = 2;
	
	boolean coalAvail[] = {true, true, true, 		true, true, true,
			true, true, true, 		true, true, true,
			true, true, true, 		true, true, true,
			true, true, true, 		true, true, true
	};
	boolean oilAvail[] = {false, false, false, 		false, false, false,
			true, true, true, 		true, true, true,
			true, true, true, 		true, true, true,
			true, true, true, 		true, true, true
	};
	boolean garbageAvail[] = {false, false, false, 		false, false, false,
			false, false, false, 	false, false, false,
			true, true, true, 		true, true, true,
			true, true, true, 		true, true, true
	};
	boolean uraniumAvail[] = {false, false, false, false,
			false, false, false, false,
			false, false, true, true
	};
	
	public ResourceMarket(int MapX, int MapY, Container Pane, MainWindow GameControl) {
		pane = Pane;
		gameControl = GameControl;
		
		// Set up a GridBagConstraints object for the city components to use to be plotted correctly
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;

		// Build and place the coal market
	    lblCoal = new JTextField("Coal");
	    lblCoal.setEditable(false);
	    c.gridx = 0;
	    c.gridy = 5*MapY + 1;
	    Pane.add(lblCoal, c);

		// Build and place the oil market
	    lblOil = new JTextField("Oil");
	    lblOil.setEditable(false);
	    c.gridy = 5*MapY + 2;
	    Pane.add(lblOil, c);

		// Build and place the garbage market
	    lblGarbage = new JTextField("Garbage");
	    lblGarbage.setEditable(false);
	    c.gridy = 5*MapY + 3;
	    Pane.add(lblGarbage, c);

		// Build and place the uranium market
	    lblUranium = new JTextField("Uranium");
	    lblUranium.setEditable(false);
	    c.gridy = 5*MapY + 4;
	    Pane.add(lblUranium, c);
	    
	    coalBox = new ArrayList<JToggleButton>();
	    oilBox = new ArrayList<JToggleButton>();
	    garbageBox = new ArrayList<JToggleButton>();
	    uraniumBox = new ArrayList<JToggleButton>();

	    String labels[] = {"1", "1", "1", "2", "2", "2",
	    		"3", "3", "3", "4", "4", "4",
	    		"5", "5", "5", "6", "6", "6",
	    		"7", "7", "7", "8", "8", "8"};
	    String labelsU[] = {"1", "2", "3", "4", "5", "6", "7", "8", "10", "12", "14", "16"};
		
	    for (int i = 0; i < labels.length; i++) {
	    	coalBox.add(new JToggleButton(labels[i], !coalAvail[i]));
	        coalBox.get(i).addActionListener(coalListener);
	    	coalBox.get(i).setVisible(coalAvail[i]);
	    	coalBox.get(i).setFont(new Font("Arial", Font.PLAIN, 10));
	    	coalBox.get(i).setBackground(Color.GRAY);
	    	coalBox.get(i).setForeground(Color.white);
	    	coalBox.get(i).setOpaque(true);
	        c.gridx = i + 1;
		    c.gridy = 5*MapY + 1;
		    pane.add(coalBox.get(i), c);
	    }
	    for (int i = 0; i < labels.length; i++) {
	    	oilBox.add(new JToggleButton(labels[i], !oilAvail[i]));
	        oilBox.get(i).addActionListener(oilListener);
	    	oilBox.get(i).setVisible(oilAvail[i]);
	    	oilBox.get(i).setFont(new Font("Arial", Font.PLAIN, 10));
	    	oilBox.get(i).setBackground(Color.BLACK);
	    	oilBox.get(i).setForeground(Color.white);
	    	oilBox.get(i).setOpaque(true);
	        c.gridx = i + 1;
		    c.gridy = 5*MapY + 2;
		    Pane.add(oilBox.get(i), c);
	    }
	    for (int i = 0; i < labels.length; i++) {
	    	garbageBox.add(new JToggleButton(labels[i], !garbageAvail[i]));
	    	garbageBox.get(i).addActionListener(garbageListener);
	    	garbageBox.get(i).setVisible(garbageAvail[i]);
	    	garbageBox.get(i).setFont(new Font("Arial", Font.PLAIN, 10));
	    	garbageBox.get(i).setBackground(Color.YELLOW
	    			);
	    	garbageBox.get(i).setOpaque(true);
	        c.gridx = i + 1;
		    c.gridy = 5*MapY + 3;
		    Pane.add(garbageBox.get(i), c);
	    }
	    for (int i = 0; i < labelsU.length; i++) {
	    	uraniumBox.add(new JToggleButton(labelsU[i], !uraniumAvail[i]));
	    	uraniumBox.get(i).addActionListener(uraniumListener);
	    	uraniumBox.get(i).setVisible(uraniumAvail[i]);
	    	uraniumBox.get(i).setFont(new Font("Arial", Font.PLAIN, 10));
	    	uraniumBox.get(i).setBackground(Color.RED);
	    	uraniumBox.get(i).setForeground(Color.white);
	    	uraniumBox.get(i).setOpaque(true);
	        c.gridx = i + 1;
		    c.gridy = 5*MapY + 4;
		    Pane.add(uraniumBox.get(i), c);
	    }
	}
	
	public void updateMarket() {
		for (int i = 0; i < MAX_COAL; i++) {
			// If the button number is at least as high as MAX_COAL minus coal balance, make the button visible and not selected
			// Otherwise it will be not visible (and selected)
			boolean enable = (i < (MAX_COAL - marketCoalBal));
			coalBox.get(i).setVisible(!enable);
			coalBox.get(i).setBackground(Color.LIGHT_GRAY);
			coalBox.get(i).setEnabled(!enable);
			coalBox.get(i).setSelected(enable);
		}
		for (int i = 0; i < MAX_OIL; i++) {
			boolean enable = (i < (MAX_OIL - marketOilBal));
			oilBox.get(i).setVisible(!enable);
			oilBox.get(i).setBackground(Color.BLACK);
			oilBox.get(i).setEnabled(!enable);
			oilBox.get(i).setSelected(enable);
		}
		for (int i = 0; i < MAX_GARBAGE; i++) {
			boolean enable = (i < (MAX_GARBAGE - marketGarbageBal));
			garbageBox.get(i).setVisible(!enable);
			garbageBox.get(i).setBackground(Color.YELLOW);
			garbageBox.get(i).setEnabled(!enable);
			garbageBox.get(i).setSelected(enable);
		}
		for (int i = 0; i < MAX_URANIUM; i++) {
			boolean enable = (i < (MAX_URANIUM - marketUraniumBal));
			uraniumBox.get(i).setVisible(!enable);
			uraniumBox.get(i).setBackground(Color.RED);
			uraniumBox.get(i).setEnabled(!enable);
			uraniumBox.get(i).setSelected(enable);
		}
	}

	public void purchaseCoal(Player CurPlayer, int ResCost) {
		CurPlayer.spendElectros(ResCost);
		CurPlayer.increaseCoalInventory(1);
		marketCoalBal--;
	}
	public void purchaseOil(Player CurPlayer, int ResCost) {
		CurPlayer.spendElectros(ResCost);
		CurPlayer.increaseOilInventory(1);
		marketOilBal--;
	}
	public void purchaseGarbage(Player CurPlayer, int ResCost) {
		CurPlayer.spendElectros(ResCost);
		CurPlayer.increaseGarbageInventory(1);
		marketGarbageBal--;
	}
	public void purchaseUranium(Player CurPlayer, int ResCost) {
		CurPlayer.spendElectros(ResCost);
		CurPlayer.increaseUraniumInventory(1);
		marketUraniumBal--;
	}
	
	public JToggleButton getNextCoalButton() {
		if (marketCoalBal == 0) {
			return null;
		}
		return coalBox.get(MAX_COAL - marketCoalBal);
	}
	public JToggleButton getNextOilButton() {
		if (marketOilBal == 0) {
			return null;
		}
		return oilBox.get(MAX_OIL - marketOilBal);
	}
	public JToggleButton getNextGarbageButton() {
		if (marketGarbageBal == 0) {
			return null;
		}
		return garbageBox.get(MAX_GARBAGE - marketGarbageBal);
	}
	public JToggleButton getNextUraniumButton() {
		if (marketUraniumBal == 0) {
			return null;
		}
		return uraniumBox.get(MAX_URANIUM - marketUraniumBal);
	}
	
	/**
	 * Restocks the resource market before starting the next round of play.  It checks
	 * whether there are enough free resources to do a full restock based in the current
	 * Step, and if not, sets the available amount to restock.
	 */
	public void restockMarket() {
		Player curPlayer;
		int totCoal = 0, totOil = 0, totGarbage = 0, totUranium = 0;
		int restockAmt;
		
		for (int i = 0; i < gameControl.getPlayers().size(); i++) {
			curPlayer = gameControl.getPlayers().get(i);
			totCoal += curPlayer.getCoalInventory();
			totOil += curPlayer.getOilInventory();
			totGarbage += curPlayer.getGarbageInventory();
			totUranium += curPlayer.getUraniumInventory();
		}
		if (marketCoalBal + totCoal + coalRestock[gameControl.getStep()-1] > MAX_COAL) {
			restockAmt = MAX_COAL - marketCoalBal - totCoal;
		} else {
			restockAmt = coalRestock[gameControl.getStep()-1];
		}
		marketCoalBal += restockAmt;
		if (marketOilBal + totOil + oilRestock[gameControl.getStep()-1] > MAX_OIL) {
			restockAmt = MAX_OIL - marketOilBal - totOil;
		} else {
			restockAmt = oilRestock[gameControl.getStep()-1];
		}
		marketOilBal += restockAmt;
		if (marketGarbageBal + totGarbage + garbageRestock[gameControl.getStep()-1] > MAX_GARBAGE) {
			restockAmt = MAX_GARBAGE - marketGarbageBal - totGarbage;
		} else {
			restockAmt = garbageRestock[gameControl.getStep()-1];
		}
		marketGarbageBal += restockAmt;
		if (marketUraniumBal + totUranium + uraniumRestock[gameControl.getStep()-1] > MAX_URANIUM) {
			restockAmt = MAX_URANIUM - marketUraniumBal - totUranium;
		} else {
			restockAmt = uraniumRestock[gameControl.getStep()-1];
		}
		marketUraniumBal += restockAmt;
		
		updateMarket();
	}

	// Create a ActionListeners for each of the resources
	ActionListener coalListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof JToggleButton) {
            	if (gameControl.getPhase() != 3) {
            		JOptionPane.showMessageDialog(gameControl.getContentPane(),
            			    "You may only select resources during Phase 3 - Purchase Resources.");
    				((AbstractButton) e.getSource()).setSelected(false);
            		return;
            	}
            	// Figure out which button was pushed, then verify the player can afford the purchase and has space for the purchase
            	// Then disable it and call the purchase routine for this material
        		int resCost = Integer.parseInt(((AbstractButton) e.getSource()).getText());
        		Player curPlayer = gameControl.getTurnPlayer();
        		int cap = curPlayer.getCapacity("Coal");
        		int inv = curPlayer.getCoalInventory();
        		if (inv >= cap) {
        			cap += curPlayer.getHybridCapacity();
        			if (curPlayer.getOilInventory() > curPlayer.getCapacity("Oil")) {
        				inv += (curPlayer.getOilInventory() - curPlayer.getCapacity("Oil"));
        			}
        			if (inv >= cap) {
        				// Message that can't hold more Coal
                		JOptionPane.showMessageDialog(gameControl.getContentPane(),
                			    "You have attempted to purchase beyond your capacity for Coal.");
        				((AbstractButton) e.getSource()).setSelected(false);
        				return;
        			}
        		}
        		int funds = curPlayer.getElectroCount();
        		if (resCost > funds) {
        			// Display message that they can't make the purchase
            		JOptionPane.showMessageDialog(gameControl.getContentPane(),
            			    "You don't have sufficient funds to purchase this Coal.");
    				((AbstractButton) e.getSource()).setSelected(false);
            		return;
        		} else {
        			((AbstractButton) e.getSource()).setEnabled(false);
        			purchaseCoal(curPlayer, resCost);
        		}
           }
        }
    };

	ActionListener oilListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof JToggleButton) {
            	if (gameControl.getPhase() != 3) {
            		JOptionPane.showMessageDialog(gameControl.getContentPane(),
            			    "You may only select resources during Phase 3 - Purchase Resources.");
    				((AbstractButton) e.getSource()).setSelected(false);
            		return;
            	}
            	// Figure out which button was pushed, then verify the player can afford the purchase and has space for the purchase
            	// Then disable it and call the purchase routine for this material
        		int resCost = Integer.parseInt(((AbstractButton) e.getSource()).getText());
        		Player curPlayer = gameControl.getTurnPlayer();
        		int cap = curPlayer.getCapacity("Oil");
        		int inv = curPlayer.getOilInventory();
        		if (inv >= cap) {
        			cap += curPlayer.getHybridCapacity();
        			if (curPlayer.getCoalInventory() > curPlayer.getCapacity("Coal")) {
        				inv += (curPlayer.getCoalInventory() - curPlayer.getCapacity("Coal"));
        			}
        			if (inv >= cap) {
        				// Message that can't hold more Oil
                		JOptionPane.showMessageDialog(gameControl.getContentPane(),
                			    "You have attempted to purchase beyond your capacity for Oil.");
        				((AbstractButton) e.getSource()).setSelected(false);
        				return;
        			}
        		}
        		int funds = curPlayer.getElectroCount();
        		if (resCost > funds) {
        			// Display message that they can't make the purchase
            		JOptionPane.showMessageDialog(gameControl.getContentPane(),
            			    "You don't have sufficient funds to purchase this Oil.");
    				((AbstractButton) e.getSource()).setSelected(false);
            		return;
        		} else {
        			((AbstractButton) e.getSource()).setEnabled(false);
        			purchaseOil(curPlayer, resCost);
        		}
           }
        }
    };
    
	ActionListener garbageListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof JToggleButton) {
            	if (gameControl.getPhase() != 3) {
            		JOptionPane.showMessageDialog(gameControl.getContentPane(),
            			    "You may only select resources during Phase 3 - Purchase Resources.");
    				((AbstractButton) e.getSource()).setSelected(false);
            		return;
            	}
            	// Figure out which button was pushed, then verify the player can afford the purchase and has space for the purchase
            	// Then disable it and call the purchase routine for this material
        		int resCost = Integer.parseInt(((AbstractButton) e.getSource()).getText());
        		Player curPlayer = gameControl.getTurnPlayer();
        		int cap = curPlayer.getCapacity("Garbage");
        		int inv = curPlayer.getGarbageInventory();
        		if (inv >= cap) {
    				// Message that can't hold more Garbage
            		JOptionPane.showMessageDialog(gameControl.getContentPane(),
            			    "You have attempted to purchase beyond your capacity for Garbage.");
    				((AbstractButton) e.getSource()).setSelected(false);
    				return;
        		}
        		int funds = curPlayer.getElectroCount();
        		if (resCost > funds) {
        			// Display message that they can't make the purchase
            		JOptionPane.showMessageDialog(gameControl.getContentPane(),
            			    "You don't have sufficient funds to purchase this Garbage.");
    				((AbstractButton) e.getSource()).setSelected(false);
            		return;
        		} else {
        			((AbstractButton) e.getSource()).setEnabled(false);
        			purchaseGarbage(curPlayer, resCost);
        		}
           }
        }
    };
    
	ActionListener uraniumListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof JToggleButton) {
            	if (gameControl.getPhase() != 3) {
            		JOptionPane.showMessageDialog(gameControl.getContentPane(),
            			    "You may only select resources during Phase 3 - Purchase Resources.");
    				((AbstractButton) e.getSource()).setSelected(false);
            		return;
            	}
            	// Figure out which button was pushed, then verify the player can afford the purchase and has space for the purchase
            	// Then disable it and call the purchase routine for this material
        		int resCost = Integer.parseInt(((AbstractButton) e.getSource()).getText());
        		Player curPlayer = gameControl.getTurnPlayer();
        		int cap = curPlayer.getCapacity("Uranium");
        		int inv = curPlayer.getUraniumInventory();
        		if (inv >= cap) {
    				// Message that can't hold more Uranium
            		JOptionPane.showMessageDialog(gameControl.getContentPane(),
            			    "You have attempted to purchase beyond your capacity for Uranium.");
    				((AbstractButton) e.getSource()).setSelected(false);
    				return;
        		}
        		int funds = curPlayer.getElectroCount();
        		if (resCost > funds) {
        			// Display message that they can't make the purchase
            		JOptionPane.showMessageDialog(gameControl.getContentPane(),
            			    "You don't have sufficient funds to purchase this Uranium.");
    				((AbstractButton) e.getSource()).setSelected(false);
            		return;
        		} else {
        			((AbstractButton) e.getSource()).setEnabled(false);
        			purchaseUranium(curPlayer, resCost);
        		}
           }
        }
    };
}
