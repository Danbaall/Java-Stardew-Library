package ir.ac.kntu.repo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ir.ac.kntu.entities.module.Ticket;
import ir.ac.kntu.util.JsonFileManager;

public class TicketRepo {
    private static final String FILE_PATH = "data/tickets.json";
    private final File file;
    private final Map<String, Ticket> ticketsById;

    public TicketRepo() {
        this.file = new File(FILE_PATH);
        List<Ticket> loaded = JsonFileManager.readList(file, Ticket.class);
        ticketsById = new HashMap<>();
        for (Ticket t : loaded) {
            ticketsById.put(t.getTicketId(), t);
        }
    }

    public List<Ticket> findAll() {
        return new ArrayList<>(ticketsById.values());
    }

    public Ticket findById(String ticketId) {
        return ticketsById.get(ticketId);
    }

    public List<Ticket> findByUserId(String userId) {
        List<Ticket> result = new ArrayList<>();
        for (Ticket t : ticketsById.values()) {
            if (t.getUserId().equals(userId)) {
                result.add(t);
            }
        }
        return result;
    }

    public void save(Ticket ticket) {
        ticketsById.put(ticket.getTicketId(), ticket);
        write();
    }

    public void delete(String ticketId) {
        ticketsById.remove(ticketId);
        write();
    }

    private void write() {
        JsonFileManager.writeList(file, new ArrayList<>(ticketsById.values()));
    }
}
