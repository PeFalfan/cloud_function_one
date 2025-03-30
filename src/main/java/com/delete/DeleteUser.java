package com.delete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

import com.microsoft.azure.functions.annotation.*;
import com.database_connection.OracleDBConnection;
import com.microsoft.azure.functions.*;

public class DeleteUser {
    private static final Logger logger = Logger.getLogger(OracleDBConnection.class.getName());

    @FunctionName("DeleteUser")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.DELETE }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<Long>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            // we generate the connection:
            Connection conn = OracleDBConnection.getConnection();

            // we prepare the query
            String deleteQuery = "DELETE FROM USUARIOS WHERE ID = ?";

            PreparedStatement preparedStatement = conn.prepareStatement(deleteQuery);

            // we add the values of the object received
            preparedStatement.setLong(1, request.getBody().get());

            // we execute the query
            int deletedRows = preparedStatement.executeUpdate();

            if (deletedRows > 0) {
                return request.createResponseBuilder(HttpStatus.OK).body(
                        "Deleted user with ID: " + request.getBody().get()).build();
            } else {
                return request.createResponseBuilder(HttpStatus.OK)
                        .body("No se encontró un usuario con ID: " + request.getBody().get()).build();
            }

        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage()).build();
        }
    }
}
