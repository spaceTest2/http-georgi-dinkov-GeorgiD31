package homework;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Upload extends HttpServlet {
    private static final String UPLOAD_DIRECTORY = "uploads";
    private static final int MEMORY_THRESHOLD = 1024 * 1024 * 3;
    private static final int MAX_FILE_SIZE = 1024 * 1024 * 10;
    private static final int MAX_REQUEST_SIZE = 1024 * 1024 * 50;

    private static final Logger logger = Logger.getLogger(Upload.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("Received a file upload request.");

        if (!ServletFileUpload.isMultipartContent(request)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Form must have enctype=multipart/form-data.");
            return;
        }

        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(MEMORY_THRESHOLD);
        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(MAX_FILE_SIZE);
        upload.setSizeMax(MAX_REQUEST_SIZE);

        String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIRECTORY;
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }

        try (PrintWriter out = response.getWriter()) {
            List<FileItem> formItems = upload.parseRequest(request);

            for (FileItem item : formItems) {
                if (!item.isFormField()) {
                    String fileName = new File(item.getName()).getName();
                    logger.info("Processing file: " + fileName);

                    if (!isValidFileType(fileName)) {
                        logger.warning("Unsupported file type: " + getFileExtension(fileName));
                        out.println("Unsupported file type: " + getFileExtension(fileName));
                        return;
                    }

                    String filePath = uploadPath + File.separator + fileName;
                    item.write(new File(filePath));
                    logger.info("File uploaded successfully: " + fileName);
                    out.println("File uploaded successfully. Download URL: /files/" + fileName);
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error processing file upload", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + ex.getMessage());
        }
    }

    private boolean isValidFileType(String fileName) {
        String[] allowedExtensions = {"txt", "jpg", "png"};
        String fileExtension = getFileExtension(fileName);
        for (String ext : allowedExtensions) {
            if (ext.equalsIgnoreCase(fileExtension)) {
                return true;
            }
        }
        return false;
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
