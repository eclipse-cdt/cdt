/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - initial API and implementation
 *     Warren Paul (Nokia) - Bug 178124, have processLine return true if processed.
 *******************************************************************************/

package org.eclipse.cdt.internal.errorparsers;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;

/**
 * @author Doug Schaefer
 *
 */
public class AbstractErrorParser implements IErrorParser {

	private ErrorPattern[] patterns;
	
	protected AbstractErrorParser(ErrorPattern[] patterns) {
		this.patterns = patterns;
	}
	
	public boolean processLine(String line, ErrorParserManager eoParser) {
		for (int i = 0; i < patterns.length; ++i)
			if (patterns[i].processLine(line, eoParser))
				return true;
		return false;
	}
}
