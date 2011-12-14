/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.astwriter;

/**
 * This class is responsible for the string concatenation and the management of
 * the indentations.
 * 
 * @since 5.0
 * @author Emanuel Graf IFS
 */
public class Scribe {
	// Indentation is not necessary since the code is going to be formatted anyway.
	// Preserved because some tests depend on it.
	private static final int INDENTATION_SIZE = 4;
	private final String newLine = System.getProperty("line.separator"); //$NON-NLS-1$
	private StringBuilder buffer = new StringBuilder();
	private int indentationLevel = 0;
	private boolean isAtBeginningOfLine = true;

	private boolean skipLineBreaks;
	private boolean skipSemicolons;

	public String getLineSeparator() {
		return newLine;
	}

	public void newLine() {
		if (!skipLineBreaks) {
			isAtBeginningOfLine = true;
			buffer.append(newLine);
		}
	}

	public boolean isAtBeginningOfLine() {
		return isAtBeginningOfLine;
	}

	private void indent() {
		printSpaces(indentationLevel * INDENTATION_SIZE);
	}

	private void indentIfNewLine() {
		if (isAtBeginningOfLine) {
			isAtBeginningOfLine = false;
			indent();
		}
	}

	public void print(String code) {
		indentIfNewLine();
		buffer.append(code);
	}

	public void println(String code) {
		print(code);
		newLine();
	}

	public void print(String code, String code2) {
		print(code);
		buffer.append(code2);
	}

	public void println(String code, String code2) {
		print(code, code2);
		newLine();
	}

	public void println(String code, char[] code2) {
		print(code);
		buffer.append(code2);
		newLine();
	}

	public void printSpaces(int number) {
		indentIfNewLine();
		for (int i = 0; i < number; ++i) {
			printSpace();
		}
	}

	public void noSemicolon() {
		skipSemicolons = true;
	}

	public void printSemicolon() {
		if (!skipSemicolons) {
			indentIfNewLine();
			buffer.append(';');
		} else {
			skipSemicolons = false;
		}
	}

	@Override
	public String toString() {
		return buffer.toString();
	}

	public void print(char code) {
		indentIfNewLine();
		buffer.append(code);
	}

	public void print(char[] code) {
		indentIfNewLine();
		buffer.append(code);
	}

	public void println(char[] code) {
		print(code);
		newLine();
	}

	public void printStringSpace(String code) {
		print(code);
		printSpace();
	}

	/**
	 * Prints a { to the buffer an increases the indentation level.
	 */
	public void printLBrace() {
		print('{');
		++indentationLevel;
	}

	/**
	 * Prints a } to the buffer an decrease the indentation level.
	 */
	public void printRBrace() {
		--indentationLevel;
		print('}');
	}

	public void incrementIndentationLevel() {
		++indentationLevel;
	}

	public void decrementIndentationLevel() {
		if (indentationLevel > 0) {
			--indentationLevel;
		}
	}

	protected void noNewLines() {
		skipLineBreaks = true;
	}

	protected void newLines() {
		skipLineBreaks = false;
	}

	public void newLine(int i) {
		while (i > 0) {
			newLine();
			--i;
		}
	}

	public void printSpace() {
		buffer.append(' ');
	}

	public void cleanCache() {
		buffer = new StringBuilder();
	}
}
