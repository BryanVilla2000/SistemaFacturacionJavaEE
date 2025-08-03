package com.fitsystem.security;

import com.fitsystem.bean.AuthBean;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Filtro que intercepta las páginas .xhtml para controlar el acceso
 * según si el usuario está autenticado o no.
 */
@WebFilter("*.xhtml")
public class AuthorizationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Obtener la sesión actual (si existe)
        HttpSession session = req.getSession(false);

        // URI de la solicitud
        String reqURI = req.getRequestURI();

        // Verificar si es un recurso que NO requiere autenticación
        boolean recursoPublico = reqURI.contains("/login.xhtml") ||
                                 reqURI.contains("/public/") ||
                                 reqURI.contains("javax.faces.resource")||
                                 reqURI.contains("/resources/");

        // Verificar si hay un usuario logueado en sesión
        boolean usuarioAutenticado = (session != null &&
                                      session.getAttribute("authBean") != null &&
                                      ((AuthBean) session.getAttribute("authBean")).isLogueado());

        // Si está autenticado o accede a recurso público → permitir
        if (usuarioAutenticado || recursoPublico) {
            chain.doFilter(request, response);
        } else {
            // Redirigir al login si no tiene acceso
            res.sendRedirect(req.getContextPath() + "/login.xhtml");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No se necesita inicialización especial
    }

    @Override
    public void destroy() {
        // No se necesita limpieza especial
    }
}
