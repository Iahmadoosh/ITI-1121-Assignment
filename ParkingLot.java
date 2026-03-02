import java.io.File;
import java.util.Scanner;

/**
 * @author Mehrdad Sabetzadeh, University of Ottawa
 * Ahmad Alomari & Ismael Abed Ali
 */
public class ParkingLot {
    private static final String SEPARATOR = ",";

    private int numRows;
    private int numSpotsPerRow;
    private CarType[][] lotDesign;
    private Spot[][] occupancy; // Changed from Car[][] to Spot[][] to store both the car and its timestamp together.

    // This constructor sets up the parking lot by finding dimensions first, 
    // initializing the 2D arrays, and then filling in the design from the file.
    public ParkingLot(String strFilename) throws Exception {
        // First, we call the dimension helper to set numRows and numSpotsPerRow.
        calculateLotDimensions(strFilename);
        
        // Then we initialize the two grids based on the size we just found.
        this.lotDesign = new CarType[numRows][numSpotsPerRow];
        this.occupancy = new Spot[numRows][numSpotsPerRow];
        
        // Finally, we fill the lotDesign array with S, R, L, or E types.
        populateDesignFromFile(strFilename);
    }

    public int getNumRows() { return numRows; }
    public int getNumSpotsPerRow() { return numSpotsPerRow; }

    // This method creates a new Spot object and places it in the occupancy grid.
    public void park(int i, int j, Car c, int timestamp) {
        // We take the car and the current simulation time and wrap them in a Spot.
        occupancy[i][j] = new Spot(c, timestamp);
    }

    // This method removes a car from the lot and returns the Spot it was in.
    public Spot remove(int i, int j) {
        // It starts with boundary checks to make sure i and j are valid indexes.
        if (i < 0 || i >= numRows || j < 0 || j >= numSpotsPerRow) return null;
        
        // It checks if the spot is already empty before trying to remove anything.
        if (occupancy[i][j] == null) return null;
        
        // It stores the spot in a local variable, clears the grid, and returns that spot.
        Spot removedSpot = occupancy[i][j];
        occupancy[i][j] = null;
        return removedSpot;
    }

    // This method returns the Spot object at a specific coordinate.
    public Spot getSpotAt(int i, int j) {
        // It checks the boundaries first to prevent crashing.
        if (i < 0 || i >= numRows || j < 0 || j >= numSpotsPerRow) return null;
        
        // It simply returns whatever is at that index (either a Spot or null).
        return occupancy[i][j];
    }

    // This method determines if a car fits into a specific spot based on size rules.
    public boolean canParkAt(int i, int j, Car c) {
        // First, it checks the index range and makes sure the spot isn't already taken.
        if (i < 0 || i >= numRows || j < 0 || j >= numSpotsPerRow) return false;
        if (occupancy[i][j] != null) return false;

        CarType carType = c.getType();
        CarType spotType = lotDesign[i][j];

        // Then it uses a switch to compare the spot type against the car type.
        switch (spotType) {
            case NA: 
                return false;
            case ELECTRIC: 
                // Only electric cars can park in electric spots.
                return carType == CarType.ELECTRIC;
            case SMALL: 
                // Small spots fit Small and Electric cars.
                return carType == CarType.SMALL || carType == CarType.ELECTRIC;
            case REGULAR: 
                // Regular spots fit Regular, Small, and Electric cars.
                return carType == CarType.REGULAR || carType == CarType.SMALL || carType == CarType.ELECTRIC;
            case LARGE: 
                // Large spots fit all car types except NA.
                return carType == CarType.LARGE || carType == CarType.REGULAR || carType == CarType.SMALL || carType == CarType.ELECTRIC;
            default: 
                return false;
        }
    }

    // This method scans the lot row-by-row to find the first spot where the car fits.
    public boolean attemptParking(Car c, int timestamp) {
        // It uses nested for-loops to go through every spot in the 2D array.
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numSpotsPerRow; j++) {
                // If it finds a valid spot, it parks the car and returns true immediately.
                if (canParkAt(i, j, c)) {
                    park(i, j, c, timestamp);
                    return true;
                }
            }
        }
        // If the loops finish without finding a spot, it returns false.
        return false;
    }

    // This method counts every spot in the lot that isn't designated as NA.
    public int getTotalCapacity() {
        int spots = 0;
        // It loops through the design grid and increments a counter for every valid type.
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numSpotsPerRow; j++) {
                if (lotDesign[i][j] != CarType.NA) {
                    spots++;
                }
            }
        }
        return spots; 
    }

    // This method counts how many spots are currently filled with a car.
    public int getTotalOccupancy() {
        int parked = 0;
        // It checks the occupancy grid and counts every index that is not null.
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numSpotsPerRow; j++) {
                if (occupancy[i][j] != null) {
                    parked++;
                }
            }
        }
        return parked;      
    }

    // This method reads the file to determine the height and width of the lot.
    private void calculateLotDimensions(String strFilename) throws Exception {
        Scanner scanner = new Scanner(new File(strFilename));
        int localRows = 0;
        int localCols = 0;

        // It reads every line in the file to count the total number of rows.
        while (scanner.hasNextLine()) {
            String str = scanner.nextLine().trim();
            if (str.isEmpty()) continue;

            localRows++;
            // On the first line, it splits the string to count the number of columns.
            if (localRows == 1) {
                String[] parts = str.split(SEPARATOR);
                localCols = parts.length;
            }
        }
        // It saves the counts to the instance variables and closes the file.
        this.numRows = localRows;
        this.numSpotsPerRow = localCols;
        scanner.close();
    }

    // This method fills the lotDesign array by converting the file labels to enums.
    private void populateDesignFromFile(String strFilename) throws Exception {
        Scanner scanner = new Scanner(new File(strFilename));
        int currentRow = 0;

        while (scanner.hasNextLine()) {
            String str = scanner.nextLine().trim();
            if (str.isEmpty()) continue;

            // It splits the line and converts each label (like "S") into a CarType.
            String[] parts = str.split(SEPARATOR);
            for (int j = 0; j < parts.length; j++) {
                this.lotDesign[currentRow][j] = Util.getCarTypeByLabel(parts[j].trim());
            }
            currentRow++;
        }
        scanner.close();
    }

    // This method creates a formatted string showing the lot design and occupancy.
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("==== Lot Design ====").append(System.lineSeparator());

        // First part: loops through lotDesign to print the layout (S, R, L, E).
        for (int i = 0; i < lotDesign.length; i++) {
            for (int j = 0; j < lotDesign[0].length; j++) {
                buffer.append((lotDesign[i][j] != null) ? Util.getLabelByCarType(lotDesign[i][j])
                        : Util.getLabelByCarType(CarType.NA));
                if (j < numSpotsPerRow - 1) buffer.append(", ");
            }
            buffer.append(System.lineSeparator());
        }

        buffer.append(System.lineSeparator()).append("==== Parking Occupancy ====").append(System.lineSeparator());

        // Second part: loops through occupancy to list every spot and what's in it.
        for (int i = 0; i < occupancy.length; i++) {
            for (int j = 0; j < occupancy[0].length; j++) {
                buffer.append("(" + i + ", " + j + "): " + 
                              ((occupancy[i][j] != null) ? occupancy[i][j] : "Unoccupied"));
                buffer.append(System.lineSeparator());
            }
        }
        return buffer.toString();
    }
}
