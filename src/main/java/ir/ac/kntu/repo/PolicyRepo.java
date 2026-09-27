package ir.ac.kntu.repo;

import java.io.File;

import ir.ac.kntu.entities.module.LibraryPolicy;
import ir.ac.kntu.util.JsonFileManager;

public class PolicyRepo {
    private static final String PATH_FILE = "data/policy.json";
    private final File file;
    private LibraryPolicy policy;

    public PolicyRepo() {
        this.file = new File(PATH_FILE);
        LibraryPolicy loaded = JsonFileManager.readSingle(file, LibraryPolicy.class);
        this.policy = (loaded != null) ? loaded : new LibraryPolicy();
        LibraryPolicy.setInstance(this.policy);
    }

    public LibraryPolicy get() {
        return policy;
    }

    public void save(LibraryPolicy updated) {
        this.policy = updated;
        LibraryPolicy.setInstance(updated);
        JsonFileManager.writeSingle(file, updated);
    }
}
