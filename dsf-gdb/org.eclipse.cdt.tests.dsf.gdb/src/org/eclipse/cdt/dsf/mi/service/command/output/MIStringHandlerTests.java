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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.text.ParseException;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

public class MIStringHandlerTests {
    @Test
    public void testTranscodeString() {
        // Test transcoding of an empty string.
        assertEquals(MIStringHandler.transcodeString(""), ""); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test transcoding of an ASCII string.
        assertEquals(MIStringHandler.transcodeString("ASCII"), "ASCII"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test transcoding of Latin-1 strings.
        assertEquals(MIStringHandler.transcodeString("\344"), "\344"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.transcodeString("abc\344"), "abc\344"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.transcodeString("\344abc"), "\344abc"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.transcodeString("abc\344def"), "abc\344def"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.transcodeString("abc\ndef\344ghi\tjkl"), "abc\ndef\344ghi\tjkl"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test transcoding of UTF-8 strings.
        assertEquals(MIStringHandler.transcodeString("\303\244"), "\344"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.transcodeString("abc\303\244"), "abc\344"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.transcodeString("\303\244abc"), "\344abc"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.transcodeString("abc\303\244def"), "abc\344def"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.transcodeString("abc\ndef\303\244ghi\tjkl"), "abc\ndef\344ghi\tjkl"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test transcoding of a string with unsupported encoding (here: UTF-16).
        assertEquals(MIStringHandler.transcodeString("\u3090"), "\u3090"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    @Test
    public void testEscapeString() {
        // Test escaping of an empty string.
        assertEquals(MIStringHandler.escapeString(""), ""); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test escaping of a plain text string.
        assertEquals(MIStringHandler.escapeString("abc"), "abc"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test escaping of strings which contain special chars.
        assertEquals(MIStringHandler.escapeString("\n"), "\\n"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.escapeString("\n\b"), "\\n\\b"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.escapeString("\0\1\2\3\4\5\6\7\10\11\12\13\14\15\16\17"), "\\0\\001\\002\\003\\004\\005\\006\\a\\b\\t\\n\\v\\f\\r\\016\\017"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.escapeString("\33"), "\\e"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.escapeString("\\", true), "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.escapeString("\\", false), "\\"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.escapeString("'\t\\", true), "\\'\\t\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.escapeString("'\t\\", false), "'\\t\\"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test escaping of strings which contain non-printable characters up to 0x7F.
        assertEquals(MIStringHandler.escapeString("\0"), "\\0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.escapeString("\177"), "\\177"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.escapeString("abc\177"), "abc\\177"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.escapeString("\177abc"), "\\177abc"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.escapeString("abc\177def"), "abc\\177def"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test escaping of strings which contain non-printable characters up to 0xFF.
        assertEquals(MIStringHandler.escapeString("\230"), "\\230"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.escapeString("abc\230"), "abc\\230"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.escapeString("\230abc"), "\\230abc"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.escapeString("abc\230def"), "abc\\230def"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test escaping of strings which contain non-printable characters up to 0xFFFF.
        assertEquals(MIStringHandler.escapeString("\ud800"), "\\ud800"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.escapeString("abc\ud800"), "abc\\ud800"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.escapeString("\ud800abc"), "\\ud800abc"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.escapeString("abc\ud800def"), "abc\\ud800def"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test escaping of strings which contain non-printable characters up to 0x10FFFF.
        assertEquals(MIStringHandler.escapeString("\udbff\udfff"), "\\U0010ffff"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.escapeString("abc\udbff\udfff"), "abc\\U0010ffff"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.escapeString("\udbff\udfffabc"), "\\U0010ffffabc"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.escapeString("abc\udbff\udfffdef"), "abc\\U0010ffffdef"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test escaping of a string which contains all sorts of non-printable characters.
        assertEquals(MIStringHandler.escapeString("abc\n\tdef\\ghi\njkl\177\230\n\ud800\udbff\udfff?mno\"", true), "abc\\n\\tdef\\\\ghi\\njkl\\177\\230\\n\\ud800\\U0010ffff\\?mno\\\""); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.escapeString("abc\n\tdef\\ghi\njkl\177\230\n\ud800\udbff\udfff?mno\"", false), "abc\\n\\tdef\\ghi\\njkl\\177\\230\\n\\ud800\\U0010ffff?mno\""); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test escaping of an ill-formed UTF-16 Java string.
        assertEquals(MIStringHandler.escapeString("abc\udbff"), "abc\\udbff"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    @Test
    public void testIsSpecialChar() {
        // Testing non-special chars.
        assertEquals(MIStringHandler.isSpecialChar('i'), false);
        assertEquals(MIStringHandler.isSpecialChar('w'), false);
        
        // Testing special chars.        
        assertEquals(MIStringHandler.isSpecialChar('a'),  true);
        assertEquals(MIStringHandler.isSpecialChar('b'),  true);
        assertEquals(MIStringHandler.isSpecialChar('e'),  true);
        assertEquals(MIStringHandler.isSpecialChar('E'),  true);
        assertEquals(MIStringHandler.isSpecialChar('f'),  true);
        assertEquals(MIStringHandler.isSpecialChar('n'),  true);
        assertEquals(MIStringHandler.isSpecialChar('r'),  true);
        assertEquals(MIStringHandler.isSpecialChar('t'),  true);
        assertEquals(MIStringHandler.isSpecialChar('v'),  true);
        assertEquals(MIStringHandler.isSpecialChar('\''), true);
        assertEquals(MIStringHandler.isSpecialChar('"'),  true);
        assertEquals(MIStringHandler.isSpecialChar('\\'), true);
        assertEquals(MIStringHandler.isSpecialChar('?'),  true);
    }
    
    @Test
    public void testIsSpecialCodePoint() {
        // Testing non-special Unicode code points.
        assertEquals(MIStringHandler.isSpecialCodePoint(0x69), false); // 'i' character
        assertEquals(MIStringHandler.isSpecialCodePoint(0x77), false); // 'w' character
        assertEquals(MIStringHandler.isSpecialCodePoint(0x61), false); // 'a' character
        assertEquals(MIStringHandler.isSpecialCodePoint(0x6E), false); // 'n' character
        
        // Testing special Unicode code points.
        assertEquals(MIStringHandler.isSpecialCodePoint(0x07), true);  // 'a' character
        assertEquals(MIStringHandler.isSpecialCodePoint(0x08), true);  // 'b' character
        assertEquals(MIStringHandler.isSpecialCodePoint(0x1B), true);  // 'e' character
        assertEquals(MIStringHandler.isSpecialCodePoint(0x1B), true);  // 'E' character
        assertEquals(MIStringHandler.isSpecialCodePoint(0x0C), true);  // 'f' character
        assertEquals(MIStringHandler.isSpecialCodePoint(0x0A), true);  // 'n' character
        assertEquals(MIStringHandler.isSpecialCodePoint(0x0D), true);  // 'r' character
        assertEquals(MIStringHandler.isSpecialCodePoint(0x09), true);  // 't' character
        assertEquals(MIStringHandler.isSpecialCodePoint(0x0B), true);  // 'v' character
        assertEquals(MIStringHandler.isSpecialCodePoint(0x27), true);  // '\'' character
        assertEquals(MIStringHandler.isSpecialCodePoint(0x22), true);  // '"' character
        assertEquals(MIStringHandler.isSpecialCodePoint(0x5C), true);  // '\\' character
        assertEquals(MIStringHandler.isSpecialCodePoint(0x3F), true);  // '?' character
   }
    
    @Test
    public void testParseSpecialChar() {
        // Testing non-special chars. This actually should throw ParseExceptions.
        try {
            MIStringHandler.parseSpecialChar('i');
            fail("No parsing exception thrown.");
        } catch (ParseException e) {
        }
        
        try {
            MIStringHandler.parseSpecialChar('w');
            fail("No parsing exception thrown.");
        } catch (ParseException e) {
        }
        
        try {
            // Testing special chars.
            assertEquals(MIStringHandler.parseSpecialChar('a'),  0x07);
            assertEquals(MIStringHandler.parseSpecialChar('b'),  0x08);
            assertEquals(MIStringHandler.parseSpecialChar('e'),  0x1B);
            assertEquals(MIStringHandler.parseSpecialChar('E'),  0x1B);
            assertEquals(MIStringHandler.parseSpecialChar('f'),  0x0C);
            assertEquals(MIStringHandler.parseSpecialChar('n'),  0x0A);
            assertEquals(MIStringHandler.parseSpecialChar('r'),  0x0D);
            assertEquals(MIStringHandler.parseSpecialChar('t'),  0x09);
            assertEquals(MIStringHandler.parseSpecialChar('v'),  0x0B);
            assertEquals(MIStringHandler.parseSpecialChar('\''), 0x27);
            assertEquals(MIStringHandler.parseSpecialChar('"'),  0x22);
            assertEquals(MIStringHandler.parseSpecialChar('\\'), 0x5C);
            assertEquals(MIStringHandler.parseSpecialChar('?'),  0x3F);

        } catch (ParseException e) {
            fail("Parsing exception thrown."); //$NON-NLS-1$
        }
    }
    
    @Test
    public void testParseSpecialCodePoint() {
        try {
            // Testing non-special Unicode code points. This actually should throw ParseExceptions.
            try {
                MIStringHandler.parseSpecialCodePoint(0x69);
                fail("No parsing exception thrown.");
            } catch (ParseException e) {
            }
            
            try {
                MIStringHandler.parseSpecialCodePoint(0x77);
                fail("No parsing exception thrown.");
            } catch (ParseException e) {
            }
            
            // Testing special Unicode code points.
            assertEquals(MIStringHandler.parseSpecialCodePoint(0x07), 'a');
            assertEquals(MIStringHandler.parseSpecialCodePoint(0x08), 'b');           
            assertEquals(MIStringHandler.parseSpecialCodePoint(0x1B), 'e');
            assertEquals(MIStringHandler.parseSpecialCodePoint(0x0C), 'f');
            assertEquals(MIStringHandler.parseSpecialCodePoint(0x0A), 'n');
            assertEquals(MIStringHandler.parseSpecialCodePoint(0x0D), 'r');
            assertEquals(MIStringHandler.parseSpecialCodePoint(0x09), 't');
            assertEquals(MIStringHandler.parseSpecialCodePoint(0x0B), 'v');
            assertEquals(MIStringHandler.parseSpecialCodePoint(0x27), '\'');
            assertEquals(MIStringHandler.parseSpecialCodePoint(0x22), '"');
            assertEquals(MIStringHandler.parseSpecialCodePoint(0x5C), '\\');
            assertEquals(MIStringHandler.parseSpecialCodePoint(0x3F), '?');

        } catch (ParseException e) {
            fail("Parsing exception thrown."); //$NON-NLS-1$
        }
    }
    
    @Test
    public void testParseString() {
        // Test parsing of an empty string.
        assertEquals(MIStringHandler.parseString(""), ""); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString(""), ""); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test parsing of a plain text string.
        assertEquals(MIStringHandler.parseString("abc"), "abc"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test parsing of strings which contain valid special char escape notations.
        assertEquals(MIStringHandler.parseString("\\n"), "\n"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\\\"), "\\"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\'\\t\\\\"), "'\t\\"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test parsing of strings which contain invalid special char escape notations.
        assertEquals(MIStringHandler.parseString("\\w"), "\\w"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\"), "\\"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("abc\\"), "abc\\"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\www"), "\\www"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("abc\\www"), "abc\\www"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\'\\z\\\\"), "'\\z\\"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\'\\z\\"), "'\\z\\"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test parsing of strings which contain valid octal escape notations.
        assertEquals(MIStringHandler.parseString("\\141\\142\\143"), "abc"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\141\\142\\143\\0"), "abc\0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\141\\142\\143\\12"), "abc\n"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\141\\\\142\\143\\\\"), "a\\142c\\"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\12"), "\n"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\012"), "\n"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("'\\011\\"), "'\t\\"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\177"), "\177"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\377"), "\377"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\3777"), "\3777"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("abc\\177"), "abc\177"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\177abc"), "\177abc"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("abc\\177def"), "abc\177def"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\0"), "\0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\1"), "\001"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\1a"), "\001a"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\18"), "\0018"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\01"), "\001"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\01a"), "\001a"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\001"), "\001"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\001a"), "\001a"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\0011"), "\0011"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\0011a"), "\0011a"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\1111"), "\1111"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test parsing of strings which contain invalid octal escape notations.
        assertEquals(MIStringHandler.parseString("\\400"), "\\400"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\4000"), "\\4000"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\8"), "\\8"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\90"), "\\90"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\ 0"), "\\ 0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\141\\142\\143\\"), "abc\\"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test parsing of strings which contain valid hexadecimal escape notations.
        assertEquals(MIStringHandler.parseString("\\x0"), "\0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\xa"), "\n"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\xa0"), "\240"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\xag"), "\ng"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\x0a"), "\n"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\x0a0"), "\240"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\x0ag"), "\ng"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\x00a"), "\n"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\x00a0"), "\240"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\x00ag"), "\ng"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\x000a"), "\n"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\x000a0"), "\n0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\x000ag"), "\ng"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\x0000a"), "\0a"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\x0000a0"), "\0a0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\x0000ag"), "\0ag"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("abc\\x20def"), "abc\u20def"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("abc\\x20def\\xa"), "abc\u20def\n"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("abc\\x20ghi"), "abc ghi"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("abc\\x20ghi\\xa"), "abc ghi\n"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test parsing of strings which contain invalid hexadecimal escape notations.
        assertEquals(MIStringHandler.parseString("\\x"), "\\x"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("abc\\x"), "abc\\x"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\xwww"), "\\xwww"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\x\\"), "\\x\\"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\x 0"), "\\x 0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\x\\0"), "\\x\0"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test parsing of strings which contain valid short Unicode escape notations.
        assertEquals(MIStringHandler.parseString("\\u0000"), "\0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\u000f"), "\017"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\u00ff"), "\377"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\u0fff"), "\u0fff"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\uffff"), "\uffff"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test parsing of strings which contain invalid short Unicode escape notations.
        assertEquals(MIStringHandler.parseString("\\u"), "\\u"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("abc\\u"), "abc\\u"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\uwww"), "\\uwww"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\u\\"), "\\u\\"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\u 0"), "\\u 0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\u\\0"), "\\u\0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\u0"), "\\u0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\u00"), "\\u00"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\u000"), "\\u000"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\u000g"), "\\u000g"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\ug"), "\\ug"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test parsing of strings which contain valid long Unicode escape notations.
        assertEquals(MIStringHandler.parseString("\\U00000000"), "\0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\U0000000f"), "\017"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\U000000ff"), "\377"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\U00000fff"), "\u0fff"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\U0000ffff"), "\uffff"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\U000fffff"), "\udbbf\udfff"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\U0010ffff"), "\udbff\udfff"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test parsing of strings which contain invalid long Unicode escape notations.
        assertEquals(MIStringHandler.parseString("\\U"), "\\U"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("abc\\U"), "abc\\U"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\Uwww"), "\\Uwww"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\U\\"), "\\U\\"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\U 0"), "\\U 0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\U\\0"), "\\U\0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\U0"), "\\U0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\U00"), "\\U00"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\U000"), "\\U000"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\U0000"), "\\U0000"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\U00000"), "\\U00000"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\U000000"), "\\U000000"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\U0000000"), "\\U0000000"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\U0000000g"), "\\U0000000g"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\U00110000"), "\\U00110000"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\U00f00000"), "\\U00f00000"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\U0f000000"), "\\U0f000000"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\Uf0000000"), "\\Uf0000000"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\Uffffffff"), "\\Uffffffff"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\Ufffffff"), "\\Ufffffff"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.parseString("\\Ug"), "\\Ug"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    @Test
    public void testTranslateCString() {
        // Test translating an empty string.
        assertEquals(MIStringHandler.translateCString("", false), ""); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("", true), ""); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test translating a plain text string.
        assertEquals(MIStringHandler.translateCString("abc", false), "abc"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("abc", true), "abc"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test translating strings which contain special chars and / or escaped special chars.
        assertEquals(MIStringHandler.translateCString("\t", true), "\\t"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("\t", false), "\t"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("\\t", true), "\\t"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("\\t", false), "\t"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("\t\\t", true), "\\t\\t"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("\t\\t", false), "\t\t"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("\\t\t", true), "\\t\\t"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("\\t\t", false), "\t\t"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("'\"", true), "'\""); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("'\"", false), "'\""); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("\\'\\\"", true), "\\'\\\""); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("\\'\\\"", false), "'\""); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("'\\\"", true), "'\\\""); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("'\\\"", false), "'\""); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("\\'\"", true), "\\'\""); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("\\'\"", false), "'\""); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test translating strings which contain hexadecimal escape notations.
        assertEquals(MIStringHandler.translateCString("\\x0000", true), "\\0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("\\x0000", false), "\0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("abc\\x31\\x32\\x33www", true), "abc123www"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("abc\\x0031\\x0032\\x0033www", true), "abc123www"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test translating strings which contain octal escape notations.
        assertEquals(MIStringHandler.translateCString("\\000", true), "\\0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("\\000", false), "\0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("\\0", true), "\\0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("\\0", false), "\0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("abc\\222def", true), "abc\\222def"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("abc\\222def", false), "abc\222def"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("abc\\303def", true), "abc\303def"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("abc\\303def", false), "abc\303def"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("abc\\303\\244def", true), "abc\344def"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("abc\"def", true), "abc\"def"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("abc\"def", false), "abc\"def"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("abc'def", true), "abc'def"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("abc'def", false), "abc'def"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("abc\\\"def", true), "abc\\\"def"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("abc\\\"def", false), "abc\"def"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("abc\\'def", true), "abc\\'def"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("abc\\'def", false), "abc'def"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test translating strings which contain short Unicode escape notations.
        assertEquals(MIStringHandler.translateCString("\\u0000", true), "\\0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("\\u0000", false), "\0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("abc\\u0031\\u0032\\u0033def", true), "abc123def"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Test translating strings which contain long Unicode escape notations.
        assertEquals(MIStringHandler.translateCString("\\U00000000", true), "\\0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("\\U00000000", false), "\0"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(MIStringHandler.translateCString("abc\\U00000031\\U00000032\\U00000033def", true), "abc123def"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(MIStringHandlerTests.class);
    }
}