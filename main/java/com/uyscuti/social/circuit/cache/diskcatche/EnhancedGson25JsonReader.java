package com.uyscuti.social.circuit.cache.diskcatche;

//import static com.uyscuti.banji.libs.cache.diskcatche.FieldSpy.spyField;
//import static com.uyscuti.banji.libs.cache.diskcatche.MethodSpy.spyMethod;



import static com.uyscuti.social.circuit.cache.diskcatche.FieldSpy.spyField;
import static com.uyscuti.social.circuit.cache.diskcatche.MethodSpy.spyMethod;

import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.Reader;

public final class EnhancedGson25JsonReader extends JsonReader {


    public interface ISlicedStringListener {

        void accept(char[] buffer, int start, int length)
                throws IOException;

    }

    // The constants can be just copied

    /** */
    private static final int PEEKED_NONE = 0;

    /** */
    private static final int PEEKED_SINGLE_QUOTED = 8;

    /** */
    private static final int PEEKED_DOUBLE_QUOTED = 9;

    // Here is a bunch of spies made to "spy" for the parent's class state

    private final FieldSpy<Integer> peeked;
    private final MethodSpy<Integer> doPeek;
    private final FieldSpy<char[]> buffer;
    private final FieldSpy<Integer> pos;
    private final FieldSpy<Integer> limit;
    private final MethodSpy<Character> readEscapeCharacter;
    private final FieldSpy<Integer> lineNumber;
    private final FieldSpy<Integer> lineStart;
    private final MethodSpy<Boolean> fillBuffer;
    private final MethodSpy<IOException> syntaxError;
    private final FieldSpy<Integer> stackSize;
    private final FieldSpy<int[]> pathIndices;

    private EnhancedGson25JsonReader(final Reader reader)
            throws NoSuchFieldException, NoSuchMethodException {
        super(reader);
        peeked = spyField(JsonReader.class, this, "peeked");
        doPeek = spyMethod(JsonReader.class, this, "doPeek");

        buffer = spyField(JsonReader.class, this, "buffer");
        pos = spyField(JsonReader.class, this, "pos");
        limit = spyField(JsonReader.class, this, "limit");
        readEscapeCharacter = spyMethod(JsonReader.class, this, "readEscapeCharacter");
        lineNumber = spyField(JsonReader.class, this, "lineNumber");
        lineStart = spyField(JsonReader.class, this, "lineStart");
        fillBuffer = spyMethod(JsonReader.class, this, "fillBuffer", int.class);
        syntaxError = spyMethod(JsonReader.class, this, "syntaxError", String.class);
        stackSize = spyField(JsonReader.class, this, "stackSize");
        pathIndices = spyField(JsonReader.class, this, "pathIndices");
    }

    public static EnhancedGson25JsonReader getEnhancedGson25JsonReader(final Reader reader) {
        try {
            return new EnhancedGson25JsonReader(reader);
        } catch ( final NoSuchFieldException | NoSuchMethodException ex ) {
            throw new RuntimeException(ex);
        }
    }

    // This method has been copied and reworked from the nextString() implementation

    public void nextSlicedString(final ISlicedStringListener listener)
            throws IOException {
        int p = peeked.get();
        if ( p == PEEKED_NONE ) {
            p = doPeek.get();
        }
        switch ( p ) {
            case PEEKED_SINGLE_QUOTED:
                nextQuotedSlicedValue('\'', listener);
                break;
            case PEEKED_DOUBLE_QUOTED:
                nextQuotedSlicedValue('"', listener);
                break;
            default:
                throw new IllegalStateException("Expected a string but was " + peek()
                        + " at line "
                        + " column "
                        + " path " + getPath()
                );
        }
        peeked.accept(PEEKED_NONE);
        pathIndices.get()[stackSize.get() - 1]++;
    }


    private void nextQuotedSlicedValue(final char quote, final ISlicedStringListener listener)
            throws IOException {
        final char[] buffer = this.buffer.get();
        final char[] singleCharBuffer = new char[1];
        while ( true ) {
            int p = pos.get();
            int l = limit.get();
            int start = p;
            while ( p < l ) {
                final int c = buffer[p++];
                if ( c == quote ) {
                    pos.accept(p);
                    listener.accept(buffer, start, p - start - 1);
                    return;
                } else if ( c == '\\' ) {
                    pos.accept(p);
                    listener.accept(buffer, start, p - start - 1);
                    singleCharBuffer[0] = readEscapeCharacter.get();
                    listener.accept(singleCharBuffer, 0, 1);
                    p = pos.get();
                    l = limit.get();
                    start = p;
                } else if ( c == '\n' ) {
                    lineNumber.accept(lineNumber.get() + 1);
                    lineStart.accept(p);
                }
            }
            listener.accept(buffer, start, p - start);
            pos.accept(p);
            if ( !fillBuffer.apply(just1) ) {
                throw syntaxError.apply(justUnterminatedString);
            }
        }
    }

    // Save some memory

    private static final Object[] just1 = { 1 };
    private static final Object[] justUnterminatedString = { "Unterminated string" };

}