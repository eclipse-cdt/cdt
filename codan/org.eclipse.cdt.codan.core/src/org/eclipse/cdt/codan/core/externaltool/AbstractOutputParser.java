/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.externaltool;

/**
 * Parses the output of an external tool.
 *
 * @author alruiz@google.com (Alex Ruiz)
 *
 * @since 2.1
 */
public abstract class AbstractOutputParser {
	/**
	 * Parses one line of output. Implementations are free to create markers from the information
	 * retrieved from the parsed output.
	 * @param line the line to parse.
	 * @return {@code true} if the line was successfully parsed; {@code false} otherwise.
	 * @throws InvocationFailure if the output indicates that the invocation of the external tool
	 *         failed.
	 */
	public abstract boolean parse(String line) throws InvocationFailure;

	/**
	 * Resets the value of this parser, usually after the execution of the external tool is
	 * finished.
	 */
	public abstract void reset();
}
