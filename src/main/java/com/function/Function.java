package com.function;

import com.function.Models.UserModel;
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

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    private static final Logger logger = Logger.getLogger(OracleDBConnection.class.getName());
    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     */
    @FunctionName("registerUser")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<UserModel>> request,
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

            return request.createResponseBuilder(HttpStatus.OK).body(response).build();

        } catch (SQLException e) {            
            logger.warning(e.getMessage());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage()).build();
        }
    }
}
