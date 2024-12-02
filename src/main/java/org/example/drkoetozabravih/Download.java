package homework;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

public class Download extends HttpServlet {
    private static final String UPLOAD_DIRECTORY = "uploads";
    private static final Logger logger = Logger.getLogger(Download.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fileName = request.getPathInfo().substring(1);
        logger.info("Download request for file: " + fileName);

        String filePath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIRECTORY + File.separator + fileName;

        File file = new File(filePath);
        if (!file.exists()) {
            logger.warning("File not found: " + fileName);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found.");
            return;
        }

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        try (FileInputStream inStream = new FileInputStream(file);
             OutputStream outStream = response.getOutputStream()) {
            logger.info("Serving file: " + fileName);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
        }
    }
}
