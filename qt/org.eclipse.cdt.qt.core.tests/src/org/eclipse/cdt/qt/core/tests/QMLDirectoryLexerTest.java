/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.qt.core.tests;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.cdt.internal.qt.core.location.Position;
import org.eclipse.cdt.qt.core.qmldir.QMLDirectoryLexer;
import org.eclipse.cdt.qt.core.qmldir.QMLDirectoryLexer.Token;
import org.eclipse.cdt.qt.core.qmldir.QMLDirectoryLexer.TokenType;
import org.junit.Test;

@SuppressWarnings("nls")
public class QMLDirectoryLexerTests {
	private void assertToken(TokenType type, String text, int start, int end, Position locStart, Position locEnd,
			Token actual) {
		// Check token type and text
		assertEquals("Unexpected token type", type, actual.getType());
		assertEquals("Unexpected token text", text, actual.getText());

		// Check position offsets
		assertEquals("Unexpected start position", start, actual.getStart());
		assertEquals("Unexpected end position", end, actual.getEnd());

		// Check SourceLocation start
		assertEquals("Unexpected location start line", locStart.getLine(), actual.getLocation().getStart().getLine());
		assertEquals("Unexpected location start column", locStart.getColumn(),
				actual.getLocation().getStart().getColumn());

		// Check SourceLocation end
		assertEquals("Unexpected location end line", locEnd.getLine(), actual.getLocation().getEnd().getLine());
		assertEquals("Unexpected location end column", locEnd.getColumn(), actual.getLocation().getEnd().getColumn());
	}

	private InputStream createInputStream(String s) {
		return new ByteArrayInputStream(s.getBytes());
	}

	@Test
	public void testCommentToken() {
		QMLDirectoryLexer lexer = new QMLDirectoryLexer();
		lexer.setInput(new ByteArrayInputStream("# This is a comment".getBytes()));
		assertToken(TokenType.COMMENT, "# This is a comment", 0, 19, new Position(1, 0), new Position(1, 19),
				lexer.nextToken(false));
	}

	@Test
	public void testMultipleCommentTokens() {
		QMLDirectoryLexer lexer = new QMLDirectoryLexer();
		lexer.setInput(createInputStream("# This is a comment\n# This is another comment\n"));
		assertToken(TokenType.COMMENT, "# This is a comment", 0, 19, new Position(1, 0), new Position(1, 19),
				lexer.nextToken(false));
		assertEquals(TokenType.COMMAND_END, lexer.nextToken(false).getType());
		assertToken(TokenType.COMMENT, "# This is another comment", 20, 45, new Position(2, 0), new Position(2, 25),
				lexer.nextToken(false));
		assertEquals(TokenType.COMMAND_END, lexer.nextToken(false).getType());
		assertEquals(TokenType.EOF, lexer.nextToken(false).getType());
	}

	@Test
	public void testModuleToken() {
		QMLDirectoryLexer lexer = new QMLDirectoryLexer();
		lexer.setInput(createInputStream("module"));
		assertToken(TokenType.MODULE, "module", 0, 6, new Position(1, 0), new Position(1, 6), lexer.nextToken());
	}

	@Test
	public void testTypeInfoToken() {
		QMLDirectoryLexer lexer = new QMLDirectoryLexer();
		lexer.setInput(createInputStream("typeinfo"));
		assertToken(TokenType.TYPEINFO, "typeinfo", 0, 8, new Position(1, 0), new Position(1, 8), lexer.nextToken());
	}

	@Test
	public void testSingletonToken() {
		QMLDirectoryLexer lexer = new QMLDirectoryLexer();
		lexer.setInput(createInputStream("singleton"));
		assertToken(TokenType.SINGLETON, "singleton", 0, 9, new Position(1, 0), new Position(1, 9), lexer.nextToken());
	}

	@Test
	public void testInternalToken() {
		QMLDirectoryLexer lexer = new QMLDirectoryLexer();
		lexer.setInput(createInputStream("internal"));
		assertToken(TokenType.INTERNAL, "internal", 0, 8, new Position(1, 0), new Position(1, 8), lexer.nextToken());
	}

	@Test
	public void testPluginToken() {
		QMLDirectoryLexer lexer = new QMLDirectoryLexer();
		lexer.setInput(createInputStream("plugin"));
		assertToken(TokenType.PLUGIN, "plugin", 0, 6, new Position(1, 0), new Position(1, 6), lexer.nextToken());
	}

	@Test
	public void testClassnameToken() {
		QMLDirectoryLexer lexer = new QMLDirectoryLexer();
		lexer.setInput(createInputStream("classname"));
		assertToken(TokenType.CLASSNAME, "classname", 0, 9, new Position(1, 0), new Position(1, 9), lexer.nextToken());
	}

	@Test
	public void testDependsToken() {
		QMLDirectoryLexer lexer = new QMLDirectoryLexer();
		lexer.setInput(createInputStream("depends"));
		assertToken(TokenType.DEPENDS, "depends", 0, 7, new Position(1, 0), new Position(1, 7), lexer.nextToken());
	}

	@Test
	public void testDesignerSupportedToken() {
		QMLDirectoryLexer lexer = new QMLDirectoryLexer();
		lexer.setInput(createInputStream("designersupported"));
		assertToken(TokenType.DESIGNERSUPPORTED, "designersupported", 0, 17, new Position(1, 0), new Position(1, 17),
				lexer.nextToken());
	}

	@Test
	public void testWordToken() {
		QMLDirectoryLexer lexer = new QMLDirectoryLexer();
		lexer.setInput(createInputStream("QtQuick.Control"));
		assertToken(TokenType.WORD, "QtQuick.Control", 0, 15, new Position(1, 0), new Position(1, 15),
				lexer.nextToken());
	}

	@Test
	public void testWordTokenContainsKeyword() {
		QMLDirectoryLexer lexer = new QMLDirectoryLexer();
		lexer.setInput(createInputStream("plugins.test"));
		assertToken(TokenType.WORD, "plugins.test", 0, 12, new Position(1, 0), new Position(1, 12), lexer.nextToken());
	}

	@Test
	public void testWordTokenAsRelativePath() {
		QMLDirectoryLexer lexer = new QMLDirectoryLexer();
		lexer.setInput(createInputStream("./test/something/"));
		assertToken(TokenType.WORD, "./test/something/", 0, 17, new Position(1, 0), new Position(1, 17),
				lexer.nextToken());
	}

	@Test
	public void testWordTokenAsAbsoluteWindowsPath() {
		QMLDirectoryLexer lexer = new QMLDirectoryLexer();
		lexer.setInput(createInputStream("C:\\Users\\someone\\test\\something\\"));
		assertToken(TokenType.WORD, "C:\\Users\\someone\\test\\something\\", 0, 32, new Position(1, 0),
				new Position(1, 32), lexer.nextToken());
	}

	@Test
	public void testWordTokenAsAbsoluteUnixPath() {
		QMLDirectoryLexer lexer = new QMLDirectoryLexer();
		lexer.setInput(createInputStream("/usr/local/test/something/"));
		assertToken(TokenType.WORD, "/usr/local/test/something/", 0, 26, new Position(1, 0), new Position(1, 26),
				lexer.nextToken());
	}

	@Test
	public void testDecimalToken() {
		QMLDirectoryLexer lexer = new QMLDirectoryLexer();
		lexer.setInput(createInputStream("2.3"));
		assertToken(TokenType.DECIMAL, "2.3", 0, 3, new Position(1, 0), new Position(1, 3), lexer.nextToken());
	}

	@Test
	public void testIntegerToken() {
		QMLDirectoryLexer lexer = new QMLDirectoryLexer();
		lexer.setInput(createInputStream("3"));
		assertToken(TokenType.INTEGER, "3", 0, 1, new Position(1, 0), new Position(1, 1), lexer.nextToken());
	}

	@Test
	public void testWhitespaceToken() {
		QMLDirectoryLexer lexer = new QMLDirectoryLexer();
		lexer.setInput(createInputStream(" \t\n"));
		assertToken(TokenType.WHITESPACE, " \t", 0, 2, new Position(1, 0), new Position(1, 2), lexer.nextToken(false));
	}

	@Test
	public void testCommandEndToken() {
		QMLDirectoryLexer lexer = new QMLDirectoryLexer();
		lexer.setInput(createInputStream("\n"));
		assertToken(TokenType.COMMAND_END, "\\n", 0, 1, new Position(1, 0), new Position(1, 1), lexer.nextToken());
	}

	@Test
	public void testEOFToken() {
		QMLDirectoryLexer lexer = new QMLDirectoryLexer();
		lexer.setInput(createInputStream(""));
		assertToken(TokenType.EOF, "", 0, 0, new Position(1, 0), new Position(1, 0), lexer.nextToken());
	}

	@Test
	public void testEOFTokenAfterCommand() {
		QMLDirectoryLexer lexer = new QMLDirectoryLexer();
		lexer.setInput(createInputStream("\n"));
		lexer.nextToken();
		assertToken(TokenType.EOF, "", 1, 1, new Position(2, 0), new Position(2, 0), lexer.nextToken());
	}
}
