import java.sql.*;

public class App {
    public static void main(String[] args) throws Exception {

        String url =
            "jdbc:sqlserver://localhost:1433;"
          + "databaseName=AirlineReservationDB;"
          + "integratedSecurity=true;"
          + "encrypt=true;"
          + "trustServerCertificate=true;";

        Connection conn = DriverManager.getConnection(url);

        System.out.println("Connected to AirlineReservationDB successfully!");

        try {
            String sql = "SELECT name FROM sys.databases";
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                System.out.println(rs.getString("name"));
}


        } catch (Exception e) {
            e.printStackTrace();
        }

        conn.close();
    }
}
