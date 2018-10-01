package com.badlogic.gdx.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue.PrettyPrintSettings;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.OrderedMap.OrderedMapValues;
import com.badlogic.gdx.utils.reflect.ArrayReflection;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Constructor;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Json {
    private static final boolean debug = false;
    private final ObjectMap<Class, Object[]> classToDefaultValues;
    private final ObjectMap<Class, Serializer> classToSerializer;
    private final ObjectMap<Class, String> classToTag;
    private Serializer defaultSerializer;
    private boolean enumNames;
    private final Object[] equals1;
    private final Object[] equals2;
    private boolean ignoreUnknownFields;
    private OutputType outputType;
    private boolean quoteLongValues;
    private final ObjectMap<String, Class> tagToClass;
    private String typeName;
    private final ObjectMap<Class, OrderedMap<String, FieldMetadata>> typeToFields;
    private boolean usePrototypes;
    private JsonWriter writer;

    public interface Serializable {
        void read(Json json, JsonValue jsonValue);

        void write(Json json);
    }

    public interface Serializer<T> {
        T read(Json json, JsonValue jsonValue, Class cls);

        void write(Json json, T t, Class cls);
    }

    public static abstract class ReadOnlySerializer<T> implements Serializer<T> {
        public abstract T read(Json json, JsonValue jsonValue, Class cls);

        public void write(Json json, T t, Class knownType) {
        }
    }

    private static class FieldMetadata {
        Class elementType;
        Field field;

        public FieldMetadata(Field field) {
            this.field = field;
            int index = (ClassReflection.isAssignableFrom(ObjectMap.class, field.getType()) || ClassReflection.isAssignableFrom(Map.class, field.getType())) ? 1 : 0;
            this.elementType = field.getElementType(index);
        }
    }

    public Json() {
        this.typeName = "class";
        this.usePrototypes = true;
        this.enumNames = true;
        this.typeToFields = new ObjectMap();
        this.tagToClass = new ObjectMap();
        this.classToTag = new ObjectMap();
        this.classToSerializer = new ObjectMap();
        this.classToDefaultValues = new ObjectMap();
        this.equals1 = new Object[]{null};
        this.equals2 = new Object[]{null};
        this.outputType = OutputType.minimal;
    }

    public Json(OutputType outputType) {
        this.typeName = "class";
        this.usePrototypes = true;
        this.enumNames = true;
        this.typeToFields = new ObjectMap();
        this.tagToClass = new ObjectMap();
        this.classToTag = new ObjectMap();
        this.classToSerializer = new ObjectMap();
        this.classToDefaultValues = new ObjectMap();
        this.equals1 = new Object[]{null};
        this.equals2 = new Object[]{null};
        this.outputType = outputType;
    }

    public void setIgnoreUnknownFields(boolean ignoreUnknownFields) {
        this.ignoreUnknownFields = ignoreUnknownFields;
    }

    public void setOutputType(OutputType outputType) {
        this.outputType = outputType;
    }

    public void setQuoteLongValues(boolean quoteLongValues) {
        this.quoteLongValues = quoteLongValues;
    }

    public void setEnumNames(boolean enumNames) {
        this.enumNames = enumNames;
    }

    public void addClassTag(String tag, Class type) {
        this.tagToClass.put(tag, type);
        this.classToTag.put(type, tag);
    }

    public Class getClass(String tag) {
        return (Class) this.tagToClass.get(tag);
    }

    public String getTag(Class type) {
        return (String) this.classToTag.get(type);
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setDefaultSerializer(Serializer defaultSerializer) {
        this.defaultSerializer = defaultSerializer;
    }

    public <T> void setSerializer(Class<T> type, Serializer<T> serializer) {
        this.classToSerializer.put(type, serializer);
    }

    public <T> Serializer<T> getSerializer(Class<T> type) {
        return (Serializer) this.classToSerializer.get(type);
    }

    public void setUsePrototypes(boolean usePrototypes) {
        this.usePrototypes = usePrototypes;
    }

    public void setElementType(Class type, String fieldName, Class elementType) {
        FieldMetadata metadata = (FieldMetadata) getFields(type).get(fieldName);
        if (metadata == null) {
            throw new SerializationException("Field not found: " + fieldName + " (" + type.getName() + ")");
        }
        metadata.elementType = elementType;
    }

    private OrderedMap<String, FieldMetadata> getFields(Class type) {
        OrderedMap<String, FieldMetadata> fields = (OrderedMap) this.typeToFields.get(type);
        if (fields != null) {
            return fields;
        }
        int i;
        Array<Class> classHierarchy = new Array();
        for (Class nextClass = type; nextClass != Object.class; nextClass = nextClass.getSuperclass()) {
            classHierarchy.add(nextClass);
        }
        ArrayList<Field> allFields = new ArrayList();
        for (i = classHierarchy.size - 1; i >= 0; i--) {
            Collections.addAll(allFields, ClassReflection.getDeclaredFields((Class) classHierarchy.get(i)));
        }
        OrderedMap<String, FieldMetadata> nameToField = new OrderedMap();
        int n = allFields.size();
        for (i = 0; i < n; i++) {
            Field field = (Field) allFields.get(i);
            if (!(field.isTransient() || field.isStatic() || field.isSynthetic())) {
                if (!field.isAccessible()) {
                    try {
                        field.setAccessible(true);
                    } catch (AccessControlException e) {
                    }
                }
                nameToField.put(field.getName(), new FieldMetadata(field));
            }
        }
        this.typeToFields.put(type, nameToField);
        return nameToField;
    }

    public String toJson(Object object) {
        return toJson(object, object == null ? null : object.getClass(), (Class) null);
    }

    public String toJson(Object object, Class knownType) {
        return toJson(object, knownType, (Class) null);
    }

    public String toJson(Object object, Class knownType, Class elementType) {
        Writer buffer = new StringWriter();
        toJson(object, knownType, elementType, buffer);
        return buffer.toString();
    }

    public void toJson(Object object, FileHandle file) {
        toJson(object, object == null ? null : object.getClass(), null, file);
    }

    public void toJson(Object object, Class knownType, FileHandle file) {
        toJson(object, knownType, null, file);
    }

    public void toJson(Object object, Class knownType, Class elementType, FileHandle file) {
        Writer writer = null;
        try {
            writer = file.writer(false, "UTF-8");
            toJson(object, knownType, elementType, writer);
            StreamUtils.closeQuietly(writer);
        } catch (Exception ex) {
            throw new SerializationException("Error writing file: " + file, ex);
        } catch (Throwable th) {
            StreamUtils.closeQuietly(writer);
        }
    }

    public void toJson(Object object, Writer writer) {
        toJson(object, object == null ? null : object.getClass(), null, writer);
    }

    public void toJson(Object object, Class knownType, Writer writer) {
        toJson(object, knownType, null, writer);
    }

    public void toJson(Object object, Class knownType, Class elementType, Writer writer) {
        setWriter(writer);
        try {
            writeValue(object, knownType, elementType);
        } finally {
            StreamUtils.closeQuietly(this.writer);
            this.writer = null;
        }
    }

    public void setWriter(Writer writer) {
        if (!(writer instanceof JsonWriter)) {
            writer = new JsonWriter(writer);
        }
        this.writer = (JsonWriter) writer;
        this.writer.setOutputType(this.outputType);
        this.writer.setQuoteLongValues(this.quoteLongValues);
    }

    public JsonWriter getWriter() {
        return this.writer;
    }

    public void writeFields(Object object) {
        ReflectionException ex;
        SerializationException ex2;
        Throwable runtimeEx;
        Class type = object.getClass();
        Object[] defaultValues = getDefaultValues(type);
        int i = 0;
        Iterator it = new OrderedMapValues(getFields(type)).iterator();
        while (it.hasNext()) {
            FieldMetadata metadata = (FieldMetadata) it.next();
            Field field = metadata.field;
            try {
                Object value = field.get(object);
                if (defaultValues != null) {
                    int i2 = i + 1;
                    try {
                        Object defaultValue = defaultValues[i];
                        if (value == null && defaultValue == null) {
                            i = i2;
                        } else {
                            if (!(value == null || defaultValue == null)) {
                                if (value.equals(defaultValue)) {
                                    i = i2;
                                } else if (value.getClass().isArray() && defaultValue.getClass().isArray()) {
                                    this.equals1[0] = value;
                                    this.equals2[0] = defaultValue;
                                    if (Arrays.deepEquals(this.equals1, this.equals2)) {
                                        i = i2;
                                    }
                                }
                            }
                            i = i2;
                        }
                    } catch (ReflectionException e) {
                        ex = e;
                        i = i2;
                    } catch (SerializationException e2) {
                        ex2 = e2;
                        i = i2;
                    } catch (Exception e3) {
                        runtimeEx = e3;
                        i = i2;
                    }
                }
                this.writer.name(field.getName());
                writeValue(value, field.getType(), metadata.elementType);
            } catch (ReflectionException e4) {
                ex = e4;
            } catch (SerializationException e5) {
                ex2 = e5;
            } catch (Exception e6) {
                runtimeEx = e6;
            }
        }
        return;
        ex2 = new SerializationException(runtimeEx);
        ex2.addTrace(field + " (" + type.getName() + ")");
        throw ex2;
        throw new SerializationException("Error accessing field: " + field.getName() + " (" + type.getName() + ")", ex);
        ex2.addTrace(field + " (" + type.getName() + ")");
        throw ex2;
    }

    private Object[] getDefaultValues(Class type) {
        SerializationException ex;
        if (!this.usePrototypes) {
            return null;
        }
        if (this.classToDefaultValues.containsKey(type)) {
            return (Object[]) this.classToDefaultValues.get(type);
        }
        try {
            Object object = newInstance(type);
            ObjectMap<String, FieldMetadata> fields = getFields(type);
            Object[] values = new Object[fields.size];
            this.classToDefaultValues.put(type, values);
            int i = 0;
            Iterator it = fields.values().iterator();
            while (it.hasNext()) {
                Field field = ((FieldMetadata) it.next()).field;
                int i2 = i + 1;
                try {
                    values[i] = field.get(object);
                    i = i2;
                } catch (ReflectionException ex2) {
                    throw new SerializationException("Error accessing field: " + field.getName() + " (" + type.getName() + ")", ex2);
                } catch (SerializationException ex3) {
                    ex3.addTrace(field + " (" + type.getName() + ")");
                    throw ex3;
                } catch (Throwable runtimeEx) {
                    ex3 = new SerializationException(runtimeEx);
                    ex3.addTrace(field + " (" + type.getName() + ")");
                    throw ex3;
                }
            }
            return values;
        } catch (Exception e) {
            this.classToDefaultValues.put(type, null);
            return null;
        }
    }

    public void writeField(Object object, String name) {
        writeField(object, name, name, null);
    }

    public void writeField(Object object, String name, Class elementType) {
        writeField(object, name, name, elementType);
    }

    public void writeField(Object object, String fieldName, String jsonName) {
        writeField(object, fieldName, jsonName, null);
    }

    public void writeField(Object object, String fieldName, String jsonName, Class elementType) {
        SerializationException ex;
        Class type = object.getClass();
        FieldMetadata metadata = (FieldMetadata) getFields(type).get(fieldName);
        if (metadata == null) {
            throw new SerializationException("Field not found: " + fieldName + " (" + type.getName() + ")");
        }
        Field field = metadata.field;
        if (elementType == null) {
            elementType = metadata.elementType;
        }
        try {
            this.writer.name(jsonName);
            writeValue(field.get(object), field.getType(), elementType);
        } catch (ReflectionException ex2) {
            throw new SerializationException("Error accessing field: " + field.getName() + " (" + type.getName() + ")", ex2);
        } catch (SerializationException ex3) {
            ex3.addTrace(field + " (" + type.getName() + ")");
            throw ex3;
        } catch (Throwable runtimeEx) {
            ex3 = new SerializationException(runtimeEx);
            ex3.addTrace(field + " (" + type.getName() + ")");
            throw ex3;
        }
    }

    public void writeValue(String name, Object value) {
        try {
            this.writer.name(name);
            if (value == null) {
                writeValue(value, null, null);
            } else {
                writeValue(value, value.getClass(), null);
            }
        } catch (Throwable ex) {
            throw new SerializationException(ex);
        }
    }

    public void writeValue(String name, Object value, Class knownType) {
        try {
            this.writer.name(name);
            writeValue(value, knownType, null);
        } catch (Throwable ex) {
            throw new SerializationException(ex);
        }
    }

    public void writeValue(String name, Object value, Class knownType, Class elementType) {
        try {
            this.writer.name(name);
            writeValue(value, knownType, elementType);
        } catch (Throwable ex) {
            throw new SerializationException(ex);
        }
    }

    public void writeValue(Object value) {
        if (value == null) {
            writeValue(value, null, null);
        } else {
            writeValue(value, value.getClass(), null);
        }
    }

    public void writeValue(Object value, Class knownType) {
        writeValue(value, knownType, null);
    }

    public void writeValue(Object value, Class knownType, Class elementType) {
        if (value == null) {
            try {
                this.writer.value(null);
            } catch (Throwable ex) {
                throw new SerializationException(ex);
            }
        } else if ((knownType != null && knownType.isPrimitive()) || knownType == String.class || knownType == Integer.class || knownType == Boolean.class || knownType == Float.class || knownType == Long.class || knownType == Double.class || knownType == Short.class || knownType == Byte.class || knownType == Character.class) {
            this.writer.value(value);
        } else {
            Class actualType = value.getClass();
            if (actualType.isPrimitive() || actualType == String.class || actualType == Integer.class || actualType == Boolean.class || actualType == Float.class || actualType == Long.class || actualType == Double.class || actualType == Short.class || actualType == Byte.class || actualType == Character.class) {
                writeObjectStart(actualType, null);
                writeValue("value", value);
                writeObjectEnd();
            } else if (value instanceof Serializable) {
                writeObjectStart(actualType, knownType);
                ((Serializable) value).write(this);
                writeObjectEnd();
            } else {
                Serializer serializer = (Serializer) this.classToSerializer.get(actualType);
                if (serializer != null) {
                    serializer.write(this, value, knownType);
                } else if (value instanceof Array) {
                    if (knownType == null || actualType == knownType || actualType == Array.class) {
                        writeArrayStart();
                        Array array = (Array) value;
                        n = array.size;
                        for (i = 0; i < n; i++) {
                            writeValue(array.get(i), elementType, null);
                        }
                        writeArrayEnd();
                        return;
                    }
                    throw new SerializationException("Serialization of an Array other than the known type is not supported.\nKnown type: " + knownType + "\nActual type: " + actualType);
                } else if (value instanceof Collection) {
                    if (this.typeName == null || actualType == ArrayList.class || (knownType != null && knownType == actualType)) {
                        writeArrayStart();
                        for (Object item : (Collection) value) {
                            writeValue(item, elementType, null);
                        }
                        writeArrayEnd();
                        return;
                    }
                    writeObjectStart(actualType, knownType);
                    writeArrayStart("items");
                    for (Object item2 : (Collection) value) {
                        writeValue(item2, elementType, null);
                    }
                    writeArrayEnd();
                    writeObjectEnd();
                } else if (actualType.isArray()) {
                    if (elementType == null) {
                        elementType = actualType.getComponentType();
                    }
                    int length = ArrayReflection.getLength(value);
                    writeArrayStart();
                    for (i = 0; i < length; i++) {
                        writeValue(ArrayReflection.get(value, i), elementType, null);
                    }
                    writeArrayEnd();
                } else if (value instanceof ObjectMap) {
                    if (knownType == null) {
                        knownType = ObjectMap.class;
                    }
                    writeObjectStart(actualType, knownType);
                    r11 = ((ObjectMap) value).entries().iterator();
                    while (r11.hasNext()) {
                        Entry entry = (Entry) r11.next();
                        this.writer.name(convertToString(entry.key));
                        writeValue(entry.value, elementType, null);
                    }
                    writeObjectEnd();
                } else if (value instanceof ArrayMap) {
                    if (knownType == null) {
                        knownType = ArrayMap.class;
                    }
                    writeObjectStart(actualType, knownType);
                    ArrayMap map = (ArrayMap) value;
                    n = map.size;
                    for (i = 0; i < n; i++) {
                        this.writer.name(convertToString(map.keys[i]));
                        writeValue(map.values[i], elementType, null);
                    }
                    writeObjectEnd();
                } else if (value instanceof Map) {
                    if (knownType == null) {
                        knownType = HashMap.class;
                    }
                    writeObjectStart(actualType, knownType);
                    for (Map.Entry entry2 : ((Map) value).entrySet()) {
                        this.writer.name(convertToString(entry2.getKey()));
                        writeValue(entry2.getValue(), elementType, null);
                    }
                    writeObjectEnd();
                } else if (!ClassReflection.isAssignableFrom(Enum.class, actualType)) {
                    writeObjectStart(actualType, knownType);
                    writeFields(value);
                    writeObjectEnd();
                } else if (this.typeName == null || (knownType != null && knownType == actualType)) {
                    this.writer.value(convertToString((Enum) value));
                } else {
                    if (actualType.getEnumConstants() == null) {
                        actualType = actualType.getSuperclass();
                    }
                    writeObjectStart(actualType, null);
                    this.writer.name("value");
                    this.writer.value(convertToString((Enum) value));
                    writeObjectEnd();
                }
            }
        }
    }

    public void writeObjectStart(String name) {
        try {
            this.writer.name(name);
            writeObjectStart();
        } catch (Throwable ex) {
            throw new SerializationException(ex);
        }
    }

    public void writeObjectStart(String name, Class actualType, Class knownType) {
        try {
            this.writer.name(name);
            writeObjectStart(actualType, knownType);
        } catch (Throwable ex) {
            throw new SerializationException(ex);
        }
    }

    public void writeObjectStart() {
        try {
            this.writer.object();
        } catch (Throwable ex) {
            throw new SerializationException(ex);
        }
    }

    public void writeObjectStart(Class actualType, Class knownType) {
        try {
            this.writer.object();
            if (knownType == null || knownType != actualType) {
                writeType(actualType);
            }
        } catch (Throwable ex) {
            throw new SerializationException(ex);
        }
    }

    public void writeObjectEnd() {
        try {
            this.writer.pop();
        } catch (Throwable ex) {
            throw new SerializationException(ex);
        }
    }

    public void writeArrayStart(String name) {
        try {
            this.writer.name(name);
            this.writer.array();
        } catch (Throwable ex) {
            throw new SerializationException(ex);
        }
    }

    public void writeArrayStart() {
        try {
            this.writer.array();
        } catch (Throwable ex) {
            throw new SerializationException(ex);
        }
    }

    public void writeArrayEnd() {
        try {
            this.writer.pop();
        } catch (Throwable ex) {
            throw new SerializationException(ex);
        }
    }

    public void writeType(Class type) {
        if (this.typeName != null) {
            String className = getTag(type);
            if (className == null) {
                className = type.getName();
            }
            try {
                this.writer.set(this.typeName, className);
            } catch (Throwable ex) {
                throw new SerializationException(ex);
            }
        }
    }

    public <T> T fromJson(Class<T> type, Reader reader) {
        return readValue((Class) type, null, new JsonReader().parse(reader));
    }

    public <T> T fromJson(Class<T> type, Class elementType, Reader reader) {
        return readValue((Class) type, elementType, new JsonReader().parse(reader));
    }

    public <T> T fromJson(Class<T> type, InputStream input) {
        return readValue((Class) type, null, new JsonReader().parse(input));
    }

    public <T> T fromJson(Class<T> type, Class elementType, InputStream input) {
        return readValue((Class) type, elementType, new JsonReader().parse(input));
    }

    public <T> T fromJson(Class<T> type, FileHandle file) {
        try {
            return readValue((Class) type, null, new JsonReader().parse(file));
        } catch (Exception ex) {
            throw new SerializationException("Error reading file: " + file, ex);
        }
    }

    public <T> T fromJson(Class<T> type, Class elementType, FileHandle file) {
        try {
            return readValue((Class) type, elementType, new JsonReader().parse(file));
        } catch (Exception ex) {
            throw new SerializationException("Error reading file: " + file, ex);
        }
    }

    public <T> T fromJson(Class<T> type, char[] data, int offset, int length) {
        return readValue((Class) type, null, new JsonReader().parse(data, offset, length));
    }

    public <T> T fromJson(Class<T> type, Class elementType, char[] data, int offset, int length) {
        return readValue((Class) type, elementType, new JsonReader().parse(data, offset, length));
    }

    public <T> T fromJson(Class<T> type, String json) {
        return readValue((Class) type, null, new JsonReader().parse(json));
    }

    public <T> T fromJson(Class<T> type, Class elementType, String json) {
        return readValue((Class) type, elementType, new JsonReader().parse(json));
    }

    public void readField(Object object, String name, JsonValue jsonData) {
        readField(object, name, name, null, jsonData);
    }

    public void readField(Object object, String name, Class elementType, JsonValue jsonData) {
        readField(object, name, name, elementType, jsonData);
    }

    public void readField(Object object, String fieldName, String jsonName, JsonValue jsonData) {
        readField(object, fieldName, jsonName, null, jsonData);
    }

    public void readField(Object object, String fieldName, String jsonName, Class elementType, JsonValue jsonMap) {
        Class type = object.getClass();
        FieldMetadata metadata = (FieldMetadata) getFields(type).get(fieldName);
        if (metadata == null) {
            throw new SerializationException("Field not found: " + fieldName + " (" + type.getName() + ")");
        }
        Field field = metadata.field;
        if (elementType == null) {
            elementType = metadata.elementType;
        }
        readField(object, field, jsonName, elementType, jsonMap);
    }

    public void readField(Object object, Field field, String jsonName, Class elementType, JsonValue jsonMap) {
        SerializationException ex;
        JsonValue jsonValue = jsonMap.get(jsonName);
        if (jsonValue != null) {
            try {
                field.set(object, readValue(field.getType(), elementType, jsonValue));
            } catch (ReflectionException ex2) {
                throw new SerializationException("Error accessing field: " + field.getName() + " (" + field.getDeclaringClass().getName() + ")", ex2);
            } catch (SerializationException ex3) {
                ex3.addTrace(field.getName() + " (" + field.getDeclaringClass().getName() + ")");
                throw ex3;
            } catch (Throwable runtimeEx) {
                ex3 = new SerializationException(runtimeEx);
                ex3.addTrace(field.getName() + " (" + field.getDeclaringClass().getName() + ")");
                throw ex3;
            }
        }
    }

    public void readFields(Object object, JsonValue jsonMap) {
        SerializationException ex;
        Class type = object.getClass();
        ObjectMap<String, FieldMetadata> fields = getFields(type);
        for (JsonValue child = jsonMap.child; child != null; child = child.next) {
            FieldMetadata metadata = (FieldMetadata) fields.get(child.name());
            if (metadata != null) {
                Field field = metadata.field;
                try {
                    field.set(object, readValue(field.getType(), metadata.elementType, child));
                } catch (ReflectionException ex2) {
                    throw new SerializationException("Error accessing field: " + field.getName() + " (" + type.getName() + ")", ex2);
                } catch (SerializationException ex3) {
                    ex3.addTrace(field.getName() + " (" + type.getName() + ")");
                    throw ex3;
                } catch (Throwable runtimeEx) {
                    ex3 = new SerializationException(runtimeEx);
                    ex3.addTrace(field.getName() + " (" + type.getName() + ")");
                    throw ex3;
                }
            } else if (!this.ignoreUnknownFields) {
                throw new SerializationException("Field not found: " + child.name() + " (" + type.getName() + ")");
            }
        }
    }

    public <T> T readValue(String name, Class<T> type, JsonValue jsonMap) {
        return readValue((Class) type, null, jsonMap.get(name));
    }

    public <T> T readValue(String name, Class<T> type, T defaultValue, JsonValue jsonMap) {
        JsonValue jsonValue = jsonMap.get(name);
        return jsonValue == null ? defaultValue : readValue((Class) type, null, jsonValue);
    }

    public <T> T readValue(String name, Class<T> type, Class elementType, JsonValue jsonMap) {
        return readValue((Class) type, elementType, jsonMap.get(name));
    }

    public <T> T readValue(String name, Class<T> type, Class elementType, T defaultValue, JsonValue jsonMap) {
        return readValue((Class) type, elementType, (Object) defaultValue, jsonMap.get(name));
    }

    public <T> T readValue(Class<T> type, Class elementType, T defaultValue, JsonValue jsonData) {
        return jsonData == null ? defaultValue : readValue((Class) type, elementType, jsonData);
    }

    public <T> T readValue(Class<T> type, JsonValue jsonData) {
        return readValue((Class) type, null, jsonData);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public <T> T readValue(java.lang.Class<T> r22, java.lang.Class r23, com.badlogic.gdx.utils.JsonValue r24) {
        /*
        r21 = this;
        if (r24 != 0) goto L_0x0004;
    L_0x0002:
        r15 = 0;
    L_0x0003:
        return r15;
    L_0x0004:
        r18 = r24.isObject();
        if (r18 == 0) goto L_0x0502;
    L_0x000a:
        r0 = r21;
        r0 = r0.typeName;
        r18 = r0;
        if (r18 != 0) goto L_0x004b;
    L_0x0012:
        r5 = 0;
    L_0x0013:
        if (r5 == 0) goto L_0x002e;
    L_0x0015:
        r0 = r21;
        r0 = r0.typeName;
        r18 = r0;
        r0 = r24;
        r1 = r18;
        r0.remove(r1);
        r0 = r21;
        r22 = r0.getClass(r5);
        if (r22 != 0) goto L_0x002e;
    L_0x002a:
        r22 = com.badlogic.gdx.utils.reflect.ClassReflection.forName(r5);	 Catch:{ ReflectionException -> 0x005e }
    L_0x002e:
        if (r22 != 0) goto L_0x006a;
    L_0x0030:
        r0 = r21;
        r0 = r0.defaultSerializer;
        r18 = r0;
        if (r18 == 0) goto L_0x0067;
    L_0x0038:
        r0 = r21;
        r0 = r0.defaultSerializer;
        r18 = r0;
        r0 = r18;
        r1 = r21;
        r2 = r24;
        r3 = r22;
        r15 = r0.read(r1, r2, r3);
        goto L_0x0003;
    L_0x004b:
        r0 = r21;
        r0 = r0.typeName;
        r18 = r0;
        r19 = 0;
        r0 = r24;
        r1 = r18;
        r2 = r19;
        r5 = r0.getString(r1, r2);
        goto L_0x0013;
    L_0x005e:
        r9 = move-exception;
        r18 = new com.badlogic.gdx.utils.SerializationException;
        r0 = r18;
        r0.<init>(r9);
        throw r18;
    L_0x0067:
        r15 = r24;
        goto L_0x0003;
    L_0x006a:
        r0 = r21;
        r0 = r0.typeName;
        r18 = r0;
        if (r18 == 0) goto L_0x00ac;
    L_0x0072:
        r18 = java.util.Collection.class;
        r0 = r18;
        r1 = r22;
        r18 = com.badlogic.gdx.utils.reflect.ClassReflection.isAssignableFrom(r0, r1);
        if (r18 == 0) goto L_0x00ac;
    L_0x007e:
        r18 = "items";
        r0 = r24;
        r1 = r18;
        r24 = r0.get(r1);
        r12 = r24;
    L_0x008a:
        if (r22 == 0) goto L_0x01cf;
    L_0x008c:
        r0 = r21;
        r0 = r0.classToSerializer;
        r18 = r0;
        r0 = r18;
        r1 = r22;
        r16 = r0.get(r1);
        r16 = (com.badlogic.gdx.utils.Json.Serializer) r16;
        if (r16 == 0) goto L_0x01cf;
    L_0x009e:
        r0 = r16;
        r1 = r21;
        r2 = r22;
        r15 = r0.read(r1, r12, r2);
        r24 = r12;
        goto L_0x0003;
    L_0x00ac:
        r0 = r21;
        r0 = r0.classToSerializer;
        r18 = r0;
        r0 = r18;
        r1 = r22;
        r16 = r0.get(r1);
        r16 = (com.badlogic.gdx.utils.Json.Serializer) r16;
        if (r16 == 0) goto L_0x00cc;
    L_0x00be:
        r0 = r16;
        r1 = r21;
        r2 = r24;
        r3 = r22;
        r15 = r0.read(r1, r2, r3);
        goto L_0x0003;
    L_0x00cc:
        r18 = java.lang.String.class;
        r0 = r22;
        r1 = r18;
        if (r0 == r1) goto L_0x0120;
    L_0x00d4:
        r18 = java.lang.Integer.class;
        r0 = r22;
        r1 = r18;
        if (r0 == r1) goto L_0x0120;
    L_0x00dc:
        r18 = java.lang.Boolean.class;
        r0 = r22;
        r1 = r18;
        if (r0 == r1) goto L_0x0120;
    L_0x00e4:
        r18 = java.lang.Float.class;
        r0 = r22;
        r1 = r18;
        if (r0 == r1) goto L_0x0120;
    L_0x00ec:
        r18 = java.lang.Long.class;
        r0 = r22;
        r1 = r18;
        if (r0 == r1) goto L_0x0120;
    L_0x00f4:
        r18 = java.lang.Double.class;
        r0 = r22;
        r1 = r18;
        if (r0 == r1) goto L_0x0120;
    L_0x00fc:
        r18 = java.lang.Short.class;
        r0 = r22;
        r1 = r18;
        if (r0 == r1) goto L_0x0120;
    L_0x0104:
        r18 = java.lang.Byte.class;
        r0 = r22;
        r1 = r18;
        if (r0 == r1) goto L_0x0120;
    L_0x010c:
        r18 = java.lang.Character.class;
        r0 = r22;
        r1 = r18;
        if (r0 == r1) goto L_0x0120;
    L_0x0114:
        r18 = java.lang.Enum.class;
        r0 = r18;
        r1 = r22;
        r18 = com.badlogic.gdx.utils.reflect.ClassReflection.isAssignableFrom(r0, r1);
        if (r18 == 0) goto L_0x0130;
    L_0x0120:
        r18 = "value";
        r0 = r21;
        r1 = r18;
        r2 = r22;
        r3 = r24;
        r15 = r0.readValue(r1, r2, r3);
        goto L_0x0003;
    L_0x0130:
        r14 = r21.newInstance(r22);
        r0 = r14 instanceof com.badlogic.gdx.utils.Json.Serializable;
        r18 = r0;
        if (r18 == 0) goto L_0x014a;
    L_0x013a:
        r18 = r14;
        r18 = (com.badlogic.gdx.utils.Json.Serializable) r18;
        r0 = r18;
        r1 = r21;
        r2 = r24;
        r0.read(r1, r2);
        r15 = r14;
        goto L_0x0003;
    L_0x014a:
        r0 = r14 instanceof com.badlogic.gdx.utils.ObjectMap;
        r18 = r0;
        if (r18 == 0) goto L_0x0173;
    L_0x0150:
        r15 = r14;
        r15 = (com.badlogic.gdx.utils.ObjectMap) r15;
        r0 = r24;
        r4 = r0.child;
    L_0x0157:
        if (r4 == 0) goto L_0x0003;
    L_0x0159:
        r18 = r4.name();
        r19 = 0;
        r0 = r21;
        r1 = r23;
        r2 = r19;
        r19 = r0.readValue(r1, r2, r4);
        r0 = r18;
        r1 = r19;
        r15.put(r0, r1);
        r4 = r4.next;
        goto L_0x0157;
    L_0x0173:
        r0 = r14 instanceof com.badlogic.gdx.utils.ArrayMap;
        r18 = r0;
        if (r18 == 0) goto L_0x019c;
    L_0x0179:
        r15 = r14;
        r15 = (com.badlogic.gdx.utils.ArrayMap) r15;
        r0 = r24;
        r4 = r0.child;
    L_0x0180:
        if (r4 == 0) goto L_0x0003;
    L_0x0182:
        r18 = r4.name();
        r19 = 0;
        r0 = r21;
        r1 = r23;
        r2 = r19;
        r19 = r0.readValue(r1, r2, r4);
        r0 = r18;
        r1 = r19;
        r15.put(r0, r1);
        r4 = r4.next;
        goto L_0x0180;
    L_0x019c:
        r0 = r14 instanceof java.util.Map;
        r18 = r0;
        if (r18 == 0) goto L_0x01c5;
    L_0x01a2:
        r15 = r14;
        r15 = (java.util.Map) r15;
        r0 = r24;
        r4 = r0.child;
    L_0x01a9:
        if (r4 == 0) goto L_0x0003;
    L_0x01ab:
        r18 = r4.name();
        r19 = 0;
        r0 = r21;
        r1 = r23;
        r2 = r19;
        r19 = r0.readValue(r1, r2, r4);
        r0 = r18;
        r1 = r19;
        r15.put(r0, r1);
        r4 = r4.next;
        goto L_0x01a9;
    L_0x01c5:
        r0 = r21;
        r1 = r24;
        r0.readFields(r14, r1);
        r15 = r14;
        goto L_0x0003;
    L_0x01cf:
        r18 = r12.isArray();
        if (r18 == 0) goto L_0x02c3;
    L_0x01d5:
        if (r22 == 0) goto L_0x01df;
    L_0x01d7:
        r18 = java.lang.Object.class;
        r0 = r22;
        r1 = r18;
        if (r0 != r1) goto L_0x01e1;
    L_0x01df:
        r22 = com.badlogic.gdx.utils.Array.class;
    L_0x01e1:
        r18 = com.badlogic.gdx.utils.Array.class;
        r0 = r18;
        r1 = r22;
        r18 = com.badlogic.gdx.utils.reflect.ClassReflection.isAssignableFrom(r0, r1);
        if (r18 == 0) goto L_0x021f;
    L_0x01ed:
        r18 = com.badlogic.gdx.utils.Array.class;
        r0 = r22;
        r1 = r18;
        if (r0 != r1) goto L_0x0212;
    L_0x01f5:
        r15 = new com.badlogic.gdx.utils.Array;
        r15.<init>();
    L_0x01fa:
        r4 = r12.child;
    L_0x01fc:
        if (r4 == 0) goto L_0x021b;
    L_0x01fe:
        r18 = 0;
        r0 = r21;
        r1 = r23;
        r2 = r18;
        r18 = r0.readValue(r1, r2, r4);
        r0 = r18;
        r15.add(r0);
        r4 = r4.next;
        goto L_0x01fc;
    L_0x0212:
        r18 = r21.newInstance(r22);
        r18 = (com.badlogic.gdx.utils.Array) r18;
        r15 = r18;
        goto L_0x01fa;
    L_0x021b:
        r24 = r12;
        goto L_0x0003;
    L_0x021f:
        r18 = java.util.Collection.class;
        r0 = r18;
        r1 = r22;
        r18 = com.badlogic.gdx.utils.reflect.ClassReflection.isAssignableFrom(r0, r1);
        if (r18 == 0) goto L_0x025b;
    L_0x022b:
        r18 = r22.isInterface();
        if (r18 == 0) goto L_0x024e;
    L_0x0231:
        r15 = new java.util.ArrayList;
        r15.<init>();
    L_0x0236:
        r4 = r12.child;
    L_0x0238:
        if (r4 == 0) goto L_0x0257;
    L_0x023a:
        r18 = 0;
        r0 = r21;
        r1 = r23;
        r2 = r18;
        r18 = r0.readValue(r1, r2, r4);
        r0 = r18;
        r15.add(r0);
        r4 = r4.next;
        goto L_0x0238;
    L_0x024e:
        r18 = r21.newInstance(r22);
        r18 = (java.util.Collection) r18;
        r15 = r18;
        goto L_0x0236;
    L_0x0257:
        r24 = r12;
        goto L_0x0003;
    L_0x025b:
        r18 = r22.isArray();
        if (r18 == 0) goto L_0x0294;
    L_0x0261:
        r6 = r22.getComponentType();
        if (r23 != 0) goto L_0x0269;
    L_0x0267:
        r23 = r6;
    L_0x0269:
        r0 = r12.size;
        r18 = r0;
        r0 = r18;
        r15 = com.badlogic.gdx.utils.reflect.ArrayReflection.newInstance(r6, r0);
        r10 = 0;
        r4 = r12.child;
        r11 = r10;
    L_0x0277:
        if (r4 == 0) goto L_0x0290;
    L_0x0279:
        r10 = r11 + 1;
        r18 = 0;
        r0 = r21;
        r1 = r23;
        r2 = r18;
        r18 = r0.readValue(r1, r2, r4);
        r0 = r18;
        com.badlogic.gdx.utils.reflect.ArrayReflection.set(r15, r11, r0);
        r4 = r4.next;
        r11 = r10;
        goto L_0x0277;
    L_0x0290:
        r24 = r12;
        goto L_0x0003;
    L_0x0294:
        r18 = new com.badlogic.gdx.utils.SerializationException;
        r19 = new java.lang.StringBuilder;
        r19.<init>();
        r20 = "Unable to convert value to required type: ";
        r19 = r19.append(r20);
        r0 = r19;
        r19 = r0.append(r12);
        r20 = " (";
        r19 = r19.append(r20);
        r20 = r22.getName();
        r19 = r19.append(r20);
        r20 = ")";
        r19 = r19.append(r20);
        r19 = r19.toString();
        r18.<init>(r19);
        throw r18;
    L_0x02c3:
        r18 = r12.isNumber();
        if (r18 == 0) goto L_0x04fe;
    L_0x02c9:
        if (r22 == 0) goto L_0x02db;
    L_0x02cb:
        r18 = java.lang.Float.TYPE;	 Catch:{ NumberFormatException -> 0x0383 }
        r0 = r22;
        r1 = r18;
        if (r0 == r1) goto L_0x02db;
    L_0x02d3:
        r18 = java.lang.Float.class;
        r0 = r22;
        r1 = r18;
        if (r0 != r1) goto L_0x02e7;
    L_0x02db:
        r18 = r12.asFloat();	 Catch:{ NumberFormatException -> 0x0383 }
        r15 = java.lang.Float.valueOf(r18);	 Catch:{ NumberFormatException -> 0x0383 }
        r24 = r12;
        goto L_0x0003;
    L_0x02e7:
        r18 = java.lang.Integer.TYPE;	 Catch:{ NumberFormatException -> 0x0383 }
        r0 = r22;
        r1 = r18;
        if (r0 == r1) goto L_0x02f7;
    L_0x02ef:
        r18 = java.lang.Integer.class;
        r0 = r22;
        r1 = r18;
        if (r0 != r1) goto L_0x0303;
    L_0x02f7:
        r18 = r12.asInt();	 Catch:{ NumberFormatException -> 0x0383 }
        r15 = java.lang.Integer.valueOf(r18);	 Catch:{ NumberFormatException -> 0x0383 }
        r24 = r12;
        goto L_0x0003;
    L_0x0303:
        r18 = java.lang.Long.TYPE;	 Catch:{ NumberFormatException -> 0x0383 }
        r0 = r22;
        r1 = r18;
        if (r0 == r1) goto L_0x0313;
    L_0x030b:
        r18 = java.lang.Long.class;
        r0 = r22;
        r1 = r18;
        if (r0 != r1) goto L_0x031f;
    L_0x0313:
        r18 = r12.asLong();	 Catch:{ NumberFormatException -> 0x0383 }
        r15 = java.lang.Long.valueOf(r18);	 Catch:{ NumberFormatException -> 0x0383 }
        r24 = r12;
        goto L_0x0003;
    L_0x031f:
        r18 = java.lang.Double.TYPE;	 Catch:{ NumberFormatException -> 0x0383 }
        r0 = r22;
        r1 = r18;
        if (r0 == r1) goto L_0x032f;
    L_0x0327:
        r18 = java.lang.Double.class;
        r0 = r22;
        r1 = r18;
        if (r0 != r1) goto L_0x033b;
    L_0x032f:
        r18 = r12.asDouble();	 Catch:{ NumberFormatException -> 0x0383 }
        r15 = java.lang.Double.valueOf(r18);	 Catch:{ NumberFormatException -> 0x0383 }
        r24 = r12;
        goto L_0x0003;
    L_0x033b:
        r18 = java.lang.String.class;
        r0 = r22;
        r1 = r18;
        if (r0 != r1) goto L_0x034b;
    L_0x0343:
        r15 = r12.asString();	 Catch:{ NumberFormatException -> 0x0383 }
        r24 = r12;
        goto L_0x0003;
    L_0x034b:
        r18 = java.lang.Short.TYPE;	 Catch:{ NumberFormatException -> 0x0383 }
        r0 = r22;
        r1 = r18;
        if (r0 == r1) goto L_0x035b;
    L_0x0353:
        r18 = java.lang.Short.class;
        r0 = r22;
        r1 = r18;
        if (r0 != r1) goto L_0x0367;
    L_0x035b:
        r18 = r12.asShort();	 Catch:{ NumberFormatException -> 0x0383 }
        r15 = java.lang.Short.valueOf(r18);	 Catch:{ NumberFormatException -> 0x0383 }
        r24 = r12;
        goto L_0x0003;
    L_0x0367:
        r18 = java.lang.Byte.TYPE;	 Catch:{ NumberFormatException -> 0x0383 }
        r0 = r22;
        r1 = r18;
        if (r0 == r1) goto L_0x0377;
    L_0x036f:
        r18 = java.lang.Byte.class;
        r0 = r22;
        r1 = r18;
        if (r0 != r1) goto L_0x0384;
    L_0x0377:
        r18 = r12.asByte();	 Catch:{ NumberFormatException -> 0x0383 }
        r15 = java.lang.Byte.valueOf(r18);	 Catch:{ NumberFormatException -> 0x0383 }
        r24 = r12;
        goto L_0x0003;
    L_0x0383:
        r18 = move-exception;
    L_0x0384:
        r24 = new com.badlogic.gdx.utils.JsonValue;
        r18 = r12.asString();
        r0 = r24;
        r1 = r18;
        r0.<init>(r1);
    L_0x0391:
        r18 = r24.isBoolean();
        if (r18 == 0) goto L_0x03c1;
    L_0x0397:
        if (r22 == 0) goto L_0x03a9;
    L_0x0399:
        r18 = java.lang.Boolean.TYPE;	 Catch:{ NumberFormatException -> 0x03b3 }
        r0 = r22;
        r1 = r18;
        if (r0 == r1) goto L_0x03a9;
    L_0x03a1:
        r18 = java.lang.Boolean.class;
        r0 = r22;
        r1 = r18;
        if (r0 != r1) goto L_0x03b4;
    L_0x03a9:
        r18 = r24.asBoolean();	 Catch:{ NumberFormatException -> 0x03b3 }
        r15 = java.lang.Boolean.valueOf(r18);	 Catch:{ NumberFormatException -> 0x03b3 }
        goto L_0x0003;
    L_0x03b3:
        r18 = move-exception;
    L_0x03b4:
        r12 = new com.badlogic.gdx.utils.JsonValue;
        r18 = r24.asString();
        r0 = r18;
        r12.<init>(r0);
        r24 = r12;
    L_0x03c1:
        r18 = r24.isString();
        if (r18 == 0) goto L_0x04fb;
    L_0x03c7:
        r17 = r24.asString();
        if (r22 == 0) goto L_0x03d5;
    L_0x03cd:
        r18 = java.lang.String.class;
        r0 = r22;
        r1 = r18;
        if (r0 != r1) goto L_0x03d9;
    L_0x03d5:
        r15 = r17;
        goto L_0x0003;
    L_0x03d9:
        r18 = java.lang.Integer.TYPE;	 Catch:{ NumberFormatException -> 0x045d }
        r0 = r22;
        r1 = r18;
        if (r0 == r1) goto L_0x03e9;
    L_0x03e1:
        r18 = java.lang.Integer.class;
        r0 = r22;
        r1 = r18;
        if (r0 != r1) goto L_0x03ef;
    L_0x03e9:
        r15 = java.lang.Integer.valueOf(r17);	 Catch:{ NumberFormatException -> 0x045d }
        goto L_0x0003;
    L_0x03ef:
        r18 = java.lang.Float.TYPE;	 Catch:{ NumberFormatException -> 0x045d }
        r0 = r22;
        r1 = r18;
        if (r0 == r1) goto L_0x03ff;
    L_0x03f7:
        r18 = java.lang.Float.class;
        r0 = r22;
        r1 = r18;
        if (r0 != r1) goto L_0x0405;
    L_0x03ff:
        r15 = java.lang.Float.valueOf(r17);	 Catch:{ NumberFormatException -> 0x045d }
        goto L_0x0003;
    L_0x0405:
        r18 = java.lang.Long.TYPE;	 Catch:{ NumberFormatException -> 0x045d }
        r0 = r22;
        r1 = r18;
        if (r0 == r1) goto L_0x0415;
    L_0x040d:
        r18 = java.lang.Long.class;
        r0 = r22;
        r1 = r18;
        if (r0 != r1) goto L_0x041b;
    L_0x0415:
        r15 = java.lang.Long.valueOf(r17);	 Catch:{ NumberFormatException -> 0x045d }
        goto L_0x0003;
    L_0x041b:
        r18 = java.lang.Double.TYPE;	 Catch:{ NumberFormatException -> 0x045d }
        r0 = r22;
        r1 = r18;
        if (r0 == r1) goto L_0x042b;
    L_0x0423:
        r18 = java.lang.Double.class;
        r0 = r22;
        r1 = r18;
        if (r0 != r1) goto L_0x0431;
    L_0x042b:
        r15 = java.lang.Double.valueOf(r17);	 Catch:{ NumberFormatException -> 0x045d }
        goto L_0x0003;
    L_0x0431:
        r18 = java.lang.Short.TYPE;	 Catch:{ NumberFormatException -> 0x045d }
        r0 = r22;
        r1 = r18;
        if (r0 == r1) goto L_0x0441;
    L_0x0439:
        r18 = java.lang.Short.class;
        r0 = r22;
        r1 = r18;
        if (r0 != r1) goto L_0x0447;
    L_0x0441:
        r15 = java.lang.Short.valueOf(r17);	 Catch:{ NumberFormatException -> 0x045d }
        goto L_0x0003;
    L_0x0447:
        r18 = java.lang.Byte.TYPE;	 Catch:{ NumberFormatException -> 0x045d }
        r0 = r22;
        r1 = r18;
        if (r0 == r1) goto L_0x0457;
    L_0x044f:
        r18 = java.lang.Byte.class;
        r0 = r22;
        r1 = r18;
        if (r0 != r1) goto L_0x045e;
    L_0x0457:
        r15 = java.lang.Byte.valueOf(r17);	 Catch:{ NumberFormatException -> 0x045d }
        goto L_0x0003;
    L_0x045d:
        r18 = move-exception;
    L_0x045e:
        r18 = java.lang.Boolean.TYPE;
        r0 = r22;
        r1 = r18;
        if (r0 == r1) goto L_0x046e;
    L_0x0466:
        r18 = java.lang.Boolean.class;
        r0 = r22;
        r1 = r18;
        if (r0 != r1) goto L_0x0474;
    L_0x046e:
        r15 = java.lang.Boolean.valueOf(r17);
        goto L_0x0003;
    L_0x0474:
        r18 = java.lang.Character.TYPE;
        r0 = r22;
        r1 = r18;
        if (r0 == r1) goto L_0x0484;
    L_0x047c:
        r18 = java.lang.Character.class;
        r0 = r22;
        r1 = r18;
        if (r0 != r1) goto L_0x0490;
    L_0x0484:
        r18 = 0;
        r18 = r17.charAt(r18);
        r15 = java.lang.Character.valueOf(r18);
        goto L_0x0003;
    L_0x0490:
        r18 = java.lang.Enum.class;
        r0 = r18;
        r1 = r22;
        r18 = com.badlogic.gdx.utils.reflect.ClassReflection.isAssignableFrom(r0, r1);
        if (r18 == 0) goto L_0x04be;
    L_0x049c:
        r18 = r22.getEnumConstants();
        r18 = (java.lang.Enum[]) r18;
        r7 = r18;
        r7 = (java.lang.Enum[]) r7;
        r10 = 0;
        r13 = r7.length;
    L_0x04a8:
        if (r10 >= r13) goto L_0x04be;
    L_0x04aa:
        r8 = r7[r10];
        r0 = r21;
        r18 = r0.convertToString(r8);
        r18 = r17.equals(r18);
        if (r18 == 0) goto L_0x04bb;
    L_0x04b8:
        r15 = r8;
        goto L_0x0003;
    L_0x04bb:
        r10 = r10 + 1;
        goto L_0x04a8;
    L_0x04be:
        r18 = java.lang.CharSequence.class;
        r0 = r22;
        r1 = r18;
        if (r0 != r1) goto L_0x04ca;
    L_0x04c6:
        r15 = r17;
        goto L_0x0003;
    L_0x04ca:
        r18 = new com.badlogic.gdx.utils.SerializationException;
        r19 = new java.lang.StringBuilder;
        r19.<init>();
        r20 = "Unable to convert value to required type: ";
        r19 = r19.append(r20);
        r0 = r19;
        r1 = r24;
        r19 = r0.append(r1);
        r20 = " (";
        r19 = r19.append(r20);
        r20 = r22.getName();
        r19 = r19.append(r20);
        r20 = ")";
        r19 = r19.append(r20);
        r19 = r19.toString();
        r18.<init>(r19);
        throw r18;
    L_0x04fb:
        r15 = 0;
        goto L_0x0003;
    L_0x04fe:
        r24 = r12;
        goto L_0x0391;
    L_0x0502:
        r12 = r24;
        goto L_0x008a;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.badlogic.gdx.utils.Json.readValue(java.lang.Class, java.lang.Class, com.badlogic.gdx.utils.JsonValue):T");
    }

    private String convertToString(Enum e) {
        return this.enumNames ? e.name() : e.toString();
    }

    private String convertToString(Object object) {
        if (object instanceof Enum) {
            return convertToString((Enum) object);
        }
        if (object instanceof Class) {
            return ((Class) object).getName();
        }
        return String.valueOf(object);
    }

    protected Object newInstance(Class type) {
        try {
            return ClassReflection.newInstance(type);
        } catch (Exception e) {
            Exception ex = e;
            try {
                Constructor constructor = ClassReflection.getDeclaredConstructor(type, new Class[0]);
                constructor.setAccessible(true);
                return constructor.newInstance(new Object[0]);
            } catch (SecurityException e2) {
                throw new SerializationException("Error constructing instance of class: " + type.getName(), ex);
            } catch (ReflectionException e3) {
                if (ClassReflection.isAssignableFrom(Enum.class, type)) {
                    if (type.getEnumConstants() == null) {
                        type = type.getSuperclass();
                    }
                    return type.getEnumConstants()[0];
                } else if (type.isArray()) {
                    throw new SerializationException("Encountered JSON object when expected array of type: " + type.getName(), ex);
                } else if (!ClassReflection.isMemberClass(type) || ClassReflection.isStaticClass(type)) {
                    throw new SerializationException("Class cannot be created (missing no-arg constructor): " + type.getName(), ex);
                } else {
                    throw new SerializationException("Class cannot be created (non-static member class): " + type.getName(), ex);
                }
            } catch (Exception privateConstructorException) {
                ex = privateConstructorException;
                throw new SerializationException("Error constructing instance of class: " + type.getName(), ex);
            }
        }
    }

    public String prettyPrint(Object object) {
        return prettyPrint(object, 0);
    }

    public String prettyPrint(String json) {
        return prettyPrint(json, 0);
    }

    public String prettyPrint(Object object, int singleLineColumns) {
        return prettyPrint(toJson(object), singleLineColumns);
    }

    public String prettyPrint(String json, int singleLineColumns) {
        return new JsonReader().parse(json).prettyPrint(this.outputType, singleLineColumns);
    }

    public String prettyPrint(Object object, PrettyPrintSettings settings) {
        return prettyPrint(toJson(object), settings);
    }

    public String prettyPrint(String json, PrettyPrintSettings settings) {
        return new JsonReader().parse(json).prettyPrint(settings);
    }
}
