package com.supermart.web;

import com.supermart.config.AppContext;
import com.supermart.model.User;
import com.supermart.security.AuthFilter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

/** Controller for the login form. */
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (AuthFilter.currentUser(request) != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        if ("session".equals(request.getParameter("error"))) {
            request.setAttribute("message", "Please sign in to continue.");
        }
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = WebUtils.string(request, "username");
        String password = request.getParameter("password");

        Optional<User> user = AppContext.from(getServletContext()).getAuthService()
                .authenticate(username, password);

        if (user.isEmpty()) {
            // Same message for both failure modes - no user enumeration.
            request.setAttribute("error", "Invalid username or password.");
            request.setAttribute("username", username);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        // Defend against session fixation: drop any pre-login session, then start a fresh one.
        HttpSession existing = request.getSession(false);
        if (existing != null) {
            existing.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(AuthFilter.SESSION_USER, user.get());
        session.setMaxInactiveInterval(30 * 60);
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}
