package ir.ac.kntu.repo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ir.ac.kntu.entities.module.Transaction;
import ir.ac.kntu.util.JsonFileManager;

public class TransactionRepo {
    private static final String FILE_PATH = "data/transactions.json";
    private final File file;
    private final Map<String, Transaction> transcsById;

    public TransactionRepo() {
        this.file = new File(FILE_PATH);
        List<Transaction> loaded = JsonFileManager.readList(file, Transaction.class);
        transcsById = new HashMap<>();
        for (Transaction t : loaded) {
            transcsById.put(t.getId(), t);
        }
    }

    public List<Transaction> findAll() {
        return new ArrayList<>(transcsById.values());
    }

    public Transaction findById(String id) {
        return transcsById.get(id);
    }

    public List<Transaction> findByUserId(String userId) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transcsById.values()) {
            if (t.userId().equals(userId)) {
                result.add(t);
            }
        }
        return result;
    }

    public void save(Transaction transc) {
        transcsById.put(transc.getId(), transc);
        write();
    }

    public void delete(String id) {
        transcsById.remove(id);
        write();
    }

    private void write() {
        JsonFileManager.writeList(file, new ArrayList<>(transcsById.values()));
    }
}
