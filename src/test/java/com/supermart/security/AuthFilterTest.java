package com.supermart.security;

import com.supermart.model.Role;
import com.supermart.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Verifies the session guard: no session -> redirect to login; valid session -> request proceeds. */
class AuthFilterTest {

    private final AuthFilter filter = new AuthFilter();

    @Test
    void requestWithoutSessionIsRedirectedToLogin() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getRequestURI()).thenReturn("/employees");
        when(request.getContextPath()).thenReturn("");
        when(request.getSession(false)).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(response).sendRedirect("/login?error=session");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void requestWithSessionButNoAuthenticatedUserIsRedirected() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getRequestURI()).thenReturn("/dashboard");
        when(request.getContextPath()).thenReturn("");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(AuthFilter.SESSION_USER)).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(response).sendRedirect("/login?error=session");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void authenticatedSessionIsAllowedThrough() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        HttpSession session = mock(HttpSession.class);
        User user = new User(1, "admin", "Admin", "hash", Role.ADMIN);

        when(request.getRequestURI()).thenReturn("/dashboard");
        when(request.getContextPath()).thenReturn("");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(AuthFilter.SESSION_USER)).thenReturn(user);

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        verify(response, never()).sendRedirect(any());
    }

    @Test
    void loginPathIsPublicEvenWithoutASession() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getRequestURI()).thenReturn("/login");
        when(request.getContextPath()).thenReturn("");

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        verify(response, never()).sendRedirect(any());
    }
}
