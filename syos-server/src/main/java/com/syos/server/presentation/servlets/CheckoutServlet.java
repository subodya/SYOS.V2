package com.syos.server.presentation.servlets;

import com.syos.common.dto.*;
import com.syos.common.util.JsonUtil;
import com.syos.server.concurrency.ClientRequest;
import com.syos.server.concurrency.RequestProcessor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Servlet that handles checkout requests
 * Routes requests through the BlockingQueue RequestProcessor
 */
public class CheckoutServlet extends HttpServlet {
    
    private RequestProcessor requestProcessor;

    @Override
    public void init() throws ServletException {
        // Get RequestProcessor from ServletContext
        requestProcessor = (RequestProcessor) getServletContext()
            .getAttribute("requestProcessor");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        try {
            // Read request body
            String jsonBody = req.getReader().lines()
                .collect(Collectors.joining());
            
            // Parse JSON to DTO
            CheckoutRequest checkoutRequest = JsonUtil.fromJson(
                jsonBody, CheckoutRequest.class);

            // Create client request and submit to queue
            ClientRequest<BillDto> clientRequest = new ClientRequest<>(
                ClientRequest.RequestType.CHECKOUT, checkoutRequest);
            
            CompletableFuture<BillDto> future = requestProcessor.submitRequest(clientRequest);

            com.syos.server.presentation.websocket.DashboardWebSocket.broadcast("REFRESH_DASHBOARD");
            BillDto bill = future.get();

            // Send response
            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(JsonUtil.toJson(bill));

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}