package com.client_rest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

import com.microsoft.azure.functions.annotation.*;
import com.models.UserModel;
import com.database_connection.OracleDBConnection;
import com.google.gson.Gson;
import com.microsoft.azure.functions.*;

public class ClientRest {
    private static final Logger logger = Logger.getLogger(OracleDBConnection.class.getName());

    @FunctionName("ClientRest")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = { HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT,
                    HttpMethod.DELETE }, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<Object>> request,
            final ExecutionContext context) {

        context.getLogger().info("Java HTTP trigger processed a request.");

        HttpResponseMessage response;

        switch (request.getHttpMethod()) {
            case GET:
                response = handleGet(request, context);
                break;
            case POST:
                response = handlePost(request, context);
                break;
            case PUT:
                response = handlePut(request, context);
                break;
            case DELETE:
                response = handleDelete(request, context);
                break;
            default:
                response = request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Error: Metodo no válido.").build();
                break;
        }

        return response;

    }

    private HttpResponseMessage handleGet(HttpRequestMessage<Optional<Object>> request, ExecutionContext context) {

        context.getLogger().info("Executing POST request");

        try {
            // we generate the connection:
            Connection conn = OracleDBConnection.getConnection();

            // we prepare the query
            String selectQuery = "SELECT * FROM USUARIOS";

            PreparedStatement preparedStatement = conn.prepareStatement(selectQuery);

            // we execute the query
            ResultSet rs = preparedStatement.executeQuery();

            List<UserModel> usersFounded = new ArrayList<>();

            while (rs.next()) {
                long id = rs.getLong("ID");
                String email = rs.getString("EMAIL");
                String password = rs.getString("PASSWORD");
                String rol = rs.getString("ROL");

                UserModel user = new UserModel();

                user.setId(id);
                user.setEmail(email);
                user.setPassword(password);
                user.setRol(rol);

                usersFounded.add(user);
            }

            String response = "Users found: " + usersFounded.size();

            logger.info(response);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(usersFounded)
                    .build();

        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage()).build();
        }
    }

    private HttpResponseMessage handlePut(HttpRequestMessage<Optional<Object>> request, ExecutionContext context) {
        try {
            // we generate the connection:
            Connection conn = OracleDBConnection.getConnection();

            // we prepare the query
            String updateQuery = "UPDATE USUARIOS SET EMAIL = ?, PASSWORD = ?, ROL = ? WHERE ID = ?";

            Gson gson = new Gson();

            UserModel usuario = gson.fromJson(gson.toJson(request.getBody().get()), UserModel.class);

            PreparedStatement preparedStatement = conn.prepareStatement(updateQuery);

            // we add the values of the object received
            preparedStatement.setString(1, usuario.getEmail());
            preparedStatement.setString(2, usuario.getPassword());
            preparedStatement.setString(3, usuario.getRol());
            preparedStatement.setLong(4, usuario.getId());

            // we execute the query
            int updatedRows = preparedStatement.executeUpdate();

            if (updatedRows == 1) {
                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body("User updated").build();
            } else {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Error updating the user").build();
            }

        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage()).build();
        }
    }

    private HttpResponseMessage handleDelete(HttpRequestMessage<Optional<Object>> request, ExecutionContext context) {
        try {
            // we generate the connection:
            Connection conn = OracleDBConnection.getConnection();

            Gson gson = new Gson();

            Long id = gson.fromJson(gson.toJson(request.getBody().get()), Long.class);

            // we prepare the query
            String deleteQuery = "DELETE FROM USUARIOS WHERE ID = ?";

            PreparedStatement preparedStatement = conn.prepareStatement(deleteQuery);

            // we add the values of the object received
            preparedStatement.setLong(1, id);

            // we execute the query
            int deletedRows = preparedStatement.executeUpdate();

            if (deletedRows > 0) {
                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(
                                "Deleted user with ID: " + id)
                        .build();
            } else {
                return request.createResponseBuilder(HttpStatus.OK)
                        .body("No se encontró un usuario con ID: " + id)
                        .build();
            }

        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage()).build();
        }
    }

    private HttpResponseMessage handlePost(HttpRequestMessage<Optional<Object>> request, ExecutionContext context) {
        try {
            // we generate the connection:
            Connection conn = OracleDBConnection.getConnection();

            // we prepare the query
            String insertQuery = "INSERT INTO USUARIOS (EMAIL, PASSWORD, ROL) VALUES (?, ?, ?)";

            PreparedStatement preparedStatement = conn.prepareStatement(insertQuery);

            Gson gson = new Gson();

            UserModel usuario = gson.fromJson(gson.toJson(request.getBody().get()), UserModel.class);

            // we add the values of the object received
            preparedStatement.setString(1, usuario.getEmail());
            preparedStatement.setString(2, usuario.getPassword());
            preparedStatement.setString(3, usuario.getRol());

            // we execute the query
            int insertedRows = preparedStatement.executeUpdate();
            String response = "Row inserted: " + insertedRows;

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(response)
                    .build();

        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage()).build();
        }
    }

}
