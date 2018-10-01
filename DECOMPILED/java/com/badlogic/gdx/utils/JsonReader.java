package com.badlogic.gdx.utils;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.utils.JsonValue.ValueType;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class JsonReader implements BaseJsonReader {
    private static final byte[] _json_actions = init__json_actions_0();
    private static final byte[] _json_eof_actions = init__json_eof_actions_0();
    private static final short[] _json_index_offsets = init__json_index_offsets_0();
    private static final byte[] _json_indicies = init__json_indicies_0();
    private static final short[] _json_key_offsets = init__json_key_offsets_0();
    private static final byte[] _json_range_lengths = init__json_range_lengths_0();
    private static final byte[] _json_single_lengths = init__json_single_lengths_0();
    private static final byte[] _json_trans_actions = init__json_trans_actions_0();
    private static final char[] _json_trans_keys = init__json_trans_keys_0();
    private static final byte[] _json_trans_targs = init__json_trans_targs_0();
    static final int json_en_array = 23;
    static final int json_en_main = 1;
    static final int json_en_object = 5;
    static final int json_error = 0;
    static final int json_first_final = 35;
    static final int json_start = 1;
    private JsonValue current;
    private final Array<JsonValue> elements = new Array(8);
    private final Array<JsonValue> lastChild = new Array(8);
    private JsonValue root;

    public JsonValue parse(String json) {
        char[] data = json.toCharArray();
        return parse(data, json_error, data.length);
    }

    public JsonValue parse(Reader reader) {
        try {
            char[] data = new char[GL20.GL_STENCIL_BUFFER_BIT];
            int offset = json_error;
            while (true) {
                int length = reader.read(data, offset, data.length - offset);
                if (length == -1) {
                    JsonValue parse = parse(data, json_error, offset);
                    StreamUtils.closeQuietly(reader);
                    return parse;
                } else if (length == 0) {
                    char[] newData = new char[(data.length * 2)];
                    System.arraycopy(data, json_error, newData, json_error, data.length);
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

    public JsonValue parse(InputStream input) {
        try {
            JsonValue parse = parse(new InputStreamReader(input, "UTF-8"));
            StreamUtils.closeQuietly(input);
            return parse;
        } catch (Throwable ex) {
            throw new SerializationException(ex);
        } catch (Throwable th) {
            StreamUtils.closeQuietly(input);
        }
    }

    public JsonValue parse(FileHandle file) {
        try {
            return parse(file.reader("UTF-8"));
        } catch (Exception ex) {
            throw new SerializationException("Error parsing file: " + file, ex);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.badlogic.gdx.utils.JsonValue parse(char[] r54, int r55, int r56) {
        /*
        r53 = this;
        r35 = r55;
        r38 = r56;
        r27 = r38;
        r45 = 0;
        r48 = 4;
        r0 = r48;
        r0 = new int[r0];
        r41 = r0;
        r40 = 0;
        r32 = new com.badlogic.gdx.utils.Array;
        r48 = 8;
        r0 = r32;
        r1 = r48;
        r0.<init>(r1);
        r33 = 0;
        r43 = 0;
        r44 = 0;
        r37 = 0;
        r25 = 0;
        if (r25 == 0) goto L_0x002e;
    L_0x0029:
        r48 = java.lang.System.out;
        r48.println();
    L_0x002e:
        r24 = 1;
        r45 = 0;
        r19 = 0;
        r12 = 0;
        r46 = r45;
    L_0x0037:
        switch(r12) {
            case 0: goto L_0x007a;
            case 1: goto L_0x0086;
            case 2: goto L_0x057f;
            case 3: goto L_0x003a;
            case 4: goto L_0x058f;
            default: goto L_0x003a;
        };
    L_0x003a:
        r45 = r46;
    L_0x003c:
        r0 = r53;
        r0 = r0.root;
        r39 = r0;
        r48 = 0;
        r0 = r48;
        r1 = r53;
        r1.root = r0;
        r48 = 0;
        r0 = r48;
        r1 = r53;
        r1.current = r0;
        r0 = r53;
        r0 = r0.lastChild;
        r48 = r0;
        r48.clear();
        r0 = r35;
        r1 = r38;
        if (r0 >= r1) goto L_0x07a9;
    L_0x0061:
        r30 = 1;
        r29 = 0;
    L_0x0065:
        r0 = r29;
        r1 = r35;
        if (r0 >= r1) goto L_0x0767;
    L_0x006b:
        r48 = r54[r29];
        r49 = 10;
        r0 = r48;
        r1 = r49;
        if (r0 != r1) goto L_0x0077;
    L_0x0075:
        r30 = r30 + 1;
    L_0x0077:
        r29 = r29 + 1;
        goto L_0x0065;
    L_0x007a:
        r0 = r35;
        r1 = r38;
        if (r0 != r1) goto L_0x0082;
    L_0x0080:
        r12 = 4;
        goto L_0x0037;
    L_0x0082:
        if (r24 != 0) goto L_0x0086;
    L_0x0084:
        r12 = 5;
        goto L_0x0037;
    L_0x0086:
        r48 = _json_key_offsets;	 Catch:{ RuntimeException -> 0x046c }
        r13 = r48[r24];	 Catch:{ RuntimeException -> 0x046c }
        r48 = _json_index_offsets;	 Catch:{ RuntimeException -> 0x046c }
        r19 = r48[r24];	 Catch:{ RuntimeException -> 0x046c }
        r48 = _json_single_lengths;	 Catch:{ RuntimeException -> 0x046c }
        r14 = r48[r24];	 Catch:{ RuntimeException -> 0x046c }
        if (r14 <= 0) goto L_0x00a0;
    L_0x0094:
        r15 = r13;
        r48 = r13 + r14;
        r20 = r48 + -1;
    L_0x0099:
        r0 = r20;
        if (r0 >= r15) goto L_0x00e4;
    L_0x009d:
        r13 = r13 + r14;
        r19 = r19 + r14;
    L_0x00a0:
        r48 = _json_range_lengths;	 Catch:{ RuntimeException -> 0x046c }
        r14 = r48[r24];	 Catch:{ RuntimeException -> 0x046c }
        if (r14 <= 0) goto L_0x00b3;
    L_0x00a6:
        r15 = r13;
        r48 = r14 << 1;
        r48 = r48 + r13;
        r20 = r48 + -2;
    L_0x00ad:
        r0 = r20;
        if (r0 >= r15) goto L_0x010d;
    L_0x00b1:
        r19 = r19 + r14;
    L_0x00b3:
        r48 = _json_indicies;	 Catch:{ RuntimeException -> 0x046c }
        r19 = r48[r19];	 Catch:{ RuntimeException -> 0x046c }
        r48 = _json_trans_targs;	 Catch:{ RuntimeException -> 0x046c }
        r24 = r48[r19];	 Catch:{ RuntimeException -> 0x046c }
        r48 = _json_trans_actions;	 Catch:{ RuntimeException -> 0x046c }
        r48 = r48[r19];	 Catch:{ RuntimeException -> 0x046c }
        if (r48 == 0) goto L_0x057f;
    L_0x00c1:
        r48 = _json_trans_actions;	 Catch:{ RuntimeException -> 0x046c }
        r10 = r48[r19];	 Catch:{ RuntimeException -> 0x046c }
        r48 = _json_actions;	 Catch:{ RuntimeException -> 0x046c }
        r11 = r10 + 1;
        r17 = r48[r10];	 Catch:{ RuntimeException -> 0x046c }
        r18 = r17;
        r36 = r35;
    L_0x00cf:
        r17 = r18 + -1;
        if (r18 <= 0) goto L_0x057d;
    L_0x00d3:
        r48 = _json_actions;	 Catch:{ RuntimeException -> 0x01d7 }
        r10 = r11 + 1;
        r48 = r48[r11];	 Catch:{ RuntimeException -> 0x01d7 }
        switch(r48) {
            case 0: goto L_0x013e;
            case 1: goto L_0x0143;
            case 2: goto L_0x0306;
            case 3: goto L_0x037b;
            case 4: goto L_0x0392;
            case 5: goto L_0x0407;
            case 6: goto L_0x041e;
            case 7: goto L_0x0495;
            case 8: goto L_0x0558;
            default: goto L_0x00dc;
        };
    L_0x00dc:
        r35 = r36;
    L_0x00de:
        r18 = r17;
        r11 = r10;
        r36 = r35;
        goto L_0x00cf;
    L_0x00e4:
        r48 = r20 - r15;
        r48 = r48 >> 1;
        r16 = r15 + r48;
        r48 = r54[r35];	 Catch:{ RuntimeException -> 0x046c }
        r49 = _json_trans_keys;	 Catch:{ RuntimeException -> 0x046c }
        r49 = r49[r16];	 Catch:{ RuntimeException -> 0x046c }
        r0 = r48;
        r1 = r49;
        if (r0 >= r1) goto L_0x00f9;
    L_0x00f6:
        r20 = r16 + -1;
        goto L_0x0099;
    L_0x00f9:
        r48 = r54[r35];	 Catch:{ RuntimeException -> 0x046c }
        r49 = _json_trans_keys;	 Catch:{ RuntimeException -> 0x046c }
        r49 = r49[r16];	 Catch:{ RuntimeException -> 0x046c }
        r0 = r48;
        r1 = r49;
        if (r0 <= r1) goto L_0x0108;
    L_0x0105:
        r15 = r16 + 1;
        goto L_0x0099;
    L_0x0108:
        r48 = r16 - r13;
        r19 = r19 + r48;
        goto L_0x00b3;
    L_0x010d:
        r48 = r20 - r15;
        r48 = r48 >> 1;
        r48 = r48 & -2;
        r16 = r15 + r48;
        r48 = r54[r35];	 Catch:{ RuntimeException -> 0x046c }
        r49 = _json_trans_keys;	 Catch:{ RuntimeException -> 0x046c }
        r49 = r49[r16];	 Catch:{ RuntimeException -> 0x046c }
        r0 = r48;
        r1 = r49;
        if (r0 >= r1) goto L_0x0124;
    L_0x0121:
        r20 = r16 + -2;
        goto L_0x00ad;
    L_0x0124:
        r48 = r54[r35];	 Catch:{ RuntimeException -> 0x046c }
        r49 = _json_trans_keys;	 Catch:{ RuntimeException -> 0x046c }
        r50 = r16 + 1;
        r49 = r49[r50];	 Catch:{ RuntimeException -> 0x046c }
        r0 = r48;
        r1 = r49;
        if (r0 <= r1) goto L_0x0136;
    L_0x0132:
        r15 = r16 + 2;
        goto L_0x00ad;
    L_0x0136:
        r48 = r16 - r13;
        r48 = r48 >> 1;
        r19 = r19 + r48;
        goto L_0x00b3;
    L_0x013e:
        r43 = 1;
        r35 = r36;
        goto L_0x00de;
    L_0x0143:
        r47 = new java.lang.String;	 Catch:{ RuntimeException -> 0x01d7 }
        r48 = r36 - r40;
        r0 = r47;
        r1 = r54;
        r2 = r40;
        r3 = r48;
        r0.<init>(r1, r2, r3);	 Catch:{ RuntimeException -> 0x01d7 }
        if (r33 == 0) goto L_0x015c;
    L_0x0154:
        r0 = r53;
        r1 = r47;
        r47 = r0.unescape(r1);	 Catch:{ RuntimeException -> 0x01d7 }
    L_0x015c:
        if (r43 == 0) goto L_0x018d;
    L_0x015e:
        r43 = 0;
        if (r25 == 0) goto L_0x017e;
    L_0x0162:
        r48 = java.lang.System.out;	 Catch:{ RuntimeException -> 0x01d7 }
        r49 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x01d7 }
        r49.<init>();	 Catch:{ RuntimeException -> 0x01d7 }
        r50 = "name: ";
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x01d7 }
        r0 = r49;
        r1 = r47;
        r49 = r0.append(r1);	 Catch:{ RuntimeException -> 0x01d7 }
        r49 = r49.toString();	 Catch:{ RuntimeException -> 0x01d7 }
        r48.println(r49);	 Catch:{ RuntimeException -> 0x01d7 }
    L_0x017e:
        r0 = r32;
        r1 = r47;
        r0.add(r1);	 Catch:{ RuntimeException -> 0x01d7 }
    L_0x0185:
        r44 = 0;
        r40 = r36;
        r35 = r36;
        goto L_0x00de;
    L_0x018d:
        r0 = r32;
        r0 = r0.size;	 Catch:{ RuntimeException -> 0x01d7 }
        r48 = r0;
        if (r48 <= 0) goto L_0x01e0;
    L_0x0195:
        r48 = r32.pop();	 Catch:{ RuntimeException -> 0x01d7 }
        r48 = (java.lang.String) r48;	 Catch:{ RuntimeException -> 0x01d7 }
        r31 = r48;
    L_0x019d:
        if (r44 == 0) goto L_0x0286;
    L_0x019f:
        r48 = "true";
        r48 = r47.equals(r48);	 Catch:{ RuntimeException -> 0x01d7 }
        if (r48 == 0) goto L_0x01e3;
    L_0x01a7:
        if (r25 == 0) goto L_0x01cb;
    L_0x01a9:
        r48 = java.lang.System.out;	 Catch:{ RuntimeException -> 0x01d7 }
        r49 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x01d7 }
        r49.<init>();	 Catch:{ RuntimeException -> 0x01d7 }
        r50 = "boolean: ";
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x01d7 }
        r0 = r49;
        r1 = r31;
        r49 = r0.append(r1);	 Catch:{ RuntimeException -> 0x01d7 }
        r50 = "=true";
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x01d7 }
        r49 = r49.toString();	 Catch:{ RuntimeException -> 0x01d7 }
        r48.println(r49);	 Catch:{ RuntimeException -> 0x01d7 }
    L_0x01cb:
        r48 = 1;
        r0 = r53;
        r1 = r31;
        r2 = r48;
        r0.bool(r1, r2);	 Catch:{ RuntimeException -> 0x01d7 }
        goto L_0x0185;
    L_0x01d7:
        r28 = move-exception;
        r45 = r46;
        r35 = r36;
    L_0x01dc:
        r37 = r28;
        goto L_0x003c;
    L_0x01e0:
        r31 = 0;
        goto L_0x019d;
    L_0x01e3:
        r48 = "false";
        r48 = r47.equals(r48);	 Catch:{ RuntimeException -> 0x01d7 }
        if (r48 == 0) goto L_0x021c;
    L_0x01eb:
        if (r25 == 0) goto L_0x020f;
    L_0x01ed:
        r48 = java.lang.System.out;	 Catch:{ RuntimeException -> 0x01d7 }
        r49 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x01d7 }
        r49.<init>();	 Catch:{ RuntimeException -> 0x01d7 }
        r50 = "boolean: ";
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x01d7 }
        r0 = r49;
        r1 = r31;
        r49 = r0.append(r1);	 Catch:{ RuntimeException -> 0x01d7 }
        r50 = "=false";
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x01d7 }
        r49 = r49.toString();	 Catch:{ RuntimeException -> 0x01d7 }
        r48.println(r49);	 Catch:{ RuntimeException -> 0x01d7 }
    L_0x020f:
        r48 = 0;
        r0 = r53;
        r1 = r31;
        r2 = r48;
        r0.bool(r1, r2);	 Catch:{ RuntimeException -> 0x01d7 }
        goto L_0x0185;
    L_0x021c:
        r48 = "null";
        r48 = r47.equals(r48);	 Catch:{ RuntimeException -> 0x01d7 }
        if (r48 == 0) goto L_0x0231;
    L_0x0224:
        r48 = 0;
        r0 = r53;
        r1 = r31;
        r2 = r48;
        r0.string(r1, r2);	 Catch:{ RuntimeException -> 0x01d7 }
        goto L_0x0185;
    L_0x0231:
        r22 = 0;
        r23 = 1;
        r29 = r40;
    L_0x0237:
        r0 = r29;
        r1 = r36;
        if (r0 >= r1) goto L_0x0246;
    L_0x023d:
        r48 = r54[r29];	 Catch:{ RuntimeException -> 0x01d7 }
        switch(r48) {
            case 43: goto L_0x02c1;
            case 45: goto L_0x02c1;
            case 46: goto L_0x02bd;
            case 48: goto L_0x02c1;
            case 49: goto L_0x02c1;
            case 50: goto L_0x02c1;
            case 51: goto L_0x02c1;
            case 52: goto L_0x02c1;
            case 53: goto L_0x02c1;
            case 54: goto L_0x02c1;
            case 55: goto L_0x02c1;
            case 56: goto L_0x02c1;
            case 57: goto L_0x02c1;
            case 69: goto L_0x02bd;
            case 101: goto L_0x02bd;
            default: goto L_0x0242;
        };
    L_0x0242:
        r22 = 0;
        r23 = 0;
    L_0x0246:
        if (r22 == 0) goto L_0x02c5;
    L_0x0248:
        if (r25 == 0) goto L_0x0274;
    L_0x024a:
        r48 = java.lang.System.out;	 Catch:{ NumberFormatException -> 0x0285 }
        r49 = new java.lang.StringBuilder;	 Catch:{ NumberFormatException -> 0x0285 }
        r49.<init>();	 Catch:{ NumberFormatException -> 0x0285 }
        r50 = "double: ";
        r49 = r49.append(r50);	 Catch:{ NumberFormatException -> 0x0285 }
        r0 = r49;
        r1 = r31;
        r49 = r0.append(r1);	 Catch:{ NumberFormatException -> 0x0285 }
        r50 = "=";
        r49 = r49.append(r50);	 Catch:{ NumberFormatException -> 0x0285 }
        r50 = java.lang.Double.parseDouble(r47);	 Catch:{ NumberFormatException -> 0x0285 }
        r49 = r49.append(r50);	 Catch:{ NumberFormatException -> 0x0285 }
        r49 = r49.toString();	 Catch:{ NumberFormatException -> 0x0285 }
        r48.println(r49);	 Catch:{ NumberFormatException -> 0x0285 }
    L_0x0274:
        r48 = java.lang.Double.parseDouble(r47);	 Catch:{ NumberFormatException -> 0x0285 }
        r0 = r53;
        r1 = r31;
        r2 = r48;
        r4 = r47;
        r0.number(r1, r2, r4);	 Catch:{ NumberFormatException -> 0x0285 }
        goto L_0x0185;
    L_0x0285:
        r48 = move-exception;
    L_0x0286:
        if (r25 == 0) goto L_0x02b2;
    L_0x0288:
        r48 = java.lang.System.out;	 Catch:{ RuntimeException -> 0x01d7 }
        r49 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x01d7 }
        r49.<init>();	 Catch:{ RuntimeException -> 0x01d7 }
        r50 = "string: ";
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x01d7 }
        r0 = r49;
        r1 = r31;
        r49 = r0.append(r1);	 Catch:{ RuntimeException -> 0x01d7 }
        r50 = "=";
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x01d7 }
        r0 = r49;
        r1 = r47;
        r49 = r0.append(r1);	 Catch:{ RuntimeException -> 0x01d7 }
        r49 = r49.toString();	 Catch:{ RuntimeException -> 0x01d7 }
        r48.println(r49);	 Catch:{ RuntimeException -> 0x01d7 }
    L_0x02b2:
        r0 = r53;
        r1 = r31;
        r2 = r47;
        r0.string(r1, r2);	 Catch:{ RuntimeException -> 0x01d7 }
        goto L_0x0185;
    L_0x02bd:
        r22 = 1;
        r23 = 0;
    L_0x02c1:
        r29 = r29 + 1;
        goto L_0x0237;
    L_0x02c5:
        if (r23 == 0) goto L_0x0286;
    L_0x02c7:
        if (r25 == 0) goto L_0x02f3;
    L_0x02c9:
        r48 = java.lang.System.out;	 Catch:{ RuntimeException -> 0x01d7 }
        r49 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x01d7 }
        r49.<init>();	 Catch:{ RuntimeException -> 0x01d7 }
        r50 = "double: ";
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x01d7 }
        r0 = r49;
        r1 = r31;
        r49 = r0.append(r1);	 Catch:{ RuntimeException -> 0x01d7 }
        r50 = "=";
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x01d7 }
        r50 = java.lang.Double.parseDouble(r47);	 Catch:{ RuntimeException -> 0x01d7 }
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x01d7 }
        r49 = r49.toString();	 Catch:{ RuntimeException -> 0x01d7 }
        r48.println(r49);	 Catch:{ RuntimeException -> 0x01d7 }
    L_0x02f3:
        r48 = java.lang.Long.parseLong(r47);	 Catch:{ NumberFormatException -> 0x0304 }
        r0 = r53;
        r1 = r31;
        r2 = r48;
        r4 = r47;
        r0.number(r1, r2, r4);	 Catch:{ NumberFormatException -> 0x0304 }
        goto L_0x0185;
    L_0x0304:
        r48 = move-exception;
        goto L_0x0286;
    L_0x0306:
        r0 = r32;
        r0 = r0.size;	 Catch:{ RuntimeException -> 0x01d7 }
        r48 = r0;
        if (r48 <= 0) goto L_0x0378;
    L_0x030e:
        r48 = r32.pop();	 Catch:{ RuntimeException -> 0x01d7 }
        r48 = (java.lang.String) r48;	 Catch:{ RuntimeException -> 0x01d7 }
        r31 = r48;
    L_0x0316:
        if (r25 == 0) goto L_0x0334;
    L_0x0318:
        r48 = java.lang.System.out;	 Catch:{ RuntimeException -> 0x01d7 }
        r49 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x01d7 }
        r49.<init>();	 Catch:{ RuntimeException -> 0x01d7 }
        r50 = "startObject: ";
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x01d7 }
        r0 = r49;
        r1 = r31;
        r49 = r0.append(r1);	 Catch:{ RuntimeException -> 0x01d7 }
        r49 = r49.toString();	 Catch:{ RuntimeException -> 0x01d7 }
        r48.println(r49);	 Catch:{ RuntimeException -> 0x01d7 }
    L_0x0334:
        r0 = r53;
        r1 = r31;
        r0.startObject(r1);	 Catch:{ RuntimeException -> 0x01d7 }
        r0 = r41;
        r0 = r0.length;	 Catch:{ RuntimeException -> 0x01d7 }
        r48 = r0;
        r0 = r46;
        r1 = r48;
        if (r0 != r1) goto L_0x036b;
    L_0x0346:
        r0 = r41;
        r0 = r0.length;	 Catch:{ RuntimeException -> 0x01d7 }
        r48 = r0;
        r48 = r48 * 2;
        r0 = r48;
        r0 = new int[r0];	 Catch:{ RuntimeException -> 0x01d7 }
        r34 = r0;
        r48 = 0;
        r49 = 0;
        r0 = r41;
        r0 = r0.length;	 Catch:{ RuntimeException -> 0x01d7 }
        r50 = r0;
        r0 = r41;
        r1 = r48;
        r2 = r34;
        r3 = r49;
        r4 = r50;
        java.lang.System.arraycopy(r0, r1, r2, r3, r4);	 Catch:{ RuntimeException -> 0x01d7 }
        r41 = r34;
    L_0x036b:
        r45 = r46 + 1;
        r41[r46] = r24;	 Catch:{ RuntimeException -> 0x080f }
        r24 = 5;
        r12 = 2;
        r46 = r45;
        r35 = r36;
        goto L_0x0037;
    L_0x0378:
        r31 = 0;
        goto L_0x0316;
    L_0x037b:
        if (r25 == 0) goto L_0x0384;
    L_0x037d:
        r48 = java.lang.System.out;	 Catch:{ RuntimeException -> 0x01d7 }
        r49 = "endObject";
        r48.println(r49);	 Catch:{ RuntimeException -> 0x01d7 }
    L_0x0384:
        r53.pop();	 Catch:{ RuntimeException -> 0x01d7 }
        r45 = r46 + -1;
        r24 = r41[r45];	 Catch:{ RuntimeException -> 0x080f }
        r12 = 2;
        r46 = r45;
        r35 = r36;
        goto L_0x0037;
    L_0x0392:
        r0 = r32;
        r0 = r0.size;	 Catch:{ RuntimeException -> 0x01d7 }
        r48 = r0;
        if (r48 <= 0) goto L_0x0404;
    L_0x039a:
        r48 = r32.pop();	 Catch:{ RuntimeException -> 0x01d7 }
        r48 = (java.lang.String) r48;	 Catch:{ RuntimeException -> 0x01d7 }
        r31 = r48;
    L_0x03a2:
        if (r25 == 0) goto L_0x03c0;
    L_0x03a4:
        r48 = java.lang.System.out;	 Catch:{ RuntimeException -> 0x01d7 }
        r49 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x01d7 }
        r49.<init>();	 Catch:{ RuntimeException -> 0x01d7 }
        r50 = "startArray: ";
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x01d7 }
        r0 = r49;
        r1 = r31;
        r49 = r0.append(r1);	 Catch:{ RuntimeException -> 0x01d7 }
        r49 = r49.toString();	 Catch:{ RuntimeException -> 0x01d7 }
        r48.println(r49);	 Catch:{ RuntimeException -> 0x01d7 }
    L_0x03c0:
        r0 = r53;
        r1 = r31;
        r0.startArray(r1);	 Catch:{ RuntimeException -> 0x01d7 }
        r0 = r41;
        r0 = r0.length;	 Catch:{ RuntimeException -> 0x01d7 }
        r48 = r0;
        r0 = r46;
        r1 = r48;
        if (r0 != r1) goto L_0x03f7;
    L_0x03d2:
        r0 = r41;
        r0 = r0.length;	 Catch:{ RuntimeException -> 0x01d7 }
        r48 = r0;
        r48 = r48 * 2;
        r0 = r48;
        r0 = new int[r0];	 Catch:{ RuntimeException -> 0x01d7 }
        r34 = r0;
        r48 = 0;
        r49 = 0;
        r0 = r41;
        r0 = r0.length;	 Catch:{ RuntimeException -> 0x01d7 }
        r50 = r0;
        r0 = r41;
        r1 = r48;
        r2 = r34;
        r3 = r49;
        r4 = r50;
        java.lang.System.arraycopy(r0, r1, r2, r3, r4);	 Catch:{ RuntimeException -> 0x01d7 }
        r41 = r34;
    L_0x03f7:
        r45 = r46 + 1;
        r41[r46] = r24;	 Catch:{ RuntimeException -> 0x080f }
        r24 = 23;
        r12 = 2;
        r46 = r45;
        r35 = r36;
        goto L_0x0037;
    L_0x0404:
        r31 = 0;
        goto L_0x03a2;
    L_0x0407:
        if (r25 == 0) goto L_0x0410;
    L_0x0409:
        r48 = java.lang.System.out;	 Catch:{ RuntimeException -> 0x01d7 }
        r49 = "endArray";
        r48.println(r49);	 Catch:{ RuntimeException -> 0x01d7 }
    L_0x0410:
        r53.pop();	 Catch:{ RuntimeException -> 0x01d7 }
        r45 = r46 + -1;
        r24 = r41[r45];	 Catch:{ RuntimeException -> 0x080f }
        r12 = 2;
        r46 = r45;
        r35 = r36;
        goto L_0x0037;
    L_0x041e:
        r42 = r36 + -1;
        r35 = r36 + 1;
        r48 = r54[r36];	 Catch:{ RuntimeException -> 0x046c }
        r49 = 47;
        r0 = r48;
        r1 = r49;
        if (r0 != r1) goto L_0x0471;
    L_0x042c:
        r0 = r35;
        r1 = r27;
        if (r0 == r1) goto L_0x043f;
    L_0x0432:
        r48 = r54[r35];	 Catch:{ RuntimeException -> 0x046c }
        r49 = 10;
        r0 = r48;
        r1 = r49;
        if (r0 == r1) goto L_0x043f;
    L_0x043c:
        r35 = r35 + 1;
        goto L_0x042c;
    L_0x043f:
        r35 = r35 + -1;
    L_0x0441:
        if (r25 == 0) goto L_0x00de;
    L_0x0443:
        r48 = java.lang.System.out;	 Catch:{ RuntimeException -> 0x046c }
        r49 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x046c }
        r49.<init>();	 Catch:{ RuntimeException -> 0x046c }
        r50 = "comment ";
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x046c }
        r50 = new java.lang.String;	 Catch:{ RuntimeException -> 0x046c }
        r51 = r35 - r42;
        r0 = r50;
        r1 = r54;
        r2 = r42;
        r3 = r51;
        r0.<init>(r1, r2, r3);	 Catch:{ RuntimeException -> 0x046c }
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x046c }
        r49 = r49.toString();	 Catch:{ RuntimeException -> 0x046c }
        r48.println(r49);	 Catch:{ RuntimeException -> 0x046c }
        goto L_0x00de;
    L_0x046c:
        r28 = move-exception;
        r45 = r46;
        goto L_0x01dc;
    L_0x0471:
        r48 = r35 + 1;
        r0 = r48;
        r1 = r27;
        if (r0 >= r1) goto L_0x0483;
    L_0x0479:
        r48 = r54[r35];	 Catch:{ RuntimeException -> 0x046c }
        r49 = 42;
        r0 = r48;
        r1 = r49;
        if (r0 != r1) goto L_0x048f;
    L_0x0483:
        r48 = r35 + 1;
        r48 = r54[r48];	 Catch:{ RuntimeException -> 0x046c }
        r49 = 47;
        r0 = r48;
        r1 = r49;
        if (r0 == r1) goto L_0x0492;
    L_0x048f:
        r35 = r35 + 1;
        goto L_0x0471;
    L_0x0492:
        r35 = r35 + 1;
        goto L_0x0441;
    L_0x0495:
        if (r25 == 0) goto L_0x049e;
    L_0x0497:
        r48 = java.lang.System.out;	 Catch:{ RuntimeException -> 0x01d7 }
        r49 = "unquotedChars";
        r48.println(r49);	 Catch:{ RuntimeException -> 0x01d7 }
    L_0x049e:
        r40 = r36;
        r33 = 0;
        r44 = 1;
        if (r43 == 0) goto L_0x0506;
    L_0x04a6:
        r35 = r36;
    L_0x04a8:
        r48 = r54[r35];	 Catch:{ RuntimeException -> 0x046c }
        switch(r48) {
            case 10: goto L_0x04d7;
            case 13: goto L_0x04d7;
            case 47: goto L_0x04e9;
            case 58: goto L_0x04d7;
            case 92: goto L_0x04e6;
            default: goto L_0x04ad;
        };	 Catch:{ RuntimeException -> 0x046c }
    L_0x04ad:
        if (r25 == 0) goto L_0x04cf;
    L_0x04af:
        r48 = java.lang.System.out;	 Catch:{ RuntimeException -> 0x046c }
        r49 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x046c }
        r49.<init>();	 Catch:{ RuntimeException -> 0x046c }
        r50 = "unquotedChar (name): '";
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x046c }
        r50 = r54[r35];	 Catch:{ RuntimeException -> 0x046c }
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x046c }
        r50 = "'";
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x046c }
        r49 = r49.toString();	 Catch:{ RuntimeException -> 0x046c }
        r48.println(r49);	 Catch:{ RuntimeException -> 0x046c }
    L_0x04cf:
        r35 = r35 + 1;
        r0 = r35;
        r1 = r27;
        if (r0 != r1) goto L_0x04a8;
    L_0x04d7:
        r35 = r35 + -1;
    L_0x04d9:
        r48 = r54[r35];	 Catch:{ RuntimeException -> 0x046c }
        r49 = 32;
        r0 = r48;
        r1 = r49;
        if (r0 != r1) goto L_0x00de;
    L_0x04e3:
        r35 = r35 + -1;
        goto L_0x04d9;
    L_0x04e6:
        r33 = 1;
        goto L_0x04ad;
    L_0x04e9:
        r48 = r35 + 1;
        r0 = r48;
        r1 = r27;
        if (r0 == r1) goto L_0x04ad;
    L_0x04f1:
        r48 = r35 + 1;
        r21 = r54[r48];	 Catch:{ RuntimeException -> 0x046c }
        r48 = 47;
        r0 = r21;
        r1 = r48;
        if (r0 == r1) goto L_0x04d7;
    L_0x04fd:
        r48 = 42;
        r0 = r21;
        r1 = r48;
        if (r0 != r1) goto L_0x04ad;
    L_0x0505:
        goto L_0x04d7;
    L_0x0506:
        r35 = r36;
    L_0x0508:
        r48 = r54[r35];	 Catch:{ RuntimeException -> 0x046c }
        switch(r48) {
            case 10: goto L_0x04d7;
            case 13: goto L_0x04d7;
            case 44: goto L_0x04d7;
            case 47: goto L_0x053b;
            case 92: goto L_0x0538;
            case 93: goto L_0x04d7;
            case 125: goto L_0x04d7;
            default: goto L_0x050d;
        };	 Catch:{ RuntimeException -> 0x046c }
    L_0x050d:
        if (r25 == 0) goto L_0x052f;
    L_0x050f:
        r48 = java.lang.System.out;	 Catch:{ RuntimeException -> 0x046c }
        r49 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x046c }
        r49.<init>();	 Catch:{ RuntimeException -> 0x046c }
        r50 = "unquotedChar (value): '";
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x046c }
        r50 = r54[r35];	 Catch:{ RuntimeException -> 0x046c }
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x046c }
        r50 = "'";
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x046c }
        r49 = r49.toString();	 Catch:{ RuntimeException -> 0x046c }
        r48.println(r49);	 Catch:{ RuntimeException -> 0x046c }
    L_0x052f:
        r35 = r35 + 1;
        r0 = r35;
        r1 = r27;
        if (r0 != r1) goto L_0x0508;
    L_0x0537:
        goto L_0x04d7;
    L_0x0538:
        r33 = 1;
        goto L_0x050d;
    L_0x053b:
        r48 = r35 + 1;
        r0 = r48;
        r1 = r27;
        if (r0 == r1) goto L_0x050d;
    L_0x0543:
        r48 = r35 + 1;
        r21 = r54[r48];	 Catch:{ RuntimeException -> 0x046c }
        r48 = 47;
        r0 = r21;
        r1 = r48;
        if (r0 == r1) goto L_0x04d7;
    L_0x054f:
        r48 = 42;
        r0 = r21;
        r1 = r48;
        if (r0 != r1) goto L_0x050d;
    L_0x0557:
        goto L_0x04d7;
    L_0x0558:
        if (r25 == 0) goto L_0x0561;
    L_0x055a:
        r48 = java.lang.System.out;	 Catch:{ RuntimeException -> 0x01d7 }
        r49 = "quotedChars";
        r48.println(r49);	 Catch:{ RuntimeException -> 0x01d7 }
    L_0x0561:
        r35 = r36 + 1;
        r40 = r35;
        r33 = 0;
    L_0x0567:
        r48 = r54[r35];	 Catch:{ RuntimeException -> 0x046c }
        switch(r48) {
            case 34: goto L_0x0574;
            case 92: goto L_0x0578;
            default: goto L_0x056c;
        };	 Catch:{ RuntimeException -> 0x046c }
    L_0x056c:
        r35 = r35 + 1;
        r0 = r35;
        r1 = r27;
        if (r0 != r1) goto L_0x0567;
    L_0x0574:
        r35 = r35 + -1;
        goto L_0x00de;
    L_0x0578:
        r33 = 1;
        r35 = r35 + 1;
        goto L_0x056c;
    L_0x057d:
        r35 = r36;
    L_0x057f:
        if (r24 != 0) goto L_0x0584;
    L_0x0581:
        r12 = 5;
        goto L_0x0037;
    L_0x0584:
        r35 = r35 + 1;
        r0 = r35;
        r1 = r38;
        if (r0 == r1) goto L_0x058f;
    L_0x058c:
        r12 = 1;
        goto L_0x0037;
    L_0x058f:
        r0 = r35;
        r1 = r27;
        if (r0 != r1) goto L_0x003a;
    L_0x0595:
        r48 = _json_eof_actions;	 Catch:{ RuntimeException -> 0x046c }
        r6 = r48[r24];	 Catch:{ RuntimeException -> 0x046c }
        r48 = _json_actions;	 Catch:{ RuntimeException -> 0x046c }
        r7 = r6 + 1;
        r8 = r48[r6];	 Catch:{ RuntimeException -> 0x046c }
        r9 = r8;
    L_0x05a0:
        r8 = r9 + -1;
        if (r9 <= 0) goto L_0x003a;
    L_0x05a4:
        r48 = _json_actions;	 Catch:{ RuntimeException -> 0x046c }
        r6 = r7 + 1;
        r48 = r48[r7];	 Catch:{ RuntimeException -> 0x046c }
        switch(r48) {
            case 1: goto L_0x05b0;
            default: goto L_0x05ad;
        };	 Catch:{ RuntimeException -> 0x046c }
    L_0x05ad:
        r9 = r8;
        r7 = r6;
        goto L_0x05a0;
    L_0x05b0:
        r47 = new java.lang.String;	 Catch:{ RuntimeException -> 0x046c }
        r48 = r35 - r40;
        r0 = r47;
        r1 = r54;
        r2 = r40;
        r3 = r48;
        r0.<init>(r1, r2, r3);	 Catch:{ RuntimeException -> 0x046c }
        if (r33 == 0) goto L_0x05c9;
    L_0x05c1:
        r0 = r53;
        r1 = r47;
        r47 = r0.unescape(r1);	 Catch:{ RuntimeException -> 0x046c }
    L_0x05c9:
        if (r43 == 0) goto L_0x05f7;
    L_0x05cb:
        r43 = 0;
        if (r25 == 0) goto L_0x05eb;
    L_0x05cf:
        r48 = java.lang.System.out;	 Catch:{ RuntimeException -> 0x046c }
        r49 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x046c }
        r49.<init>();	 Catch:{ RuntimeException -> 0x046c }
        r50 = "name: ";
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x046c }
        r0 = r49;
        r1 = r47;
        r49 = r0.append(r1);	 Catch:{ RuntimeException -> 0x046c }
        r49 = r49.toString();	 Catch:{ RuntimeException -> 0x046c }
        r48.println(r49);	 Catch:{ RuntimeException -> 0x046c }
    L_0x05eb:
        r0 = r32;
        r1 = r47;
        r0.add(r1);	 Catch:{ RuntimeException -> 0x046c }
    L_0x05f2:
        r44 = 0;
        r40 = r35;
        goto L_0x05ad;
    L_0x05f7:
        r0 = r32;
        r0 = r0.size;	 Catch:{ RuntimeException -> 0x046c }
        r48 = r0;
        if (r48 <= 0) goto L_0x0641;
    L_0x05ff:
        r48 = r32.pop();	 Catch:{ RuntimeException -> 0x046c }
        r48 = (java.lang.String) r48;	 Catch:{ RuntimeException -> 0x046c }
        r31 = r48;
    L_0x0607:
        if (r44 == 0) goto L_0x06e7;
    L_0x0609:
        r48 = "true";
        r48 = r47.equals(r48);	 Catch:{ RuntimeException -> 0x046c }
        if (r48 == 0) goto L_0x0644;
    L_0x0611:
        if (r25 == 0) goto L_0x0635;
    L_0x0613:
        r48 = java.lang.System.out;	 Catch:{ RuntimeException -> 0x046c }
        r49 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x046c }
        r49.<init>();	 Catch:{ RuntimeException -> 0x046c }
        r50 = "boolean: ";
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x046c }
        r0 = r49;
        r1 = r31;
        r49 = r0.append(r1);	 Catch:{ RuntimeException -> 0x046c }
        r50 = "=true";
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x046c }
        r49 = r49.toString();	 Catch:{ RuntimeException -> 0x046c }
        r48.println(r49);	 Catch:{ RuntimeException -> 0x046c }
    L_0x0635:
        r48 = 1;
        r0 = r53;
        r1 = r31;
        r2 = r48;
        r0.bool(r1, r2);	 Catch:{ RuntimeException -> 0x046c }
        goto L_0x05f2;
    L_0x0641:
        r31 = 0;
        goto L_0x0607;
    L_0x0644:
        r48 = "false";
        r48 = r47.equals(r48);	 Catch:{ RuntimeException -> 0x046c }
        if (r48 == 0) goto L_0x067d;
    L_0x064c:
        if (r25 == 0) goto L_0x0670;
    L_0x064e:
        r48 = java.lang.System.out;	 Catch:{ RuntimeException -> 0x046c }
        r49 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x046c }
        r49.<init>();	 Catch:{ RuntimeException -> 0x046c }
        r50 = "boolean: ";
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x046c }
        r0 = r49;
        r1 = r31;
        r49 = r0.append(r1);	 Catch:{ RuntimeException -> 0x046c }
        r50 = "=false";
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x046c }
        r49 = r49.toString();	 Catch:{ RuntimeException -> 0x046c }
        r48.println(r49);	 Catch:{ RuntimeException -> 0x046c }
    L_0x0670:
        r48 = 0;
        r0 = r53;
        r1 = r31;
        r2 = r48;
        r0.bool(r1, r2);	 Catch:{ RuntimeException -> 0x046c }
        goto L_0x05f2;
    L_0x067d:
        r48 = "null";
        r48 = r47.equals(r48);	 Catch:{ RuntimeException -> 0x046c }
        if (r48 == 0) goto L_0x0692;
    L_0x0685:
        r48 = 0;
        r0 = r53;
        r1 = r31;
        r2 = r48;
        r0.string(r1, r2);	 Catch:{ RuntimeException -> 0x046c }
        goto L_0x05f2;
    L_0x0692:
        r22 = 0;
        r23 = 1;
        r29 = r40;
    L_0x0698:
        r0 = r29;
        r1 = r35;
        if (r0 >= r1) goto L_0x06a7;
    L_0x069e:
        r48 = r54[r29];	 Catch:{ RuntimeException -> 0x046c }
        switch(r48) {
            case 43: goto L_0x0722;
            case 45: goto L_0x0722;
            case 46: goto L_0x071e;
            case 48: goto L_0x0722;
            case 49: goto L_0x0722;
            case 50: goto L_0x0722;
            case 51: goto L_0x0722;
            case 52: goto L_0x0722;
            case 53: goto L_0x0722;
            case 54: goto L_0x0722;
            case 55: goto L_0x0722;
            case 56: goto L_0x0722;
            case 57: goto L_0x0722;
            case 69: goto L_0x071e;
            case 101: goto L_0x071e;
            default: goto L_0x06a3;
        };
    L_0x06a3:
        r22 = 0;
        r23 = 0;
    L_0x06a7:
        if (r22 == 0) goto L_0x0726;
    L_0x06a9:
        if (r25 == 0) goto L_0x06d5;
    L_0x06ab:
        r48 = java.lang.System.out;	 Catch:{ NumberFormatException -> 0x06e6 }
        r49 = new java.lang.StringBuilder;	 Catch:{ NumberFormatException -> 0x06e6 }
        r49.<init>();	 Catch:{ NumberFormatException -> 0x06e6 }
        r50 = "double: ";
        r49 = r49.append(r50);	 Catch:{ NumberFormatException -> 0x06e6 }
        r0 = r49;
        r1 = r31;
        r49 = r0.append(r1);	 Catch:{ NumberFormatException -> 0x06e6 }
        r50 = "=";
        r49 = r49.append(r50);	 Catch:{ NumberFormatException -> 0x06e6 }
        r50 = java.lang.Double.parseDouble(r47);	 Catch:{ NumberFormatException -> 0x06e6 }
        r49 = r49.append(r50);	 Catch:{ NumberFormatException -> 0x06e6 }
        r49 = r49.toString();	 Catch:{ NumberFormatException -> 0x06e6 }
        r48.println(r49);	 Catch:{ NumberFormatException -> 0x06e6 }
    L_0x06d5:
        r48 = java.lang.Double.parseDouble(r47);	 Catch:{ NumberFormatException -> 0x06e6 }
        r0 = r53;
        r1 = r31;
        r2 = r48;
        r4 = r47;
        r0.number(r1, r2, r4);	 Catch:{ NumberFormatException -> 0x06e6 }
        goto L_0x05f2;
    L_0x06e6:
        r48 = move-exception;
    L_0x06e7:
        if (r25 == 0) goto L_0x0713;
    L_0x06e9:
        r48 = java.lang.System.out;	 Catch:{ RuntimeException -> 0x046c }
        r49 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x046c }
        r49.<init>();	 Catch:{ RuntimeException -> 0x046c }
        r50 = "string: ";
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x046c }
        r0 = r49;
        r1 = r31;
        r49 = r0.append(r1);	 Catch:{ RuntimeException -> 0x046c }
        r50 = "=";
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x046c }
        r0 = r49;
        r1 = r47;
        r49 = r0.append(r1);	 Catch:{ RuntimeException -> 0x046c }
        r49 = r49.toString();	 Catch:{ RuntimeException -> 0x046c }
        r48.println(r49);	 Catch:{ RuntimeException -> 0x046c }
    L_0x0713:
        r0 = r53;
        r1 = r31;
        r2 = r47;
        r0.string(r1, r2);	 Catch:{ RuntimeException -> 0x046c }
        goto L_0x05f2;
    L_0x071e:
        r22 = 1;
        r23 = 0;
    L_0x0722:
        r29 = r29 + 1;
        goto L_0x0698;
    L_0x0726:
        if (r23 == 0) goto L_0x06e7;
    L_0x0728:
        if (r25 == 0) goto L_0x0754;
    L_0x072a:
        r48 = java.lang.System.out;	 Catch:{ RuntimeException -> 0x046c }
        r49 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x046c }
        r49.<init>();	 Catch:{ RuntimeException -> 0x046c }
        r50 = "double: ";
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x046c }
        r0 = r49;
        r1 = r31;
        r49 = r0.append(r1);	 Catch:{ RuntimeException -> 0x046c }
        r50 = "=";
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x046c }
        r50 = java.lang.Double.parseDouble(r47);	 Catch:{ RuntimeException -> 0x046c }
        r49 = r49.append(r50);	 Catch:{ RuntimeException -> 0x046c }
        r49 = r49.toString();	 Catch:{ RuntimeException -> 0x046c }
        r48.println(r49);	 Catch:{ RuntimeException -> 0x046c }
    L_0x0754:
        r48 = java.lang.Long.parseLong(r47);	 Catch:{ NumberFormatException -> 0x0765 }
        r0 = r53;
        r1 = r31;
        r2 = r48;
        r4 = r47;
        r0.number(r1, r2, r4);	 Catch:{ NumberFormatException -> 0x0765 }
        goto L_0x05f2;
    L_0x0765:
        r48 = move-exception;
        goto L_0x06e7;
    L_0x0767:
        r48 = new com.badlogic.gdx.utils.SerializationException;
        r49 = new java.lang.StringBuilder;
        r49.<init>();
        r50 = "Error parsing JSON on line ";
        r49 = r49.append(r50);
        r0 = r49;
        r1 = r30;
        r49 = r0.append(r1);
        r50 = " near: ";
        r49 = r49.append(r50);
        r50 = new java.lang.String;
        r51 = 256; // 0x100 float:3.59E-43 double:1.265E-321;
        r52 = r38 - r35;
        r51 = java.lang.Math.min(r51, r52);
        r0 = r50;
        r1 = r54;
        r2 = r35;
        r3 = r51;
        r0.<init>(r1, r2, r3);
        r49 = r49.append(r50);
        r49 = r49.toString();
        r0 = r48;
        r1 = r49;
        r2 = r37;
        r0.<init>(r1, r2);
        throw r48;
    L_0x07a9:
        r0 = r53;
        r0 = r0.elements;
        r48 = r0;
        r0 = r48;
        r0 = r0.size;
        r48 = r0;
        if (r48 == 0) goto L_0x07e4;
    L_0x07b7:
        r0 = r53;
        r0 = r0.elements;
        r48 = r0;
        r26 = r48.peek();
        r26 = (com.badlogic.gdx.utils.JsonValue) r26;
        r0 = r53;
        r0 = r0.elements;
        r48 = r0;
        r48.clear();
        if (r26 == 0) goto L_0x07dc;
    L_0x07ce:
        r48 = r26.isObject();
        if (r48 == 0) goto L_0x07dc;
    L_0x07d4:
        r48 = new com.badlogic.gdx.utils.SerializationException;
        r49 = "Error parsing JSON, unmatched brace.";
        r48.<init>(r49);
        throw r48;
    L_0x07dc:
        r48 = new com.badlogic.gdx.utils.SerializationException;
        r49 = "Error parsing JSON, unmatched bracket.";
        r48.<init>(r49);
        throw r48;
    L_0x07e4:
        if (r37 == 0) goto L_0x080e;
    L_0x07e6:
        r48 = new com.badlogic.gdx.utils.SerializationException;
        r49 = new java.lang.StringBuilder;
        r49.<init>();
        r50 = "Error parsing JSON: ";
        r49 = r49.append(r50);
        r50 = new java.lang.String;
        r0 = r50;
        r1 = r54;
        r0.<init>(r1);
        r49 = r49.append(r50);
        r49 = r49.toString();
        r0 = r48;
        r1 = r49;
        r2 = r37;
        r0.<init>(r1, r2);
        throw r48;
    L_0x080e:
        return r39;
    L_0x080f:
        r28 = move-exception;
        r35 = r36;
        goto L_0x01dc;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.badlogic.gdx.utils.JsonReader.parse(char[], int, int):com.badlogic.gdx.utils.JsonValue");
    }

    private static byte[] init__json_actions_0() {
        return new byte[]{(byte) 0, (byte) 1, (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 3, (byte) 1, (byte) 4, (byte) 1, (byte) 5, (byte) 1, (byte) 6, (byte) 1, (byte) 7, (byte) 1, (byte) 8, (byte) 2, (byte) 0, (byte) 7, (byte) 2, (byte) 0, (byte) 8, (byte) 2, (byte) 1, (byte) 3, (byte) 2, (byte) 1, (byte) 5};
    }

    private static short[] init__json_key_offsets_0() {
        return new short[]{(short) 0, (short) 0, (short) 11, (short) 13, (short) 14, (short) 16, (short) 25, (short) 31, (short) 37, (short) 39, (short) 50, (short) 57, (short) 64, (short) 73, (short) 74, (short) 83, (short) 85, (short) 87, (short) 96, (short) 98, (short) 100, (short) 101, (short) 103, (short) 105, (short) 116, (short) 123, (short) 130, (short) 141, (short) 142, (short) 153, (short) 155, (short) 157, (short) 168, (short) 170, (short) 172, (short) 174, (short) 179, (short) 184, (short) 184};
    }

    private static char[] init__json_trans_keys_0() {
        return new char[]{'\r', ' ', '\"', ',', '/', ':', '[', ']', '{', '\t', '\n', '*', '/', '\"', '*', '/', '\r', ' ', '\"', ',', '/', ':', '}', '\t', '\n', '\r', ' ', '/', ':', '\t', '\n', '\r', ' ', '/', ':', '\t', '\n', '*', '/', '\r', ' ', '\"', ',', '/', ':', '[', ']', '{', '\t', '\n', '\t', '\n', '\r', ' ', ',', '/', '}', '\t', '\n', '\r', ' ', ',', '/', '}', '\r', ' ', '\"', ',', '/', ':', '}', '\t', '\n', '\"', '\r', ' ', '\"', ',', '/', ':', '}', '\t', '\n', '*', '/', '*', '/', '\r', ' ', '\"', ',', '/', ':', '}', '\t', '\n', '*', '/', '*', '/', '\"', '*', '/', '*', '/', '\r', ' ', '\"', ',', '/', ':', '[', ']', '{', '\t', '\n', '\t', '\n', '\r', ' ', ',', '/', ']', '\t', '\n', '\r', ' ', ',', '/', ']', '\r', ' ', '\"', ',', '/', ':', '[', ']', '{', '\t', '\n', '\"', '\r', ' ', '\"', ',', '/', ':', '[', ']', '{', '\t', '\n', '*', '/', '*', '/', '\r', ' ', '\"', ',', '/', ':', '[', ']', '{', '\t', '\n', '*', '/', '*', '/', '*', '/', '\r', ' ', '/', '\t', '\n', '\r', ' ', '/', '\t', '\n', '\u0000'};
    }

    private static byte[] init__json_single_lengths_0() {
        return new byte[]{(byte) 0, (byte) 9, (byte) 2, (byte) 1, (byte) 2, (byte) 7, (byte) 4, (byte) 4, (byte) 2, (byte) 9, (byte) 7, (byte) 7, (byte) 7, (byte) 1, (byte) 7, (byte) 2, (byte) 2, (byte) 7, (byte) 2, (byte) 2, (byte) 1, (byte) 2, (byte) 2, (byte) 9, (byte) 7, (byte) 7, (byte) 9, (byte) 1, (byte) 9, (byte) 2, (byte) 2, (byte) 9, (byte) 2, (byte) 2, (byte) 2, (byte) 3, (byte) 3, (byte) 0, (byte) 0};
    }

    private static byte[] init__json_range_lengths_0() {
        return new byte[]{(byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 1, (byte) 1, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 1, (byte) 0, (byte) 0};
    }

    private static short[] init__json_index_offsets_0() {
        return new short[]{(short) 0, (short) 0, (short) 11, (short) 14, (short) 16, (short) 19, (short) 28, (short) 34, (short) 40, (short) 43, (short) 54, (short) 62, (short) 70, (short) 79, (short) 81, (short) 90, (short) 93, (short) 96, (short) 105, (short) 108, (short) 111, (short) 113, (short) 116, (short) 119, (short) 130, (short) 138, (short) 146, (short) 157, (short) 159, (short) 170, (short) 173, (short) 176, (short) 187, (short) 190, (short) 193, (short) 196, (short) 201, (short) 206, (short) 207};
    }

    private static byte[] init__json_indicies_0() {
        return new byte[]{(byte) 1, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 3, (byte) 5, (byte) 3, (byte) 6, (byte) 1, (byte) 0, (byte) 7, (byte) 7, (byte) 3, (byte) 8, (byte) 3, (byte) 9, (byte) 9, (byte) 3, (byte) 11, (byte) 11, (byte) 12, (byte) 13, (byte) 14, (byte) 3, (byte) 15, (byte) 11, (byte) 10, (byte) 16, (byte) 16, (byte) 17, (byte) 18, (byte) 16, (byte) 3, (byte) 19, (byte) 19, (byte) 20, (byte) 21, (byte) 19, (byte) 3, (byte) 22, (byte) 22, (byte) 3, (byte) 21, (byte) 21, (byte) 24, (byte) 3, (byte) 25, (byte) 3, (byte) 26, (byte) 3, (byte) 27, (byte) 21, (byte) 23, (byte) 28, (byte) 29, (byte) 28, (byte) 28, (byte) 30, (byte) 31, (byte) 32, (byte) 3, (byte) 33, (byte) 34, (byte) 33, (byte) 33, (byte) 13, (byte) 35, (byte) 15, (byte) 3, (byte) 34, (byte) 34, (byte) 12, (byte) 36, (byte) 37, (byte) 3, (byte) 15, (byte) 34, (byte) 10, (byte) 16, (byte) 3, (byte) 36, (byte) 36, (byte) 12, (byte) 3, (byte) 38, (byte) 3, (byte) 3, (byte) 36, (byte) 10, (byte) 39, (byte) 39, (byte) 3, (byte) 40, (byte) 40, (byte) 3, (byte) 13, (byte) 13, (byte) 12, (byte) 3, (byte) 41, (byte) 3, (byte) 15, (byte) 13, (byte) 10, (byte) 42, (byte) 42, (byte) 3, (byte) 43, (byte) 43, (byte) 3, (byte) 28, (byte) 3, (byte) 44, (byte) 44, (byte) 3, (byte) 45, (byte) 45, (byte) 3, (byte) 47, (byte) 47, (byte) 48, (byte) 49, (byte) 50, (byte) 3, (byte) 51, (byte) 52, (byte) 53, (byte) 47, (byte) 46, (byte) 54, (byte) 55, (byte) 54, (byte) 54, (byte) 56, (byte) 57, (byte) 58, (byte) 3, (byte) 59, (byte) 60, (byte) 59, (byte) 59, (byte) 49, (byte) 61, (byte) 52, (byte) 3, (byte) 60, (byte) 60, (byte) 48, (byte) 62, (byte) 63, (byte) 3, (byte) 51, (byte) 52, (byte) 53, (byte) 60, (byte) 46, (byte) 54, (byte) 3, (byte) 62, (byte) 62, (byte) 48, (byte) 3, (byte) 64, (byte) 3, (byte) 51, (byte) 3, (byte) 53, (byte) 62, (byte) 46, (byte) 65, (byte) 65, (byte) 3, (byte) 66, (byte) 66, (byte) 3, (byte) 49, (byte) 49, (byte) 48, (byte) 3, (byte) 67, (byte) 3, (byte) 51, (byte) 52, (byte) 53, (byte) 49, (byte) 46, (byte) 68, (byte) 68, (byte) 3, (byte) 69, (byte) 69, (byte) 3, (byte) 70, (byte) 70, (byte) 3, (byte) 8, (byte) 8, (byte) 71, (byte) 8, (byte) 3, (byte) 72, (byte) 72, (byte) 73, (byte) 72, (byte) 3, (byte) 3, (byte) 3, (byte) 0};
    }

    private static byte[] init__json_trans_targs_0() {
        return new byte[]{(byte) 35, (byte) 1, (byte) 3, (byte) 0, (byte) 4, (byte) 36, (byte) 36, (byte) 36, (byte) 36, (byte) 1, (byte) 6, (byte) 5, (byte) 13, (byte) 17, (byte) 22, (byte) 37, (byte) 7, (byte) 8, (byte) 9, (byte) 7, (byte) 8, (byte) 9, (byte) 7, (byte) 10, (byte) 20, (byte) 21, (byte) 11, (byte) 11, (byte) 11, (byte) 12, (byte) 17, (byte) 19, (byte) 37, (byte) 11, (byte) 12, (byte) 19, (byte) 14, (byte) 16, (byte) 15, (byte) 14, (byte) 12, (byte) 18, (byte) 17, (byte) 11, (byte) 9, (byte) 5, (byte) 24, (byte) 23, (byte) 27, (byte) 31, (byte) 34, (byte) 25, (byte) 38, (byte) 25, (byte) 25, (byte) 26, (byte) 31, (byte) 33, (byte) 38, (byte) 25, (byte) 26, (byte) 33, (byte) 28, (byte) 30, (byte) 29, (byte) 28, (byte) 26, (byte) 32, (byte) 31, (byte) 25, (byte) 23, (byte) 2, (byte) 36, (byte) 2};
    }

    private static byte[] init__json_trans_actions_0() {
        return new byte[]{(byte) 13, (byte) 0, (byte) 15, (byte) 0, (byte) 0, (byte) 7, (byte) 3, (byte) 11, (byte) 1, (byte) 11, (byte) 17, (byte) 0, (byte) 20, (byte) 0, (byte) 0, (byte) 5, (byte) 1, (byte) 1, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 11, (byte) 13, (byte) 15, (byte) 0, (byte) 7, (byte) 3, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 23, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 11, (byte) 11, (byte) 0, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 13, (byte) 0, (byte) 15, (byte) 0, (byte) 0, (byte) 7, (byte) 9, (byte) 3, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 26, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 11, (byte) 11, (byte) 0, (byte) 11, (byte) 11, (byte) 11, (byte) 1, (byte) 0, (byte) 0};
    }

    private static byte[] init__json_eof_actions_0() {
        return new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 0};
    }

    private void addChild(String name, JsonValue child) {
        child.setName(name);
        if (this.current == null) {
            this.current = child;
            this.root = child;
        } else if (this.current.isArray() || this.current.isObject()) {
            if (this.current.size == 0) {
                this.current.child = child;
            } else {
                JsonValue last = (JsonValue) this.lastChild.pop();
                last.next = child;
                child.prev = last;
            }
            this.lastChild.add(child);
            JsonValue jsonValue = this.current;
            jsonValue.size += json_start;
        } else {
            this.root = this.current;
        }
    }

    protected void startObject(String name) {
        JsonValue value = new JsonValue(ValueType.object);
        if (this.current != null) {
            addChild(name, value);
        }
        this.elements.add(value);
        this.current = value;
    }

    protected void startArray(String name) {
        JsonValue value = new JsonValue(ValueType.array);
        if (this.current != null) {
            addChild(name, value);
        }
        this.elements.add(value);
        this.current = value;
    }

    protected void pop() {
        this.root = (JsonValue) this.elements.pop();
        if (this.current.size > 0) {
            this.lastChild.pop();
        }
        this.current = this.elements.size > 0 ? (JsonValue) this.elements.peek() : null;
    }

    protected void string(String name, String value) {
        addChild(name, new JsonValue(value));
    }

    protected void number(String name, double value, String stringValue) {
        addChild(name, new JsonValue(value, stringValue));
    }

    protected void number(String name, long value, String stringValue) {
        addChild(name, new JsonValue(value, stringValue));
    }

    protected void bool(String name, boolean value) {
        addChild(name, new JsonValue(value));
    }

    private String unescape(String value) {
        int i;
        int length = value.length();
        StringBuilder buffer = new StringBuilder(length + 16);
        int i2 = json_error;
        while (i2 < length) {
            i = i2 + json_start;
            char c = value.charAt(i2);
            if (c != '\\') {
                buffer.append(c);
                i2 = i;
            } else if (i == length) {
                return buffer.toString();
            } else {
                i2 = i + json_start;
                c = value.charAt(i);
                if (c == 'u') {
                    buffer.append(Character.toChars(Integer.parseInt(value.substring(i2, i2 + 4), 16)));
                    i2 += 4;
                } else {
                    switch (c) {
                        case Keys.F /*34*/:
                        case Keys.S /*47*/:
                        case Keys.PAGE_UP /*92*/:
                            break;
                        case Keys.BUTTON_C /*98*/:
                            c = '\b';
                            break;
                        case HttpStatus.SC_PROCESSING /*102*/:
                            c = '\f';
                            break;
                        case Keys.BUTTON_MODE /*110*/:
                            c = '\n';
                            break;
                        case 'r':
                            c = '\r';
                            break;
                        case 't':
                            c = '\t';
                            break;
                        default:
                            throw new SerializationException("Illegal escaped character: \\" + c);
                    }
                    buffer.append(c);
                }
            }
        }
        i = i2;
        return buffer.toString();
    }
}
