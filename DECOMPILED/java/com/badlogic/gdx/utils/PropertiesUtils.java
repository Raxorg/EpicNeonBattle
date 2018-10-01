package com.badlogic.gdx.utils;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.compression.lzma.Base;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.Iterator;

public final class PropertiesUtils {
    private static final int CONTINUE = 3;
    private static final int IGNORE = 5;
    private static final int KEY_DONE = 4;
    private static final String LINE_SEPARATOR = "\n";
    private static final int NONE = 0;
    private static final int SLASH = 1;
    private static final int UNICODE = 2;

    private PropertiesUtils() {
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void load(com.badlogic.gdx.utils.ObjectMap<java.lang.String, java.lang.String> r21, java.io.Reader r22) throws java.io.IOException {
        /*
        if (r21 != 0) goto L_0x000a;
    L_0x0002:
        r19 = new java.lang.NullPointerException;
        r20 = "ObjectMap cannot be null";
        r19.<init>(r20);
        throw r19;
    L_0x000a:
        if (r22 != 0) goto L_0x0014;
    L_0x000c:
        r19 = new java.lang.NullPointerException;
        r20 = "Reader cannot be null";
        r19.<init>(r20);
        throw r19;
    L_0x0014:
        r11 = 0;
        r17 = 0;
        r5 = 0;
        r19 = 40;
        r0 = r19;
        r4 = new char[r0];
        r14 = 0;
        r10 = -1;
        r7 = 1;
        r3 = new java.io.BufferedReader;
        r0 = r22;
        r3.<init>(r0);
    L_0x0028:
        r8 = r3.read();
        r19 = -1;
        r0 = r19;
        if (r8 != r0) goto L_0x0046;
    L_0x0032:
        r19 = 2;
        r0 = r19;
        if (r11 != r0) goto L_0x0183;
    L_0x0038:
        r19 = 4;
        r0 = r19;
        if (r5 > r0) goto L_0x0183;
    L_0x003e:
        r19 = new java.lang.IllegalArgumentException;
        r20 = "Invalid Unicode sequence: expected format \\uxxxx";
        r19.<init>(r20);
        throw r19;
    L_0x0046:
        r13 = (char) r8;
        r0 = r4.length;
        r19 = r0;
        r0 = r19;
        if (r14 != r0) goto L_0x0063;
    L_0x004e:
        r0 = r4.length;
        r19 = r0;
        r19 = r19 * 2;
        r0 = r19;
        r12 = new char[r0];
        r19 = 0;
        r20 = 0;
        r0 = r19;
        r1 = r20;
        java.lang.System.arraycopy(r4, r0, r12, r1, r14);
        r4 = r12;
    L_0x0063:
        r19 = 2;
        r0 = r19;
        if (r11 != r0) goto L_0x009f;
    L_0x0069:
        r19 = 16;
        r0 = r19;
        r6 = java.lang.Character.digit(r13, r0);
        if (r6 < 0) goto L_0x0091;
    L_0x0073:
        r19 = r17 << 4;
        r17 = r19 + r6;
        r5 = r5 + 1;
        r19 = 4;
        r0 = r19;
        if (r5 < r0) goto L_0x0028;
    L_0x007f:
        r11 = 0;
        r15 = r14 + 1;
        r0 = r17;
        r0 = (char) r0;
        r19 = r0;
        r4[r14] = r19;
        r19 = 10;
        r0 = r19;
        if (r13 == r0) goto L_0x00a0;
    L_0x008f:
        r14 = r15;
        goto L_0x0028;
    L_0x0091:
        r19 = 4;
        r0 = r19;
        if (r5 > r0) goto L_0x007f;
    L_0x0097:
        r19 = new java.lang.IllegalArgumentException;
        r20 = "Invalid Unicode sequence: illegal character";
        r19.<init>(r20);
        throw r19;
    L_0x009f:
        r15 = r14;
    L_0x00a0:
        r19 = 1;
        r0 = r19;
        if (r11 != r0) goto L_0x00d7;
    L_0x00a6:
        r11 = 0;
        switch(r13) {
            case 10: goto L_0x00bd;
            case 13: goto L_0x00b9;
            case 98: goto L_0x00c1;
            case 102: goto L_0x00c4;
            case 110: goto L_0x00c7;
            case 114: goto L_0x00ca;
            case 116: goto L_0x00cd;
            case 117: goto L_0x00d0;
            default: goto L_0x00aa;
        };
    L_0x00aa:
        r7 = 0;
        r19 = 4;
        r0 = r19;
        if (r11 != r0) goto L_0x00b3;
    L_0x00b1:
        r10 = r15;
        r11 = 0;
    L_0x00b3:
        r14 = r15 + 1;
        r4[r15] = r13;
        goto L_0x0028;
    L_0x00b9:
        r11 = 3;
        r14 = r15;
        goto L_0x0028;
    L_0x00bd:
        r11 = 5;
        r14 = r15;
        goto L_0x0028;
    L_0x00c1:
        r13 = 8;
        goto L_0x00aa;
    L_0x00c4:
        r13 = 12;
        goto L_0x00aa;
    L_0x00c7:
        r13 = 10;
        goto L_0x00aa;
    L_0x00ca:
        r13 = 13;
        goto L_0x00aa;
    L_0x00cd:
        r13 = 9;
        goto L_0x00aa;
    L_0x00d0:
        r11 = 2;
        r5 = 0;
        r17 = r5;
        r14 = r15;
        goto L_0x0028;
    L_0x00d7:
        switch(r13) {
            case 10: goto L_0x0113;
            case 13: goto L_0x011d;
            case 33: goto L_0x00f4;
            case 35: goto L_0x00f4;
            case 58: goto L_0x015f;
            case 61: goto L_0x015f;
            case 92: goto L_0x0154;
            default: goto L_0x00da;
        };
    L_0x00da:
        r19 = java.lang.Character.isSpace(r13);
        if (r19 == 0) goto L_0x0174;
    L_0x00e0:
        r19 = 3;
        r0 = r19;
        if (r11 != r0) goto L_0x00e7;
    L_0x00e6:
        r11 = 5;
    L_0x00e7:
        if (r15 == 0) goto L_0x01ce;
    L_0x00e9:
        if (r15 == r10) goto L_0x01ce;
    L_0x00eb:
        r19 = 5;
        r0 = r19;
        if (r11 != r0) goto L_0x016a;
    L_0x00f1:
        r14 = r15;
        goto L_0x0028;
    L_0x00f4:
        if (r7 == 0) goto L_0x00da;
    L_0x00f6:
        r8 = r3.read();
        r19 = -1;
        r0 = r19;
        if (r8 != r0) goto L_0x0103;
    L_0x0100:
        r14 = r15;
        goto L_0x0028;
    L_0x0103:
        r13 = (char) r8;
        r19 = 13;
        r0 = r19;
        if (r13 == r0) goto L_0x01ce;
    L_0x010a:
        r19 = 10;
        r0 = r19;
        if (r13 != r0) goto L_0x00f6;
    L_0x0110:
        r14 = r15;
        goto L_0x0028;
    L_0x0113:
        r19 = 3;
        r0 = r19;
        if (r11 != r0) goto L_0x011d;
    L_0x0119:
        r11 = 5;
        r14 = r15;
        goto L_0x0028;
    L_0x011d:
        r11 = 0;
        r7 = 1;
        if (r15 > 0) goto L_0x0125;
    L_0x0121:
        if (r15 != 0) goto L_0x0150;
    L_0x0123:
        if (r10 != 0) goto L_0x0150;
    L_0x0125:
        r19 = -1;
        r0 = r19;
        if (r10 != r0) goto L_0x012c;
    L_0x012b:
        r10 = r15;
    L_0x012c:
        r16 = new java.lang.String;
        r19 = 0;
        r0 = r16;
        r1 = r19;
        r0.<init>(r4, r1, r15);
        r19 = 0;
        r0 = r16;
        r1 = r19;
        r19 = r0.substring(r1, r10);
        r0 = r16;
        r20 = r0.substring(r10);
        r0 = r21;
        r1 = r19;
        r2 = r20;
        r0.put(r1, r2);
    L_0x0150:
        r10 = -1;
        r14 = 0;
        goto L_0x0028;
    L_0x0154:
        r19 = 4;
        r0 = r19;
        if (r11 != r0) goto L_0x015b;
    L_0x015a:
        r10 = r15;
    L_0x015b:
        r11 = 1;
        r14 = r15;
        goto L_0x0028;
    L_0x015f:
        r19 = -1;
        r0 = r19;
        if (r10 != r0) goto L_0x00da;
    L_0x0165:
        r11 = 0;
        r10 = r15;
        r14 = r15;
        goto L_0x0028;
    L_0x016a:
        r19 = -1;
        r0 = r19;
        if (r10 != r0) goto L_0x0174;
    L_0x0170:
        r11 = 4;
        r14 = r15;
        goto L_0x0028;
    L_0x0174:
        r19 = 5;
        r0 = r19;
        if (r11 == r0) goto L_0x0180;
    L_0x017a:
        r19 = 3;
        r0 = r19;
        if (r11 != r0) goto L_0x00aa;
    L_0x0180:
        r11 = 0;
        goto L_0x00aa;
    L_0x0183:
        r19 = -1;
        r0 = r19;
        if (r10 != r0) goto L_0x018c;
    L_0x0189:
        if (r14 <= 0) goto L_0x018c;
    L_0x018b:
        r10 = r14;
    L_0x018c:
        if (r10 < 0) goto L_0x01cd;
    L_0x018e:
        r16 = new java.lang.String;
        r19 = 0;
        r0 = r16;
        r1 = r19;
        r0.<init>(r4, r1, r14);
        r19 = 0;
        r0 = r16;
        r1 = r19;
        r9 = r0.substring(r1, r10);
        r0 = r16;
        r18 = r0.substring(r10);
        r19 = 1;
        r0 = r19;
        if (r11 != r0) goto L_0x01c6;
    L_0x01af:
        r19 = new java.lang.StringBuilder;
        r19.<init>();
        r0 = r19;
        r1 = r18;
        r19 = r0.append(r1);
        r20 = "\u0000";
        r19 = r19.append(r20);
        r18 = r19.toString();
    L_0x01c6:
        r0 = r21;
        r1 = r18;
        r0.put(r9, r1);
    L_0x01cd:
        return;
    L_0x01ce:
        r14 = r15;
        goto L_0x0028;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.badlogic.gdx.utils.PropertiesUtils.load(com.badlogic.gdx.utils.ObjectMap, java.io.Reader):void");
    }

    public static void store(ObjectMap<String, String> properties, Writer writer, String comment) throws IOException {
        storeImpl(properties, writer, comment, false);
    }

    private static void storeImpl(ObjectMap<String, String> properties, Writer writer, String comment, boolean escapeUnicode) throws IOException {
        if (comment != null) {
            writeComment(writer, comment);
        }
        writer.write("#");
        writer.write(new Date().toString());
        writer.write(LINE_SEPARATOR);
        StringBuilder sb = new StringBuilder((int) HttpStatus.SC_OK);
        Iterator it = properties.entries().iterator();
        while (it.hasNext()) {
            Entry<String, String> entry = (Entry) it.next();
            dumpString(sb, (String) entry.key, true, escapeUnicode);
            sb.append('=');
            dumpString(sb, (String) entry.value, false, escapeUnicode);
            writer.write(LINE_SEPARATOR);
            writer.write(sb.toString());
            sb.setLength(NONE);
        }
        writer.flush();
    }

    private static void dumpString(StringBuilder outBuffer, String string, boolean escapeSpace, boolean escapeUnicode) {
        int len = string.length();
        for (int i = NONE; i < len; i += SLASH) {
            char ch = string.charAt(i);
            if (ch <= '=' || ch >= '\u007f') {
                switch (ch) {
                    case Matrix4.M12 /*9*/:
                        outBuffer.append("\\t");
                        break;
                    case Base.kNumPosModels /*10*/:
                        outBuffer.append("\\n");
                        break;
                    case Base.kNumStates /*12*/:
                        outBuffer.append("\\f");
                        break;
                    case Matrix4.M13 /*13*/:
                        outBuffer.append("\\r");
                        break;
                    case DefaultTextureBinder.MAX_GLES_UNITS /*32*/:
                        if (i != 0 && !escapeSpace) {
                            break;
                        }
                        outBuffer.append("\\ ");
                        break;
                        break;
                    case Keys.E /*33*/:
                    case Keys.G /*35*/:
                    case Keys.ALT_RIGHT /*58*/:
                    case Keys.TAB /*61*/:
                        outBuffer.append('\\').append(ch);
                        break;
                    default:
                        int i2 = (ch < ' ' || ch > '~') ? SLASH : NONE;
                        if ((i2 & escapeUnicode) == 0) {
                            outBuffer.append(ch);
                            break;
                        }
                        String hex = Integer.toHexString(ch);
                        outBuffer.append("\\u");
                        for (int j = NONE; j < 4 - hex.length(); j += SLASH) {
                            outBuffer.append('0');
                        }
                        outBuffer.append(hex);
                        break;
                }
            }
            outBuffer.append(ch == '\\' ? "\\\\" : Character.valueOf(ch));
        }
    }

    private static void writeComment(Writer writer, String comment) throws IOException {
        writer.write("#");
        int len = comment.length();
        int curIndex = NONE;
        int lastIndex = NONE;
        while (curIndex < len) {
            char c = comment.charAt(curIndex);
            if (c > '\u00ff' || c == '\n' || c == '\r') {
                if (lastIndex != curIndex) {
                    writer.write(comment.substring(lastIndex, curIndex));
                }
                if (c > '\u00ff') {
                    String hex = Integer.toHexString(c);
                    writer.write("\\u");
                    for (int j = NONE; j < 4 - hex.length(); j += SLASH) {
                        writer.write(48);
                    }
                    writer.write(hex);
                } else {
                    writer.write(LINE_SEPARATOR);
                    if (c == '\r' && curIndex != len - 1 && comment.charAt(curIndex + SLASH) == '\n') {
                        curIndex += SLASH;
                    }
                    if (curIndex == len - 1 || !(comment.charAt(curIndex + SLASH) == '#' || comment.charAt(curIndex + SLASH) == '!')) {
                        writer.write("#");
                    }
                }
                lastIndex = curIndex + SLASH;
            }
            curIndex += SLASH;
        }
        if (lastIndex != curIndex) {
            writer.write(comment.substring(lastIndex, curIndex));
        }
        writer.write(LINE_SEPARATOR);
    }
}
