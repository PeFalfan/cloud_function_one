package com.create;

import com.Models.UserModel;
import com.database_connection.OracleDBConnection;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Logger;

public class RegisterUserFunction {
    private static final Logger logger = Logger.getLogger(OracleDBConnection.class.getName());

    @FunctionName("registerUser")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<UserModel>> request,
            final ExecutionContext context) {

        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            // we generate the connection:
            Connection conn = OracleDBConnection.getConnection();

            // we prepare the query
            String insertQuery = "INSERT INTO USUARIOS (EMAIL, PASSWORD, ROL) VALUES (?, ?, ?)";

            PreparedStatement preparedStatement = conn.prepareStatement(insertQuery);

            // we add the values of the object received
            preparedStatement.setString(1, request.getBody().get().getEmail());
            preparedStatement.setString(2, request.getBody().get().getPassword());
            preparedStatement.setString(3, request.getBody().get().getRol());

            // we execute the query
            int insertedRows = preparedStatement.executeUpdate();
            String response = "Rows inserted: " + insertedRows;

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
