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

import java.text.MessageFormat;

/**
 * A message may indicate an error, warning or informational bit of information.
 * It may also contain data which may be retrieved.
 */
public class ScriptParserMessage {
	
	public static final int INFO = 0;
	public static final int WARNING = 1;
	public static final int ERROR = 2;
	public static final int SEVERE = 3;
	private int severity = 0;
	private String text = ""; //$NON-NLS-1$
	private Object[] data = new Object[0];
	private int lineNumber;
	
	/**
	 * Creates a new message with the given severity and text.  The message is 
	 * not bound to any data values.
	 * @param lineNumber the line number to which this message applies.
	 * @param severity the severity of the message.  Must be one of INFO, WARNING, 
	 * ERROR, or SEVERE.
	 * @param text the text of the message in the form processable by MessageFormat.
	 * @see MessageFormat
	 * @throws IllegalArgumentException if the severity is invalid.
	 */
	public ScriptParserMessage(int lineNumber, int severity, String text) {
		if (severity < INFO || severity > SEVERE) throw new IllegalArgumentException();
		this.lineNumber = lineNumber;
		this.severity = severity;
		this.text = text;
	}
	
	/**
	 * Creates a new message with the given severity and text.  The message is 
	 * bound to the given data values.
	 * @param lineNumber the line number to which this message applies.
	 * @param severity the severity of the message.  Must be one of INFO, WARNING, 
	 * ERROR, or SEVERE.
	 * @param text the text of the message in the form processable by MessageFormat.
	 * @see MessageFormat
	 * @param data the data values to which the message will be bound.
	 * @throws IllegalArgumentException if the severity is invalid.
	 */
	public ScriptParserMessage(int lineNumber, int severity, String text, Object[] data) {
		this.lineNumber = lineNumber;
		this.severity = severity;
		this.text = text;
		bind(data);
	}
	
	/**
	 * @return the severity of the message.
	 */
	public int getSeverity() {
		return severity;
	}
	
	/**
	 * @return the unbound text of the message.
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * @return the data values that this message is bound to.  If the message is
	 * unbound then this will return null.
	 */
	public Object[] getData() {
		Object[] result = new Object[data.length];
		System.arraycopy(result, 0, data, 0, data.length);
		return result;
	}
	
	/**
	 * @return Returns the line number of this message.
	 */
	public int getLineNumber() {
		return lineNumber;
	}
	
	/**
	 * Binds the data values to the message.  It does not perform any subtitutions
	 * at this time.  A shallow copy of the array is made.
	 * @param data the values to be bound to the message
	 * @return the message itself.
	 */
	public ScriptParserMessage bind(Object[] data) {
		this.data = new Object[data.length];
		System.arraycopy(data, 0, this.data, 0, data.length);
		return this;
	}
	
	/**
	 * @return the string result of formating the text of the message using
	 * the message data for substitution.
	 */
	public String toString() {
		String result = MessageFormat.format(text, data);
		return result;
	}
	
}
