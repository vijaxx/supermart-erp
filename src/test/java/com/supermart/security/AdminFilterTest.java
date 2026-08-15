package com.supermart.security;

import com.supermart.model.Role;
import com.supermart.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies the role guard: a STAFF user hitting an admin-only page is genuinely blocked (403,
 * request never reaches the servlet), while an ADMIN user passes through.
 */
class AdminFilterTest {

    private final AdminFilter filter = new AdminFilter();

    @Test
    void staffUserIsDeniedWithForbiddenStatus() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        User staffUser = new User(2, "staff", "Staff", "hash", Role.STAFF);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(AuthFilter.SESSION_USER)).thenReturn(staffUser);
        when(request.getRequestURI()).thenReturn("/reports");
        when(request.getRequestDispatcher("/WEB-INF/views/forbidden.jsp")).thenReturn(dispatcher);

        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(dispatcher).forward(request, response);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void adminUserPassesThrough() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        HttpSession session = mock(HttpSession.class);
        User adminUser = new User(1, "admin", "Admin", "hash", Role.ADMIN);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(AuthFilter.SESSION_USER)).thenReturn(adminUser);

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void anonymousRequestIsRedirectedToLoginRatherThanForbidden() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("");

        filter.doFilter(request, response, chain);

        verify(response).sendRedirect("/login?error=session");
        verify(chain, never()).doFilter(any(), any());
    }
}
