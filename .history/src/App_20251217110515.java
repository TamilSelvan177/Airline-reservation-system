import java.sql.*;


public class App {

    static void showdb(Connection conn){
        try {
            Statement st = conn.createStatement();
            String sql = "SELECT name FROM sys.databases";
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                System.out.println(rs.getString("name"));
        }


        } catch (Exception e) {
            System.out.println("SQL exception occured");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {

        String url =
            "jdbc:sqlserver://localhost:1433;"
          + "databaseName=AirlineReservationDB;"
          + "integratedSecurity=true;"
          + "encrypt=true;"
          + "trustServerCertificate=true;";

        Connection conn = DriverManager.getConnection(url);

        System.out.println("Connected to AirlineReservationDB successfully!");

        showdb(conn);

        conn.close();
    }
}
