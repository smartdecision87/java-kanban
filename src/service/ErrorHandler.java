package service;

import com.sun.net.httpserver.HttpExchange;
import exception.ManagerSaveException;
import exception.NotFoundException;
import exception.OverlapsException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ErrorHandler {
    public void handle(HttpExchange h, Exception e) throws IOException {
        int statusCode = HttpTaskServer.HttpStatus.UNKNOWN_ERROR.getCode();
        String text = "";
        try {
            if (e instanceof ManagerSaveException) {
                statusCode = HttpTaskServer.HttpStatus.INTERNAL_SERVER_ERROR.getCode();
            }
            if (e instanceof OverlapsException) {
                statusCode = HttpTaskServer.HttpStatus.OVERLAPS_ERROR.getCode();
            }
            if (e instanceof NotFoundException) {
                statusCode = HttpTaskServer.HttpStatus.NOT_FOUND.getCode();
            }
            byte[] resp = e.getMessage().getBytes(StandardCharsets.UTF_8);
            h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            h.sendResponseHeaders(statusCode, resp.length);
            h.getResponseBody().write(resp);
            h.close();
        } catch (Exception err) {
            err.printStackTrace();
        }
    }
}
