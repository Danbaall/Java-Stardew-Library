package ir.ac.kntu.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonFileManager {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        MAPPER.configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false
        );
    }

    public static <T> List<T> readList(File file, Class<T> clazz) {
        try {
            if (!file.exists() || file.length() == 0) {
                return new ArrayList<>();
            }
            CollectionType listType =
                MAPPER.getTypeFactory().constructCollectionType(
                    ArrayList.class,
                    clazz
                );
            return MAPPER.readValue(file, listType);
        } catch (IOException e) {
            throw new IllegalStateException(
                "Failed to read JSON from " + file.getName(),
                e
            );
        }
    }

    public static <T> void writeList(File file, List<T> list) {
        try {
            if (
                file.getParentFile() != null && !file.getParentFile().exists()
            ) {
                file.getParentFile().mkdirs();
            }
            MAPPER.writeValue(file, list);
        } catch (IOException e) {
            throw new IllegalStateException(
                "Failed to write JSON to " + file.getName(),
                e
            );
        }
    }

    public static <T> T readSingle(File file, Class<T> clazz) {
        try {
            if (!file.exists() || file.length() == 0) {
                return null;
            }
            return MAPPER.readValue(file, clazz);
        } catch (IOException e) {
            throw new IllegalStateException(
                "Failed to read JSON from " + file.getName(),
                e
            );
        }
    }

    public static <T> void writeSingle(File file, T object) {
        try {
            if (
                file.getParentFile() != null && !file.getParentFile().exists()
            ) {
                file.getParentFile().mkdirs();
            }
            MAPPER.writeValue(file, object);
        } catch (IOException e) {
            throw new IllegalStateException(
                "Failed to write JSON to " + file.getName(),
                e
            );
        }
    }
}
