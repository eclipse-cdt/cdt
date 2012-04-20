/*******************************************************************************
 *  Copyright (c) 2004, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

/**
 * A basic interface for console parsers
 */
public interface IConsoleParser {
	/**
	 * Parse one line of output.
	 *
	 * @param line
	 * @return true if line was successfully processed; skip other console parsers<p>
	 * 		   false - try other console parsers
	 */
	public boolean processLine(String line);

	/**
	 * Called to let the parser know that the end of the error stream has been reached.
	 * Can be used by the parser to flush its internal buffers.
	 */
	public void shutdown();
}
