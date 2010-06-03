/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - initial API and implementation,  @author Doug Schaefer
 *     Warren Paul (Nokia) - Bug 178124, have processLine return true if processed.
 *******************************************************************************/

package org.eclipse.cdt.core.errorparsers;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;

/**
 * Abstract Error Parser that implements simple line processing using patterns array
 * @since 5.1
 */
public class AbstractErrorParser implements IErrorParser {

	private ErrorPattern[] patterns;
	
	protected AbstractErrorParser(ErrorPattern[] patterns) {
		this.patterns = patterns;
	}
	
	/**
	 * @param line - line of the input
	 * @param epManager - error parsers manager
	 * @return true if error parser recognized and accepted line, false otherwise
	 */
	public boolean processLine(String line, ErrorParserManager epManager) {
		for (int i = 0; i < patterns.length; ++i)
			if (patterns[i].processLine(line, epManager))
				return true;
		return false;
	}
}
