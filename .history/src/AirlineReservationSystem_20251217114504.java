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

    private static int idCounter = 1;

    private int passengerId;
    private String name;
    private int age;
    private Gender gender;
    private SeatClass seat;
    private double fare;

    public Passenger(String name, int age, Gender gender) {
        this.passengerId = idCounter++;
        this.name = name;
        this.age = age;
        this.gender = gender;
    }

    public int getPassengerId() {
        return passengerId;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public Gender getGender() {
        return gender;
    }

    public SeatClass getSeat() {
        return seat;
    }

    public void setSeat(SeatClass seat) {
        this.seat = seat;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    public double getFare() {
        return fare;
    }

    public void displayDetails() {
        System.out.println("--------------------------------");
        System.out.println("Passenger ID : " + passengerId);
        System.out.println("Name         : " + name);
        System.out.println("Age          : " + age);
        System.out.println("Gender       : " + gender);
        System.out.println("Seat Class   : " + seat);
        System.out.println("Fare         : ₹" + fare);
        System.out.println("--------------------------------");
    }
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

        try(Connection conn = DBConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql)){

                if(!rs.next()){
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
            
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public void searchPassenger(int passengerId) {

        String sql = "SELECT * FROM Passengers where PassengerID =? ";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

                ps.setInt(1, passengerId);
                ResultSet rs = ps.executeQuery();

                if(rs.next()){
                    System.out.println("--------------------------------");
                    System.out.println("Passenger ID : " + rs.getInt("PassengerID"));
                    System.out.println("Name         : " + rs.getString("Name"));
                    System.out.println("Age          : " + rs.getInt("Age"));
                    System.out.println("Gender       : " + rs.getString("Gender"));
                    System.out.println("Seat Class   : " + rs.getString("SeatClass"));
                    System.out.println("Fare         : ₹" + rs.getDouble("Fare"));
                    System.out.println("--------------------------------");
                }else{
                    System.out.println("Passenger not found");
                }

        }catch(Exception e){
            e.printStackTrace();
        }       
    }

    public void showTotalRevenue() {

        String sql = "SELECT SUM(Fare) AS Total from Passengers";

        try(Connection conn = DBConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql)){
                
                if(rs.next()){
                    System.out.println("Total Revenue: ₹" + rs.getDouble("Total"));
                }

            }catch(Exception e){
                e.
            }
        
    }
}

class FlightReservation extends ReservationSystem {

    private int economy;
    private int premium;
    private int business;
    private int standby;
    private int waiting;

    public FlightReservation(String flightCode, int eco, int pre, int bus, int std, int wait) {
        super(flightCode);
        this.economy = eco;
        this.premium = pre;
        this.business = bus;
        this.standby = std;
        this.waiting = wait;
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

        if (pref == SeatClass.ECONOMY && economy > 0) {
            allotted = SeatClass.ECONOMY; economy--;
        } else if (pref == SeatClass.PREMIUM_ECONOMY && premium > 0) {
            allotted = SeatClass.PREMIUM_ECONOMY; premium--;
        } else if (pref == SeatClass.BUSINESS && business > 0) {
            allotted = SeatClass.BUSINESS; business--;
        } else if (standby > 0) {
            allotted = SeatClass.STANDBY; standby--;
        } else if (waiting > 0) {
            allotted = SeatClass.WAITING; waiting--;
        } else {
            System.out.println("No seats available.");
            return;
        }

        p.setSeat(allotted);
        double fare = calculateFare(allotted);
        p.setFare(fare);

        totalRevenue += fare;
        passengers.add(p);

        System.out.println("Ticket booked on flight " + flightCode);
        p.displayDetails();
    }

    @Override
    public void cancelTicket(int passengerId) {

        Iterator<Passenger> it = passengers.iterator();

        while (it.hasNext()) {
            Passenger p = it.next();
            if (p.getPassengerId() == passengerId) {

                SeatClass s = p.getSeat();
                if (s == SeatClass.ECONOMY) economy++;
                else if (s == SeatClass.PREMIUM_ECONOMY) premium++;
                else if (s == SeatClass.BUSINESS) business++;
                else if (s == SeatClass.STANDBY) standby++;
                else waiting++;

                totalRevenue -= p.getFare();
                it.remove();

                System.out.println("Ticket cancelled successfully.");
                return;
            }
        }
        System.out.println("Passenger not found.");
    }

    public void showAvailableSeats() {
        System.out.println("\nFlight: " + flightCode);
        System.out.println("Economy         : " + economy);
        System.out.println("Premium Economy : " + premium);
        System.out.println("Business        : " + business);
        System.out.println("Standby         : " + standby);
        System.out.println("Waiting         : " + waiting);
    }
}

public class AirlineReservationSystem {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        ReservationSystem flight =
                new FlightReservation("6E-204", 3, 2, 2, 2, 2);

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
                    return ;
            }
        }
    }
}
