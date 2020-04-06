import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class AuctionPanel extends JPanel {

	private static final long serialVersionUID = 88L;
	private static final int MAX_POWER_PLANTS = 3;
	private JFrame myFrame;
	private Container cp;
	private GridBagConstraints c;
	private PowerPlant auctionPP;
	private MainWindow gameControl;
	private JTextField currentBid;
	private JTextField bidColor;

	// Create a generic ActionListener for the bids; it counts on the button text having the color of the player that bid
	ActionListener bidListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof JButton) {
            	// Figure out which button was pushed, and call the routine to increase the bid and update the leader
        		String bidder = ((AbstractButton) e.getSource()).getText();
        		addNewBid(bidder);
           }
        }
    };

	// Create an ActionListener for ending the auction; it will just call the routine that handles all of the paperwork
	ActionListener endListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof JButton) {
        		endAuction();
           }
        }
    };

	public AuctionPanel(PowerPlant AuctionPP, MainWindow GameControl) {
		// TODO Auto-generated constructor stub
		auctionPP = AuctionPP;
		gameControl = GameControl;
		
		//Create a scrollbar using JScrollPane and add panel into it's viewport  
		//Set vertical and horizontal scrollbar always show  
//		JScrollPane scrollBar=new JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		myFrame = new JFrame("PP Auction Window");
//		myFrame.setContentPane(this);
		myFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  
		myFrame.setSize(800,500);
		myFrame.setVisible(true);
		cp = myFrame.getContentPane();
//		cp.add(scrollBar);

		myFrame.setLayout(new GridBagLayout());
//		this.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		showPowerPlant();
		setupPlayers();
//		myFrame.add(this);
	}

	public AuctionPanel(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

	public AuctionPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public AuctionPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public void showPowerPlant() {
		// First we will create a spot to show the current bid and who placed it
		c.gridy = 0;
		JLabel hdrCurBid = new JLabel("Current Bid:");
		JLabel hdrBidPlayer = new JLabel("Player:");
		c.gridx = 0;
		c.gridwidth = 2;
		cp.add(hdrCurBid, c);
		c.gridx = 4;
		cp.add(hdrBidPlayer, c);
		currentBid = new JTextField(auctionPP.getMinBid()+"");
		currentBid.setEditable(false);
		bidColor = new JTextField(gameControl.getPlayers().get(gameControl.getPlayerTurn()).getColor());
		bidColor.setEditable(false);
		c.gridx = 2;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		cp.add(currentBid, c);
		c.gridx = 6;
		cp.add(bidColor, c);

		// Add the headers for the PP info
		JLabel hdrMinBid = new JLabel("Min Bid");
		JLabel hdrRsrcTyp = new JLabel("Resource Type");
		JLabel hdrRsrcAmt = new JLabel("Resource Amt");
		JLabel hdrCities = new JLabel("Cities Powered");
		JTextField tmpPPinfo = new JTextField("Power Plant info:");
		tmpPPinfo.setEditable(false);
		JTextField fldMinBid = new JTextField(auctionPP.getMinBid()+"");
		fldMinBid.setEditable(false);
		JTextField fldRsrcTyp = new JTextField(auctionPP.getFuelType());
		fldRsrcTyp.setEditable(false);
		JTextField fldRsrcAmt = new JTextField(auctionPP.getResourceCount()+"");
		fldRsrcAmt.setEditable(false);
		JTextField fldCities = new JTextField(auctionPP.getCitiesPowered()+"");
		fldCities.setEditable(false);

		c.gridx = 3;
		c.gridy = 1;
		c.gridwidth = 2;
		cp.add(tmpPPinfo, c);

		c.gridwidth = 1;
		c.gridx = 3;
		c.gridy = 2;
		cp.add(hdrMinBid, c);
		c.gridy = 3;
		cp.add(hdrRsrcTyp, c);
		c.gridy = 4;
		cp.add(hdrRsrcAmt, c);
		c.gridy = 5;
		cp.add(hdrCities, c);

		c.gridx = 4;
		c.gridy = 2;
		cp.add(fldMinBid, c);
		c.gridy = 3;
		cp.add(fldRsrcTyp, c);
		c.gridy = 4;
		cp.add(fldRsrcAmt, c);
		c.gridy = 5;
		cp.add(fldCities, c);
	}
	
	public void setupPlayers() {
		// For now just note that we will be displaying Player info in the frame
		JTextField tmpPlayerInfo = new JTextField("Player info");
		tmpPlayerInfo.setEditable(false);
		c.gridx = 0;
		c.gridy = 6;
	    c.gridwidth = 6;
		cp.add(tmpPlayerInfo, c);
		
		c.gridwidth = 1;
		JLabel lblPlayers = new JLabel("Players");
		c.gridy = 7;
		cp.add(lblPlayers, c);
		JLabel lblBalance = new JLabel("Electros");
		c.gridy = 8;
		cp.add(lblBalance, c);
		JLabel lblBidders = new JLabel("Bidders");
		c.gridy = 9;
		cp.add(lblBidders, c);
		ArrayList<Player> curPlayers = gameControl.getPlayers();
		for (int i = 0; i < curPlayers.size(); i++) {
			c.gridx = i+1;
			String name = curPlayers.get(i).getColor();
			JLabel lblName = new JLabel(name);
			JTextField electroCount = new JTextField(curPlayers.get(i).getElectroCount()+"");
			c.gridy = 7;
			cp.add(lblName, c);
			c.gridy = 8;
			cp.add(electroCount, c);
			// Only add Bid buttons for eligible players
			if (curPlayers.get(i).isEligibleToBid()) {
				JButton bidButton = new JButton(curPlayers.get(i).getColor());
				bidButton.addActionListener(bidListener);
				c.gridy = 9;
				cp.add(bidButton, c);
			}
		}
		JButton endAuctionButton = new JButton("End Auction");
		endAuctionButton.setMnemonic(KeyEvent.VK_E);
		endAuctionButton.addActionListener(endListener);
		c.gridy = 11;
		cp.add(endAuctionButton, c);
	}
	
	public void addNewBid(String Bidder) {
		// Make sure the bidder has enough cash to bid
		Player bidPlayer = gameControl.getPlayer(Bidder);
		int curBidAmt = Integer.parseInt(currentBid.getText());
		
		if (bidPlayer.getElectroCount() > curBidAmt) {
			currentBid.setText(curBidAmt + 1 + "");
			bidColor.setText(Bidder);
		} else {
			// Message that they can't afford to bid
    		JOptionPane.showMessageDialog(gameControl,
    			    bidColor + "Can't afford to increase bid to " + (curBidAmt+1));
		}
	}
	
	public void endAuction() {
		// Here are the steps that have to be taken:
		//   - Assign the PP to the winning bidder, letting the user choose which to dispose if they have 3
		//		(for now just dispose of the lowest number
		//   - Subtract the bid amount from the winning player's electro balance
		//   - Make the winning bidder ineligible to bid in future auctions for this turn
		//   - If it was the winning bidder's turn, move to the next player's turn
		//   - Remove the PP from the PP market
		//   - Add a replacement from the deck and resort the PP market
		Player winningBidder = gameControl.getPlayer(bidColor.getText());
		if (!gameControl.isFirstTurn()) {
			if (winningBidder.getMyPlants().size() == MAX_POWER_PLANTS) {
				int idx = winningBidder.getMinPlantNo();
				winningBidder.getMyPlants().remove(idx);
			}
		}
		int bidAmt = Integer.parseInt(currentBid.getText());
		winningBidder.spendElectros(bidAmt);
		winningBidder.addPowerPlant(auctionPP);
		winningBidder.setEligibleToBid(false);
		if (gameControl.getTurnPlayer().equals(winningBidder)) {
			// End this player's turn
			gameControl.endBiddingTurn();
		}
		gameControl.removePowerPlant(auctionPP);
		gameControl.drawPowerPlant();
		gameControl.setPurchase(); 		// Confirm a PP was purchased this round
		myFrame.dispose();		// Close the auction window
	}
}
