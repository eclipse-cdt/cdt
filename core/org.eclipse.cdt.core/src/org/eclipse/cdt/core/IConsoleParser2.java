/*******************************************************************************
 *  Copyright (c) 2015 Ericsson and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.core;

/**
 * Interface for console parsers. Adds knowledge about whether the line comes
 * from stdout or stderr.
 *
 * @since 5.12
 */
public interface IConsoleParser2 extends IConsoleParser {
	/**
	 * Parse one line of output.
	 *
	 * @param line
	 * @param isErrorStream whether or not the line comes from the error stream or the output stream.
	 * @return true if line was successfully processed; skip other console parsers<p>
	 * 		   false - try other console parsers
	 */
	public boolean processLine(String line, boolean isErrorStream);
}
