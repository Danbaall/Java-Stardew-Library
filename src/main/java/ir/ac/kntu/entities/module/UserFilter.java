package ir.ac.kntu.entities.module;

import ir.ac.kntu.entities.enums.Role;

public record UserFilter(
        String username,
        String id,
        Role role
) implements FilterCriteria<User> {

    @Override
    public boolean matches(User candidate) {
        boolean idOk = id == null || id.isEmpty()
                || candidate.getId().toLowerCase().contains(id.toLowerCase());
        boolean nameOk = username == null || username.isEmpty()
                || candidate.getUserName().toLowerCase().contains(username.toLowerCase());
        boolean roleOk = role == null || candidate.getRole() == role;
        return idOk && nameOk && roleOk;
    }
}
