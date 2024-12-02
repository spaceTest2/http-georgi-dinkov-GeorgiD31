package homework;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        context.addServlet(new ServletHolder(new Upload()), "/upload");
        context.addServlet(new ServletHolder(new Download()), "/files/*");

        System.out.println("Server started at http://localhost:8080");
        server.start();
        server.join();
    }
}
