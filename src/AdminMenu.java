import api.AdminResource;
import model.FreeRoom;
import model.IRoom;
import model.Room;
import model.RoomType;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private static final AdminResource adminResource = AdminResource.getInstance();
    private static final Scanner scanner = new Scanner(System.in);

    public static void start() {
        boolean running = true;

        while (running) {
            printAdminMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    adminResource.getAllCustomers()
                            .forEach(System.out::println);
                    break;

                case "2":
                    adminResource.getAllRooms()
                            .forEach(System.out::println);
                    break;

                case "3":
                    adminResource.displayAllReservations();
                    break;

                case "4":
                    addRoom();
                    break;

                case "5":
                    running = false;
                    break;

                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private static void printAdminMenu() {
        System.out.println("\n--- Admin Menu ---");
        System.out.println("1. See all Customers");
        System.out.println("2. See all Rooms");
        System.out.println("3. See all Reservations");
        System.out.println("4. Add a Room");
        System.out.println("5. Back to Main Menu");
        System.out.print("Enter choice: ");
    }

    private static void addRoom() {
        try {
            System.out.print("Enter room number: ");
            String roomNumber = scanner.nextLine();

            if (!roomNumber.matches("\\d+")) {
                throw new IllegalArgumentException("Room number must contain only digits.");
            }
            if (adminResource.getAllRooms()
                    .stream()
                    .anyMatch(r -> r.getRoomNumber().equals(roomNumber))) {
                System.out.println("Invalid Room. A room with this number already exists.");
                return; // stop further input
            }


            // Validate room number
            if (roomNumber == null || roomNumber.trim().isEmpty()) {
                throw new IllegalArgumentException("Room number cannot be empty.");
            }

            System.out.print("Enter price: ");
            String priceInput = scanner.nextLine();
            double price = Double.parseDouble(priceInput);

            // Validate price
            if (price < 0) {
                throw new IllegalArgumentException("Price cannot be negative.");
            }

            System.out.print("Enter room type (SINGLE/DOUBLE): ");
            RoomType roomType =
                    RoomType.valueOf(scanner.nextLine().toUpperCase());

            IRoom room;
            if (price == 0) {
                room = new FreeRoom(roomNumber, roomType);
            } else {
                room = new Room(roomNumber, price, roomType);
            }

            List<IRoom> rooms = new ArrayList<>();
            rooms.add(room);

            AdminResource.getInstance().addRoom(rooms);
            System.out.println("Room added successfully!");

        } catch (NumberFormatException e) {
            System.out.println("Invalid: Price must be a valid number.");

        } catch (IllegalArgumentException e) {
            System.out.println("Invalid " + e.getMessage());

        } catch (Exception e) {
            System.out.println("Error: Invalid input. Please try again.");
        }
    }
}