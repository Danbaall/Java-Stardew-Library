package ir.ac.kntu.entities.module;

import ir.ac.kntu.entities.enums.TicketType;
import java.util.List;

public interface TicketHandler {
    List<TicketType> getAssignedTypes();

    boolean canHandle(TicketType type);
}
