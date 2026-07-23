package ec.edu.espe.eurekadesktop.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class JsonUtils {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    public static <T> T fromJson(String json, Type type) {
        return GSON.fromJson(json, type);
    }

    public static <T> List<T> fromJsonList(String json) {
        Type listType = new TypeToken<List<T>>(){}.getType();
        return GSON.fromJson(json, listType);
    }

    public static String prettyPrint(String json) {
        try {
            JsonElement element = JsonParser.parseString(json);
            return GSON.toJson(element);
        } catch (Exception e) {
            return json;
        }
    }

    public static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}