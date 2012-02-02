/*******************************************************************************
 * Copyright (c) 2012 Mathias Kunter and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Mathias Kunter       - Initial Implementation (Bug 307311)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.text.ParseException;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The MIStringHandler class provides several static functions to handle C and / or MI strings.
 * @since 4.1
 */
public class MIStringHandler {
    
    /**
     * A map of special characters which are used within escape notations to represent a
     * corresponding Unicode code point (i.e. character code).
     */
	// Use a LinkedHashMap to preserve order, so as to get 'e' and not 'E'
    private static Map<Character,Integer> fSpecialCharactersToCodePointMap = new LinkedHashMap<Character,Integer>();
    static {
    	fSpecialCharactersToCodePointMap.put('a',  0x07);    // Alert (bell) character
    	fSpecialCharactersToCodePointMap.put('b',  0x08);    // Backspace character
    	fSpecialCharactersToCodePointMap.put('e',  0x1B);    // GNU extension: Escape character
    	fSpecialCharactersToCodePointMap.put('E',  0x1B);    // same as 'e'
    	fSpecialCharactersToCodePointMap.put('f',  0x0C);    // Form feed character
    	fSpecialCharactersToCodePointMap.put('n',  0x0A);    // New line character
    	fSpecialCharactersToCodePointMap.put('r',  0x0D);    // Carriage return character
    	fSpecialCharactersToCodePointMap.put('t',  0x09);    // Horizontal tabulation character
    	fSpecialCharactersToCodePointMap.put('v',  0x0B);    // Vertical tabulation character
    	fSpecialCharactersToCodePointMap.put('\'', 0x27);    // Single quotation mark
    	fSpecialCharactersToCodePointMap.put('"',  0x22);    // Double quotation mark
    	fSpecialCharactersToCodePointMap.put('\\', 0x5C);    // Backslash
    	fSpecialCharactersToCodePointMap.put('?',  0x3F);    // Literal question mark
    }

    /**
     * An internal helper enumeration which holds the current status while parsing an escaped
     * text sequence.
     */
    private enum EscapeStatus {
        NONE,
        BEGIN,
        OCTAL_NUMBER,
        HEX_NUMBER,
        UNICODE_SHORT_NUMBER,
        UNICODE_LONG_NUMBER,
        VALID,
        INVALID
    }
    
    /**
     * An enumeration defining the escape sequences which should be parsed.
     */
    public enum ParseFlags {
        SPECIAL_CHARS,
        OCTAL_NUMBERS,
        HEX_NUMBERS,
        UNICODE_SHORT_NUMBERS,
        UNICODE_LONG_NUMBERS
    }
    
    /**
     * Translates the given C string into a string suitable for display. This includes handling
     * of escaped characters and different string encodings. This is necessary in order to correctly
     * deal with non-ASCII strings.
     * @param str The C string to translate.
     * @param escapeChars Defines whether non-printable characters should be escaped within
     * the translated string, or not.
     * @return The translated string.
     */
    public static String translateCString(String str, boolean escapeChars) {
        if (escapeChars) {
            // Don't parse the special character escape notations here. We can do this here because
            // we want to keep them in their escaped form anyway, and because the following string
            // transcoding process isn't affected by escaped special chars. By doing so we avoid
            // caring about some nasty details of the special character escaping process: for
            // example, single quotation marks are commonly only escaped within character constants,
            // while double quotation marks are commonly only escaped within string constants. By
            // not parsing the special character escape notations at all here, we just keep the
            // original special character escaping provided by the given MI string.
            str = parseString(str, EnumSet.complementOf(EnumSet.of(ParseFlags.SPECIAL_CHARS)));
        } else {
            // Parse all escaped characters.
            str = parseString(str);
        }
        
        // Transcode the string in order to handle non-ASCII strings correctly.
        str = transcodeString(str);
        
        if (escapeChars) {
            // Escape any non-printable characters again, as we want to be able to display them.
            // However, don't escape any printable special chars, as they haven't been parsed before.
            str = escapeString(str, false);
        } else {
            // No escaping necessary here. We however have to make sure that we use the correct line
            // separation character sequence.
            str = str.replace("\n", System.getProperty("line.separator", "\n")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        
        return str;
    }
    
    /**
     * Returns whether the given character is a special character, or not.
     * @param c The character to test.
     * @return The test result.
     */
    public static boolean isSpecialChar(char c) {
    	return fSpecialCharactersToCodePointMap.containsKey(c);
    }
    
    /**
     * Returns whether the given Unicode code point is a special code point, or not.
     * @param codePoint The Unicode code point to test.
     * @return The test result.
     */
    public static boolean isSpecialCodePoint(int codePoint) {
    	return fSpecialCharactersToCodePointMap.containsValue(codePoint);
    }
    
    /**
     * Parses the given special character into an Unicode code point.
     * @param c The special character to parse.
     * @return The parsed Unicode code point.
     * @throws ParseException Thrown when the given character can't be parsed. This happens when it's
     * not a special character.
     */
    public static int parseSpecialChar(char c) throws ParseException {
    	Integer codePoint = fSpecialCharactersToCodePointMap.get(c);
    	if (codePoint != null) {
    		return codePoint;
    	}
        throw new ParseException("The given character '" + c + "' is not a special character.", 0); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /**
     * Parses the given special Unicode code point into a character.
     * @param codePoint The special Unicode code point to parse.
     * @return The parsed character.
     * @throws ParseException Thrown when the given Unicode code point can't be parsed. This happens
     * when it's not a special code point.
     */
    public static char parseSpecialCodePoint(int codePoint) throws ParseException {
    	for (Entry<Character, Integer> entry : fSpecialCharactersToCodePointMap.entrySet()) {
            if (entry.getValue().equals(codePoint)) {
                return entry.getKey();
            }
        }
        throw new ParseException("The given Unicode code point " + codePoint + " is not a special code point.", 0); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /**
     * This is an overloaded function. See the Javadoc of the other function overload for details.
     * @param str The string which should be parsed.
     * @return The parsed string.
     */
    public static String parseString(String str) {
        return parseString(str, EnumSet.allOf(ParseFlags.class));
    }

    /**
     * Parses any escaped characters and replaces them with their corresponding Unicode code points.
     * This function parses all escape notations which are supported by gcc and / or gdb. Those are:</br></br>
     * 
     * <ul>
     * <li>Special char escape notations: \a, \b, \e, \E, \f, \n, \r, \t, \v, \', \", \\, and \?</li>
     * 
     * <li>Octal escape notation: An initial backslash, followed by 1, 2, or 3 octal digits. Values
     * above 0xFF are ignored. Octal escape notations may not use more than 3 octal digits.</li>
     * 
     * <li>Hexadecimal escape notation: An initial backslash, followed by an "x" and 1 or more
     * hexadecimal digits. Hexadecimal escape notations may not use more than 4 hexadecimal digits
     * (although gcc accepts hexadecimal escape notations of any arbitrary length).</li>
     * 
     * <li>Short Unicode escape notation: An initial backslash, followed by an "u" and exactly 4
     * hexadecimal digits.</li>
     * 
     * <li>Long Unicode escape notation: An initial backslash, followed by an "U" and exactly 8
     * hexadecimal digits.</li>
     * </ul>
     * @param str The string which should be parsed.
     * @param parseFlags The set of escape notations which should be parsed.
     * @return The parsed string.
     */
    public static String parseString(String str, EnumSet<ParseFlags> parseFlags) {
        StringBuffer buffer = new StringBuffer();
        StringBuffer escapeBuffer = new StringBuffer();
        EscapeStatus escStatus = EscapeStatus.NONE;
        
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            boolean consumeChar = true;
            boolean isLastChar = i == str.length() - 1;
            
            if (escStatus == EscapeStatus.NONE) {
                if (c == '\\') {
                    // Escaping begins. Reset the escape buffer.
                    escapeBuffer.setLength(0);
                    escapeBuffer.append(c);
                    escStatus = EscapeStatus.BEGIN;
                }
            } else if (escStatus == EscapeStatus.BEGIN) {
                if (parseFlags.contains(ParseFlags.SPECIAL_CHARS) && isSpecialChar(c)) {
                    try {
                        buffer.appendCodePoint(parseSpecialChar(c));
                        escStatus = EscapeStatus.VALID;
                    } catch (ParseException e) {
                        // This is just for completeness. We will actually never catch any ParseException here
                        // since we already checked the character with isSpecialChar() before.
                        escapeBuffer.append(c);
                        escStatus = EscapeStatus.INVALID;
                    }
                } else if (parseFlags.contains(ParseFlags.OCTAL_NUMBERS) && c >= '0' && c <= '7') {
                    escStatus = EscapeStatus.OCTAL_NUMBER;
                    // Don't consume this character right now - as this wouldn't work if it's the last character.
                    consumeChar = false;
                } else if (parseFlags.contains(ParseFlags.HEX_NUMBERS) && c == 'x') {
                    escStatus = EscapeStatus.HEX_NUMBER;
                } else if (parseFlags.contains(ParseFlags.UNICODE_SHORT_NUMBERS) && c == 'u') {
                    escStatus = EscapeStatus.UNICODE_SHORT_NUMBER;
                } else if (parseFlags.contains(ParseFlags.UNICODE_LONG_NUMBERS) && c == 'U') {
                    escStatus = EscapeStatus.UNICODE_LONG_NUMBER;
                } else {
                    escStatus = EscapeStatus.INVALID;
                }
                if (consumeChar) {
                    escapeBuffer.append(c);
                }
            } else if (escStatus == EscapeStatus.HEX_NUMBER) {
                // Only consume this character if it belongs to the escape sequence.
                consumeChar = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
                if (consumeChar) {
                    escapeBuffer.append(c);
                }
                
                if (!consumeChar || isLastChar || escapeBuffer.length() == 6) {
                    // The escape sequence is terminated. Set the escape status to invalid until
                    // we know that it's actually valid.
                    escStatus = EscapeStatus.INVALID;
                    if (escapeBuffer.length() > 2) {
                        // Decode the hexadecimal number.
                        try {
                            int codePoint = Integer.parseInt(escapeBuffer.toString().substring(2), 16);
                            if (codePoint <= 0x10FFFF) {
                                buffer.appendCodePoint(codePoint);
                                escStatus = EscapeStatus.VALID;
                            }
                        } catch (NumberFormatException e) {
                        }
                    }
                }
            } else if (escStatus == EscapeStatus.UNICODE_SHORT_NUMBER || escStatus == EscapeStatus.UNICODE_LONG_NUMBER) {
                // Only consume this character if it belongs to the escape sequence.
                consumeChar = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
                if (consumeChar) {
                    escapeBuffer.append(c);
                }
                
                int finalLength = escStatus == EscapeStatus.UNICODE_SHORT_NUMBER ? 6 : 10;
                if (escapeBuffer.length() == finalLength) {
                    // The escape sequence is terminated. Set the escape status to invalid until
                    // we know that it's actually valid. Decode the hexadecimal number.
                    escStatus = EscapeStatus.INVALID;
                    try {
                        int codePoint = Integer.parseInt(escapeBuffer.toString().substring(2), 16);
                        if (codePoint <= 0x10FFFF) {
                            buffer.appendCodePoint(codePoint);
                            escStatus = EscapeStatus.VALID;
                        }
                    } catch (NumberFormatException e) {
                    }
                } else if (!consumeChar || isLastChar) {
                    // The escape sequence is terminated and invalid.
                    escStatus = EscapeStatus.INVALID;
                }
            } else if (escStatus == EscapeStatus.OCTAL_NUMBER) {
                // Only consume this character if it belongs to the escape sequence.
                consumeChar = c >= '0' && c <= '7';
                if (consumeChar) {
                    escapeBuffer.append(c);
                }
                
                if (!consumeChar || isLastChar || escapeBuffer.length() == 4) {
                    // The escape sequence is terminated. Set the escape status to invalid until
                    // we know that it's actually valid.
                    escStatus = EscapeStatus.INVALID;
                    if (escapeBuffer.length() > 1) {
                        // Decode the octal number.
                        try {
                            int codePoint = Integer.parseInt(escapeBuffer.toString().substring(1), 8);
                            if (codePoint <= 0xFF) {
                                buffer.appendCodePoint(codePoint);
                                escStatus = EscapeStatus.VALID;
                            }
                        } catch (NumberFormatException e) {
                        }
                    }
                }
            }
            
            if (escStatus == EscapeStatus.NONE) {
                // Current character isn't escaped - copy it over to the destination buffer.
                buffer.append(c);
            } else if (escStatus == EscapeStatus.VALID) {
                escStatus = EscapeStatus.NONE;
            } else if (escStatus == EscapeStatus.INVALID) {
                buffer.append(escapeBuffer);
                escStatus = EscapeStatus.NONE;
            }
            
            if (!consumeChar) {
                // Don't consume the current character.
                i--;
            }
        }
        
        // Check for non-finished escape sequences at the end of the string.
        if (escStatus != EscapeStatus.NONE) {
            buffer.append(escapeBuffer);
        }
        
        // Convert the buffer into a string and return it.
        return buffer.toString();
    }
    
    /**
     * Transcodes the given string. This is done as follows:</br></br>
     * 1) The given string is encoded into a binary byte buffer.</br></br>
     * 2) It's tested whether this binary byte buffer seems to represent a string which is encoded as
     * either ASCII, Latin-1, or UTF-8. If this is the case, the binary byte buffer is decoded back into
     * a string and this string is returned. If the test is negative, the given string is returned without
     * modification because its encoding can't be reliably determined in this case.
     * The most important use case of this function is to transcode a string which is actually UTF-8 but has
     * been incorrectly decoded as Latin-1 instead.
     * @param str The string to transcode.
     * @return The transcoded string.
     */
    public static String transcodeString(String str) {
        // Try to transcode the string from Latin-1 to UTF-8 (ASCII doesn't need to be explicitly
        // considered here since Latin-1 is backwards compatible with ASCII). The transcoding will
        // almost certainly only succeed if the string actually *is* encoded in UTF-8. If the
        // transcoding fails, the string is simply left unchanged.
        try {
            // First, try to encode the string as Latin-1 in order to obtain the binary byte
            // representation of the string.
            CharsetEncoder latin1Encoder = Charset.forName("ISO-8859-1").newEncoder(); //$NON-NLS-1$
            ByteBuffer stringBytes = latin1Encoder.encode(CharBuffer.wrap(str.toCharArray()));
            
            // Next, try to decode the string as UTF-8. This will almost certainly only succeed
            // if the string actually *is* encoded in UTF-8. Note that if the decoding fails,
            // an exception is thrown before the str variable is assigned. The original string
            // is therefore left unchanged in this case.
            CharsetDecoder utf8Decoder = Charset.forName("UTF-8").newDecoder(); //$NON-NLS-1$
            str = utf8Decoder.decode(stringBytes).toString();
        } catch (Exception e) {
        }
        
        return str;
    }
    
    /**
     * Escapes any non-printable characters as well as the printable special characters single quotation
     * mark, double quotation mark, backslash, and literal question mark within the given string. Supports
     * the entire Unicode code space.
     * @param str The string which should be escaped.
     * @return The escaped string.
     */
    public static String escapeString(String str) {
        return escapeString(str, true);
    }
    
    /**
     * Escapes any non-printable characters within the given string. Supports the entire Unicode code space.
     * @param str The string which should be escaped.
     * @param escapePrintableSpecialChars Defines whether the printable special characters single
     * quotation mark, double quotation mark, backslash, and literal question mark should be
     * escaped as well, or not.
     * @return The escaped string.
     */
    public static String escapeString(String str, boolean escapePrintableSpecialChars) {
        StringBuffer buffer = new StringBuffer();
        
        for (int i = 0; i < str.length(); i++) {
            // Get the current character code point. Note that using the Java "char" data type isn't
            // sufficient here, as it can't handle all Unicode characters.
            int codePoint = str.codePointAt(i);
            if (Character.isSupplementaryCodePoint(codePoint)) {
                i++;
            }
            
            // Check the code point type of the character in order to determine whether it's
            // printable or not.
            int codePointType = Character.getType(codePoint);
            switch (codePointType) {
                case Character.LINE_SEPARATOR:
                case Character.PARAGRAPH_SEPARATOR:
                case Character.CONTROL:
                case Character.PRIVATE_USE:
                case Character.SURROGATE:
                case Character.UNASSIGNED:
                    // Non-printable character.
                    if (isSpecialCodePoint(codePoint)) {
                        // Escape by using the special character escape notation.
                        buffer.append('\\');
                        try {
                            buffer.append(parseSpecialCodePoint(codePoint));
                        } catch (ParseException e) {
                            buffer.appendCodePoint(codePoint);
                        }
                    } else if (codePoint == 0x00) {
                        // Escape the null character separately - don't use leading zeros.
                        buffer.append("\\0"); //$NON-NLS-1$
                    } else if (codePoint <= 0xFF) {
                        // Escape by using the octal escape notation.
                        buffer.append(String.format("\\%03o", codePoint)); //$NON-NLS-1$
                    } else if (codePoint <= 0xFFFF) {
                        // Escape by using the short Unicode escape notation.
                        buffer.append(String.format("\\u%04x", codePoint)); //$NON-NLS-1$
                    } else {
                        // Escape by using the long Unicode escape notation.
                        buffer.append(String.format("\\U%08x", codePoint)); //$NON-NLS-1$
                    }
                    break;
                default:
                    // Printable character.
                    if (escapePrintableSpecialChars && isSpecialCodePoint(codePoint)) {
                        // Escape by using the special character escape notation.
                        buffer.append('\\');
                        try {
                            buffer.append(parseSpecialCodePoint(codePoint));
                        } catch (ParseException e) {
                            buffer.appendCodePoint(codePoint);
                        }
                    } else {
                        // Don't escape.
                        buffer.appendCodePoint(codePoint);
                    }
            }
        }
        
        return buffer.toString();
    }
}
