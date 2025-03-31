package com.get_users;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

import com.microsoft.azure.functions.annotation.*;
import com.Models.UserModel;
import com.database_connection.OracleDBConnection;
import com.microsoft.azure.functions.*;

public class GetAllUsers {
    private static final Logger logger = Logger.getLogger(OracleDBConnection.class.getName());

    @FunctionName("GetAllUsers")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");
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
}
