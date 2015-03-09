import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/* 
 * This class implements the hex-grid
 */
public class Grid {
	HashMap<String,Tile> gridMap;
	int nrTiles = 61;
	String key;
	String team;
	int skillAttacker;
	int skillDefender;
	double hitChance;
	ArrayList<String> beasts;
	ArrayList<String> humans;
	
	
	/*
	 * Grid constructor which initializes the grid and 
	 * places the units according to the start position
	 */
	public Grid() {
		this.initializeGrid();
		this.placeUnits();
	}
	
	/* 
	 * Initialize the grid
	 */
	public void initializeGrid() {
		System.out.println("Initialize the grid");
				
		// Create a grid object
		gridMap = new HashMap<String, Tile>(100);
		
		// Fill the first half of the grid
		for (int x = -4; x <= 0; x++) {
			for (int y = -4 - x; y <= 4; y++) {
				gridMap.put(toKey(x,y), new Tile(x,y));
			}
		}
		
		// Fill the second half of the grid
		for (int x = 1; x <= 4; x++) {
			for (int y = -4; y <= 4 - x; y++) {
				gridMap.put(toKey(x,y), new Tile(x,y));
			}
		}
	}
	
	/* 
	 * Place all units on the grid in the right starting position
	 */
	public void placeUnits() {
		System.out.println("Create the startingposition on the grid");
		
		// Specify which tiles have which units at the starting position
		String[] generals = {toKey(4,-4), toKey(4,-1), toKey(3,1)};
		String[] swordsmen = {toKey(3,-4), toKey(3,-3), toKey(3,-2), toKey(3,-1), toKey(3,0), toKey(4,-3)};
		String[] orcs = {toKey(-4,4), toKey(-3,-1)};
		String[] goblins = {toKey(-4,1), toKey(-3,0), toKey(-3,1), toKey(-3,2), toKey(-3,3), toKey(-3,4), toKey(-2,4), toKey(-2,-1)};
		
		// Keep a list of humans and beasts
		humans = new ArrayList<String>();
		beasts = new ArrayList<String>();
		
		// Place all units on their tiles
		for (String coord : generals) {
			gridMap.get(coord).addUnit(new General());
			humans.add(coord);
		}
		for (String coord : swordsmen) {
			gridMap.get(coord).addUnit(new Swordsman());
			humans.add(coord);
		}
		for (String coord : orcs) {
			gridMap.get(coord).addUnit(new Orc());
			beasts.add(coord);
		}
		for (String coord : goblins) {
			gridMap.get(coord).addUnit(new Goblin());
			beasts.add(coord);
		}
		
		// Initialize the tiles by calculating adjacent tiles and buffers for every tile
		for (Map.Entry<String, Tile> tile : gridMap.entrySet())
		{
			tile.getValue().adjacentTiles(gridMap);
		}
	}
	
	/*
	 * Get the tile at the specific position
	 */
	public Tile getTile(int x, int y) {
		key = toKey(x,y);
		return gridMap.get(key);
	}
	
	/*
	 * Move a unit to a specified position
	 */
	public boolean moveUnit(int x, int y, int x1, int y1) {		
		Tile oldTile = getTile(x,y);
		Tile newTile = getTile(x1,y1);
		
		// Move unit if the move is legal and the goal tile is not occupied
		if (oldTile.legalMoves().contains(toKey(x1,y1))) {			
			// Update the lists containing all the units
			if (team.equals("Humans")) {
				humans.set(humans.indexOf(toKey(x,y)), toKey(x1,y1));
			}
			else if (team.equals("Beasts")) {
				beasts.set(beasts.indexOf(toKey(x,y)), toKey(x1,y1));
			}
			
			// Move the unit
			newTile.unit = oldTile.unit;
			newTile.team = oldTile.team;
			oldTile.unit = null;
			oldTile.unit = null;
			return true;
		}
		// If the move isn't legal, return false
		System.out.println("This is not a legal move!");
		return false;
	}
	
	/*
	 * Attack a unit with another unit
	 */
	public boolean attackUnit(int x, int y , int x1, int y1) {
		Tile tileSelf = getTile(x,y);
		Tile tileHostile = getTile(x1,y1);
		Unit unitSelf = tileSelf.unit;
		Unit unitHostile = tileHostile.unit;
		
		// Check if it is possible to attack
		if (attackIsPossible(x,y,x1,y1) == false) {
			return false;
		}
		
		// Get weapon skills
		skillAttacker = unitSelf.weaponSkill + tileSelf.getBuffer();
		skillDefender = unitHostile.weaponSkill + tileHostile.getBuffer();
		hitChance = 1 / (1 + Math.exp(0.4 * (skillAttacker - skillDefender)));
		
		// Attack the hostile
		if (Math.random() <= hitChance ) {
			unitHostile.hitPoints -= 1;
			System.out.println("BOOM in the balls!");
			
			// Remove the unit if he died
			if (unitHostile.hitPoints == 0) {
				tileHostile.unit = null;
				tileHostile.team = null;
				// Remove the unit also from the lists of units
				if (team.equals("Humans")) {
					beasts.remove(toKey(x1,y1));
				}
				else if (team.equals("Beasts")) {
					humans.remove(toKey(x1, y1));
				}
			}
			return true;
		}
		System.out.println("Ha, you missed!");
		return false;
	}
	
	/*
	 * This method implements a couple of checks explained above each check
	 * to make sure it is possible to attack the unit at (x1,y1) with unit (x,y)
	 */
	public boolean attackIsPossible(int x, int y, int x1, int y1) {
		Tile tileSelf = getTile(x,y);
		Tile tileHostile = getTile(x1,y1);
		Unit unitSelf = tileSelf.unit;
		Unit unitHostile = tileHostile.unit;
		
		// Check if the tiles exist
		if (tileHostile == null || tileSelf == null) {
			return false;
		}	
		
		// Check if the tile is adjacent
		if (!tileSelf.adjacentTiles.contains(tileHostile)) {
			System.out.println("Your tool of death is not long enough for this attack.");
			return false;
		}
		
		// Check if the attacker exists and is friendly
		if (unitSelf == null || !unitSelf.team.equals(team)) {
			System.out.println("There must be a (friendly) unit to attack with!");
			return false;
		}
		
		// Check if there is a unit to attack
		if (unitHostile == null) {
			System.out.println("Stop attacking air");
			return false;
		}
		
		
		// Check if the defender is friendly or hostile
		if (unitHostile.team.equals(unitSelf.team)) {
			System.out.println("Friendly fire!");
			return false;
		}
		return true;
	}
	
	/*
	 * Convert the coordinate of a tile to a string, so it
	 * can be used as key to access a tile in the hashmap
	 */
	public String toKey(int x, int y) {
		return new Integer(x).toString() + new Integer(y).toString();
	}
	
	
}