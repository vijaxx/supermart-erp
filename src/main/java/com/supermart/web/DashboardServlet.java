package com.supermart.web;

import com.supermart.config.AppContext;
import com.supermart.service.InventoryService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Landing page after login: headline counts for whichever role is signed in. */
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        AppContext context = AppContext.from(getServletContext());
        InventoryService inventory = context.getInventoryService();

        request.setAttribute("headcount", context.getEmployeeService().headcount());
        request.setAttribute("productCount", inventory.productCount());
        request.setAttribute("lowStockCount", inventory.lowStockCount());
        request.setAttribute("inventoryValue", context.getReportService().totalInventoryValue());
        request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
    }
}
