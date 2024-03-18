package com.mohistmc.yml.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

/**
 * "Writes" to a {@link StringBuilder}.
 */
public class BufferedSBWriter extends BufferedWriter {
    public StringBuilder builder;

    public BufferedSBWriter() {
        this(new StringBuilder());
    }

    public BufferedSBWriter(@NotNull StringBuilder builder) {
        super(new OutputStreamWriter(new ByteArrayOutputStream()));
        this.builder = builder;
    }

    @Override
    public void write(char @NotNull [] cbuf) {
        builder.append(cbuf);
    }

    @Override
    public void write(@NotNull String str) {
        builder.append(str);
    }

    @Override
    public void write(int c) {
        builder.append((char) c);
    }

    @Override
    public void write(char @NotNull [] cbuf, int off, int len) {
        builder.append(Arrays.copyOfRange(cbuf, off, len));
    }

    @Override
    public void write(@NotNull String s, int off, int len) {
        builder.append(Arrays.copyOfRange(s.toCharArray(), off, len));
    }
}
