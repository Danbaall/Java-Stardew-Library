package ir.ac.kntu.entities.module;

import ir.ac.kntu.entities.enums.ItemType;

public record FilterFactor(
        String keyword,
        ItemType itemType,
        String category,
        Boolean isAvailable
) implements FilterCriteria<Item> {

    @Override
    public boolean matches(Item candidate) {
        if (itemType != null && candidate.getType() != itemType) {
            return false;
        }
        if (category != null && !candidate.getCategory().equalsIgnoreCase(category)) {
            return false;
        }
        if (isAvailable != null && isAvailable && !candidate.isAvailable()) {
            return false;
        }
        return keyword == null || keyword.isEmpty() || matchesKeyword(candidate);
    }

    private boolean matchesKeyword(Item item) {
        String kw = keyword.toLowerCase();
        return item.getTitle().toLowerCase().contains(kw)
                || item.getAuthor().toLowerCase().contains(kw);
    }
}
