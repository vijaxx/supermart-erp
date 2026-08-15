package com.supermart;

import com.supermart.config.AppContext;
import com.supermart.config.Database;
import com.supermart.config.SchemaInitializer;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Entry point. Starts an embedded Jetty server with JSP support, wires the H2-backed
 * {@link AppContext} into the servlet context, and initializes the schema/seed data.
 *
 * <p>Run with: {@code mvn exec:java} (see the README for the one-command instructions).
 */
public class Main {

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getProperty("supermart.port", System.getenv().getOrDefault("PORT", "8080")));
        String dbPath = System.getProperty("supermart.dbPath", "./data/supermart");

        Database database = Database.file(dbPath);
        SchemaInitializer.initialize(database);

        Server server = new Server(port);

        WebAppContext webApp = new WebAppContext();
        webApp.setContextPath("/");
        webApp.setResourceBase(resolveWebappBase());
        webApp.setAttribute(AppContext.ATTRIBUTE, new AppContext(database));
        webApp.setConfigurationDiscovered(true);
        webApp.setAttribute(
                "org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                ".*/[^/]*jstl.*\\.jar$|.*/[^/]*taglibs.*\\.jar$");

        server.setHandler(webApp);
        server.start();
        System.out.println("Supermart ERP started on http://localhost:" + port
                + "  (database file: " + dbPath + ".mv.db)");
        server.join();
    }

    /** Locates src/main/webapp both when run from the source tree and from a packaged jar's sibling dir. */
    private static String resolveWebappBase() {
        String[] candidates = {"src/main/webapp", "webapp"};
        for (String candidate : candidates) {
            java.io.File dir = new java.io.File(candidate);
            if (dir.exists() && dir.isDirectory()) {
                return dir.getAbsolutePath();
            }
        }
        throw new IllegalStateException("Could not locate the webapp resource base (src/main/webapp)");
    }
}
