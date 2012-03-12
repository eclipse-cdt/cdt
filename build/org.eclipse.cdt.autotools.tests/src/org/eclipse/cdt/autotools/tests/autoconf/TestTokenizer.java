/*******************************************************************************
 * Copyright (c) 2008 Nokia Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ed Swartz (Nokia) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.autotools.tests.autoconf;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfTokenizer;
import org.eclipse.cdt.autotools.ui.editors.parser.IAutoconfErrorHandler;
import org.eclipse.cdt.autotools.ui.editors.parser.ITokenConstants;
import org.eclipse.cdt.autotools.ui.editors.parser.ParseException;
import org.eclipse.cdt.autotools.ui.editors.parser.Token;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;


/**
 * Test autoconf tokenizer.  The tokenizer mainly is used to detect boundaries and keywords
 * and is not a full shell tokenizer.
 * @author eswartz
 *
 */
public class TestTokenizer extends TestCase {

	private ArrayList tokenizerErrors;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
	}

	protected IDocument createDocument(String text) {
		return new Document(text);
	}

	protected List/*<Token>*/tokenize(IDocument document) {
		return tokenize(document, false);
	}

	protected List/*<Token>*/tokenize(IDocument document, boolean isM4Mode) {
		tokenizerErrors = new ArrayList();
		AutoconfTokenizer tokenizer = new AutoconfTokenizer(document, new IAutoconfErrorHandler() {

			public void handleError(ParseException exception) {
				tokenizerErrors.add(exception);
			}
			
		});
		tokenizer.setM4Context(isM4Mode);
		
		List/*<Token>*/ tokens = tokenize(tokenizer);
		return tokens;
	}

	protected List/*<Token>*/tokenize(AutoconfTokenizer tokenizer) {
		List/*<Token>*/tokens = new ArrayList();
		while (true) {
			Token token = tokenizer.readToken();
			if (token.getType() == ITokenConstants.EOF)
				break;
			tokens.add(token);
		}
		return tokens;
	}

	protected void checkNoErrors() {
		assertEquals(0, tokenizerErrors.size());
	}
	public void testEmpty() {
		IDocument document = createDocument("");
		List tokens = tokenize(document, false);
		checkNoErrors();
		assertEquals(0, tokens.size());
	}

	public void testEOL1() {
		IDocument document = createDocument("\n");
		List tokens = tokenize(document, false);
		checkNoErrors();
		assertEquals(1, tokens.size());
		checkToken((Token) tokens.get(0), document, ITokenConstants.EOL, "\n");
	}

	public void testEOL2() {
		IDocument document = createDocument("\r\n");
		List tokens = tokenize(document, false);
		checkNoErrors();
		assertEquals(1, tokens.size());
		checkToken((Token) tokens.get(0), document, ITokenConstants.EOL, "\r\n");
	}

	public void testEOL3() {
		IDocument document = createDocument("\n\r\n\n");
		List tokens = tokenize(document, false);
		checkNoErrors();
		assertEquals(3, tokens.size());
		checkToken((Token) tokens.get(0), document, ITokenConstants.EOL, "\n");
		checkToken((Token) tokens.get(1), document, ITokenConstants.EOL, "\r\n");
		checkToken((Token) tokens.get(2), document, ITokenConstants.EOL, "\n");
	}

	public void testShellText() {
		// default mode is shell
		String text = "random\nstuff\n";
		IDocument document = createDocument(text);
		List tokens = tokenize(document, false);
		checkNoErrors();
		assertEquals(4, tokens.size());
		checkToken((Token) tokens.get(0), document, ITokenConstants.WORD,
				"random");
		checkToken((Token) tokens.get(1), document, ITokenConstants.EOL, "\n");
		checkToken((Token) tokens.get(2), document, ITokenConstants.WORD,
				"stuff");
		checkToken((Token) tokens.get(3), document, ITokenConstants.EOL, "\n");
	}

	public void testShellTokens() {
		// default mode is shell
		String text = "while true; do ls; done\n";
		IDocument document = createDocument(text);
		List tokens = tokenize(document, false);
		checkNoErrors();
		assertEquals(8, tokens.size());
		checkToken((Token) tokens.get(0), document, ITokenConstants.SH_WHILE,
				"while");
		checkToken((Token) tokens.get(1), document, ITokenConstants.WORD,
				"true");
		checkToken((Token) tokens.get(2), document, ITokenConstants.SEMI, ";");
		checkToken((Token) tokens.get(3), document, ITokenConstants.SH_DO, "do");
		checkToken((Token) tokens.get(4), document, ITokenConstants.WORD, "ls");
		checkToken((Token) tokens.get(5), document, ITokenConstants.SEMI, ";");
		checkToken((Token) tokens.get(6), document, ITokenConstants.SH_DONE,
				"done");
		checkToken((Token) tokens.get(7), document, ITokenConstants.EOL, "\n");
	}

	public void testShellTokens2() {
		// don't misread partial tokens
		String text = "while_stuff incase";
		IDocument document = createDocument(text);
		List tokens = tokenize(document, false);
		checkNoErrors();
		assertEquals(2, tokens.size());
		checkToken((Token) tokens.get(0), document, ITokenConstants.WORD,
				"while_stuff");
		checkToken((Token) tokens.get(1), document, ITokenConstants.WORD,
				"incase");
	}

	public void testShellTokens3() {
		// don't interpret m4 strings in shell mode
		String text = "`foo'";
		IDocument document = createDocument(text);
		List tokens = tokenize(document, false);
		assertEquals(1, tokenizerErrors.size());
		assertEquals(1, tokens.size());
		checkToken((Token) tokens.get(0), document, ITokenConstants.SH_STRING_BACKTICK, "foo'", 5);
	}

	public void testShellTokens4() {
		String text = "echo $if $((foo)) $\n";
		IDocument document = createDocument(text);
		List tokens = tokenize(document, false);
		checkNoErrors();
		assertEquals(11, tokens.size());
		checkToken((Token) tokens.get(0), document, ITokenConstants.WORD, "echo");
		checkToken((Token) tokens.get(1), document, ITokenConstants.SH_DOLLAR, "$");
		// dollar guards keywords, but the tokenizer doesn't know this
		checkToken((Token) tokens.get(2), document, ITokenConstants.SH_IF, "if");
		checkToken((Token) tokens.get(3), document, ITokenConstants.SH_DOLLAR, "$");
		checkToken((Token) tokens.get(4), document, ITokenConstants.LPAREN, "(");
		checkToken((Token) tokens.get(5), document, ITokenConstants.LPAREN, "(");
		checkToken((Token) tokens.get(6), document, ITokenConstants.WORD, "foo");
		checkToken((Token) tokens.get(7), document, ITokenConstants.RPAREN, ")");
		checkToken((Token) tokens.get(8), document, ITokenConstants.RPAREN, ")");
		checkToken((Token) tokens.get(9), document, ITokenConstants.SH_DOLLAR, "$");
		checkToken((Token) tokens.get(10), document, ITokenConstants.EOL, "\n");

	}

	public void testShellTokens5() {
		String text = "while do select for until done\n";
		IDocument document = createDocument(text);
		List tokens = tokenize(document, false);
		checkNoErrors();
		assertEquals(7, tokens.size());
		checkToken((Token) tokens.get(0), document, ITokenConstants.SH_WHILE, "while");
		checkToken((Token) tokens.get(1), document, ITokenConstants.SH_DO, "do");
		checkToken((Token) tokens.get(2), document, ITokenConstants.SH_SELECT, "select");
		checkToken((Token) tokens.get(3), document, ITokenConstants.SH_FOR, "for");
		checkToken((Token) tokens.get(4), document, ITokenConstants.SH_UNTIL, "until");
		checkToken((Token) tokens.get(5), document, ITokenConstants.SH_DONE, "done");
		checkToken((Token) tokens.get(6), document, ITokenConstants.EOL, "\n");

	}

	public void testShellComments() {
		// comments are stripped and ignored in the shell mode
		String text = "for # while case\n";
		IDocument document = createDocument(text);
		List tokens = tokenize(document, false);
		checkNoErrors();
		assertEquals(2, tokens.size());
		checkToken((Token) tokens.get(0), document, ITokenConstants.SH_FOR,
				"for");
		checkToken((Token) tokens.get(1), document, ITokenConstants.EOL, "\n");
	}

	public void testShellComments2() {
		// comments are stripped and ignored in the shell mode
		String text = "# while case\n" + "#for x in 3\n" + "\n";
		IDocument document = createDocument(text);
		List tokens = tokenize(document, false);
		checkNoErrors();
		assertEquals(3, tokens.size());
		checkToken((Token) tokens.get(0), document, ITokenConstants.EOL, "\n");
		checkToken((Token) tokens.get(1), document, ITokenConstants.EOL, "\n");
		checkToken((Token) tokens.get(2), document, ITokenConstants.EOL, "\n");
	}

	public void testM4Tokens0() {
		String text = "while_stuff incase";
		IDocument document = createDocument(text);
		List tokens = tokenize(document, true);
		checkNoErrors();
		assertEquals(2, tokens.size());
		checkToken((Token) tokens.get(0), document, ITokenConstants.WORD,
				"while_stuff");
		checkToken((Token) tokens.get(1), document, ITokenConstants.WORD,
				"incase");
	}
	
	public void testShellStrings() {
		String QUOTED = 
			"ls -la \"*.c\"";
		String text = "echo `" + QUOTED + "`\n";
		IDocument document = createDocument(text);
		List tokens = tokenize(document, false);
		checkNoErrors();
		assertEquals(3, tokens.size());
		checkToken((Token) tokens.get(0), document, ITokenConstants.WORD,
				"echo");
		checkToken((Token) tokens.get(1), document, ITokenConstants.SH_STRING_BACKTICK,
				QUOTED, QUOTED.length() + 2);
		checkToken((Token) tokens.get(2), document, ITokenConstants.EOL, "\n");
	}
	public void testShellStrings2() {
		String QUOTED = 
			"ls -la 'space file'";
		String text = "echo \"" + QUOTED + "\"\n";
		IDocument document = createDocument(text);
		List tokens = tokenize(document, false);
		checkNoErrors();
		assertEquals(3, tokens.size());
		checkToken((Token) tokens.get(0), document, ITokenConstants.WORD,
				"echo");
		checkToken((Token) tokens.get(1), document, ITokenConstants.SH_STRING_DOUBLE,
				QUOTED, QUOTED.length() + 2);
		checkToken((Token) tokens.get(2), document, ITokenConstants.EOL, "\n");
	}
	public void testShellStrings3() {
		String QUOTED = 
			"echo \"*.c\" | sed s/[a-z]/[A-Z]/g";
		String text = "echo '" + QUOTED + "'\n";
		IDocument document = createDocument(text);
		List tokens = tokenize(document, false);
		checkNoErrors();
		assertEquals(3, tokens.size());
		checkToken((Token) tokens.get(0), document, ITokenConstants.WORD,
				"echo");
		checkToken((Token) tokens.get(1), document, ITokenConstants.SH_STRING_SINGLE,
				QUOTED, QUOTED.length() + 2);
		checkToken((Token) tokens.get(2), document, ITokenConstants.EOL, "\n");
	}
	public void testM4Tokens1() {
		String text = "define(`hi\', `HI\')\n";
		IDocument document = createDocument(text);
		List tokens = tokenize(document, true);
		checkNoErrors();
		assertEquals(7, tokens.size());
		checkToken((Token) tokens.get(0), document, ITokenConstants.WORD,
				"define");
		checkToken((Token) tokens.get(1), document, ITokenConstants.LPAREN, "(");
		// strings are unquoted in token text
		checkToken((Token) tokens.get(2), document, ITokenConstants.M4_STRING,
				"hi", 4);
		checkToken((Token) tokens.get(3), document, ITokenConstants.COMMA, ",");
		checkToken((Token) tokens.get(4), document, ITokenConstants.M4_STRING,
				"HI", 4);
		checkToken((Token) tokens.get(5), document, ITokenConstants.RPAREN, ")");
		checkToken((Token) tokens.get(6), document, ITokenConstants.EOL, "\n");
	}

	public void testM4Comments() {
		String text = "dnl # comment\n";
		IDocument document = createDocument(text);
		List tokens = tokenize(document, true);
		checkNoErrors();
		assertEquals(2, tokens.size());
		checkToken((Token) tokens.get(0), document, ITokenConstants.WORD, "dnl");
		checkToken((Token) tokens.get(1), document, ITokenConstants.M4_COMMENT,
				"# comment\n");
	}

	public void testM4Comments2() {
		String text = "dnl /* word(`quoted')\n" + "*/\n";
		IDocument document = createDocument(text);
		AutoconfTokenizer tokenizer = createTokenizer(document);
		tokenizer.setM4Context(true);
		tokenizer.setM4Comment("/*", "*/");

		List tokens = tokenize(tokenizer);
		assertEquals(3, tokens.size());
		checkToken((Token) tokens.get(0), document, ITokenConstants.WORD, "dnl");
		checkToken((Token) tokens.get(1), document, ITokenConstants.M4_COMMENT,
				"/* word(`quoted')\n*/");
		checkToken((Token) tokens.get(2), document, ITokenConstants.EOL, "\n");
	}

	public void testM4Strings1() {
		// double quotes only removes one level of quotes
		String text = "``double''\n";
		IDocument document = createDocument(text);
		AutoconfTokenizer tokenizer = createTokenizer(document);
		tokenizer.setM4Context(true);

		List tokens = tokenize(tokenizer);
		assertEquals(2, tokens.size());
		checkToken((Token) tokens.get(0), document, ITokenConstants.M4_STRING,
				"`double'", 8 + 1 + 1);
		checkToken((Token) tokens.get(1), document, ITokenConstants.EOL, "\n");
	}

	public void testM4Strings2() {
		String text = "myword(!!boundary==)\n";
		IDocument document = createDocument(text);
		AutoconfTokenizer tokenizer = createTokenizer(document);
		tokenizer.setM4Context(true);
		tokenizer.setM4Quote("!!", "==");

		List tokens = tokenize(tokenizer);
		assertEquals(5, tokens.size());
		checkToken((Token) tokens.get(0), document, ITokenConstants.WORD,
				"myword");
		checkToken((Token) tokens.get(1), document, ITokenConstants.LPAREN, "(");
		checkToken((Token) tokens.get(2), document, ITokenConstants.M4_STRING,
				"boundary", 8 + 2 + 2);
		checkToken((Token) tokens.get(3), document, ITokenConstants.RPAREN, ")");
		checkToken((Token) tokens.get(4), document, ITokenConstants.EOL, "\n");
	}

	public void testM4Tokens2() {
		// dollar is not seen in m4 mode (only important when expanding)
		String text = "define(foo,$1)\n";
		IDocument document = createDocument(text);
		AutoconfTokenizer tokenizer = createTokenizer(document);
		tokenizer.setM4Context(true);

		List tokens = tokenize(tokenizer);
		assertEquals(8, tokens.size());
		checkToken((Token) tokens.get(0), document, ITokenConstants.WORD,
				"define");
		checkToken((Token) tokens.get(1), document, ITokenConstants.LPAREN, "(");
		checkToken((Token) tokens.get(2), document, ITokenConstants.WORD, "foo");
		checkToken((Token) tokens.get(3), document, ITokenConstants.COMMA, ",");
		checkToken((Token) tokens.get(4), document, ITokenConstants.TEXT, "$");
		checkToken((Token) tokens.get(5), document, ITokenConstants.TEXT, "1");
		checkToken((Token) tokens.get(6), document, ITokenConstants.RPAREN, ")");
		checkToken((Token) tokens.get(7), document, ITokenConstants.EOL, "\n");
	}
	
	public void testM4QuoteNesting() {
		String quote = "this is `nested\'!";
		String text = "`" + quote +"'\n";
		IDocument document = createDocument(text);
		AutoconfTokenizer tokenizer = createTokenizer(document);
		tokenizer.setM4Context(true);

		List tokens = tokenize(tokenizer);
		assertEquals(2, tokens.size());
		checkToken((Token) tokens.get(0), document, ITokenConstants.M4_STRING,
				quote, quote.length() + 2);
		checkToken((Token) tokens.get(1), document, ITokenConstants.EOL, "\n");
		
	}

	public void testMixedContext() {
		String text = "AM_INIT([arg])if true\n";
		IDocument document = createDocument(text);
		AutoconfTokenizer tokenizer = createTokenizer(document);
		tokenizer.setM4Context(false);
		tokenizer.setM4Quote("[", "]");

		Token token;
		token = tokenizer.readToken();
		checkToken(token, document, ITokenConstants.WORD, "AM_INIT");

		// "hey, that's a macro"
		tokenizer.setM4Context(true);

		token = tokenizer.readToken();
		checkToken(token, document, ITokenConstants.LPAREN, "(");
		token = tokenizer.readToken();
		checkToken(token, document, ITokenConstants.M4_STRING, "arg", 5);
		token = tokenizer.readToken();
		checkToken(token, document, ITokenConstants.RPAREN, ")");

		// "check it's not a dangling paren"
		// it'll still be an m4 word
		token = tokenizer.readToken();
		checkToken(token, document, ITokenConstants.WORD, "if");

		// push back token
		tokenizer.unreadToken(token);

		// "done reading macro"
		tokenizer.setM4Context(false);

		// "get shell stuff"
		token = tokenizer.readToken();
		checkToken(token, document, ITokenConstants.SH_IF, "if");
		token = tokenizer.readToken();
		checkToken(token, document, ITokenConstants.WORD, "true");

		token = tokenizer.readToken();
		checkToken(token, document, ITokenConstants.EOL, "\n");

		checkToken(tokenizer.readToken(), document, ITokenConstants.EOF);

	}

	private AutoconfTokenizer createTokenizer(IDocument document) {
		return new AutoconfTokenizer(document, new IAutoconfErrorHandler() {

			public void handleError(ParseException exception) {
				fail(exception.toString());
			}
			
		});
	}
	
	private void checkToken(Token token, IDocument document, int type) {
		assertEquals(type, token.getType());
		assertSame(document, token.getDocument());
		assertTrue(token.getOffset() >= 0);
		assertTrue(token.getType() == ITokenConstants.EOF || token.getLength() > 0);
		assertEquals(document.get().substring(token.getOffset(),
				token.getOffset() + token.getLength()), token.getText());
	}

	private void checkToken(Token token, IDocument document, int type,
			String text) {
		assertEquals(type, token.getType());
		assertSame(document, token.getDocument());
		assertTrue(token.getOffset() >= 0);
		assertEquals(text, token.getText());
		assertEquals(text.length(), token.getLength());
	}

	private void checkToken(Token token, IDocument document, int type,
			String text, int length) {
		assertEquals(type, token.getType());
		assertSame(document, token.getDocument());
		assertTrue(token.getOffset() >= 0);
		assertEquals(text, token.getText());
		assertEquals(length, token.getLength());
	}
}
