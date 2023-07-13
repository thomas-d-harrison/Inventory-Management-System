import java.sql.*;

public class ims{
    private static final String DB_URL = "jdbc:mysql://localhost/inventory_db";
    private static final String DB_USERNAME = "your_username";
    private static final String DB_PASSWORD = "your_password";

    public static void main(String[] args) {
        try {
            // Connect to the database
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            System.out.println("Connected to the database.");

            // Create inventory table if it doesn't exist
            createInventoryTable(connection);

            // Start the program loop
            boolean exitProgram = false;
            while (!exitProgram) {
                // Display menu options
                displayMenu();

                // Get user input
                int choice = getUserChoice();

                // Perform actions based on user choice
                switch (choice) {
                    case 1:
                        addInventory(connection);
                        break;
                    case 2:
                        displayInventory(connection);
                        break;
                    case 3:
                        rentInventory(connection);
                        break;
                    case 4:
                        displayRentals(connection);
                        break;
                    case 5:
                        exitProgram = true;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }

            // Close the database connection
            connection.close();
            System.out.println("Disconnected from the database.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void createInventoryTable(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();

        String createTableQuery = "CREATE TABLE IF NOT EXISTS inventory (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "name VARCHAR(100) NOT NULL," +
                "quantity INT NOT NULL," +
                "available BOOLEAN NOT NULL" +
                ")";

        statement.executeUpdate(createTableQuery);
    }

    private static void displayMenu() {
        System.out.println("========== Inventory Management Program ==========");
        System.out.println("1. Add inventory");
        System.out.println("2. Display inventory");
        System.out.println("3. Rent inventory");
        System.out.println("4. Display rentals");
        System.out.println("5. Exit");
        System.out.println("=============================================");
    }

    private static int getUserChoice() {
        System.out.print("Enter your choice: ");
        int choice = 0;
        try {
            choice = Integer.parseInt(System.console().readLine());
        } catch (NumberFormatException e) {
            // Ignore
        }
        return choice;
    }

    private static void addInventory(Connection connection) throws SQLException {
        System.out.println("========== Add Inventory ==========");
        System.out.print("Enter item name: ");
        String name = System.console().readLine();
        System.out.print("Enter quantity: ");
        int quantity = Integer.parseInt(System.console().readLine());

        String insertQuery = "INSERT INTO inventory (name, quantity, available) VALUES (?, ?, ?)";

        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
        preparedStatement.setString(1, name);
        preparedStatement.setInt(2, quantity);
        preparedStatement.setBoolean(3, true);
        preparedStatement.executeUpdate();

        System.out.println("Inventory added successfully.");
    }

    private static void displayInventory(Connection connection) throws SQLException {
        System.out.println("========== Inventory ==========");
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM inventory");

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            int quantity = resultSet.getInt("quantity");
            boolean available = resultSet.getBoolean("available");

            System.out.println("ID: " + id);
            System.out.println("Name: " + name);
            System.out.println("Quantity: " + quantity);
            System.out.println("Available: " + available);
            System.out.println("--------------------");
        }
    }

    private static void rentInventory(Connection connection) throws SQLException {
        System.out.println("========== Rent Inventory ==========");
        System.out.print("Enter customer name: ");
        String customerName = System.console().readLine();
        System.out.print("Enter item ID to rent: ");
        int itemId = Integer.parseInt(System.console().readLine());

        // Check if item is available
        String checkAvailabilityQuery = "SELECT * FROM inventory WHERE id = ? AND available = true";
        PreparedStatement availabilityStatement = connection.prepareStatement(checkAvailabilityQuery);
        availabilityStatement.setInt(1, itemId);
        ResultSet availabilityResultSet = availabilityStatement.executeQuery();

        if (availabilityResultSet.next()) {
            // Item is available, update the inventory and insert rental details
            String updateInventoryQuery = "UPDATE inventory SET available = false WHERE id = ?";
            PreparedStatement updateInventoryStatement = connection.prepareStatement(updateInventoryQuery);
            updateInventoryStatement.setInt(1, itemId);
            updateInventoryStatement.executeUpdate();

            String insertRentalQuery = "INSERT INTO rentals (customer_name, item_id) VALUES (?, ?)";
            PreparedStatement insertRentalStatement = connection.prepareStatement(insertRentalQuery);
            insertRentalStatement.setString(1, customerName);
            insertRentalStatement.setInt(2, itemId);
            insertRentalStatement.executeUpdate();

            System.out.println("Inventory rented successfully.");
        } else {
            // Item is not available
            System.out.println("Item is not available for rent.");
        }
    }

    private static void displayRentals(Connection connection) throws SQLException {
        System.out.println("========== Rentals ==========");
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM rentals");

        while (resultSet.next()) {
            int rentalId = resultSet.getInt("id");
            String customerName = resultSet.getString("customer_name");
            int itemId = resultSet.getInt("item_id");

            System.out.println("Rental ID: " + rentalId);
            System.out.println("Customer Name: " + customerName);
            System.out.println("Item ID: " + itemId);
            System.out.println("--------------------");
        }
    }
}
