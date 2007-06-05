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

import java.util.List;
import java.util.Vector;

/**
 * A MessageList is a collection of messages.  Its severity is the highest
 * severity of the messages it contains.
 */
public class ScriptParserMessageList {
	
	private List messages = new Vector(10); // the initial size is arbitrary
	private int severity = ScriptParserMessage.INFO;
	
	/**
	 * @param message the message to add to this MessageList
	 */
	public void add(ScriptParserMessage message) {
		messages.add(message);
		severity = Math.max(severity, message.getSeverity());
	}
	
	/**
	 * @return the severity of this MessageList
	 */
	public int getSeverity() {
		return severity;
	}
	
	/**
	 * @return the messages in this message list
	 */
	public ScriptParserMessage[] getMessages() {
		ScriptParserMessage[] result = new ScriptParserMessage[messages.size()];
		messages.toArray(result);
		return result;
	}
	
}
