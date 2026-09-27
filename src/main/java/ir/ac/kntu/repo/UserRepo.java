package ir.ac.kntu.repo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ir.ac.kntu.entities.module.User;
import ir.ac.kntu.util.JsonFileManager;

public class UserRepo {
    private static final String PATH_FILE = "data/users.json";
    private final File file;
    private Map<String, User> usersById;
    private Map<String, User> usersByEmail;
    private Map<String, User> usersByPhone;
    private Map<String, User> usersByUsername;

    public UserRepo() {
        this.file = new File(PATH_FILE);
        List<User> userList = JsonFileManager.readList(file, User.class);
        buildIndexes(userList);
    }

    private void buildIndexes(List<User> users) {
        usersById = new HashMap<>();
        usersByEmail = new HashMap<>();
        usersByPhone = new HashMap<>();
        usersByUsername = new HashMap<>();
        for (User u : users) {
            usersById.put(u.getId(), u);
            if (u.getEmail() != null) {
                usersByEmail.put(u.getEmail(), u);
            }
            if (u.getPhoneNumber() != null) {
                usersByPhone.put(u.getPhoneNumber(), u);
            }
            if (u.getUserName() != null) {
                usersByUsername.put(u.getUserName(), u);
            }
        }
    }

    public List<User> findAll() {
        return new ArrayList<>(usersById.values());
    }

    public User findById(String id) {
        return usersById.get(id);
    }

    public User findByEmail(String email) {
        return usersByEmail.get(email);
    }

    public User findByPhone(String phone) {
        return usersByPhone.get(phone);
    }

    public User findByUsername(String username) {
        return usersByUsername.get(username);
    }

    public void save(User user) {
        User old = usersById.get(user.getId());
        if (old != null) {
            if (old.getEmail() != null) {
                usersByEmail.remove(old.getEmail());
            }
            if (old.getPhoneNumber() != null) {
                usersByPhone.remove(old.getPhoneNumber());
            }
            if (old.getUserName() != null) {
                usersByUsername.remove(old.getUserName());
            }
        }

        usersById.put(user.getId(), user);
        if (user.getEmail() != null) {
            usersByEmail.put(user.getEmail(), user);
        }
        if (user.getPhoneNumber() != null) {
            usersByPhone.put(user.getPhoneNumber(), user);
        }
        if (user.getUserName() != null) {
            usersByUsername.put(user.getUserName(), user);
        }
        JsonFileManager.writeList(file, new ArrayList<>(usersById.values()));
    }

    public void delete(String id) {
        User removed = usersById.remove(id);
        if (removed != null) {
            if (removed.getEmail() != null) {
                usersByEmail.remove(removed.getEmail());
            }
            if (removed.getPhoneNumber() != null) {
                usersByPhone.remove(removed.getPhoneNumber());
            }
            if (removed.getUserName() != null) {
                usersByUsername.remove(removed.getUserName());
            }
            JsonFileManager.writeList(file, new ArrayList<>(usersById.values()));
        }
    }
}
