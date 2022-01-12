/*******************************************************************************
 * Copyright (c) 2012 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors.parser;

public interface ITokenConstants {

	/** end of file */
	int EOF = 0;
	/** end of line */
	int EOL = 1;

	/** an open parenthesis */
	int LPAREN = 2;
	/** a close parenthesis */
	int RPAREN = 3;
	/** a comma */
	int COMMA = 4;
	/** a semicolon */
	int SEMI = 5;

	/** a word (either m4 word or shell identifier-looking word) */
	int WORD = 6;

	/** other text (usually punctuation or number, one char at a time) */
	int TEXT = 7;

	/** an m4 string (the text does not contain the outermost quotes) */
	int M4_STRING = 21;
	/** an m4 comment (as determined by changecomment, NOT dnl) */
	int M4_COMMENT = 22;

	/** the sh 'if' token */
	int SH_IF = 40;
	/** the sh 'then' token */
	int SH_THEN = 41;
	/** the sh 'else' token */
	int SH_ELSE = 42;
	/** the sh 'elif' token */
	int SH_ELIF = 43;
	/** the sh 'fi' token */
	int SH_FI = 44;

	/** the sh 'while' token */
	int SH_WHILE = 45;
	/** the sh 'for' token */
	int SH_FOR = 46;
	/** the sh 'select' token */
	int SH_SELECT = 47;
	/** the sh 'until' token */
	int SH_UNTIL = 48;
	/** the sh 'do' token */
	int SH_DO = 49;
	/** the sh 'done' token */
	int SH_DONE = 50;
	/** the sh 'case' token */

	int SH_CASE = 51;
	/** the sh 'in' token */
	int SH_IN = 52;
	/** the sh ';;' token */
	int SH_CASE_CONDITION_END = 53;
	/** the sh 'esac' token */
	int SH_ESAC = 54;

	/** the sh '$' token */
	int SH_DOLLAR = 60;

	/** the sh '{' token */
	int SH_LBRACE = 61;
	/** the sh '}' token */
	int SH_RBRACE = 62;
	/** the sh '[' token */
	int SH_LBRACKET = 63;
	/** the sh ']' token */
	int SH_RBRACKET = 64;

	/** the sh '<<' token */
	int SH_HERE = 65;
	/** the sh '<<-' token */
	int SH_HERE_DASH = 66;
	/** an sh double-quoted string */
	int SH_STRING_DOUBLE = 67;
	/** an sh single-quoted string */
	int SH_STRING_SINGLE = 68;
	/** an sh backtick-quoted string */
	int SH_STRING_BACKTICK = 69;

}
