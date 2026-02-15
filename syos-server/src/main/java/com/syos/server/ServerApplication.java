package com.syos.server;

import com.syos.server.business.BusinessFacade;
import com.syos.server.concurrency.RequestProcessor;
import com.syos.server.domain.repositories.*;
import com.syos.server.infrastructure.repositories.*;
import com.syos.server.presentation.servlets.*;
import com.syos.server.presentation.websocket.DashboardWebSocket;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.websocket.server.WsContextListener;
import org.apache.catalina.connector.Connector;

import java.io.File;

import javax.websocket.server.ServerContainer;

/**
 * Main server application with embedded Tomcat
 * NO SPRING BOOT - Pure Tomcat servlets
 */
public class ServerApplication {
    
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try {
            System.out.println("===========================================");
            System.out.println("  SYOS Server Starting (Tomcat " + PORT + ")");
            System.out.println("===========================================\n");

            // Initialize repositories
            IItemRepository itemRepo = new ItemRepositoryImpl();
            IBillRepository billRepo = new BillRepositoryImpl();
            ICustomerRepository customerRepo = new CustomerRepositoryImpl();

            // Initialize business facade
            BusinessFacade businessFacade = new BusinessFacade(
                itemRepo, billRepo, customerRepo);

            // Initialize request processor (BLOCKING QUEUE CONCURRENCY)
            RequestProcessor requestProcessor = new RequestProcessor(businessFacade);

            // Create Tomcat instance
            Tomcat tomcat = new Tomcat();
            tomcat.setPort(PORT);
            
            Context context = tomcat.addContext("", new File(".").getAbsolutePath());

            // --- WEBSOCKET SETUP START ---
            
            // 1. Add the WebSocket listener to Tomcat context
            context.addApplicationListener(WsContextListener.class.getName());
            
            // 2. Start the server (Required before getting ServerContainer)
            tomcat.start();
            
            // 3. Register the Endpoint programmatically
            ServerContainer container = 
                (ServerContainer) context.getServletContext()
                .getAttribute(ServerContainer.class.getName());
                
            if (container != null) {
                //container.addEndpoint(DashboardWebSocket.class);
                System.out.println("✓ WebSocket Registered: ws://localhost:" + PORT + "/ws/dashboard");
            } else {
                System.err.println("⚠ Failed to initialize WebSocket container!");
            }
            
            // --- WEBSOCKET SETUP END ---


            // Set base directory
            String baseDir = System.getProperty("java.io.tmpdir");
            tomcat.setBaseDir(baseDir);
            @SuppressWarnings("unused")
            Connector connector = tomcat.getConnector();
            
            // Create context with proper path
            String contextPath = "";
            String docBase = new File(".").getAbsolutePath();
           // Context context = tomcat.addContext(contextPath, docBase);

            // Store request processor in context
            context.getServletContext().setAttribute("requestProcessor", requestProcessor);

            // Register servlets with proper mappings
            System.out.println("Registering servlets...");
            
            // CheckoutServlet
            String checkoutName = "CheckoutServlet";
            CheckoutServlet checkoutServlet = new CheckoutServlet();
            Tomcat.addServlet(context, checkoutName, checkoutServlet);
            context.addServletMappingDecoded("/api/checkout", checkoutName);
            System.out.println("  ✓ Registered: POST /api/checkout");

            // InventoryServlet
            String inventoryName = "InventoryServlet";
            InventoryServlet inventoryServlet = new InventoryServlet();
            Tomcat.addServlet(context, inventoryName, inventoryServlet);
            context.addServletMappingDecoded("/api/inventory", inventoryName);
            System.out.println("  ✓ Registered: POST /api/inventory");

            // ItemServlet
            String itemName = "ItemServlet";
            ItemServlet itemServlet = new ItemServlet();
            Tomcat.addServlet(context, itemName, itemServlet);
            context.addServletMappingDecoded("/api/items", itemName);
            System.out.println("  ✓ Registered: GET  /api/items");

            // ServerStatusServlet
            String statusName = "ServerStatusServlet";
            ServerStatusServlet statusServlet = new ServerStatusServlet();
            Tomcat.addServlet(context, statusName, statusServlet);
            context.addServletMappingDecoded("/api/status", statusName);
            System.out.println("  ✓ Registered: GET  /api/status");

            // Start server
            //tomcat.start();
            
            System.out.println("\n===========================================");
            System.out.println("✓ Server started successfully!");
            System.out.println("✓ Listening on http://localhost:" + PORT);
            System.out.println("\n✓ API endpoints:");
            System.out.println("  - POST http://localhost:" + PORT + "/api/checkout");
            System.out.println("  - POST http://localhost:" + PORT + "/api/inventory");
            System.out.println("  - GET  http://localhost:" + PORT + "/api/items?action=getAll");
            System.out.println("  - GET  http://localhost:" + PORT + "/api/status");
            System.out.println("\nPress Ctrl+C to stop");
            System.out.println("===========================================\n");

            // Keep server running
            tomcat.getServer().await();
            
        } catch (LifecycleException e) {
            System.err.println("Server failed to start: " + e.getMessage());
            e.printStackTrace();
        }
    }
}