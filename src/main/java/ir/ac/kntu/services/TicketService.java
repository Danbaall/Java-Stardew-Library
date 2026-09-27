package ir.ac.kntu.services;

import java.util.ArrayList;
import java.util.List;

import ir.ac.kntu.entities.enums.Role;
import ir.ac.kntu.entities.enums.TicketStatus;
import ir.ac.kntu.entities.enums.TicketType;
import ir.ac.kntu.entities.module.Admin;
import ir.ac.kntu.entities.module.Message;
import ir.ac.kntu.entities.module.Ticket;
import ir.ac.kntu.entities.module.User;
import ir.ac.kntu.repo.TicketRepo;
import ir.ac.kntu.util.ServiceResult;

public class TicketService {

    private static final String FOUND_PREFIX = "Found ";

    private final TicketRepo ticketRepo;

    public TicketService(TicketRepo tickets) {
        this.ticketRepo = tickets;
    }

    public ServiceResult<Ticket> openTicket(User user, TicketType type, String subject, String msg) {
        if (subject == null || subject.trim().isEmpty()) {
            return ServiceResult.failure("Subject cannot be empty.");
        }
        if (msg == null || msg.trim().isEmpty()) {
            return ServiceResult.failure("Message cannot be empty.");
        }
        Ticket ticket = new Ticket(type, user.getId(), subject, msg);
        ticketRepo.save(ticket);
        return ServiceResult.success(ticket, "Ticket created.");
    }

    public ServiceResult<Void> replyToTicket(String ticketId, User sender, String msg) {
        Ticket ticket = ticketRepo.findById(ticketId);
        if (ticket == null) {
            return ServiceResult.failure("Ticket not found.");
        }
        if (sender.getRole() == Role.ADMIN) {
            Admin admin = (Admin) sender;
            if (!admin.canHandle(ticket.getTicketType())) {
                return ServiceResult
                        .failure("You are not assigned to handle '" + ticket.getTicketType() + "' tickets.");
            }
        } else if (!ticket.getUserId().equals(sender.getId())) {
            return ServiceResult.failure("You do not have access to this ticket.");
        }
        ticket.addMessage(new Message(sender.getId(), msg));
        ticketRepo.save(ticket);
        return ServiceResult.success("Reply added successfully.");
    }

    public ServiceResult<Void> closeTicket(String ticketId, User user) {
        if (user.getRole() != Role.ADMIN && user.getRole() != Role.MANAGER) {
            return ServiceResult.failure("Only admins or the manager can close tickets.");
        }
        Ticket ticket = ticketRepo.findById(ticketId);
        if (ticket == null) {
            return ServiceResult.failure("Ticket not found.");
        }
        ticket.closeTicket();
        ticketRepo.save(ticket);
        return ServiceResult.success("Ticket closed successfully.");
    }

    public ServiceResult<List<Ticket>> getUserTickets(User user) {
        List<Ticket> tickets = ticketRepo.findByUserId(user.getId());
        return ServiceResult.success(tickets, FOUND_PREFIX + tickets.size() + " tickets.");
    }

    public ServiceResult<List<Ticket>> getTicketsForAdmin(Admin admin) {
        List<Ticket> all = ticketRepo.findAll();
        List<Ticket> filtered = all.stream()
                .filter(t -> admin.canHandle(t.getTicketType()))
                .toList();
        return ServiceResult.success(filtered, FOUND_PREFIX + filtered.size() + " tickets.");
    }

    public ServiceResult<List<Ticket>> getTicketsByStatus(User currentUser, TicketStatus status) {
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.MANAGER) {
            return ServiceResult.failure("Admins and the manager only.");
        }
        List<Ticket> allTickets = ticketRepo.findAll();
        List<Ticket> filtered = new ArrayList<>();
        if (currentUser.getRole() == Role.ADMIN) {
            Admin admin = (Admin) currentUser;
            filtered = allTickets.stream()
                    .filter(t -> t.getStatus() == status)
                    .filter(t -> admin.canHandle(t.getTicketType()))
                    .toList();
        } else {
            filtered = allTickets.stream()
                    .filter(t -> t.getStatus() == status)
                    .toList();
        }
        return ServiceResult.success(filtered, FOUND_PREFIX + filtered.size() + " tickets with status " + status);
    }
}
