
public class PowerPlant implements Comparable<PowerPlant> {

	private int minBid;
	private String fuelType;
	private int resourceCount;
	private int citiesPowered;
	
	public PowerPlant(int MinBid, String FuelType, int ResourceCount, int CitiesPowered) {
		// TODO Auto-generated constructor stub
		minBid = MinBid;
		fuelType = FuelType;
		resourceCount = ResourceCount;
		citiesPowered = CitiesPowered;
	}

	@Override
	public int compareTo(PowerPlant comparePP) {
		// TODO Auto-generated method stub
		int compMinBid = comparePP.getMinBid();
		return this.minBid - compMinBid;
	}

	// Access methods
	public int getMinBid() {
		return minBid;
	}
	public String getFuelType() {
		return fuelType;
	}
	public int getResourceCount() {
		return resourceCount;
	}
	public int getCitiesPowered() {
		return citiesPowered;
	}
}
