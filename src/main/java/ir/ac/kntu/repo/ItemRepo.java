package ir.ac.kntu.repo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ir.ac.kntu.entities.module.Item;
import ir.ac.kntu.util.JsonFileManager;

public class ItemRepo {
    private static final String FILE_PATH = "data/items.json";
    private final File file;
    private final Map<String, Item> itemsById;

    public ItemRepo() {
        this.file = new File(FILE_PATH);
        List<Item> loaded = JsonFileManager.readList(file, Item.class);
        itemsById = new HashMap<>();
        for (Item item : loaded) {
            itemsById.put(item.getId(), item);
        }
    }

    public List<Item> findAll() {
        return new ArrayList<>(itemsById.values());
    }

    public Item findById(String id) {
        return itemsById.get(id);
    }

    public void save(Item item) {
        itemsById.put(item.getId(), item);
        write();
    }

    public void delete(String id) {
        itemsById.remove(id);
        write();
    }

    private void write() {
        JsonFileManager.writeList(file, new ArrayList<>(itemsById.values()));
    }

    public void saveAll(List<Item> items) {
        for (Item item : items) {
            itemsById.put(item.getId(), item);
        }
        JsonFileManager.writeList(file, new ArrayList<>(itemsById.values()));
    }
}
