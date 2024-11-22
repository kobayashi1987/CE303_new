
package SOMSServerJava;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LocalDateTimeAdapter handles serialization and deserialization of LocalDateTime objects.
 */
public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Serializes a LocalDateTime object to JSON.
     *
     * @param src       The LocalDateTime source.
     * @param typeOfSrc The type of the source.
     * @param context   The JSON serialization context.
     * @return A JsonElement representing the serialized LocalDateTime.
     */
    @Override
    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.format(formatter));
    }

    /**
     * Deserializes a JSON element to a LocalDateTime object.
     *
     * @param json    The JSON element.
     * @param typeOfT The type of the target.
     * @param context The JSON deserialization context.
     * @return The deserialized LocalDateTime object.
     * @throws JsonParseException If parsing fails.
     */
    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        return LocalDateTime.parse(json.getAsString(), formatter);
    }
}