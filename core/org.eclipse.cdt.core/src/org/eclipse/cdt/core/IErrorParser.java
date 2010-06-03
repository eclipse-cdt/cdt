/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core;

/**
 * Interface for error parser to parse build output to produce Errors, Warnings or Infos in Problems View.
 */
public interface IErrorParser {
	/**
	 * Finds error or warnings on the given line
	 * 
	 * @param line - line to process
	 * @param eoParser - {@link ErrorParserManager}
	 * @return {@code true} if the parser found a problem reported in output.
	 *    More accurately, {@code true} will consume the line (prevent other parsers from seeing it)
	 *    and {@code false} won't (the line will be handed to the next parser). 
	 */
	boolean processLine(String line, ErrorParserManager eoParser);

}

