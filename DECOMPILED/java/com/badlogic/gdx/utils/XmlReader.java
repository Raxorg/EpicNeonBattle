package com.badlogic.gdx.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.compression.lzma.Base;
import com.badlogic.gdx.utils.compression.lzma.Encoder;
import com.epicness.neonbattle.android.BuildConfig;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

public class XmlReader {
    private static final byte[] _xml_actions = init__xml_actions_0();
    private static final short[] _xml_index_offsets = init__xml_index_offsets_0();
    private static final byte[] _xml_indicies = init__xml_indicies_0();
    private static final byte[] _xml_key_offsets = init__xml_key_offsets_0();
    private static final byte[] _xml_range_lengths = init__xml_range_lengths_0();
    private static final byte[] _xml_single_lengths = init__xml_single_lengths_0();
    private static final byte[] _xml_trans_actions = init__xml_trans_actions_0();
    private static final char[] _xml_trans_keys = init__xml_trans_keys_0();
    private static final byte[] _xml_trans_targs = init__xml_trans_targs_0();
    static final int xml_en_elementBody = 15;
    static final int xml_en_main = 1;
    static final int xml_error = 0;
    static final int xml_first_final = 34;
    static final int xml_start = 1;
    private Element current;
    private final Array<Element> elements = new Array(8);
    private Element root;
    private final StringBuilder textBuffer = new StringBuilder(64);

    public static class Element {
        private ObjectMap<String, String> attributes;
        private Array<Element> children;
        private final String name;
        private Element parent;
        private String text;

        public Element(String name, Element parent) {
            this.name = name;
            this.parent = parent;
        }

        public String getName() {
            return this.name;
        }

        public ObjectMap<String, String> getAttributes() {
            return this.attributes;
        }

        public String getAttribute(String name) {
            if (this.attributes == null) {
                throw new GdxRuntimeException("Element " + name + " doesn't have attribute: " + name);
            }
            String value = (String) this.attributes.get(name);
            if (value != null) {
                return value;
            }
            throw new GdxRuntimeException("Element " + name + " doesn't have attribute: " + name);
        }

        public String getAttribute(String name, String defaultValue) {
            if (this.attributes == null) {
                return defaultValue;
            }
            String value = (String) this.attributes.get(name);
            if (value != null) {
                return value;
            }
            return defaultValue;
        }

        public void setAttribute(String name, String value) {
            if (this.attributes == null) {
                this.attributes = new ObjectMap(8);
            }
            this.attributes.put(name, value);
        }

        public int getChildCount() {
            if (this.children == null) {
                return XmlReader.xml_error;
            }
            return this.children.size;
        }

        public Element getChild(int index) {
            if (this.children != null) {
                return (Element) this.children.get(index);
            }
            throw new GdxRuntimeException("Element has no children: " + this.name);
        }

        public void addChild(Element element) {
            if (this.children == null) {
                this.children = new Array(8);
            }
            this.children.add(element);
        }

        public String getText() {
            return this.text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void removeChild(int index) {
            if (this.children != null) {
                this.children.removeIndex(index);
            }
        }

        public void removeChild(Element child) {
            if (this.children != null) {
                this.children.removeValue(child, true);
            }
        }

        public void remove() {
            this.parent.removeChild(this);
        }

        public Element getParent() {
            return this.parent;
        }

        public String toString() {
            return toString(BuildConfig.VERSION_NAME);
        }

        public String toString(String indent) {
            StringBuilder buffer = new StringBuilder((int) Base.kNumFullDistances);
            buffer.append(indent);
            buffer.append('<');
            buffer.append(this.name);
            if (this.attributes != null) {
                Iterator it = this.attributes.entries().iterator();
                while (it.hasNext()) {
                    Entry<String, String> entry = (Entry) it.next();
                    buffer.append(' ');
                    buffer.append((String) entry.key);
                    buffer.append("=\"");
                    buffer.append((String) entry.value);
                    buffer.append('\"');
                }
            }
            if (this.children == null && (this.text == null || this.text.length() == 0)) {
                buffer.append("/>");
            } else {
                buffer.append(">\n");
                String childIndent = indent + '\t';
                if (this.text != null && this.text.length() > 0) {
                    buffer.append(childIndent);
                    buffer.append(this.text);
                    buffer.append('\n');
                }
                if (this.children != null) {
                    Iterator it2 = this.children.iterator();
                    while (it2.hasNext()) {
                        buffer.append(((Element) it2.next()).toString(childIndent));
                        buffer.append('\n');
                    }
                }
                buffer.append(indent);
                buffer.append("</");
                buffer.append(this.name);
                buffer.append('>');
            }
            return buffer.toString();
        }

        public Element getChildByName(String name) {
            if (this.children == null) {
                return null;
            }
            for (int i = XmlReader.xml_error; i < this.children.size; i += XmlReader.xml_start) {
                Element element = (Element) this.children.get(i);
                if (element.name.equals(name)) {
                    return element;
                }
            }
            return null;
        }

        public Element getChildByNameRecursive(String name) {
            if (this.children == null) {
                return null;
            }
            for (int i = XmlReader.xml_error; i < this.children.size; i += XmlReader.xml_start) {
                Element element = (Element) this.children.get(i);
                if (element.name.equals(name)) {
                    return element;
                }
                Element found = element.getChildByNameRecursive(name);
                if (found != null) {
                    return found;
                }
            }
            return null;
        }

        public Array<Element> getChildrenByName(String name) {
            Array<Element> result = new Array();
            if (this.children != null) {
                for (int i = XmlReader.xml_error; i < this.children.size; i += XmlReader.xml_start) {
                    Element child = (Element) this.children.get(i);
                    if (child.name.equals(name)) {
                        result.add(child);
                    }
                }
            }
            return result;
        }

        public Array<Element> getChildrenByNameRecursively(String name) {
            Array<Element> result = new Array();
            getChildrenByNameRecursively(name, result);
            return result;
        }

        private void getChildrenByNameRecursively(String name, Array<Element> result) {
            if (this.children != null) {
                for (int i = XmlReader.xml_error; i < this.children.size; i += XmlReader.xml_start) {
                    Element child = (Element) this.children.get(i);
                    if (child.name.equals(name)) {
                        result.add(child);
                    }
                    child.getChildrenByNameRecursively(name, result);
                }
            }
        }

        public float getFloatAttribute(String name) {
            return Float.parseFloat(getAttribute(name));
        }

        public float getFloatAttribute(String name, float defaultValue) {
            String value = getAttribute(name, null);
            return value == null ? defaultValue : Float.parseFloat(value);
        }

        public int getIntAttribute(String name) {
            return Integer.parseInt(getAttribute(name));
        }

        public int getIntAttribute(String name, int defaultValue) {
            String value = getAttribute(name, null);
            return value == null ? defaultValue : Integer.parseInt(value);
        }

        public boolean getBooleanAttribute(String name) {
            return Boolean.parseBoolean(getAttribute(name));
        }

        public boolean getBooleanAttribute(String name, boolean defaultValue) {
            String value = getAttribute(name, null);
            return value == null ? defaultValue : Boolean.parseBoolean(value);
        }

        public String get(String name) {
            String value = get(name, null);
            if (value != null) {
                return value;
            }
            throw new GdxRuntimeException("Element " + this.name + " doesn't have attribute or child: " + name);
        }

        public String get(String name, String defaultValue) {
            String value;
            if (this.attributes != null) {
                value = (String) this.attributes.get(name);
                if (value != null) {
                    return value;
                }
            }
            Element child = getChildByName(name);
            if (child == null) {
                return defaultValue;
            }
            value = child.getText();
            if (value == null) {
                return defaultValue;
            }
            return value;
        }

        public int getInt(String name) {
            String value = get(name, null);
            if (value != null) {
                return Integer.parseInt(value);
            }
            throw new GdxRuntimeException("Element " + this.name + " doesn't have attribute or child: " + name);
        }

        public int getInt(String name, int defaultValue) {
            String value = get(name, null);
            return value == null ? defaultValue : Integer.parseInt(value);
        }

        public float getFloat(String name) {
            String value = get(name, null);
            if (value != null) {
                return Float.parseFloat(value);
            }
            throw new GdxRuntimeException("Element " + this.name + " doesn't have attribute or child: " + name);
        }

        public float getFloat(String name, float defaultValue) {
            String value = get(name, null);
            return value == null ? defaultValue : Float.parseFloat(value);
        }

        public boolean getBoolean(String name) {
            String value = get(name, null);
            if (value != null) {
                return Boolean.parseBoolean(value);
            }
            throw new GdxRuntimeException("Element " + this.name + " doesn't have attribute or child: " + name);
        }

        public boolean getBoolean(String name, boolean defaultValue) {
            String value = get(name, null);
            return value == null ? defaultValue : Boolean.parseBoolean(value);
        }
    }

    public Element parse(String xml) {
        char[] data = xml.toCharArray();
        return parse(data, xml_error, data.length);
    }

    public Element parse(Reader reader) throws IOException {
        try {
            char[] data = new char[GL20.GL_STENCIL_BUFFER_BIT];
            int offset = xml_error;
            while (true) {
                int length = reader.read(data, offset, data.length - offset);
                if (length == -1) {
                    Element parse = parse(data, xml_error, offset);
                    StreamUtils.closeQuietly(reader);
                    return parse;
                } else if (length == 0) {
                    char[] newData = new char[(data.length * 2)];
                    System.arraycopy(data, xml_error, newData, xml_error, data.length);
                    data = newData;
                } else {
                    offset += length;
                }
            }
        } catch (Throwable ex) {
            throw new SerializationException(ex);
        } catch (Throwable th) {
            StreamUtils.closeQuietly(reader);
        }
    }

    public Element parse(InputStream input) throws IOException {
        try {
            Element parse = parse(new InputStreamReader(input, "UTF-8"));
            StreamUtils.closeQuietly(input);
            return parse;
        } catch (Throwable ex) {
            throw new SerializationException(ex);
        } catch (Throwable th) {
            StreamUtils.closeQuietly(input);
        }
    }

    public Element parse(FileHandle file) throws IOException {
        try {
            return parse(file.reader("UTF-8"));
        } catch (Exception ex) {
            throw new SerializationException("Error parsing file: " + file, ex);
        }
    }

    public Element parse(char[] data, int offset, int length) {
        int p = offset;
        int pe = length;
        int s = xml_error;
        String attributeName = null;
        boolean hasBody = false;
        int cs = xml_start;
        int _goto_targ = xml_error;
        while (true) {
            switch (_goto_targ) {
                case xml_error /*0*/:
                    if (p != pe) {
                        if (cs == 0) {
                            _goto_targ = 5;
                            break;
                        }
                    }
                    _goto_targ = 4;
                    continue;
                case xml_start /*1*/:
                    int _lower;
                    int _upper;
                    int _mid;
                    int _acts;
                    int _acts2;
                    int _nacts;
                    int _nacts2;
                    char c;
                    int end;
                    boolean entityFound;
                    int current;
                    int current2;
                    int entityStart;
                    String str;
                    String value;
                    StringBuilder stringBuilder;
                    int _keys = _xml_key_offsets[cs];
                    int _trans = _xml_index_offsets[cs];
                    int _klen = _xml_single_lengths[cs];
                    if (_klen > 0) {
                        _lower = _keys;
                        _upper = (_keys + _klen) - 1;
                        while (_upper >= _lower) {
                            _mid = _lower + ((_upper - _lower) >> xml_start);
                            if (data[p] < _xml_trans_keys[_mid]) {
                                _upper = _mid - 1;
                            } else {
                                if (data[p] > _xml_trans_keys[_mid]) {
                                    _lower = _mid + xml_start;
                                } else {
                                    _trans += _mid - _keys;
                                    _trans = _xml_indicies[_trans];
                                    cs = _xml_trans_targs[_trans];
                                    if (_xml_trans_actions[_trans] != (byte) 0) {
                                        _acts = _xml_trans_actions[_trans];
                                        _acts2 = _acts + xml_start;
                                        _nacts = _xml_actions[_acts];
                                        while (true) {
                                            _nacts2 = _nacts - 1;
                                            if (_nacts > 0) {
                                                _acts = _acts2 + xml_start;
                                                switch (_xml_actions[_acts2]) {
                                                    case xml_error /*0*/:
                                                        s = p;
                                                        break;
                                                    case xml_start /*1*/:
                                                        c = data[s];
                                                        if (c == '?' && c != '!') {
                                                            hasBody = true;
                                                            open(new String(data, s, p - s));
                                                            break;
                                                        }
                                                        if (data[s + xml_start] != '[' && data[s + 2] == 'C' && data[s + 3] == 'D' && data[s + 4] == 'A' && data[s + 5] == 'T' && data[s + 6] == 'A' && data[s + 7] == '[') {
                                                            s += 8;
                                                            p = s + 2;
                                                            while (true) {
                                                                if (data[p - 2] == ']' && data[p - 1] == ']' && data[p] == '>') {
                                                                    text(new String(data, s, (p - s) - 2));
                                                                } else {
                                                                    p += xml_start;
                                                                }
                                                            }
                                                        } else if (c != '!' && data[s + xml_start] == '-' && data[s + 2] == '-') {
                                                            p = s + 3;
                                                            while (true) {
                                                                if (data[p] == '-' && data[p + xml_start] == '-' && data[p + 2] == '>') {
                                                                    p += 2;
                                                                } else {
                                                                    p += xml_start;
                                                                }
                                                            }
                                                        } else {
                                                            while (data[p] != '>') {
                                                                p += xml_start;
                                                            }
                                                        }
                                                        cs = xml_en_elementBody;
                                                        _goto_targ = 2;
                                                        continue;
                                                        continue;
                                                        continue;
                                                        break;
                                                    case Base.kNumLenToPosStatesBits /*2*/:
                                                        hasBody = false;
                                                        close();
                                                        cs = xml_en_elementBody;
                                                        _goto_targ = 2;
                                                        continue;
                                                        continue;
                                                    case Base.kNumMidLenBits /*3*/:
                                                        close();
                                                        cs = xml_en_elementBody;
                                                        _goto_targ = 2;
                                                        continue;
                                                        continue;
                                                    case Base.kStartPosModelIndex /*4*/:
                                                        if (hasBody) {
                                                            break;
                                                        }
                                                        cs = xml_en_elementBody;
                                                        _goto_targ = 2;
                                                        continue;
                                                        continue;
                                                    case Encoder.kPropSize /*5*/:
                                                        attributeName = new String(data, s, p - s);
                                                        break;
                                                    case com.badlogic.gdx.utils.compression.rangecoder.Encoder.kNumBitPriceShiftBits /*6*/:
                                                        attribute(attributeName, new String(data, s, p - s));
                                                        break;
                                                    case Matrix4.M31 /*7*/:
                                                        end = p;
                                                        while (end != s) {
                                                            switch (data[end - 1]) {
                                                                case Matrix4.M12 /*9*/:
                                                                case Base.kNumPosModels /*10*/:
                                                                case Matrix4.M13 /*13*/:
                                                                case DefaultTextureBinder.MAX_GLES_UNITS /*32*/:
                                                                    end--;
                                                                default:
                                                                    break;
                                                            }
                                                            entityFound = false;
                                                            current = s;
                                                            while (current != end) {
                                                                current2 = current + xml_start;
                                                                if (data[current] == '&') {
                                                                    current = current2;
                                                                } else {
                                                                    entityStart = current2;
                                                                    current = current2;
                                                                    while (current != end) {
                                                                        current2 = current + xml_start;
                                                                        if (data[current] == ';') {
                                                                            current = current2;
                                                                        } else {
                                                                            this.textBuffer.append(data, s, (entityStart - s) - 1);
                                                                            str = new String(data, entityStart, (current2 - entityStart) - 1);
                                                                            value = entity(str);
                                                                            stringBuilder = this.textBuffer;
                                                                            if (value == null) {
                                                                                value = str;
                                                                            }
                                                                            stringBuilder.append(value);
                                                                            s = current2;
                                                                            entityFound = true;
                                                                            current = current2;
                                                                        }
                                                                    }
                                                                    current2 = current;
                                                                    current = current2;
                                                                }
                                                            }
                                                            if (!entityFound) {
                                                                if (s < end) {
                                                                    this.textBuffer.append(data, s, end - s);
                                                                }
                                                                text(this.textBuffer.toString());
                                                                this.textBuffer.setLength(xml_error);
                                                                break;
                                                            }
                                                            text(new String(data, s, end - s));
                                                            break;
                                                        }
                                                        entityFound = false;
                                                        current = s;
                                                        while (current != end) {
                                                            current2 = current + xml_start;
                                                            if (data[current] == '&') {
                                                                entityStart = current2;
                                                                current = current2;
                                                                while (current != end) {
                                                                    current2 = current + xml_start;
                                                                    if (data[current] == ';') {
                                                                        this.textBuffer.append(data, s, (entityStart - s) - 1);
                                                                        str = new String(data, entityStart, (current2 - entityStart) - 1);
                                                                        value = entity(str);
                                                                        stringBuilder = this.textBuffer;
                                                                        if (value == null) {
                                                                            value = str;
                                                                        }
                                                                        stringBuilder.append(value);
                                                                        s = current2;
                                                                        entityFound = true;
                                                                        current = current2;
                                                                    } else {
                                                                        current = current2;
                                                                    }
                                                                }
                                                                current2 = current;
                                                                current = current2;
                                                            } else {
                                                                current = current2;
                                                            }
                                                        }
                                                        if (!entityFound) {
                                                            if (s < end) {
                                                                this.textBuffer.append(data, s, end - s);
                                                            }
                                                            text(this.textBuffer.toString());
                                                            this.textBuffer.setLength(xml_error);
                                                        } else {
                                                            text(new String(data, s, end - s));
                                                        }
                                                    default:
                                                        break;
                                                }
                                                _nacts = _nacts2;
                                                _acts2 = _acts;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        _keys += _klen;
                        _trans += _klen;
                    }
                    _klen = _xml_range_lengths[cs];
                    if (_klen > 0) {
                        _lower = _keys;
                        _upper = ((_klen << xml_start) + _keys) - 2;
                        while (_upper >= _lower) {
                            _mid = _lower + (((_upper - _lower) >> xml_start) & -2);
                            if (data[p] < _xml_trans_keys[_mid]) {
                                _upper = _mid - 2;
                            } else {
                                if (data[p] > _xml_trans_keys[_mid + xml_start]) {
                                    _lower = _mid + 2;
                                } else {
                                    _trans += (_mid - _keys) >> xml_start;
                                }
                            }
                        }
                        _trans += _klen;
                    }
                    _trans = _xml_indicies[_trans];
                    cs = _xml_trans_targs[_trans];
                    if (_xml_trans_actions[_trans] != (byte) 0) {
                        _acts = _xml_trans_actions[_trans];
                        _acts2 = _acts + xml_start;
                        _nacts = _xml_actions[_acts];
                        while (true) {
                            _nacts2 = _nacts - 1;
                            if (_nacts > 0) {
                                _acts = _acts2 + xml_start;
                                switch (_xml_actions[_acts2]) {
                                    case xml_error /*0*/:
                                        s = p;
                                        break;
                                    case xml_start /*1*/:
                                        c = data[s];
                                        if (c == '?') {
                                            break;
                                        }
                                        if (data[s + xml_start] != '[') {
                                            break;
                                        }
                                        if (c != '!') {
                                            break;
                                        }
                                        while (data[p] != '>') {
                                            p += xml_start;
                                        }
                                        cs = xml_en_elementBody;
                                        _goto_targ = 2;
                                        continue;
                                        continue;
                                        continue;
                                    case Base.kNumLenToPosStatesBits /*2*/:
                                        hasBody = false;
                                        close();
                                        cs = xml_en_elementBody;
                                        _goto_targ = 2;
                                        continue;
                                        continue;
                                    case Base.kNumMidLenBits /*3*/:
                                        close();
                                        cs = xml_en_elementBody;
                                        _goto_targ = 2;
                                        continue;
                                        continue;
                                    case Base.kStartPosModelIndex /*4*/:
                                        if (hasBody) {
                                            break;
                                        }
                                        cs = xml_en_elementBody;
                                        _goto_targ = 2;
                                        continue;
                                        continue;
                                    case Encoder.kPropSize /*5*/:
                                        attributeName = new String(data, s, p - s);
                                        break;
                                    case com.badlogic.gdx.utils.compression.rangecoder.Encoder.kNumBitPriceShiftBits /*6*/:
                                        attribute(attributeName, new String(data, s, p - s));
                                        break;
                                    case Matrix4.M31 /*7*/:
                                        end = p;
                                        while (end != s) {
                                            switch (data[end - 1]) {
                                                case Matrix4.M12 /*9*/:
                                                case Base.kNumPosModels /*10*/:
                                                case Matrix4.M13 /*13*/:
                                                case DefaultTextureBinder.MAX_GLES_UNITS /*32*/:
                                                    end--;
                                                default:
                                                    break;
                                            }
                                            entityFound = false;
                                            current = s;
                                            while (current != end) {
                                                current2 = current + xml_start;
                                                if (data[current] == '&') {
                                                    current = current2;
                                                } else {
                                                    entityStart = current2;
                                                    current = current2;
                                                    while (current != end) {
                                                        current2 = current + xml_start;
                                                        if (data[current] == ';') {
                                                            current = current2;
                                                        } else {
                                                            this.textBuffer.append(data, s, (entityStart - s) - 1);
                                                            str = new String(data, entityStart, (current2 - entityStart) - 1);
                                                            value = entity(str);
                                                            stringBuilder = this.textBuffer;
                                                            if (value == null) {
                                                                value = str;
                                                            }
                                                            stringBuilder.append(value);
                                                            s = current2;
                                                            entityFound = true;
                                                            current = current2;
                                                        }
                                                    }
                                                    current2 = current;
                                                    current = current2;
                                                }
                                            }
                                            if (!entityFound) {
                                                text(new String(data, s, end - s));
                                                break;
                                            }
                                            if (s < end) {
                                                this.textBuffer.append(data, s, end - s);
                                            }
                                            text(this.textBuffer.toString());
                                            this.textBuffer.setLength(xml_error);
                                            break;
                                        }
                                        entityFound = false;
                                        current = s;
                                        while (current != end) {
                                            current2 = current + xml_start;
                                            if (data[current] == '&') {
                                                entityStart = current2;
                                                current = current2;
                                                while (current != end) {
                                                    current2 = current + xml_start;
                                                    if (data[current] == ';') {
                                                        this.textBuffer.append(data, s, (entityStart - s) - 1);
                                                        str = new String(data, entityStart, (current2 - entityStart) - 1);
                                                        value = entity(str);
                                                        stringBuilder = this.textBuffer;
                                                        if (value == null) {
                                                            value = str;
                                                        }
                                                        stringBuilder.append(value);
                                                        s = current2;
                                                        entityFound = true;
                                                        current = current2;
                                                    } else {
                                                        current = current2;
                                                    }
                                                }
                                                current2 = current;
                                                current = current2;
                                            } else {
                                                current = current2;
                                            }
                                        }
                                        if (!entityFound) {
                                            text(new String(data, s, end - s));
                                        } else {
                                            if (s < end) {
                                                this.textBuffer.append(data, s, end - s);
                                            }
                                            text(this.textBuffer.toString());
                                            this.textBuffer.setLength(xml_error);
                                        }
                                    default:
                                        break;
                                }
                                _nacts = _nacts2;
                                _acts2 = _acts;
                            }
                        }
                    }
                    break;
                case Base.kNumLenToPosStatesBits /*2*/:
                    if (cs != 0) {
                        p += xml_start;
                        if (p == pe) {
                            break;
                        }
                        _goto_targ = xml_start;
                        break;
                    }
                    _goto_targ = 5;
                    continue;
                default:
                    break;
            }
            if (p < pe) {
                int lineNumber = xml_start;
                for (int i = xml_error; i < p; i += xml_start) {
                    if (data[i] == '\n') {
                        lineNumber += xml_start;
                    }
                }
                throw new SerializationException("Error parsing XML on line " + lineNumber + " near: " + new String(data, p, Math.min(32, pe - p)));
            } else if (this.elements.size != 0) {
                Element element = (Element) this.elements.peek();
                this.elements.clear();
                throw new SerializationException("Error parsing XML, unclosed element: " + element.getName());
            } else {
                Element root = this.root;
                this.root = null;
                return root;
            }
        }
    }

    private static byte[] init__xml_actions_0() {
        return new byte[]{(byte) 0, (byte) 1, (byte) 0, (byte) 1, (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 3, (byte) 1, (byte) 4, (byte) 1, (byte) 5, (byte) 1, (byte) 6, (byte) 1, (byte) 7, (byte) 2, (byte) 0, (byte) 6, (byte) 2, (byte) 1, (byte) 4, (byte) 2, (byte) 2, (byte) 4};
    }

    private static byte[] init__xml_key_offsets_0() {
        return new byte[]{(byte) 0, (byte) 0, (byte) 4, (byte) 9, (byte) 14, (byte) 20, (byte) 26, (byte) 30, (byte) 35, (byte) 36, (byte) 37, (byte) 42, (byte) 46, (byte) 50, (byte) 51, (byte) 52, (byte) 56, (byte) 57, (byte) 62, (byte) 67, (byte) 73, (byte) 79, (byte) 83, (byte) 88, (byte) 89, (byte) 90, (byte) 95, (byte) 99, (byte) 103, (byte) 104, (byte) 108, (byte) 109, (byte) 110, (byte) 111, (byte) 112, (byte) 115};
    }

    private static char[] init__xml_trans_keys_0() {
        return new char[]{' ', '<', '\t', '\r', ' ', '/', '>', '\t', '\r', ' ', '/', '>', '\t', '\r', ' ', '/', '=', '>', '\t', '\r', ' ', '/', '=', '>', '\t', '\r', ' ', '=', '\t', '\r', ' ', '\"', '\'', '\t', '\r', '\"', '\"', ' ', '/', '>', '\t', '\r', ' ', '>', '\t', '\r', ' ', '>', '\t', '\r', '\'', '\'', ' ', '<', '\t', '\r', '<', ' ', '/', '>', '\t', '\r', ' ', '/', '>', '\t', '\r', ' ', '/', '=', '>', '\t', '\r', ' ', '/', '=', '>', '\t', '\r', ' ', '=', '\t', '\r', ' ', '\"', '\'', '\t', '\r', '\"', '\"', ' ', '/', '>', '\t', '\r', ' ', '>', '\t', '\r', ' ', '>', '\t', '\r', '<', ' ', '/', '\t', '\r', '>', '>', '\'', '\'', ' ', '\t', '\r', '\u0000'};
    }

    private static byte[] init__xml_single_lengths_0() {
        return new byte[]{(byte) 0, (byte) 2, (byte) 3, (byte) 3, (byte) 4, (byte) 4, (byte) 2, (byte) 3, (byte) 1, (byte) 1, (byte) 3, (byte) 2, (byte) 2, (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 3, (byte) 3, (byte) 4, (byte) 4, (byte) 2, (byte) 3, (byte) 1, (byte) 1, (byte) 3, (byte) 2, (byte) 2, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 0};
    }

    private static byte[] init__xml_range_lengths_0() {
        return new byte[]{(byte) 0, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 0, (byte) 0, (byte) 1, (byte) 1, (byte) 1, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 0, (byte) 0, (byte) 1, (byte) 1, (byte) 1, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0};
    }

    private static short[] init__xml_index_offsets_0() {
        return new short[]{(short) 0, (short) 0, (short) 4, (short) 9, (short) 14, (short) 20, (short) 26, (short) 30, (short) 35, (short) 37, (short) 39, (short) 44, (short) 48, (short) 52, (short) 54, (short) 56, (short) 60, (short) 62, (short) 67, (short) 72, (short) 78, (short) 84, (short) 88, (short) 93, (short) 95, (short) 97, (short) 102, (short) 106, (short) 110, (short) 112, (short) 116, (short) 118, (short) 120, (short) 122, (short) 124, (short) 127};
    }

    private static byte[] init__xml_indicies_0() {
        return new byte[]{(byte) 0, (byte) 2, (byte) 0, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2, (byte) 3, (byte) 5, (byte) 6, (byte) 7, (byte) 5, (byte) 4, (byte) 9, (byte) 10, (byte) 1, (byte) 11, (byte) 9, (byte) 8, (byte) 13, (byte) 1, (byte) 14, (byte) 1, (byte) 13, (byte) 12, (byte) 15, (byte) 16, (byte) 15, (byte) 1, (byte) 16, (byte) 17, (byte) 18, (byte) 16, (byte) 1, (byte) 20, (byte) 19, (byte) 22, (byte) 21, (byte) 9, (byte) 10, (byte) 11, (byte) 9, (byte) 1, (byte) 23, (byte) 24, (byte) 23, (byte) 1, (byte) 25, (byte) 11, (byte) 25, (byte) 1, (byte) 20, (byte) 26, (byte) 22, (byte) 27, (byte) 29, (byte) 30, (byte) 29, (byte) 28, (byte) 32, (byte) 31, (byte) 30, (byte) 34, (byte) 1, (byte) 30, (byte) 33, (byte) 36, (byte) 37, (byte) 38, (byte) 36, (byte) 35, (byte) 40, (byte) 41, (byte) 1, (byte) 42, (byte) 40, (byte) 39, (byte) 44, (byte) 1, (byte) 45, (byte) 1, (byte) 44, (byte) 43, (byte) 46, (byte) 47, (byte) 46, (byte) 1, (byte) 47, (byte) 48, (byte) 49, (byte) 47, (byte) 1, (byte) 51, (byte) 50, (byte) 53, (byte) 52, (byte) 40, (byte) 41, (byte) 42, (byte) 40, (byte) 1, (byte) 54, (byte) 55, (byte) 54, (byte) 1, (byte) 56, (byte) 42, (byte) 56, (byte) 1, (byte) 57, (byte) 1, (byte) 57, (byte) 34, (byte) 57, (byte) 1, (byte) 1, (byte) 58, (byte) 59, (byte) 58, (byte) 51, (byte) 60, (byte) 53, (byte) 61, (byte) 62, (byte) 62, (byte) 1, (byte) 1, (byte) 0};
    }

    private static byte[] init__xml_trans_targs_0() {
        return new byte[]{(byte) 1, (byte) 0, (byte) 2, (byte) 3, (byte) 3, (byte) 4, (byte) 11, (byte) 34, (byte) 5, (byte) 4, (byte) 11, (byte) 34, (byte) 5, (byte) 6, (byte) 7, (byte) 6, (byte) 7, (byte) 8, (byte) 13, (byte) 9, (byte) 10, (byte) 9, (byte) 10, (byte) 12, (byte) 34, (byte) 12, (byte) 14, (byte) 14, (byte) 16, (byte) 15, (byte) 17, (byte) 16, (byte) 17, (byte) 18, (byte) 30, (byte) 18, (byte) 19, (byte) 26, (byte) 28, (byte) 20, (byte) 19, (byte) 26, (byte) 28, (byte) 20, (byte) 21, (byte) 22, (byte) 21, (byte) 22, (byte) 23, (byte) 32, (byte) 24, (byte) 25, (byte) 24, (byte) 25, (byte) 27, (byte) 28, (byte) 27, (byte) 29, (byte) 31, (byte) 35, (byte) 33, (byte) 33, (byte) 34};
    }

    private static byte[] init__xml_trans_actions_0() {
        return new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 3, (byte) 3, (byte) 20, (byte) 1, (byte) 0, (byte) 0, (byte) 9, (byte) 0, (byte) 11, (byte) 11, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 17, (byte) 0, (byte) 13, (byte) 5, (byte) 23, (byte) 0, (byte) 1, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 15, (byte) 1, (byte) 0, (byte) 0, (byte) 3, (byte) 3, (byte) 20, (byte) 1, (byte) 0, (byte) 0, (byte) 9, (byte) 0, (byte) 11, (byte) 11, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 17, (byte) 0, (byte) 13, (byte) 5, (byte) 23, (byte) 0, (byte) 0, (byte) 0, (byte) 7, (byte) 1, (byte) 0, (byte) 0};
    }

    protected void open(String name) {
        Element child = new Element(name, this.current);
        Element parent = this.current;
        if (parent != null) {
            parent.addChild(child);
        }
        this.elements.add(child);
        this.current = child;
    }

    protected void attribute(String name, String value) {
        this.current.setAttribute(name, value);
    }

    protected String entity(String name) {
        if (name.equals("lt")) {
            return "<";
        }
        if (name.equals("gt")) {
            return ">";
        }
        if (name.equals("amp")) {
            return "&";
        }
        if (name.equals("apos")) {
            return "'";
        }
        if (name.equals("quot")) {
            return "\"";
        }
        if (name.startsWith("#x")) {
            return Character.toString((char) Integer.parseInt(name.substring(2), 16));
        }
        return null;
    }

    protected void text(String text) {
        String existing = this.current.getText();
        Element element = this.current;
        if (existing != null) {
            text = existing + text;
        }
        element.setText(text);
    }

    protected void close() {
        this.root = (Element) this.elements.pop();
        this.current = this.elements.size > 0 ? (Element) this.elements.peek() : null;
    }
}
