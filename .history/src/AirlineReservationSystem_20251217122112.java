import java.sql.*;
import java.util.*;

class DBConnection {
    public static Connection getConnection() throws Exception {
        String url =
            "jdbc:sqlserver://localhost:1433;"
          + "databaseName=AirlineReservationDB;"
          + "integratedSecurity=true;"
          + "encrypt=true;"
          + "trustServerCertificate=true;";
        return DriverManager.getConnection(url);
    }
}

enum Gender {
    MALE, FEMALE, OTHERS
}

enum SeatClass {
    ECONOMY, PREMIUM_ECONOMY, BUSINESS, STANDBY, WAITING
}

class Passenger {

    private String name;
    private int age;
    private Gender gender;
    private SeatClass seat;
    private double fare;

    public Passenger(String name, int age, Gender gender) {
        this.name = name;
        this.age = age;
        this.gender = gender;
    }

    public String getName() { return name; }
    public int getAge() { return age; }
    public Gender getGender() { return gender; }
    public SeatClass getSeat() { return seat; }
    public void setSeat(SeatClass seat) { this.seat = seat; }
    public double getFare() { return fare; }
    public void setFare(double fare) { this.fare = fare; }
}

abstract class ReservationSystem {

    protected String flightCode;

    public ReservationSystem(String flightCode) {
        this.flightCode = flightCode;
    }

    public abstract void bookTicket(Passenger p, SeatClass preference);
    public abstract void cancelTicket(int passengerId);

    public void showPassengers() {
        String sql = "SELECT * FROM Passengers";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (!rs.next()) {
                System.out.println("No Passenger Booked");
                return;
            }

            do {
                System.out.println("--------------------------------");
                System.out.println("Passenger ID : " + rs.getInt("PassengerID"));
                System.out.println("Name         : " + rs.getString("Name"));
                System.out.println("Age          : " + rs.getInt("Age"));
                System.out.println("Gender       : " + rs.getString("Gender"));
                System.out.println("Seat Class   : " + rs.getString("SeatClass"));
                System.out.println("Fare         : ₹" + rs.getDouble("Fare"));
                System.out.println("--------------------------------");
            } while (rs.next());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void searchPassenger(int passengerId) {
        String sql = "SELECT * FROM Passengers WHERE PassengerID=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, passengerId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("--------------------------------");
                System.out.println("Passenger ID : " + rs.getInt("PassengerID"));
                System.out.println("Name         : " + rs.getString("Name"));
                System.out.println("Age          : " + rs.getInt("Age"));
                System.out.println("Gender       : " + rs.getString("Gender"));
                System.out.println("Seat Class   : " + rs.getString("SeatClass"));
                System.out.println("Fare         : ₹" + rs.getDouble("Fare"));
                System.out.println("--------------------------------");
            } else {
                System.out.println("Passenger not found");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showTotalRevenue() {
        String sql = "SELECT SUM(Fare) AS Total FROM Passengers";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                System.out.println("Total Revenue: ₹" + rs.getDouble("Total"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class FlightReservation extends ReservationSystem {

    public FlightReservation(String flightCode) {
        super(flightCode);
    }

    private int getSeat(String column) {
        String sql = "SELECT " + column + " FROM Flights WHERE FlightCode=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, flightCode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(column);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void updateSeat(String column, int value) {
        String sql = "UPDATE Flights SET " + column + "=? WHERE FlightCode=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, value);
            ps.setString(2, flightCode);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertPassenger(Passenger p) {
        String sql =
            "INSERT INTO Passengers (Name, Age, Gender, SeatClass, Fare) " +
            "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getName());
            ps.setInt(2, p.getAge());
            ps.setString(3, p.getGender().toString());
            ps.setString(4, p.getSeat().toString());
            ps.setDouble(5, p.getFare());
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double calculateFare(SeatClass seat) {
        switch (seat) {
            case ECONOMY: return 3500;
            case PREMIUM_ECONOMY: return 5500;
            case BUSINESS: return 9000;
            case STANDBY: return 2000;
            default: return 0;
        }
    }

    @Override
    public void bookTicket(Passenger p, SeatClass pref) {

        SeatClass allotted;

        if (pref == SeatClass.ECONOMY && getSeat("Economy") > 0) {
            allotted = SeatClass.ECONOMY;
            updateSeat("Economy", getSeat("Economy") - 1);

        } else if (pref == SeatClass.PREMIUM_ECONOMY && getSeat("PremiumEconomy") > 0) {
            allotted = SeatClass.PREMIUM_ECONOMY;
            updateSeat("PremiumEconomy", getSeat("PremiumEconomy") - 1);

        } else if (pref == SeatClass.BUSINESS && getSeat("Business") > 0) {
            allotted = SeatClass.BUSINESS;
            updateSeat("Business", getSeat("Business") - 1);

        } else if (getSeat("Standby") > 0) {
            allotted = SeatClass.STANDBY;
            updateSeat("Standby", getSeat("Standby") - 1);

        } else if (getSeat("Waiting") > 0) {
            allotted = SeatClass.WAITING;
            updateSeat("Waiting", getSeat("Waiting") - 1);

        } else {
            System.out.println("No seats available.");
            return;
        }

        p.setSeat(allotted);
        double fare = calculateFare(allotted);
        p.setFare(fare);

        insertPassenger(p);

        System.out.println("Ticket booked on flight " + flightCode);
    }

    @Override
    public void cancelTicket(int passengerId) {
        String sql = "DELETE FROM Passengers WHERE PassengerID=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, passengerId);

            if (ps.executeUpdate() > 0)
                System.out.println("Ticket cancelled successfully.");
            else
                System.out.println("Passenger not found.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAvailableSeats() {
        String sql = "SELECT * FROM Flights WHERE FlightCode=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, flightCode);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("\nFlight: " + flightCode);
                System.out.println("Economy         : " + rs.getInt("Economy"));
                System.out.println("Premium Economy : " + rs.getInt("PremiumEconomy"));
                System.out.println("Business        : " + rs.getInt("Business"));
                System.out.println("Standby         : " + rs.getInt("Standby"));
                System.out.println("Waiting         : " + rs.getInt("Waiting"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

public class AirlineReservationSystem {

    public static void main(String[] args) {

        System.out.println("");

        Scanner sc = new Scanner(System.in);
        ReservationSystem flight = new FlightReservation("6E-204");

        while (true) {

            System.out.println("\n1. Book Ticket");
            System.out.println("2. Cancel Ticket (by ID)");
            System.out.println("3. Show Passengers");
            System.out.println("4. Search Passenger by ID");
            System.out.println("5. Show Available Seats");
            System.out.println("6. Show Total Revenue");
            System.out.println("7. Exit");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

                case 1:
                    System.out.print("Name: ");
                    String name = sc.nextLine();

                    System.out.print("Age: ");
                    int age = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Gender (MALE/FEMALE/OTHERS): ");
                    Gender g = Gender.valueOf(sc.nextLine().toUpperCase());

                    System.out.print("Seat Preference: ");
                    SeatClass s = SeatClass.valueOf(sc.nextLine().toUpperCase());

                    Passenger p = new Passenger(name, age, g);
                    flight.bookTicket(p, s);
                    break;

                case 2:
                    System.out.print("Passenger ID: ");
                    flight.cancelTicket(sc.nextInt());
                    sc.nextLine();
                    break;

                case 3:
                    flight.showPassengers();
                    break;

                case 4:
                    System.out.print("Passenger ID: ");
                    flight.searchPassenger(sc.nextInt());
                    sc.nextLine();
                    break;

                case 5:
                    ((FlightReservation) flight).showAvailableSeats();
                    break;

                case 6:
                    flight.showTotalRevenue();
                    break;

                case 7:
                    System.out.println("----------Thank You----------");
                    sc.close();
                    return;
            }
        }
    }
}
