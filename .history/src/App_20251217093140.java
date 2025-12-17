import java.sql.*;

public class App {
    public static void main(String[] args) throws Exception {

        String url =
            "jdbc:sqlserver://localhost:1433;"
          + "databaseName=YourDatabaseName;"
          + "integratedSecurity=true;"
          + "encrypt=true;"
          + "trustServerCertificate=true;";

        Connection conn = DriverManager.getConnection(url);

        System.out.println("Connected to MS SQL Server successfully!");
    }
}