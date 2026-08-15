package com.supermart.web;

import com.supermart.config.AppContext;
import com.supermart.model.Product;
import com.supermart.security.AuthFilter;
import com.supermart.service.InventoryService;
import com.supermart.service.ValidationException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

/**
 * Inventory controller. Browsing stock and recording stock movements is open to both roles;
 * creating, editing and deleting a product is restricted to ADMIN.
 */
public class ProductServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        InventoryService service = AppContext.from(getServletContext()).getInventoryService();
        String action = path(request);

        switch (action) {
            case "/low-stock" -> {
                request.setAttribute("products", service.lowStock());
                request.setAttribute("lowStockOnly", Boolean.TRUE);
                request.getRequestDispatcher("/WEB-INF/views/products.jsp").forward(request, response);
            }
            case "/form" -> {
                if (denyNonAdmin(request, response)) {
                    return;
                }
                int id = WebUtils.integer(request, "id", 0);
                if (id > 0) {
                    Optional<Product> existing = service.find(id);
                    if (existing.isEmpty()) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such product");
                        return;
                    }
                    request.setAttribute("product", existing.get());
                }
                request.setAttribute("categories", service.categories());
                request.setAttribute("suppliers", service.suppliers());
                request.getRequestDispatcher("/WEB-INF/views/product-form.jsp").forward(request, response);
            }
            default -> {
                int categoryId = WebUtils.integer(request, "categoryId", 0);
                request.setAttribute("products",
                        categoryId > 0 ? service.listByCategory(categoryId) : service.listAll());
                request.setAttribute("categories", service.categories());
                request.setAttribute("selectedCategory", categoryId);
                request.getRequestDispatcher("/WEB-INF/views/products.jsp").forward(request, response);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        InventoryService service = AppContext.from(getServletContext()).getInventoryService();
        String action = path(request);

        if ("/adjust".equals(action)) {
            try {
                service.adjustStock(WebUtils.integer(request, "id", 0), WebUtils.integer(request, "delta", 0));
            } catch (ValidationException e) {
                request.setAttribute("error", e.getMessage());
                request.setAttribute("products", service.listAll());
                request.setAttribute("categories", service.categories());
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                request.getRequestDispatcher("/WEB-INF/views/products.jsp").forward(request, response);
                return;
            }
            response.sendRedirect(request.getContextPath() + "/products?adjusted=1");
            return;
        }

        if (denyNonAdmin(request, response)) {
            return;
        }

        if ("/delete".equals(action)) {
            service.delete(WebUtils.integer(request, "id", 0));
            response.sendRedirect(request.getContextPath() + "/products?deleted=1");
            return;
        }

        Product product = new Product();
        product.setId(WebUtils.integer(request, "id", 0));
        product.setName(WebUtils.string(request, "name"));
        product.setSku(WebUtils.string(request, "sku"));
        product.setCategoryId(WebUtils.integer(request, "categoryId", 0));
        product.setSupplierId(WebUtils.integer(request, "supplierId", 0));
        product.setUnitPrice(WebUtils.decimal(request, "unitPrice"));
        product.setStockQuantity(WebUtils.integer(request, "stockQuantity", -1));
        product.setReorderLevel(WebUtils.integer(request, "reorderLevel", -1));

        try {
            if (product.getId() > 0) {
                service.update(product);
            } else {
                service.create(product);
            }
        } catch (ValidationException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("product", product);
            request.setAttribute("categories", service.categories());
            request.setAttribute("suppliers", service.suppliers());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.getRequestDispatcher("/WEB-INF/views/product-form.jsp").forward(request, response);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/products?saved=1");
    }

    /** Returns true (and renders 403) when a STAFF user attempts a product write. */
    private boolean denyNonAdmin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        var user = AuthFilter.currentUser(request);
        if (user != null && user.isAdmin()) {
            return false;
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        request.setAttribute("deniedPath", request.getRequestURI());
        request.getRequestDispatcher("/WEB-INF/views/forbidden.jsp").forward(request, response);
        return true;
    }

    private String path(HttpServletRequest request) {
        String info = request.getPathInfo();
        return info == null ? "/" : info;
    }
}
