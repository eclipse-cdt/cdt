/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors.parser;

public class ParseException extends Exception {
	
	static final long serialVersionUID = 1;
	String message;
	int severity;
	int lineNumber;
	int startColumn;
	int endColumn;
	private int startOffset;
	private int endOffset;
	public int getEndColumn() {
		return endColumn;
	}
	public void setEndColumn(int endColumn) {
		this.endColumn = endColumn;
	}
	public int getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public int getStartColumn() {
		return startColumn;
	}
	public void setStartColumn(int startColumn) {
		this.startColumn = startColumn;
	}
	public ParseException(String message,
			int startOffset, int endOffset,
			int lineNumber, int startColumn, int endColumn, int severity) {
		super();
		this.message = message;
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.lineNumber = lineNumber;
		this.startColumn = startColumn;
		this.endColumn = endColumn;
		this.severity = severity;
	}
	public int getSeverity() {
		return severity;
	}
	public void setSeverity(int severity) {
		this.severity = severity;
	}
	public int getStartOffset() {
		return startOffset;
	}
	public int getEndOffset() {
		return endOffset;
	}

}
