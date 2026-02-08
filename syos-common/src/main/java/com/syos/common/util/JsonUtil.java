package com.syos.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JsonUtil {
    private static final Gson gson;

    static {
        GsonBuilder builder = new GsonBuilder();
        
        builder.registerTypeAdapter(LocalDateTime.class, 
            (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        
        builder.registerTypeAdapter(LocalDateTime.class,
            (JsonDeserializer<LocalDateTime>) (json, type, context) ->
                LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        builder.registerTypeAdapter(LocalDate.class,
            (JsonSerializer<LocalDate>) (src, typeOfSrc, context) ->
                new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)));
        
        builder.registerTypeAdapter(LocalDate.class,
            (JsonDeserializer<LocalDate>) (json, type, context) ->
                LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE));
        
        gson = builder.create();
    }

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    // Method 1: For simple classes
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }
    
    // Method 2: For generic types (Lists, etc.) - THIS IS REQUIRED
    public static <T> T fromJson(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }
}