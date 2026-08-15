package com.supermart.web;

import com.supermart.config.AppContext;
import com.supermart.model.Employee;
import com.supermart.service.EmployeeService;
import com.supermart.service.ValidationException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

/**
 * Employee CRUD controller (admin only - see the AdminFilter mapping in web.xml).
 *
 * <p>Controller responsibilities only: read parameters, call the service, choose a view. No SQL and
 * no business rules live here.
 */
public class EmployeeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        EmployeeService service = AppContext.from(getServletContext()).getEmployeeService();
        String action = path(request);

        if ("/form".equals(action)) {
            int id = WebUtils.integer(request, "id", 0);
            if (id > 0) {
                Optional<Employee> existing = service.find(id);
                if (existing.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such employee");
                    return;
                }
                request.setAttribute("employee", existing.get());
            }
            request.setAttribute("departments", service.departments());
            request.getRequestDispatcher("/WEB-INF/views/employee-form.jsp").forward(request, response);
            return;
        }

        String query = WebUtils.string(request, "q");
        request.setAttribute("employees", service.search(query));
        request.setAttribute("query", query);
        request.getRequestDispatcher("/WEB-INF/views/employees.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        EmployeeService service = AppContext.from(getServletContext()).getEmployeeService();
        String action = path(request);

        if ("/delete".equals(action)) {
            service.delete(WebUtils.integer(request, "id", 0));
            response.sendRedirect(request.getContextPath() + "/employees?deleted=1");
            return;
        }

        Employee employee = new Employee();
        employee.setId(WebUtils.integer(request, "id", 0));
        employee.setFullName(WebUtils.string(request, "fullName"));
        employee.setEmail(WebUtils.string(request, "email"));
        employee.setDepartmentId(WebUtils.integer(request, "departmentId", 0));
        employee.setSalary(WebUtils.decimal(request, "salary"));
        employee.setJoiningDate(WebUtils.date(request, "joiningDate"));

        try {
            if (employee.getId() > 0) {
                service.update(employee);
            } else {
                service.create(employee);
            }
        } catch (ValidationException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("employee", employee);
            request.setAttribute("departments", service.departments());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.getRequestDispatcher("/WEB-INF/views/employee-form.jsp").forward(request, response);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/employees?saved=1");
    }

    private String path(HttpServletRequest request) {
        String info = request.getPathInfo();
        return info == null ? "/" : info;
    }
}
