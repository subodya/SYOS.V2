package com.syos.server.presentation.servlets;

import com.syos.common.dto.ItemDto;
import com.syos.common.util.JsonUtil;
import com.syos.server.concurrency.ClientRequest;
import com.syos.server.concurrency.RequestProcessor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ItemServlet extends HttpServlet {
    
    private RequestProcessor requestProcessor;

    @Override
    public void init() throws ServletException {
        requestProcessor = (RequestProcessor) getServletContext()
            .getAttribute("requestProcessor");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        try {
            String action = req.getParameter("action");
            
            if ("getAll".equals(action)) {
                ClientRequest<List<ItemDto>> clientRequest = new ClientRequest<>(
                    ClientRequest.RequestType.GET_ALL_ITEMS, null);
                
                CompletableFuture<List<ItemDto>> future = 
                    requestProcessor.submitRequest(clientRequest);
                
                List<ItemDto> items = future.get();
                
                resp.setContentType("application/json");
                resp.getWriter().write(JsonUtil.toJson(items));
                
            } else if ("search".equals(action)) {
                String query = req.getParameter("q");
                
                ClientRequest<List<ItemDto>> clientRequest = new ClientRequest<>(
                    ClientRequest.RequestType.SEARCH_ITEMS, query);
                
                List<ItemDto> items = requestProcessor.submitRequest(clientRequest).get();
                
                resp.setContentType("application/json");
                resp.getWriter().write(JsonUtil.toJson(items));
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}