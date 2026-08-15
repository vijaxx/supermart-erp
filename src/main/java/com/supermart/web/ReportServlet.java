package com.supermart.web;

import com.supermart.config.AppContext;
import com.supermart.service.ReportService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Admin-only reporting controller (see the AdminFilter mapping in web.xml). */
public class ReportServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ReportService reports = AppContext.from(getServletContext()).getReportService();

        request.setAttribute("departmentRows", reports.departmentSalaries());
        request.setAttribute("categoryRows", reports.categoryValuations());
        request.setAttribute("lowStockRows", reports.lowStock());
        request.setAttribute("totalPayroll", reports.totalPayroll());
        request.setAttribute("totalInventoryValue", reports.totalInventoryValue());
        request.getRequestDispatcher("/WEB-INF/views/reports.jsp").forward(request, response);
    }
}
