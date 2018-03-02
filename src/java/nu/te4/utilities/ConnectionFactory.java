package nu.te4.utilities;

import com.mysql.jdbc.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory
{
    public static Connection getConnection() throws SQLException, ClassNotFoundException
    {
        String url = "jdbc:mysql://localhost/sebastian_recipe_db";
        String user = "root";
        String password = "te4te4te4";
        Class.forName("com.mysql.jdbc.Driver");
        Connection connection = (com.mysql.jdbc.Connection)DriverManager.getConnection(url, user, password);
        return connection;
    }
}
