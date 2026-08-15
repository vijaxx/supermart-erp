package com.supermart.security;

import com.supermart.model.User;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Role guard for admin-only areas (employee management, reports). Runs after {@link AuthFilter},
 * so the user is known to be authenticated; here we only check the role and answer 403 otherwise.
 */
public class AdminFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        User user = AuthFilter.currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=session");
            return;
        }
        if (!user.isAdmin()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("deniedPath", request.getRequestURI());
            request.getRequestDispatcher("/WEB-INF/views/forbidden.jsp").forward(request, response);
            return;
        }
        chain.doFilter(req, res);
    }
}
