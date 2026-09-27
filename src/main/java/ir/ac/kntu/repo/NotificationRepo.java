package ir.ac.kntu.repo;

import ir.ac.kntu.entities.module.Notification;
import ir.ac.kntu.util.JsonFileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationRepo {

    private static final String PATH_FILE = "data/notifications.json";
    private final File file;
    private final LinkedHashMap<String, Notification> byId;

    public NotificationRepo() {
        this.file = new File(PATH_FILE);
        byId = new LinkedHashMap<>();
        for (Notification n : JsonFileManager.readList(file, Notification.class)) {
            byId.put(n.getId(), n);
        }
    }

    public List<Notification> findByUserId(String userId) {
        return byId.values().stream()
                .filter(n -> userId.equals(n.getUserId()))
                .collect(Collectors.toList());
    }

    public Notification findById(String id) {
        return byId.get(id);
    }

    public boolean existsByUserAndTag(String userId, String tag) {
        if (tag == null) return false;
        return byId.values().stream()
                .anyMatch(n -> userId.equals(n.getUserId()) && tag.equals(n.getTag()));
    }

    public void save(Notification notification) {
        byId.put(notification.getId(), notification);
        persist();
    }

    public void deleteAllByUserId(String userId) {
        byId.entrySet().removeIf(e -> userId.equals(e.getValue().getUserId()));
        persist();
    }

    private void persist() {
        JsonFileManager.writeList(file, new ArrayList<>(byId.values()));
    }
}
