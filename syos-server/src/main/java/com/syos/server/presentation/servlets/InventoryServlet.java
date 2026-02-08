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

import java.util.stream.Collectors;

public class InventoryServlet extends HttpServlet {
    
    private RequestProcessor requestProcessor;

    @Override
    public void init() throws ServletException {
        requestProcessor = (RequestProcessor) getServletContext()
            .getAttribute("requestProcessor");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        try {
            String jsonBody = req.getReader().lines()
                .collect(Collectors.joining());
            
            InventoryBatchDto batch = JsonUtil.fromJson(
                jsonBody, InventoryBatchDto.class);

            ClientRequest<Void> clientRequest = new ClientRequest<>(
                ClientRequest.RequestType.ADD_INVENTORY, batch);
            
            requestProcessor.submitRequest(clientRequest).get();

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"success\": true}");

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}