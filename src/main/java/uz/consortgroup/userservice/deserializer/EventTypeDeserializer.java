package uz.consortgroup.userservice.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import uz.consortgroup.userservice.event.EventType;

import java.io.IOException;


public class EventTypeDeserializer extends JsonDeserializer<EventType> {
    @Override
    public EventType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return EventType.valueOf(p.getText());
    }
}