// Created by Lawrence Dol.  Released into the public domain.
//
// Source is licensed for any use, provided this copyright notice is retained.
// No warranty for any purpose whatsoever is implied or expressed.  The author
// is not liable for any losses of any kind, direct or indirect, which result
// from the use of this software.

package org.marc4j.util;

import java.io.BufferedInputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * A pull-event parser for JSON data.
 * <p>
 * Note that the event and member values are only valid while the parser is not searching for
 * the next event.
 * <p>
 * Threading Design : [x] Single Threaded  [ ] Threadsafe  [ ] Immutable  [ ] Isolated
 *
 * @author          Lawrence Dol
 * @since           Build 2007.0726.0032
 */

public class JsonParser extends Object {

    // *****************************************************************************
    // INSTANCE PROPERTIES
    // *****************************************************************************

    // PARSER CONSTANTS
    private final boolean optEolIsComma;                          // parser option

    private final boolean optInternKeywords;                      // parser option

    private final boolean optInternValues;                        // parser option

    private final boolean optMultilineComments;                   // parser option

    private final boolean optMultilineStrings;                    // parser option

    private final boolean optSingleQuoteStrings;                  // parser option

    private final boolean optPreloadInput;                        // parser option

    private final boolean optUnquotedKeywords;                    // parser option

    private final ArrayList<ObjectData> objectStack;              // stack of object data for

    // nested objects

    private final StringBuilder accumulator;                      // text accumulator

    // PARSER VARIABLES
    private String inpName;                                       // the name of the input source (for location

    // reporting)

    private Reader inpReader;                                     // input inpReader

    private boolean inpClose;                                     // close input inpReader when it reaches end of

    // stream

    private int inpLine;                                          // input line number

    private int inpColumn;                                        // input column

    private ObjectData objectData;                                // current object data

    private int pushBack;                                         // push-back character (will be returned by next read)

    // EVENT VARIABLES
    private int evtCode;                                          // current event code

    private int evtLine;                                          // current event line number

    private int evtColumn;                                        // current event column number

    private String mbrName;                                       // current event member name

    private String mbrValue;                                      // current event member value

    // *****************************************************************************
    // INSTANCE CREATE/DELETE
    // *****************************************************************************

    /**
     * Construct a JSON parser from a character input source.
     * @param opt       Parsing option flags, combined from OPT_xxx values.
     */
    public JsonParser(final int opt) {
        super();

        optEolIsComma = (opt & OPT_EOL_IS_COMMA) != 0;
        optInternKeywords = (opt & OPT_INTERN_KEYWORDS) != 0;
        optInternValues = (opt & OPT_INTERN_VALUES) != 0;
        optMultilineComments = (opt & OPT_MULTILINE_COMMENTS) != 0;
        optMultilineStrings = (opt & OPT_MULTILINE_STRINGS) != 0;
        optSingleQuoteStrings = (opt & OPT_SINGLE_QUOTE_STRINGS) != 0;
        optPreloadInput = (opt & OPT_PRELOAD_INPUT) != 0;
        optUnquotedKeywords = (opt & OPT_UNQUOTED_KEYWORDS) != 0;

        objectStack = new ArrayList<ObjectData>();
        accumulator = new StringBuilder();

        reset(true);
    }

    /**
     * Reset this parser, and clear any internal caches or pools used to improve performance.  The parser may be reused if desired.
     * This should be called after completing the parsing of all input sources.
     */
    public void close() {
        reset(true);
    }

    /**
     * Reset this parser to be ready for more input after completing the parsing of some input.  This includes closing the input source if it was set with the flag which requests this behavior.
     * This should be called between input sources.
     */
    public void reset() {
        reset(false);
    }

    private void reset(final boolean all) {
        if (inpClose && inpReader != null) {
            try {
                inpReader.close();
            } catch (final Throwable ign) {
                ;
            }
        }

        objectStack.clear();
        accumulator.setLength(0);

        if (all) {
            accumulator.trimToSize();
        }

        inpName = null;
        inpReader = null;
        inpLine = 1;
        inpColumn = 0;
        objectData = new ObjectData("");
        pushBack = -1;

        evtCode = 0;
        evtLine = 0;
        evtColumn = 0;
        mbrName = "";
        mbrValue = "";
    }

    // *****************************************************************************
    // INSTANCE METHODS - ACCESSORS
    // *****************************************************************************

    /**
     * Get the name assigned to the input source at construction. This is informational only.
     * @return the name assigned to the input source at construction
     */
    public String getInputName() {
        return inpName;
    }

    /** 
     * Get the current location in the input source.
     * @return the current location in the input source.
     */
    public Location getInputLocation() {
        return createLocation(inpLine, inpColumn);
    }

    /** 
     * Get the name of the current object. 
     * @return the name of the current object.
     */
    public String getObjectName() {
        return objectData.name;
    }

    /**
     * Get the code for the current event.
     * @return the code for the current event.
     */
    public int getEventCode() {
        return evtCode;
    }

    /** 
     * Get the name for the current event.
     * @return the name for the current event.
     */
    public String getEventName() {
        return getEventName(evtCode);
    }

    /** 
     * Get the input source line number for the current event.
     * @return the input source line number for the current event.
     */
    public int getEventLine() {
        return evtLine;
    }

    /**
     * Get the input source column number for the current event.
     * @return he input source column number for the current event
     */
    public int getEventColumn() {
        return evtColumn;
    }

    /**
     * Get the input source location for the current event.
     * @return the input source location for the current event
     */
    public Location getEventLocation() {
        return createLocation(evtLine, evtColumn);
    }

    /**
     * Get the current object member name.
     * @return the current object member name
     */
    public String getMemberName() {
        return mbrName;
    }

    /**
     * Get the current object member value (valid only if the current event is EVT_OBJECT_MEMBER).
     * <p>
     * <b><u>NOTE</u></b>
     * <p>
     * All values are returned with EMCA-262 unescaping already applied. However if the value was specified in quotes
     * it is return enclosed in quotes in order to reflect the explicit indication of a text value. This means that
     * the string may contain ambiguous quotes when observed at face value, which situation is easily resolve using
     * the static methods <code>isQuoted()</code> and <code>stripQuotes</code>. Alternatively, the methods
     * <code>getTypedMemberValue()</code> or <code>createTypeValue()</code> may be used to get a Java typed object,
     * which may be identified using <code>instanceof</code>.
     * @return the current object member value (valid only if the current event is EVT_OBJECT_MEMBER)
     */
    public String getMemberValue() {
        return mbrValue;
    }

    /**
     * Get the current object member array flag. This indicates that the current member is an array. 
     * @return true if the current member is an array
     */
    public boolean getMemberArray() {
        return objectData.arrayDepth != 0;
    }

    /**
     * Get the current object member value applying JSON typing rules (valid only if the current event is
     * EVT_OBJECT_MEMBER). All text values must be quoted, otherwise they will be treated as a special value or
     * number.
     * <p>
     * Actual return types are as follows:
     * <ul>
     * <li>"..." - String value after quotes are stripped.
     * <li>true - Boolean.TRUE (not case sensitive, unlike strict JSON specification requirement)
     * <li>false - Boolean.FALSE (not case sensitive, unlike strict JSON specification requirement)
     * <li>null - null (not case sensitive, unlike strict JSON specification requirement)
     * <li>Anything else - BigDecimal (exception thrown from the BigDecimal constructor if not a valid number)
     * </ul>
     *
     * @param val - the data to process
     * @return the current object member value applying JSON typing rules
     * @throws NumberFormatException If an unquoted value which is no null, true or false is not a valid decimal
     *         number.
     */
    static public Object getTypedMemberValue(final String val) {
        return createTypedValue(val);
    }

    // *****************************************************************************
    // INSTANCE METHODS
    // *****************************************************************************

    /**
     * Construct a JSON parser from a character input source.
     *
     * @param inpnam A text description of the source, used only for location text.
     * @param inpsrc Input source.
     * @param inpcls Whether to close the input source at end-of-input.
     * @return the current JSONParser object for chaining purposes
     */
    public JsonParser setInput(final String inpnam, final Reader inpsrc, final boolean inpcls) {
        reset(false);

        inpName = inpnam;
        inpReader = inpsrc;
        inpClose = inpcls;

        if (optPreloadInput) {
            inpReader = preloadInput(inpnam, inpReader, inpClose, 0);
            inpClose = true;
        }

        return this;
    }

    /**
     * Construct a JSON parser from a byte input source.
     *
     * @param inpnam A text description of the source, used only for location text.
     * @param inpsrc Input source.
     * @param inpenc Character encoding used by the input source.
     * @param inpcls Whether to close the input source at end-of-input.
     * @return the current JSONParser object for chaining purposes
     */
    public JsonParser setInput(final String inpnam, final InputStream inpsrc, final String inpenc,
            final boolean inpcls) {
        reset(false);

        inpName = inpnam;
        try {
            inpReader = new InputStreamReader(inpsrc, inpenc);
        } catch (final UnsupportedEncodingException thr) {
            throw new Escape(Escape.BAD_ENCODING,
                    "The encoding '" + inpenc + "' is not supported by this Java Runtime Engine");
        }
        inpClose = inpcls;

        if (optPreloadInput) {
            inpReader = preloadInput(inpnam, inpReader, inpClose, 0);
            inpClose = true;
        }

        return this;
    }

    /**
     * Construct a JSON parser from a file input source.
     *
     * @param inpfil Input source.
     * @param inpenc Character encoding used by the input source.
     * @param bufsiz Size of input buffer for reading from the file.
     * @return the current JSONParser object for chaining purposes
     */
    public JsonParser setInput(final String inpfil, final String inpenc, final int bufsiz) {
        reset(false);

        inpName = inpfil;
        try {
            inpReader = new InputStreamReader(new BufferedInputStream(new FileInputStream(inpfil),
                    bufsiz), inpenc);
        } catch (final UnsupportedEncodingException thr) {
            throw new Escape(Escape.BAD_ENCODING,
                    "The encoding '" + inpenc + "' is not supported by this Java Runtime Engine",
                    thr);
        } catch (final IOException thr) {
            throw new Escape(Escape.IOERROR, "Could not access file \"" + inpfil + "\": " + thr,
                    thr);
        }
        inpClose = true;

        if (optPreloadInput) {
            inpReader = preloadInput(inpfil, inpReader, inpClose, Math.min(Integer.MAX_VALUE,
                    (int) new File(inpfil).length()));
            inpClose = true;
        }

        return this;
    }

    /**
     * Construct a JSON parser from a file input source.
     *
     * @param inpfil Input source.
     * @param inpenc Character encoding used by the input source.
     * @param bufsiz Size of input buffer for reading from the file.
     * @return the current JSONParser object for chaining purposes
     */
    public JsonParser setInput(final File inpfil, final String inpenc, final int bufsiz) {
        reset(false);

        inpName = inpfil.toString();
        try {
            inpReader = new InputStreamReader(new BufferedInputStream(new FileInputStream(inpfil),
                    bufsiz), inpenc);
        } catch (final UnsupportedEncodingException thr) {
            throw new Escape(Escape.BAD_ENCODING,
                    "The encoding '" + inpenc + "' is not supported by this Java Runtime Engine",
                    thr);
        } catch (final IOException thr) {
            throw new Escape(Escape.IOERROR, "Could not access file \"" + inpfil + "\": " + thr,
                    thr);
        }
        inpClose = true;

        if (optPreloadInput) {
            inpReader = preloadInput(inpfil.toString(), inpReader, inpClose, Math.min(
                    Integer.MAX_VALUE, (int) inpfil.length()));
            inpClose = true;
        }

        return this;
    }

    /**
     * Parse next event from input source.
     * @return the next event from the input source
     */
    public int next() {
        try {
            if (evtCode != EVT_INPUT_ENDED) {
                int pet = evtCode;                            // previous event code
                boolean qut = false, squ = false;                    // flags when within quotes,
                // and if they are single
                // quotes
                boolean amd = true;                               // flags whether we can accumulate more data
                // (set false after closing quote, true
                // after appropriate divider (: or ,))
                boolean pws = false;                              // flags that the previous input character
                // was whitespace
                int ich;                                    // input character as an integer

                evtCode = 0;
                evtLine = 0;
                evtColumn = 0;
                if (pet == EVT_OBJECT_BEGIN) {
                    mbrName = "";
                } else if (pet == EVT_OBJECT_ENDED) {
                    popObjectData();
                } else if (pet == EVT_ARRAY_ENDED) {
                    mbrName = "";
                } else if (pet == EVT_OBJECT_MEMBER) {
                    mbrName = "";
                }
                mbrValue = null;

                while ((ich = readChar()) != -1) {
                    if (!qut) {
                        // TEST FOR COMMENTS
                        if (ich == '*' || ich == '#') {
                            while ((ich = readChar()) != -1 && ich != '\n') {
                                ;
                            }
                        } else if (ich == '/') {
                            int tmp = readChar();
                            if (tmp == '/') {
                                while ((ich = readChar()) != -1 && ich != '\n') {
                                    ;
                                }
                            } else if (tmp == '*') {
                                if (!optMultilineComments) {
                                    throw parserError(Escape.MALFORMED,
                                            "Multiline comment not permitted by parser", null,
                                            evtLine, evtColumn);
                                }
                                while ((tmp = readChar()) != -1) {
                                    if (tmp == '*' && (tmp = readChar()) == '/') {
                                        break;
                                    }
                                }
                                if (tmp != '/') {
                                    throw parserError(Escape.MALFORMED,
                                            "Multiline comment not closed before EOF", null,
                                            evtLine, evtColumn);
                                }
                                ich = ' ';
                            } else {                                                      // one slash, but not two
                                unreadChar(tmp);
                                ich = '/';
                            }
                        }
                        if (ich == -1) {
                            break;
                        }
                    }

                    // --------------------------------------------------------------------------------------------------------------
                    // EACH OF THE FOLLOWING TESTS COMPLETES WITH A CONTINUE OR
                    // A RETURN OR THROWS (SEQUENCE MATTERS FOR THESE TESTS)
                    // --------------------------------------------------------------------------------------------------------------

                    if (ich == '\\') {
                        final int lin = inpLine, col = inpColumn;
                        if ((ich = readChar()) == -1) {
                            throw parserError(Escape.BAD_ESCAPE,
                                    "The input stream ended with an incomplete escape sequence",
                                    null, lin, col);
                        }
                        switch (ich) {
                            case '"': {
                                storeChar('\"');
                            }
                                continue;              // double quote
                            case '\\': {
                                storeChar('\\');
                            }
                                continue;              // backslash
                            case '/': {
                                storeChar('\u2044');
                            }
                                continue;              // solidus (I was suprised too!!)
                            case 'b': {
                                storeChar('\b');
                            }
                                continue;              // backspace
                            case 'f': {
                                storeChar('\f');
                            }
                                continue;              // form feed (aka vertical tab)
                            case 'n': {
                                storeChar('\n');
                            }
                                continue;              // line feed
                            case 'r': {
                                storeChar('\r');
                            }
                                continue;              // carriage return
                            case 't': {
                                storeChar('\t');
                            }
                                continue;              // horizontal tab
                            case 'u': {                                               // unicode 0x0000 - 0xFFFF
                                final int ic1 = readChar();
                                final int ic2 = readChar();
                                final int ic3 = readChar();
                                final int ic4 = readChar();
                                if (ic4 == -1) {
                                    throw parserError(
                                            Escape.BAD_ESCAPE,
                                            "The input stream ended with an incomplete escape sequence",
                                            null, lin, col);
                                }
                                storeChar((char) decodeHexChar((char) ic1, (char) ic2, (char) ic3,
                                        (char) ic4, lin, col));
                            }
                                continue;
                            case 'x': {                                               // ascii 0x00-0xFF
                                final int ic1 = readChar();
                                final int ic2 = readChar();
                                if (ic2 == -1) {
                                    throw parserError(
                                            Escape.BAD_ESCAPE,
                                            "The input stream ended with an incomplete escape sequence",
                                            null, lin, col);
                                }
                                storeChar((char) decodeHexByte((char) ic1, (char) ic2, lin, col));
                            }
                                continue;
                            default: {
                            }
                                throw parserError(
                                        Escape.BAD_ESCAPE,
                                        "The text string contains the invalid escape sequence '\\" + (char) ich,
                                        null, lin, col);
                        }
                    }

                    if (qut) {
                        if (ich < 0x0020 && !(optMultilineStrings && (ich == '\r' || ich == '\n'))) {
                            throw parserError(
                                    Escape.MALFORMED,
                                    "A quoted literal may not contain any control characters" + (optMultilineStrings ? " except CR and LF"
                                            : "") + " - controls must be escaped using \\uHHHH");
                        }
                        if (!squ && ich == '"' || squ && ich == '\'') {
                            qut = false;
                            amd = false;
                        }
                        storeChar((char) ich);
                        continue;
                    }

                    if (!optEolIsComma && (ich == '\r' || ich == '\n')) {
                        // ignore CR & LF if not treating EOL as a comma
                        continue;
                    }

                    if (ich == '"' || optSingleQuoteStrings && ich == '\'') {
                        if (accumulator.length() != 0) {
                            throw parserError(
                                    Escape.MALFORMED,
                                    "Text was found preceding an unescaped opening quote: \"" + accumulator + "\" (this is usually caused by a missing colon, a missing comma or missing quotes); Text=\"" + accumulator
                                            .toString() + "\"");
                        }
                        if (!amd) {
                            throw parserError(
                                    Escape.MALFORMED,
                                    "A string value cannot contain unescaped quotes (this is usually caused by a missing comma between members)");
                        }
                        qut = true;
                        squ = ich == '\'';
                        storeChar((char) ich);
                        continue;
                    }

                    switch (ich) {
                        case ':': {
                            if (mbrName.length() > 0) {
                                throw parserError(
                                        Escape.MALFORMED,
                                        "An object member value contained a colon but was not enclosed in quotes (this can often be caused by a missing comma between members)");
                            }
                            if (objectData.arrayDepth != 0) {
                                throw parserError(
                                        Escape.MALFORMED,
                                        "An array element cannot be a Name:Value pair - it must be only a value (this is most likely caused by misplaced or missing closing bracket)");
                            }
                            final String txt = getAccumulatedText(true);
                            if (txt.length() == 0) {
                                throw parserError(Escape.MALFORMED,
                                        "An object member name cannot be blank");
                            }

                            mbrName = stripQuotes(txt);
                            amd = true;

                            if (!optUnquotedKeywords && !isQuoted(txt)) {
                                throw parserError(Escape.MALFORMED,
                                        "An object member name was not enclosed in quotes");
                            }

                            continue;
                        }

                        case ',':
                        case '\r':
                        case '\n': {
                            if (accumulator.length() == 0) {                                   // empty value
                                // ignored
                                mbrName = "";                                                 // continue as if member-value
                                // event was previously generated
                                pet = EVT_OBJECT_MEMBER;                                      // ditto
                            } else if (pet == EVT_OBJECT_ENDED) {
                                throw parserError(
                                        Escape.MALFORMED,
                                        "Text was found between an object's closing brace and a subsequent comma or end of line (this is usually caused by a missing comma); Text=\"" + accumulator
                                                .toString() + "\"");
                            } else if (pet == EVT_ARRAY_ENDED) {
                                throw parserError(
                                        Escape.MALFORMED,
                                        "Text was found between an array's closing bracket and a subsequent comma (this is usually caused by a missing comma); Text=\"" + accumulator
                                                .toString() + "\"");
                            } else if (objectData.arrayDepth == 0 && mbrName.length() == 0) {
                                throw parserError(
                                        Escape.MALFORMED,
                                        "Object member name or value is missing in a Name:Value pair (this is possibly caused by missing array brackets in an array)");
                            } else {
                                if (objectData.arrayDepth != 0) {
                                    mbrName = objectData.arrayName;
                                }
                                mbrValue = getAccumulatedText(false);
                                return evtCode = EVT_OBJECT_MEMBER;
                            }
                            amd = true;
                            continue;
                        }

                        case '{': {
                            if (accumulator.length() != 0) {
                                throw parserError(
                                        Escape.MALFORMED,
                                        "Text was found preceding an object's opening brace (this is usually caused by a missing comma or colon, or by using equals instead of a colon); Text=\"" + accumulator
                                                .toString() + "\"");
                            }
                            pushObjectData();
                            if (objectData.arrayDepth != 0) {
                                mbrName = objectData.arrayName;
                            }
                            objectData = new ObjectData(mbrName);
                            return evtCode = EVT_OBJECT_BEGIN;
                        }

                        case '}': {
                            mbrValue = getAccumulatedText(false);
                            if (objectData.arrayDepth == 0 && mbrName.length() == 0 && mbrValue
                                    .length() > 0) {
                                throw parserError(
                                        Escape.MALFORMED,
                                        "Object member name or value is missing in a Name:Value pair (this is possibly caused by missing array brackets in an array)");
                            } else if (mbrValue.length() > 0) {
                                unreadChar(ich);
                                if (objectData.arrayDepth != 0) {
                                    mbrName = objectData.arrayName;
                                }
                                return evtCode = EVT_OBJECT_MEMBER;
                            }

                            mbrName = objectData.name;
                            return evtCode = EVT_OBJECT_ENDED;
                        }

                        case '[': {
                            if (accumulator.length() != 0) {
                                throw parserError(
                                        Escape.MALFORMED,
                                        "Text was found preceding an array's opening bracket (this is usually caused by a missing comma or colon, or by using equals instead of a colon); Text=\"" + accumulator
                                                .toString() + "\"");
                            }
                            if (objectData.arrayDepth == 0) {
                                objectData.arrayName = mbrName;
                            } else {
                                mbrName = objectData.arrayName;
                            }
                            objectData.arrayDepth++;
                            return evtCode = EVT_ARRAY_BEGIN;
                        }

                        case ']': {
                            if (objectData.arrayDepth == 0) {
                                throw parserError(
                                        Escape.MALFORMED,
                                        "Extraneous closing array bracket detected (this is usually caused by a missing opening array bracket)");
                            }

                            mbrValue = getAccumulatedText(false);
                            if (mbrValue.length() > 0) {
                                unreadChar(ich);
                                if (objectData.arrayDepth != 0) {
                                    mbrName = objectData.arrayName;
                                }
                                return evtCode = EVT_OBJECT_MEMBER;
                            }

                            objectData.arrayDepth--;
                            mbrName = objectData.arrayName;
                            if (objectData.arrayDepth == 0) {
                                objectData.arrayName = "";
                            }
                            return evtCode = EVT_ARRAY_ENDED;
                        }

                        default: {
                            if (Character.isWhitespace((char) ich)) {
                                pws = true;
                            } else {
                                if (!amd) {
                                    throw parserError(
                                            Escape.MALFORMED,
                                            "A string value cannot contain data after its closing quote (this is most likely caused by a missing comma between members)");
                                }
                                if (pws && accumulator.length() != 0) {
                                    throw parserError(
                                            Escape.MALFORMED,
                                            "Text with embedded spaces was found but not enclosed in quotes (this is often caused by a missing comma following an unquoted value); Text=\"" + accumulator
                                                    .toString() + "\"");
                                }
                                storeChar((char) ich);
                                pws = false;
                            }
                            continue;
                        }
                    }
                }

                // END OF INPUT REACHED
                mbrName = null;
                mbrValue = null;
                if (objectStack.size() != 0) {
                    if (qut) {
                        throw parserError(
                                Escape.MALFORMED,
                                "A string's closing quote was missing from the input data (end of input was reached before string was terminated)");
                    } else {
                        throw parserError(
                                Escape.MALFORMED,
                                "An object's closing brace was missing from the input data (end of input was reached before object was terminated)");
                    }
                }
                if (objectData.arrayDepth != 0) {
                    throw parserError(
                            Escape.MALFORMED,
                            "Array named '" + objectData.arrayName + "' was not ended (end of input was reached before array was terminated)");
                }

                evtLine = inpLine;
                evtColumn = inpColumn;
                if (inpClose) {
                    try {
                        inpReader.close();
                    } catch (final Throwable ign) {
                        ;
                    }
                }
            }
            return evtCode = EVT_INPUT_ENDED;
        } catch (final IOException thr) {
            if (inpClose) {
                try {
                    inpReader.close();
                } catch (final Throwable ign) {
                    ;
                }
            }
            throw new Escape(Escape.IOERROR, "I/O Exception: " + thr, thr);
        }
    }

    /**
     * Skip the object that the parser is currently positioned at.  The current event must be EVT_OBJECT_BEGIN.
     */
    public void skipObject() {
        if (getEventCode() != EVT_OBJECT_BEGIN) {
            throw parserError(Escape.INVALID_STATE,
                    "An object can only be skipped when the current event is EVT_OBJECT_BEGIN");
        }

        int level = 1;
        while (level > 0) {
            final int eventCode = next();
            if (eventCode == EVT_OBJECT_BEGIN) {
                level++;
            } else if (eventCode == EVT_OBJECT_ENDED) {
                level--;
            }
        }
    }

    /**
     * Skip the array that the parser is currently positioned at.  The current event must be EVT_ARRAY_BEGIN.
     */
    public void skipArray() {
        if (getEventCode() != EVT_ARRAY_BEGIN) {
            throw parserError(Escape.INVALID_STATE,
                    "An object can only be skipped when the current event is EVT_ARRAY_BEGIN");
        }

        int level = 1;
        while (level > 0) {
            final int eventCode = next();
            if (eventCode == EVT_ARRAY_BEGIN) {
                level++;
            } else if (eventCode == EVT_ARRAY_ENDED) {
                level--;
            } else if (eventCode == EVT_OBJECT_BEGIN) {
                skipObject();
            }
        }
    }

    private void storeChar(final char ch) {
        if (evtLine == 0) {
            evtLine = inpLine;
            evtColumn = inpColumn;
        }
        accumulator.append(ch);
    }

    private int decodeHexByte(final char c1, final char c2, final int lin, final int col) {
        try {
            return decodeHexByte(c1, c2);
        } catch (final Exception thr) {
            throw parserError(Escape.BAD_ESCAPE, thr.getMessage(), null, lin, col);
        }
    }

    private int decodeHexChar(final char c1, final char c2, final char c3, final char c4,
            final int lin, final int col) {
        try {
            return decodeHexChar(c1, c2, c3, c4);
        } catch (final Exception thr) {
            throw parserError(Escape.BAD_ESCAPE, thr.getMessage(), null, lin, col);
        }
    }

    private Location createLocation(final int lin, final int col) {
        return new Location(inpName, lin, col, mbrName != null && mbrName.length() != 0 ? mbrName
                : objectData.arrayName);
    }

    // *****************************************************************************
    // INSTANCE METHODS - PRIVATE
    // *****************************************************************************

    private int readChar() throws IOException {
        int ich;

        if (pushBack != -1) {
            ich = pushBack;
            pushBack = -1;
            inpColumn++;
        } else {
            if ((ich = inpReader.read()) != -1) {
                if (ich == '\n') {
                    inpColumn = 0;
                    inpLine++;
                } else if (ich == '\uFEFF') {
                    ich = ' ';
                }                  // FEFF is used as BOM; otherwise is a zero width space
                else {
                    inpColumn++;
                }
            }
        }
        return ich;
    }

    private void unreadChar(final int ich) throws IOException {
        if (ich != -1) {
            if (pushBack != -1) {
                throw parserError(
                        Escape.GENERAL,
                        "Cannot unread '" + (char) ich + "' the character '" + (char) pushBack + "' is already pending");
            }
            pushBack = ich;
            if (ich == '\n') {
                inpColumn = 0;
                inpLine--;
            } else {
                inpColumn--;
            }
        }
    }

    private void pushObjectData() {
        objectStack.add(objectData);
    }

    private void popObjectData() {
        final int siz = objectStack.size();

        if (siz == 0) {
            throw parserError(Escape.MALFORMED,
                    "An extraneous object closing brace was present in the input data");
        }

        if (objectData.arrayDepth != 0) {
            throw parserError(
                    Escape.MALFORMED,
                    "Array named '" + objectData.arrayName + "' was not ended (end of enclosing object was reached before array was ended)");
        }

        objectData = objectStack.remove(siz - 1);
        mbrName = objectData.name;
    }

    private String getAccumulatedText(final boolean kwd) {
        String val;

        val = accumulator.toString().trim();
        accumulator.setLength(0);
        if (kwd) {
            if (optInternKeywords) {
                val = val.intern();
            }
        } else {
            if (optInternValues) {
                val = val.intern();
            }
        }
        return val;
    }

    private Escape parserError(final int cod, final String txt) {
        return parserError(cod, txt, null);
    }

    private Escape parserError(final int cod, final String txt, final Throwable thr) {
        return parserError(cod, txt, thr, inpLine, inpColumn);
    }

    private Escape parserError(final int cod, final String txt, final Throwable thr, final int lin,
            final int col) {
        return new Escape(cod, txt + "; at " + createLocation(lin, col), thr);
    }

    // *****************************************************************************
    // INSTANCE INNER CLASSES
    // *****************************************************************************

    // *****************************************************************************
    // STATIC NESTED CLASSES
    // *****************************************************************************

    static public final class Location extends Object {

        private final String inpName;

        private final int inpLine;

        private final int inpCol;

        private final String mbrName;

        Location(final String inpnam, final int inplin, final int inpcol, final String mbrnam) {
            inpName = inpnam;
            inpLine = inplin;
            inpCol = inpcol;
            mbrName = mbrnam == null || mbrnam.length() == 0 ? null : mbrnam;
        }

        @Override
        public String toString() {
            if (inpCol > 0) {
                return "Input Source: \"" + inpName + "\", Line: " + inpLine + ", Column: " + inpCol + (mbrName == null ? ""
                        : ", Member Name: " + mbrName);
            } else {
                return "Input Source: \"" + inpName + "\", Line: " + (inpLine - 1) + ", Column: EOL" + (mbrName == null ? ""
                        : ", Member Name: " + mbrName);
            }
        }

        public String getInputSource() {
            return inpName;
        }

        public int getInputLine() {
            return inpLine;
        }

        public int getInputColumn() {
            return inpCol;
        }

        public String getMemberName() {
            return mbrName;
        }
    }

    // *****************************************************************************
    // STATIC NESTED CLASSES
    // *****************************************************************************

    static class ObjectData extends Object {

        final String name;                                   // object name (may be "")

        String arrayName;                              // array member name for this object (initially "")

        int arrayDepth;                             // array depth to detect when multi-dimensional array

        // finally ends

        ObjectData(final String nam) {
            name = nam;
            arrayName = "";
            arrayDepth = 0;
        }
    }

    // *****************************************************************************
    // STATIC PROPERTIES
    // *****************************************************************************

    /** Returned from next() when the beginning of an object is read. */
    static public final int EVT_OBJECT_BEGIN = 1;

    /** Returned from next() when the end of an object is read. */
    static public final int EVT_OBJECT_ENDED = 2;

    /** Returned from next() when the beginning of a declared array is read. */
    static public final int EVT_ARRAY_BEGIN = 3;

    /** Returned from next() when the end of a declared array is read. */
    static public final int EVT_ARRAY_ENDED = 4;

    /** Returned from next() when the end of the input source is reached. */
    static public final int EVT_INPUT_ENDED = 5;

    /** Returned from next() when a simple object member (Name:Value pair or array element) is read. */
    static public final int EVT_OBJECT_MEMBER = 6;

    static private String[] EVT_NAMES = { "Invalid",                                                                  // 0
            "ObjectBegin",                                                              // 1
            "ObjectEnded",                                                              // 2
            "ArrayBegin",                                                               // 3
            "ArrayEnded",                                                               // 4
            "InputEnded",                                                               // 6
            "ObjectMember",                                                             // 5
    };

    /** Option to allow keywords to be unquoted. */
    static public final int OPT_UNQUOTED_KEYWORDS = 0x00000001;

    /** Option to allow an end-of-line to be treated as a comma. */
    static public final int OPT_EOL_IS_COMMA = 0x00000002;

    /** Option to allow multiline comments using &#47;* and *&#47;. */
    static public final int OPT_MULTILINE_COMMENTS = 0x00000004;

    /** Option to allow mutiline strings - this permits strings to be broken over multiple lines in an unambigous manner. */
    static public final int OPT_MULTILINE_STRINGS = 0x00000008;

    /** Option to allow single-quotes to be used for strings. */
    static public final int OPT_SINGLE_QUOTE_STRINGS = 0x00000010;

    /** Option to preload file input data when the input source is set - this is not a JSON compliance option. Preloading file contents can be useful in order to overlap loading and processing of multiple files using a deferred processing pipeline. */
    static public final int OPT_PRELOAD_INPUT = 0x20000000;

    /** Option to cause keywords to be interned (String.intern()) - this is not a JSON compliance option. Interning keywords is typically a good idea and is generally recommended. */
    static public final int OPT_INTERN_KEYWORDS = 0x40000000;

    /** Option to cause keywords to be interned (String.intern()) - this is not a JSON compliance option. Interning values is typically a good idea and is generally recommended. */
    static public final int OPT_INTERN_VALUES = 0x80000000;

    /** All options off. Input must conform strictly to the JSON spec. */
    static public final int OPT_STRICT = 0;

    /** Recommended options for messaging parsing mode - OPT_UNQUOTED_KEYWORDS, OPT_INTERN_KEYWORDS. */
    static public final int OPT_MESSAGING = OPT_UNQUOTED_KEYWORDS | OPT_INTERN_KEYWORDS;

    /** Recommended for config parsing mode - OPT_UNQUOTED_KEYWORDS, OPT_EOL_IS_COMMA, OPT_MULTILINE_COMMENTS, OPT_SINGLE_QUOTE_STRINGS, OPT_INTERN_KEYWORDS, OPT_INTERN_VALUES. */
    static public final int OPT_CONFIG = OPT_UNQUOTED_KEYWORDS | OPT_EOL_IS_COMMA | OPT_MULTILINE_COMMENTS | OPT_SINGLE_QUOTE_STRINGS | OPT_INTERN_KEYWORDS | OPT_INTERN_VALUES;

    /** All options on. */
    static public final int OPT_ALL = 0xFFFFFFFF;

    // *****************************************************************************
    // STATIC INIT & MAIN
    // *****************************************************************************

    // *****************************************************************************
    // STATIC METHODS - PUBLIC UTILITY
    // *****************************************************************************

    /**
     * Create a typed member value applying JSON typing rules.  All text values must be quoted.
     * <p>
     * Actual return types are as follows:
     * <ul>
     * <li>true - Boolean.TRUE (not case sensitive)
     * <li>false - Boolean.FALSE (not case sensitive)
     * <li>null - Java null (not case sensitive)
     * <li>"..." - String value after quotes are stripped
     * <li>Anything else - BigDecimal (exception thrown from the BigDecimal constructor if not a valid number)
     * </ul>
     * @param val - the value to use to create a typed member
     * @return a types member value based on the contents of val
     * @throws NumberFormatException If an unquoted value which is no null, true or false is not a valid decimal number.
     */
    static public Object createTypedValue(final String val) {
        if (val.equalsIgnoreCase("null")) {
            return null;
        } else if (val.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        } else if (val.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        } else if (isQuoted(val)) {
            return stripQuotes(val);
        } else if (val.length() == 0) {
            return val;
        } else {
            return new BigDecimal(val);
        }
    }

    /**
     * Get a text name for the event code.
     * @param cod - the event code
     * @return the event name for that event code
     */
    static public String getEventName(final int cod) {
        if (cod < 1) {
            return EVT_NAMES[0];
        } else if (cod >= EVT_NAMES.length) {
            return "EVT_" + cod;
        } else {
            return EVT_NAMES[cod];
        }
    }

    /**
     * Strip the text-value-indicating quotes from the supplied member value, if any.
     * @param val - a string perhaps surrounded with quotes
     * @return the string with the quotes removed
     */
    static public String stripQuotes(String val) {
        if (isQuoted(val)) {
            val = val.substring(1, val.length() - 1);
        }
        return val;
    }

    /**
     * Test if the member value is enclosed in text-value-indicating quotes.
     * @param val - a string perhaps surrounded with quotes
     * @return if the member value is enclosed in text-value-indicating quotes
     */
    static public boolean isQuoted(final String val) {
        int len;

        if (val != null && (len = val.length()) > 1) {
            final char ch0 = val.charAt(0);
            if ((ch0 == '"' || ch0 == '\'') && ch0 == val.charAt(len - 1)) {
                return true;                                                        // NOTE: Don't test for unescaped quotes because
                // the value has already been unescaped as it was
                // parsed.
            }
        }
        return false;
    }

    // *****************************************************************************
    // STATIC METHODS - PRIVATE UTILITY
    // *****************************************************************************

    static private Reader preloadInput(final String inpnam, final Reader inprdr,
            final boolean inpcls, final int sizest) throws Escape {
        CharArrayWriter wtr;                                    // writer
        final char[] buf = new char[10240];                    // read buffer
        int len;                                    // read length

        try {
            wtr = new CharArrayWriter(sizest > 0 ? sizest : 10240);
            while ((len = inprdr.read(buf)) != -1) {
                wtr.write(buf, 0, len);
            }
            wtr.flush();
            if (inpcls) {
                wtr.close();
            }
            return new CharArrayReader(wtr.toCharArray());
        } catch (final IOException thr) {
            throw new Escape(Escape.IOERROR,
                    "Could not read input from \"" + inpnam + "\": " + thr, thr);
        }
    }

    // *****************************************************************************
    // STATIC METHODS - HEX ENCODE/DECODE
    // *****************************************************************************

    static int[] decodeHex;                              // hex table for decoding hex-based escapes
    static {
        decodeHex = new int[256];
        for (int xa = 0; xa < decodeHex.length; xa++) {
            decodeHex[xa] = -1;
        }
        decodeHex['0'] = 0;
        decodeHex['1'] = 1;
        decodeHex['2'] = 2;
        decodeHex['3'] = 3;
        decodeHex['4'] = 4;
        decodeHex['5'] = 5;
        decodeHex['6'] = 6;
        decodeHex['7'] = 7;
        decodeHex['8'] = 8;
        decodeHex['9'] = 9;
        decodeHex['A'] = 10;
        decodeHex['B'] = 11;
        decodeHex['C'] = 12;
        decodeHex['D'] = 13;
        decodeHex['E'] = 14;
        decodeHex['F'] = 15;
        decodeHex['a'] = 10;
        decodeHex['b'] = 11;
        decodeHex['c'] = 12;
        decodeHex['d'] = 13;
        decodeHex['e'] = 14;
        decodeHex['f'] = 15;
    }

    static private int decodeHexByte(final char hex1, final char hex2) {
        int n1;                                 // nibble 1
        int n2;                                 // nibble 2

        if (hex1 > 0xFF || (n1 = decodeHex[hex1]) == -1) {
            throw new RuntimeException(
                    "Escape sequence contains the invalid hexadecimal digit '" + hex1 + "'; not 0-9, a-f or A-F");
        }
        if (hex2 > 0xFF || (n2 = decodeHex[hex2]) == -1) {
            throw new RuntimeException(
                    "Escape sequence contains the invalid hexadecimal digit '" + hex2 + "'; not 0-9, a-f or A-F");
        }
        return n1 << 4 | n2;
    }

    static private int decodeHexChar(final char hex1, final char hex2, final char hex3,
            final char hex4) {
        return decodeHexByte(hex1, hex2) << 8 | decodeHexByte(hex3, hex4);
    }

    // *****************************************************************************
    // STATIC NESTED CLASSES - ESCAPE (COULD DROP THIS FOR RUNTIME EXCEPTION)
    // *****************************************************************************

    static public class Escape extends RuntimeException {

        /**
         * A <code>serialVersionUID</code> for the class.
         */
        private static final long serialVersionUID = 7769040813982342515L;

        /** General exception. */
        static public final int GENERAL = 1;

        /** Input/Output error. */
        static public final int IOERROR = 2;

        /** Invalid encoding. */
        static public final int BAD_ENCODING = 3;

        /** Malformed input data - the data was not valid JSON data-interchange format. */
        static public final int MALFORMED = 4;

        /** A bad escape sequence was encountered. */
        static public final int BAD_ESCAPE = 6;

        /** An method was invokeD when the parser was in an invalid state for it. */
        static public final int INVALID_STATE = 7;

        /** An method could not be reflectively retrieved or invoked. */
        static public final int METHOD_ERROR = 8;

        /** Data-type conversion error. */
        static public final int CONVERSION = 9;

        /** Minimum code required for any sub-class. */
        static public final int SUBCLSMIN = 1000;

        private final int code;

        /**
         * Create an exception with a code and details.  It is recommened that the detail always include very specific information about the cause of the error.
         * @param code      The error code.
         * @param detail    Specific detail text indicating the cause of the error.
         */
        public Escape(final int code, final String detail) {
            this(code, detail, null);
        }

        /**
         * Create an exception with a code and details.  It is recommened that the detail always include very specific information about the cause of the error.
         * @param code      The error code.
         * @param detail    Specific detail text indicating the cause of the error.
         * @param cause     The causitive throwable object, if any.
         */
        public Escape(final int code, final String detail, final Throwable cause) {
            super(detail, cause);

            this.code = code;
        }

        /** 
         * Return the numeric code for this exception condition.
         * @return the numeric code for this exception condition
         */
        public int getCode() {
            return code;
        }
    }

} // END PUBLIC CLASS
