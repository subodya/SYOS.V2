package com.syos.database;

import org.h2.tools.Server;

import java.sql.*;

/**
 * Standalone H2 Database Server
 */
public class DatabaseServer {
    
    private static final int TCP_PORT = 9092;
    private static Server server;

    public static void main(String[] args) {
        try {
            System.out.println("========================================");
            System.out.println("  SYOS Database Server (H2)");
            System.out.println("========================================\n");

            // Start H2 TCP server
            server = Server.createTcpServer(
                "-tcp",
                "-tcpAllowOthers",
                "-tcpPort", String.valueOf(TCP_PORT),
                "-ifNotExists"  // ADD THIS - allows database creation
            ).start();

            System.out.println("✓ H2 Database Server started");
            System.out.println("✓ Port: " + TCP_PORT);
            System.out.println("✓ Connection URL: jdbc:h2:tcp://localhost:" + 
                             TCP_PORT + "/~/syos\n");

            // Initialize database schema
            initializeDatabase();

            System.out.println("✓ Database initialized with schema and sample data");
            System.out.println("\nPress Ctrl+C to stop");
            
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (server != null) {
                    server.stop();
                    System.out.println("\n✓ Database server stopped");
                }
            }));

            // Keep running
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("Failed to start database server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initializeDatabase() throws SQLException {
        // UPDATED: Add ;IFEXISTS=FALSE to allow database creation
        String url = "jdbc:h2:tcp://localhost:" + TCP_PORT + "/~/syos;IFEXISTS=FALSE";
        
        try (Connection conn = DriverManager.getConnection(url, "sa", "")) {
            Statement stmt = conn.createStatement();

            System.out.println("Creating database tables...");

            // Create tables
            stmt.execute("CREATE TABLE IF NOT EXISTS items (" +
                "item_code VARCHAR(20) PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "description VARCHAR(255)," +
                "price DECIMAL(10, 2) NOT NULL," +
                "category_code VARCHAR(20)," +
                "current_stock INT NOT NULL DEFAULT 0," +
                "reorder_level INT NOT NULL DEFAULT 10)");

            stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                "customer_id VARCHAR(20) PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "email VARCHAR(100)," +
                "phone VARCHAR(20)," +
                "address VARCHAR(255))");

            stmt.execute("CREATE TABLE IF NOT EXISTS bills (" +
                "bill_id VARCHAR(30) PRIMARY KEY," +
                "customer_id VARCHAR(20) NOT NULL," +
                "cashier_id VARCHAR(20) NOT NULL," +
                "payment_method VARCHAR(10) NOT NULL," +
                "subtotal DECIMAL(10, 2) NOT NULL," +
                "tax DECIMAL(10, 2) NOT NULL," +
                "total DECIMAL(10, 2) NOT NULL," +
                "timestamp TIMESTAMP NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS bill_items (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "bill_id VARCHAR(30) NOT NULL," +
                "item_code VARCHAR(20) NOT NULL," +
                "item_name VARCHAR(100) NOT NULL," +
                "quantity INT NOT NULL," +
                "unit_price DECIMAL(10, 2) NOT NULL," +
                "subtotal DECIMAL(10, 2) NOT NULL)");

            System.out.println("  ✓ Tables created");

            // Insert sample data (use MERGE to avoid duplicates)
            System.out.println("Inserting sample data...");
            
            stmt.execute("MERGE INTO items (item_code, name, description, price, category_code, current_stock, reorder_level) " +
                "KEY(item_code) VALUES " +
                "('ITEM-001', 'Laptop', 'HP Laptop 15-inch', 750.00, 'ELECTRONICS', 25, 5)," +
                "('ITEM-002', 'Mouse', 'Wireless Mouse', 15.50, 'ACCESSORIES', 100, 20)," +
                "('ITEM-003', 'Keyboard', 'Mechanical Keyboard', 89.99, 'ACCESSORIES', 50, 10)," +
                "('ITEM-004', 'Monitor', '24-inch LED Monitor', 199.99, 'ELECTRONICS', 15, 5)," +
                "('ITEM-005', 'USB Cable', 'Type-C USB Cable', 9.99, 'ACCESSORIES', 200, 50)");

            stmt.execute("MERGE INTO customers (customer_id, name, email, phone, address) " +
                "KEY(customer_id) VALUES " +
                "('CUST-001', 'John Doe', 'john@email.com', '555-0001', '123 Main St')," +
                "('CUST-002', 'Jane Smith', 'jane@email.com', '555-0002', '456 Oak Ave')," +
                "('CUST-003', 'Bob Johnson', 'bob@email.com', '555-0003', '789 Pine Rd')");

            System.out.println("  ✓ Sample data inserted");
            
            // Verify data
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM items");
            if (rs.next()) {
                System.out.println("  ✓ Items in database: " + rs.getInt(1));
            }
            
            rs = stmt.executeQuery("SELECT COUNT(*) FROM customers");
            if (rs.next()) {
                System.out.println("  ✓ Customers in database: " + rs.getInt(1));
            }
        }
    }
}