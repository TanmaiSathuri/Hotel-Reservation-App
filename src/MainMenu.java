import api.HotelResource;
import model.IRoom;
import model.Reservation;
import java.text.SimpleDateFormat;

import java.util.Collection;
import java.util.Date;
import java.util.Scanner;
import java.util.Calendar;

public class MainMenu {

    private static final HotelResource hotelResource = HotelResource.getInstance();
    private static final Scanner scanner = new Scanner(System.in);

    public static void start() {
        boolean running = true;

        while (running) {
            printMainMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    findAndReserveRoom();
                    break;
                case "2":
                    seeMyReservations();
                    break;
                case "3":
                    createAccount();
                    break;
                case "4":
                    AdminMenu.start();
                    break;
                case "5":
                    System.out.println("Exiting application...");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void printMainMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. Find and reserve a room");
        System.out.println("2. See my reservations");
        System.out.println("3. Create an account");
        System.out.println("4. Admin");
        System.out.println("5. Exit");
        System.out.print("Enter choice: ");
    }

    private static void createAccount() {
        try {
            System.out.print("Enter Email: ");
            String email = scanner.nextLine();

            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                System.out.println("Invalid email format. Please try again.");
                return; // stop here
            }

            // OPTIONAL: check if email already exists
            if (hotelResource.getCustomer(email) != null) {
                System.out.println("An account with this email already exists.");
                return;
            }

            System.out.print("Enter First Name: ");
            String firstName = scanner.nextLine();

            System.out.print("Enter Last Name: ");
            String lastName = scanner.nextLine();

            hotelResource.createACustomer(email, firstName, lastName);
            System.out.println("Account created successfully!");

        } catch (IllegalArgumentException e) {
            System.out.println("Invalid email format. Please try again.");
        }
    }

    private static void seeMyReservations() {
        System.out.print("Enter your email: ");
        String email = scanner.nextLine();

        hotelResource.getCustomersReservations(email)
                .forEach(System.out::println);
    }

    private static void findAndReserveRoom() {
        try {
            System.out.print("Enter your email: ");
            String email = scanner.nextLine();

            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                System.out.println("Invalid email format.");
                return;
            }

            System.out.print("Enter check-in date (yyyy-mm-dd): ");
            Date checkInDate = parseDate(scanner.nextLine());

            System.out.print("Enter check-out date (yyyy-mm-dd): ");
            Date checkOutDate = parseDate(scanner.nextLine());

            java.util.Date today = new java.util.Date();

            if (checkInDate.before(today)) {
                System.out.println("Check-in date cannot be in the past");
                return;
            }

            if (!checkOutDate.after(checkInDate)) {
                System.out.println("Check-out date must be after check-in date");
                return;
            }


            Collection<IRoom> availableRooms =
                    hotelResource.findARoom(checkInDate, checkOutDate);

            // --------------------------
            // CASE 1 — NO ROOMS AVAILABLE
            // --------------------------
            if (availableRooms.isEmpty()) {
                System.out.println("No rooms available for these dates.");

                // ⭐ Recommended Dates (+7 days)
                Calendar cal = Calendar.getInstance();

                cal.setTime(checkInDate);
                cal.add(Calendar.DATE, 7);
                Date recommendedCheckIn = cal.getTime();

                cal.setTime(checkOutDate);
                cal.add(Calendar.DATE, 7);
                Date recommendedCheckOut = cal.getTime();

                System.out.println("Recommended dates (+7 Days): "
                        + recommendedCheckIn + " to " + recommendedCheckOut);

                Collection<IRoom> recommendedRooms =
                        hotelResource.findARoom(recommendedCheckIn, recommendedCheckOut);

                if (recommendedRooms.isEmpty()) {
                    System.out.println("No rooms available even for recommended dates.");
                    return;
                }

                System.out.println("Available recommended rooms:");
                for (IRoom room : recommendedRooms) {
                    System.out.println(room);
                }

                System.out.print("Do you want to book a recommended room? (y/n): ");
                if (!scanner.nextLine().equalsIgnoreCase("y")) {
                    System.out.println("Booking cancelled.");
                    return;
                }

                System.out.print("Enter room number to reserve: ");
                String roomNumber = scanner.nextLine();

                hotelResource.bookARoom(
                        email, roomNumber, recommendedCheckIn, recommendedCheckOut);

                System.out.println("Room reserved successfully for recommended dates!");
                return;
            }

            // ---------------------------------
            // CASE 2 — ROOMS AVAILABLE (NORMAL)
            // ---------------------------------
            System.out.println("Available rooms:");
            for (IRoom room : availableRooms) {
                System.out.println(room);
            }

            System.out.print("Enter room number to reserve: ");
            String roomNumber = scanner.nextLine();
            IRoom selectedRoom = hotelResource.getRoom(roomNumber);

            // ❗CHECK IF ROOM IS AVAILABLE FOR THESE DATES
            if (!hotelResource.isRoomFreeForDates(roomNumber, checkInDate, checkOutDate)) {

                System.out.println("Room already booked for selected dates.");

                // ⭐ Recommended Dates (+7 days)
                Calendar cal = Calendar.getInstance();

                cal.setTime(checkInDate);
                cal.add(Calendar.DATE, 7);
                Date recommendedCheckIn = cal.getTime();

                cal.setTime(checkOutDate);
                cal.add(Calendar.DATE, 7);
                Date recommendedCheckOut = cal.getTime();

                System.out.println("Recommended dates (+7 Days): "
                        + recommendedCheckIn + " to " + recommendedCheckOut);

                Collection<IRoom> recommendedRooms =
                        hotelResource.findARoom(recommendedCheckIn, recommendedCheckOut);

                if (recommendedRooms.isEmpty()) {
                    System.out.println("No rooms available even after adding 7 days.");
                    return;
                }

                System.out.println("Available recommended rooms:");
                for (IRoom room : recommendedRooms) {
                    System.out.println(room);
                }

                System.out.print("Enter room number to reserve recommended room: ");
                roomNumber = scanner.nextLine();

                hotelResource.bookARoom(
                        email, roomNumber, recommendedCheckIn, recommendedCheckOut);

                System.out.println("Room reserved successfully for recommended dates!");
                return;
            }

            // If room is free → book normally
            hotelResource.bookARoom(email, roomNumber, checkInDate, checkOutDate);
            System.out.println("Room reserved successfully!");

        } catch (IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception e) {
            System.out.println("Error while reserving room.");
        }
    }

    private static Date parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);  // ❗STOP accepting invalid dates
            return sdf.parse(dateStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date. Use correct format yyyy-mm-dd.");
        }
    }

}
