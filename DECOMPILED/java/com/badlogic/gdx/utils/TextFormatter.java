package com.badlogic.gdx.utils;

import com.epicness.neonbattle.android.BuildConfig;
import java.text.MessageFormat;
import java.util.Locale;

class TextFormatter {
    private StringBuilder buffer = new StringBuilder();
    private MessageFormat messageFormat;

    public TextFormatter(Locale locale, boolean useMessageFormat) {
        if (useMessageFormat) {
            this.messageFormat = new MessageFormat(BuildConfig.VERSION_NAME, locale);
        }
    }

    public String format(String pattern, Object... args) {
        if (this.messageFormat == null) {
            return simpleFormat(pattern, args);
        }
        this.messageFormat.applyPattern(replaceEscapeChars(pattern));
        return this.messageFormat.format(args);
    }

    private String replaceEscapeChars(String pattern) {
        this.buffer.setLength(0);
        boolean changed = false;
        int len = pattern.length();
        int i = 0;
        while (i < len) {
            char ch = pattern.charAt(i);
            if (ch == '\'') {
                changed = true;
                this.buffer.append("''");
            } else if (ch == '{') {
                int j = i + 1;
                while (j < len && pattern.charAt(j) == '{') {
                    j++;
                }
                int escaped = (j - i) / 2;
                if (escaped > 0) {
                    changed = true;
                    this.buffer.append('\'');
                    do {
                        this.buffer.append('{');
                        escaped--;
                    } while (escaped > 0);
                    this.buffer.append('\'');
                }
                if ((j - i) % 2 != 0) {
                    this.buffer.append('{');
                }
                i = j - 1;
            } else {
                this.buffer.append(ch);
            }
            i++;
        }
        return changed ? this.buffer.toString() : pattern;
    }

    private String simpleFormat(String pattern, Object... args) {
        this.buffer.setLength(0);
        boolean changed = false;
        int placeholder = -1;
        int patternLength = pattern.length();
        int i = 0;
        while (i < patternLength) {
            char ch = pattern.charAt(i);
            if (placeholder < 0) {
                if (ch == '{') {
                    changed = true;
                    if (i + 1 >= patternLength || pattern.charAt(i + 1) != '{') {
                        placeholder = 0;
                    } else {
                        this.buffer.append(ch);
                        i++;
                    }
                } else {
                    this.buffer.append(ch);
                }
            } else if (ch == '}') {
                if (placeholder >= args.length) {
                    throw new IllegalArgumentException("Argument index out of bounds: " + placeholder);
                } else if (pattern.charAt(i - 1) == '{') {
                    throw new IllegalArgumentException("Missing argument index after a left curly brace");
                } else {
                    if (args[placeholder] == null) {
                        this.buffer.append("null");
                    } else {
                        this.buffer.append(args[placeholder].toString());
                    }
                    placeholder = -1;
                }
            } else if (ch < '0' || ch > '9') {
                throw new IllegalArgumentException("Unexpected '" + ch + "' while parsing argument index");
            } else {
                placeholder = (placeholder * 10) + (ch - 48);
            }
            i++;
        }
        if (placeholder < 0) {
            return changed ? this.buffer.toString() : pattern;
        } else {
            throw new IllegalArgumentException("Unmatched braces in the pattern.");
        }
    }
}
