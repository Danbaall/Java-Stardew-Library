package ir.ac.kntu.cli;

import ir.ac.kntu.entities.enums.Role;
import ir.ac.kntu.entities.enums.TicketType;
import ir.ac.kntu.entities.module.*;
import ir.ac.kntu.repo.ItemRepo;
import ir.ac.kntu.repo.TransactionRepo;
import ir.ac.kntu.services.ReservationService;
import ir.ac.kntu.services.TicketService;
import ir.ac.kntu.services.UserService;
import ir.ac.kntu.util.ServiceResult;
import java.util.List;
import java.util.Scanner;

public class TicketUI {

    private static final String RESET = "\033[0m";
    private static final String BOLD = "\033[1m";
    private static final String CYAN = "\033[0;36m";
    private static final String YELLOW = "\033[0;33m";
    private static final String GREEN = "\033[0;32m";
    private static final String RED = "\033[0;31m";

    private final Scanner scanner;
    private final TicketService ticketService;
    private final UserService userService;
    private final ItemUI itemUI;
    private final ItemRepo itemRepo;
    private final TransactionRepo transactionRepo;
    private final ReservationService reservationService;

    public TicketUI(Scanner scanner,
            TicketService ticketService,
            UserService userService,
            ItemUI itemUI,
            ItemRepo itemRepo,
            TransactionRepo transactionRepo,
            ReservationService reservationService) {
        this.scanner = scanner;
        this.ticketService = ticketService;
        this.userService = userService;
        this.itemUI = itemUI;
        this.itemRepo = itemRepo;
        this.transactionRepo = transactionRepo;
        this.reservationService = reservationService;
    }

    public void displayTicketDetail(Ticket ticket, User currentUser) {
        int width = 80;
        boolean running = true;
        while (running) {
            ConsoleUtils.clearScreen();
            printDetailHeader(ticket, width);
            printMessages(ticket, currentUser, width);
            String choice = printActions(ticket, currentUser);

            if (currentUser.getRole() == Role.ADMIN) {
                running = handleAdminActions(ticket, currentUser, choice);
            } else {
                running = handleUserActions(ticket, currentUser, choice);
            }
        }
    }

    private boolean handleAdminActions(Ticket ticket, User currentUser, String choice) {
        switch (choice) {
            case "1" -> handleReply(ticket, currentUser);
            case "2" -> {
                handleClose(ticket, currentUser);
                return false;
            }
            case "3" -> handleAdminAction3(ticket);
            case "4" -> {
                return handleAdminAction4(ticket);
            }
            case "5" -> {
                if (ticket.getTicketType() == TicketType.RESERVATION) {
                    handleCancelReservationForUser();
                }
            }
            case "6" -> {
                if (ticket.getTicketType() == TicketType.RESERVATION) {
                    return false;
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    private void handleAdminAction3(Ticket ticket) {
        if (ticket.getTicketType() == TicketType.FINANCIAL) {
            handleViewWallet(ticket);
        } else if (ticket.getTicketType() == TicketType.RESERVATION) {
            handleViewReservations(ticket);
        }
    }

    private boolean handleAdminAction4(Ticket ticket) {
        if (ticket.getTicketType() == TicketType.RESERVATION) {
            handleReserveForUser(ticket);
            return true;
        } else if (ticket.getTicketType() == TicketType.FINANCIAL) {
            return false;
        }
        return true;
    }

    private boolean handleUserActions(Ticket ticket, User currentUser, String choice) {
        switch (choice) {
            case "1" -> {
                handleReply(ticket, currentUser);
                return true;
            }
            case "2" -> {
                return false;
            }
            default -> {
                return false;
            }
        }
    }

    private void printDetailHeader(Ticket ticket, int width) {
        System.out.println(CYAN + "╔" + ConsoleUtils.repeat('═', width) + "╗" + RESET);
        System.out.println(CYAN + "║" + BOLD + YELLOW +
                ConsoleUtils.centerText(ticket.getSubject(), width) + RESET + CYAN + "║" + RESET);
        System.out.println(CYAN + "╚" + ConsoleUtils.repeat('═', width) + "╝" + RESET);
        System.out.println("ID: " + ticket.getTicketId());
        System.out.println("Type: " + ticket.getTicketType());
        System.out.println("Status: " + ticket.getStatus());
        System.out.println("Created: " + ticket.getCreatedAt());
        System.out.println(CYAN + ConsoleUtils.repeat('─', width) + RESET);
    }

    private void printMessages(Ticket ticket, User currentUser, int width) {
        List<Message> messages = ticket.getMessages();
        if (messages.isEmpty()) {
            System.out.println("No messages.");
            return;
        }
        for (int i = 0; i < messages.size(); i++) {
            Message msg = messages.get(i);
            String senderName = resolveSenderName(msg.senderId(), currentUser);
            System.out.println(RED + "#" + (i + 1) + RESET + " " + BOLD + senderName + RESET
                    + "  (" + msg.timestamp() + ")");
            System.out.println("  " + msg.msg());
            System.out.println(CYAN + ConsoleUtils.repeat('─', width) + RESET);
        }
    }

    private String resolveSenderName(String senderId, User currentUser) {
        if (senderId.equals(currentUser.getId())) {
            return "You";
        }
        User sender = userService.findUserById(senderId);
        if (sender != null) {
            return sender.getUserName() + " (" + sender.getRole() + ")";
        }
        return "Unknown";
    }

    private String printActions(Ticket ticket, User currentUser) {
        System.out.println(GREEN + "1. Reply" + RESET);

        if (currentUser.getRole() == Role.ADMIN) {
            System.out.println(GREEN + "2. Close Ticket" + RESET);

            if (ticket.getTicketType() == TicketType.FINANCIAL) {
                System.out.println(GREEN + "3. View User Wallet & Transactions" + RESET);
                System.out.println(GREEN + "4. Back" + RESET);
            } else if (ticket.getTicketType() == TicketType.RESERVATION) {
                System.out.println(GREEN + "3. View User Reservations" + RESET);
                System.out.println(GREEN + "4. Reserve Item for User" + RESET);
                System.out.println(GREEN + "5. Cancel a Reservation (by ID)" + RESET);
                System.out.println(GREEN + "6. Back" + RESET);
            } else {
                System.out.println(GREEN + "3. Back" + RESET);
            }
        } else {
            System.out.println(GREEN + "2. Back" + RESET);
        }

        System.out.print("> ");
        return scanner.nextLine();
    }

    private void handleReserveForUser(Ticket ticket) {
        User owner = userService.findUserById(ticket.getUserId());
        if (!(owner instanceof Client)) {
            System.out.println("User is not a client ; cannot reserve.");
            scanner.nextLine();
            return;
        }
        System.out.print("Enter item ID to reserve: ");
        String itemId = scanner.nextLine().trim();
        ServiceResult<Reservation> result = reservationService.reserveItem(owner, itemId);
        System.out.println(result.getMessage());
        scanner.nextLine();
    }

    private void handleCancelReservationForUser() {
        System.out.print("Enter reservation ID to cancel: ");
        String reservationId = scanner.nextLine().trim();
        ServiceResult<Void> result = reservationService.adminCancelReservation(reservationId);
        System.out.println(result.getMessage());
        scanner.nextLine();
    }

    private void handleReply(Ticket ticket, User currentUser) {
        System.out.print("Your reply: ");
        String msg = scanner.nextLine();
        if (msg.trim().isEmpty()) {
            System.out.println("Message cannot be empty.");
            scanner.nextLine();
            return;
        }
        ServiceResult<Void> res = ticketService.replyToTicket(ticket.getTicketId(), currentUser, msg);
        System.out.println((res.isSuccess() ? "done, " : "failed, ") + res.getMessage());
        scanner.nextLine();
    }

    private void handleClose(Ticket ticket, User currentUser) {
        ServiceResult<Void> res = ticketService.closeTicket(ticket.getTicketId(), currentUser);
        System.out.println((res.isSuccess() ? "done, " : "failed, ") + res.getMessage());
        scanner.nextLine();
    }

    private void handleViewWallet(Ticket ticket) {
        User owner = userService.findUserById(ticket.getUserId());
        if (!(owner instanceof Client client)) {
            System.out.println("User does not have a wallet.");
            scanner.nextLine();
            return;
        }
        ConsoleUtils.clearScreen();
        System.out.println("Wallet of " + client.getUserName() + " (ID: " + client.getId() + ")");
        System.out.println("Balance: $" + client.getWallet().getBalance());
        List<Transaction> transactions = transactionRepo.findByUserId(client.getId());
        itemUI.displayPagedList(transactions, "Transaction History");
    }

    private void handleViewReservations(Ticket ticket) {
        User owner = userService.findUserById(ticket.getUserId());
        if (owner == null) {
            System.out.println("User not found.");
            scanner.nextLine();
            return;
        }
        List<Reservation> reservations = reservationService.getUserReservations(owner);
        if (reservations.isEmpty()) {
            System.out.println("No reservations.");
            scanner.nextLine();
            return;
        }
        List<String> lines = reservations.stream()
                .map(r -> {
                    Item item = itemRepo.findById(r.getItemId());
                    String itemTitle = (item != null) ? item.getTitle() : r.getItemId();
                    String deadline = (r.getClaimDeadline() != null) ? r.getClaimDeadline().toString() : "N/A";
                    return r.getReservationId() + " | " + itemTitle + " | Status: " + r.getStatus()
                            + " | Deadline: " + deadline;
                })
                .toList();
        itemUI.displayPagedList(lines, "Reservations of " + owner.getUserName());
    }
}