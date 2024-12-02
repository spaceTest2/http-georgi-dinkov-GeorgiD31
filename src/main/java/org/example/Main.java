package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class SimpleWebCrawler {
    private static final Logger logger = Logger.getLogger(SimpleWebCrawler.class.getName());
    private static final String OUTPUT_FILE = "results.json";

    public static void main(String[] args) {
        if (args.length == 0) {
            logger.severe("Моля, подайте URL като аргумент!");
            return;
        }

        String initialUrl = args[0];
        if (!isValidUrl(initialUrl)) {
            logger.severe("Невалиден URL адрес: " + initialUrl);
            return;
        }

        try {
            processUrl(initialUrl);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Възникна грешка при обработката на URL: " + initialUrl, e);
        }
    }

    private static boolean isValidUrl(String url) {
        try {
            URI uri = new URI(url);
            return (uri.getScheme().equalsIgnoreCase("http") || uri.getScheme().equalsIgnoreCase("https"))
                    && uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }


    private static void processUrl(String url) throws IOException, InterruptedException, URISyntaxException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        int statusCode = response.statusCode();
        logger.log(Level.INFO, "URL: {0}, Status Code: {1}", new Object[]{url, statusCode});

        switch (statusCode) {
            case 200 -> {
                logger.info("OK");
                logCookies(response);
            }
            case 404 -> logger.warning("Not Found");
            case 403 -> logger.warning("Forbidden");
            case 500 -> logger.severe("Internal Server Error");
            default -> logger.warning("Error: " + statusCode);
        }

        saveResultToFile(url, statusCode);
    }

    private static void logCookies(HttpResponse<String> response) {
        response.headers().allValues("set-cookie").forEach(cookie -> logger.info("Извлечена Cookie: " + cookie));
    }

    private static void saveResultToFile(String url, int statusCode) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();
        result.put("url", url);
        result.put("status_code", statusCode);

        File outputFile = new File(OUTPUT_FILE);
        if (outputFile.exists()) {
            ObjectNode existingData = mapper.readValue(outputFile, ObjectNode.class);
            existingData.set(url, result);
            mapper.writeValue(outputFile, existingData);
        } else {
            mapper.writeValue(outputFile, result);
        }

        logger.info("Резултатът е записан в файла " + OUTPUT_FILE);
    }
}
