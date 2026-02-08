package com.syos.server.presentation.servlets;

import com.syos.common.util.JsonUtil;
import com.syos.server.concurrency.RequestProcessor;
import com.syos.server.concurrency.ServerMetrics;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServerStatusServlet extends HttpServlet {
    
    private RequestProcessor requestProcessor;

    @Override
    public void init() throws ServletException {
        requestProcessor = (RequestProcessor) getServletContext()
            .getAttribute("requestProcessor");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        ServerMetrics metrics = requestProcessor.getMetrics();
        
        resp.setContentType("application/json");
        resp.getWriter().write(JsonUtil.toJson(metrics));
    }
}