package ir.ac.kntu.repo;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ir.ac.kntu.entities.enums.ReservationStatus;
import ir.ac.kntu.entities.module.Reservation;
import ir.ac.kntu.util.JsonFileManager;

public class ReservationRepo {
    private static final String FILE_PATH = "data/reservations.json";
    private final File file;
    private final Map<String, Reservation> byId;

    public ReservationRepo() {
        file = new File(FILE_PATH);
        List<Reservation> loaded = JsonFileManager.readList(file, Reservation.class);
        byId = new HashMap<>();
        for (Reservation r : loaded) {
            byId.put(r.getReservationId(), r);
        }
    }

    public List<Reservation> findAll() {
        return new ArrayList<>(byId.values());
    }

    public Reservation findById(String id) {
        return byId.get(id);
    }

    public List<Reservation> findByUserId(String userId) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : byId.values()) {
            if (r.getUserId().equals(userId)) {
                result.add(r);
            }
        }
        return result;
    }

    public List<Reservation> findByItemId(String itemId) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : byId.values()) {
            if (r.getItemId().equals(itemId)) {
                result.add(r);
            }
        }
        return result;
    }

    public List<Reservation> findByItemIdSorted(String itemId) {
        return byId.values().stream()
                .filter(r -> r.getItemId().equals(itemId))
                .sorted(Comparator.comparing(Reservation::getReservedAt))
                .collect(Collectors.toList());
    }

    public Reservation findFirstWaiting(String itemId) {
        return byId.values().stream()
                .filter(r -> r.getItemId().equals(itemId)
                        && r.getStatus() == ReservationStatus.WAITING)
                .min(Comparator.comparing(Reservation::getReservedAt))
                .orElse(null);
    }

    public void save(Reservation res) {
        byId.put(res.getReservationId(), res);
        write();
    }

    public void delete(String id) {
        byId.remove(id);
        write();
    }

    private void write() {
        JsonFileManager.writeList(file, new ArrayList<>(byId.values()));
    }

}
