package ir.ac.kntu.repo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ir.ac.kntu.entities.enums.BorrowStatus;
import ir.ac.kntu.entities.module.BorrowRecord;
import ir.ac.kntu.util.JsonFileManager;

public class BorrowHistoryRepo {
    private static final String FILE_PATH = "data/records.json";
    private final File file;
    private final Map<String, BorrowRecord> recordsById;

    public BorrowHistoryRepo() {
        this.file = new File(FILE_PATH);
        List<BorrowRecord> loaded = JsonFileManager.readList(file, BorrowRecord.class);
        recordsById = new HashMap<>();
        for (BorrowRecord r : loaded) {
            recordsById.put(r.getRecordId(), r);
        }
    }

    public List<BorrowRecord> findAll() {
        return new ArrayList<>(recordsById.values());
    }

    public BorrowRecord findById(String recordId) {
        return recordsById.get(recordId);
    }

    public List<BorrowRecord> findByUserId(String userId) {
        List<BorrowRecord> result = new ArrayList<>();
        for (BorrowRecord r : recordsById.values()) {
            if (r.getUserId().equals(userId)) {
                result.add(r);
            }
        }
        return result;
    }

    public List<BorrowRecord> findActiveByUserId(String userId) {
        List<BorrowRecord> result = new ArrayList<>();
        for (BorrowRecord r : recordsById.values()) {
            if (r.getUserId().equals(userId)
                    && r.getStatus() != BorrowStatus.RETURNED) {
                result.add(r);
            }
        }
        return result;
    }

    public void save(BorrowRecord record) {
        recordsById.put(record.getRecordId(), record);
        write();
    }

    public void delete(String recordId) {
        recordsById.remove(recordId);
        write();
    }

    private void write() {
        JsonFileManager.writeList(file, new ArrayList<>(recordsById.values()));
    }
}
