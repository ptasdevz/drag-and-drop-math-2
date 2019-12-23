package com.ptasdevz.draganddropmath2;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.ptasdevz.ModelAbstractAdapter;

import java.lang.reflect.Type;

public class TutorMyPeerEleModelAbstractAdapter extends
        ModelAbstractAdapter implements JsonSerializer<TutorMyPeerElement>, JsonDeserializer<TutorMyPeerElement> {

    @Override
    public JsonElement serialize(TutorMyPeerElement src, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject result = new JsonObject();
        result.add("type", new JsonPrimitive(src.getClass().getName()));
        result.add("properties", jsonSerializationContext.serialize(src, src.getClass()));
        return result;
    }

    @Override
    public TutorMyPeerElement deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
        return (TutorMyPeerElement) getDeserializeObject(jsonElement, type, jsonDeserializationContext);
    }
}
