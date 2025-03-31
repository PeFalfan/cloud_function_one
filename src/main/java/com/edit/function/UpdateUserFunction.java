package com.edit.function;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

import com.microsoft.azure.functions.annotation.*;
import com.Models.UserModel;
import com.database_connection.OracleDBConnection;
import com.microsoft.azure.functions.*;

public class UpdateUserFunction {

    private static final Logger logger = Logger.getLogger(OracleDBConnection.class.getName());

    @FunctionName("UpdateUserFunction")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.PUT }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<UserModel>> request,
            final ExecutionContext context) {

        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            // we generate the connection:
            Connection conn = OracleDBConnection.getConnection();

            // we prepare the query
            String updateQuery = "UPDATE USUARIOS SET EMAIL = ?, PASSWORD = ?, ROL = ? WHERE ID = ?";

            PreparedStatement preparedStatement = conn.prepareStatement(updateQuery);

            // we add the values of the object received
            preparedStatement.setString(1, request.getBody().get().getEmail());
            preparedStatement.setString(2, request.getBody().get().getPassword());
            preparedStatement.setString(3, request.getBody().get().getRol());
            preparedStatement.setLong(4, request.getBody().get().getId());

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
}
