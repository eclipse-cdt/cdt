/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 *******************************************************************************/
package org.eclipse.rse.internal.tests.framework.scripting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;

/**
 * A Parser is an object capable of taking a reader and producing a script from it.
 */
public class ScriptParser {
	private ScriptParserMessageList messageList;
	private int lineNumber = 0;
	private String statement = null;
	private int statementLineNumber = 0;
	private BufferedReader in = null;
	private Stack nodes = null;

	/**
	 * Creates a new parser on an existing MessageList.  Any messages found
	 * during parsing are added to this list.
	 * @param messageList
	 */
	public ScriptParser(ScriptParserMessageList messageList) {
		this.messageList = messageList;
	}

	/**
	 * @param inStream The InputStream holding the input to the parser.  The stream is left
	 * open after parsing is completed.  It is up to the client to close the stream
	 * if necessary.
	 * @return the Script produced by the parser or null if none was produced.
	 * @throws IOException if an IO error occurs while reading the stream
	 */
	public Script parse(InputStream inStream) throws IOException {
		lineNumber = 0;
		nodes = new Stack();
		nodes.push(new Script());
		in = new BufferedReader(new InputStreamReader(inStream));
		for (getStatement(); statement != null; getStatement()) {
			parseStatement();
		}
		Script result = null;
		SyntaxNode top = getCurrentNode();
		if (top != null) {
			if (top instanceof Script) {
				result = (Script) top;
			} else {
				messageList.add(new ScriptParserMessage(lineNumber, ScriptParserMessage.ERROR, "Incomplete statement")); //$NON-NLS-1$
			}
		} else {
			messageList.add(new ScriptParserMessage(lineNumber, ScriptParserMessage.ERROR, "Internal error")); //$NON-NLS-1$
		}
		return result;
	}

	private void parseStatement() {
		if (statement.startsWith("tell")) //$NON-NLS-1$
			parseTell();
		else if (statement.startsWith("show")) //$NON-NLS-1$
			parseShow();
		else if (statement.startsWith("pause")) //$NON-NLS-1$
			parsePause();
		else
			messageList.add(new ScriptParserMessage(lineNumber, ScriptParserMessage.ERROR, "Unrecognized statement")); //$NON-NLS-1$
	}

	private void parseTell() {
		String remark = statement.substring(4);
		remark = remark.trim();
		SyntaxNode tell = new ScriptTell(remark, statementLineNumber);
		getCurrentNode().add(tell);
	}

	private void parseShow() {
		String imageName = statement.substring(4);
		imageName = imageName.trim();
		SyntaxNode show = new ScriptShow(imageName, statementLineNumber);
		getCurrentNode().add(show);
	}

	private void parsePause() {
		String remark = statement.substring(5);
		remark = remark.trim();
		SyntaxNode pause = new ScriptPause(remark, statementLineNumber);
		getCurrentNode().add(pause);
	}

	private void getStatement() throws IOException {
		statement = null;
		String line = in.readLine();
		while (line != null) {
			lineNumber++;
			line = trimComment(line);
			line = line.trim();
			if (statement == null) {
				statementLineNumber = lineNumber;
				statement = line;
			} else {
				statement += line;
			}
			if (!statement.endsWith("+")) break; //$NON-NLS-1$
			statement = statement.substring(0, statement.length() - 1);
			line = in.readLine();
		}
	}

	private String trimComment(String line) {
		int n = line.indexOf('#');
		if (n >= 0) {
			line = line.substring(0, n);
		}
		return line;
	}

	private SyntaxNode getCurrentNode() {
		if (nodes.isEmpty()) return null;
		return (SyntaxNode) nodes.peek();
	}


}
